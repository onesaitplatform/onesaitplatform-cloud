/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.persistence.mongodb;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.commons.model.TimeSeriesResult;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.exceptions.QueryNativeFormatException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.metrics.MetricQueryResolver;
import com.minsait.onesait.platform.persistence.mongodb.quasar.connector.QuasarMongoDBbHttpConnector;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplate;
import com.minsait.onesait.platform.persistence.mongodb.timeseries.MongoDBTimeSeriesProcessor;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("MongoBasicOpsDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
public class MongoBasicOpsDBRepository implements BasicOpsDBRepository {

	@Value("${onesaitplatform.database.timeseries.timezone:UTC}")
	private String timeZone;

	@Autowired
	private UtilMongoDB util;

	@Autowired
	private MongoDbTemplate mongoDbConnector;

	@Autowired
	private QuasarMongoDBbHttpConnector quasarMongoConnector;

	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;

	@Autowired
	private MongoDBTimeSeriesProcessor timeSeriesProcessor;

	@Autowired
	private MetricQueryResolver metricQueryResolver;

	private static final String QUASAR_QUERY_ERROR = "Error executing query in Quasar: {}";
	private static final String UPDATE = "update";
	private static final String DB_PERSISTENCE_EXCEPTION = "DBPersistenceException: {}";

	private static final List<String> METRICS_ONTOLOGIES = Arrays.asList(new String[] { "MetricsOntology",
			"MetricsOperation", "MetricsApi", "MetricsControlPanel", "MetricsQueriesControlPanel" });

	public static final String METRICS_BASE = "MetricsBase";

	@Getter
	@Setter
	private long queryExecutionTimeout = 10000;

	private final int queryDefaultLimit = 1000;

	@Autowired
	private IntegrationResourcesService resourcesServices;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private int getMaxRegisters() {
		try {
			return ((Integer) resourcesServices.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
					.intValue();
		} catch (final Exception e) {
			return queryDefaultLimit;
		}
	}

	private long getExecutionTimeout() {
		try {
			final Long executionTiemout = ((Long) resourcesServices.getGlobalConfiguration().getEnv().getDatabase()
					.get("execution-timeout")).longValue();
			log.info("Execution timeout is {}", executionTiemout);
			return executionTiemout;
		} catch (final Exception e) {
			log.error("Error retrieving execution timeout from central config");
			return queryExecutionTimeout;
		}
	}

	@Override
	public String insert(String ontology, String instance) {
		log.debug("insertInstance", ontology, instance);
		try {
			if (ontologyTimeSeriesRepository.isTimeSeries(ontology)) {
				final List<TimeSeriesResult> result = timeSeriesProcessor
						.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), ontology, instance);
				return objectMapper.writeValueAsString(result);
			} else {
				final ObjectId objectId = mongoDbConnector.insert(Tenant2SchemaMapper.getRtdbSchema(), ontology,
						util.prepareQuotes(instance));
				return objectId.toString();
			}
		} catch (final javax.persistence.PersistenceException | JsonProcessingException e) {
			log.error("Error inserting Instance", e);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		final ArrayList<String> dataToInsert = new ArrayList<>(instances.size());

		for (final String document : instances) {
			dataToInsert.add(util.prepareQuotes(document));
		}

		final ComplexWriteResult result = new ComplexWriteResult();
		try {
			if (ontologyTimeSeriesRepository.isTimeSeries(ontology)) {
				// new
				final List<TimeSeriesResult> data = new ArrayList<>();
				for (final String instance : instances) {
					data.addAll(timeSeriesProcessor.processTimeSerie(Tenant2SchemaMapper.getRtdbSchema(), ontology,
							instance));
				}
				// final List<TimeSeriesResult> data =
				// timeSeriesProcessor.processTimeSerie(ontology, instances.get(0));

				result.setType(ComplexWriteResultType.TIME_SERIES);
				result.setData(data);
			} else {
				final List<BulkWriteResult> data = mongoDbConnector.bulkInsert(Tenant2SchemaMapper.getRtdbSchema(),
						ontology, dataToInsert, order, includeIds);

				result.setType(ComplexWriteResultType.BULK);
				result.setData(data);
				result.setTotalWritten(data.size());
			}
		} catch (final javax.persistence.PersistenceException e) {
			log.error("insertBulk", e);
			throw new DBPersistenceException(e);
		}

		return result;

	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		log.debug("deleteNative", collection, query);
		return mongoDbConnector.remove(Tenant2SchemaMapper.getRtdbSchema(), collection, query, includeIds);
	}

	@Override
	public MultiDocumentOperationResult delete(String collection, boolean includeIds) {
		return deleteNative(collection, "{}", includeIds);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String collection, String objectId) {
		log.debug("deleteNativeById", collection, objectId);
		return deleteNative(collection, "{\"_id\": { \"$oid\" : \"" + objectId + "\" }}", false);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String updateQuery, boolean includeIds) {
		String query = null;
		String dataToUpdate = null;

		try {
			if (updateQuery.indexOf(".update(") != -1) {
				updateQuery = util.getQueryContent(updateQuery);
			}

			final int endOfQuery = endOfQuery(updateQuery);
			query = updateQuery.substring(0, endOfQuery + 1);

			dataToUpdate = updateQuery.substring(endOfQuery + 1, updateQuery.length());
			dataToUpdate = dataToUpdate.substring(dataToUpdate.indexOf(',') + 1, dataToUpdate.length());

			return updateNative(collection, query, dataToUpdate, includeIds);
		} catch (final DBPersistenceException e) {
			log.error(UPDATE, e);
			throw e;
		} catch (final Exception e) {
			log.error(UPDATE, e);
			throw new DBPersistenceException("Error on updating native", e);
		}

	}

	private int endOfQuery(String query) {
		int i = -1;
		boolean found = false;
		int openBracketCount = 0;
		int closeBracketCount = 0;
		while (i < query.length() && !found) {
			i++;
			final char c = query.charAt(i);
			if (c == '{') {
				openBracketCount++;
			} else if (c == '}') {
				closeBracketCount++;
			}
			found = openBracketCount > 0 && openBracketCount == closeBracketCount;

		}
		if (!found) {
			throw new InvalidParameterException("Bad formed updated query");
		}
		return i;
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String dataToUpdate,
			boolean includeIds) {
		log.debug(UPDATE, collection, query, dataToUpdate);
		return mongoDbConnector.update(Tenant2SchemaMapper.getRtdbSchema(), collection, query, dataToUpdate, true,
				includeIds);
		//
		// FIXME: Update contextData
		/*
		 * List<String> ids = new ArrayList<String>(); BasicDBObject updatedInstance =
		 * (BasicDBObject) JSON.parse(util.prepareQuotes(data)); for (BasicDBObject
		 * document : cursor) { BasicDBObject contextData = (BasicDBObject)
		 * document.get("contextData"); BasicDBObject updatedDocument = new
		 * BasicDBObject(); updatedDocument.append("contextData", contextData); for
		 * (String key : updatedInstance.keySet()) { updatedDocument.append(key,
		 * updatedInstance.get(key)); }
		 * mongoDbConnector.replace(Tenant2SchemaMapper.getRtdbSchema(), collection,
		 * document, updatedDocument);
		 * ids.add(util.getObjectIdString(document.getObjectId("_id"))); } return ids;
		 */
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queriesList, boolean includeIds) {
		log.debug(UPDATE, collection, queriesList);

		String[] split = queriesList.split("db.".concat(collection));
		List<String> queries = new ArrayList<>();

		for (int i = 1; i < split.length; i++) {
			String queryAux = null;
			if (split[i].endsWith(","))
				queryAux = "db.".concat(collection).concat(split[i].substring(0, split[i].length() - 1));
			else
				queryAux = "db.".concat(collection).concat(split[i]);

			queries.add(queryAux);
		}

		final ComplexWriteResult result = new ComplexWriteResult();
		List<WriteModel<BasicDBObject>> bulkWrites = new ArrayList<>();
		List<String> instances = new ArrayList<>();
		for (String query : queries) {
			String updateQuery = null;
			if (query.indexOf(".update(") != -1) {
				updateQuery = util.getQueryContent(query);
			}

			final int endOfQuery = endOfQuery(updateQuery);
			query = updateQuery.substring(0, endOfQuery + 1);

			String dataToUpdate = updateQuery.substring(endOfQuery + 1, updateQuery.length());
			dataToUpdate = dataToUpdate.substring(dataToUpdate.indexOf(',') + 1, dataToUpdate.length());

			UpdateOptions options = new UpdateOptions();

			if (dataToUpdate.contains("{\"upsert\":true}")) {
				options.upsert(true);
				dataToUpdate = dataToUpdate.replace(",{\"upsert\":true}", "");
			}

			UpdateManyModel upsert = new UpdateManyModel(Document.parse(query),
					new Document("$set", Document.parse(dataToUpdate)), options);
			if (dataToUpdate.contains("$set:")) {
				upsert = new UpdateManyModel(Document.parse(query), Document.parse(dataToUpdate), options);
			}
			instances.add(dataToUpdate);
			bulkWrites.add(upsert);

		}

		final List<BulkWriteResult> data = mongoDbConnector.bulkUpsert(Tenant2SchemaMapper.getRtdbSchema(), collection,
				bulkWrites, includeIds, false, instances);

		result.setType(ComplexWriteResultType.BULK);
		result.setData(data);
		return result;
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String collection, String objectId,
			String bodyData) {
		final String updateQuery = "db." + collection + ".update({\"_id\": {\"$oid\" : \"" + objectId + "\" }}, {$set:"
				+ bodyData + " })";
		return updateNative(collection, updateQuery, false);
	}

	@Override
	public String queryNativeAsJson(String collection, String query) {
		return queryNativeAsJson(collection, query, 0, getMaxRegisters());
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {

		final String preparedQuery = query.replace(" ", "");
		if (preparedQuery.contains("db." + ontology + ".count(")) {
			log.debug("countNativeAsJson", query, ontology);
			return "[ {\"value\": " + Long.toString(countNative(ontology, query)) + "}]";
		} else {
			log.debug("queryNativeAsJson", query, ontology);

			try {
				return this.serializeMongoIterableOfBasicDBObject(queryNativeMongo(ontology, query, offset, limit));
			} catch (final javax.persistence.PersistenceException e) {
				log.error("find", e, query, ontology);
				throw new DBPersistenceException(e);
			}
		}
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		return queryNative(ontology, query, 0, getMaxRegisters());
	}

	/**
	 * this method converts a query in shell language to Java representation
	 * .limit() .sort() projections skip
	 *
	 */
	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		log.debug("queryNative", query, ontology);
		final List<String> result = new ArrayList<>();
		try {
			final MongoIterable<BasicDBObject> cursor = queryNativeMongo(ontology, query, offset, limit);
			for (final BasicDBObject obj : cursor) {
				result.add(obj.toJson());
			}
			return result;
		} catch (final DBPersistenceException e) {
			log.error("find", e, query, ontology);
			throw e;
		} catch (final Exception e) {
			log.error("find", e, query, ontology);
			throw new DBPersistenceException(e);
		}
	}

	private MongoIterable<BasicDBObject> queryNativeMongo(String ontology, String query, int offset, int limit) {
		log.debug("queryNativeMongo", query, ontology);
		try {
			log.info("Executing query: {}", query);
			final MongoQueryAndParams mc = new MongoQueryAndParams();
			mc.parseQuery(query, limit, offset);
			if (mc.getLimit() == 0) {
				mc.setLimit(getMaxRegisters());
			}
			return mongoDbConnector.find(Tenant2SchemaMapper.getRtdbSchema(), ontology, mc, getExecutionTimeout());
		} catch (final QueryNativeFormatException e) {
			log.error("Error: ", e, query, ontology);
			throw new QueryNativeFormatException(e);
		} catch (final DBPersistenceException e) {
			log.error("find", e, query, ontology);
			throw e;
		} catch (final Exception e) {
			log.error("find", e, query, ontology);
			throw new DBPersistenceException("Error querying native", e);
		}
	}

	@Override
	public String findById(String collection, String objectId) {
		try {
			final BasicDBObject o = mongoDbConnector.findById(Tenant2SchemaMapper.getRtdbSchema(), collection,
					objectId);
			if (o != null) {
				return o.toJson();
			}
			return null;
		} catch (final Exception e) {
			log.error("findById", e, objectId);
			throw new DBPersistenceException("Error finding by id", e);
		}
	}

	@Override
	public String findAllAsJson(String ontology) {
		return this.serializeListOfString(findAll(ontology));
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		return this.serializeListOfString(findAll(ontology, limit));

	}

	private String serializeMongoIterableOfBasicDBObject(MongoIterable<BasicDBObject> lObjs) {
		StringBuilder buf = new StringBuilder();

		boolean first = true;
		buf.append("[ ");

		for (final BasicDBObject o : lObjs) {
			if (first) {
				first = false;
			} else {
				buf.append(" , ");
			}

			buf.append(o.toJson());
		}
		buf.append("]");

		return buf.toString();
	}

	private String serializeListOfString(List<String> lObjs) {
		StringBuilder buf = new StringBuilder();

		boolean first = true;
		buf.append("[ ");

		for (final String o : lObjs) {
			if (first) {
				first = false;
			} else {
				buf.append(" , ");
			}

			buf.append(o.toString());
		}
		buf.append("]");

		return buf.toString();
	}

	@Override
	public List<String> findAll(String collection) {
		return findAll(collection, getMaxRegisters());
	}

	@Override
	public List<String> findAll(String collection, int limit) {
		final List<String> result = new ArrayList<>();
		log.debug("findAll", collection, limit);
		for (final BasicDBObject obj : mongoDbConnector.findAll(Tenant2SchemaMapper.getRtdbSchema(), collection, 0,
				limit, getExecutionTimeout())) {
			result.add(obj.toJson());
		}
		return result;
	}

	@Override
	public long count(String collectionName) {
		return mongoDbConnector.count(Tenant2SchemaMapper.getRtdbSchema(), collectionName, "{}");
	}

	@Override
	public long countNative(String collectionName, String query) {
		return mongoDbConnector.count(Tenant2SchemaMapper.getRtdbSchema(), collectionName, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		try {
			if (isMetricsOntology(ontology)) {
				return processMetricsQuery(ontology, query);
			}
			return quasarMongoConnector.queryAsJson(ontology, query, 0, getMaxRegisters());
		} catch (final DBPersistenceException e) {
			log.error(DB_PERSISTENCE_EXCEPTION, e.getMessage(), e);
			throw e;
		} catch (final Exception e) {
			log.error(QUASAR_QUERY_ERROR, query, e);
			throw new DBPersistenceException(QUASAR_QUERY_ERROR + query, e);
		}
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		try {
			return quasarMongoConnector.queryAsTable(query, 0, getMaxRegisters());
		} catch (final DBPersistenceException e) {
			log.error(DB_PERSISTENCE_EXCEPTION, e.getMessage(), e);
			if (e.getCause().getClass().equals(ResourceAccessException.class)) {
				mongoDbConnector.createCollection(Tenant2SchemaMapper.getRtdbSchema(), ontology);
				return "{}";
			} else {
				throw e;
			}
		} catch (final Exception e) {
			log.error(QUASAR_QUERY_ERROR + query, e);
			throw new DBPersistenceException(QUASAR_QUERY_ERROR + query, e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		try {
			if (isMetricsOntology(ontology)) {
				return processMetricsQuery(ontology, query);
			}
			return quasarMongoConnector.queryAsJson(ontology, query, offset, getMaxRegisters());
		} catch (final QueryNativeFormatException e) {
			log.error("QueryNativeFormatException: {}", e.getMessage(), e);
			throw e;
		} catch (final DBPersistenceException e) {
			log.error(DB_PERSISTENCE_EXCEPTION, e.getMessage(), e);
			if (e.getCause().getClass().equals(ResourceAccessException.class)
					& !mongoDbConnector.collectionExists(Tenant2SchemaMapper.getRtdbSchema(), ontology)) {
				mongoDbConnector.createCollection(Tenant2SchemaMapper.getRtdbSchema(), ontology);
				return "{}";
			} else {
				throw e;
			}
		} catch (final Exception e) {
			log.error(QUASAR_QUERY_ERROR, query, e);
			throw new DBPersistenceException(QUASAR_QUERY_ERROR + query, e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		try {
			if (isMetricsOntology(ontology)) {
				return processMetricsQuery(ontology, query);
			}
			return quasarMongoConnector.queryAsJson(ontology, query, offset, limit > 0 ? limit : getMaxRegisters());
		} catch (final QueryNativeFormatException e) {
			log.error("QueryNativeFormatException: {}" + e.getMessage(), e);
			throw e;
		} catch (final DBPersistenceException e) {
			log.error(DB_PERSISTENCE_EXCEPTION, e.getMessage(), e);
			if (e.getCause().getClass().equals(ResourceAccessException.class)
					& !mongoDbConnector.collectionExists(Tenant2SchemaMapper.getRtdbSchema(), ontology)) {
				mongoDbConnector.createCollection(Tenant2SchemaMapper.getRtdbSchema(), ontology);
				return "{}";
			} else {
				throw e;
			}
		} catch (final Exception e) {
			log.error(QUASAR_QUERY_ERROR, query, e);
			throw new DBPersistenceException(QUASAR_QUERY_ERROR + query, e);
		}
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		try {
			return quasarMongoConnector.queryAsTable(query, offset, getMaxRegisters());
		} catch (final DBPersistenceException e) {
			log.error(DB_PERSISTENCE_EXCEPTION, e.getMessage(), e);
			throw e;
		} catch (final Exception e) {
			log.error(QUASAR_QUERY_ERROR, query, e);
			throw new DBPersistenceException(QUASAR_QUERY_ERROR + query, e);
		}
	}

	private boolean isMetricsOntology(String ontology) {
		return METRICS_ONTOLOGIES.contains(ontology);
	}

	private String processMetricsQuery(String ontology, String query) throws DBPersistenceException {
		try {
			final int indexOfHours = query.indexOf("HOURS") != -1 ? 1 : -1;
			final int indexOfDays = query.indexOf("DAYS") != -1 ? 1 : -1;
			final int indexOfMonths = query.indexOf("MONTHS") != -1 ? 1 : -1;

			final int indexesSum = indexOfHours + indexOfDays + indexOfMonths;

			if (indexesSum == -3) {// No interval selected
				throw new DBPersistenceException(
						"Metrics query needs to indicate temporal interval (HOUS, DAYS or MONTHS)");
			} else if (indexesSum != -1) { // Multiple intervals selected
				throw new DBPersistenceException(
						"Metrics query needs to indicate only one temporal interval (HOUS, DAYS or MONTHS)");
			}

			final Map<String, String> queryData = metricQueryResolver.buildMongoDBQueryStatement(query);

			if (!ontology.equals(queryData.get(MetricQueryResolver.ONTOLOGY_NAME))) {
				throw new DBPersistenceException(
						"Ontology in query statement is not the indicated as selected ontology");
			}

			final List<String> data = this.queryNative(ontology, queryData.get(MetricQueryResolver.STATEMENT));

			metricQueryResolver.loadMetricsBase(this);

			final List<String> cerosInInterval = this.queryNative(METRICS_BASE,
					queryData.get(MetricQueryResolver.STATEMENT_CEROS_COMPLETION));

			final String result = metricQueryResolver.buildUnifiedResponse(data, cerosInInterval,
					queryData.get(MetricQueryResolver.SELECT_ITEMS));

			return result;

		} catch (final Exception e) {
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateQuery)
			throws DBPersistenceException {
		String query = null;

		try {
			if (updateQuery.indexOf(".update(") != -1) {
				updateQuery = util.getQueryContent(updateQuery);
			}

			final int endOfQuery = endOfQuery(updateQuery);
			// query = updateQuery.substring(0, updateQuery.indexOf("},") + 1);
			query = updateQuery.substring(0, endOfQuery + 1);

			return this.queryNative(ontology, query, 0, 0);

		} catch (final DBPersistenceException e) {
			log.error("update", e);
			throw e;
		} catch (final Exception e) {
			log.error("update", e);
			throw new DBPersistenceException("Error on updating native", e);
		}
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		return this.queryUpdateTransactionCompensationNative(collection, query);
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {

		return findById(ontologyName, objectId);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		try {
			if (query.indexOf("db.") != -1) {
				query = util.getQueryContent(query);
			}
			return this.queryNative(collection, query, 0, 0);
		} catch (final DBPersistenceException e) {
			log.error("update", e);
			throw e;
		} catch (final Exception e) {
			log.error("update", e);
			throw new DBPersistenceException("Error on updating native", e);
		}
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) throws DBPersistenceException {
		try {
			return this.queryNative(collection, "{}", 0, 0);
		} catch (final DBPersistenceException e) {
			log.error("update", e);
			throw e;
		} catch (final Exception e) {
			log.error("update", e);
			throw new DBPersistenceException("Error on updating native", e);
		}
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		return findById(collection, objectId);
	}
}

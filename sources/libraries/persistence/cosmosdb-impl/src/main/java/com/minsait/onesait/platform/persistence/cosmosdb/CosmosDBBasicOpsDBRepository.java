/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.cosmosdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.bulkexecutor.BulkImportResponse;
import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.bean.CosmosDBBulkManager;
import com.minsait.onesait.platform.persistence.cosmosdb.utils.CosmosDBUtils;
import com.minsait.onesait.platform.persistence.cosmosdb.utils.sql.CosmosDBSQLUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Component("CosmosDBBasicOpsDBRepository")
@Slf4j
public class CosmosDBBasicOpsDBRepository implements BasicOpsDBRepository {
	@Autowired
	private DocumentClient cosmosClient;
	private static final String NOT_IMPLEMENTED = "Not implemented";

	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private CosmosDBSQLUtils sqlUtils;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private IntegrationResourcesService resourcesServices;

	@Autowired
	private CosmosDBBulkManager bulkManager;

	private static final int CONCURRENCY_MAX = 20;

	private static final int QUERY_DEFAULT_LIMIT = 1000;
	private static final String LIMIT = " LIMIT ";
	private static final String OFFSET = " OFFSET ";

	private int getMaxRegisters() {
		try {
			return ((Integer) resourcesServices.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
					.intValue();
		} catch (final Exception e) {
			return QUERY_DEFAULT_LIMIT;
		}
	}

	@Override
	public String insert(String ontology, String instance) {
		try {
			final Document doc = new Document(instance);
			return cosmosClient.createDocument(CosmosDBUtils.collectionLink(ontology), doc, new RequestOptions(), false)
					.getResource().getId();
		} catch (final DocumentClientException e) {
			log.error("Error storing document in Cosmos DB", e);
			throw new DBPersistenceException("Could not insert ontology", e);
		}
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {

		final DocumentBulkExecutor executor = bulkManager.get(ontology);
		try {
			final BulkImportResponse response = executor.importAll(instances, false, false, CONCURRENCY_MAX);
			logBulkImportResponse(response, ontology);
			final List<BulkWriteResult> list = new ArrayList<>();
			return ComplexWriteResult.builder().totalWritten(response.getNumberOfDocumentsImported())
					.failedData(response.getBadInputDocuments()).type(ComplexWriteResultType.BULK).data(list).build();
		} catch (final DocumentClientException e) {
			log.error("Failed operation insertBulk on CosmosDB", e);
			throw new DBPersistenceException("Failed bulk import", e);
		}
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String query, boolean includeIds) {
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		// TO-DO BULK UPDATE

		final String fetchQuery = CosmosDBUtils.QUERY_GENERIC + sqlUtils.extractQueryUpdate(query);
		try {
			final DocumentBulkExecutor executor = bulkManager.get(ontology);
			final ArrayNode fetched = mapper.readValue(this.queryNativeAsJson(ontology, fetchQuery), ArrayNode.class);
			if (fetched.size() > 0) {
				final List<String> toUpdate = sqlUtils.updateInstancesFromStatement(fetched, query);
				final BulkImportResponse response = executor.importAll(toUpdate, true, false, CONCURRENCY_MAX);
				logBulkImportResponse(response, ontology);
				result.setCount(response.getNumberOfDocumentsImported());
				if (includeIds) {
					toUpdate.stream().map(Document::new).forEach(d -> result.getIds().add(d.getId()));
				}
			}

		} catch (final IOException | DocumentClientException e) {
			log.error("Error while update SQL on Cosmosdb", e);
			throw new DBPersistenceException("Error while updating SQL");
		}

		return result;

	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		final Ontology o = ontologyRepository.findByIdentification(collection);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		final String fetchQuery = CosmosDBUtils.QUERY_GENERIC + sqlUtils.extractQueryDelete(query);

		try {
			final ArrayNode array = mapper.readValue(this.querySQLAsJson(collection, fetchQuery), ArrayNode.class);
			array.forEach(d -> {
				final Document doc = new Document(d.toString());
				deleteSingleDocument(doc, o.getPartitionKey());
				if (includeIds) {
					result.getIds().add(doc.getId());
				}
				result.setCount(result.getCount() + 1);
			});
		} catch (final IOException e) {
			log.error("Error while deleting SQL on Cosmosdb", e);
			throw new DBPersistenceException("Error while deleting SQL");
		}
		return result;

	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		final List<String> results = new ArrayList<>();
		cosmosClient.queryDocuments(CosmosDBUtils.collectionLink(ontology), query, CosmosDBUtils.feedOptions())
				.getQueryIterator().forEachRemaining(d -> results.add(d.toJson()));
		return results;
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		query = query + OFFSET + offset + LIMIT + limit;
		return queryNative(ontology, query);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return this.querySQLAsJson(ontology, query);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		query = query + OFFSET + offset + LIMIT + limit;
		return this.querySQLAsJson(ontology, query);
	}

	@Override
	public String findById(String ontology, String objectId) {
		try {
			final Iterator<Document> it = cosmosClient.queryDocuments(CosmosDBUtils.collectionLink(ontology),
					CosmosDBUtils.queryId(objectId), CosmosDBUtils.feedOptions()).getQueryIterator();
			if (it.hasNext()) {
				return it.next().toJson();
			} else {
				return "{}";
			}
		} catch (final Exception e) {
			log.error("Could not execute find by id on Cosmos DB", e);
			throw new DBPersistenceException("Could not find document by id", e);
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		final ArrayNode array = mapper.createArrayNode();
		cosmosClient.queryDocuments(CosmosDBUtils.collectionLink(ontology), query, CosmosDBUtils.feedOptions())
				.getQueryIterator().forEachRemaining(d -> {
					array.add(d.toObject(JsonNode.class));
				});
		return array.toString();
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return querySQLAsJson(ontology, preparedQuery(query, offset, -1));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return querySQLAsJson(ontology, preparedQuery(query, offset, limit));
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String findAllAsJson(String ontology) {
		final ArrayNode array = mapper.createArrayNode();
		cosmosClient.readDocuments(CosmosDBUtils.collectionLink(ontology), CosmosDBUtils.feedOptions())
				.getQueryIterator().forEachRemaining(d -> array.add(d.toObject(JsonNode.class)));
		return array.toString();
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		return findAllAsJson(ontology);
	}

	@Override
	public List<String> findAll(String ontology) {
		final List<String> result = new ArrayList<>();
		cosmosClient.readDocuments(CosmosDBUtils.collectionLink(ontology), CosmosDBUtils.feedOptions())
				.getQueryIterator().forEachRemaining(d -> result.add(d.toJson()));
		return result;
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		final List<String> results = findAll(ontology);
		if (!results.isEmpty() && results.size() >= limit) {
			return results.subList(0, limit);
		} else {
			return results;
		}
	}

	@Override
	public long count(String ontology) {
		final FeedResponse<Document> response = cosmosClient.queryDocuments(CosmosDBUtils.collectionLink(ontology),
				CosmosDBUtils.countQuery(), CosmosDBUtils.feedOptions());
		return response.getQueryIterator().next().getInt(CosmosDBUtils.AGGREGATE_FIELD);
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		return deleteNative(ontology, CosmosDBUtils.QUERY_GENERIC, includeIds);

	}

	@Override
	public long countNative(String collectionName, String query) {
		final FeedResponse<Document> response = cosmosClient.queryDocuments(
				CosmosDBUtils.collectionLink(collectionName), CosmosDBUtils.countQuery() + " " + query,
				CosmosDBUtils.feedOptions());
		return response.getQueryIterator().next().getInt(CosmosDBUtils.AGGREGATE_FIELD);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {

		final Document doc = new Document(findById(ontologyName, objectId));
		deleteSingleDocument(doc, ontologyRepository.findByIdentification(ontologyName).getPartitionKey());
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setIds(Arrays.asList(objectId));
		result.setCount(1);
		result.setStrIds(objectId);
		return result;

	}

	private void deleteSingleDocument(Document document, String partitionKey) {
		try {
			cosmosClient.deleteDocument(document.getSelfLink(),
					CosmosDBUtils.requestOptions(document.toJson(), partitionKey));
		} catch (final DocumentClientException e) {
			log.error("Could not delete document on Cosmos DB");
			throw new DBPersistenceException("Could not delete by id", e);
		}
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		try {
			final Document payload = new Document(body);
			if (StringUtils.isEmpty(payload.getId())) {
				payload.setId(objectId);
			}
			final String id = cosmosClient
					.replaceDocument(CosmosDBUtils.documentLink(ontologyName, objectId), payload, new RequestOptions())
					.getResource().getId();
			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setIds(Arrays.asList(id));
			result.setCount(1);
			result.setStrIds(id);
			return result;
		} catch (final DocumentClientException e) {
			log.error("Couldn't update instance with id {} on Cosmos DB", objectId, e);
			throw new DBPersistenceException("Couldn't update instance", e);
		}

	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	public String preparedQuery(String query, int offset, int limit) {
		final Pattern pattern = Pattern.compile(".*(limit|LIMIT \\d+)");
		final Matcher matcher = pattern.matcher(query);
		boolean changed = false;
		while (matcher.find()) {
			final String group = matcher.group(1);
			if (!query.toLowerCase().contains(" offset ")) {
				query = query.replace(group, OFFSET + offset + " " + group);
			}
			changed = true;
		}
		if (!changed) {
			limit = limit > 0 ? limit : getMaxRegisters();
			return query + OFFSET + offset + LIMIT + limit;
		} else {
			return query;
		}
	}

	private void logBulkImportResponse(BulkImportResponse response, String ontology) {
		final int inserted = response.getNumberOfDocumentsImported();
		final long timeTakenMillis = response.getTotalTimeTaken().getNano() / 1000000;
		final long importsPerMillisecond = inserted / timeTakenMillis;
		log.info(
				"Inserted {} documents for ontology {} on CosmosDb with a total RU consumed of {}. Total time taken {} milliseconds with avg {} imports/millisecond",
				inserted, ontology, response.getTotalRequestUnitsConsumed(), timeTakenMillis, importsPerMillisecond);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

}

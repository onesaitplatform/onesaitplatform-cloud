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
package com.minsait.onesait.platform.persistence.timescaledb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.commons.model.TimeSeriesResult;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.models.ErrorResult;
import com.minsait.onesait.platform.persistence.timescaledb.config.TimescaleDBConfiguration;
import com.minsait.onesait.platform.persistence.timescaledb.parser.JSONTimescaleResultsetExtractor;
import com.minsait.onesait.platform.persistence.timescaledb.processor.TimescaleDBTimeSeriesProcessor;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

@Component("TimescaleDBBasicOpsDBRepository")
@Slf4j
public class TimescaleDBBasicOpsDBRepository implements BasicOpsDBRepository {

	private static final String ONTOLOGY_NOTNULLEMPTY = "Ontology can't be null or empty";
	private static final String QUERY_NOTNULLEMPTY = "Query can't be null or empty";
	private static final String OFFSET_GREATERTHANZERO = "Offset must be greater or equals to 0";
	private static final String NOT_IMPLEMENTED_METHOD = "Not implemented method";
	private static final String NOT_SUPPORTED_OPERATION = "Operation not supported for TimescaleDB Timeseries";
	private static final String SELECT_FROM = "SELECT * FROM ";
	private static final String NOT_ENABLED = "TimescaleDB is not enabled.";

	@Autowired(required = false)
	@Qualifier(TimescaleDBConfiguration.TIMESCALEDB_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate timescaleDBJdbcTemplate;

	@Autowired
	private TimescaleDBTimeSeriesProcessor timescaleDBProcessor;

	private static final int QUERY_DEFAULT_LIMIT = 1000;

	@Autowired
	private IntegrationResourcesService resourcesServices;

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
		final List<TimeSeriesResult> result = insertOperation(ontology, Collections.singletonList(instance));
		if (result == null || result.isEmpty()) {
			return "No rows were affected";
		} else {
			return result.get(0).getId() != null ? result.get(0).getId() : "1";
		}
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		final List<TimeSeriesResult> result = insertOperation(ontology, instances);
		return new ComplexWriteResult().setData(result).setType(ComplexWriteResultType.TIME_SERIES);
	}

	private List<TimeSeriesResult> insertOperation(final String ontology, final List<String> instances) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.notEmpty(instances, "Instances can't be null or empty");

			return timescaleDBProcessor.processTimeSerie(ontology, instances);

		} catch (final Exception e) {
			log.error("Error inserting bulk data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.TIMESCALE, e.getMessage()),
					"Error inserting bulk data on TimescaleDB ontology");
		}
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		// When IDs are implemented
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		// When IDs are implemented
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		// When IDs are implemented
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		return queryNative(ontology, query, 0, getMaxRegisters());
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		if (timescaleDBJdbcTemplate == null) {
			throw new DBPersistenceException(NOT_ENABLED);
		}
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(query, QUERY_NOTNULLEMPTY);
			Assert.isTrue(offset >= 0, OFFSET_GREATERTHANZERO);
			Assert.isTrue(limit >= 1, "Limit must be greater or equal to 1");

			// Parse query with JSQLParser and validate ontology
			final Statement statement = CCJSqlParserUtil.parse(query);

			final TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
			final List<String> tableNames = tablesNamesFinder.getTableList(statement);
			for (final String tableName : tableNames) {
				if (!tableName.equalsIgnoreCase(ontology)
						&& !tableName.toUpperCase().startsWith(ontology.toUpperCase() + "_")) {
					throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.TIMESCALE,
							"Selected table in query does not belong to the selected ontology."));
				}
			}
			// detect the type of query to execure right method: SELECT, UPDATE, DELETE
			if (statement instanceof Select) {
				final Optional<PlainSelect> select = parseQuery(query);
				if (select.isPresent()) {
					query = addLimit(select.get(), limit, offset).toString();
				}
				return timescaleDBJdbcTemplate.query(query, new JSONTimescaleResultsetExtractor(query));
			} else if (statement instanceof Update) {
				final int numRows = timescaleDBJdbcTemplate.update(query);
				final List<String> result = new ArrayList<>();
				result.add(String.format("{'updatedRows':%s}", numRows));
				return result;
			} else if (statement instanceof Delete) {
				final int numRows = timescaleDBJdbcTemplate.update(query);
				final List<String> result = new ArrayList<>();
				result.add(String.format("{'deletedRows':%s}", numRows));
				return result;
			}
		} catch (final Exception e) {
			log.error("Error querying data on TimescaleDB ontology {}. Query={}, Cause={}, Message={}.", ontology,
					query, e.getCause(), e.getMessage());
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.TIMESCALE, e.getMessage()),
					"Error querying data on TimescalDB ontology");
		}
		log.error("Invalid query type for TimescaleDB. Ontology={}, Query={}.", ontology, query);
		throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.TIMESCALE,
				"Error querying data on TimescalDB ontology. Invalid query type for TimescaleDB"));
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return queryNativeAsJson(ontology, query, 0, getMaxRegisters());
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		final List<String> result = this.queryNative(ontology, query, offset, limit);
		final JSONArray jsonResult = new JSONArray();
		for (final String instance : result) {
			final JSONObject obj = new JSONObject(instance);
			jsonResult.put(obj);
		}
		return jsonResult.toString();
	}

	@Override
	public String findById(String ontology, String objectId) {
		// When IDs are implemented
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		return queryNativeAsJson(ontology, query, 0, getMaxRegisters());
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return queryNativeAsJson(ontology, query, offset, getMaxRegisters());
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String findAllAsJson(String ontology) {
		return this.queryNativeAsJson(ontology, SELECT_FROM + ontology);
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		return this.queryNativeAsJson(ontology, SELECT_FROM + ontology, 0, limit);
	}

	@Override
	public List<String> findAll(String ontology) {
		return this.queryNative(ontology, SELECT_FROM + ontology);
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		return this.queryNative(ontology, SELECT_FROM + ontology, 0, limit);
	}

	@Override
	public long count(String ontology) {
		return countNative(ontology, "SELECT COUNT(*) FROM " + ontology);
	}

	@Override
	public long countNative(String collectionName, String query) {
		final JSONArray result = new JSONArray(this.queryNativeAsJson(collectionName, query));
		final Iterator<String> itr = result.getJSONObject(0).keys();
		final String key = itr.next();
		return result.getJSONObject(0).getLong(key);
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		// When IDs are implemented
		return null;
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		// When IDs are implemented
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		// When IDs are implemented
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query, offset, limit);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	private PlainSelect addLimit(final PlainSelect select, final long limit, final long offset) {
		final PlainSelect limitedSelect = addLimit(select, limit);

		if (offset > 0) {
			final boolean hasOffset = (limitedSelect.getOffset() != null
					|| (limitedSelect.getLimit() != null && limitedSelect.getLimit().getOffset() != null));
			if (hasOffset) {
				if (limitedSelect.getOffset() != null) {
					limitedSelect.getOffset().setOffset(new LongValue(offset));
				} else {
					limitedSelect.getLimit().setOffset(new LongValue(offset));
				}
			} else {
				final Offset qOffset = new Offset();
				qOffset.setOffset(new LongValue(offset));
				limitedSelect.setOffset(qOffset);
			}
		}
		return limitedSelect;
	}

	private PlainSelect addLimit(final PlainSelect select, final long limit) {
		final boolean hasLimit = (select.getLimit() != null);
		if (hasLimit) {
			final long oldLimit = ((LongValue) select.getLimit().getRowCount()).getValue();
			select.getLimit().setRowCount(new LongValue(Math.min(limit, oldLimit)));
		} else {
			final Limit qLimit = new Limit();
			qLimit.setRowCount(new LongValue(Math.max(limit, 1)));
			select.setLimit(qLimit);
		}

		return select;
	}

	private Optional<PlainSelect> parseQuery(final String query) {
		try {
			final Statement statement = CCJSqlParserUtil.parse(query);
			if (statement instanceof Select) {
				return Optional.ofNullable((PlainSelect) ((Select) statement).getSelectBody());
			} else {
				log.debug("The statement passed as argument is not a select query, returning the original");
				return Optional.empty();
			}
		} catch (final JSQLParserException e) {
			log.debug("Could not parse the query with JSQL returning the original. ", e);
			return Optional.empty();
		}
	}
}

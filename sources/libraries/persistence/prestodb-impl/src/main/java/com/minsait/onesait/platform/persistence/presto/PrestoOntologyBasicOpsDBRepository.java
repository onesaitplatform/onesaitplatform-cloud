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
package com.minsait.onesait.platform.persistence.presto;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jline.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.DBResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.model.OntologyPresto;
import com.minsait.onesait.platform.config.repository.OntologyPrestoRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.models.ErrorResult;
import com.minsait.onesait.platform.persistence.presto.generator.PrestoSQLHelper;
import com.minsait.onesait.platform.persistence.presto.generator.PrestoSQLTableReplacer;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.ColumnPresto;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;
import com.minsait.onesait.platform.persistence.presto.parser.JSONResultsetExtractor;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.create.table.ColDataType;

@Component("PrestoOntologyBasicOpsDBRepository")
@Lazy
@Slf4j
public class PrestoOntologyBasicOpsDBRepository implements PrestoOntologyOpsDBRepository {

	private static final String SELECT_COUNT = "SELECT COUNT";
	private static final String DELETE = "DELETE";
	private static final String SELECT = "SELECT";
	private static final String OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY = "Operation not supported for Presto Ontology";
	private static final String NOT_SUPPORTED_OPERATION = "Operation not supported for Presto Ontologies";
	private static final String SELECT_FROM = "SELECT * FROM ";
	private static final String ONTOLOGY_NOTNULLEMPTY = "Ontology can't be null or empty";
	private static final String NOT_IMPLEMENTED_METHOD = "Not implemented method";
	private static final String QUERY_NOTNULLEMPTY = "Query can't be null or empty";
	private static final String OFFSET_GREATERTHANZERO = "Offset must be greater or equals to 0";

	@Autowired
	private OntologyPrestoRepository ontologyPrestoRepository;

	@Autowired
	@Qualifier("PrestoDatasourceManagerImpl")
	private PrestoDatasourceManager prestoDatasourceManager;

	@Autowired
	private SQLGenerator sqlGenerator;
	
	@Autowired
	private PrestoSQLHelper prestoSQLHelper;
	
	@Value("#{'${onesaitplatform.database.excludeParse:dual}'.split(',')}")
	private List<String> excludeParse;

	private NamedParameterJdbcTemplate getJdbTemplate(final String ontology) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			final OntologyPresto op = prestoDatasourceManager.getOntologyPrestoForOntology(ontology);
			return new NamedParameterJdbcTemplate(prestoDatasourceManager
					.getDatasource(op.getDatasourceCatalog(), op.getDatasourceSchema()));
		} catch (final Exception e) {
			throw new DBPersistenceException("JdbcTemplate not found for Presto ontology: " + ontology, e);
		}
	}

	@Override
	public List<String> getTables(final String datasourceName) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String insert(final String ontology, final String instance) {
		final List<DBResult> result = insertOperation(ontology, Collections.singletonList(instance));
		if (result.isEmpty()) {
			return "No rows were affected";
		} else {
			return result.get(0).getId() != null ? result.get(0).getId() : "1";
		}
	}

	@Override
	public ComplexWriteResult insertBulk(final String ontology, final List<String> instances, final boolean order,
			final boolean includeIds) {
		final List<DBResult> result = insertOperation(ontology, instances);
		return new ComplexWriteResult().setData(result).setType(ComplexWriteResultType.BULK);
	}

	private List<DBResult> insertOperation(final String ontology, final List<String> instances) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.notEmpty(instances, "Instances can't be null or empty");

			final OntologyPresto op = prestoDatasourceManager.getOntologyPrestoForOntology(ontology);
			final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(prestoDatasourceManager
					.getDatasource(op.getDatasourceCatalog(), op.getDatasourceSchema()));

			final int affected;
			final PreparedStatement sql = sqlGenerator.buildInsert().setOntology(ontology)
					.setValuesAndColumnsForInstances(instances).generate(true);

			sql.setStatement(PrestoSQLTableReplacer.replaceTableNameInInsert(sql.getStatement(),
					ontologyPrestoRepository, excludeParse, ontology));

			affected = jdbcTemplate.update(sql.getStatement(), new MapSqlParameterSource(sql.getParams()));
				
			return IntStream.range(0, affected).mapToObj(index -> new DBResult().setOk(true))
							.collect(Collectors.toList());
		} catch (final Exception e) {
			log.error("Error inserting bulk data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.PRESTO, e.getMessage()),
					"Error inserting bulk data on Presto ontology");
		}

	}

	@Override
	public MultiDocumentOperationResult insertNative(final String ontology, final String insertStmt,
			final boolean includeIds) {
		return insertNative(ontology, new PreparedStatement(insertStmt.replaceAll(";", "")), includeIds);
	}

	private MultiDocumentOperationResult insertNative(final String ontology, final PreparedStatement ps,
			final boolean includeIds) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(ps.getStatement(), "Statement can't be null or empty");

			ps.setStatement(PrestoSQLTableReplacer.replaceTableNameInInsert(ps.getStatement(),
					ontologyPrestoRepository, excludeParse, ontology));

			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(getJdbTemplate(ontology).update(ps.getStatement(), ps.getParams()));
			return result;
		} catch (final Exception e) {
			log.error("Error updating data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.PRESTO, e.getMessage()),
					"Error inserting data into Presto ontology");
		}
	}

	@Override
	public MultiDocumentOperationResult updateNative(final String ontology, final String updateStmt,
			final boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public MultiDocumentOperationResult updateNative(final String ontology, final String query, final String data,
			final boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public MultiDocumentOperationResult deleteNative(final String ontology, final String query,
			final boolean includeIds) {
		return deleteNative(ontology, new PreparedStatement(query.replaceAll(";", "")), includeIds);
	}

	private MultiDocumentOperationResult deleteNative(final String ontology, final PreparedStatement ps,
			final boolean includeIds) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(ps.getStatement(), QUERY_NOTNULLEMPTY);

			ps.setStatement(PrestoSQLTableReplacer.replaceTableNameInDelete(ps.getStatement(),
					ontologyPrestoRepository.findOntologyPrestoByOntologyIdentification(ontology)));

			final NamedParameterJdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
			final int deleted = jdbcTemplate.update(ps.getStatement(), ps.getParams());
			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(deleted);
			return result;
		} catch (final Exception e) {
			log.error("Error removing data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.PRESTO, e.getMessage()),
					"Error removing data from Presto ontology");
		}
	}

	@Override
	public List<String> queryNative(final String ontology, final String query) {
		return this.queryNative(ontology, query, 0, prestoDatasourceManager.getQueryLimit());
	}

	@Override
	public List<String> queryNative(final String ontology, String query, final int offset, final int limit) {
		return queryNative(ontology, new PreparedStatement(query.replaceAll(";", "")), offset, limit);
	}

	private List<String> queryNative(final String ontology, PreparedStatement ps, final int offset, final int limit) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(ps.getStatement(), QUERY_NOTNULLEMPTY);
			Assert.isTrue(offset >= 0, OFFSET_GREATERTHANZERO);
			Assert.isTrue(limit >= 1, "Limit must be greater or equals to 1");

			ps.setStatement(PrestoSQLTableReplacer.replaceTableNameInSelect(ps.getStatement(), ontologyPrestoRepository,
					excludeParse, ontology));

			ps.setStatement(prestoSQLHelper.addLimit(ps.getStatement(), Math.min(limit, prestoDatasourceManager.getQueryLimit()), offset));

			final NamedParameterJdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
			return jdbcTemplate.query(ps.getStatement(), ps.getParams(), new JSONResultsetExtractor(ps.getStatement()));
		} catch (final Exception e) {
			log.error("Error querying data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.PRESTO, e.getMessage()),
					"Error querying data on Presto ontology");
		}
	}

	@Override
	public String queryNativeAsJson(final String ontology, final String query) {
		return queryNativeAsJson(ontology, new PreparedStatement(query));
	}

	private String queryNativeAsJson(final String ontology, final PreparedStatement ps) {
		return queryNativeAsJson(ontology, ps, 0,
				prestoDatasourceManager.getQueryLimit());
	}

	@Override
	public String queryNativeAsJson(final String ontology, final String originalQuery, final int offset,
			final int limit) {
		return queryNativeAsJson(ontology, new PreparedStatement(originalQuery), offset, limit);
	}

	private String queryNativeAsJson(final String ontology, final PreparedStatement ps, final int offset,
			final int limit) {
		final List<String> result = this.queryNative(ontology, ps, offset, limit);
		final JSONArray jsonResult = new JSONArray();
		for (final String instance : result) {
			final JSONObject obj = new JSONObject(instance);
			jsonResult.put(obj);
		}
		if (checkQueryIsQueryCount(ps.getStatement())) {
			return jsonResult.toString().replace("_col0", "count");
		} else {
			return jsonResult.toString();
		}
	}
	
	private boolean checkQueryIsQueryCount(String query) {
		query = query.replace("\n", "");
		query = StringUtils.normalizeSpace(query);
		if (query.toLowerCase().indexOf("select count") != -1) {
			return true;
		} else {
			return false;
		}
	};

	@Override
	public String findById(final String ontology, final String objectId) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String querySQLAsJson(final String ontology, final String query) {
		return queryNativeAsJson(ontology, query);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public String querySQLAsJson(final String ontology, final String query, final int offset) {
		return queryNativeAsJson(ontology, query, offset, prestoDatasourceManager.getQueryLimit());
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
		return this.queryNative(ontology, SELECT_FROM + ontology);
	}

	@Override
	public long count(final String ontology) {
		return countNative(ontology, "SELECT COUNT(*) FROM " + ontology);
	}

	@Override
	public MultiDocumentOperationResult delete(final String ontology, final boolean includeIds) {
		return this.deleteNative(ontology, "DELETE FROM " + ontology, includeIds);
	}

	@Override
	public long countNative(final String ontology, final String query) {
		final JSONArray result = new JSONArray(this.queryNativeAsJson(ontology, query));
		final Iterator<String> itr = result.getJSONObject(0).keys();
		final String key = itr.next();
		return result.getJSONObject(0).getLong(key);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(final String ontology, final String objectId) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));

	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(final String ontology, final String uniqueId,
			final String body) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));

	}

	@Override
	public List<String> getInstanceFromTable(final String datasource, final String query) {
		final String finalQuery = getStatementFromJson(query);
		return getInstanceFromTable(datasource, new PreparedStatement(finalQuery.replaceAll(";", "")));
	}

	private List<String> getInstanceFromTable(final String datasource, final PreparedStatement ps) {
		try {
			Assert.hasLength(datasource, "Datasource can't be null or empty");
			Assert.hasLength(ps.getStatement(), QUERY_NOTNULLEMPTY);
			if (log.isDebugEnabled()) {
				log.debug("Query requested: {}", ps.getStatement());
			}
			ps.setStatement(prestoSQLHelper.addLimit(ps.getStatement(), 
					prestoDatasourceManager.getQueryLimit()));
			final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(
					prestoDatasourceManager.getDatasource(""));
			return jdbcTemplate.query(ps.getStatement(), ps.getParams(), new JSONResultsetExtractor(ps.getStatement()));
		} catch (final Exception e) {
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.PRESTO, e.getMessage()),
					"Error getting instance data on Presto ontology");
		}
	}

	@Override
	public String getTableMetadata(final String catalog, final String schema,
			final String ontology) {
		try {
			Assert.hasLength(ontology, "Collection can't be null or empty");

			final HashMap<String, Object> map = new HashMap<>(getTableTypes(catalog, schema, ontology));
			for (final Map.Entry<String, Object> entry : map.entrySet()) {
				entry.setValue(getSQLTypeForTableType((Integer) entry.getValue()));
			}

			return new JSONObject(map).toString();
		} catch (final Exception exception) {
			throw new DBPersistenceException(exception,
					new ErrorResult(ErrorResult.PersistenceType.PRESTO, exception.getMessage()),
					"Error getting meta data on Presto ontology");
		}
	}

	private JsonArray getColumnsMetadata(final String catalog, final String schema,
			final String ontology) {
		final JsonArray columns = new JsonArray();
		java.sql.Connection conn = null;

		try {
			conn = prestoDatasourceManager.getDatasource(catalog, schema).getConnection();
			final DatabaseMetaData dbmeta = conn.getMetaData();

			String ontologyName;

			ontologyName = ontology;
			final ResultSet rs = dbmeta.getColumns(catalog, schema, ontologyName, null);
			while (rs.next()) {
				final JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("ordinal_position", rs.getString("ORDINAL_POSITION"));
				jsonObj.addProperty("column_name", rs.getString("COLUMN_NAME").trim());
				jsonObj.addProperty("data_type", rs.getString("DATA_TYPE"));
				jsonObj.addProperty("is_nullable", rs.getString("IS_NULLABLE"));
				jsonObj.addProperty("remarks", rs.getString("REMARKS"));
				columns.add(jsonObj);

			}
		} catch (final SQLException e) {
			throw new GenericRuntimeOPException("Error getting table metadata", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (final Exception e2) {
				throw new GenericRuntimeOPException("Error closing connection when getting table metadata", e2);
			}
		}

		return columns;
	}

	@Override
	public Map<String, Integer> getTableTypes(final String catalog, final String schema,
			final String ontology) {
		final Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		try {
			final JsonArray columns = getColumnsMetadata(catalog, schema, ontology);
			for (final JsonElement col : columns) {
				final JsonObject colObject = col.getAsJsonObject();
				map.put(colObject.get("column_name").getAsString(), colObject.get("data_type").getAsInt());
			}
			return map;
		} catch (final GenericRuntimeOPException e) {
			throw new GenericRuntimeOPException("Error getting table metadata", e);
		}
	}

	@Override
	public String getSqlTableDefinitionFromSchema(final String ontology, final String schema) {
		if (schema == null) {
			throw new IllegalArgumentException("Ontology schema not found in ontology");
		}
		final List<ColumnPresto> cols = generateColumns(schema);
		CreateStatement cs = sqlGenerator.buildCreate().setOntology(ontology);
		PrestoCreateStatement createStatement = new PrestoCreateStatement(cs);
		createStatement.setColumns(cols);

		return this.getSQLCreateStatment(createStatement);
	}

	@Override
	public String getSQLCreateStatment(PrestoCreateStatement createStatement) {
        createStatement = prestoSQLHelper.parseCreateStatementColumns(createStatement);
        createStatement = prestoSQLHelper.parseHistoricalOptionsStatement(createStatement);
        return new PreparedStatement(createStatement.toString()).getStatement();	
	}

	@Override
	public List<String> getStringSupportedFieldDataTypes() {
		return new ArrayList<>(Arrays.asList(OntologyVirtualSchemaFieldType.OBJECT.getValue(),
				OntologyVirtualSchemaFieldType.STRING.getValue(), OntologyVirtualSchemaFieldType.NUMBER.getValue(),
				OntologyVirtualSchemaFieldType.INTEGER.getValue(), OntologyVirtualSchemaFieldType.DATE.getValue(),
				OntologyVirtualSchemaFieldType.TIMESTAMP.getValue(), OntologyVirtualSchemaFieldType.ARRAY.getValue(),
				OntologyVirtualSchemaFieldType.GEOMERTY.getValue(), OntologyVirtualSchemaFieldType.FILE.getValue(), 
				OntologyVirtualSchemaFieldType.BOOLEAN.getValue()));

	}

	@Override
	public List<String> getStringSupportedConstraintTypes() {
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);

	}

	private Object getSQLTypeForTableType(final Integer type) {
		switch (type) {
		case Types.NULL:
			return null;
		case Types.BOOLEAN:
		case Types.BIT:
		case Types.TINYINT:
			return Boolean.TRUE;
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.ROWID:
		case Types.BIGINT:
			return 1;
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.REAL:
			return 1.1;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
		case Types.TIMESTAMP:
		case Types.TIMESTAMP_WITH_TIMEZONE:
		case Types.DATE:
		case Types.TIME:
		case Types.TIME_WITH_TIMEZONE:
		case Types.SQLXML:
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.CLOB:
		case Types.NCLOB:
		case Types.BLOB:
		case Types.STRUCT:
		case Types.REF_CURSOR:
		case Types.REF:
		case Types.DATALINK:
		case Types.DISTINCT:
		case Types.JAVA_OBJECT:
		case Types.OTHER:
		case Types.ARRAY:
		default:
			return "string";
		}
	}

	private String getStatementFromJson(String statement) {
		if (statement.trim().startsWith("{")) {
			statement = statement.substring(statement.indexOf('{') + 1);
		}
		if (statement.trim().endsWith("}")) {
			statement = statement.substring(0, statement.lastIndexOf('}'));
		}
		return statement;
	}

	@Override
	public String executeQuery(final String ontology, final String query) {
		if (query.toUpperCase().startsWith(SELECT)) {
			return this.queryNativeAsJson(ontology, query);
		} else if (query.toUpperCase().startsWith(DELETE)) {
			return String.valueOf(this.deleteNative(ontology, query, false));
		} else if (query.toUpperCase().startsWith(SELECT_COUNT)) {
			return String.valueOf(countNative(ontology, query));
		} else {
			throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
		}
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query, offset, limit);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(OPERATION_NOT_SUPPORTED_FOR_PRESTO_ONTOLOGY);
	}

	@Override
	public List<String> getDatabases(String datasource) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");
			return new JdbcTemplate(prestoDatasourceManager.getDatasource(""))
					.queryForList(prestoSQLHelper.getDatabasesStatement(),
					String.class);
		} catch (final Exception e) {
			log.error("Error listing databases from user in external database", e);
			throw new DBPersistenceException("Error listing databases from user in external database", e);
		}
	}

	@Override
	public List<String> getSchemasDB(String datasource, String catalog) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");
			return new JdbcTemplate(prestoDatasourceManager.getDatasource(catalog,""))
					.queryForList(prestoSQLHelper.getSchemasStatement(catalog),
					String.class);
		} catch (final Exception e) {
			log.error("Error listing schemas from  Presto", e);
			throw new DBPersistenceException("Error listing schemas from Presto", e);
		}
	}

	@Override
	public List<String> getTables(String datasource, String catalog, String schema) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");
			return new JdbcTemplate(prestoDatasourceManager.getDatasource(catalog, schema))
					.queryForList(prestoSQLHelper.getAllTablesStatement(catalog, schema), String.class);
		} catch (final Exception e) {
			log.error("Error listing databases from user in external database", e);
			throw new DBPersistenceException("Error listing databases from user in external database", e);
		}
	}
	
	private List<ColumnPresto> generateColumns(final String ontologyJsonSchema) {
		List<ColumnPresto> cols = new ArrayList<>();
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(ontologyJsonSchema);
            if (jsonTree.isJsonObject()) {
                JsonObject jsonObject = jsonTree.getAsJsonObject();
                extractAllFieldsFromJson(cols, jsonObject);
            } else {
                throw new OPResourceServiceException(
                        "Invalid schema to be converted to SQL schema: " + ontologyJsonSchema);
            }

        } catch (Exception e) {
        	Log.warn("Not possible to convert schema to SQL schema: ", e.getMessage());
        	throw new OPResourceServiceException("Not possible to convert schema to SQL schema: " + e.getMessage());
        }
        return cols;
}

	private void extractAllFieldsFromJson(List<ColumnPresto> cols, JsonObject datosJson) {
		if (datosJson.has("properties")) {
			JsonObject propertiesJson = datosJson.get("properties").getAsJsonObject();
			Set<Entry<String, JsonElement>> keyValues = propertiesJson.entrySet();
			for (Entry<String, JsonElement> entry : keyValues) {
				String fieldName = entry.getKey();
				JsonObject fieldSpec = entry.getValue().getAsJsonObject();
				boolean fieldIsRequired = fieldSpec.get("required").getAsBoolean();
				String fieldDescription = fieldSpec.get("id").getAsString();
				String fieldType = fieldSpec.get("type").getAsString();
				
				ColumnPresto col = new ColumnPresto();
	            ColDataType colDT = new ColDataType();
	            colDT.setDataType(fieldType);
	            col.setColDataType(colDT);
	            col.setColumnName(fieldName);
	            col.setNotNull(fieldIsRequired);
	            col.setColComment(fieldDescription);
	            cols.add(col);
	        }
	    }
	}

}

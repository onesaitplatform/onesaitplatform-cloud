/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.persistence.external.virtual;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.*;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.exception.NotSupportedOperationException;
import com.minsait.onesait.platform.persistence.external.virtual.parser.JSONResultsetExtractor;
import com.minsait.onesait.platform.persistence.models.ErrorResult;

import lombok.extern.slf4j.Slf4j;

@Component("VirtualRelationalOntologyOpsDBRepository")
@Lazy
@Slf4j
public class VirtualRelationalOntologyOpsDBRepository implements VirtualOntologyDBRepository {

	private static final String NOT_SUPPORTED_OPERATION = "Operation not supported for Virtual Ontologies";
	private static final String SELECT_FROM = "SELECT * FROM ";
	private static final String ONTOLOGY_NOTNULLEMPTY = "Ontology can't be null or empty";
	private static final String NOT_IMPLEMENTED_METHOD = "Not implemented method";
	private static final String QUERY_NOTNULLEMPTY = "Query can't be null or empty";
	private static final String OFFSET_GREATERTHANZERO = "Offset must be greater or equals to 0";
	private static final String NO_UNIQUE_ID = "Virtual ontology does not have an unique id registered";

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	@Qualifier("VirtualDatasourcesManagerImpl")
	private VirtualDatasourcesManager virtualDatasourcesManager;

	@Autowired
	private SQLGenerator sqlGenerator;

	private JdbcTemplate getJdbTemplate(final String ontology) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);

			final String dataSourceName = virtualDatasourcesManager.getDatasourceForOntology(ontology).getDatasourceName();
			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager.getDataSourceDescriptor(dataSourceName);
			return new JdbcTemplate(dataSource.getDatasource());
		} catch (final Exception e) {
			throw new DBPersistenceException("JdbcTemplate not found for virtual ontology: " + ontology, e);
		}
	}

	@Override
	public List<String> getTables(final String datasourceName) {
		try {
			Assert.hasLength(datasourceName, "Datasource name can't be null or empty");

			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(datasourceName);
			final SQLHelper helper = virtualDatasourcesManager
					.getOntologyHelper(dataSource.getVirtualDatasourceType());
			return new JdbcTemplate(dataSource.getDatasource())
					.queryForList(helper.getAllTablesStatement(), String.class);
		} catch (final Exception e) {
			log.error("Error listing tables from user in external database", e);
			throw new DBPersistenceException("Error listing tables from user in external database", e);
		}
	}

	@Override
	public String insert(final String ontology, final String schema, final String instance) {
		final List<DBResult> result = this.insertOperation(ontology, schema, Collections.singletonList(instance));
		if(result.isEmpty()){
			return "No rows were affected";
		} else {
			return result.get(0).getId() != null ? result.get(0).getId()  : "1";
		}
	}

	@Override
	public ComplexWriteResult insertBulk(final String ontology, final String schema, final List<String> instances,
										 final boolean order, final boolean includeIds){
		final List<DBResult> result = this.insertOperation(ontology, schema, instances);
		return new ComplexWriteResult()
				.setData(result)
				.setType(ComplexWriteResultType.BULK);
	}

	private List<DBResult> insertOperation(final String ontology, final String schema, final List<String> instances){
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(schema, "Schema can't be null or empty");
			Assert.notEmpty(instances, "Instances can't be null or empty");

			final String statement = sqlGenerator.buildInsert()
					.setOntology(ontology)
					.setValuesAndColumnsForJson(instances)
					.generate();

			final JdbcTemplate jdbTemplate = getJdbTemplate(ontology);
			final GeneratedKeyHolder holder = new GeneratedKeyHolder();
			final int affected = jdbTemplate.update(connection -> connection.prepareStatement(statement), holder);
			final List<Map<String, Object>> keyList = holder.getKeyList();

			if(!keyList.isEmpty()) {
				return IntStream.range(0, affected)
						.mapToObj(index -> Long.parseLong(String.valueOf(keyList.get(index).get("insert_id"))) )
						.map( id -> new DBResult().setId(String.valueOf(id)).setOk(true) )
						.collect(Collectors.toList());
			} else {
				return IntStream.range(0, affected)
						.mapToObj(index -> new DBResult().setOk(true))
						.collect(Collectors.toList());
			}
		} catch (final Exception e) {
			log.error("Error inserting bulk data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error inserting bulk data on virtual ontology");
		}
	}

	@Override
	public MultiDocumentOperationResult updateNative(final String ontology, final String updateStmt, final boolean includeIds) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(updateStmt, "Statement can't be null or empty");

			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(getJdbTemplate(ontology).update(updateStmt.replaceAll(";", "")));
			return result;
		} catch (final Exception e) {
			log.error("Error updating data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error updating data on virtual ontology");
		}
	}

	@Override
	public MultiDocumentOperationResult updateNative(final String ontology, final String query, final String data, final boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD, new NotImplementedException(NOT_SUPPORTED_OPERATION));
	}

	@Override
	public MultiDocumentOperationResult deleteNative(final String ontology, final String query, final boolean includeIds) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(query, QUERY_NOTNULLEMPTY);

			final JdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
			final int updated = jdbcTemplate.update(query.replaceAll(";", ""));
			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(updated);
			return result;
		} catch (Exception e) {
			log.error("Error removing data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error removing data on virtual ontology");
		}
	}

	@Override
	public List<String> queryNative(final String ontology, final String query) {
		return this.queryNative(ontology, query, 0, virtualDatasourcesManager.getDatasourceForOntology(ontology).getQueryLimit());
	}

	@Override
	public List<String> queryNative(final String ontology, final String query, final int offset, final int limit) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(query, QUERY_NOTNULLEMPTY);
			Assert.isTrue(offset >= 0, OFFSET_GREATERTHANZERO);
			Assert.isTrue(limit >= 1, "Limit must be greater or equals to 1");

			final OntologyVirtualDatasource dataSource = virtualDatasourcesManager.getDatasourceForOntology(ontology);
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getSgdb());
			final String finalQuery = helper.addLimit(query, Math.min(limit, dataSource.getQueryLimit()), offset);
			final JdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
			return jdbcTemplate.query(finalQuery.replaceAll(";", ""), new JSONResultsetExtractor(finalQuery));
		} catch (final Exception e) {
			log.error("Error querying data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error querying data on virtual ontology");
		}
	}

	@Override
	public String queryNativeAsJson(final String ontology, final String query) {
		return queryNativeAsJson(ontology, query, 0, virtualDatasourcesManager.getDatasourceForOntology(ontology).getQueryLimit());
	}

	@Override
	public String queryNativeAsJson(final String ontology, final String originalQuery, final int offset, final int limit) {
		final List<String> result = this.queryNative(ontology, originalQuery, offset, limit);
		final JSONArray jsonResult = new JSONArray();
		for (final String instance : result) {
			final JSONObject obj = new JSONObject(instance);
			jsonResult.put(obj);
		}
		return jsonResult.toString();
	}

	@Override
	public String findById(final String ontology, final String objectId) {
		final String objId;
		final String query;
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(objectId, QUERY_NOTNULLEMPTY);
			objId = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
			Assert.hasLength(objId, NO_UNIQUE_ID);

			query = sqlGenerator.buildSelect()
					.setOntology(ontology)
					.setLimit(1)
					.setWhere(Collections.singletonList(new WhereStatement(objId, "=", objectId)))
					.generate();

		} catch (final Exception e) {
			log.error("Error finding by id", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error finding by id on virtual ontology");
		}

		return this.queryNativeAsJson(ontology, query);
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
		return queryNativeAsJson(ontology, query, offset, virtualDatasourcesManager.getDatasourceForOntology(ontology).getQueryLimit());
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
		return this.countNative(ontology, "SELECT COUNT(*) FROM " + ontology);
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
		final String column;
		final String query;
		try {
			column = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
			Assert.hasLength(column, NO_UNIQUE_ID);

			query = sqlGenerator.buildDelete()
					.setOntology(ontology)
					.setWhere(Collections.singletonList(new WhereStatement(column, "=", objectId)))
					.generate();
		} catch (final Exception e) {
			log.error("Error removing data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error removing data on virtual ontology");
		}
		return this.deleteNative(ontology, query, false);
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(final String ontology, final String uniqueId, final String body) {
		final String objId;
		final String statement;
		try {
			Assert.hasLength(ontology, "Ontology name can't be null or empty");
			Assert.hasLength(uniqueId, "Unique ID can't be null or empty");
			Assert.hasLength(body, "Body can't be null or empty");
			objId = this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
			Assert.hasLength(objId, NO_UNIQUE_ID);

			statement = sqlGenerator.buildUpdate()
					.setOntology(ontology)
					.setWhere(Collections.singletonList(new WhereStatement(objId, "=", uniqueId)))
					.setValuesForJson(body)
					.generate();
		}  catch (final Exception e) {
			log.error("Error updating data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error updating data on virtual ontology");
		}
		return this.updateNative(ontology, statement,false);
	}

	@Override
	public List<String> getInstanceFromTable(final String datasource, final String query) {
		try {
			Assert.hasLength(datasource, "Datasource can't be null or empty");
			Assert.hasLength(query, QUERY_NOTNULLEMPTY);

			final String finalQuery = getStatementFromJson(query);
			final VirtualDataSourceDescriptor sourceDescriptor = this.virtualDatasourcesManager
					.getDataSourceDescriptor(datasource);

			if (log.isDebugEnabled())
				log.debug("Query requested: {}", finalQuery);

			final String limitedQuery = virtualDatasourcesManager.getOntologyHelper(sourceDescriptor.getVirtualDatasourceType()).addLimit(query,
					sourceDescriptor.getQueryLimit());
			final JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceDescriptor.getDatasource());

			return jdbcTemplate.query(limitedQuery.replaceAll(";", ""), new JSONResultsetExtractor(limitedQuery));
		} catch (final Exception e) {
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error getting instance data on virtual ontology");
		}
	}

	@Override
	public String getTableMetadata(final String datasource, final String ontology) {
		try {
			Assert.hasLength(datasource, "Datasource can't be null or empty");
			Assert.hasLength(ontology, "Collection can't be null or empty");

			final HashMap<String,Object> map = new HashMap<>(this.getTableTypes(datasource, ontology));
			for (final Map.Entry<String, Object> entry : map.entrySet()) {
				entry.setValue(this.getSQLTypeForTableType((Integer) entry.getValue()));
			}

			return new JSONObject(map).toString();
		} catch (final Exception exception) {
			throw new DBPersistenceException(exception, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, exception.getMessage()),
					"Error getting meta data on virtual ontology");
		}
	}

	@Override
	public Map<String, Integer> getTableTypes(final String datasource, final String ontology) {
		final Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		java.sql.Connection conn = null;
		try {
			conn = this.virtualDatasourcesManager.getDataSourceDescriptor(datasource).getDatasource().getConnection();
			final DatabaseMetaData dbmeta = conn.getMetaData();
			final ResultSet rs = dbmeta.getColumns(null, null, ontology, null);
			while (rs.next()) {
				map.put(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"));
			}
			return map;
		} catch (final SQLException e) {
			throw new GenericRuntimeOPException("Error getting table metadata", e);
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (final Exception e2) {
				throw new GenericRuntimeOPException("Error closing connection when getting table metadata", e2);
			}
		}
	}

	private Object getSQLTypeForTableType(final Integer type) {
		switch (type) {
			case Types.NULL:
				return null;
			case Types.BOOLEAN:
				return Boolean.TRUE;
			case Types.BIT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
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
		if (query.toUpperCase().startsWith("SELECT")) {
			return this.queryNativeAsJson(ontology, query);
		} else if (query.toUpperCase().startsWith("UPDATE")) {
			return String.valueOf(this.updateNative(ontology, query, false));
		} else if (query.toUpperCase().startsWith("DELETE")) {
			return String.valueOf(this.deleteNative(ontology, query, false));
		} else if (query.toUpperCase().startsWith("SELECT COUNT")) {
			return String.valueOf(this.countNative(ontology, query));
		} else {
			throw new NotSupportedOperationException("Operation not supported for Virtual Ontology");
		}
	}

}


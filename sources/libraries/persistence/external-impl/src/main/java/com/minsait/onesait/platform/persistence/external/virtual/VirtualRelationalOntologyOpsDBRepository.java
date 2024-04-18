/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.DBResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.config.components.OntologyVirtualSchemaFieldType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceInfoDTO;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.exception.NotSupportedOperationException;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.helper.ExpandFnHelper;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLTableReplacer;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint.ConstraintType;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ExpandReplacement;
import com.minsait.onesait.platform.persistence.external.generator.model.common.RelatedEntityValues;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpsertStatement;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.TableKeysHolder;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.TableKeysHolder.SupportedDatabase;
import com.minsait.onesait.platform.persistence.external.virtual.parser.DatabaseEntityKeysParser;
import com.minsait.onesait.platform.persistence.external.virtual.parser.JSONResultsetExtractor;
import com.minsait.onesait.platform.persistence.models.ErrorResult;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;

@Component("VirtualRelationalOntologyOpsDBRepository")
@Lazy
@Slf4j
public class VirtualRelationalOntologyOpsDBRepository implements VirtualOntologyOpsDBRepository {

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

	@Value("#{'${onesaitplatform.database.excludeParse:dual}'.split(',')}")
	private List<String> excludeParse;

	private NamedParameterJdbcTemplate getJdbTemplate(final String ontology) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);

			final String dataSourceName = virtualDatasourcesManager.getDatasourceForOntology(ontology)
					.getIdentification();
			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(dataSourceName);
			return new NamedParameterJdbcTemplate(dataSource.getDatasource());
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
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());
			return new JdbcTemplate(dataSource.getDatasource()).queryForList(helper.getAllTablesStatement(),
					String.class);
		} catch (final Exception e) {
			log.error("Error listing tables from user in external database", e);
			throw new DBPersistenceException("Error listing tables from user in external database", e);
		}
	}

	@Override
	public List<Map<String, Object>> getTablePKInformation(String datasourceName, String database, String schema) {

		final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
				.getDataSourceDescriptor(datasourceName);
		final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());

		return new JdbcTemplate(dataSource.getDatasource()).queryForList(helper.getTableIndexes(database, schema));
	}

	@Override
	@Transactional
	public String insert(final String ontology, final String instance) {
		final List<DBResult> result = insertOperation(ontology, Collections.singletonList(instance));
		if (result.isEmpty()) {
			return "No rows were affected";
		} else {
			return result.get(0).getId() != null ? result.get(0).getId() : "1";
		}
	}

	@Override
	@Transactional
	public ComplexWriteResult insertBulk(final String ontology, final List<String> instances, final boolean order,
			final boolean includeIds) {
		final List<DBResult> result = insertOperation(ontology, instances);
		return new ComplexWriteResult().setData(result).setType(ComplexWriteResultType.BULK);
	}

	private List<DBResult> insertOperation(final String ontology, final List<String> instances) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.notEmpty(instances, "Instances can't be null or empty");
			final List<DBResult> resultsDb = new ArrayList<>();
			final OntologyVirtualDatasource ontologyVirtualDatasource = virtualDatasourcesManager
					.getDatasourceForOntology(ontology);
			final String dataSourceName = ontologyVirtualDatasource.getIdentification();
			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(dataSourceName);
			// START TRANSACTION DEFINITION
			final DataSourceTransactionManager tm = new DataSourceTransactionManager(dataSource.getDatasource());
			final TransactionTemplate tt = new TransactionTemplate(tm);
			tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			// END TRANSACTION DEFINITION
			final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(tm.getDataSource());

			final InsertStatement insertStatement = sqlGenerator.buildInsert().setOntology(ontology)
					.setValuesAndColumnsForInstances(instances);
			final Map<String, InsertStatement> statements = new LinkedHashMap<>();
			// ORDEN DE INSERCCION LINKEDMAP
			statements.put(ontology, insertStatement);
			if (!insertStatement.getRelatedValues().isEmpty()) {
				insertStatement.getRelatedValues().keySet().forEach(o -> {
					final InsertStatement i = sqlGenerator.buildInsert().setOntology(o);
					i.setColumns(insertStatement.getRelatedColumns().get(o));
					i.setValues(insertStatement.getRelatedValues().get(o));
					statements.put(o, i);
				});
			}
			final StringBuilder errors = new StringBuilder();
			// SE USA JDBCTEMPLATE Y PARAMETROS DE LA CONEXION PRINCIPAL, SE ASUME LA MISMA
			tt.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					doInsertWithRollback(status, statements, jdbcTemplate, ontologyVirtualDatasource, resultsDb,
							errors);

				}
			});
			if (!errors.isEmpty()) {
				throw new RuntimeException(errors.toString());
			}
			return resultsDb;
		} catch (final Exception e) {
			log.error("Error inserting bulk data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error inserting bulk data on virtual ontology");
		}

	}

	public void doInsertWithRollback(TransactionStatus status, Map<String, InsertStatement> statements,
			NamedParameterJdbcTemplate jdbcTemplate, OntologyVirtualDatasource ontologyVirtualDatasource,
			List<DBResult> resultsDb, StringBuilder errors) {
		try {
			final List<Map<String, Object>> keyList = new ArrayList<>();
			for (final java.util.Map.Entry<String, InsertStatement> e : statements.entrySet()) {
				int affected;
				final PreparedStatement sql = e.getValue().generate(true);
				final String statement = null;
//    				if (ontologyVirtualDatasource.getSgdb()
//    						.equals(OntologyVirtualDatasource.VirtualDatasourceType.POSTGRESQL)) {
//    					String[] split = sql.getStatement().split("VALUES");
//    					if (split.length > 0) {
//    						String values = split[1].trim().replace("'", "");
//    						statement = sql.getStatement().replace(split[1], values);
//    					}
//    				}

				sql.setStatement(
						SQLTableReplacer.replaceTableNameInInsert(statement != null ? statement : sql.getStatement(),
								ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(e.getKey())));

				if (ontologyVirtualDatasource.getSgdb().equals(OntologyVirtualDatasource.VirtualDatasourceType.ORACLE)
						|| ontologyVirtualDatasource.getSgdb()
								.equals(OntologyVirtualDatasource.VirtualDatasourceType.ORACLE11)) {
					// ORACLE ON JDBC DOES NOT SUPPORT INSERT BULK WITH RETURN ID, PRODUCES
					// ORA-00933: SQL command not properly ended
					affected = jdbcTemplate.update(sql.getStatement(), sql.getParams());
//    					keyList = new ArrayList<>();
				} else {
					final GeneratedKeyHolder holder = new GeneratedKeyHolder();
					affected = jdbcTemplate.update(sql.getStatement(), new MapSqlParameterSource(sql.getParams()),
							holder);
					keyList.clear();
					keyList.addAll(holder.getKeyList());
				}

				if (keyList != null && !keyList.isEmpty()) {
					switch (ontologyVirtualDatasource.getSgdb()) {
					case MYSQL:
					case MARIADB:
						resultsDb.addAll(IntStream.range(0, affected)
								.mapToObj(index -> String.valueOf(keyList.get(index).get("GENERATED_KEY")))
								.map(id -> new DBResult().setId(id).setOk(true)).collect(Collectors.toList()));
						break;
					case SQLSERVER:
						// Only last inserted id recoverable
						// https://github.com/microsoft/mssql-jdbc/issues/245
						IntStream.range(0, affected - 1).mapToObj(index -> new DBResult().setOk(true))
								.collect(Collectors.toList());
						resultsDb.add(
								new DBResult().setId(String.valueOf(keyList.get(0).get("GENERATED_KEYS"))).setOk(true));
						break;
					case POSTGRESQL:
						final String objId = ontologyVirtualRepository
								.findOntologyVirtualObjectIdByOntologyIdentification(e.getKey());
						if (objId != null) {
							resultsDb.addAll(IntStream.range(0, affected)
									.mapToObj(index -> String.valueOf(keyList.get(index).get(objId)))
									.map(id -> new DBResult().setId(id).setOk(true)).collect(Collectors.toList()));
							break;
						}
						// else fallback
					case ORACLE:
					case ORACLE11:
					default:
						resultsDb.addAll(IntStream.range(0, affected).mapToObj(index -> new DBResult().setOk(true))
								.collect(Collectors.toList()));
						break;
					}
				} else {
					resultsDb.addAll(IntStream.range(0, affected).mapToObj(index -> new DBResult().setOk(true))
							.collect(Collectors.toList()));
				}
			}
		} catch (final Exception e) {
			errors.append(e.getMessage());
			log.error("Error inside transaction, rolling back", e);
			status.setRollbackOnly();
		}
	}

	public MultiDocumentOperationResult insertNative(final String ontology, final String insertStmt,
			final boolean includeIds) {
		return insertNative(ontology, new PreparedStatement(insertStmt.replaceAll(";", "")), includeIds);
	}

	private MultiDocumentOperationResult insertNative(final String ontology, final PreparedStatement ps,
			final boolean includeIds) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(ps.getStatement(), "Statement can't be null or empty");

			ps.setStatement(SQLTableReplacer.replaceTableNameInInsert(ps.getStatement(),
					ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(ontology)));

			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(getJdbTemplate(ontology).update(ps.getStatement(), ps.getParams()));
			return result;
		} catch (final Exception e) {
			log.error("Error updating data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error updating data on virtual ontology");
		}
	}

	@Override
	public MultiDocumentOperationResult updateNative(final String ontology, final String updateStmt,
			final boolean includeIds) {
		return updateNative(ontology, new PreparedStatement(updateStmt.replaceAll(";", "")), includeIds);
	}

	private MultiDocumentOperationResult updateNative(final String ontology, final PreparedStatement ps,
			final boolean includeIds) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(ps.getStatement(), "Statement can't be null or empty");

			ps.setStatement(SQLTableReplacer.replaceTableNameInUpdate(ps.getStatement(), ontology,
					ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(ontology)));

			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(getJdbTemplate(ontology).update(ps.getStatement(), ps.getParams()));
			return result;
		} catch (final Exception e) {
			log.error("Error updating data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error updating data on virtual ontology");
		}
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

			ps.setStatement(SQLTableReplacer.replaceTableNameInDelete(ps.getStatement(),
					ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(ontology)));

			final NamedParameterJdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
			final int deleted = jdbcTemplate.update(ps.getStatement(), ps.getParams());
			final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(deleted);
			return result;
		} catch (final Exception e) {
			log.error("Error removing data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error removing data on virtual ontology");
		}
	}

	@Override
	public List<String> queryNative(final String ontology, final String query) {
		return this.queryNative(ontology, query, 0,
				virtualDatasourcesManager.getDatasourceForOntology(ontology).getQueryLimit());
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

			if (!SQLHelper.hasExpand(ps.getStatement())) {
				final String originalStatement = ps.getStatement();

				final OntologyVirtualDatasource dataSource = virtualDatasourcesManager
						.getDatasourceForOntology(ontology);

				ps.setStatement(SQLTableReplacer.replaceTableNameInSelect(ps.getStatement(), ontologyVirtualRepository,
						excludeParse));

				final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getSgdb());
				ps.setStatement(helper.parseGeometryFields(ps.getStatement(), ontology));

				ps.setStatement(
						helper.addLimit(ps.getStatement(), Math.min(limit, dataSource.getQueryLimit()), offset));

				final NamedParameterJdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
				log.info("Invoke SGDB with jdbcTemplate");
				List<String> result = jdbcTemplate.query(ps.getStatement(), ps.getParams(),
						new JSONResultsetExtractor(ps.getStatement(), originalStatement));
				log.info("Receive result from SGDB with jdbcTemplate");
				
				return result;
			} else {
				return queryNativeExpand(ontology, ps, offset, limit);
			}
		} catch (final Exception e) {
			log.error("Error querying data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error querying data on virtual ontology");
		}
	}

	private List<String> queryNativeExpand(final String ontology, PreparedStatement ps, final int offset,
			final int limit) {
		try {

			final ExpandReplacement expandReplacement = SQLHelper.replaceExpandInStatement(ps.getStatement());

			final String originalStatement = expandReplacement.getStatement();

			final String expandStatement = ps.getStatement();

			final OntologyVirtualDatasource dataSource = virtualDatasourcesManager.getDatasourceForOntology(ontology);

			ps.setStatement(SQLTableReplacer.replaceTableNameInSelect(originalStatement, ontologyVirtualRepository,
					excludeParse));

			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getSgdb());
			ps.setStatement(helper.parseGeometryFields(ps.getStatement(), ontology));

			ps.setStatement(helper.addLimit(ps.getStatement(), Math.min(limit, dataSource.getQueryLimit()), offset));

			final NamedParameterJdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
			final List<JSONObject> objs = new ExpandFnHelper(ontology, expandStatement, originalStatement, jdbcTemplate,
					excludeParse, expandReplacement).query(ps);

			return objs.stream().map(o -> o.toString()).toList();
		} catch (final Exception e) {
			log.error("Error querying data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error querying data on virtual ontology");
		}
	}

	@Override
	public String queryNativeAsJson(final String ontology, final String query) {
		return queryNativeAsJson(ontology, new PreparedStatement(query));
	}

	private String queryNativeAsJson(final String ontology, final PreparedStatement ps) {
		return queryNativeAsJson(ontology, ps, 0,
				virtualDatasourcesManager.getDatasourceForOntology(ontology).getQueryLimit());
	}

	@Override
	public String queryNativeAsJson(final String ontology, final String originalQuery, final int offset,
			final int limit) {
		return queryNativeAsJson(ontology, new PreparedStatement(originalQuery), offset, limit);
	}

	private String queryNativeAsJson(final String ontology, final PreparedStatement ps, final int offset,
			final int limit) {
		log.info("Send Query Native to SGDB");
		final List<String> result = this.queryNative(ontology, ps, offset, limit);
		log.info("Receive ressult ofD Query Native to SGDB");
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
		final PreparedStatement ps;
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);
			Assert.hasLength(objectId, QUERY_NOTNULLEMPTY);
			objId = ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
			Assert.hasLength(objId, NO_UNIQUE_ID);

			ps = sqlGenerator.buildSelect().setOntology(ontology).setLimit(1)
					.setWhere(Collections.singletonList(new WhereStatement(objId, "=", objectId))).generate(true);

		} catch (final Exception e) {
			log.error("Error finding by id", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error finding by id on virtual ontology");
		}

		return this.queryNativeAsJson(ontology, ps);
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
		return queryNativeAsJson(ontology, query, offset,
				virtualDatasourcesManager.getDatasourceForOntology(ontology).getQueryLimit());
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
		final String column;
		final PreparedStatement query;
		try {
			column = ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
			Assert.hasLength(column, NO_UNIQUE_ID);

			query = sqlGenerator.buildDelete().setOntology(ontology)
					.setWhere(Collections.singletonList(new WhereStatement(column, "=", objectId))).generate(true);
		} catch (final Exception e) {
			log.error("Error removing data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error removing data on virtual ontology");
		}
		return this.deleteNative(ontology, query, false);
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

			final String originalStatement = ps.getStatement();
			final VirtualDataSourceDescriptor sourceDescriptor = virtualDatasourcesManager
					.getDataSourceDescriptor(datasource);

			if (log.isDebugEnabled()) {
				log.debug("Query requested: {}", ps.getStatement());
			}

			ps.setStatement(virtualDatasourcesManager.getOntologyHelper(sourceDescriptor.getVirtualDatasourceType())
					.addLimit(ps.getStatement(), sourceDescriptor.getQueryLimit()));
			final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(
					sourceDescriptor.getDatasource());

			return jdbcTemplate.query(ps.getStatement(), ps.getParams(),
					new JSONResultsetExtractor(ps.getStatement(), originalStatement));
		} catch (final Exception e) {
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error getting instance data on virtual ontology");
		}
	}

	@Override
	public String getTableMetadata(final String datasource, final String database, final String schema,
			final String ontology) {
		try {
			Assert.hasLength(datasource, "Datasource can't be null or empty");
			Assert.hasLength(ontology, "Collection can't be null or empty");

			final HashMap<String, Object> map = new HashMap<>(getTableTypes(datasource, database, schema, ontology));
			for (final Map.Entry<String, Object> entry : map.entrySet()) {
				entry.setValue(getSQLTypeForTableType((Integer) entry.getValue()));
			}

			return new JSONObject(map).toString();
		} catch (final Exception exception) {
			throw new DBPersistenceException(exception,
					new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, exception.getMessage()),
					"Error getting meta data on virtual ontology");
		}
	}

	private JsonArray getColumnsMetadata(final String datasource, final String database, final String schema,
			final String ontology) {
		final JsonArray columns = new JsonArray();
		java.sql.Connection conn = null;

		try {
			conn = virtualDatasourcesManager.getDataSourceDescriptor(datasource).getDatasource().getConnection();
			final DatabaseMetaData dbmeta = conn.getMetaData();

			String ontologyName;

			ontologyName = ontology;
			final ResultSet rs = dbmeta.getColumns(database, schema, ontologyName, null);
			if (rs.next()) {
				do {
					final JsonObject jsonObj = new JsonObject();
					jsonObj.addProperty("ordinal_position", rs.getString("ORDINAL_POSITION"));
					jsonObj.addProperty("column_name", rs.getString("COLUMN_NAME").trim());
					jsonObj.addProperty("data_type", rs.getString("DATA_TYPE"));
					// jsonObj.addProperty("column_def", rs.getString("COLUMN_DEF")); it's not used
					// and can have SQLException 99999 in fetching with oracle
					jsonObj.addProperty("is_nullable", rs.getString("IS_NULLABLE"));
					jsonObj.addProperty("remarks", rs.getString("REMARKS"));
					columns.add(jsonObj);

				} while (rs.next());
			} else {
				if (rs.getMetaData() != null && rs.getMetaData().getColumnCount() > 0) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						final JsonObject jsonObj = new JsonObject();

						jsonObj.addProperty("ordinal_position", i);
						jsonObj.addProperty("column_name", rs.getMetaData().getColumnName(i).trim());
						jsonObj.addProperty("data_type", rs.getMetaData().getColumnType(i));
						// jsonObj.addProperty("column_def", rs.getString("COLUMN_DEF")); it's not used
						// and can have SQLException 99999 in fetching with oracle
						jsonObj.addProperty("is_nullable", rs.getMetaData().isNullable(i));
						// jsonObj.addProperty("remarks", rs.getString("REMARKS"));
						columns.add(jsonObj);
					}

				}

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
	public Map<String, Integer> getTableTypes(final String datasource, final String database, final String schema,
			final String ontology) {
		final Map<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		try {
			final JsonArray columns = getColumnsMetadata(datasource, database, schema, ontology);
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
	public String getSqlTableDefinitionFromSchema(final String ontology, final String schema,
			final VirtualDatasourceType datasource) {
		return sqlGenerator.getSqlTableDefinitionFromSchema(ontology, schema, datasource);
	}

	@Override
	public String getSQLCreateStatment(CreateStatement statement, VirtualDatasourceType datasource) {
		statement.setSqlGenerator(sqlGenerator);
		return sqlGenerator.getSQLCreateTable(statement, datasource).getStatement();
	}

	@Override
	public List<String> getStringSupportedFieldDataTypes() {
		return new ArrayList<>(Arrays.asList(OntologyVirtualSchemaFieldType.OBJECT.getValue(),
				OntologyVirtualSchemaFieldType.STRING.getValue(), OntologyVirtualSchemaFieldType.NUMBER.getValue(),
				OntologyVirtualSchemaFieldType.INTEGER.getValue(), OntologyVirtualSchemaFieldType.DATE.getValue(),
				OntologyVirtualSchemaFieldType.TIMESTAMP.getValue(), OntologyVirtualSchemaFieldType.ARRAY.getValue(),
				OntologyVirtualSchemaFieldType.GEOMERTY.getValue(),
				// OntologyVirtualSchemaFieldType.GEOMERTY_POINT.getValue(),
				// OntologyVirtualSchemaFieldType.GEOMERTY_LINESTRING.getValue(),
				// OntologyVirtualSchemaFieldType.GEOMERTY_POLYGON.getValue(),
				OntologyVirtualSchemaFieldType.FILE.getValue(), OntologyVirtualSchemaFieldType.BOOLEAN.getValue()));

	}

	@Override
	public List<String> getStringSupportedConstraintTypes() {
		return new ArrayList<>(Arrays.asList(ConstraintType.PRIMARY_KEY.name(), ConstraintType.FOREIGN_KEY.name(),
				ConstraintType.UNIQUE.name()));

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
		case Types.TIMESTAMP:
			return "datetime";
		case Types.DATE:
			return "date";
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
		case Types.TIMESTAMP_WITH_TIMEZONE:
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
			return String.valueOf(countNative(ontology, query));
		} else {
			throw new NotSupportedOperationException("Operation not supported for Virtual Ontology");
		}
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return queryNativeAsJson(ontology, query, offset, limit);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException("Operation not supported for Virtual Ontology");
	}

	@Override
	public VirtualDatasourceInfoDTO getInfo(String datasourceName) {
		try {
			Assert.hasLength(datasourceName, "Datasource name can't be null or empty");

			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(datasourceName);
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());
			final VirtualDatasourceInfoDTO vDTO = new VirtualDatasourceInfoDTO();
			vDTO.setHasSchema(helper.hasSchema());
			vDTO.setHasDatabase(helper.hasDatabase());
			vDTO.setHasCrossDatabase(helper.hasCrossDatabase());
			if (helper.hasDatabase()) {
				final List<String> ldb = new JdbcTemplate(dataSource.getDatasource())
						.queryForList(helper.getDatabaseStatement(), String.class);
				if (!ldb.isEmpty()) {
					vDTO.setCurrentDatabase(ldb.get(0));
				}
			}
			if (helper.hasSchema()) {
				final List<String> lsc = new JdbcTemplate(dataSource.getDatasource())
						.queryForList(helper.getSchemaStatement(), String.class);
				if (!lsc.isEmpty()) {
					vDTO.setCurrentSchema(lsc.get(0));
				}
			}
			return vDTO;
		} catch (final Exception e) {
			log.error("Error getting info ", e);
			throw new DBPersistenceException("Error listing tables from user in external database", e);
		}
	}

	@Override
	public List<String> getDatabases(String datasource) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");

			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(datasource);
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());
			try {
				return new JdbcTemplate(dataSource.getDatasource()).queryForList(helper.getDatabasesStatement(),
						String.class);
			} catch (final org.springframework.jdbc.IncorrectResultSetColumnCountException e) {
				final List<Map<String, Object>> listMap = new JdbcTemplate(dataSource.getDatasource())
						.queryForList(helper.getDatabasesStatement());

				final List<String> result = new ArrayList<String>();
				for (final Map<String, Object> map : listMap) {
					result.add(map.get("name").toString());
				}
				return result;
			}

		} catch (final Exception e) {
			log.error("Error listing databases from user in external database", e);
			throw new DBPersistenceException("Error listing databases from user in external database", e);
		}
	}

	@Override
	public List<String> getSchemasDB(String datasource, String database) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");

			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(datasource);
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());
			return new JdbcTemplate(dataSource.getDatasource()).queryForList(helper.getSchemasStatement(database),
					String.class);
		} catch (final Exception e) {
			log.error("Error listing databases from user in external database", e);
			throw new DBPersistenceException("Error listing databases from user in external database", e);
		}
	}

	@Override
	public List<Map<String, Object>> getTableInformation(String datasource, String database, String schema) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");

			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(datasource);
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());

			return new JdbcTemplate(dataSource.getDatasource())
					.queryForList(helper.getTableInformationStatement(database, schema));
		} catch (final Exception e) {
			log.error("Error listing databases from user in external database", e);
			throw new DBPersistenceException("Error listing databases from user in external database", e);
		}
	}

	@Override
	public List<String> getTables(String datasource, String database, String schema) {
		try {
			Assert.hasLength(datasource, "Datasource name can't be null or empty");

			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(datasource);
			final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(dataSource.getVirtualDatasourceType());
			return new JdbcTemplate(dataSource.getDatasource())
					.queryForList(helper.getAllTablesStatement(database, schema), String.class);
		} catch (final Exception e) {
			log.error("Error listing databases from user in external database", e);
			throw new DBPersistenceException("Error listing databases from user in external database", e);
		}
	}

	@Override
	public TableKeysHolder getTableKeysHolder(Ontology ontology, String table) {
		final String dataSourceName = virtualDatasourcesManager.getDatasourceForOntology(ontology.getIdentification())
				.getIdentification();
		final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
				.getDataSourceDescriptor(dataSourceName);
		;
		try {
			final DatabaseMetaData dm = new NamedParameterJdbcTemplate(dataSource.getDatasource()).getJdbcTemplate()
					.getDataSource().getConnection().getMetaData();
			final DatabaseEntityKeysParser parser = new DatabaseEntityKeysParser(dm, table,
					dataSource.getVirtualDatasourceType());
			final TableKeysHolder tkh = parser.getKeysHolder();
			return tkh;

		} catch (final SQLException e1) {
			log.error("Error while retrieving metadata for ontology {}, table {}", ontology.getIdentification(), table);
			throw new DBPersistenceException(
					"Error while retrieving metadata for ontology " + ontology.getIdentification());
		}

	}

	@Override
	public void cloneTableNoData(String ontology, String sourceTable, String targetTable,
			SupportedDatabase databaseType) {
		String statement;
		final NamedParameterJdbcTemplate jdbcTemplate = getJdbTemplate(ontology);
		switch (databaseType) {
		case POSTGRES:
			statement = "CREATE TABLE " + targetTable + " AS (SELECT * FROM " + sourceTable + ") WITH NO DATA";
			log.info("Executing statement {}", statement);
			try {
				jdbcTemplate.getJdbcTemplate().getDataSource().getConnection().createStatement()
						.executeUpdate(statement);
			} catch (final SQLException e) {
				throw new RuntimeException(e);
			}
			break;
		case MARIADB:
			statement = "SHOW CREATE TABLE " + sourceTable;
			getJdbTemplate(ontology).query(statement, rs -> {
				final String c = rs.getMetaData().getColumnLabel(2);
				final String result = rs.getString(c);
				try {
					final CreateTable ct = (CreateTable) CCJSqlParserUtil.parse(result);
					ct.setIndexes(new ArrayList<>());
					ct.setTable(new Table(targetTable));
					jdbcTemplate.getJdbcTemplate().getDataSource().getConnection().createStatement()
							.executeUpdate(ct.toString());
				} catch (final JSQLParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			break;
		default:
			throw new IllegalArgumentException("Unsupported database value: " + databaseType);
		}

	}

	@Override
	public void cloneUKeys(String ontology, String targetTable, TableKeysHolder tkh) {
		// REPLACE TABLES ON TKH BY PREFIX
		final String queries = tkh.generateUKs(targetTable);
		for (final String statement : queries.split(";")) {
			log.info("Executing statement {}", statement);
			try {
				getJdbTemplate(ontology).getJdbcTemplate().getDataSource().getConnection().createStatement()
						.executeUpdate(statement);
			} catch (final SQLException e) {
				if (e.getMessage() != null && e.getMessage().contains("already exists")) {
					log.warn("CloneUKeys error: ", e.getMessage());
				} else {
					throw new RuntimeException(e);
				}
			}
		}

	}

	@Override
	public void cloneFKeys(String ontology, String targetTable, TableKeysHolder tkh) {
		// REPLACE TABLES ON TKH BY PREFIX
		final String queries = tkh.generateFKs(targetTable);
		for (final String statement : queries.split(";")) {
			log.info("Executing statement {}", statement);
			try {
				getJdbTemplate(ontology).getJdbcTemplate().getDataSource().getConnection().createStatement()
						.executeUpdate(statement);
			} catch (final SQLException e) {
				if (e.getMessage() != null && e.getMessage().contains("already exists")) {
					log.warn("CloneFKeys error: ", e.getMessage());
				} else {
					throw new RuntimeException(e);
				}
			}
		}

	}

	private String getDeleteNonPresentTuples(Set<String> ids, String entity, String oid, String fk, String fkValue) {
		final StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM ");
		builder.append(entity);
		builder.append(" WHERE ");
		builder.append(oid);
		builder.append(" NOT IN (");

		if (ids.isEmpty()) {
			builder.append("''");
		} else {
			for (final String id : ids) {
				if (!builder.toString().endsWith("(")) {
					builder.append(",");
				}
				builder.append("'" + id + "'");
			}
		}

		builder.append(") AND ");
		builder.append(fk);
		builder.append("='");
		builder.append(fkValue);
		builder.append("'");

		String finalStatement = null;
		try {
			finalStatement = SQLTableReplacer.replaceTableNameInDelete(builder.toString(),
					ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(entity));
//			getJdbTemplate(entity).getJdbcTemplate().getDataSource().getConnection().createStatement()
//					.executeUpdate(finalStatement);
		} catch (final Exception e) {
			log.error("Could not run delete children {}", finalStatement, e);
		}
		return finalStatement;

	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(final String ontology, final String uniqueId,
			final String body) {
		final String objId;
		try {
			Assert.hasLength(ontology, "Ontology name can't be null or empty");
			Assert.hasLength(uniqueId, "Unique ID can't be null or empty");
			Assert.hasLength(body, "Body can't be null or empty");
			objId = ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
			Assert.hasLength(objId, NO_UNIQUE_ID);

			final UpdateStatement mainUpdateSt = sqlGenerator.buildUpdate().setOntology(ontology)
					.setWhere(Collections.singletonList(new WhereStatement(objId, "=", uniqueId)))
					.setValuesForJson(body);
			// Iterate children entities to create the statements
			if (!mainUpdateSt.getRelatedValues().isEmpty()) {
				return upsertRelationalEntities(mainUpdateSt, ontology);
			} else {
				return this.updateNative(ontology, mainUpdateSt.generate(true), false);
			}
		} catch (final Exception e) {
			log.error("Error updating data", e);
			throw new DBPersistenceException(e, new ErrorResult(ErrorResult.PersistenceType.VIRTUAL, e.getMessage()),
					"Error updating data on virtual ontology");
		}

	}

	private MultiDocumentOperationResult upsertRelationalEntities(UpdateStatement mainUpdateSt, String ontology) {
		// Statements of children entities
		final Map<String, List<UpsertStatement>> statements = new HashMap<>();
		final Map<String, String> deleteStatements = new HashMap<>();
		mainUpdateSt.getRelatedValues().entrySet().forEach(e -> {
			final String oid = ontologyVirtualRepository
					.findOntologyVirtualObjectIdByOntologyIdentification(e.getKey());
			// Store uniqueIds to perform delete later
			final Set<String> uniqueIDValues = new HashSet<>();
			final RelatedEntityValues rv = e.getValue();
			// IF X-TO-MANY multiple instanes
			rv.getColumnsAndValues().forEach(instance -> {
				final UpsertStatement ust = sqlGenerator.buildUpsert().withOntology(e.getKey()).withValues(instance)
						.setUniqueIDWithValue(oid);
				if (!statements.containsKey(e.getKey())) {
					statements.put(e.getKey(), new ArrayList<>());
				}
				statements.get(e.getKey()).add(ust);
				uniqueIDValues.add(ust.getUniqueIDValue());
			});
			final String deleteStt = getDeleteNonPresentTuples(uniqueIDValues, e.getKey(), oid, rv.getForeignKey(),
					rv.getForeignKeyValue());
			deleteStatements.put(e.getKey(), deleteStt);
		});
		// TO-DO in transaction
		final OntologyVirtualDatasource ontologyVirtualDatasource = virtualDatasourcesManager
				.getDatasourceForOntology(ontology);
		final String dataSourceName = ontologyVirtualDatasource.getIdentification();
		final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
				.getDataSourceDescriptor(dataSourceName);
		// START TRANSACTION DEFINITION
		final DataSourceTransactionManager tm = new DataSourceTransactionManager(dataSource.getDatasource());
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		// END TRANSACTION DEFINITION
		final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(tm.getDataSource());

		final StringBuilder errors = new StringBuilder();
		// SE USA JDBCTEMPLATE Y PARAMETROS DE LA CONEXION PRINCIPAL, SE ASUME LA MISMA
		final MultiDocumentOperationResult mdor = tt.execute(status -> doUpsertWithRollback(statements,
				deleteStatements, ontologyVirtualDatasource, ontology, mainUpdateSt, status, jdbcTemplate, errors));
		if (!errors.isEmpty()) {
			throw new RuntimeException(errors.toString());
		}
		return mdor;

//		final MultiDocumentOperationResult mdor = this.updateNative(ontology, mainUpdateSt.generate(true), false);
//		statements.entrySet().forEach(es -> {
//			es.getValue().forEach(st -> {
//				try {
//					final PreparedStatement ps = st.generate(true);
//					ps.setStatement(SQLTableReplacer.replaceTableNameInInsert(ps.getStatement(),
//							ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(es.getKey())));
//					getJdbTemplate(es.getKey()).update(ps.getStatement(), ps.getParams());
//				} catch (final Exception e) {
//					log.error("Could not execute insert", e);
//				}
//			});
//
//		});
		// return only main entity changes
//		return mdor;
	}

	public MultiDocumentOperationResult doUpsertWithRollback(Map<String, List<UpsertStatement>> upsertStatements,
			Map<String, String> deleteStatements, OntologyVirtualDatasource ontologyVirtualDatasource,
			String mainEntity, UpdateStatement mainUpdateSt, TransactionStatus status,
			NamedParameterJdbcTemplate jdbcTemplate, StringBuilder errors) {
		try {
			// RUN UPSERTS
			for (final java.util.Map.Entry<String, List<UpsertStatement>> es : upsertStatements.entrySet()) {
				for (final UpsertStatement st : es.getValue()) {
					final PreparedStatement ps = st.generate(true);
					ps.setStatement(SQLTableReplacer.replaceTableNameInInsert(ps.getStatement(),
							ontologyVirtualRepository.findOntologyVirtualByOntologyIdentification(es.getKey())));
					log.debug("Running statement {}", ps.getStatement());
					getJdbTemplate(es.getKey()).update(ps.getStatement(), ps.getParams());
				}
			}
			// RUN DELETES
			for (final java.util.Map.Entry<String, String> e : deleteStatements.entrySet()) {
				log.debug("Running statement {}", e.getValue());
				getJdbTemplate(e.getKey()).getJdbcTemplate().getDataSource().getConnection().createStatement()
						.executeUpdate(e.getValue());
			}

			// RUN MAIN UPDATE
			final PreparedStatement ps = mainUpdateSt.generate(true);
			log.debug("Running statement {}", ps.getStatement());
			final MultiDocumentOperationResult mdor = this.updateNative(mainEntity, ps, false);
			return mdor;
		} catch (final Exception e) {
			errors.append(e.getMessage());
			log.error("Error inside transaction, rolling back", e);
			status.setRollbackOnly();
			return null;
		}
	}

}

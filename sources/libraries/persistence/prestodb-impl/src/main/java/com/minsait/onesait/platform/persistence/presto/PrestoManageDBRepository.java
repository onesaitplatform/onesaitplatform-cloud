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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.historical.minio.HistoricalMinioException;
import com.minsait.onesait.platform.persistence.historical.minio.HistoricalMinioService;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.presto.generator.PrestoSQLHelper;

import lombok.extern.slf4j.Slf4j;

@Component("PrestoManageDBRepository")
@Lazy
@Slf4j
public class PrestoManageDBRepository implements ManageDBRepository {

	private static final String NOT_IMPLEMENTED_METHOD = "Not implemented method";
	private static final String PREFIX_CREATE_TABLE = "CREATE TABLE";

	private static final String KEY_ALLOWS_CREATE_TABLE = "allowsCreateTable";
	private static final String KEY_SQL_STATEMENT = "sqlStatement";
	private static final String KEY_SCHEMA = "datasourceSchema";
	private static final String KEY_CATALOG = "datasourceCatalog";
	private static final String KEY_BUCKET = "bucketName";

	@Value("${onesaitplatform.database.prestodb.historicalCatalog:minio}")
	private String historicalCatalog;

	@Autowired
	@Qualifier("PrestoDatasourceManagerImpl")
	private PrestoDatasourceManager prestoDatasourceManager;
	
	@Autowired
	private HistoricalMinioService historicalMinioService;

	@Autowired
	private SQLGenerator sqlGenerator;
	
	@Autowired
	private PrestoSQLHelper prestoSQLHelper;
	
	@Autowired
	private OntologyService ontologySevice;

//	private PrestoSQLGeneratorOps sqlGeneratorOps = new PrestoSQLGeneratorOpsImpl();
	
	public JdbcTemplate getJdbTemplate(String catalog, String schema) {
		return new JdbcTemplate(prestoDatasourceManager.getDatasource(catalog, schema));
	}

	public JdbcTemplate getJdbTemplate(String ontology) {
		return new JdbcTemplate(prestoDatasourceManager.getDatasource(ontology));
	}
	
	@Override
	public Map<String, Boolean> getStatusDatabase() {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		boolean allowsCreateTable = false;
		String statement = "";
		String catalog = "";
		
		if (config != null && config.containsKey(KEY_ALLOWS_CREATE_TABLE)) {
			allowsCreateTable = config.get(KEY_ALLOWS_CREATE_TABLE).equals("true");
		}
		if (allowsCreateTable) {
			if (config.containsKey(KEY_SQL_STATEMENT) && config.get(KEY_SQL_STATEMENT) != null
					&& !config.get(KEY_SQL_STATEMENT).equals("")) {
				statement = config.get(KEY_SQL_STATEMENT);

				if (statement.toUpperCase().trim().indexOf(PREFIX_CREATE_TABLE) != 0)
					throw new DBPersistenceException("Error creating table: invalid sqlstatement " + statement);
			} else {
				throw new DBPersistenceException("Error creating table: invalid sqlstatement " + statement);
			}
			if (config.containsKey(KEY_CATALOG) && config.get(KEY_CATALOG) != null &&
					config.containsKey(KEY_SCHEMA) && config.get(KEY_SCHEMA) != null) {
				catalog = config.get(KEY_CATALOG);
				if (catalog.equals(historicalCatalog) &&
						config.containsKey(KEY_BUCKET) && config.get(KEY_BUCKET) != null) {
						if (!historicalMinioService.validateBucketString(ontology, statement)) {
							throw new DBPersistenceException("Error creating table: forbidden external location for entity");
						}
						
						try {
							historicalMinioService.createUserAndBucketIfNotExists(ontologySevice.getOntologyByIdentification(ontology).getUser().getUserId());
							historicalMinioService.createBucketDirectory(ontology);
						
						
						} catch (final HistoricalMinioException e) {
							log.error("Error creating table", e);
							throw new DBPersistenceException("Error creating table: " + e.getCause(), e);
						}
				}
				log.info("Launching SQL statment for ontology {} to Presto catalog {} : {}",
						ontology, catalog, statement);
				try {
					getJdbTemplate(config.get(KEY_CATALOG), config.get(KEY_SCHEMA)).execute(statement);
					log.info("Created table succesfully: " + ontology);
				} catch (final CannotGetJdbcConnectionException e) {
					log.error("Error creating table", e);
					throw new DBPersistenceException(" Error creating table: Unable to connect to Presto", e);
				} catch (final Exception e) {
					log.error("Error creating table", e);
					throw new DBPersistenceException("Error creating table: " + e.getCause(), e);
				}
			} 
				
		} else {
			// else: skip create table (table exists yet)
			log.warn("Skipping create table because do not allows create table (it already exists in db)");
		}

		return ontology;
	}
	

	@Override
	public List<String> getListOfTables() {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		try {
			return new JdbcTemplate(prestoDatasourceManager.getDatasource(ontology))
					.queryForList(prestoSQLHelper.getAllTablesStatement(),
					String.class);
		} catch (final Exception e) {
			log.error("Error listing tables", e);
			throw new DBPersistenceException("Error listing tables", e);
		}

	}

	@Override
	public void removeTable4Ontology(String ontology) {
		removeTable4Ontology(ontology, false);
	}
	
	public void removeTable4Ontology(String ontology, boolean deleteData) {
		try {
			if (deleteData) {
				historicalMinioService.removeBucketDirectoryAndDataFromOntology(ontology);
			}
			final String statement = sqlGenerator.buildDrop().setOntology(ontology).setCheckIfExists(false)
					.generate(true).getStatement();
			getJdbTemplate(ontology).execute(statement);			
		} catch (final Exception e) {
			log.error("Error deleting table: " + e.getMessage(), e);
			throw new DBPersistenceException(" Error deleting table: " + e.getMessage(), e);
		}
	}

	@Override
	public void createIndex(String ontology, String attribute) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public void createIndex(String sentence) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public List<String> getListIndexes(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public String getIndexes(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String path) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}	
}

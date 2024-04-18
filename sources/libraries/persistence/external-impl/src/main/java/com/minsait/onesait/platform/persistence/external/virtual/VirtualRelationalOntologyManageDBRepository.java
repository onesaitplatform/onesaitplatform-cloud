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


import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.SQLGeneratorOps;
import com.minsait.onesait.platform.persistence.external.generator.SQLGeneratorOpsImpl;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import lombok.extern.slf4j.Slf4j;

@Component("VirtualRelationalOntologyManageDBRepository")
@Lazy
@Slf4j
public class VirtualRelationalOntologyManageDBRepository implements ManageDBRepository {

	private static final String ONTOLOGY_NOTNULLEMPTY = "Ontology can't be null or empty";
	private static final String NOT_IMPLEMENTED_METHOD = "Not implemented method";
	private static final String ERROR_DATASOURCE_NOT_EMPTY = "Datasource name can't be null or empty";
	private static final String PREFIX_CREATE_TABLE = "CREATE TABLE";
	
	private static final String KEY_ALLOWS_CREATE_TABLE = "allowsCreateTable";
	private static final String KEY_SQL_STATEMENT = "sqlStatement";

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	@Qualifier("VirtualDatasourcesManagerImpl")
	private VirtualDatasourcesManager virtualDatasourcesManager;
	
	@Autowired
	private SQLGenerator sqlGenerator;
	
	private SQLGeneratorOps sqlGeneratorOps = new SQLGeneratorOpsImpl();
	
	private JdbcTemplate getJdbTemplate(final String ontology) {
		try {
			Assert.hasLength(ontology, ONTOLOGY_NOTNULLEMPTY);

			final String dataSourceName = virtualDatasourcesManager.getDatasourceForOntology(ontology)
					.getDatasourceName();
			final VirtualDataSourceDescriptor dataSource = virtualDatasourcesManager
					.getDataSourceDescriptor(dataSourceName);
			return new JdbcTemplate(dataSource.getDatasource());
		} catch (final Exception e) {
			throw new DBPersistenceException("JdbcTemplate not found for virtual ontology: " + ontology, e);
		}
	}
	

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}
	
	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		OntologyVirtualDatasource datasource;
		boolean allowsCreateTable = false;
		String statement;
		try {
			if (config != null && config.containsKey(KEY_ALLOWS_CREATE_TABLE)) {
				allowsCreateTable = config.get(KEY_ALLOWS_CREATE_TABLE).equals("true");
			}
			if (allowsCreateTable) {
				datasource = ontologyVirtualRepository.findOntologyVirtualDatasourceByOntologyIdentification(
						ontology);
				Assert.hasLength(datasource.getDatasourceName(), ERROR_DATASOURCE_NOT_EMPTY);
				
				if (config.containsKey(KEY_SQL_STATEMENT) && config.get(KEY_SQL_STATEMENT) != null
						&& !config.get(KEY_SQL_STATEMENT).equals("")) {
					statement = config.get(KEY_SQL_STATEMENT); 
					if (!statement.toUpperCase().replace(" ", "").startsWith(PREFIX_CREATE_TABLE.replace(" ", "")+ontology.toUpperCase().trim())) {
						// CREATETABLEONTOLOGY
						throw new OPResourceServiceException("Not possible to create table with sql statment: " + statement);
					}
				}
				else {
					final List<ColumnRelational> cols = sqlGeneratorOps.generateColumnsRelational(schema);
					CreateStatement createStatement = sqlGenerator.buildCreate().setOntology(ontology); 
					createStatement.setColumnsRelational(cols); 
					statement = createStatement.generate(true).getStatement();
				}
				
				log.info("Launching SQL statment for ontology " + ontology + " to database " + datasource.getDatasourceName() + " : " + statement);
				getJdbTemplate(ontology).execute(statement);
				log.info("Created table succesfully: " + ontology);
			}
			
			else {
				// else: skip create table (table exists yet)
				log.warn("Skipping create table because do not allows create table (it already exists in db)");
			}
		
		} catch (final Exception e) {
			log.error("Error creating table from user in external database", e);
			throw new DBPersistenceException("Error creating table from user in external database", e);
		}
		return ontology;
	}
	

	@Override
	public List<String> getListOfTables() {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}
	

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		String datasourceName;
		try {
			datasourceName = ontologyVirtualRepository.findOntologyVirtualDatasourceByOntologyIdentification(
					ontology
					).getDatasourceName();
			Assert.hasLength(datasourceName, ERROR_DATASOURCE_NOT_EMPTY);

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
	public void removeTable4Ontology(String ontology) {
		String datasourceName;
		try {
			datasourceName = ontologyVirtualRepository.findOntologyVirtualDatasourceByOntologyIdentification(
					ontology
					).getDatasourceName();
			Assert.hasLength(datasourceName, ERROR_DATASOURCE_NOT_EMPTY);
			final String statement = sqlGenerator.buildDrop().setOntology(ontology).setCheckIfExists(false).generate(true).getStatement();
			getJdbTemplate(ontology).execute(statement);
		
		} catch (final Exception e) {
			log.error("Error deleting table from user in external database", e);
			throw new DBPersistenceException("Error deleting table from user in external database", e);
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

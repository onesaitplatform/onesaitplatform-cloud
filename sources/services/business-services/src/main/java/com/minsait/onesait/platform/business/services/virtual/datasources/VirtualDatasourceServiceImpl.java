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
package com.minsait.onesait.platform.business.services.virtual.datasources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.persistence.external.exception.SGDBNotSupportedException;
import com.minsait.onesait.platform.persistence.external.virtual.helper.VirtualOntologyHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VirtualDatasourceServiceImpl implements VirtualDatasourceService {

	@Autowired
	OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;

	@Autowired
	@Qualifier("OracleVirtualOntologyHelper")
	private VirtualOntologyHelper oracleVirtualOntologyHelper;

	@Autowired
	@Qualifier("MysqlVirtualOntologyHelper")
	private VirtualOntologyHelper mysqlVirtualOntologyHelper;

	@Autowired
	@Qualifier("MariaDBVirtualOntologyHelper")
	private VirtualOntologyHelper mariaVirtualOntologyHelper;

	@Autowired
	@Qualifier("PostgreSQLVirtualOntologyHelper")
	private VirtualOntologyHelper postgreVirtualOntologyHelper;

	@Autowired
	@Qualifier("SQLServerVirtualOntologyHelper")
	private VirtualOntologyHelper sqlserverVirtualOntologyHelper;

	@Autowired
	@Qualifier("ImpalaVirtualOntologyHelper")
	private VirtualOntologyHelper impalaVirtualOntologyHelper;

	@Autowired
	@Qualifier("HiveVirtualOntologyHelper")
	private VirtualOntologyHelper hiveVirtualOntologyHelper;

	@Override
	public List<String> getAllIdentifications() {
		final List<OntologyVirtualDatasource> datasources = ontologyVirtualDatasourceRepository
				.findAllByOrderByDatasourceNameAsc();
		final List<String> identifications = new ArrayList<String>();
		for (final OntologyVirtualDatasource datasource : datasources) {
			identifications.add(datasource.getDatasourceName());

		}
		return identifications;
	}

	@Override
	public List<OntologyVirtualDatasource> getAllDatasources() {
		return ontologyVirtualDatasourceRepository.findAll();
	}

	@Override
	public void createDatasource(OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.save(datasource);
	}

	@Override
	public OntologyVirtualDatasource getDatasourceById(String id) {
		final OntologyVirtualDatasource datasource = ontologyVirtualDatasourceRepository.findById(id);

		if (datasource != null) {
			return datasource;
		} else {
			return null;
		}

	}

	@Override
	public void updateOntology(OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.save(datasource);
	}

	@Override
	public void deleteDatasource(OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.delete(datasource);
	}

	@Override
	public Boolean checkConnection(String datasourceName, String user, String credentials, String sgdb, String url,
			String queryLimit) throws GenericOPException {

		String driverClassName;
		VirtualDatasourceType type = VirtualDatasourceType.valueOf(sgdb);

		switch (VirtualDatasourceType.valueOf(sgdb)) {// Esto tiene que recuperar el BasicDataSource
		case ORACLE:
			driverClassName = oracle.jdbc.driver.OracleDriver.class.getName();
			break;
		case MYSQL:
			driverClassName = com.mysql.jdbc.Driver.class.getName();
			break;
		case MARIADB:
			driverClassName = org.mariadb.jdbc.Driver.class.getName();
			break;
		case POSTGRESQL:
			driverClassName = org.postgresql.Driver.class.getName();
			break;
		case SQLSERVER:
			driverClassName = com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName();
			break;
		case IMPALA:
			driverClassName = org.apache.hive.jdbc.HiveDriver.class.getName();
			break;
		case HIVE:
			driverClassName = org.apache.hive.jdbc.HiveDriver.class.getName();
			break;
		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + sgdb);

		}

		// Only to test connection from control panel. It has no sense to create a
		// pooling connection here
		DriverManagerDataSource driverManagerDatasource = new DriverManagerDataSource();
		driverManagerDatasource.setDriverClassName(driverClassName);
		driverManagerDatasource.setUrl(url);
		driverManagerDatasource.setUsername(user);
		driverManagerDatasource.setPassword(credentials);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(driverManagerDatasource);
		VirtualOntologyHelper helper = getOntologyHelper(type);

		try {
			jdbcTemplate.queryForList(helper.getAllTablesStatement(), String.class);
			return true;
		} catch (Exception e) {
			log.error("Error checking connection to datasource", e);
			throw new GenericOPException(e.getMessage());
		}
	}

	@Override
	public Boolean changePublic(String datasource) {
		OntologyVirtualDatasource virtualDatasource = ontologyVirtualDatasourceRepository
				.findByDatasourceName(datasource);
		if (virtualDatasource != null) {
			if (virtualDatasource.isPublic())
				virtualDatasource.setPublic(false);
			else
				virtualDatasource.setPublic(true);
			ontologyVirtualDatasourceRepository.save(virtualDatasource);
			return true;
		}
		return false;

	}

	@Override
	public Boolean checkConnectionExtern(String datasourceName) throws GenericOPException {
		OntologyVirtualDatasource ovdatasource = ontologyVirtualDatasourceRepository
				.findByDatasourceName(datasourceName);
		// VirtualDataSourceDescriptor datasource = new VirtualDataSourceDescriptor();

		String driverClassName;
		VirtualDatasourceType type = VirtualDatasourceType.valueOf(ovdatasource.getSgdb().toString());
		switch (type) {
		case ORACLE:
			driverClassName = oracle.jdbc.driver.OracleDriver.class.getName();
			// datasource.setVirtualDatasourceType(ovdatasource.getSgdb());
			break;
		case MYSQL:
			driverClassName = com.mysql.jdbc.Driver.class.getName();
			// datasource.setVirtualDatasourceType(VirtualDatasourceType.MYSQL);
			break;
		default:
			throw new SGDBNotSupportedException("Not supported SGDB");

		}

		// Only to test connection from control panel. It has no sense to create a
		// pooling connection here
		DriverManagerDataSource driverManagerDatasource = new DriverManagerDataSource();
		driverManagerDatasource.setDriverClassName(driverClassName);
		driverManagerDatasource.setUrl(ovdatasource.getConnectionString());
		driverManagerDatasource.setUsername(ovdatasource.getUser());
		driverManagerDatasource.setPassword(ovdatasource.getCredentials());

		JdbcTemplate jdbcTemplate = new JdbcTemplate(driverManagerDatasource);

		VirtualOntologyHelper helper = getOntologyHelper(type);

		try {
			jdbcTemplate.queryForList(helper.getAllTablesStatement(), String.class);
			return true;
		} catch (Exception e) {
			log.error("Error checking connection to datasource", e);
			throw new GenericOPException(e.getMessage());
		}
	}

	private VirtualOntologyHelper getOntologyHelper(VirtualDatasourceType type) {
		switch (type) {
		case ORACLE:
			return oracleVirtualOntologyHelper;
		case MYSQL:
			return mysqlVirtualOntologyHelper;
		case MARIADB:
			return mariaVirtualOntologyHelper;
		case POSTGRESQL:
			return postgreVirtualOntologyHelper;
		case SQLSERVER:
			return sqlserverVirtualOntologyHelper;
		case IMPALA:
			return impalaVirtualOntologyHelper;
		case HIVE:
			return hiveVirtualOntologyHelper;
		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + type);
		}

	}

}

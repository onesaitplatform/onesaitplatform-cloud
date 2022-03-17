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
package com.minsait.onesait.platform.persistence.external.virtual;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;
import com.minsait.onesait.platform.persistence.external.exception.SGDBNotSupportedException;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;

@Component("VirtualDatasourcesManagerImpl")
public class VirtualDatasourcesManagerImpl implements VirtualDatasourcesManager {

	@Value("${onesaitplatform.database.virtual.datasource.minIdle:0}")
	private int minIdle;

	@Value("${onesaitplatform.database.virtual.datasource.maxWait:30000}")
	private long maxWait;

	@Value("${device.broker.rest.removeAbandoned:true}")
	private boolean removeAbandoned;

	@Value("${device.broker.rest.removeAbandonedTimeout:300}")
	private int removeAbandonedTimeout;

	private Map<String, VirtualDataSourceDescriptor> virtualDatasouces;

	@Autowired
	private OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	@Qualifier("SQLHelperImpl")
	private SQLHelper sqlHelper;

	@Autowired
	@Qualifier("PostgreSQLHelper")
	private SQLHelper postgreSQLHelper;

	@Autowired
	@Qualifier("OracleHelper")
	private SQLHelper oracleHelper;

	@Autowired
	@Qualifier("Oracle11Helper")
	private SQLHelper oracle11Helper;

	@Autowired
	@Qualifier("SQLServerHelper")
	private SQLHelper sqlserverHelper;

	@Autowired
	@Qualifier("OpQueryDatahubHelper")
	private SQLHelper opQueryDatahubHelper;

	@PostConstruct
	public void init() {
		this.virtualDatasouces = new HashMap<>();
	}

	@Override
	public VirtualDataSourceDescriptor getDataSourceDescriptor(String datasourceName) {
		final VirtualDataSourceDescriptor datasourceDescriptor = this.virtualDatasouces.get(datasourceName);
		if (datasourceDescriptor == null) {
			setDatasourceDescriptor(datasourceName);
		}
		return this.virtualDatasouces.get(datasourceName);
	}

	@Override
	public void setDatasourceDescriptor(String datasourceName) {
		final VirtualDataSourceDescriptor dsDescriptor = new VirtualDataSourceDescriptor();

		final BasicDataSource datasource = new BasicDataSource();
		final OntologyVirtualDatasource datasourceConfiguration = ontologyVirtualDatasourceRepository
				.findByIdentification(datasourceName);

		final String driverClassName = this.getDriverClassName(datasourceConfiguration.getSgdb());
		final String connectionString = datasourceConfiguration.getConnectionString();
		final String user = datasourceConfiguration.getUserId();

		final String password = datasourceConfiguration.getCredentials();
		final int poolSize = Integer.parseInt(datasourceConfiguration.getPoolSize());

		datasource.setDriverClassName(driverClassName);
		datasource.setUrl(connectionString);

		if (user != null && !"".equals(user)) {
			datasource.setUsername(user);
		}
		if (password != null && !"".equals(password)) {
			datasource.setPassword(JasyptConfig.getEncryptor().decrypt(password));
		}
		datasource.setMaxActive(poolSize);

		datasource.setInitialSize(1);
		datasource.setMaxIdle(poolSize / 2);

		datasource.setMinIdle(this.minIdle);
		datasource.setMaxWait(maxWait);
		datasource.setRemoveAbandoned(removeAbandoned);
		datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);

		// if they are not definede, the default datasource values will be used.
		if (datasourceConfiguration.getValidationQuery() != null) {
			datasource.setValidationQuery(datasourceConfiguration.getValidationQuery());
		}
		if (datasourceConfiguration.getValidationQueryTimeout() != null) {
			datasource.setValidationQueryTimeout(datasourceConfiguration.getValidationQueryTimeout());
		}
		if (datasourceConfiguration.getTestOnBorrow() != null) {
			datasource.setTestOnBorrow(datasourceConfiguration.getTestOnBorrow());
		}
		if (datasourceConfiguration.getTestWhileIdle() != null) {
			datasource.setTestWhileIdle(datasourceConfiguration.getTestWhileIdle());
		}

		dsDescriptor.setQueryLimit(datasourceConfiguration.getQueryLimit());
		dsDescriptor.setDatasource(datasource);
		dsDescriptor.setVirtualDatasourceType(datasourceConfiguration.getSgdb());

		this.virtualDatasouces.put(datasourceName, dsDescriptor);
	}

	@Override
	public OntologyVirtualDatasource getDatasourceForOntology(final String ontology) {
		final OntologyVirtualDatasource ontologyDatasource = this.ontologyVirtualRepository
				.findOntologyVirtualDatasourceByOntologyIdentification(ontology);
		Assert.notNull(ontologyDatasource, "Datasource not found for virtual ontology: " + ontology);
		return ontologyDatasource;
	}

	@Override
	public String getDriverClassName(VirtualDatasourceType type) {
		switch (type) {
		case ORACLE:
		case ORACLE11:
			return oracle.jdbc.driver.OracleDriver.class.getName();
		case MYSQL:
		case MARIADB:
			return com.mysql.cj.jdbc.Driver.class.getName();
		case POSTGRESQL:
			return org.postgresql.Driver.class.getName();
		case SQLSERVER:
			return com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName();
		case IMPALA:
		case HIVE:
			return org.apache.hive.jdbc.HiveDriver.class.getName();
		case OP_QUERYDATAHUB:
			return org.apache.calcite.avatica.remote.Driver.class.getName();
		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + type.name());
		}
	}

	@Override
	public SQLHelper getOntologyHelper(VirtualDatasourceType type) {
		switch (type) {
		case MARIADB:
		case MYSQL:
		case HIVE:
		case IMPALA:
			return sqlHelper;
		case POSTGRESQL:
			return postgreSQLHelper;
		case SQLSERVER:
			return sqlserverHelper;
		case ORACLE:
			return oracleHelper;
		case ORACLE11:
			return oracle11Helper;
		case OP_QUERYDATAHUB:
			return opQueryDatahubHelper;
		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + type);
		}
	}

}

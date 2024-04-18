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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.persistence.external.exception.SGDBNotSupportedException;

@Component
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

	@PostConstruct
	public void init() {
		this.virtualDatasouces = new HashMap<>();
	}

	@Override
	public VirtualDataSourceDescriptor getDataSourceDescriptor(String datasourceName) {
		VirtualDataSourceDescriptor datasourceDescriptor = this.virtualDatasouces.get(datasourceName);
		if (null == datasourceDescriptor) {

			BasicDataSource datasource = new BasicDataSource();

			OntologyVirtualDatasource datasourceConfiguration = ontologyVirtualDatasourceRepository
					.findByDatasourceName(datasourceName);

			String driverClassName = this.getDriverClassName(datasourceConfiguration.getSgdb());
			String connectionString = datasourceConfiguration.getConnectionString();
			String user = datasourceConfiguration.getUser();
			String password = datasourceConfiguration.getCredentials();
			int poolSize = Integer.parseInt(datasourceConfiguration.getPoolSize());

			datasource.setDriverClassName(driverClassName);
			datasource.setUrl(connectionString);
			datasource.setUsername(user);
			datasource.setPassword(password);
			datasource.setMaxActive(poolSize);

			datasource.setInitialSize(1);
			datasource.setMaxIdle(poolSize / 2);

			datasource.setMinIdle(this.minIdle);
			datasource.setMaxWait(maxWait);
			datasource.setRemoveAbandoned(removeAbandoned);
			datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);

			datasourceDescriptor = new VirtualDataSourceDescriptor();
			datasourceDescriptor.setQueryLimit(datasourceConfiguration.getQueryLimit());
			datasourceDescriptor.setDatasource(datasource);
			datasourceDescriptor.setVirtualDatasourceType(datasourceConfiguration.getSgdb());

			this.virtualDatasouces.put(datasourceName, datasourceDescriptor);

		}

		return datasourceDescriptor;
	}

	private String getDriverClassName(VirtualDatasourceType type) {
		switch (type) {
		case ORACLE:
			return oracle.jdbc.driver.OracleDriver.class.getName();
		case MYSQL:
			return com.mysql.jdbc.Driver.class.getName();
		case MARIADB:
			return org.mariadb.jdbc.Driver.class.getName();
		case POSTGRESQL:
			return org.postgresql.Driver.class.getName();
		case SQLSERVER:
			return com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName();
		case IMPALA:
			return org.apache.hive.jdbc.HiveDriver.class.getName();
		case HIVE:
			return org.apache.hive.jdbc.HiveDriver.class.getName();
		default:
			throw new SGDBNotSupportedException("Not supported SGDB: " + type.name());

		}
	}

}

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
package com.minsait.onesait.platform.persistence.presto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.minsait.onesait.platform.config.model.OntologyPresto;
import com.minsait.onesait.platform.config.repository.OntologyPrestoRepository;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Slf4j
@Component("PrestoDatasourceManagerImpl")
public class PrestoDatasourceManagerImpl implements PrestoDatasourceManager {

	@Value("${onesaitplatform.database.presto.datasource.minIdle:0}")
	private int minIdle;

	@Value("${onesaitplatform.database.presto.datasource.maxWait:30000}")
	private long maxWait;
	
	@Value("${onesaitplatform.database.presto.datasource.poolSize:100}")
	private int poolSize;
	
	@Value("${onesaitplatform.database.presto.queries.defaultLimit:2000}")
	private int queryLimit;

	@Value("${device.broker.rest.removeAbandoned:true}")
	private boolean removeAbandoned;

	@Value("${device.broker.rest.removeAbandonedTimeout:300}")
	private int removeAbandonedTimeout;
		
	private Map<String, DataSource> datasources;
	
	@Autowired
	private OntologyPrestoRepository ontologyPrestoRepository;
	
	@Autowired
	private IntegrationResourcesService resourcesService;

	@PostConstruct
	public void init() {
		this.datasources = new HashMap<>();
	}
	
	private int getMaxRegisters() {
		try {
			return ((Integer) resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("queries-limit"))
					.intValue();
		} catch (final Exception e) {
			return queryLimit;
		}
	}
	
	@Override
	public DataSource getDatasource(String catalog, String schema) {
		final String datasourceName = buildDatasourceName(catalog, schema);
		final DataSource ds = this.datasources.get(datasourceName);
		if (ds == null) {
			setDatasource(datasourceName);
		}
		return this.datasources.get(datasourceName);
	}

	@Override
	public void setDatasource(String datasourceName) {
		final BasicDataSource ds = new BasicDataSource();

		final String driverClassName = this.getDriverClassName();
		final String url = resourcesService.getUrl(Module.PRESTO, ServiceUrl.BASE);
		String connectionString = "jdbc:presto://";
		try {
			final URI uri = new URI(url);
			connectionString += uri.getHost() + ':' + uri.getPort() + '/' + datasourceName;
		} catch (URISyntaxException e) {
			log.error("Unable to convert Presto URI: {}", url);
		}
		final String user = "root";
		final String password = "";
		final int poolSize = this.poolSize;

		ds.setDriverClassName(driverClassName);
		ds.setUrl(connectionString);
		ds.setUsername(user);
		ds.setPassword(password);
		ds.setMaxActive(poolSize);
		ds.setInitialSize(1);
		ds.setMaxIdle(poolSize / 2);
		ds.setMinIdle(this.minIdle);
		ds.setMaxWait(maxWait);
		
		ds.setRemoveAbandoned(removeAbandoned);
		ds.setRemoveAbandonedTimeout(removeAbandonedTimeout);

		ds.setValidationQuery("SELECT 1");
		ds.setTestOnBorrow(true);
		ds.setTestWhileIdle(true);


		this.datasources.put(datasourceName, ds);
	}
	
	@Override
	public DataSource getDatasource(String ontology) {
		final OntologyPresto op = this.getOntologyPrestoForOntology(ontology);
		return this.getDatasource(op.getDatasourceCatalog(), op.getDatasourceSchema());
	}

	private String getDriverClassName() {
			return com.facebook.presto.jdbc.PrestoDriver.class.getName();
	}

	@Override
	public OntologyPresto getOntologyPrestoForOntology(final String ontology) {
		final OntologyPresto ontologyPresto = this.ontologyPrestoRepository
				.findOntologyPrestoByOntologyIdentification(ontology);
		Assert.notNull(ontologyPresto, "Datasource not found for Presto ontology: " + ontology);
		return ontologyPresto;
	}
	
	@Override
	public int getQueryLimit() {
		return getMaxRegisters();
	}
	
	private String buildDatasourceName(String catalog, String schema) {
		if (!catalog.isEmpty())
			return catalog + "/" + schema;
		else return "";
	}
	
}

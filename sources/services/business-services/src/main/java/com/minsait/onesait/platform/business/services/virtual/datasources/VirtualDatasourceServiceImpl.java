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

import java.util.List;
import java.util.stream.Collectors;

import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDatasourcesManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VirtualDatasourceServiceImpl implements VirtualDatasourceService {

	@Autowired
	@Qualifier("VirtualDatasourcesManagerImpl")
	private VirtualDatasourcesManager virtualDatasourcesManager;

	@Autowired
	private OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Override
	public List<String> getAllIdentifications() {
		final List<OntologyVirtualDatasource> datasources = ontologyVirtualDatasourceRepository
				.findAllByOrderByDatasourceNameAsc();
		return datasources.stream().map(OntologyVirtualDatasource::getDatasourceName).collect(Collectors.toList());
	}

	@Override
	public List<OntologyVirtualDatasource> getAllDatasources() {
		return ontologyVirtualDatasourceRepository.findAll();
	}

	@Override
	public void createDatasource(final OntologyVirtualDatasource datasource) {
		datasource.setCredentials(JasyptConfig.getEncryptor().encrypt(datasource.getCredentials()));
		ontologyVirtualDatasourceRepository.save(datasource);
	}

	@Override
	public OntologyVirtualDatasource getDatasourceById(final String id) {
		return ontologyVirtualDatasourceRepository.findById(id);
	}

	@Override
	public void updateOntology(final OntologyVirtualDatasource datasource) {
		datasource.setCredentials(JasyptConfig.getEncryptor().encrypt(datasource.getCredentials()));
		ontologyVirtualDatasourceRepository.save(datasource);
		virtualDatasourcesManager.setDatasourceDescriptor(datasource.getDatasourceName());
	}

	@Override
	public void deleteDatasource(final OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.delete(datasource);
	}

	@Override
	public Boolean checkConnection(final String dataSourceName, final String user, final String credentials,
								   final String sgdb, final String url,	final String queryLimit) throws GenericOPException {
		final VirtualDatasourceType type = VirtualDatasourceType.valueOf(sgdb);
		final String driverClassName = virtualDatasourcesManager.getDriverClassName(type);

		// Only to test connection from control panel. It has no sense to create a
		// pooling connection here
		final DriverManagerDataSource driverManagerDatasource = new DriverManagerDataSource();
		driverManagerDatasource.setDriverClassName(driverClassName);
		driverManagerDatasource.setUrl(url);
		driverManagerDatasource.setUsername(user);
		driverManagerDatasource.setPassword(credentials);

		final JdbcTemplate jdbcTemplate = new JdbcTemplate(driverManagerDatasource);
		final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(type);

		try {
			jdbcTemplate.queryForList(helper.getAllTablesStatement(), String.class);
			return true;
		} catch (Exception e) {
			log.error("Error checking connection to datasource", e);
			throw new GenericOPException(e.getMessage());
		}
	}

	@Override
	public Boolean changePublic(final String dataSource) {
		final OntologyVirtualDatasource virtualDataSource = ontologyVirtualDatasourceRepository
				.findByDatasourceName(dataSource);
		if (virtualDataSource != null) {
			if (virtualDataSource.isPublic())
				virtualDataSource.setPublic(false);
			else
				virtualDataSource.setPublic(true);
			ontologyVirtualDatasourceRepository.save(virtualDataSource);
			return true;
		}
		return false;

	}

	@Override
	public Boolean checkConnectionExtern(final String dataSourceName) throws GenericOPException {
		final OntologyVirtualDatasource dataSource = ontologyVirtualDatasourceRepository
				.findByDatasourceName(dataSourceName);
		return this.checkConnection(dataSourceName,
				dataSource.getUser(),
				JasyptConfig.getEncryptor().decrypt(dataSource.getCredentials()),
				dataSource.getSgdb().toString(),
				dataSource.getConnectionString(),
				String.valueOf(dataSource.getQueryLimit()));
	}

	private VirtualDatasourceType getTypeFromOntology(final String ontology){
		return ontologyVirtualRepository.findOntologyVirtualDatasourceByOntologyIdentification(ontology).getSgdb();
	}

	@Override
	public String getUniqueColumn(final String ontology){
		return this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
	}

}

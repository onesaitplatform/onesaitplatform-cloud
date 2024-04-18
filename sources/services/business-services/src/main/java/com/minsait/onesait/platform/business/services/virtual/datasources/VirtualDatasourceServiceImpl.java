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
package com.minsait.onesait.platform.business.services.virtual.datasources;

import static com.minsait.onesait.platform.encryptor.config.JasyptConfig.JASYPT_BEAN;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.exceptions.VirtualDatasourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;
import com.minsait.onesait.platform.persistence.external.generator.helper.SQLHelper;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualDatasourcesManager;

import lombok.extern.slf4j.Slf4j;

@Service
@DependsOn(JASYPT_BEAN)
@Slf4j
public class VirtualDatasourceServiceImpl implements VirtualDatasourceService {

	@Autowired
	@Qualifier("VirtualDatasourcesManagerImpl")
	private VirtualDatasourcesManager virtualDatasourcesManager;

	@Autowired
	private OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;

	@Autowired
	private OntologyVirtualRepository ontologyVirtualRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OPResourceService resourceService;

	@Override
	public List<OntologyVirtualDatasource> getAllByDatasourceNameAndUser(String identification, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (userService.isUserAdministrator(sessionUser)) {
			if (identification == null || identification.trim().equals("")) {
				return getAllDatasources();
			} else {
				return ontologyVirtualDatasourceRepository
						.findAllByDatasourceNameLikeOrderByDatasourceNameAsc(identification);
			}
		} else {
			if (identification == null || identification.trim().equals("")) {
				return getAllDatasourcesByUser(sessionUser);
			} else {
				return ontologyVirtualDatasourceRepository
						.findAllByDatasourceNameLikeAndUserIdOrIsPublicTrueOrderByDatasourceNameAsc(identification,
								sessionUser);
			}
		}
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<OntologyVirtualDatasource> datasources = ontologyVirtualDatasourceRepository
				.findAllByOrderByIdentificationAsc();
		return datasources.stream().map(OntologyVirtualDatasource::getIdentification).collect(Collectors.toList());
	}

	@Override
	public List<OntologyVirtualDatasource> getAllDatasources() {
		return ontologyVirtualDatasourceRepository.findAll();
	}

	@Override
	public List<OntologyVirtualDatasource> getAllDatasourcesByUser(User user) {
		return ontologyVirtualDatasourceRepository.findByUserOrIsPublicTrueOrAccess(user);
	}

	@Override
	public void createDatasource(final OntologyVirtualDatasource datasource) throws GenericOPException {
		datasource.setCredentials(JasyptConfig.getEncryptor().encrypt(datasource.getCredentials()));

		if (datasource.getIdentification() != null && !datasource.getIdentification().trim().equals("")) {
			OntologyVirtualDatasource datasourceBD = ontologyVirtualDatasourceRepository
					.findByIdentification(datasource.getIdentification());
			if (datasourceBD != null) {
				throw new GenericOPException("Datasource Identification already exists");
			}
		}

		if (datasource.getDatasourceDomain() != null && !datasource.getDatasourceDomain().trim().equals("")) {
		    List<OntologyVirtualDatasource> datasourceList = ontologyVirtualDatasourceRepository
                .findByDatasourceDomain(datasource.getDatasourceDomain());
		    if (datasourceList != null && !datasourceList.isEmpty()) {
                throw new GenericOPException("Datasource Domain already exists");
            }
		}

		ontologyVirtualDatasourceRepository.saveAndFlush(datasource);
	}

	@Override
	public OntologyVirtualDatasource getDatasourceById(final String id) {
		return ontologyVirtualDatasourceRepository.findById(id).orElse(null);
	}
	
	@Override
	public List <OntologyVirtualDatasource> getDatasourceByDomain (final String domain) {
		return (ontologyVirtualDatasourceRepository.findByDatasourceDomain(domain));
	}
	
	@Override
	public void updateOntology(final OntologyVirtualDatasource datasource, Boolean maintainCredentials,
			String oldCredentials) {
		setUserPasswordDB(datasource, maintainCredentials, oldCredentials);
		ontologyVirtualDatasourceRepository.save(datasource);
		virtualDatasourcesManager.setDatasourceDescriptor(datasource.getIdentification());
	}

	@Override
	public void deleteDatasource(final OntologyVirtualDatasource datasource) {
		ontologyVirtualDatasourceRepository.delete(datasource);
	}

	@Override
	public Boolean checkConnection(final String dataSourceName, final String user, final String credentials,
			final String sgdb, final String url, final String queryLimit) throws GenericOPException {
		final VirtualDatasourceType type = VirtualDatasourceType.valueOf(sgdb);
		final String driverClassName = virtualDatasourcesManager.getDriverClassName(type);

		// Only to test connection from control panel. It has no sense to create a
		// pooling connection here
		final DriverManagerDataSource driverManagerDatasource = new DriverManagerDataSource();
		driverManagerDatasource.setDriverClassName(driverClassName);
		driverManagerDatasource.setUrl(url);
		if (user != null && !"".equals(user)) {
			driverManagerDatasource.setUsername(user);
		}
		if (credentials != null && !"".equals(credentials)) {
			driverManagerDatasource.setPassword(credentials);
		}

		final JdbcTemplate jdbcTemplate = new JdbcTemplate(driverManagerDatasource);
		final SQLHelper helper = virtualDatasourcesManager.getOntologyHelper(type);

		try {
			jdbcTemplate.queryForList(helper.getValidateQuery(), String.class);
			return true;
		} catch (Exception e) {
			log.error("Error checking connection to datasource", e);
			throw new GenericOPException(e.getMessage());
		}
	}

	@Override
	public Boolean changePublic(final String dataSource) {
		final OntologyVirtualDatasource virtualDataSource = ontologyVirtualDatasourceRepository
				.findByIdentification(dataSource);
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
				.findByIdentification(dataSourceName);
		String user = dataSource.getUserId();
		String credentials = dataSource.getCredentials();
		return this.checkConnection(dataSourceName, user,
				(credentials != null ? JasyptConfig.getEncryptor().decrypt(credentials) : null),
				dataSource.getSgdb().toString(), dataSource.getConnectionString(),
				String.valueOf(dataSource.getQueryLimit()));
	}

	private VirtualDatasourceType getTypeFromOntology(final String ontology) {
		return ontologyVirtualRepository.findOntologyVirtualDatasourceByOntologyIdentification(ontology).getSgdb();
	}

	// Set user and password to null when they are empty string and no encryption
	private void setUserPasswordDB(OntologyVirtualDatasource datasource, Boolean maintainCredentials,
			String oldCredentials) {
		if ("".equals(datasource.getUserId())) {
			datasource.setUserId(null);
		}
		if (!maintainCredentials) {
			if ("".equals(datasource.getCredentials())) {
				datasource.setCredentials(null);
			} else {
				datasource.setCredentials(JasyptConfig.getEncryptor().encrypt(datasource.getCredentials()));
			}
		} else {
			datasource.setCredentials(oldCredentials);
		}
	}

	@Override
	public String getUniqueColumn(final String ontology) {
		return this.ontologyVirtualRepository.findOntologyVirtualObjectIdByOntologyIdentification(ontology);
	}

	@Override
	public OntologyVirtualDatasource getDatasourceByIdAndUserId(String id, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (userService.isUserAdministrator(sessionUser)) {
			return ontologyVirtualDatasourceRepository.findById(id).orElse(null);
		} else {
			return ontologyVirtualDatasourceRepository.findByIdAndUser(id, sessionUser);
		}
	}

	@Override
	public List <OntologyVirtual> getAssociationExternalDatabase(String datasourcesId) {
		
		return ontologyVirtualRepository.findByDatasourcesId(datasourcesId);
	}
	
		
	@Override
	public OntologyVirtualDatasource getDatasourceByIdAndUserIdOrIsPublic(String id, String sessionUserId,
			ResourceAccessType type) {

		final OntologyVirtualDatasource datasource = ontologyVirtualDatasourceRepository.findById(id).orElse(null);
		final User sessionUser = userService.getUser(sessionUserId);
		if (datasource != null) {
			if (hasUserPermission(sessionUser, datasource, type)) {
				return datasource;
			} else {
				throw new VirtualDatasourceServiceException("The user is not authorized.");
			}
		} else {
			return null;
		}
	}

	private boolean hasUserPermission(User user, OntologyVirtualDatasource datasource, ResourceAccessType type) {
		if (datasource == null || user == null) {
			return false;
		}
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (datasource.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else if (datasource.isPublic() && type.equals(ResourceAccessType.VIEW)) {
			return true;
		} else {
			return resourceService.hasAccess(user.getUserId(), datasource.getId(), type);
		}
	}

}

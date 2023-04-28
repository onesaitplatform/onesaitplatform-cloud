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
package com.minsait.onesait.platform.business.services.presto.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.OntologyPrestoDatasource;
import com.minsait.onesait.platform.config.model.OntologyPrestoDatasource.PrestoDatasourceType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.repository.OntologyPrestoDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyPrestoRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.exceptions.PrestoDatasourceServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;
import com.minsait.onesait.platform.persistence.presto.PrestoOntologyBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.presto.catalog.PrestoCatalogManager;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrestoDatasourceServiceImpl implements PrestoDatasourceService {

	@Autowired
	private OntologyPrestoDatasourceRepository prestoDatasourceRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PrestoOntologyBasicOpsDBRepository prestoDBBasicOpsDBRepository;

	@Autowired
	private PrestoCatalogManager prestoCatalogManager;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private OntologyPrestoRepository ontologyPrestoRepository;

	@Autowired
	private PrestoDatasourceConfigurationService prestoDatasourceConfigurationService;

	@Override
	public OntologyPrestoDatasource getPrestoDatasourceById(String id) {
		return prestoDatasourceRepository.findById(id).orElse(null);
	}

	@Override
	public OntologyPrestoDatasource getPrestoDatasourceByIdentification(String identification) {
		return prestoDatasourceRepository.findByIdentification(identification);
	}

	@Override
	public Boolean existsPrestoDatasourceIdentification(String identification) {
		final OntologyPrestoDatasource datasourceDB = getPrestoDatasourceByIdentification(identification.toLowerCase());
		return (datasourceDB != null);
	}

	@Override
	public List<OntologyPrestoDatasource> getAllByIdentificationAndTypeAndUser(String identification,
			PrestoDatasourceType type, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);

		if (userService.isUserAdministrator(sessionUser)) {
			if ((identification == null || identification.trim().equals("")) && (type == null)) {
				return prestoDatasourceRepository.findAllByOrderByIdentificationAsc();
			} else if (!(identification == null || identification.trim().equals("")) && (type == null)) {
				return prestoDatasourceRepository.findAllByIdentificationLikeOrderByIdentificationAsc(identification);
			} else if ((identification == null || identification.trim().equals("")) && (type != null)) {
				return prestoDatasourceRepository.findAllByTypeOrderByIdentificationAsc(type);
			} else {
				return prestoDatasourceRepository
						.findAllByIdentificationLikeAndTypeOrderByIdentificationAsc(identification, type);
			}
		} else {
			if ((identification == null || identification.trim().equals("")) && (type == null)) {
				return prestoDatasourceRepository.findByIsPublicTrueOrderByIdentificationAsc();
			} else if (!(identification == null || identification.trim().equals("")) && (type == null)) {
				return prestoDatasourceRepository
						.findAllByIdentificationLikeAndIsPublicTrueOrderByIdentificationAsc(identification);
			} else if ((identification == null || identification.trim().equals("")) && (type != null)) {
				return prestoDatasourceRepository.findAllByTypeAndIsPublicTrueOrderByIdentificationAsc(type);
			} else {
				return prestoDatasourceRepository
						.findAllByIdentificationLikeAndTypeAndIsPublicTrueOrderByIdentificationAsc(identification,
								type);
			}
		}
	}

	@Override
	public List<String> getAllPrestoDatasourceIdentifications() {
		final List<OntologyPrestoDatasource> datasources = prestoDatasourceRepository
				.findAllByOrderByIdentificationAsc();
		return datasources.stream().map(OntologyPrestoDatasource::getIdentification).collect(Collectors.toList());
	}

	@Override
	public void createPrestoDatasource(OntologyPrestoDatasource datasource, Properties properties,
			String sessionUserId) {

		try {
			final List<PrestoConnectionProperty> configProperties = getConfigPropertiesByType(
					datasource.getType().getPrestoDatasourceType());
			for (PrestoConnectionProperty p : configProperties) {
				if (p.getRequired() && !properties.containsKey(p.getName())) {
					log.error("Error creating catalog file: Missing required properties for catalog");
					throw new PrestoDatasourceServiceException("Missing required properties for catalog.");
				}
			}
			final User sessionUser = userService.getUser(sessionUserId);
			datasource.setUser(sessionUser);

			create(datasource, properties);

		} catch (IOException e) {
			log.error("Error creating catalog file: " + e.getMessage());
			throw new PrestoDatasourceServiceException(e.getMessage(), e);
		}
	}

	@Transactional
	private void create(OntologyPrestoDatasource datasource, Properties properties) throws IOException {
		prestoCatalogManager.writeCatalogFile(datasource.getIdentification(), properties);
		prestoDatasourceRepository.saveAndFlush(datasource);
	}

	@Override
	public Properties getPropertiesFromPrestoDatasource(OntologyPrestoDatasource datasource) {
		Properties properties = new Properties();

		try {
			properties = prestoCatalogManager.readCatalogFile(datasource.getIdentification());
			List<PrestoConnectionProperty> configProperties = getConfigPropertiesByType(
					datasource.getType().getPrestoDatasourceType());
			for (PrestoConnectionProperty p : configProperties) {
				if (p.getEncrypt()) {
					final String value = properties.getProperty(p.getName());
					if (value != null) {
						properties.setProperty(p.getName(), JasyptConfig.getEncryptor().encrypt(value));
					}
				}
			}

		} catch (IOException e) {
			log.error("Error obtaining Presto Connection Properties configuration: " + e.getMessage());
			throw new PrestoDatasourceServiceException(e.getMessage());
		}
		return properties;
	}

	@Override
	public void updatePrestoDatasource(OntologyPrestoDatasource datasource, Properties properties) {

		try {
			final List<PrestoConnectionProperty> configProperties = getConfigPropertiesByType(
					datasource.getType().getPrestoDatasourceType());
			final Properties fileProperties = prestoCatalogManager.readCatalogFile(datasource.getIdentification());
			for (PrestoConnectionProperty p : configProperties) {
				if (p.getRequired() && !properties.containsKey(p.getName())) {
					log.error("Error creating catalog file: Missing required properties for catalog");
					throw new PrestoDatasourceServiceException("Missing required properties for catalog");
				}
				if (p.getEncrypt()) {
					final String value = properties.getProperty(p.getName());
					if (value != null && fileProperties.containsKey(p.getName())) {
						try {
							JasyptConfig.getEncryptor().decrypt(value).equals(fileProperties.get(p.getName()));
							properties.setProperty(p.getName(), JasyptConfig.getEncryptor().decrypt(value));
						} catch (EncryptionOperationNotPossibleException e) {
						}
					}
				}
			}
			update(datasource, properties);
		} catch (IOException e) {
			log.error("Error creating catalog file: " + e.getMessage());
			throw new PrestoDatasourceServiceException(e.getMessage(), e);
		}
	}

	@Transactional
	private void update(OntologyPrestoDatasource datasource, Properties properties) throws IOException {
		prestoCatalogManager.writeCatalogFile(datasource.getIdentification(), properties);
		prestoDatasourceRepository.save(datasource);
	}

	@Override
	@Transactional
	public void deletePrestoDatasource(OntologyPrestoDatasource datasource) {
		if (!isPrestoDatasourceRemovable(datasource.getIdentification())) {
			throw new PrestoDatasourceServiceException("Datasource cannot be deleted");
		} else if (hasPrestoDatasourceEntitiesAssociated(datasource.getIdentification())) {
			throw new PrestoDatasourceServiceException("Datasource cannot be deleted, it has entities associated");
		} else {
			prestoCatalogManager.deleteCatalogFile(datasource.getIdentification());
			prestoDatasourceRepository.delete(datasource);
		}
	}

	@Override
	public Boolean checkConnection(String catalog) {
		prestoDBBasicOpsDBRepository.getSchemas(catalog);
		return true;
	}

	@Override
	public List<PrestoConnectionProperty> getConfigPropertiesByType(String type)
			throws JsonMappingException, JsonProcessingException {

		List<PrestoConnectionProperty> properties = new ArrayList<>();

		Configuration config = configurationService.getConfiguration(Type.PRESTO_PROPERTIES,
				"Presto Connection Properties");
		if (config == null) {
			log.error("Error obtaining Presto Connection Properties configuration: configuration does not exist");
			throw new PrestoDatasourceServiceException(
					"Error obtaining Presto connection properties, configuration does not exist.");
		}
		String configValues = config.getYmlConfig();
		JsonNode jsonNodeconf = new ObjectMapper().readTree(configValues);
		ArrayNode arrayNode = (ArrayNode) jsonNodeconf.get(type);
		if (arrayNode == null) {
			log.error("Error obtaining Presto Connection Properties configuration: connection type does not exist");
			throw new PrestoDatasourceServiceException(
					"Error obtaining Presto connection properties, connection type does not exist in configuration.");
		}
		for (JsonNode node : arrayNode) {
			properties.add(PrestoConnectionProperty.builder().name(node.get("name").asText())
					.description(node.get("description").asText()).defaultValue(node.get("defaultValue").asText())
					.required(node.get("required").asBoolean()).encrypt(node.get("encrypt").asBoolean()).build());
		}

		return properties;
	}

	private Boolean isPrestoDatasourceRemovable(String catalog) {

		if (prestoDatasourceConfigurationService.isHistoricalCatalog(catalog)
				|| prestoDatasourceConfigurationService.isRealtimedbCatalog(catalog)) {
			return false;
		} else {
			return true;
		}
	}

	private Boolean hasPrestoDatasourceEntitiesAssociated(String catalog) {
		return !(ontologyPrestoRepository.findOntologyPrestoByDatasourceCatalog(catalog).isEmpty());
	}

	@Override
	public List<String> getPrestoCatalogsByUser(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.isAdmin()) {
			return prestoDatasourceRepository.findAll().stream().map(o -> o.getIdentification())
					.collect(Collectors.toList());
		} else {
			return prestoDatasourceRepository.findByIsPublicTrue().stream().map(o -> o.getIdentification())
					.collect(Collectors.toList());
		}
	}

}

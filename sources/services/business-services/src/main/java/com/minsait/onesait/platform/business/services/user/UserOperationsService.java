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
package com.minsait.onesait.platform.business.services.user;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserOperationsService {
	private static final String ANONYMOUS_USER = "anonymous";
	private static final String ADMIN_USER = "administrator";
	@Autowired
	private ManageDBPersistenceServiceFacade manageFacade;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DataModelRepository dataModelRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private RtdbDatasource defaultAuditDatasource = RtdbDatasource.OPEN_SEARCH;

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profileDetector;

	private int defaultReplicas = 0;
	private int defaultShards = 5;
	private String defaultPatternField = "formatedTimeStamp";
	private String defaultPatternFunction = "YEAR_MONTH";
	private static final Long DEFAULT_TTL = 77760000L;
	// No need to create policy
	// private String defaultTtlRetentionPeriod = "30d";
	// private Integer defaultTtlPriority = 10;

	@PostConstruct
	public void init() {

		try {
			final String profile = profileDetector.getActiveProfile();
			final GlobalConfiguration globalConfiguration = configurationService.getGlobalConfiguration(profile);
			final Map<String, Object> database = globalConfiguration.getEnv().getDatabase();

			@SuppressWarnings("unchecked")
			final Map<String, Object> opensearch = (Map<String, Object>) database.get("opensearch");
			@SuppressWarnings("unchecked")
			final Map<String, Object> defaults = (Map<String, Object>) opensearch.get("defaults");

			defaultReplicas = (int) defaults.get("replicas");
			defaultShards = (int) defaults.get("shards");
			defaultPatternField = (String) defaults.get("ttlPatternField");
			defaultPatternFunction = (String) defaults.get("ttlPatternFunction");
			// No need to create policy
			// defaultTtlRetentionPeriod = (String) defaults.get("ttlRetentionPeriod");
			// defaultTtlPriority = (int) defaults.get("ttlPriority");
		} catch (final Exception e) {
			log.warn("Error loading configuration values for elasticSearch indexes. Using defauts.");
		}

		final String defaultDatasource = resourcesService.getGlobalConfiguration().getEnv().getAudit()
				.getDefaultRtdbDatasource();

		if (defaultDatasource != null) {
			switch (defaultDatasource) {
			case "elasticsearch":
				defaultAuditDatasource = RtdbDatasource.ELASTIC_SEARCH;
				break;
			case "opensearch":
				defaultAuditDatasource = RtdbDatasource.OPEN_SEARCH;
				break;
			case "mongodb":
				defaultAuditDatasource = RtdbDatasource.MONGO;
				break;
			}
		}

	}

	public String getAuditOntology(String userId) {
		return ServiceUtils.getAuditCollectionName(userId);
	}

	public void createAuditOntology(String userId) {
		final User user = userService.getUser(userId);
		if (user != null) {
			createPostOperationsUser(user);
			createPostOntologyUser(user);
		} else if (userId.equals(ANONYMOUS_USER)) {
			createPostOperationsUser(userService.getUserByIdentification(ADMIN_USER), true);
			createPostOntologyUser(userService.getUserByIdentification(ADMIN_USER), true);
		}
	}

	public void deleteAuditOntology(String userId) {
		final User user = userService.getUser(userId);
		if (user != null) {
			final String collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());
			final Ontology ds = ontologyRepository.findByIdentification(collectionAuditName);
			if (ds != null) {
				manageFacade.removeTable4Ontology(collectionAuditName);
			}
		}
	}

	public void createPostOperationsUser(User user) {

		this.createPostOperationsUser(user, false);

	}

	public void createPostOperationsUser(User user, boolean anonymous) {
		final String collectionAuditName;
		if (anonymous) {
			collectionAuditName = ServiceUtils.getAuditCollectionName(ANONYMOUS_USER);
		} else {
			collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());
		}

		if (ontologyService.getOntologyByIdentification(collectionAuditName, user.getUserId()) == null) {
			final DataModel dataModel = dataModelRepository.findDatamodelsByIdentification("AuditPlatform");
			final Ontology ontology = new Ontology();
			ontology.setJsonSchema(dataModel.getJsonSchema());
			ontology.setIdentification(collectionAuditName);
			ontology.setDataModel(dataModel);
			ontology.setDescription(
					"System Ontology. Auditory of operations between user and Platform for user: " + user.getUserId());
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(false);
			ontology.setUser(user);
			// create audit ontology in datasource selected in audit config
			ontology.setRtdbDatasource(defaultAuditDatasource);
			OntologyConfiguration config = null;
			if (defaultAuditDatasource == RtdbDatasource.OPEN_SEARCH) {
				config = createElasticAuditConfigForTTL();
			}
			ontologyService.createOntology(ontology, config);

		}

	}

	private OntologyConfiguration createElasticAuditConfigForTTL() {
		final OntologyConfiguration config = new OntologyConfiguration();
		// Get default TTL config from centralized config
		config.setShards(String.valueOf(defaultShards));
		config.setReplicas(String.valueOf(defaultReplicas));
		config.setPatternField(defaultPatternField);
		config.setPatternFunction(defaultPatternFunction);
		config.setAllowsTemplateConfig(true);
		// no need to create TTL for template, as a global audit policy is created on
		// ConfigInit
		// config.setTtlRetentionPeriod(defaultTtlRetentionPeriod);
		// config.setTtlRetentionPeriod(String.valueOf(defaultTtlPriority));
		return config;
	}

	private void update(User user, RtdbDatasource datasource, boolean anonymous) {
		final String collectionAuditName;
		if (anonymous) {
			collectionAuditName = ServiceUtils.getAuditCollectionName(ANONYMOUS_USER);
		} else {
			collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());
		}

		final Ontology ontology = ontologyService.getOntologyByIdentification(collectionAuditName, user.getUserId());
		ontology.setRtdbDatasource(datasource);

		ontologyService.updateOntology(ontology, user.getUserId(), null);
	}

	public void createPostOntologyUser(User user, boolean anonymous) {
		final String collectionAuditName;
		if (anonymous) {
			collectionAuditName = ServiceUtils.getAuditCollectionName(ANONYMOUS_USER);
		} else {
			collectionAuditName = ServiceUtils.getAuditCollectionName(user.getUserId());
		}

		final DataModel dataModel = dataModelRepository.findDatamodelsByIdentification("AuditPlatform");

		Map<String, String> config = null;

		if (defaultAuditDatasource == RtdbDatasource.OPEN_SEARCH) {
			config = new HashMap<>();
			config.put("allowsTemplateConfig", "true");
			config.put("shards", String.valueOf(defaultShards));
			config.put("replicas", String.valueOf(defaultReplicas));
			config.put("patternField", String.valueOf(defaultPatternField));
			config.put("patternFunction", String.valueOf(defaultPatternFunction));
			// Audit Policy is created beforehand, no need to create new per user
			// config.put("ttlRetentionPeriod", String.valueOf(defaultTtlRetentionPeriod));
			// config.put("ttlPriority", String.valueOf(defaultTtlPriority));
		}
		try {
			manageFacade.createTable4Ontology(collectionAuditName, dataModel.getJsonSchema(), config);
			try {
				manageFacade.createTTLIndex(collectionAuditName, "formatedTimeStamp", DEFAULT_TTL);
			} catch (final Exception e) {
				// NO-OP only mongo
			}
		} catch (final Exception e) {
			log.error(
					"Audit ontology couldn't be created in ElasticSearch/OpenSearch, so we need Mongo to Store Something");
			update(user, RtdbDatasource.MONGO, anonymous);
			manageFacade.createTable4Ontology(collectionAuditName, dataModel.getJsonSchema(), null);
			manageFacade.createTTLIndex(collectionAuditName, "mongoTimestamp", getTTLMongo());
		}

	}

	public void createPostOntologyUser(User user) {

		this.createPostOntologyUser(user, false);
	}

	private Long getTTLMongo() {
		try {
			return (Long) resourcesService.getGlobalConfiguration().getEnv().getDatabase().get("mongodb-ttl");
		} catch (final Exception e) {
			return DEFAULT_TTL;
		}
	}
}

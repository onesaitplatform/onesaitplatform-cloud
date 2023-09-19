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
package com.minsait.onesait.platform.systemconfig.init;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.opensearch.api.OSBaseApi;
import com.minsait.onesait.platform.persistence.opensearch.api.OSInsertService;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.util.ElasticSearchFileUtil;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.opensearchdb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitOpenSearchDB {
	private boolean started = false;
	private User userCollaborator = null;
	private User userAdministrator = null;
	private User user = null;
	private User userAnalytics = null;
	private User userSysAdmin = null;
	private User userPartner = null;
	private User userOperation = null;

	private static final String ACCOUNTS_STR = "Accounts";
	private static final String TYPE_TEXT = "            \"type\": \"text\",\n";
	private static final String FIELDDATA_TRUE = "            \"fielddata\": true\n";

	@Autowired
	private ManageDBPersistenceServiceFacade manageFacade;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private UserRepository userCDBRepository;

	@Autowired
	private DataModelRepository datamodelRepository;

	@Autowired
	OSBaseApi connector;
	@Autowired
	OSInsertService sSInsertService;

	@Value("${onesaitplatform.database.opensearch.default.replicas:0}")
	private Integer defaultReplicas;
	@Value("${onesaitplatform.database.opensearch.default.shards:1}")
	private Integer defaultShards;
	@Value("${onesaitplatform.database.opensearch.default.ttlPatternField:formatedTimeStamp}")
	private String defaultTtlPatternField;
	@Value("${onesaitplatform.database.opensearch.default.ttlPatternFunction:YEAR_MONTH}")
	private String defaultTtlPatternFunction;
	@Value("${onesaitplatform.database.opensearch.default.ttlRetentionPeriod:30d}")
	private String defaultTtlRetentionPeriod;
	@Value("${onesaitplatform.database.opensearch.default.ttlPriority:10}")
	private Integer defaultTtlPriority;

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;
			// initAccountsDataSet();
			// we suppose that we have created Users and roles
			initAuditOntology();
			log.info("OK init_AuditOntology");

		}

	}

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					StandardCharsets.UTF_8);

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						StandardCharsets.UTF_8);
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}

	public void initAccountsDataSet() {
		try {
			log.info("init_AccountsDataSet");

			final String INDEX_NAME = "accounts";
			if (ontologyService.getOntologyByIdentification(ACCOUNTS_STR, getUserDeveloper().getUserId()) == null) {
				final Ontology ontology = new Ontology();
				ontology.setId("MASTER-Ontology-Accounts-1");
				ontology.setDataModel(datamodelRepository.findByIdentification("EmptyBase").get(0));
				ontology.setJsonSchema(loadFromResources("examples/Accounts-schema.json"));
				ontology.setIdentification(ACCOUNTS_STR);
				ontology.setDescription("Accounts Example for user " + getUserDeveloper().getUserId());
				ontology.setActive(true);
				ontology.setRtdbClean(false);
				ontology.setRtdbToHdb(false);
				ontology.setPublic(false);
				ontology.setUser(getUserDeveloper());
				ontology.setRtdbDatasource(RtdbDatasource.OPEN_SEARCH);
				ontologyService.createOntology(ontology, null);
			}
			try {
				connector.deleteIndex(INDEX_NAME);
			} catch (final Exception e) {
				log.error("Error deleting Restaurants DataSet...ignoring", e);
			}
			final String dataMapping = "{  \"" + INDEX_NAME + "\": {" + " \"properties\": {\n"
					+ "          \"gender\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          },"
					+ "          \"address\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          },"
					+ "          \"state\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          }" + "       }" + "   }"
					+ "}";
			final Map<String, Property> mapping = JSONPersistenceUtilsElasticSearch
					.getOpenSearchSchemaFromJSONSchema(dataMapping);
			connector.createIndex(INDEX_NAME, mapping, null);
			// connector.prepareIndex(INDEX_NAME, dataMapping);

			final List<String> list = ElasticSearchFileUtil.readLines(
					new File(getClass().getClassLoader().getResource("examples/Accounts-dataset.json").getFile()));

			final List<String> result = list.stream().filter(x -> x.startsWith("{\"account_number\""))
					.collect(Collectors.toList());
			OntologyElastic elasticOntol = ontologyService.getOntologyElasticByOntologyId(
					ontologyService.getOntologyByIdentification(ACCOUNTS_STR, getUserDeveloper().getUserId()));
			sSInsertService.bulkInsert(elasticOntol, result);

		} catch (final Exception e) {
			log.error("Error creating Restaurants DataSet...ignoring", e);
		}
	}

	private User getUserDeveloper() {
		if (userCollaborator == null)
			userCollaborator = userCDBRepository.findByUserId("developer");
		return userCollaborator;
	}

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = userCDBRepository.findByUserId("administrator");
		return userAdministrator;
	}

	private User getUser() {
		if (user == null)
			user = userCDBRepository.findByUserId("user");
		return user;
	}

	private User getUserAnalytics() {
		if (userAnalytics == null)
			userAnalytics = userCDBRepository.findByUserId("analytics");
		return userAnalytics;
	}

	private User getUserPartner() {
		if (userPartner == null)
			userPartner = userCDBRepository.findByUserId("partner");
		return userPartner;
	}

	private User getUserSysAdmin() {
		if (userSysAdmin == null)
			userSysAdmin = userCDBRepository.findByUserId("sysadmin");
		return userSysAdmin;
	}

	private User getUserOperations() {
		if (userOperation == null)
			userOperation = userCDBRepository.findByUserId("operations");
		return userOperation;
	}

	public void createPostOperationsUser(User user) {
		createPostOperationsUser(user, ServiceUtils.getAuditCollectionName(user.getUserId()));
	}

	public void createPostOperationsUser(User user, String collectionAuditName) {

		if (ontologyService.getOntologyByIdentification(collectionAuditName, user.getUserId()) == null) {
			final DataModel dataModel = datamodelRepository.findByIdentification("AuditPlatform").get(0);
			final Ontology ontology = new Ontology();

			ontology.setJsonSchema(dataModel.getJsonSchema());
			ontology.setDataModel(dataModel);
			ontology.setIdentification(collectionAuditName);
			ontology.setDescription(
					"System Ontology. Auditory of operations between user and Platform for user: " + user.getUserId());
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(false);
			ontology.setUser(user);
			ontology.setRtdbDatasource(RtdbDatasource.OPEN_SEARCH);
			OntologyConfiguration config = new OntologyConfiguration();
			// Get default TTL config from centralized config
			config.setShards(String.valueOf(defaultShards));
			config.setReplicas(String.valueOf(defaultReplicas));
			config.setPatternField(defaultTtlPatternField);
			config.setPatternFunction(defaultTtlPatternFunction);
			config.setAllowsTemplateConfig(true);
			ontologyService.createOntology(ontology, config);

		}

	}

	private void update(User user, String collectionAuditName, RtdbDatasource datasource) {
		final Ontology ontology = ontologyService.getOntologyByIdentification(collectionAuditName, user.getUserId());
		ontology.setRtdbDatasource(datasource);
		ontologyService.updateOntology(ontology, user.getUserId(), null);
	}

	public void createPostOntologyUser(User user) {
		createPostOntologyUser(user, ServiceUtils.getAuditCollectionName(user.getUserId()));
	}

	public void createPostOntologyUser(User user, String collectionAuditName) {
		try {
			final DataModel dataModel = datamodelRepository.findDatamodelsByIdentification("AuditPlatform");
			HashMap<String, String> config = new HashMap<>();
			config.put("allowsTemplateConfig", "true");
			config.put("shards", String.valueOf(defaultShards));
			config.put("replicas", String.valueOf(defaultReplicas));
			config.put("patternField", String.valueOf(defaultTtlPatternField));
			config.put("patternFunction", String.valueOf(defaultTtlPatternFunction));
			manageFacade.createTable4Ontology(collectionAuditName, dataModel.getJsonSchema(), config);
		} catch (final Exception e) {
			log.error("Audit ontology couldn't be created in OpenSearch, so we need Mongo to Store Something");
			update(user, collectionAuditName, RtdbDatasource.MONGO);
			try {
				manageFacade.createTable4Ontology(collectionAuditName, "{}", null);
			} catch (final Exception ex) {
				log.error("error creating ontology table", ex);
			}
		}
	}

	public void initAuditOntology() {
		log.info("create deletion policy - TTL for old audit indexes");
		
		//recreate audit_ policy
		connector.deleteTTLPolicy("platform-audit-policy");
		connector.createTTLPolicy("platform-audit-policy", "audit_*", defaultTtlRetentionPeriod, defaultTtlPriority);
		
		//recreate Gravitee plolicy
		connector.deleteTTLPolicy("gravitee-ttl-policy");
		connector.createTTLPolicy("gravitee-ttl-policy", "gravitee-*", defaultTtlRetentionPeriod, defaultTtlPriority);
		
		//apply policies to existing Indices
		connector.addPolicyToIndices("platform-audit-policy", "audit_*");
		connector.addPolicyToIndices("gravitee-ttl-policy", "gravitee-*");
		
		log.info("adding audit ontologies...");

		createPostOperationsUser(getUserAdministrator());
		createPostOntologyUser(getUserAdministrator());

		createPostOperationsUser(getUserDeveloper());
		createPostOntologyUser(getUserDeveloper());

		createPostOperationsUser(getUser());
		createPostOntologyUser(getUser());

		createPostOperationsUser(getUserAnalytics());
		createPostOntologyUser(getUserAnalytics());

		createPostOperationsUser(getUserPartner());
		createPostOntologyUser(getUserPartner());

		createPostOperationsUser(getUserSysAdmin());
		createPostOntologyUser(getUserSysAdmin());

		createPostOperationsUser(getUserOperations());
		createPostOntologyUser(getUserOperations());
		//
		createPostOperationsUser(getUserAdministrator(), ServiceUtils.getAuditCollectionName("anonymous"));
		createPostOntologyUser(getUserAdministrator(), ServiceUtils.getAuditCollectionName("anonymous"));

	}
}

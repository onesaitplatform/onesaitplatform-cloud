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
package com.minsait.onesait.platform.systemconfig.init;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.elasticdb")
@RunWith(SpringRunner.class)
@Order(3)
@SpringBootTest
public class InitElasticSearchDB {

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
	ESBaseApi connector;
	@Autowired
	ESInsertService sSInsertService;

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;
			initAccountsDataSet();
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
				ontology.setRtdbDatasource(RtdbDatasource.ELASTIC_SEARCH);
				ontologyService.createOntology(ontology, null);
			}

			connector.deleteIndex(INDEX_NAME);
			connector.createIndex(INDEX_NAME);
			final String dataMapping = "{  \"" + INDEX_NAME + "\": {" + " \"properties\": {\n"
					+ "          \"gender\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          },"
					+ "          \"address\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          },"
					+ "          \"state\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          }" + "       }" + "   }"
					+ "}";
			connector.createType(INDEX_NAME, INDEX_NAME, dataMapping);

			final List<String> list = ESInsertService.readLines(
					new File(getClass().getClassLoader().getResource("examples/Accounts-dataset.json").getFile()));

			final List<String> result = list.stream().filter(x -> x.startsWith("{\"account_number\""))
					.collect(Collectors.toList());

			sSInsertService.load(INDEX_NAME, INDEX_NAME, result, ontologyService
					.getOntologyByIdentification(ACCOUNTS_STR, getUserDeveloper().getUserId()).getJsonSchema());

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
			DataModel dataModel = datamodelRepository.findByIdentification("AuditPlatform").get(0);
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
			ontology.setRtdbDatasource(RtdbDatasource.ELASTIC_SEARCH);

			ontologyService.createOntology(ontology, null);

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
			manageFacade.createTable4Ontology(collectionAuditName, "{}", null);
		} catch (final Exception e) {
			log.error("Audit ontology couldn't be created in ElasticSearch, so we need Mongo to Store Something");
			update(user, collectionAuditName, RtdbDatasource.MONGO);
			try {
				manageFacade.createTable4Ontology(collectionAuditName, "{}", null);
			} catch (final Exception ex) {
				log.error("error creating ontology table", ex);
			}
		}
	}

	public void initAuditOntology() {
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

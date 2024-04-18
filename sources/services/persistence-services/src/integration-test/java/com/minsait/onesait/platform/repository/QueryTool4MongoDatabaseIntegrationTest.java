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
package com.minsait.onesait.platform.repository;

import java.util.List;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.services.QueryAsTextMongoDBImpl;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Ignore
public class QueryTool4MongoDatabaseIntegrationTest {

	private static final String ONTOLOGY_NAME = "OntologyTest" + System.currentTimeMillis();
	private static final String JSON_TEST = "{" + "\"name\":\"Jack\"," + "\"job\":\"Administrator\","
			+ "\"location\":\"Spain\"" + "}";
	private static final String JSON_TEST_UPDATE = "{" + "\"name\":\"John\"," + "\"job\":\"Developer\","
			+ "\"location\":\"France\"" + "}";
	private static final String SQL_TEST = "select * from ";

	private static User userAdministrator = null;

	@Autowired
	QueryToolService queryTool;

	@Autowired
	QueryAsTextMongoDBImpl queryMongo;

	@Autowired
	@Qualifier("MongoManageDBRepository")
	ManageDBRepository manageMongo;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	BasicOpsDBRepository repositoryMongo;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	UserRepository userCDBRepository;

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = this.userCDBRepository.findByUserId("administrator");
		return userAdministrator;
	}

	// private String SQL_TEST = "select * from "+database+"/"+TEST_INDEX_ONLINE;

	@Before
	public void doBefore() {
		log.warn("THIS INTEGRATION TESTS NEEED TO HAVE CONFIGDB AND RTDB MONGO STARTED PREVIOUSLY");
		log.info("UP process...");

		try {
			Ontology ontology = new Ontology();
			ontology.setJsonSchema("{}");
			ontology.setIdentification(ONTOLOGY_NAME);
			ontology.setDescription("Description 4 " + ONTOLOGY_NAME);
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setRtdbDatasource(RtdbDatasource.MONGO);
			ontology.setUser(getUserAdministrator());

			Ontology ont1 = ontologyService.getOntologyByIdentification(ONTOLOGY_NAME,
					getUserAdministrator().getUserId());
			if (ont1 == null)
				ontologyService.createOntology(ontology, null);

			manageMongo.createTable4Ontology(ONTOLOGY_NAME, "{}", null);

			String idMongo = repositoryMongo.insert(ONTOLOGY_NAME, JSON_TEST);
			log.info("Returned Mongo inserted object with id " + idMongo);

			Thread.sleep(5000);

		} catch (Exception e) {
			log.info("Issue creating table4ontology " + e);
		}

	}

	@After
	public void tearDown() {
		log.info("teardown process...");
		try {
			manageMongo.removeTable4Ontology(ONTOLOGY_NAME);

		} catch (Exception e) {
			log.error("Issue deleting table4ontology " + e);
		}
		try {
			ontologyRepository.deleteById(ONTOLOGY_NAME);

		} catch (Exception e) {
			log.error("Issue deleting ONTOLOGY " + e);
		}

	}

	@Test
	public void testFindAllOnTestOntology() {
		try {
			List<String> listData = repositoryMongo.findAll(ONTOLOGY_NAME);
			log.info("Returned list of found objects " + listData);
			Assert.assertTrue(!listData.isEmpty());
		} catch (Exception e) {
			Assert.fail("testInsertCountDelete failure. " + e);
		}
	}

	@Test
	public void testSQLSelectAllOnTestOntology() {
		try {
			String sql = SQL_TEST + " " + ONTOLOGY_NAME;
			String outpoutSQL2 = repositoryMongo.querySQLAsJson(ONTOLOGY_NAME, sql);
			JSONArray jsonObj2 = new JSONArray(outpoutSQL2);
			Assert.assertTrue(jsonObj2.length() > 0);
		} catch (Exception e) {
			Assert.fail("testSQLSelectAllOnTestOntology failure. " + e);
		}
	}

}

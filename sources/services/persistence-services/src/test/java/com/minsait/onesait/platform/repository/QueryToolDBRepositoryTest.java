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
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.elasticsearch.ElasticSearchQueryAsTextDBRepository;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.services.QueryAsTextMongoDBImpl;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Ignore
public class QueryToolDBRepositoryTest {

	public static final String TEST_INDEX = "test" + System.currentTimeMillis();
	public static final String TEST_INDEX_ONLINE = "ontology" + TEST_INDEX;
	public static final String TEST_INDEX_ONLINE_ELASTIC = "elastic" + TEST_INDEX_ONLINE;

	private static User userAdministrator = null;

	@Autowired
	QueryToolService queryTool;

	@Autowired
	QueryAsTextMongoDBImpl queryMongo;

	@Autowired
	ElasticSearchQueryAsTextDBRepository queryElasticSearch;

	@Autowired
	@Qualifier("MongoManageDBRepository")
	ManageDBRepository manageMongo;

	@Autowired
	@Qualifier("ElasticSearchManageDBRepository")
	ManageDBRepository manageElasticSearch;

	@Autowired
	@Qualifier("ElasticSearchBasicOpsDBRepository")
	BasicOpsDBRepository repositoryElasticSearch;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	BasicOpsDBRepository repositoryMongo;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	UserRepository userCDBRepository;

	@Autowired
	ESBaseApi connector;

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = this.userCDBRepository.findByUserId("administrator");
		return userAdministrator;
	}

	private String JSON_TEST = "{" + "\"name\":\"skyji\"," + "\"job\":\"Admin\"," + "\"location\":\"India\"" + "}";

	private String JSON_TEST_UPDATE = "{" + "\"name\":\"pepe\"," + "\"job\":\"pepe\"," + "\"location\":\"pepe\"" + "}";

	private String SQL_TEST = "select * from ";
	// private String SQL_TEST = "select * from "+database+"/"+TEST_INDEX_ONLINE;

	@Before
	public void doBefore() {
		log.warn("THIS INTEGRATION TESTS NEEED TO HAVE RTDB MONGO STARTED PREVIOUSLY");
		log.info("UP process...");

		try {
			connector.deleteIndex("test*");
			Ontology ontology = new Ontology();
			ontology.setJsonSchema("{}");
			ontology.setIdentification(TEST_INDEX_ONLINE);
			ontology.setDescription(TEST_INDEX_ONLINE);
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setRtdbDatasource(RtdbDatasource.MONGO);
			ontology.setUser(getUserAdministrator());

			Ontology index1 = ontologyService.getOntologyByIdentification(TEST_INDEX_ONLINE,
					getUserAdministrator().getUserId());
			if (index1 == null)
				ontologyService.createOntology(ontology, null);

			manageMongo.createTable4Ontology(TEST_INDEX_ONLINE, "{}", null);

			ontology = new Ontology();
			ontology.setJsonSchema("{}");
			ontology.setIdentification(TEST_INDEX_ONLINE_ELASTIC);
			ontology.setDescription(TEST_INDEX_ONLINE_ELASTIC);
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setRtdbDatasource(RtdbDatasource.ELASTIC_SEARCH);
			ontology.setUser(getUserAdministrator());

			index1 = ontologyService.getOntologyByIdentification(TEST_INDEX_ONLINE_ELASTIC,
					getUserAdministrator().getUserId());
			if (index1 == null)
				ontologyService.createOntology(ontology, null);

			manageElasticSearch.createTable4Ontology(TEST_INDEX_ONLINE_ELASTIC, "{}", null);

			String idES = repositoryElasticSearch.insert(TEST_INDEX_ONLINE_ELASTIC, JSON_TEST);
			String idMongo = repositoryMongo.insert(TEST_INDEX_ONLINE, JSON_TEST);
			log.info("Returned ES inserted object with id " + idES);
			log.info("Returned Mongo inserted object with id " + idMongo);

			Thread.sleep(10000);

		} catch (Exception e) {
			log.info("Issue creating table4ontology " + e);
		}

	}

	@After
	public void tearDown() {
		log.info("teardown process...");
		try {
			manageMongo.removeTable4Ontology(TEST_INDEX_ONLINE);
			manageElasticSearch.removeTable4Ontology(TEST_INDEX_ONLINE_ELASTIC);

		} catch (Exception e) {
			log.info("Issue deleting table4ontology " + e);
		}

		try {
			ontologyRepository.deleteById(TEST_INDEX_ONLINE_ELASTIC);

		} catch (Exception e) {
			log.info("Issue deleting TEST_INDEX_ONLINE_ELASTIC " + e);
		}

		try {
			ontologyRepository.deleteById(TEST_INDEX_ONLINE);

		} catch (Exception e) {
			log.info("Issue deleting TEST_INDEX_ONLINE " + e);
		}

	}

	@Test
	public void testSearchQueryFindAll() {
		try {

			log.info("testSearchQuery");

			List<String> listDataES = repositoryElasticSearch.findAll(TEST_INDEX_ONLINE_ELASTIC);
			log.info("Returned list of found objects " + listDataES);

			List<String> listData = repositoryMongo.findAll(TEST_INDEX_ONLINE);
			log.info("Returned list of found objects " + listData);

			log.info("testSearchQuery END ");

			Assert.assertTrue(listData != null && listDataES != null);
		} catch (Exception e) {
			Assert.fail("testInsertCountDelete failure. " + e);
		}
	}

	@Test
	public void testSearchQuery() {
		try {

			log.info("testSearchQuery 11111");

			String sql = SQL_TEST + "" + TEST_INDEX_ONLINE_ELASTIC;

			String outpoutSQL = repositoryElasticSearch.querySQLAsJson(TEST_INDEX_ONLINE_ELASTIC, sql);

			log.info("Returned SQL " + outpoutSQL);

			JSONArray jsonObj = new JSONArray(outpoutSQL);

			sql = SQL_TEST + "" + TEST_INDEX_ONLINE;

			String outpoutSQL2 = repositoryMongo.querySQLAsJson(TEST_INDEX_ONLINE, sql);

			log.info("Returned SQL " + outpoutSQL2);

			JSONArray jsonObj2 = new JSONArray(outpoutSQL2);

			log.info("testSearchQuery END ");

			Assert.assertTrue(jsonObj != null && jsonObj2 != null);
		} catch (Exception e) {
			Assert.fail("testInsertCountDelete failure. " + e);
		}
	}

	@Test
	public void testSearchQuery2() {
		try {

			log.info("testSearchQuery 22222");

			String sql = SQL_TEST + "" + TEST_INDEX_ONLINE_ELASTIC;

			String outpoutSQL = queryTool.querySQLAsJson(getUserAdministrator().getUserId(), TEST_INDEX_ONLINE_ELASTIC,
					sql, 0);

			log.info("Returned SQL " + outpoutSQL);

			JSONArray jsonObj = new JSONArray(outpoutSQL);

			sql = SQL_TEST + "" + TEST_INDEX_ONLINE;

			String outpoutSQL2 = queryTool.querySQLAsJson(getUserAdministrator().getUserId(), TEST_INDEX_ONLINE, sql,
					0);

			log.info("Returned SQL " + outpoutSQL2);

			JSONArray jsonObj2 = new JSONArray(outpoutSQL2);

			log.info("testSearchQuery END ");

			Assert.assertTrue(jsonObj != null && jsonObj2 != null);
		} catch (Exception e) {
			Assert.fail("testSearchQuery failure. " + e);
		}
	}

	@Test
	public void testSearchQuery3() {
		try {

			log.info("testSearchQuery 22222");

			String sql = SQL_TEST + "" + TEST_INDEX_ONLINE;

			String outpoutSQL2 = queryTool.querySQLAsJson(getUserAdministrator().getUserId(), TEST_INDEX_ONLINE, sql,
					0);

			log.info("Returned SQL " + outpoutSQL2);

			JSONArray jsonObj2 = new JSONArray(outpoutSQL2);

			log.info("testSearchQuery END ");

			outpoutSQL2 = queryTool.querySQLAsJson(getUserAdministrator().getUserId(), TEST_INDEX_ONLINE, sql, 0);

			log.info("Returned SQL " + outpoutSQL2);

			jsonObj2 = new JSONArray(outpoutSQL2);

			outpoutSQL2 = queryTool.querySQLAsJson(getUserAdministrator().getUserId(), TEST_INDEX_ONLINE, sql, 0);

			log.info("Returned SQL " + outpoutSQL2);

			jsonObj2 = new JSONArray(outpoutSQL2);

			Assert.assertTrue(jsonObj2 != null);
		} catch (Exception e) {
			Assert.fail("testSearchQuery failure. " + e);
		}
	}

}

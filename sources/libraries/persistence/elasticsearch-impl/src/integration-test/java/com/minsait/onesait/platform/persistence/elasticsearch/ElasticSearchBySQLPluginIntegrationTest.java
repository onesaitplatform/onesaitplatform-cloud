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
package com.minsait.onesait.platform.persistence.elasticsearch;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;
import com.minsait.onesait.platform.persistence.elasticsearch.sql.connector.ElasticSearchSQLDbHttpImpl;
import com.minsait.onesait.platform.persistence.util.ElasticSearchFileUtil;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@Slf4j
public class ElasticSearchBySQLPluginIntegrationTest {

	private final static String TEST_INDEX_ACCOUNT = "account";
	private final static String TEST_ONTOLOGY_IDENTIFICATION = "Accounts";

	@Autowired
	private ElasticSearchSQLDbHttpImpl httpConnector;


	@Autowired
	private ESBaseApi connector;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ESInsertService sSInsertService;

	//private String dataMapping = "{\"properties\": {\"gender\": {\"type\": \"text\",\"fielddata\": true }, \"address\": {\"type\": \"text\", \"fielddata\": true }, \"state\": {\"type\": \"text\", \"fielddata\": true}}}";

	@Before
	public void setUp() throws Exception {
		
		//connector.createIndex(TEST_INDEX_ACCOUNT, dataMapping, null);

		//Get "Accounts" ontology. This is created in ConfigInit.
		Ontology testOntology = ontologyService.getOntologyByIdentification(TEST_ONTOLOGY_IDENTIFICATION);
		OntologyElastic elasticOntol = ontologyService.getOntologyElasticByOntologyId(testOntology);
		
		final List<String> list = ElasticSearchFileUtil
				.readLines(new File(this.getClass().getClassLoader().getResource("accounts.json").toURI()));

		final List<String> result = list.stream().filter(x -> x.startsWith("{\"account_number\""))
				.collect(Collectors.toList());

		sSInsertService.bulkInsert(elasticOntol, result);

		Thread.sleep(5000);

	}

	@After
	public void tearDown() {
		log.info("teardown process...");
		connector.deleteIndex(TEST_INDEX_ACCOUNT);
	}

	@Test
	public void test_SQL_Select_MAX() {
		try {
			final String query = "select max(age) from " + TEST_INDEX_ACCOUNT;
			final String result = httpConnector.queryAsJson(query, 100);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("\"value\":40") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_SelectWhere() {
		try {
			final String query = "select * from " + TEST_INDEX_ACCOUNT + " where lastname='Santos'";
			final String result = httpConnector.queryAsJson(query, 500);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("Santos") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_SelectGroup() {
		try {
			// final String query = "select gender,count(*) from " + TEST_INDEX_ACCOUNT + "
			// group by gender";
			final String query = "select topHits('size'=3,'include'='age,firstname',age='desc') from "
					+ TEST_INDEX_ACCOUNT + " group by gender";
			final String result = httpConnector.queryAsJson(query, 500);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("topHits") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_SelectAll() {
		try {
			final String query = "select * from " + TEST_INDEX_ACCOUNT;
			final String result = httpConnector.queryAsJson(query, 500);
			log.info("Returned:" + result);
			Assert.assertTrue(result.split("account_number").length == 501);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_Count() {
		try {
			final String query = "select count(*) from " + TEST_INDEX_ACCOUNT + " where lastname='Santos'";
			final String result = httpConnector.queryAsJson(query, 1);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("\"value\":1") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_CountAndGroup() {
		try {
			final String query = "select count(*),sum(balance) from " + TEST_INDEX_ACCOUNT
					+ " where age in (35,36) group by gender";
			final String result = httpConnector.queryAsJson(query, 1);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("\"value\":59") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_SelectAll_Page() {
		try {
			final String query = "select * from " + TEST_INDEX_ACCOUNT;
			final String result = httpConnector.queryAsJson(query, 100, 200);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("{\"account_number\"") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_Select_SUM() {
		try {
			final String query = "SELECT SUM(balance + balance) as doubleSum FROM " + TEST_INDEX_ACCOUNT;
			final String result = httpConnector.queryAsJson(query, 1000);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("{\"doubleSum\"") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_Select_WithFilter() {
		try {
			final String query = "select age , firstname from " + TEST_INDEX_ACCOUNT
					+ " where age > 31 order by age desc";
			final String result = httpConnector.queryAsJson(query, 1000);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("{\"firstname\"") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_Select_WithLike() {
		try {
			final String query = "select * from " + TEST_INDEX_ACCOUNT + " where firstname NOT LIKE 'amb%'";
			final String result = httpConnector.queryAsJson(query, 1000);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("{\"account_number\"") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}

	@Test
	public void test_SQL_Select_cast() {
		try {
			final String query = "select cast(age as double)/2 from " + TEST_INDEX_ACCOUNT;
			final String result = httpConnector.queryAsJson(query, 1000);
			log.info("Returned:" + result);
			Assert.assertTrue(result.indexOf("{\"account_number\"") != -1);
		} catch (final Exception e) {
			Assert.fail("Error:" + e.getMessage());
		}
	}
}

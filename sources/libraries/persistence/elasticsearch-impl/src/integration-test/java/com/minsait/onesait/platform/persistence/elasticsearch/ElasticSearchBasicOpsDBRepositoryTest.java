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
package com.minsait.onesait.platform.persistence.elasticsearch;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = { "com.minsait.onesait.platform"})
@EnableAutoConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@Slf4j
public class ElasticSearchBasicOpsDBRepositoryTest {

	public final static String TEST_INDEX = "test" + System.currentTimeMillis();
	public final static String TEST_INDEX_ONLINE = TEST_INDEX + "_online";

	@Autowired
	ElasticSearchBasicOpsDBRepository repository;

	@Autowired
	ElasticSearchManageDBRepository manage;
	
	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Configuration
    static class Config {
        @Bean
        @Primary
        public OntologyRepository getOntologyRepository() {
            return mock(OntologyRepository.class);
        }
        
        @Bean("dataHubRest")
        public RestTemplate restTemplate() throws GenericOPException {
            return new RestTemplate();
        }
    }
	
	@Autowired
    private OntologyRepository ontologyRepository;

	private static final String JSON_TEST = "{" + "\"name\":\"skyji\"," + "\"job\":\"Admin\"," + "\"location\":\"India\"" + "}";

	private static final String JSON_TEST_UPDATE = "{" + "\"name\":\"pepe\"," + "\"job\":\"pepe\"," + "\"location\":\"pepe\"" + "}";

	private static final String SQL_TEST = "select * from ";

    private static final String QUERY_ALL = "{\"match_all\" : {}}";

    
    private static final String JSON_SCHEMA = "{\n" + 
            "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" + 
            "    \"title\": \"test\",\n" + 
            "    \"type\": \"object\",\n" + 
            "\n" + 
            "\n" + 
            "        \"description\": \"Properties for DataModel test\",\n" + 
            "\n" + 
            "        \"required\": [\n" + 
            "            \"name\",\n" + 
            "            \"job\",\n" + 
            "            \"location\"\n" + 
            "        ],\n" + 
            "        \"properties\": {\n" + 
            "            \"name\": {\n" + 
            "                \"type\": \"string\"\n" + 
            "            },\n" + 
            "            \"job\": {\n" + 
            "                \"type\": \"string\"\n" + 
            "            },\n" + 
            "            \"location\": {\n" + 
            "                \"type\": \"string\"\n" + 
            "            }\n" + 
            "        },\n" + 
            "    \"additionalProperties\": true\n" + 
            "}";

	@Before
	public void doBefore() throws Exception {
		log.info("up process...");
		manage.createTable4Ontology(TEST_INDEX_ONLINE, JSON_SCHEMA, null);
		Ontology ontology = new Ontology();
		ontology.setId(TEST_INDEX_ONLINE);
		ontology.setIdentification(TEST_INDEX_ONLINE);
		ontology.setJsonSchema(JSON_SCHEMA);
		when(ontologyRepository.findByIdentification(ontology.getIdentification())).thenReturn(ontology);
	}

	@After
	public void tearDown() {
		log.info("teardown process...");
		try {
			manage.removeTable4Ontology(TEST_INDEX_ONLINE);
		} catch (Exception e) {
			log.info("Issue deleting table4ontology " + e);
		}

	}

	@Test
	public void testInsertAndGet() {
		try {
			log.info("testInsertAndGet");

			String id = repository.insert(TEST_INDEX_ONLINE, JSON_TEST);

			log.info("Returned inserted object with id " + id);

			String resultById = repository.findById(TEST_INDEX_ONLINE, id);

			log.info("Returned searched object with this data " + resultById);

			log.info("testInsertAndGet END ");

			Assert.assertTrue(!resultById.isEmpty());
		} catch (Exception e) {
			Assert.fail("testInsertAndGet failure. " + e);
		}
	}

	@Test
	public void testInsertCountDelete() {
		try {
			log.info("testInsertCountDelete");

			String id = repository.insert(TEST_INDEX_ONLINE, JSON_TEST);
			log.info("Returned inserted object with id " + id);

			long many = repository.count(TEST_INDEX_ONLINE);
			log.info("Returned count object with type " + TEST_INDEX_ONLINE + " size: " + many);

			long size = repository.deleteNativeById(TEST_INDEX_ONLINE, id).getCount();
			log.info("Returned delete object with type " + TEST_INDEX_ONLINE + " size: " + size + " id " + id);
			Thread.sleep(5000);

			many = repository.count(TEST_INDEX_ONLINE);
			log.info("Returned count object after deleting with type " + TEST_INDEX_ONLINE + " size: " + many);

			log.info("testInsertCountDelete END ");

			Assert.assertTrue(!id.isEmpty());
		} catch (Exception e) {
			Assert.fail("testInsertCountDelete failure. " + e);
		}
	}

	@Test
	public void testInsertUpdate() {
		try {

			log.info("testInsertUpdate");

			String id = repository.insert(TEST_INDEX_ONLINE, JSON_TEST);
			log.info("Returned inserted object with id " + id);

			long many = repository.updateNativeByObjectIdAndBodyData(TEST_INDEX_ONLINE, id, JSON_TEST_UPDATE)
					.getCount();
			log.info("Returned count updateNativeByObjectIdAndBodyData " + TEST_INDEX_ONLINE + " id: " + id + " count:"
					+ many);

			String resultById = repository.findById(TEST_INDEX_ONLINE, id);

			log.info("Returned searched object with this data " + resultById);

			log.info("testInsertUpdate END ");

			Assert.assertTrue(many == 1);
		} catch (Exception e) {
			Assert.fail("testInsertUpdate failure. " + e);
		}
	}

	@Test
	public void test_1_SearchQuery() {
		try {

			log.info("testSearchQuery");

			String id = repository.insert(TEST_INDEX_ONLINE, JSON_TEST);
			log.info("Returned inserted object with id " + id);

			List<String> listData = repository.findAll(TEST_INDEX_ONLINE);
			log.info("Returned list of found objects " + listData);

			String sql = SQL_TEST + " " + TEST_INDEX_ONLINE;
			log.info("Testing " + sql);
			String outpoutSQL = repository.querySQLAsJson(TEST_INDEX_ONLINE, sql);
			log.info("Returned SQL " + outpoutSQL);

			sql = "select count(*) from " + TEST_INDEX_ONLINE;
			log.info("Testing " + sql);
			outpoutSQL = repository.querySQLAsJson(TEST_INDEX_ONLINE, sql);
			log.info("Returned SQL " + outpoutSQL);

			log.info("testSearchQuery END ");

			Assert.assertTrue(outpoutSQL != null);
		} catch (Exception e) {
			Assert.fail("testSearchQuery failure. " + e);
		}
	}

	@Test
	public void testSearchQueryNative() {
		try {

			log.info("testSearchQuery");

			String id = repository.insert(TEST_INDEX_ONLINE, JSON_TEST);
			log.info("Returned inserted object with id " + id);

			List<String> listData = repository.findAll(TEST_INDEX_ONLINE);
			log.info("Returned list of found objects " + listData);

			String output = repository.queryNativeAsJson(TEST_INDEX_ONLINE, QUERY_ALL);

			log.info("query native :" + output);
			log.info("testSearchQuery END ");

			Assert.assertTrue(output != null);
		} catch (Exception e) {
			Assert.fail("testSearchQueryNative failure. " + e);
		}
	}

}

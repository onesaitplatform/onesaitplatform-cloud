/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.mongodb;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.model.ContextData;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.mongodb.services.QueryAsTextMongoDBImpl;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplateImpl;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@TestPropertySource(properties = { "spring.config.location=classpath:application-integration-test.yml" })
@Slf4j
public class QuerySQLMongoDBIntegrationTest {

	@TestConfiguration
	static class ContextConfiguration {
		@Bean("dataHubRest")
		public RestTemplate restTemplate() throws GenericOPException {
			final RestTemplate rt = new RestTemplate();
			return rt;
		}
	}

	@Autowired
	QueryAsTextMongoDBImpl queryTool;

	@Autowired
	MongoDbTemplateImpl connect;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	BasicOpsDBRepository repository;


	private static final String ONT_NAME = "contextData";
	private static final String DATABASE = "onesaitplatform_rtdb";

	String refOid = "";

	@Before
	public void setUp() throws Exception {
		log.warn(
				"This Integration Test needs MongoDB RTDB, ConfigDB and Quasar started and correctly configured with ConfigInit Module");
		if (!connect.collectionExists(DATABASE, ONT_NAME))
			connect.createCollection(DATABASE, ONT_NAME);
		// 1º
		ContextData data = ContextData
				.builder("user", UUID.randomUUID().toString(), UUID.randomUUID().toString(), System.currentTimeMillis(),
						"Testing")
				.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
				.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
		ObjectMapper mapper = new ObjectMapper();
		refOid = repository.insert(ONT_NAME, mapper.writeValueAsString(data));
		// 2º
		data = ContextData
				.builder("admin", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
						System.currentTimeMillis(), "Testing")
				.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
				.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
		mapper = new ObjectMapper();
		refOid = repository.insert(ONT_NAME, mapper.writeValueAsString(data));
		// 3º
		data = ContextData
				.builder("other", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
						System.currentTimeMillis(), "Testing")
				.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
				.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
		mapper = new ObjectMapper();
		refOid = repository.insert(ONT_NAME, mapper.writeValueAsString(data));
	}

	@After
	public void tearDown() {
		connect.dropCollection(DATABASE, ONT_NAME);
	}

	@Test
	public void test1_QuerySQLWithLimit() {
		try {
			String json = queryTool.querySQLAsJson(ONT_NAME, "select * from " + ONT_NAME + " limit 2", 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test1_QueryNativeLimit" + e.getMessage());
		}
	}

	@Test
	public void test3_QuerySQLSort() {
		try {
			String json = queryTool.querySQLAsJson(ONT_NAME, "select * from " + ONT_NAME + " order by user ASC", 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test3_QueryNativeSort" + e.getMessage());
		}
	}

	@Test
	public void test4_QuerySQLSkip() {
		try {
			String json = queryTool.querySQLAsJson(ONT_NAME, "select * from " + ONT_NAME + " offset 2", 0);
			Assert.assertTrue(json.indexOf("other") != -1);
		} catch (Exception e) {
			Assert.fail("Error test4_QueryNativeSkip" + e.getMessage());
		}
	}

	@Test
	public void test8_QuerySQLFindUser() {
		try {
			String json = queryTool.querySQLAsJson(ONT_NAME, "select * from " + ONT_NAME + " where user='admin'", 0);
			Assert.assertTrue(json.indexOf("admin") != -1);
		} catch (Exception e) {
			Assert.fail("Error test8_QueryNativeFindUser" + e.getMessage());
		}
	}

}

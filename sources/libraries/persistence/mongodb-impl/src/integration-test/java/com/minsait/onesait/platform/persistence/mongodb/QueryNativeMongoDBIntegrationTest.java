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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class QueryNativeMongoDBIntegrationTest {

	@Autowired
	QueryAsTextMongoDBImpl queryTool;

	@Autowired
	MongoDbTemplateImpl connect;

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	BasicOpsDBRepository repository;

	@Autowired
	MongoTemplate nativeTemplate;
	
	private static final String ONT_NAME = "contextData";
	private static final String DATABASE = "onesaitplatform_rtdb";

	String refOid = "";

	@Before
	public void setUp() throws Exception{
		log.warn("This Integration Test needs MongoDB RTDB and ConfigDB started and correctly configured with ConfigInit Module");
		if (!connect.collectionExists(DATABASE, ONT_NAME))
			connect.createCollection(DATABASE, ONT_NAME);
		// 1º
		ContextData data = ContextData
				.builder("user", UUID.randomUUID().toString(), UUID.randomUUID().toString(), System.currentTimeMillis(), "Testing")
				.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
				.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
		ObjectMapper mapper = new ObjectMapper();
		refOid = repository.insert(ONT_NAME, "", mapper.writeValueAsString(data));
		// 2º
		data = ContextData
				.builder("admin", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
						System.currentTimeMillis(), "Testing")
				.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
				.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
		mapper = new ObjectMapper();
		refOid = repository.insert(ONT_NAME, "", mapper.writeValueAsString(data));
		// 3º
		data = ContextData
				.builder("other", UUID.randomUUID().toString(), UUID.randomUUID().toString(),
						System.currentTimeMillis(), "Testing")
				.clientConnection(UUID.randomUUID().toString()).deviceTemplate(UUID.randomUUID().toString())
				.device(UUID.randomUUID().toString()).clientSession(UUID.randomUUID().toString()).build();
		mapper = new ObjectMapper();
		refOid = repository.insert(ONT_NAME, "", mapper.writeValueAsString(data));
	}

	@After
	public void tearDown() {
		connect.dropCollection(DATABASE, ONT_NAME);
	}


	@Test
	public void test1_QueryNativeLimit() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "db." + ONT_NAME + ".find({'user':'user'}).limit(2)", 0,
					0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test1_QueryNativeLimit" + e.getMessage());
		}
	}

	@Test
	public void test2_QueryNativeProjections() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME,
					"db." + ONT_NAME + ".find({'user':'user'},{user:1,_id:0})", 0, 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test2_QueryNativeProjections" + e.getMessage());
		}
	}

	@Test
	public void test3_QueryNativeSort() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "db." + ONT_NAME + ".find().sort({'user':-1})", 0, 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test3_QueryNativeSort" + e.getMessage());
		}
	}

	@Test
	public void test4_QueryNativeSkip() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "db." + ONT_NAME + ".find().skip(2)", 0, 0);
			Assert.assertTrue(json.indexOf("other") != -1);
		} catch (Exception e) {
			Assert.fail("Error test4_QueryNativeSkip" + e.getMessage());
		}
	}

	@Test
	public void test5_QueryNativeType4() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "db." + ONT_NAME + ".find()", 0, 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test5_QueryNativeType4" + e.getMessage());
		}
	}

	@Test
	public void test6_QueryNativeWithOutFind() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "{}", 0, 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test6_QueryNativeWithOutFind" + e.getMessage());
		}
	}

	@Test
	public void test7_QueryNativeOnlyParams() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "{'user':'user'}", 0, 0);
			Assert.assertTrue(json.indexOf("user") != -1);
		} catch (Exception e) {
			Assert.fail("Error test7_QueryNativeOnlyParams" + e.getMessage());
		}
	}

	@Test
	public void test8_QueryNativeFindUser() {
		try {
			String json = queryTool.queryNativeAsJson(ONT_NAME, "db." + ONT_NAME + ".find({\"user\":\"admin\"})", 0, 0);
			Assert.assertTrue(json.indexOf("admin") != -1);
		} catch (Exception e) {
			Assert.fail("Error test8_QueryNativeFindUser" + e.getMessage());
		}
	}


	@Test
	public void test9_createAndDropIndex() {
		try {
			String result = queryTool.queryNativeAsJson(ONT_NAME,
					"db.contextData.createIndex({'user':1},{'name':'user_i'})");
			Assert.assertTrue(result.indexOf("Created index") != -1);
			result = queryTool.queryNativeAsJson(ONT_NAME, "db.contextData.getIndexes()");
			Assert.assertTrue(result.indexOf("user_i") != -1);
			result = queryTool.queryNativeAsJson(ONT_NAME, "db.contextData.dropIndex('user_i')");
			Assert.assertTrue(result.indexOf("Dropped index") != -1);
		} catch (Exception e) {
			Assert.fail("test9_createAndDropIndex:" + e.getMessage());
		}
	}

	@Test
	public void test10_InsertAndUpdateAndRemove() {
		try {
			String result = queryTool.queryNativeAsJson(ONT_NAME, "db.contextData.count()");
			Assert.assertTrue(result.indexOf("0") == -1);
			//
			result = queryTool.queryNativeAsJson(ONT_NAME,
					"db.contextData.insert({\"user\":\"user_temp_1\",\"deviceTemplate\":\"1\"})");
			Assert.assertTrue(result.indexOf("Inserted row") != -1);
			result = queryTool.queryNativeAsJson(ONT_NAME, "db.contextData.remove({\"user\":\"user_temp_1\"})");
			Assert.assertTrue(result.indexOf("{\"count\":1}") != -1);
			//
			result = queryTool.queryNativeAsJson(ONT_NAME,
					"db.contextData.insert({'user':'user_temp_2','deviceTemplate':'2'})");
			Assert.assertTrue(result.indexOf("Inserted row") != -1);
			result = queryTool.queryNativeAsJson(ONT_NAME,
					"db.contextData.update({'user':'user_temp_2'},{'deviceTemplate':'3'})");
			Assert.assertTrue(result.indexOf("{\"count\":1}") != -1);
			//

			result = queryTool.queryNativeAsJson(ONT_NAME,
					"db.contextData.remove({'user':'user_temp_2','deviceTemplate':'3'})");
			Assert.assertTrue(result.indexOf("{\"count\":1}") != -1);

		} catch (Exception e) {
			Assert.fail("Error test10_InsertAndUpdateAndRemove:" + e.getMessage());
		}
	}

}

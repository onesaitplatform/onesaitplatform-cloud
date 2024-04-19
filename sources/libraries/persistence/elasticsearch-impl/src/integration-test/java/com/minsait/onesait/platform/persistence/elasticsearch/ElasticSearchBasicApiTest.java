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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;
import com.minsait.onesait.platform.persistence.util.ElasticSearchFileUtil;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class ElasticSearchBasicApiTest {

	public final static String TEST_INDEX = "test" + System.currentTimeMillis();
	public final static String TEST_INDEX_GAME_OF_THRONES = TEST_INDEX + "_game_of_thrones";
	public final static String TEST_INDEX_ONLINE = TEST_INDEX + "_online";
	public String dataMapping = "{  \"" + TEST_INDEX_GAME_OF_THRONES + "\": { " + " \"properties\": {\n"
			+ " \"nickname\": {\n" + "\"type\":\"text\", " + "\"fielddata\":true" + "},\n" + " \"name\": {\n"
			+ "\"properties\": {\n" + "\"firstname\": {\n" + "\"type\": \"text\",\n" + "  \"fielddata\": true\n"
			+ "},\n" + "\"lastname\": {\n" + "\"type\": \"text\",\n" + "  \"fielddata\": true\n" + "},\n"
			+ "\"ofHerName\": {\n" + "\"type\": \"integer\"\n" + "},\n" + "\"ofHisName\": {\n"
			+ "\"type\": \"integer\"\n" + "}\n" + "}\n" + "}" + "} } }";
	@Autowired
	ESBaseApi connector;

	@Autowired
	ESInsertService sSInsertService;

	private boolean createTestIndex(String index) {
		final boolean res = connector.createIndex(index);
		log.info("createTestIndex :" + res);
		return res;
	}

	@After
	public void tearDown() {
		log.info("teardown process...");

		try {
			deleteTestIndex(TEST_INDEX_ONLINE);
		} catch (final Exception e) {
			log.error("Something happens when deleting indexes :" + e.getMessage());
		}

	}

	private void deleteTestIndex(String index) {
		final boolean res = connector.deleteIndex(index);
		log.info("deleteTestIndex :" + res);
	}

	@Test
	public void testCreateTable() {
		try {
			createTestIndex(TEST_INDEX_ONLINE);
			List<String> list = ElasticSearchFileUtil
					.readLines(new File(this.getClass().getClassLoader().getResource("online.json").toURI()));

			List<String> result = list.stream().filter(x -> x.startsWith("{\"0\"")).collect(Collectors.toList());

			ComplexWriteResult r = sSInsertService.bulkInsert(TEST_INDEX_ONLINE, result, dataMapping);

			log.info("Loaded Bulk :" + r.getData().size());

			Assert.assertTrue(r.getData().size() > 0);
		} catch (final Exception e) {
			Assert.fail("No connection. " + e);
		}
	}

}

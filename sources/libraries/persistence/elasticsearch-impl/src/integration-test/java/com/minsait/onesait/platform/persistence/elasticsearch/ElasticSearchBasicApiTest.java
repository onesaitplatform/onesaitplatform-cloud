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
package com.minsait.onesait.platform.persistence.elasticsearch;

import static org.mockito.Mockito.when;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESBaseApi;
import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;
import com.minsait.onesait.platform.persistence.util.ElasticSearchFileUtil;

import lombok.extern.slf4j.Slf4j;

//@RunWith(MockitoJUnitRunner.Silent.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class ElasticSearchBasicApiTest {

	private static final String ACCOUNTS_STR = "Accounts" + System.currentTimeMillis();
	private static final String ACCOUNTS_ONTOLOGY = ACCOUNTS_STR + System.currentTimeMillis();
	private static final String TYPE_TEXT = "            \"type\": \"text\",\n";
	private static final String FIELDDATA_TRUE = "            \"fielddata\": true\n";
	@Autowired
	ESBaseApi connector;
	@Autowired
	ESInsertService sSInsertService;

	@MockBean
	private OntologyElastic elasticOntol;

	@MockBean
	private Ontology ontology;

	private boolean createTestIndex() {

		deleteTestIndex();
		final String dataMapping = "{  \"" + ACCOUNTS_STR + "\": {" + " \"properties\": {\n"
				+ "          \"gender\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          },"
				+ "          \"address\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          },"
				+ "          \"state\": {\n" + TYPE_TEXT + FIELDDATA_TRUE + "          }" + "       }" + "   }" + "}";

		final boolean res = connector.createIndex(ACCOUNTS_ONTOLOGY.toLowerCase(), dataMapping, null);
		log.info("createTestIndex :" + res);
		return res;
	}

	@After
	public void tearDown() {
		log.info("teardown process...");

		try {
			deleteTestIndex();
		} catch (final Exception e) {
			log.error("Something happens when deleting indexes :" + e.getMessage());
		}

	}

	private void deleteTestIndex() {
		try {
			final boolean res = connector.deleteIndex(ACCOUNTS_ONTOLOGY.toLowerCase());
			log.info("deleteTestIndex :" + res);
		} catch (final Exception e) {
			log.error("Something happens when deleting indexes :" + e.getMessage());
		}
	}

	@Test
	public void testCreateTable() {
		try {

			// Create index
			createTestIndex();
			// Insert Data
			final List<String> list = ElasticSearchFileUtil
					.readLines(new File(getClass().getClassLoader().getResource("Accounts-dataset.json").getFile()));

			final List<String> result = list.stream().filter(x -> x.startsWith("{\"account_number\""))
					.collect(Collectors.toList());
			when(elasticOntol.getOntologyId()).thenReturn(ontology);
			when(elasticOntol.getOntologyId().getIdentification()).thenReturn(ACCOUNTS_ONTOLOGY);
			when(elasticOntol.getCustomIdConfig()).thenReturn(false);
			when(elasticOntol.getTemplateConfig()).thenReturn(false);
			ComplexWriteResult r = sSInsertService.bulkInsert(elasticOntol, result);

			log.info("Loaded Bulk :" + r.getData().size());

			Assert.assertTrue(r.getData().size() > 0);
		} catch (final Exception e) {
			Assert.fail("No connection. " + e);
		}
	}
}

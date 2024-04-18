/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.config.repository;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyAI;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@Slf4j
//@Category(IntegrationTest.class)
public class DeleteMeTest {

	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyAIRepository ontologyAIRepository;

	@Test
	public void ontologyAITest() {
		final Ontology concreteData = ontologyRepository.findByIdentification("concrete_data");
		if (concreteData != null && concreteData.getOntologyAI() == null) {
			Ontology oNew = new Ontology();
			oNew.setActive(true);
			oNew.setIdentification(concreteData.getIdentification() + "_strength");
			oNew.setRtdbDatasource(RtdbDatasource.AI_MINDS_DB);
			oNew.setUser(concreteData.getUser());
			oNew.setJsonSchema(concreteData.getJsonSchema());
			oNew.setDataModel(concreteData.getDataModel());
			final OntologyAI oAI = new OntologyAI();
			oAI.setConnectionName("mongo_local");
			oAI.setInputProperties("");
			oAI.setTargetProperties("strength");
			oAI.setOntology(oNew);
			oAI.setSourceEntity(concreteData.getIdentification());
			oAI.setOriginalDatasource(concreteData.getRtdbDatasource());
			oAI.setPredictor("strength");
			oNew = ontologyRepository.save(oNew);
			ontologyAIRepository.save(oAI);
		}
	}

	@Test
	public void testSplit() {
		String server = "realtimedb:27017";
		assertTrue("27017".equals(server.split(",")[0].split(":")[1]));
		server = "onesaitplatform-cogapro-shard-00-00.7wlzg.mongodb.net:27017,onesaitplatform-cogapro-shard-00-01.7wlzg.mongodb.net:27017,onesaitplatform-cogapro-shard-00-02.7wlzg.mongodb.net:27017";
		assertTrue("27017".equals(server.split(",")[0].split(":")[1]));
	}

	@Test
	public void testQuery() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		RestTemplate restTemplate = new RestTemplate();

		final ResponseEntity<JsonNode> queryResponse = restTemplate.exchange("http://127.0.0.1:47334/api/sql/query",
				HttpMethod.POST,
				new HttpEntity<>(
						"{\"query\":\"SELECT * FROM mindsdb.strength_predictor where when_data='{\\\"age\\\": 28, \\\"superPlasticizer\\\": 2.5, \\\"slag\\\": 1, \\\"water\\\": 162, \\\"fineAggregate\\\": 1040}'\",\"context\":{\"db\":\"mindsdb\"}}",
						headers),
				JsonNode.class);
	}

}

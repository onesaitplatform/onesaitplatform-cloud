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
package com.minsait.onesait.platform.simulator.job;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.simulator.job.utils.JsonUtils2;
import com.minsait.onesait.platform.simulator.service.FieldRandomizerService;
import com.minsait.onesait.platform.simulator.service.FieldRandomizerServiceImpl;
import com.minsait.onesait.platform.simulator.service.IoTBrokerClient;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
public class DeviceSimulatorTest {

	@TestConfiguration
	static class DeviceSimulatorJobTestContextConfiguration {

		@Bean
		public DeviceSimulatorJob deviceSimulatorJob() {
			return new DeviceSimulatorJob();
		}

		@Bean
		public FieldRandomizerService fieldRandomizerService() {
			return new FieldRandomizerServiceImpl();
		}
		
		@Bean
		public JsonUtils2 jsonUtils2() {
			return new JsonUtils2();
		}
		
		@Bean ObjectMapper objectMapper() {
			return new ObjectMapper();
		}
	}

	@MockBean
	IoTBrokerClient persistenceService;
	@MockBean
	OntologyService ontologyService;
	@Autowired
	FieldRandomizerService fieldRandomizerService;
	
	@Autowired
	private DeviceSimulatorJob deviceSimulatorJob;

	private String user;
	private String json;
	private String jsonSchema;
	
	@Mock
	JobExecutionContext jobContext;
	@Mock
	JobDetail jobDetail;
	@Mock
	JobDataMap jobDataMap;
	@Mock
	private Ontology ontology;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.user = "administrator";
		this.json = "{\"clientPlatform\":\"DeviceTemp\",\"clientPlatformInstance\":\"DeviceTemp:TempSimulate\",\"token\":\"170c81ecbb3347179acf690efe48f9c3\",\"ontology\":\"Ontology\",\"fields\":{\"Temp\":{\"function\":\"RANDOM_NUMBER\",\"from\":\"5\",\"to\":\"35\",\"precision\":\"0\"}}}";
		this.jsonSchema = "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"Ontology\",\"type\":\"object\",\"required\":[\"Ontology\"],\"properties\":{\"Ontology\":{\"type\":\"string\",\"$ref\":\"#/datos\"}},\"datos\":{\"description\":\"Info EmptyBase\",\"type\":\"object\",\"required\":[\"Temp\"],\"properties\":{\"Temp\":{\"type\":\"number\"}}},\"description\":\"Ontology test\",\"additionalProperties\":true}";
		this.initMocks();
	}

	public void initMocks() throws Exception {
		this.ontology = new Ontology();
		this.ontology.setJsonSchema(jsonSchema);

		Mockito.doNothing().when(this.persistenceService).insertOntologyInstance(any(), any(), any(), any(), any());
		when(this.ontologyService.getOntologyByIdentification(any(), any())).thenReturn(this.ontology);
		when(this.jobContext.getJobDetail()).thenReturn(this.jobDetail);
		when(this.jobDetail.getJobDataMap()).thenReturn(this.jobDataMap);
		when(this.jobDataMap.getString("userId")).thenReturn("administrator");
		when(this.jobDataMap.getString("json")).thenReturn(this.json);

	}

	@Test
	public void Test_SimulateTempValues() throws Exception {

		JsonNode randomInstance = this.deviceSimulatorJob.generateInstanceAndInsert(this.user, this.json);

		Assert.assertTrue(randomInstance.get("Ontology").get("Temp").asInt() >= 5);
		Assert.assertTrue(randomInstance.get("Ontology").get("Temp").asInt() <= 35);
	}

	@Test
	public void Test_JobExecution() throws IOException {
		this.deviceSimulatorJob.execute(this.jobContext);
	}

	@Ignore
	@Test
	public void Test_fails_when_schema_is_invalid() {
		//todo
	}
}

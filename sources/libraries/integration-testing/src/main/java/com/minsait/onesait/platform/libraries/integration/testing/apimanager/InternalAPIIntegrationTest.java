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
package com.minsait.onesait.platform.libraries.integration.testing.apimanager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.libraries.integration.testing.IntegrationTestingApp;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTestingApp.class)
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InternalAPIIntegrationTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private APIUtils apiUtils;

	@Autowired
	private ObjectMapper mapper;

	private int nInstances = 0;

	private static final String JSON_PATH_TEMPERATURE = "temperature";
	private static final String JSON_PATH_OID = "_id";
	private static final String JSON_PATH_COUNT = "count";

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${apimanager:http://localhost:19100/api-manager}")
	private String apiManager;

	@Value("${oauth-server:http://localhost:21000/oauth-server}")
	private String oauthServer;

	private static final String BASE_URL = "/server/api/v1/";
	private static final String PATH_GET_CRITICAL = "/critical/%d";
	private static final String PATH_UPDATE_OID = "/update/%s";
	private static final String PATH_DELETE_ALLL = "/delete";

	private static final String DATASET_SENSOR_TAG = "SensorTagInstances.json";

	@BeforeClass
	public void setUp() throws IOException {
		apiUtils.createExternalAPI();
		apiUtils.createInternalAPI();
		if (nInstances == 0)
			loadSampleData();
	}

	@AfterClass
	public void tearDown() {
		apiUtils.deleteResources();
	}

	@Test
	public void When_NoTokenIsProvided_Expect_403() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

		final ResponseEntity<String> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG), HttpMethod.GET,
				new HttpEntity<>(null, headers), String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.FORBIDDEN));
	}

	@Test
	public void When_GetByIdIsInvoked_Then_OneInstanceIsFetched() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		final JsonNode instance = response.getBody().get(0);

		final ResponseEntity<ArrayNode> responseSingleResult = restTemplate.exchange(
				getURL(APIUtils.API_SENSOR_TAG + "/" + instance.path(JSON_PATH_OID).asText()), HttpMethod.GET,
				new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);
		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));
		assertTrue(responseSingleResult.getBody() != null && responseSingleResult.getBody().get(0) != null);
		assertTrue(responseSingleResult.getBody().get(0).path(APIUtils.SENSOR_TAG).path("id").asText()
				.equals(instance.path(APIUtils.SENSOR_TAG).path("id").asText()));

	}

	@Test
	public void When_UpdateByIdIsInvoked_Then_OneInstanceIsUpdated() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		final String id = instance.path(JSON_PATH_OID).asText();
		((ObjectNode) instance.path(APIUtils.SENSOR_TAG)).put(JSON_PATH_TEMPERATURE, 125);
		((ObjectNode) instance).remove(JSON_PATH_OID);
		final ResponseEntity<JsonNode> responseSingleResult = restTemplate.exchange(
				getURL(APIUtils.API_SENSOR_TAG + "/" + id), HttpMethod.PUT,
				new HttpEntity<>(instance, apiUtils.headers()), JsonNode.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));
		assertTrue(responseSingleResult.getBody() != null);
		assertTrue(responseSingleResult.getBody().path(APIUtils.SENSOR_TAG).path(JSON_PATH_TEMPERATURE)
				.asInt() == instance.path(APIUtils.SENSOR_TAG).path(JSON_PATH_TEMPERATURE).asInt());

	}

	@Test
	public void When_UpdateByIdSQLIsInvoked_Then_OneInstanceIsUpdated() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		final String id = instance.path(JSON_PATH_OID).asText();
		final ResponseEntity<JsonNode> responseSingleResult = restTemplate.exchange(
				getURL(APIUtils.API_SENSOR_TAG) + String.format(PATH_UPDATE_OID, id), HttpMethod.GET,
				new HttpEntity<>(null, apiUtils.headers()), JsonNode.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));
		assertTrue(responseSingleResult.getBody() != null);
		assertTrue(!responseSingleResult.getBody().get("count").isMissingNode());

	}

	@Test
	public void When_InsertInstanceWithBadSchema_Expect_500() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		((ObjectNode) instance.path(APIUtils.SENSOR_TAG)).remove(JSON_PATH_TEMPERATURE);
		((ObjectNode) instance).remove(JSON_PATH_OID);
		final ResponseEntity<String> responseSingleResult = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.POST, new HttpEntity<>(instance, apiUtils.headers()), String.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));

	}

	@Test
	public void When_InsertInstance_Expect_200() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		((ObjectNode) instance).remove(JSON_PATH_OID);
		final ResponseEntity<String> responseSingleResult = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.POST, new HttpEntity<>(instance, apiUtils.headers()), String.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));

	}

	@Test
	public void When_RequestingGETCustomQueryFilteringByHighTemperature_Expect_MoreThanOneInstanceAndLessThanTotal() {

		final int criticalTempValue = 80;
		final ResponseEntity<ArrayNode> response = restTemplate.exchange(
				getURL(APIUtils.API_SENSOR_TAG) + String.format(PATH_GET_CRITICAL, criticalTempValue), HttpMethod.GET,
				new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(response.getBody().size() >= 1 && response.getBody().size() < nInstances);

	}

	@Test
	public void When_DeleteById_Expect_InstanceDeleted() {
		final ResponseEntity<ArrayNode> reference = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);

		final JsonNode instance = reference.getBody().get(0);
		final ResponseEntity<JsonNode> response = restTemplate.exchange(
				getURL(APIUtils.API_SENSOR_TAG) + "/" + instance.path(JSON_PATH_OID).asText(), HttpMethod.DELETE,
				new HttpEntity<>(null, apiUtils.headers()), JsonNode.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(response.getBody().path(JSON_PATH_COUNT).asInt() == 1);

	}

	@Test
	public void A_When_GetAllMethodIsInvoked_Expect_NOntologyInstancesFetched() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG),
				HttpMethod.GET, new HttpEntity<>(null, apiUtils.headers()), ArrayNode.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertNotNull(response.getBody());
		assertTrue(response.getBody().size() == nInstances);

	}

	@Test
	public void Z_When_RequestingDELETECustomQuery_Then_AllInstancesAreDeleted() {

		final ResponseEntity<JsonNode> response = restTemplate.exchange(
				getURL(APIUtils.API_SENSOR_TAG) + PATH_DELETE_ALLL, HttpMethod.GET,
				new HttpEntity<>(null, apiUtils.headers()), JsonNode.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(response.getBody().path(JSON_PATH_COUNT).asInt() == nInstances);

	}

	private void loadSampleData() {
		try {
			final ArrayNode instances = mapper.readValue(apiUtils.loadFromResources(DATASET_SENSOR_TAG),
					ArrayNode.class);

			assert instances != null;
			nInstances = instances.size();
			instances.forEach(this::insertInstance);
		} catch (final Exception e) {
			log.error("Could not load sample data for test");
		}
	}

	private void insertInstance(JsonNode instance) {
		restTemplate.exchange(getURL(APIUtils.API_SENSOR_TAG), HttpMethod.POST,
				new HttpEntity<>(instance, apiUtils.headers()), String.class);
	}

	private String getURL(String apiIdentification) {
		return apiManager + BASE_URL + apiIdentification;
	}

}

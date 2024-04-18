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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@EnableAutoConfiguration
public class InternalAPIIntegrationTest {

	@Autowired
	private APIUtils apiUtils;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private ApiManagerService apimanagerService;

	private Api petStore;
	private Api sensorTag;
	private User user;

	private int nInstances = 0;

	private static final String X_OP_APIKEY = "X-OP-APIKey";
	private static final String JSON_PATH_TEMPERATURE = "temperature";
	private static final String JSON_PATH_OID = "_id";
	private static final String JSON_PATH_COUNT = "count";

	private TestRestTemplate restTemplate;

	@Value("${apimanager:http://localhost:19100/api-manager}")
	private String apiManager;
	private static final String BASE_URL = "/server/api/v1/";
	private static final String PATH_GET_CRITICAL = "/critical/%d";
	private static final String PATH_UPDATE_OID = "/update/%s";
	private static final String PATH_DELETE_ALLL = "/delete";
	private static final String PATH_CREATE_API_REST = "/services/management/apis";

	private static final String ADMIN = "administrator";

	private static final String DATASET_SENSOR_TAG = "SensorTagInstances.json";

	@Autowired
	@Qualifier("MongoBasicOpsDBRepository")
	private BasicOpsDBRepository basicOps;
	@Autowired
	@Qualifier("MongoManageDBRepository")
	private ManageDBRepository manageDb;

	@Before
	public void setUp() throws IOException {
		if (restTemplate == null)
			restTemplate = new TestRestTemplate();
		if (user == null)
			user = userRepository.findByUserId(ADMIN);
		if (petStore == null)
			petStore = apiUtils.createExternalAPI(user, ApiType.EXTERNAL_FROM_JSON);
		if (sensorTag == null) {
			final ApiDTO apiDTO = apiUtils.readInternalAPIDTO(user);
			createApi(apiDTO);
			sensorTag = apimanagerService.getApiByIdentificationVersionOrId(apiDTO.getIdentification(),
					String.valueOf(apiDTO.getVersion()));
		}
		if (nInstances == 0)
			loadSampleData();
	}

	public void createApi(ApiDTO api) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(X_OP_APIKEY, apiUtils.getUserToken(user));
		restTemplate.exchange(apiManager + PATH_CREATE_API_REST, HttpMethod.POST, new HttpEntity<>(api, headers),
				String.class);

	}

	@After
	public void tearDown() {
		removeSampleData();
		apiUtils.deleteAPITest(petStore);
		apiUtils.deleteAPITest(sensorTag);
	}

	@Test
	public void When_NoTokenIsProvided_Expect_403() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

		final ResponseEntity<String> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.FORBIDDEN));
	}

	@Test
	public void When_GetAllMethodIsInvoked_Expect_NOntologyInstancesFetched() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(X_OP_APIKEY, apiUtils.getUserToken(user));

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers), ArrayNode.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertNotNull(response.getBody());
		assertTrue(response.getBody().size() == nInstances);

	}

	@Test
	public void When_GetByIdIsInvoked_Then_OneInstanceIsFetched() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		final JsonNode instance = response.getBody().get(0);

		final ResponseEntity<ArrayNode> responseSingleResult = restTemplate.exchange(
				getURL(sensorTag.getIdentification() + "/" + instance.path(JSON_PATH_OID).asText()), HttpMethod.GET,
				new HttpEntity<>(null, headers()), ArrayNode.class);
		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));
		assertTrue(responseSingleResult.getBody() != null && responseSingleResult.getBody().get(0) != null);
		assertTrue(responseSingleResult.getBody().get(0).path(sensorTag.getOntology().getIdentification()).path("id")
				.asText().equals(instance.path(sensorTag.getOntology().getIdentification()).path("id").asText()));

	}

	@Test
	public void When_UpdateByIdIsInvoked_Then_OneInstanceIsUpdated() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		final String id = instance.path(JSON_PATH_OID).asText();
		((ObjectNode) instance.path(sensorTag.getOntology().getIdentification())).put(JSON_PATH_TEMPERATURE, 125);
		((ObjectNode) instance).remove(JSON_PATH_OID);
		final ResponseEntity<JsonNode> responseSingleResult = restTemplate.exchange(
				getURL(sensorTag.getIdentification() + "/" + id), HttpMethod.PUT, new HttpEntity<>(instance, headers()),
				JsonNode.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));
		assertTrue(responseSingleResult.getBody() != null);
		assertTrue(responseSingleResult.getBody().path(sensorTag.getOntology().getIdentification())
				.path(JSON_PATH_TEMPERATURE).asInt() == instance.path(sensorTag.getOntology().getIdentification())
						.path(JSON_PATH_TEMPERATURE).asInt());

	}

	@Test
	public void When_UpdateByIdSQLIsInvoked_Then_OneInstanceIsUpdated() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		final String id = instance.path(JSON_PATH_OID).asText();
		final ResponseEntity<JsonNode> responseSingleResult = restTemplate.exchange(
				getURL(sensorTag.getIdentification()) + String.format(PATH_UPDATE_OID, id), HttpMethod.GET,
				new HttpEntity<>(null, headers()), JsonNode.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));
		assertTrue(responseSingleResult.getBody() != null);
		assertTrue(!responseSingleResult.getBody().get("count").isMissingNode());

	}

	@Test
	public void When_InsertInstanceWithBadSchema_Expect_500() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		((ObjectNode) instance.path(sensorTag.getOntology().getIdentification())).remove(JSON_PATH_TEMPERATURE);
		((ObjectNode) instance).remove(JSON_PATH_OID);
		final ResponseEntity<String> responseSingleResult = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.POST, new HttpEntity<>(instance, headers()), String.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));

	}

	@Test
	public void When_InsertInstance_Expect_200() {

		final ResponseEntity<ArrayNode> response = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);

		final JsonNode instance = response.getBody().get(0);
		((ObjectNode) instance).remove(JSON_PATH_OID);
		final ResponseEntity<String> responseSingleResult = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.POST, new HttpEntity<>(instance, headers()), String.class);

		assertTrue(responseSingleResult.getStatusCode().equals(HttpStatus.OK));

	}

	@Test
	public void When_RequestingGETCustomQueryFilteringByHighTemperature_Expect_MoreThanOneInstanceAndLessThanTotal() {

		final int criticalTempValue = 80;
		final ResponseEntity<ArrayNode> response = restTemplate.exchange(
				getURL(sensorTag.getIdentification()) + String.format(PATH_GET_CRITICAL, criticalTempValue),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(response.getBody().size() >= 1 && response.getBody().size() < nInstances);

	}

	@Test
	public void When_DeleteById_Expect_InstanceDeleted() {
		final ResponseEntity<ArrayNode> reference = restTemplate.exchange(getURL(sensorTag.getIdentification()),
				HttpMethod.GET, new HttpEntity<>(null, headers()), ArrayNode.class);

		final JsonNode instance = reference.getBody().get(0);
		final ResponseEntity<JsonNode> response = restTemplate.exchange(
				getURL(sensorTag.getIdentification()) + "/" + instance.path(JSON_PATH_OID).asText(), HttpMethod.DELETE,
				new HttpEntity<>(null, headers()), JsonNode.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(response.getBody().path(JSON_PATH_COUNT).asInt() == 1);

	}

	@Test
	public void Z_When_RequestingDELETECustomQuery_Then_AllInstancesAreDeleted() {

		final ResponseEntity<JsonNode> response = restTemplate.exchange(
				getURL(sensorTag.getIdentification()) + PATH_DELETE_ALLL, HttpMethod.GET,
				new HttpEntity<>(null, headers()), JsonNode.class);

		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		assertTrue(response.getBody().path(JSON_PATH_COUNT).asInt() == nInstances);

	}

	private HttpHeaders headers() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(X_OP_APIKEY, apiUtils.getUserToken(user));
		return headers;
	}

	private void loadSampleData() {
		try {
			final ArrayNode instances = mapper.readValue(apiUtils.loadFromResources(DATASET_SENSOR_TAG),
					ArrayNode.class);

			assert instances != null;
			nInstances = instances.size();
			instances.forEach(i -> basicOps.insert(sensorTag.getOntology().getIdentification(),
					sensorTag.getOntology().getJsonSchema(), i.toString()));
		} catch (final Exception e) {
			log.error("Could not load sample data for test");
		}
	}

	private void removeSampleData() {
		basicOps.delete(sensorTag.getOntology().getIdentification(), false);
		manageDb.removeTable4Ontology(sensorTag.getOntology().getIdentification());
	}

	private String getURL(String apiIdentification) {
		return apiManager + BASE_URL + apiIdentification;
	}

}

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
package com.minsait.onesait.platform.simulator.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

@RunWith(SpringRunner.class)
public class DeviceSimulatorServiceTest {

	@TestConfiguration
	static class PersistenceServiceImplTestContextConfiguration {

		@Bean
		public IoTBrokerClient persistenceService() {
			return new IoTBrokerClientImpl();
		}
	}

	@Autowired
	IoTBrokerClientImpl persistenceService;
	@MockBean
	IntegrationResourcesService integrationResourceService;
	@MockBean
	RestService restService;
	@MockBean
	TokenService tokenService;
	@MockBean
	ClientPlatformService clientPlatformService;
	private String clientPlatform;
	private String clientPlatformInstance;
	private String ontology;
	private String user;
	private String instance;

	@Before
	public void setUp() throws IOException {
		clientPlatform = "TicketingApp";
		clientPlatformInstance = "Test";
		ontology = "Ticket";
		user = "developer";
		instance = "{\"Ticket\":{\"Identification\":\"\",\"Status\":\"\",\"Email\":\"\",\"Name\":\"\",\"Response_via\":\"\",\"File\":{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}},\"Coordinates\":{\"coordinates\":{\"latitude\":55.5842,\"longitude\":44.0645},\"type\":\"Point\"}},\"contextData\":{\"clientPatform\":\"Ticketing App\",\"clientPatformInstance\":\"Ticketing App:Ticket\",\"clientConnection\":\"\",\"clientSession\":\"01dbb0ec-74b8-4567-b103-759cf164d9a6\",\"user\":\"developer\",\"timezoneId\":\"Europe/Paris\",\"timestamp\":\"Wed Apr 18 15:01:21 CEST 2018\",\"timestampMillis\":{\"$numberLong\":\"1524056481126\"}}}";
		initMocks();
	}

	public void initMocks() throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode nodeInsert = mapper.createObjectNode();
		final JsonNode nodeConnect = mapper.createObjectNode();
		((ObjectNode) nodeConnect).put("sessionKey", "sessionKey");
		final Token token = new Token();
		token.setTokenName("token");

		MockitoAnnotations.initMocks(this);
		when(restService.disconnectRest(any(), any())).thenReturn(null);
		when(restService.connectRest(any(), any(), eq("TicketingApp"), any())).thenReturn(nodeConnect);
		when(restService.connectRest(any(), any(), eq("NoDevice"), any())).thenReturn(mapper.createObjectNode());
		when(restService.insertRest(any(), any(), any(), any())).thenReturn(nodeInsert);
		when(tokenService.getToken(any())).thenReturn(token);
		when(clientPlatformService.getByIdentification(any())).thenReturn(new ClientPlatform());
		when(integrationResourceService.getUrl(any(), any())).thenReturn("http://localhost:8081/");
	}

	@Test
	public void test_Iotbroker_connection() throws Exception {
		persistenceService.connectDeviceRest(clientPlatform, clientPlatformInstance);
		Assert.assertNotNull(persistenceService.getSessionKeys().get(clientPlatform));
	}

	@Test(expected = Exception.class)
	public void test_Itobroker_trysToConnect_withInvalidDevice_thenThrowsException() throws Exception {
		clientPlatform = "NoDevice";
		persistenceService.connectDeviceRest(clientPlatform, clientPlatformInstance);
	}

	@Test
	public void test_IotbrokerDisconnect() throws Exception {
		persistenceService.connectDeviceRest(clientPlatform, clientPlatformInstance);
		Assert.assertNotNull(persistenceService.getSessionKeys().get(clientPlatform));
		persistenceService.disconnectDeviceRest(clientPlatform);
		Assert.assertNull(persistenceService.getSessionKeys().get(clientPlatform));
	}

	@Test
	public void test_IotbrokerInsert() throws Exception {
		clientPlatform = "TicketingApp";
		persistenceService.insertOntologyInstance(instance, ontology, user, clientPlatform, clientPlatformInstance);
		Assert.assertNotNull(persistenceService.getSessionKeys().get(clientPlatform));
	}
}

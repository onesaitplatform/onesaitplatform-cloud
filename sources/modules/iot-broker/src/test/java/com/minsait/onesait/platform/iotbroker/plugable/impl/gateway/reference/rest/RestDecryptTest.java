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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.rest;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.minsait.onesait.platform.config.model.IoTSession;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;

@RunWith(MockitoJUnitRunner.class)
public class RestDecryptTest {

	private MockMvc mockMvc;

	@InjectMocks
	private Rest rest;

	@Mock
	SecurityPluginManager securityPluginManager;

	@Mock
	OntologyDataService ontologyDataService;

	private final String simpleEncryptedData = "{\"data\": \"encrypted\"}";
	private final String simpleClearData = "{\"data\": \"clear\"}";
	private final String simpleOntology = "ontology";

	@Before
	public void setup() {
		// Setup Spring test in standalone mode
		this.mockMvc = MockMvcBuilders.standaloneSetup(rest).build();
	}

	@Test
	public void given_EncryptedEntityFromOneOntology_When_InvalidSessionIsProvided_Then_UnathorizedErrorIsGenerated()
			throws Exception {

		String sessionKey = "sessionkey";
		when(securityPluginManager.getSession(sessionKey)).thenReturn(Optional.empty());

		mockMvc.perform(post("/rest/ontology/decrypt/" + simpleOntology).header("Authorization", sessionKey)
				.content(simpleEncryptedData)).andExpect(status().isUnauthorized());
	}

	@Test
	public void given_EncryptedEntityFromOneOntology_When_ValidSessionIsProvidedButTheServiceFailsDecrypting_Then_BadRequestIsResturned()
			throws Exception {

		String user = "alguien";

		String sessionKey = "sessionkey";
		IoTSession iotSession = new IoTSession();
		iotSession.setUserID(user);
		Optional<IoTSession> session = Optional.of(iotSession);

		when(securityPluginManager.getSession(sessionKey)).thenReturn(session);
		when(ontologyDataService.decrypt(simpleEncryptedData, simpleOntology, user)).thenReturn(null);

		mockMvc.perform(post("/rest/ontology/decrypt/" + simpleOntology).header("Authorization", sessionKey)
				.content(simpleEncryptedData)).andExpect(status().isBadRequest());
	}

	@Test
	public void given_EncryptedEntityFromOneOntology_When_ValidSessionIsProvided_Then_TheClearDataIsReturned()
			throws Exception {
		String user = "alguien";

		String sessionKey = "sessionkey";
		IoTSession iotSession = new IoTSession();
		iotSession.setUserID(user);
		Optional<IoTSession> session = Optional.of(iotSession);

		when(securityPluginManager.getSession(sessionKey)).thenReturn(session);
		when(ontologyDataService.decrypt(simpleEncryptedData, simpleOntology, user)).thenReturn(simpleClearData);

		mockMvc.perform(post("/rest/ontology/decrypt/" + simpleOntology).header("Authorization", sessionKey)
				.content(simpleEncryptedData)).andExpect(status().isOk()).andExpect(jsonPath("$.data", is("clear")));
	}

	@Test
	public void given_EncryptedEntityFromOneOntology_When_UnauthorizedUserTryToDecrypt_Then_UnathorizedErrorIsGenerated()
			throws Exception {
		String user = "alguien";

		String sessionKey = "sessionkey";
		IoTSession iotSession = new IoTSession();
		iotSession.setUserID(user);
		Optional<IoTSession> session = Optional.of(iotSession);

		when(securityPluginManager.getSession(sessionKey)).thenReturn(session);
		when(ontologyDataService.decrypt(simpleEncryptedData, simpleOntology, user))
				.thenThrow(new OntologyDataUnauthorizedException());

		mockMvc.perform(post("/rest/ontology/decrypt/" + simpleOntology).header("Authorization", sessionKey)
				.content(simpleEncryptedData)).andExpect(status().isUnauthorized());
	}

	@Test
	public void given_EncryptedEntityFromOneOntology_When_FailsToDecryptData_Then_OntologyDataJsonProblemExceptionIsGenerated()
			throws Exception {
		String user = "alguien";

		String sessionKey = "sessionkey";
		IoTSession iotSession = new IoTSession();
		iotSession.setUserID(user);
		Optional<IoTSession> session = Optional.of(iotSession);

		when(securityPluginManager.getSession(sessionKey)).thenReturn(session);
		when(ontologyDataService.decrypt(simpleEncryptedData, simpleOntology, user))
				.thenThrow(new OntologyDataJsonProblemException());

		mockMvc.perform(post("/rest/ontology/decrypt/" + simpleOntology).header("Authorization", sessionKey)
				.content(simpleEncryptedData)).andExpect(status().isInternalServerError());
	}

}

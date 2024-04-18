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
package com.minsait.onesait.platform.config.services.client;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

@RunWith(MockitoJUnitRunner.class)
public class ClientPlatformServiceImplTest {

	@Mock
	private UserService userService;

	@Mock
	private ClientPlatformRepository clientPlatformRepository;

	@Mock
	private OntologyRepository ontologyRepository;

	@Mock
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;

	@InjectMocks
	private ClientPlatformServiceImpl clientPlatformServiceImpl;

	@Test
	public void given_OneUserAdminWithClientPlatforms_When_AllClientPlatformsAreRequested_Then_AllTheClientPlatformOfTheUserAreReturned() {
		String userId = "userId";
		String clientIdentification = "clientIdentification";

		Role role = new Role();
		role.setId(Role.Type.ROLE_ADMINISTRATOR.toString());

		User user = new User();
		user.setUserId(userId);
		user.setRole(role);

		ClientPlatform cp1 = new ClientPlatform();
		cp1.setIdentification(clientIdentification);
		List<ClientPlatform> clients = new ArrayList<>();
		clients.add(cp1);

		String id = "1";
		Ontology ontology = new Ontology();
		ontology.setId(id);
		ontology.setIdentification(id);
		String[] ontologies = new String[] { id };

		ClientPlatformOntology cpo = new ClientPlatformOntology();

		when(userService.getUser(userId)).thenReturn(user);
		when(userService.isUserAdministrator(user)).thenReturn(true);
		when(clientPlatformRepository.findByIdentification(clientIdentification)).thenReturn(cp1);
		when(clientPlatformRepository.findAll()).thenReturn(clients);
		when(ontologyRepository.findByIdentification(id)).thenReturn(ontology);
		when(clientPlatformOntologyRepository.findByOntologyAndClientPlatform(id, clientIdentification))
				.thenReturn(cpo);

		List<ClientPlatform> allClientPlatformByCriteria = clientPlatformServiceImpl
				.getAllClientPlatformByCriteria(userId, clientIdentification, ontologies);

		assertTrue("The number of returned clients is not correct", allClientPlatformByCriteria.size() == 1);
	}

}

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
package com.minsait.onesait.platform.iotbroker.plugable.impl.security.reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.javafaker.Faker;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthenticationException;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthorizationException;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
@Ignore
public class ReferenceSecurityTest {
	@Autowired
	SecurityPluginManager security;

	@Autowired
	UserService userService;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	ClientPlatformService clientPlatformService;
	@Autowired
	OntologyService ontologyService;
	@Autowired
	DataModelRepository dataModelRepository;
	@Autowired
	TokenService tokenService;
	@Autowired
	ClientPlatformOntologyRepository clientPlatformOntologyRepository;

	ClientPlatform subjectClientPlatform;
	User subjectUser;
	Ontology subjectOntology;

	Faker faker = new Faker();

	@Before
	public void setup() throws JsonGenerationException, IOException {
		final User user = new User();
		user.setActive(true);
		user.setEmail(faker.internet().emailAddress());
		user.setFullName(faker.name().fullName());
		user.setPassword("changeIt!");
		user.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.name()).orElse(null));
		final String userId = UUID.randomUUID().toString();
		user.setUserId(userId);
		userService.createUser(user);
		subjectUser = userService.getUser(userId);

		final Ontology ontology = new Ontology();
		ontology.setActive(true);
		ontology.setDescription(faker.lorem().fixedString(10));
		final String ontologyIdentification = UUID.randomUUID().toString();
		ontology.setIdentification(ontologyIdentification);
		ontology.setDataModel(dataModelRepository.findAll().get(0));
		ontology.setJsonSchema("{}");
		ontology.setMetainf("meta");
		ontology.setPublic(false);
		ontology.setRtdbClean(false);
		ontology.setRtdbToHdb(false);
		ontology.setUser(subjectUser);
		ontologyService.createOntology(ontology, null);
		subjectOntology = ontologyService.getOntologyByIdentification(ontology.getIdentification(),
				subjectUser.getUserId());

		final ClientPlatform clientPlatform = new ClientPlatform();
		final String clientPlatformIdentification = UUID.randomUUID().toString();
		clientPlatform.setIdentification(clientPlatformIdentification);
		clientPlatform.setUser(subjectUser);
		// clientPlatformService.createClientAndToken(Arrays.asList(subjectOntology),
		// clientPlatform);
		clientPlatformService.createClientAndToken(new ArrayList<>(), clientPlatform);
		subjectClientPlatform = clientPlatformService.getByIdentification(clientPlatformIdentification);

		final ClientPlatformOntology cpo = new ClientPlatformOntology();
		cpo.setAccess(AccessType.ALL);

		cpo.setClientPlatform(subjectClientPlatform);
		cpo.setOntology(subjectOntology);

		clientPlatformOntologyRepository.save(cpo);

		tokenService.generateTokenForClient(subjectClientPlatform);

	}

	@Before
	public void tearDown() {
	}

	@Test
	public void given_OneValidToken_When_ASessionIsCreatedAndCheckedAndFinallyClosed_TheSessionReturnsTheCorrectParametersAndThenItIsClosed()
			throws AuthenticationException, AuthorizationException {
		final Token t = tokenService.getToken(subjectClientPlatform);

		final Optional<IoTSession> session = security.authenticate(t.getTokenName(),
				subjectClientPlatform.getIdentification(), UUID.randomUUID().toString(), "");

		Assert.assertTrue(session.isPresent());
		Assert.assertTrue(!StringUtils.isEmpty(session.get().getSessionKey()));
		Assert.assertTrue(security.checkSessionKeyActive(session));
		Assert.assertTrue(security.closeSession(session.get().getSessionKey()));
		Assert.assertFalse(security.checkSessionKeyActive(session));
	}

	@Test
	public void given_OneInvalidToken_When_ASessionIsCreated_Then_ItReturnsAnInvalidSession()
			throws AuthenticationException {
		final Optional<IoTSession> session = security.authenticate("INVALID_TOKEN",
				subjectClientPlatform.getIdentification(), UUID.randomUUID().toString(), "");

		Assert.assertFalse(session.isPresent());
	}

	@Test
	public void given_OneNotValidSessionKey_When_TheSessionIsChecked_Then_ItRetrunsThatTheSessionIsNotAcctive()
			throws AuthorizationException {
		Assert.assertFalse(security.checkSessionKeyActive(Optional.empty()));
	}

	@Test
	public void given_OneValidClientSessionPlatform_When_ItCreatesASession_Then_ItIsAuthorizedForUsingTheOntologyAssociated()
			throws AuthenticationException, AuthorizationException {
		final Token t = tokenService.getToken(subjectClientPlatform);

		final Optional<IoTSession> session = security.authenticate(t.getTokenName(),
				subjectClientPlatform.getIdentification(), UUID.randomUUID().toString(), "");

		Assert.assertTrue(security.checkAuthorization(SSAPMessageTypes.INSERT, subjectOntology.getIdentification(),
				session));
	}

	@Test
	public void given_OneValidClientSessionPlatform_When_ItCreatesASession_Then_ItIsNotAuthorizedForUsingNotAuthorizedOntologies()
			throws AuthenticationException, AuthorizationException {
		final Token t = tokenService.getToken(subjectClientPlatform);

		final Optional<IoTSession> session = security.authenticate(t.getTokenName(),
				subjectClientPlatform.getIdentification(), UUID.randomUUID().toString(), "");

		Assert.assertFalse(security.checkAuthorization(SSAPMessageTypes.INSERT, "NOT_ASSIGNED_ONTOLOGY",
				session));
	}

}

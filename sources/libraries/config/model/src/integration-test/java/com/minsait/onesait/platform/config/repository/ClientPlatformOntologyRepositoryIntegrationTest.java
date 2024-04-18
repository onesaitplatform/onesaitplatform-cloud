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
package com.minsait.onesait.platform.config.repository;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class ClientPlatformOntologyRepositoryIntegrationTest {

	@Autowired
	ClientPlatformOntologyRepository repository;
	@Autowired
	ClientPlatformRepository cprepository;
	@Autowired
	OntologyRepository orepository;
	@Autowired
	UserRepository userRepository;

	@Before
	public void setUp() {

		final List<ClientPlatformOntology> cpos = repository.findAll();
		if (cpos.isEmpty())
			throw new RuntimeException("There must be at least one ClientPlatform in our ConfigDB");
	}

	@Test
	@Transactional
	public void given_SomeClientPlatformOntologysExist_When_ItIsSearchedByOntologyAndClientPlatform_Then_TheCorrectObjectIsReturned() {
		final ClientPlatformOntology cpo = repository.findAll().get(0);
		Assert.assertTrue(repository.findByOntologyAndClientPlatform(cpo.getOntology().getIdentification(),
				cpo.getClientPlatform().getIdentification()) != null);

	}

	@Test
	@Transactional
	public void given_SomeClientPlatformOntology_When_ItIsUpdated_Then_TheClientOntologiesAreRemoved() {

		final User user = userRepository.findByUserId("developer");
		Ontology ontology1 = new Ontology();
		ontology1.setId("TESTING-ONTOLOGY-1");
		ontology1.setIdentification("ontology-1");
		ontology1.setUser(user);
		ontology1.setJsonSchema("{}");
		ontology1.setDescription("Something");
		ontology1 = orepository.save(ontology1);

		final ClientPlatform cp = new ClientPlatform();
		cp.setIdentification("testing-device");
		cp.setUser(user);
		cp.setClientConnections(new HashSet<ClientConnection>());
		cp.setClientPlatformOntologies(new HashSet<ClientPlatformOntology>());
		cp.setEncryptionKey("key");
		cp.setTokens(new HashSet<Token>());

		final ClientPlatformOntology cpo1 = new ClientPlatformOntology();
		cpo1.setOntology(ontology1);
		cpo1.setAccess(Ontology.AccessType.ALL);
		cpo1.setClientPlatform(cp);
		cp.getClientPlatformOntologies().add(cpo1);

		cprepository.save(cp);

		ClientPlatform cpStored = cprepository.findByIdentification("testing-device");
		Set<ClientPlatformOntology> clientPlatformOntologies = cpStored.getClientPlatformOntologies();
		assertTrue("There should be 1 ontology linked with this client platform", clientPlatformOntologies.size() == 1);

		Ontology ontology2 = new Ontology();
		ontology2.setId("TESTING-ONTOLOGY-2");
		ontology2.setIdentification("ontology-2");
		ontology2.setUser(user);
		ontology2.setJsonSchema("{}");
		ontology2.setDescription("Something");
		ontology2 = orepository.save(ontology2);

		final ClientPlatformOntology cpo2 = new ClientPlatformOntology();
		cpo2.setOntology(ontology2);
		cpo2.setAccess(Ontology.AccessType.ALL);
		cpo2.setClientPlatform(cpStored);
		cpStored.getClientPlatformOntologies().add(cpo2);

		cprepository.save(cpStored);

		cpStored = cprepository.findByIdentification("testing-device");
		clientPlatformOntologies = cpStored.getClientPlatformOntologies();
		assertTrue("There should be 2 ontologies linked with this client platform",
				clientPlatformOntologies.size() == 2);

		final ClientPlatformOntology ocpToRemove = repository
				.findByOntologyAndClientPlatform(ontology1.getIdentification(), cpStored.getIdentification());

		cpStored.getClientPlatformOntologies().remove(ocpToRemove);
		ocpToRemove.setClientPlatform(null);
		cprepository.save(cpStored);

		cpStored = cprepository.findByIdentification("testing-device");
		clientPlatformOntologies = cpStored.getClientPlatformOntologies();
		assertTrue("There should be 1 ontology linked with this client platform", clientPlatformOntologies.size() == 1);

	}

}

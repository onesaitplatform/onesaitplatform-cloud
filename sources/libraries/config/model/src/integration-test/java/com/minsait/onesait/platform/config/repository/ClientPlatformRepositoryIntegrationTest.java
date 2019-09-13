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

import java.util.List;

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
import com.minsait.onesait.platform.config.model.ClientPlatform;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class ClientPlatformRepositoryIntegrationTest {
	@Autowired
	ClientPlatformRepository repository;

	@Autowired
	UserRepository userRep;

	@Before
	public void setUp() {
		List<ClientPlatform> clients = this.repository.findAll();
		if (clients.isEmpty()) {
			log.info("No clients ...");
			ClientPlatform client = new ClientPlatform();
			client.setUser(userRep.findByUserId("collaborator"));
			client.setIdentification("Client-MasterData");
			client.setEncryptionKey("b37bf11c-631e-4bc4-ae44-910e58525952");
			client.setDescription("ClientPatform created as MasterData");
			repository.save(client);
			client = new ClientPlatform();
			client.setUser(userRep.findByUserId("collaborator"));
			client.setIdentification("GTKP-Example");
			client.setEncryptionKey("f9dfe72e-7082-4fe8-ba37-3f569b30a691");
			client.setDescription("ClientPatform created as Example");
			repository.save(client);

		}
	}

	@Test
	@Transactional
	public void given_SomeClientPlatformsExistWithIdentificationAndDescription_When_ItIsSearchedByIdentificationAndNullDescription_Then_TheClientPlatformWithTheIdentificationIsFound() {
		List<ClientPlatform> client = this.repository.findByIdentificationAndDescription("GTKP-fjgcornejo", null);
		Assert.assertTrue(client != null);
	}

}

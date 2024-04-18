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

import java.util.Calendar;
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
import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@Slf4j
public class ClientConnectionRepositoryIntegrationTest {

	@Autowired
	ClientConnectionRepository repository;
	@Autowired
	ClientPlatformRepository clientRep;

	@Before
	public void setUp() {
		List<ClientConnection> clients = this.repository.findAll();
		if (clients.isEmpty()) {
			log.info("No clients ...");
			ClientConnection con = new ClientConnection();
			ClientPlatform client = clientRep.findAll().get(0);
			con.setClientPlatform(client);
			con.setIdentification(client.getIdentification() + "-1");
			con.setIpStrict(true);
			con.setStaticIp(false);
			con.setLastIp("192.168.1.89");
			Calendar date = Calendar.getInstance();
			con.setLastConnection(date);
			repository.save(con);
		}
	}

	@Test
	@Transactional
	public void given_SomeClientConnectionsExist_When_AllAreSearched_Then_AllAreReturned() {
		ClientConnection con = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.countByClientPlatform(con.getClientPlatform()) > 0);
	}

	@Test
	@Transactional
	public void given_SomeClientConnectionsExist_When_ItIsSearchedByTheUserOfTheClientPlatform_Then_TheCorrectObjectsAreReturned() {
		ClientConnection con = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findByUser(con.getClientPlatform().getUser()).size() > 0);
	}

	@Test
	@Transactional
	public void given_SomeClientConnectionsExist_When_ItIsSearchedByClientPlatform_Then_TheCorrectObjectsAreReturned() {
		ClientConnection con = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findByClientPlatform(con.getClientPlatform()).size() > 0);
	}

	@Test
	@Transactional
	public void given_SomeClientConnectionsExist_When_ItIsSearchedByClientPlatformAndIdentification_Then_TheCorrectObjectsAreReturned() {
		ClientConnection con = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository
				.findByClientPlatformAndIdentification(con.getClientPlatform(), con.getIdentification()).size() > 0);
	}

}

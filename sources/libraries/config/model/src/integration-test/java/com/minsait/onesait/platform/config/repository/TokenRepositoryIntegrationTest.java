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
package com.minsait.onesait.platform.config.repository;

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
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Token;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class TokenRepositoryIntegrationTest {

	@Autowired
	TokenRepository repository;
	@Autowired
	ClientPlatformRepository cpRepository;

	@Before
	public void setUp() {
		List<Token> tokens = this.repository.findAll();
		if (tokens.isEmpty()) {
			log.info("No Tokens, adding ...");

			ClientPlatform client = new ClientPlatform();
			client.setId("06be1962-aa27-429c-960c-d8a324eef6d4");
			Set<Token> hashSetTokens = new HashSet<Token>();

			Token token = new Token();
			token.setClientPlatform(client);
			token.setTokenName("Token1");
			token.setActive(true);
			hashSetTokens.add(token);
			client.setTokens(hashSetTokens);
			repository.save(token);
		}
	}

	@Test
	@Transactional
	public void given_SomeTokensExist_When_ItIsSearchedByPlatformId_Then_TheCorrectObjectsAreObtained() {
		Token token = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findByClientPlatform(token.getClientPlatform()).size() > 0);
	}

}

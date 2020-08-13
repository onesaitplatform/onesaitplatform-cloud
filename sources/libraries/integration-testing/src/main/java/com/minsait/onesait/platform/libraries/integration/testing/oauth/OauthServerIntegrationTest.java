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
package com.minsait.onesait.platform.libraries.integration.testing.oauth;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.libraries.integration.testing.IntegrationTestingApp;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = IntegrationTestingApp.class)
@RunWith(SpringRunner.class)
@Slf4j
public class OauthServerIntegrationTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String OAUTH_TOKEN_ENDPOINT = "/oauth/token";
	private static final String GRANT_TYPE = "grant_type";
	private static final String SCOPE = "openid";
	private static final String SCOPE_DEFAULT = "openid";
	private static final String GRANT_TYPE_PASSWORD = "password";
	@Value("${username:developer}")
	private String username;
	@Value("${password:Changed2019!}")
	private String pass_word;
	private static final String USER = "username";
	@Value("${client-id}")
	private String clientId;
	@Value("${client-secret}")
	private String clientSecret;

	@Value("${oauth-server}")
	private String oauthServer;

	@Test(alwaysRun = true, invocationCount = 250, threadPoolSize = 10, timeOut = 100000)
	public void whenRightCredentialsAreProvided_thenOauthTokenIsFetched() {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(GRANT_TYPE, GRANT_TYPE_PASSWORD);
		params.add(SCOPE, SCOPE_DEFAULT);
		params.add(GRANT_TYPE_PASSWORD, pass_word);
		params.add(USER, username);
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic "
				+ Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));
		final ResponseEntity<JsonNode> response = restTemplate.exchange(oauthServer + OAUTH_TOKEN_ENDPOINT,
				HttpMethod.POST, new HttpEntity<>(params, headers), JsonNode.class);
		log.info("Token: {}", response.getBody().get("access_token").asText());
		assertTrue(response.getStatusCode() == HttpStatus.OK);
	}

}

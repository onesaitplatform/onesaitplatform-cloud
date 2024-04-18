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
package com.minsait.onesait.platform.libraries.integration.testing.oauth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.libraries.integration.testing.IntegrationTestingApp;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = IntegrationTestingApp.class)
@Slf4j
public class OauthServerIntegrationTest extends AbstractTestNGSpringContextTests {

	private static final String VERTICAL2 = "vertical";

	private static final String BASIC = "Basic ";

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String OAUTH_TOKEN_ENDPOINT = "/oauth/token";
	private static final String OAUTH_CHECK_TOKEN_ENDPOINT = "/oauth/check_token";
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
	@Value("${vertical:onesaitplatform}")
	private String vertical;

	@Value("${oauth-server}")
	private String oauthServer;

	@Test(alwaysRun = true, invocationCount = 20, threadPoolSize = 10, timeOut = 100000)
	public void whenRightCredentialsAreProvided_thenOauthTokenIsFetched() {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(GRANT_TYPE, GRANT_TYPE_PASSWORD);
		params.add(SCOPE, SCOPE_DEFAULT);
		params.add(GRANT_TYPE_PASSWORD, pass_word);
		params.add(USER, username);
		params.add(VERTICAL2, vertical);
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, BASIC
				+ Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));
		final ResponseEntity<JsonNode> response = restTemplate.exchange(oauthServer + OAUTH_TOKEN_ENDPOINT,
				HttpMethod.POST, new HttpEntity<>(params, headers), JsonNode.class);
		log.info("Token: {}", response.getBody().get("access_token").asText());
		assertTrue(response.getStatusCode() == HttpStatus.OK);
	}

	@Test
	public void checkTocken() {
		final HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization",
				BASIC + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));

		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oauthServer + OAUTH_CHECK_TOKEN_ENDPOINT)
				.queryParam("token", getOauthToken()).queryParam(VERTICAL2, vertical);

		final HttpEntity<?> entity = new HttpEntity<>(headers);

		final ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity,
				String.class);
		assertTrue(response.getStatusCode() == HttpStatus.OK);
	}

	private String getOauthToken() {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(GRANT_TYPE, GRANT_TYPE_PASSWORD);
		params.add(SCOPE, SCOPE_DEFAULT);
		params.add(GRANT_TYPE_PASSWORD, pass_word);
		params.add(USER, username);
		params.add(VERTICAL2, vertical);
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, BASIC
				+ Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));
		final ResponseEntity<JsonNode> response = restTemplate.exchange(oauthServer + OAUTH_TOKEN_ENDPOINT,
				HttpMethod.POST, new HttpEntity<>(params, headers), JsonNode.class);
		return response.getBody().get("access_token").asText();
	}

}

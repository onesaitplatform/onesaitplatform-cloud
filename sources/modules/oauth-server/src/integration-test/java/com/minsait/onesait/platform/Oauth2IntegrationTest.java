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
package com.minsait.onesait.platform;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.entity.cast.EntitiesCast;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Oauth2AuthorizationServerApplication.class)
@Category(IntegrationTest.class)
@TestPropertySource(locations = "/application-integration-test.yml")
public class Oauth2IntegrationTest {

	// TO-DO replace with env var
	@Value("${oauthserver:http://localhost:21000/oauth-server}")
	private String oauthServer;
	private static final String IMPLICIT_ENDPOINT = "/oauth/token";
	private static final String PRINCIPAL_ENDPOINT = "/user";
	private static final String OIDC_ENDPOINT = "/oidc/userinfo";
	private static final String TOKEN_INFO_ENDPOINT = "/openplatform-oauth/tokenInfo";
	private static final String REVOKE_TOKEN_ENDPOINT = "/openplatform-oauth/revoke_token";

	private static final String USERNAME = "user@test";
	private static final String SECRET = "p@ssw0rd";
	private static final String FULL_NAME = "Test user";
	private static final String EMAIL = "test@domain.com";
	private static final String ROLE = "ROLE_ADMINISTRATOR";

	private static final String REALM_ID = "TestRealm";
	private static final String REALM_ROLE = "ROLE_TEST";

	private static final String GRANT_TYPE_IMPLICIT = "password";
	private static final String SCOPE = "openid";
	private static final String AUTHORITIES = "authorities";

	private static final String DEFAULT_CLIENT_ID = "onesaitplatform";

	private static final String PASSWORD_INCORRECT = "Password incorrect";
	private static final String ERROR_DESC = "error_description";

	private static final String ACCESS_TOKEN = "access_token";
	private static final String TOKEN = "token";
	private static final String REFRESH_TOKEN = "refresh_token";

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AppRepository appRepository;

	private final TestRestTemplate restTemplate = new TestRestTemplate();

	@Before
	public void setUp() {

		restTemplate.getRestTemplate().getMessageConverters().add(new FormHttpMessageConverter());
		createTestUser();
		createTestRealm();
	}

	@After
	public void tearDown() {
		final App realm = EntitiesCast.castAppList(appRepository.findByIdentificationLike(REALM_ID).get(0), false);
		realm.getAppRoles().clear();
		appRepository.delete(realm);
		userRepository.deleteByUserId(USERNAME);
	}

	@Test
	public void When_ImplicitFlow_And_RightRealmCredentials_Expect_ValidToken() {

		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		assertNotNull(responseToken.getBody());
		assertTrue(responseToken.getStatusCode().equals(HttpStatus.OK));
		assertTrue(!responseToken.getBody().path(ACCESS_TOKEN).isMissingNode());
	}

	@Test
	public void When_ImplicitFlow_And_RightRealmCredentials_Expect_RigthRole() {

		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		assertNotNull(responseToken.getBody());
		assertTrue(responseToken.getStatusCode().equals(HttpStatus.OK));
		assertTrue(!responseToken.getBody().path(AUTHORITIES).isMissingNode());
		assertTrue(!responseToken.getBody().path(AUTHORITIES).path(0).isMissingNode());
		assertTrue(responseToken.getBody().path(AUTHORITIES).path(0).asText().equals(REALM_ROLE));
	}

	@Test
	public void When_ImplicitFlow_And_RightOnesaitCredentials_Expect_RigthRole() {

		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(
						Oauth2RequestPojo.builder().clientId(DEFAULT_CLIENT_ID).grantType(GRANT_TYPE_IMPLICIT)
								.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getBasicOPHeaders()),
				JsonNode.class);
		assertNotNull(responseToken.getBody());
		assertTrue(responseToken.getStatusCode().equals(HttpStatus.OK));
		assertTrue(!responseToken.getBody().path(AUTHORITIES).isMissingNode());
		assertTrue(!responseToken.getBody().path(AUTHORITIES).path(0).isMissingNode());
		assertTrue(responseToken.getBody().path(AUTHORITIES).path(0).asText().equals(ROLE));
	}

	@Test
	public void When_ImplicitFlow_And_NoBasicAuthHeader_Expect_401() {

		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(), null),
				JsonNode.class);
		assertNotNull(responseToken.getBody());
		assertTrue(responseToken.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
	}

	@Test
	public void When_ImplicitFlow_And_WrongPassword_Expect_401() {

		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password("").scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		assertNotNull(responseToken.getBody());
		assertTrue(responseToken.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
		assertTrue(responseToken.getBody().path(ERROR_DESC).asText().contains(PASSWORD_INCORRECT));
	}

	@Test
	public void When_RightAccessToken_Then_Principal_And_OIDCDataAreRetrieved() {
		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		final String accessToken = responseToken.getBody().path(ACCESS_TOKEN).asText();
		assertTrue(!accessToken.isEmpty());
		final ResponseEntity<Object> principal = restTemplate.exchange(oauthServer + PRINCIPAL_ENDPOINT, HttpMethod.GET,
				new HttpEntity<>(getBearerHeader(accessToken)), Object.class);
		assertNotNull(principal.getBody());
		assertTrue(principal.getStatusCode().equals(HttpStatus.OK));

		final JsonNode oidcInfo = restTemplate.exchange(oauthServer + OIDC_ENDPOINT, HttpMethod.GET,
				new HttpEntity<>(getBearerHeader(accessToken)), JsonNode.class).getBody();
		assertNotNull(oidcInfo);
		assertTrue(oidcInfo.path("mail").asText().equals(EMAIL));

	}

	@Test
	public void When_RightAccessToken_Then_TokenInfoIsRequested() {
		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		final String accessToken = responseToken.getBody().path(ACCESS_TOKEN).asText();
		assertTrue(!accessToken.isEmpty());
		final JsonNode tokenInfo = restTemplate.exchange(oauthServer + TOKEN_INFO_ENDPOINT, HttpMethod.POST,
				new HttpEntity<>(accessToken, getBearerHeader(accessToken)), JsonNode.class).getBody();
		assertNotNull(tokenInfo);
		assertTrue(tokenInfo.path(TOKEN).asText().equals(accessToken));

	}

	@Test
	public void When_RightAccessToken_And_TokenIsRefreshed() {
		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		final String accessToken = responseToken.getBody().path(ACCESS_TOKEN).asText();
		final String refreshToken = responseToken.getBody().path(REFRESH_TOKEN).asText();
		assertTrue(!refreshToken.isEmpty());
		final JsonNode newToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT, HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(REFRESH_TOKEN)
						.refreshToken(refreshToken).build().getMultiValueMap(), getRealmBasicHeaders()),
				JsonNode.class).getBody();
		assertNotNull(newToken);
		assertTrue(!newToken.path(ACCESS_TOKEN).asText().equals(accessToken));

	}

	@Test
	public void When_AccessTokenIsRevoked_And_TokenInfoIsRetrievedWithRevokedToken_Expect_401() {
		final ResponseEntity<JsonNode> responseToken = restTemplate.exchange(oauthServer + IMPLICIT_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(Oauth2RequestPojo.builder().clientId(REALM_ID).grantType(GRANT_TYPE_IMPLICIT)
						.username(USERNAME).password(SECRET).scope(SCOPE).build().getMultiValueMap(),
						getRealmBasicHeaders()),
				JsonNode.class);
		final String accessToken = responseToken.getBody().path(ACCESS_TOKEN).asText();
		assertTrue(!accessToken.isEmpty());

		ResponseEntity<JsonNode> tokenInfo = restTemplate.exchange(oauthServer + TOKEN_INFO_ENDPOINT, HttpMethod.POST,
				new HttpEntity<>(accessToken, getBearerHeader(accessToken)), JsonNode.class);
		assertTrue(tokenInfo.getStatusCode().equals(HttpStatus.OK));

		final MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add(TOKEN, accessToken);
		restTemplate.exchange(oauthServer + REVOKE_TOKEN_ENDPOINT, HttpMethod.POST,
				new HttpEntity<>(request, getRealmBasicHeaders()), JsonNode.class);
		tokenInfo = restTemplate.exchange(oauthServer + TOKEN_INFO_ENDPOINT, HttpMethod.POST,
				new HttpEntity<>(accessToken, getBearerHeader(accessToken)), JsonNode.class);
		assertTrue(tokenInfo.getStatusCode().equals(HttpStatus.UNAUTHORIZED));

	}

	private void createTestUser() {
		final User test = new User();
		test.setUserId(USERNAME);
		test.setPassword(SECRET);
		test.setFullName(FULL_NAME);
		test.setActive(true);
		test.setEmail(EMAIL);
		test.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.name()).orElse(null));
		userRepository.save(test);

	}

	private void createTestRealm() {
		final App realm = new App();
		realm.setIdentification(REALM_ID);
		realm.setSecret(SECRET);
		final AppRole role = new AppRole();
		role.setApp(realm);
		role.setName(REALM_ROLE);
		role.getAppUsers().add(AppUser.builder().user(userRepository.findByUserId(USERNAME)).role(role).build());
		realm.getAppRoles().add(role);
		appRepository.save(realm);
	}

	private HttpHeaders getRealmBasicHeaders() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((REALM_ID + ":" + SECRET).getBytes()));
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		return headers;
	}

	private HttpHeaders getBasicOPHeaders() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic "
				+ Base64.getEncoder().encodeToString((DEFAULT_CLIENT_ID + ":" + DEFAULT_CLIENT_ID).getBytes()));
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		return headers;
	}

	private HttpHeaders getBearerHeader(String accessToken) {
		final HttpHeaders bearer = new HttpHeaders();
		bearer.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		bearer.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		return bearer;

	}

}

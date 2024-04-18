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
package com.minsait.onesait.platform.libraries.integration.testing.apimanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * This class is meant ton be used for integration purposes only
 *
 */
@Slf4j
@TestComponent
public class APIUtils {

	public static final String API_IDENTIFICATION_EXTERNAL = "pet-store";
	public static final String API_JSON = "APISensorTag.json";
	public static final String API_PET_STORE_JSON = "APIPetStore.json";
	public static final String SENSOR_TAG = "SensorTag";

	public static final String SENSOR_TAG_JSON = "SensorTag.json";
	public static final String API_SENSOR_TAG_JSON = "APISensorTag.json";
	public static final String API_SENSOR_TAG = "sensor-tag";

	public static final String EMPTY_BASE = "EmptyBase";
	public static final String OAUTH_TOKEN_ENDPOINT = "/oauth/token";
	public static final String GRANT_TYPE = "grant_type";
	public static final String SCOPE = "openid";
	public static final String SCOPE_DEFAULT = "openid";
	public static final String GRANT_TYPE_PASSWORD = "password";
	public static final String USER_DEV = "developer";
	public static final String USER_PASS = "Changed2019!";
	public static final String USER = "username";

	@Value("${client-id}")
	private String clientId;
	@Value("${client-secret}")
	private String clientSecret;

	@Autowired
	private TestRestTemplate template;
	private final ObjectMapper mapper = new ObjectMapper();
	@Getter
	@Setter
	private String token;

	@Value("${apimanager}")
	private String apiManager;

	@Value("${oauth-server}")
	private String oauthServer;

	@Value("${controlpanel}")
	private String controlpanel;

	@Value("${vertical:onesaitplatform}")
	private String vertical;

	@Value("${username:developer}")
	private String username;
	@Value("${password:Changed2019!}")
	private String pass_word;

	private static final String API_APIS = "/api/apis";
	private static final String API_ONTOLOGIES = "/api/ontologies";

	@PostConstruct
	public void setUp() {
		fetchOauthToken(username, pass_word);
	}

	public void createInternalAPI() throws IOException {
		final JsonNode ontology = mapper.readValue(loadFromResources(SENSOR_TAG_JSON), JsonNode.class);
		create(controlpanel + API_ONTOLOGIES, ontology);
		final JsonNode api = mapper.readValue(loadFromResources(API_SENSOR_TAG_JSON), JsonNode.class);
		create(controlpanel + API_APIS, api);
	}

	public void createExternalAPI() throws IOException {
		final JsonNode api = mapper.readValue(loadFromResources(API_PET_STORE_JSON), JsonNode.class);
		create(controlpanel + API_APIS, api);

	}

	private void create(String path, JsonNode api) {
		template.exchange(path, HttpMethod.POST, new HttpEntity<>(api, headers()), String.class);
	}

	private void delete(String path) {
		template.exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers()), String.class);
	}

	public void deleteResources() {
		delete(controlpanel + API_APIS + "/" + API_SENSOR_TAG + "?version=1");
		delete(controlpanel + API_APIS + "/" + API_IDENTIFICATION_EXTERNAL + "?version=1");
		delete(controlpanel + API_ONTOLOGIES + "/" + SENSOR_TAG);
	}

	public void fetchOauthToken(String user, String pass) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(GRANT_TYPE, GRANT_TYPE_PASSWORD);
		params.add(SCOPE, SCOPE_DEFAULT);
		params.add(GRANT_TYPE_PASSWORD, pass);
		params.add(USER, user);
		params.add("vertical", vertical);
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic "
				+ Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));
		token = "Bearer "
				+ template
						.exchange(oauthServer + OAUTH_TOKEN_ENDPOINT, HttpMethod.POST,
								new HttpEntity<>(params, headers), JsonNode.class)
						.getBody().get("access_token").asText();
	}

	public String loadFromResources(String name) {
		try {
			return new String(
					Files.readAllBytes(
							Paths.get(getClass().getClassLoader().getResource("apimanager/" + name).toURI())),
					StandardCharsets.UTF_8);

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						StandardCharsets.UTF_8);
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}

	public HttpHeaders headers() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		headers.add(HttpHeaders.AUTHORIZATION, getToken());
		return headers;
	}
}

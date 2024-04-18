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
package com.minsait.onesait.platform.test.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * This class is meant ton be used for integration purposes only
 *
 */
@Slf4j
@TestComponent
public class APIUtils {

	private static final String API_IDENTIFICATION_EXTERNAL = "pet-store";
	private static final String API_JSON = "APISensorTag.json";
	private static final String SWAGGER_JSON = "PetStoreSwagger.yml";
	private static final String SENSOR_TAG = "SensorTag";
	private static final String SENSOR_TAG_SCHEMA_FILE = "SensorTagSchema";
	private static final String EMPTY_BASE = "EmptyBase";

	@Autowired
	private DataModelRepository dataModelRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private UserTokenRepository tokenRepository;
	@Autowired
	private ObjectMapper mapper;

	public ApiDTO readInternalAPIDTO(User user) throws IOException {
		final ApiDTO api = mapper.readValue(loadFromResources(API_JSON), ApiDTO.class);
		api.setOntologyId(createTestOntology(user).getId());
		return api;
	}

	public Api createExternalAPI(User user, ApiType type) {
		final Api apiTest = new Api();
		apiTest.setIdentification(API_IDENTIFICATION_EXTERNAL);
		apiTest.setUser(user);
		apiTest.setApiType(type);
		apiTest.setCategory(ApiCategories.ALL);
		apiTest.setDescription("Test api");
		apiTest.setNumversion(1);
		apiTest.setState(ApiStates.CREATED);

		apiTest.setSwaggerJson(loadFromResources(SWAGGER_JSON));

		return apiRepository.save(apiTest);
	}

	@Transactional
	public void deleteAPITest(Api api) {
		apiRepository.delete(api);
		if (api.getOntology() != null)
			ontologyRepository.delete(api.getOntology());
	}

	@Transactional
	private Ontology createTestOntology(User user) {
		final Ontology sensorTag = new Ontology();
		sensorTag.setIdentification(SENSOR_TAG);
		sensorTag.setUser(user);
		sensorTag.setDataModel(dataModelRepository.findByIdentification(EMPTY_BASE).get(0));
		sensorTag.setJsonSchema(loadFromResources(SENSOR_TAG_SCHEMA_FILE));
		sensorTag.setActive(true);
		sensorTag.setMetainf("");
		sensorTag.setDescription("Sensor tag");
		sensorTag.setPublic(false);
		return ontologyRepository.save(sensorTag);

	}

	public String getUserToken(User user) {
		final List<UserToken> tokens = tokenRepository.findByUser(user);
		if (CollectionUtils.isEmpty(tokens)) {
			final UserToken token = new UserToken();
			token.setUser(user);
			token.setToken(UUID.randomUUID().toString());
			return tokenRepository.save(token).getToken();

		} else {
			return tokens.get(0).getToken();
		}
	}

	public String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
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
}

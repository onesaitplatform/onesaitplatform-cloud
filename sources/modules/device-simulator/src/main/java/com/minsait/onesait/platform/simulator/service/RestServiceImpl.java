/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.simulator.service;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Token;

@Service
public class RestServiceImpl implements RestService {

	@Override
	public JsonNode connectRest(String iotbrokerUrl, Token token, String clientPlatform,
			String clientPlatformInstance) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(iotbrokerUrl + "/rest/client/join")
				.queryParam("token", token.getTokenName()).queryParam("clientPlatform", clientPlatform)
				.queryParam("clientPlatformId", clientPlatformInstance);
		return restTemplate.getForObject(builder.build().encode().toUri(), JsonNode.class);

	}

	@Override
	public ResponseEntity<String> disconnectRest(String iotbrokerUrl, String sessionKey) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", sessionKey);
		headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		return restTemplate.exchange(iotbrokerUrl + "/rest/client/leave", HttpMethod.GET, new HttpEntity<>(headers),
				String.class);
	}

	@Override
	public JsonNode insertRest(String iotbrokerUrl, String instance, String ontology, String sessionKey)
			throws IOException {
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", sessionKey);
		headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode ontologyData = mapper.readTree(instance);

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		final HttpEntity<JsonNode> request = new HttpEntity<>(ontologyData, headers);

		return restTemplate.postForObject(iotbrokerUrl + "/rest/ontology/" + ontology, request, JsonNode.class);
	}

}

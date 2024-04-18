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
package com.minsait.onesait.platform.rulesengine.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class QueryWrapper {

	@Getter
	@Setter
	private List<OntologyJsonWrapper> queryResult;

	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private String ontology;

	@Getter
	@Setter
	private String token;

	@Getter
	@Setter
	private String clientPlatform;

	@Getter
	@Setter
	private String iotbrokerUrl;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	private ObjectMapper mapper = new ObjectMapper();

	 

	public QueryWrapper(String ontology, String query, String clientPlatform, String token, String iotbrokerUrl) {
		if (log.isDebugEnabled()) {
			log.debug("New QueryWrapper object created for ontology {} with query: {}", ontology, query);
		}		
		this.ontology = ontology;
		this.query = query;
		this.clientPlatform = clientPlatform;
		this.token = token;
		this.iotbrokerUrl = iotbrokerUrl;
	}

	public void run() {
		JsonNode joinResult = connect();
		if (joinResult.has("sessionKey")) {
			queryResult = executeQuery(joinResult.get("sessionKey").asText());
		}
	}

	private JsonNode connect() {
		final UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(iotbrokerUrl + "/iot-broker/rest/client/join").queryParam("token", token)
				.queryParam("clientPlatform", clientPlatform).queryParam("clientPlatformId", clientPlatform);
		return restTemplate.getForObject(builder.build().encode().toUri(), JsonNode.class);

	}

	private List<OntologyJsonWrapper> executeQuery(String sessionKey) {
		List<OntologyJsonWrapper> listResult = new ArrayList<>();
		try {
			final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Authorization", sessionKey);
			headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

			final UriComponentsBuilder builder = UriComponentsBuilder
					.fromHttpUrl(iotbrokerUrl + "/iot-broker/rest/ontology/" + ontology + "/query")
					.queryParam("query", query).queryParam("queryType", SSAPQueryType.SQL);

			ResponseEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST,
					new HttpEntity<>(headers), String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {

				JsonNode arrNode = mapper.readTree(response.getBody());
				if (arrNode.isArray()) {
					for (final JsonNode objNode : arrNode) {
						ObjectNode node = ((ObjectNode) objNode);
						node.remove("_id");
						node.remove("contextData");
						listResult.add(new OntologyJsonWrapper(mapper.writeValueAsString(node)));
					}
				} else {
					ObjectNode node = ((ObjectNode) arrNode);
					node.remove("_id");
					node.remove("contextData");
					listResult.add(new OntologyJsonWrapper(mapper.writeValueAsString(node)));
				}
			}
			return listResult;
		} catch (IOException e) {
			log.error("Error parsing JSON query result.", e);
			return listResult;
		}
	}

}

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
package com.minsait.onesait.platform.controlpanel.gravitee.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class TransformHeadersPolicy {

	public static final String TRANSFORM_HEADERS = "transform-headers";
	public static final String TRANSFORM_HEADERS_DESCRIPTION = "Description of the Transform Headers Gravitee Policy";
	private String scope;
	private String[] removeHeaders;
	private HttpHeader[] addHeaders;
	public static final String TRANSFORM_HEADERS_V3 = "{\"name\":\"Transform Headers\",\"description\":\"Description of the Transform Headers Gravitee Policy\",\"enabled\":true,\"policy\":\"transform-headers\",\"configuration\":{\"addHeaders\":[{\"name\":\"X-Requested-With\",\"value\":\"Gravitee-Server\"}],\"scope\":\"REQUEST\",\"removeHeaders\":[]}}";

	public JsonNode toJsonNode() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}

	public static JsonNode getTHV3() {
		try {
			return new ObjectMapper().readValue(TRANSFORM_HEADERS_V3, JsonNode.class);
		} catch (final JsonProcessingException e) {
			log.error("Transform headers v3 error", e);
			return new ObjectMapper().createObjectNode();
		}
	}
}

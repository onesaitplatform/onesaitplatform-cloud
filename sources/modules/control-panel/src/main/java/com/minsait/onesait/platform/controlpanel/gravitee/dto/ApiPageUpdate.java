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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiPageUpdate {

	private String content;
	private int order;
	private boolean homepage;
	private boolean published;
	private String name;
	private JsonNode configuration;

	private static final String DEFAULT_NAME = "Swagger Docs";
	private static final String TRY_IT = "tryIt";

	public static ApiPageUpdate defaultSwaggerDocPage(String swaggerJson) {
		return ApiPageUpdate.builder().content(swaggerJson).order(1).homepage(false).published(true).name(DEFAULT_NAME)
				.configuration(ApiPageUpdate.tryItFlag()).build();
	}

	static JsonNode tryItFlag() {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode tryIt = mapper.createObjectNode();
		((ObjectNode) tryIt).put(TRY_IT, String.valueOf(true));
		return tryIt;
	}
}

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
package com.minsait.onesait.platform.controlpanel.gravitee.dto;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class CorsApi {
	private boolean enabled;
	private boolean allowCredentials;
	private String[] allowOrigin;
	private String[] allowHeaders;
	private String[] allowMethods;
	private String[] exposeHeaders;
	private int maxAge;

	private static final String X_OP_APIKEY = "X-OP-APIKey";

	public static CorsApi defaultCorsPolicy(boolean enabled) {
		return CorsApi.builder().enabled(enabled).allowCredentials(true)
				.allowHeaders(new String[] { X_OP_APIKEY, HttpHeaders.AUTHORIZATION, HttpHeaders.ACCEPT,
						HttpHeaders.CONTENT_TYPE })
				.allowMethods(new String[] { HttpMethod.DELETE.name(), HttpMethod.GET.name(), HttpMethod.POST.name(),
						HttpMethod.PUT.name() })
				.allowOrigin(new String[] { "*" }).maxAge(-1).build();
	}

	public JsonNode toJsonNode() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}
}

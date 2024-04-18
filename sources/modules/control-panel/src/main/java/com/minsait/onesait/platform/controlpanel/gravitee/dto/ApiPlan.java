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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.FlowPath.PathOperator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiPlan {
	private static String DEFAULT_NAME = "Keyless plan";
	private static String DEFAULT_OAUTH_NAME = "Oauth2 jwt plan";
	private static String DEFAULT_DESCRIPTION = "Keyless plan no subscription";
	private static String DEFAULT_OAUTH_DESCRIPTION = "Oauth2 jwt plan";
	private static String DEFAULT_PATHS = "{\"/\":[]}";

	public enum Security {
		KEY_LESS, key_less, OAUTH2, oauth2, JWT, jwt, API_KEY, api_key;
	}

	public enum Validation {
		AUTO, auto;
	}

	public enum Type {
		API, api;
	}

	public enum Status {
		STAGING, staging, PUBLISHED, published;
	}

	String id;
	String name;
	String description;
	Validation validation;
	Security security;
	String securityDefinition;
	Type type;
	Status status;
	List<FlowPath> flows;
	String api;
	String[] characteristics;
	JsonNode paths;
	@JsonProperty("excluded_groups")
	String[] excludedGroups;
	@JsonProperty("comment_required")
	boolean commentRequired;

	public static ApiPlan defaultPlan(String apiId) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return ApiPlan.builder().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION).api(apiId)
				.validation(Validation.AUTO).type(Type.API).paths(mapper.readTree(DEFAULT_PATHS))
				.security(Security.KEY_LESS).status(Status.PUBLISHED).build();

	}

	public static ApiPlan oauthPlan() {
		final FlowPath fp = new FlowPath();
		final PathOperator po = new PathOperator("/", "STARTS_WITH");
		fp.setEnabled(true);
		fp.setPre(new ArrayList<>());
		fp.setPost(new ArrayList<>());
		fp.setPathOperator(po);
		return ApiPlan.builder().name(DEFAULT_OAUTH_NAME).description(DEFAULT_OAUTH_DESCRIPTION)
				.validation(Validation.AUTO).type(Type.API).flows(List.of(fp))
				.securityDefinition(
						"{\"extractPayload\":true,\"checkRequiredScopes\":false,\"modeStrict\":false,\"propagateAuthHeader\":true,\"oauthResource\":\""
								+ ApiOauthResource.AUTH_SERVER_PRIMARY + "\"}")
				.security(Security.OAUTH2).status(Status.PUBLISHED).build();
	}

}

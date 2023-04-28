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

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ApiCreate {
	String name;
	String version;
	String contextPath;
	String[] paths;
	String endpoint;
	String description;

	private static final String HTTP = "http";
	private static final String HTTPS = "https";

	public ApiCreate(Api api) {
		version = api.getNumversion() + ".0";
		paths = new String[] { "/" };
		contextPath = ApiCreate.getApiContextPath(api);
		description = api.getDescription();
		name = api.getIdentification();
	}

	public static ApiCreate createFromOPApi(Api api, String endpoint) {
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON))
			return ApiCreate.fromSwagger(api);
		else
			return ApiCreate.builder().version(api.getNumversion() + ".0").paths(new String[] { "/" })
					.contextPath(ApiCreate.getApiContextPath(api)).description(api.getDescription())
					.name(api.getIdentification()).endpoint(endpoint).build();
	}

	private static ApiCreate fromSwagger(Api api) {
		final SwaggerParser swaggerParser = new SwaggerParser();
		final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
		String endpoint = "";
		// By default HTTP
		String scheme = HTTP;

		if (!CollectionUtils.isEmpty(swagger.getSchemes()))
			scheme = swagger.getSchemes().stream().map(s -> s.toString()).filter(s -> s.equalsIgnoreCase(HTTPS))
					.findFirst().orElse(HTTP);
		endpoint = scheme.concat(swagger.getHost()).concat(swagger.getBasePath());

		final String description = StringUtils.isEmpty(swagger.getInfo().getDescription()) ? api.getDescription()
				: swagger.getInfo().getDescription();
		return ApiCreate.builder().version(swagger.getInfo().getVersion())
				.paths(swagger.getPaths().keySet().toArray(new String[swagger.getPaths().size()]))
				.description(description).name(api.getIdentification()).contextPath(ApiCreate.getApiContextPath(api))
				.endpoint(endpoint).build();
	}

	public static String getApiContextPath(Api api) {
		return "/".concat(api.getIdentification()).concat("/v").concat(String.valueOf(api.getNumversion()));
	}

}

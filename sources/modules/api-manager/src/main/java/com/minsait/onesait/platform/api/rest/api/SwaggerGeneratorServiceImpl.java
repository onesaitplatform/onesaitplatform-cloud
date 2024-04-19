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
package com.minsait.onesait.platform.api.rest.api;

import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.rest.swagger.RestSwaggerReader;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

@Component("swaggerGeneratorServiceImpl")
public class SwaggerGeneratorServiceImpl implements SwaggerGeneratorService {

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiFIQL apiFIQL;

	private static final String BASE_PATH = "/api-manager/server/api";
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private MultitenancyService masterUserService;

	@Value("${server.port:19090}")
	private String port;

	@Override
	public Response getApi(String identificacion, String token) throws GenericOPException {

		final ApiDTO apiDto = apiFIQL.toApiDTO(apiService.findApi(identificacion, token));

		final int version = apiDto.getVersion();
		final String vVersion = "v" + version;

		final BeanConfig config = new BeanConfig();
		config.setHost("localhost:8080");
		config.setSchemes(new String[] { "http" });
		config.setBasePath("/api" + "/" + vVersion + "/" + identificacion);

		final RestSwaggerReader reader = new RestSwaggerReader();
		final Swagger swagger = reader.read(apiDto, config);

		// Get JSON data from Swagger...
		final String swaggerJson = Json.pretty(swagger);

		// ... and converts it with OpenAPI Parser
		final OpenAPIParser openAPIParser = new OpenAPIParser();
		final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(swaggerJson, null, null);
		final OpenAPI openAPI = swaggerParseResult.getOpenAPI();

		return Response.ok(io.swagger.v3.core.util.Json.pretty(openAPI)).build();
	}

	public ApiServiceRest getApiService() {
		return apiService;
	}

	public void setApiService(ApiServiceRest apiService) {
		this.apiService = apiService;
	}

	public ApiFIQL getApiFIQL() {
		return apiFIQL;
	}

	public void setApiFIQL(ApiFIQL apiFIQL) {
		this.apiFIQL = apiFIQL;
	}

	@Override
	public Response getApiWithoutToken(String numVersion, String identification, String vertical)
			throws GenericOPException {
		if (StringUtils.hasText(vertical))
			masterUserService.getVertical(vertical)
					.ifPresent(v -> MultitenancyContextHolder.setVerticalSchema(v.getSchema()));

		if (numVersion.indexOf('v') != -1) {
			numVersion = numVersion.substring(1, numVersion.length());
		}
		final Api api = apiService.getApiByIdentificationAndVersion(identification, numVersion);
		if (api == null) {
			MultitenancyContextHolder.clear();
			return Response.noContent().status(404).build();

		}

		// EXTERNAL API FROM JSON
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final SwaggerParser swaggerParser = new SwaggerParser();
			final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
			if (swagger != null) {
				// SWAGGER PARSER
				return getExternalApiWithSwagger(api, swagger);
			} else {
				// OPENAPI PARSER
				final OpenAPIParser openAPIParser = new OpenAPIParser();
				final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(api.getSwaggerJson(), null,
						null);
				final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
				return getExternalApiWithOpenAPI(api, openAPI);
			}
		}

		// INTERNAL API
		if (api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
			return getInternalApiWithOpenAPI(api, numVersion);
		}

		// OHTER API
		return getOtherApiWithSwagger(api, numVersion);
	}

	private Response getExternalApiWithSwagger(Api api, Swagger swagger) {

		if (!StringUtils.hasText(api.getGraviteeId())) {
			addCustomHeaderToPaths(swagger);
			swagger.setHost(null);
			swagger.setBasePath(getApiBasePath(api, String.valueOf(api.getNumversion())));
		} else {
			swagger.setHost(getGraviteeHost());
			swagger.setBasePath(getGraviteeBasePath(api));
		}
		MultitenancyContextHolder.clear();
		return Response.ok(Json.pretty(swagger)).build();
	}

	private Response getExternalApiWithOpenAPI(Api api, OpenAPI openAPI) {
		addCustomHeaderToPaths(openAPI);
		final Server server = new Server();

		if (StringUtils.hasText(api.getGraviteeId())) {
			server.setUrl(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY) + getGraviteeBasePath(api));
		} else {
			server.setUrl(getApiBasePath(api, String.valueOf(api.getNumversion())));
		}
		openAPI.setServers(Arrays.asList(server));
		MultitenancyContextHolder.clear();
		return Response.ok(io.swagger.v3.core.util.Json.pretty(openAPI)).build();
	}

	private Response getInternalApiWithOpenAPI(Api api, String numVersion) {
		final ApiDTO apiDto = apiFIQL.toApiDTO(api);
		final BeanConfig config = new BeanConfig();

		if (StringUtils.hasText(api.getGraviteeId())) {
			config.setHost(getGraviteeHost());
			config.setBasePath(getGraviteeBasePath(api));
		} else {
			config.setBasePath(getApiBasePath(api, numVersion));
			config.setHost(null);
		}

		final RestSwaggerReader reader = new RestSwaggerReader();
		final Swagger swagger = reader.read(apiDto, config);
		// Get JSON data from Swagger...
		final String swaggerJson = Json.pretty(swagger);

		// ... and converts it with OpenAPI Parser
		final OpenAPIParser openAPIParser = new OpenAPIParser();
		final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(swaggerJson, null, null);
		final OpenAPI openAPI = swaggerParseResult.getOpenAPI();

		final String json = io.swagger.v3.core.util.Json.pretty(openAPI);
		MultitenancyContextHolder.clear();
		return Response.ok(json).build();
	}

	private Response getOtherApiWithSwagger(Api api, String numVersion) {
		final ApiDTO apiDto = apiFIQL.toApiDTO(api);
		final BeanConfig config = new BeanConfig();
		config.setBasePath(getApiBasePath(api, numVersion));
		final RestSwaggerReader reader = new RestSwaggerReader();
		final Swagger swagger = reader.read(apiDto, config);
		if (StringUtils.hasText(api.getGraviteeId())) {
			swagger.setHost(getGraviteeHost());
		}
		MultitenancyContextHolder.clear();
		return Response.ok(Json.pretty(swagger)).build();
	}

	private String getApiBasePath(Api api, String numVersion) {
		return BASE_PATH + "/v" + numVersion + "/" + api.getIdentification();
	}

	private String getGraviteeBasePath(Api api) {
		return "/".concat(api.getIdentification().concat("/v").concat(String.valueOf(api.getNumversion())));
	}

	private String getGraviteeHost() {
		return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY).replaceAll("^(http://|https://)", "");
	}

	/**
	 * Add Authentication Header to Swagger instance.
	 *
	 * @param swagger
	 */
	private void addCustomHeaderToPaths(Swagger swagger) {
		final HeaderParameter header = new HeaderParameter();
		header.setIn("header");
		header.setDescription("Onesait Platform API Key");
		header.setName(Constants.AUTHENTICATION_HEADER);
		header.setRequired(true);
		header.setType("string");
		swagger.getPaths().entrySet().forEach(p -> {
			final Path path = p.getValue();
			path.getOperations().forEach(o -> o.addParameter(header));
		});
	}

	/**
	 * Add Authentication Header to OpenAPI instance.
	 *
	 * @param openAPI
	 */
	private void addCustomHeaderToPaths(OpenAPI openAPI) {
		final io.swagger.v3.oas.models.parameters.HeaderParameter header = new io.swagger.v3.oas.models.parameters.HeaderParameter();
		header.setIn("header");
		header.setDescription("Onesait Platform API Key");
		header.setName(Constants.AUTHENTICATION_HEADER);
		header.setRequired(true);
		final Schema<String> schema = new Schema<>();
		schema.setType("string");
		header.setSchema(schema);
		openAPI.getPaths().entrySet().forEach(p -> {
			final PathItem path = p.getValue();
			path.readOperations().forEach(o -> o.addParametersItem(header));
		});
	}
}

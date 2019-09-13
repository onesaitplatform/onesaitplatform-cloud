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
package com.minsait.onesait.platform.api.rest.api;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.minsait.onesait.platform.api.rest.api.dto.ApiDTO;
import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.rest.swagger.RestSwaggerReader;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;

@Component("swaggerGeneratorServiceImpl")
@Slf4j
public class SwaggerGeneratorServiceImpl implements SwaggerGeneratorService {

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiFIQL apiFIQL;

	private static final String BASE_PATH = "/api-manager/server/api";

	@Autowired
	private IntegrationResourcesService resourcesService;

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

		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String json = null;
		try {
			json = mapper.writeValueAsString(swagger);
		} catch (JsonProcessingException e) {
			log.error("getApi Error", e);
		}

		return Response.ok(json).build();
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
	public Response getApiWithoutToken(String numVersion, String identification) throws GenericOPException {
		if (numVersion.indexOf('v') != -1) {
			numVersion = numVersion.substring(1, numVersion.length());
		}
		final Api api = apiService.getApiByIdentificationAndVersion(identification, numVersion);
		if (api == null)
			return Response.noContent().status(404).build();

		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final SwaggerParser swaggerParser = new SwaggerParser();
			final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());

			addCustomHeaderToPaths(swagger);
			swagger.setHost(null);
			swagger.setBasePath(BASE_PATH + "/v" + api.getNumversion() + "/" + api.getIdentification());

			try {
				return Response.ok(mapper.writeValueAsString(swagger)).build();
			} catch (JsonProcessingException e) {
				log.error("getApiWithoutToken Error", e);
			}
		}
		final ApiDTO apiDto = apiFIQL.toApiDTO(api);

		final BeanConfig config = new BeanConfig();

		config.setBasePath(BASE_PATH + "/v" + numVersion + "/" + api.getIdentification());

		final RestSwaggerReader reader = new RestSwaggerReader();

		final Swagger swagger = reader.read(apiDto, config);

		String json = null;
		try {
			json = mapper.writeValueAsString(swagger);
		} catch (JsonProcessingException e) {
			log.error("getApiWithoutToken Error", e);
		}

		return Response.ok(json).build();
	}

	private void addCustomHeaderToPaths(Swagger swagger) {
		final Parameter header = new HeaderParameter();
		header.setIn("header");
		header.setDescription("onesait Platform API Key");
		header.setName(Constants.AUTHENTICATION_HEADER);
		header.setRequired(true);
		swagger.getPaths().entrySet().forEach(p -> {
			final Path path = p.getValue();
			path.getOperations().forEach(o -> o.addParameter(header));
		});
	}

}

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
package com.minsait.onesait.platform.business.services.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.business.services.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.business.services.api.swagger.RestSwaggerReader;
import com.minsait.onesait.platform.commons.utils.FileIOUtils;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Api.ClientJS;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.persistence.factory.BasicOpsDBRepositoryFactory;
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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class APIBusinessServiceImpl implements APIBusinessService {

	private static final String TMP_DIR = "/tmp";
	private static final String JS_FOLDER = "client-js";
	private static final String PATH_TO_SAMPLE = "/src/data/sample.json";
	private static final String PATH_TO_MENU = "/src/components/MenuVertical.js";
	private static final String PATH_TO_ENV = "/.env.production";
	private static final String OUTPUT_ZIP = "client-js.zip";
	private static final String APIS_VAR = "APIS";
	private static final String SAMPLES_VAR = "SAMPLES";
	private static final String SERVER_NAME_VAR = "SERVER_NAME";
	private static final String BASE_PATH = "/api-manager/server/api";

	@Autowired
	private FileIOUtils fileUtils;
	@Autowired
	private BasicOpsDBRepositoryFactory basicOpsDBRepositoryFactory;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private ApiFIQL apiFIQL;

	@Override
	public File generateJSClient(ClientJS framework, List<String> apiIds, String userId) {
		try {
			String pathToZip = null;
			if (ClientJS.REACT_JS.equals(framework)) {
				pathToZip = "templates/client-react-js.zip";
			} else {
				// TO-DO VUe js
				pathToZip = "";
			}
			final String basePath = TMP_DIR + File.separator + userId + File.separator + JS_FOLDER;
			log.debug("Creating DIRs");
			fileUtils.createDirs(basePath);
			if (log.isDebugEnabled()) {
				log.debug("Unzipping {} to path {}", pathToZip, basePath);
			}
			fileUtils.unzipToPath(pathToZip, basePath);
			final List<Api> apis = apiIds.stream().map(apiManagerService::getById).collect(Collectors.toList());
			log.debug("Compiling templates");
			compileTemplates(basePath, apis);
			if (log.isDebugEnabled()) {
				log.debug("Zipping files to {}", basePath + File.separator + OUTPUT_ZIP);
			}
			return fileUtils.zipFiles(basePath, TMP_DIR + File.separator + userId + File.separator + OUTPUT_ZIP);
		} catch (final Exception e) {
			log.error("Error while generating JS client", e);
			throw new OPResourceServiceException("Could not generate JS client: " + e.getMessage(), e);
		}
	}

	private void compileTemplates(String basePath, List<Api> apis) throws IOException {
		final JsonNode samples = mapper.createObjectNode();
		final ArrayNode apisMenu = mapper.createArrayNode();
		apis.forEach(a -> {
			((ObjectNode) samples).set(a.getIdentification(), getSample(a.getOntology()));
			apisMenu.add(mapper.convertValue(
					ApiClientJS.builder().name(a.getIdentification()).version("v" + a.getNumversion()).build(),
					JsonNode.class));
		});
		final String serverURL = resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE);

		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put(SAMPLES_VAR, mapper.writeValueAsString(samples));
		scopes.put(SERVER_NAME_VAR, serverURL);
		scopes.put(APIS_VAR, apisMenu);
		compileTemplate(basePath + PATH_TO_MENU, scopes);
		compileTemplate(basePath + PATH_TO_SAMPLE, scopes);
		compileTemplate(basePath + PATH_TO_ENV, scopes);

	}

	private void compileTemplate(String writePath, Map<String, Object> scopes) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Compiling template {}", writePath);
		}
		final String content = new String(Files.readAllBytes(Paths.get(writePath)));

		try (Writer writer = new FileWriter(writePath)) {

			final MustacheFactory mf = new DefaultMustacheFactory();
			final Mustache mustache = mf.compile(new StringReader(content), "compile");
			mustache.execute(writer, scopes);
			writer.flush();
		} catch (final IOException e) {
			log.error("error at file {}", writePath);
			throw e;
		}
	}

	private JsonNode getSample(Ontology ontology) {
		final String result = basicOpsDBRepositoryFactory.getInstance(ontology.getRtdbDatasource())
				.findAllAsJson(ontology.getIdentification(), 1);
		if (log.isDebugEnabled()) {
			log.debug("Getting sample for ontology {}", ontology.getIdentification());
		}
		ArrayNode arrayResult = null;
		try {
			arrayResult = mapper.readValue(result, ArrayNode.class);

		} catch (final IOException e) {
			log.error("Object mapper error while getting samples", e);
			throw new OPResourceServiceException("Object mapper error while getting api-ontology samples");
		}
		if (arrayResult.size() == 0) {
			throw new OPResourceServiceException("Selected API with ontology " + ontology + " has no records");
		}
		try {
			return mapper.readValue(arrayResult.get(0).asText(), JsonNode.class);
		} catch (final IOException e) {
			throw new OPResourceServiceException(
					"Error while parsing json for ontology " + ontology.getIdentification());
		}
	}

	@Override
	public String getSwagger(Api api) {
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final SwaggerParser swaggerParser = new SwaggerParser();
			final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
			if (swagger != null) {
				// SWAGGER PARSER
				return io.swagger.v3.core.util.Json.pretty(swagger);
			} else {
				// OPENAPI PARSER
				final OpenAPIParser openAPIParser = new OpenAPIParser();
				final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(api.getSwaggerJson(), null,
						null);
				final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
				return io.swagger.v3.core.util.Json.pretty(openAPI);
			}
		}
		// INTERNAL API
		if (api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
			return getSwaggerJSONInternal(api);
		}

		// OHTER API
		return getOtherApiWithSwagger(api, api.getNumversion());
	}

	@Override
	public OpenAPI getOpenAPI(Api api) {
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
				return openAPI;
			}
		}
		// INTERNAL API
		if (api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
			return getSwaggerJSONInternalOpenAPI(api);
		}

		// OHTER API
		return getOtherApiWithSwaggerOpenAPI(api, api.getNumversion());
	}

	@Override
	public String getSwaggerJSONInternal(Api api) {
		return io.swagger.v3.core.util.Json.pretty(getSwaggerJSONInternalOpenAPI(api));
	}

	@Override
	public OpenAPI getSwaggerJSONInternalOpenAPI(Api api) {
		final ApiDTO apiDto = apiFIQL.toApiDTO(api);
		final BeanConfig config = new BeanConfig();

		if (StringUtils.hasText(api.getGraviteeId())) {
			config.setHost(getGraviteeHost());
			config.setBasePath(getGraviteeBasePath(api));
		} else {
			config.setBasePath(getApiBasePath(api, api.getNumversion()));
			config.setHost(getApiHost());
		}

		final RestSwaggerReader reader = new RestSwaggerReader();
		final Swagger swagger = reader.read(apiDto, config);
		// Get JSON data from Swagger...
		final String swaggerJson = Json.pretty(swagger);

		// ... and converts it with OpenAPI Parser
		final OpenAPIParser openAPIParser = new OpenAPIParser();
		final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(swaggerJson, null, null);
		final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
		openAPI.getInfo().setTitle(api.getIdentification());
		openAPI.getInfo().setVersion("v" + api.getNumversion());
		MultitenancyContextHolder.clear();
		return openAPI;
	}

	@Override
	public String getSwaggerJSONExternal(Api api) {
		return io.swagger.v3.core.util.Json.pretty(getSwaggerJSONExternalOpenAPI(api));
	}

	@Override
	public OpenAPI getSwaggerJSONExternalOpenAPI(Api api) {
		final SwaggerParser swaggerParser = new SwaggerParser();
		final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
		if (swagger != null) {
			// SWAGGER PARSER
			return getExternalApiWithSwagger(api, swagger);
		} else {
			// OPENAPI PARSER
			final OpenAPIParser openAPIParser = new OpenAPIParser();
			final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(api.getSwaggerJson(), null, null);
			final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
			return getExternalApiWithOpenAPI(api, openAPI);
		}

	}

	@Override
	public String getOtherApiWithSwagger(Api api, Integer numVersion) {
		return Json.pretty(getOtherApiWithSwaggerOpenAPI(api, numVersion));
	}

	@Override
	public OpenAPI getOtherApiWithSwaggerOpenAPI(Api api, Integer numVersion) {
		final ApiDTO apiDto = apiFIQL.toApiDTO(api);
		final BeanConfig config = new BeanConfig();
		config.setBasePath(getApiBasePath(api, numVersion));
		final RestSwaggerReader reader = new RestSwaggerReader();
		final Swagger swagger = reader.read(apiDto, config);
		if (StringUtils.hasText(api.getGraviteeId())) {
			swagger.setHost(getGraviteeHost());
		} else {
			swagger.setHost(getApiHost());
		}
		MultitenancyContextHolder.clear();
		final OpenAPI openAPI = new OpenAPIParser().readContents(Json.pretty(swagger), null, null).getOpenAPI();
		openAPI.getInfo().setTitle(api.getIdentification());
		openAPI.getInfo().setVersion("v" + api.getNumversion());

		return openAPI;
	}

	private OpenAPI getExternalApiWithSwagger(Api api, Swagger swagger) {

		if (!StringUtils.hasText(api.getGraviteeId())) {
			addCustomHeaderToPaths(swagger);
			swagger.setHost(null);
			swagger.setBasePath(getApiBasePath(api, api.getNumversion()));
		} else {
			swagger.setHost(getGraviteeHost());
			swagger.setBasePath(getGraviteeBasePath(api));
		}
		MultitenancyContextHolder.clear();
		return new OpenAPIParser().readContents(Json.pretty(swagger), null, null).getOpenAPI();
	}

	private OpenAPI getExternalApiWithOpenAPI(Api api, OpenAPI openAPI) {
		addCustomHeaderToPaths(openAPI);
		final Server server = new Server();

		if (StringUtils.hasText(api.getGraviteeId())) {
			server.setUrl(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY) + getGraviteeBasePath(api));
		} else {
			server.setUrl(getApiBasePath(api, api.getNumversion()));
		}
		openAPI.setServers(Arrays.asList(server));
		MultitenancyContextHolder.clear();
		return openAPI;
	}

	private String getApiBasePath(Api api, Integer numVersion) {
		return BASE_PATH + "/v" + numVersion + "/" + api.getIdentification();
	}

	private String getApiHost() {
		try {
			final URI uri = new URI(resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
			return uri.getPort() == -1 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
		} catch (final URISyntaxException e) {
			log.error("Error un URI", e);
			return null;
		}

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
		header.setName("X-OP-APIKey");
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
		header.setName("X-OP-APIKey");
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

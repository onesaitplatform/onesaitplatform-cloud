/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.services.exceptions.FlowDomainServiceException;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiInvocationParams;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationParamDTO;
import com.minsait.onesait.platform.flowengine.exception.NoValueForParamIvocationException;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpenAPI3Utils {

	private RestTemplate template;
	@Autowired
	IntegrationResourcesService resourcesService;
	final Map<String, Components> cacheExternalReferences = new HashMap<>();

	private static final String DEFAULT_RESPONSE_DESC = "Other status code";

	@PostConstruct
	private void setUp() {
		template = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		template.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	public List<RestApiOperationDTO> getOperationsFromOpenAPI(String openApi, String opNameFilter,
			String opMethodFilter) {
		final List<RestApiOperationDTO> operationNames = new ArrayList<>();

		final OpenAPI openAPI = getOpenAPI(openApi);
		openAPI.getPaths().entrySet().forEach(p -> {
			final PathItem path = p.getValue();
			path.getParameters();
			path.readOperationsMap().entrySet().forEach(op -> {
				if (opNameFilter != null && opMethodFilter != null
						&& op.getValue().getOperationId().equals(opNameFilter)
						&& op.getKey().toString().equals(opMethodFilter)
						|| opMethodFilter == null && opNameFilter == null) {
					final RestApiOperationDTO opDTO = new RestApiOperationDTO();
					opDTO.setPath(p.getKey());
					opDTO.setName(op.getValue().getOperationId());
					opDTO.setMethod(op.getKey().name());
					final List<RestApiOperationParamDTO> parameters = new ArrayList<>();
					Stream<Parameter> openApiParams = Stream.empty();
					if (path.getParameters() == null && op.getValue().getParameters() != null) {
						openApiParams = op.getValue().getParameters().stream().filter(Objects::nonNull);
					} else if (path.getParameters() != null) {
						if (op.getValue().getParameters() != null) {
							openApiParams = Stream.of(path.getParameters(), op.getValue().getParameters())
									.flatMap(List::stream).filter(Objects::nonNull);
						} else {
							openApiParams = path.getParameters().stream();
						}
					}

					openApiParams.forEach(param -> {
						try {
							parameters.add(getApiParam(param, openAPI, cacheExternalReferences));
						} catch (final Exception e) {
							// DON'T ADD CONFLICTIVE PARAMETER
							log.error("Error parsing OpenAPI parameter {}", param.toString());
						}
					});
					if (op.getValue().getRequestBody() != null) {
						parameters.add(getApiBody(op.getValue().getRequestBody(), openAPI, cacheExternalReferences));
					}
					final Map<String, String> statusCodes = new HashMap<>();
					op.getValue().getResponses().entrySet().forEach(rsp -> statusCodes.put(rsp.getKey(),
							getStatusDescription(rsp.getValue(), openAPI, cacheExternalReferences)));

					if (op.getValue().getResponses().isEmpty()) {
						opDTO.setReturnMessagesresponseCodes(getDefaultStatusCodes());
					} else {
						// always add "other" statusCode
						statusCodes.put("???", DEFAULT_RESPONSE_DESC);
						opDTO.setReturnMessagesresponseCodes(statusCodes);
					}
					opDTO.setParams(parameters);
					operationNames.add(opDTO);
				}

			});

		});

		return operationNames;
	}

	public OpenAPI getOpenAPI(String openAPI) {
		final OpenAPIParser openAPIParser = new OpenAPIParser();
		final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(openAPI, null, null);
		return swaggerParseResult.getOpenAPI();
	}

	private RestApiOperationParamDTO getApiParam(Parameter parameter, OpenAPI openAPI,
			Map<String, Components> cacheExternalReferences) {
		if (parameter.get$ref() == null) {
			final RestApiOperationParamDTO paramDTO = new RestApiOperationParamDTO();
			paramDTO.setName(parameter.getName());
			paramDTO.setRequired(parameter.getRequired() != null ? parameter.getRequired() : Boolean.FALSE);
			paramDTO.setType(parameter.getIn().toUpperCase());
			paramDTO.setRequired(parameter.getRequired() != null ? parameter.getRequired() : Boolean.FALSE);
			return paramDTO;
		} else {
			return getApiParamFromRef(parameter, openAPI, cacheExternalReferences);

		}
	}

	private RestApiOperationParamDTO getApiBody(RequestBody body, OpenAPI openAPI,
			Map<String, Components> cacheExternalReferences) {

		final RestApiOperationParamDTO paramDTO = new RestApiOperationParamDTO();
		paramDTO.setName("body");
		paramDTO.setType("body");
		return paramDTO;

	}

	private String getStatusDescription(ApiResponse response, OpenAPI openAPI,
			Map<String, Components> cacheExternalReferences) {
		try {
			if (response.get$ref() == null) {
				return response.getDescription();
			} else {
				return getStatusDescriptionFromRef(response, openAPI, cacheExternalReferences);
			}
		} catch (final Exception e) {
			log.error("Could not compute api response description, returning default.", e);
			return DEFAULT_RESPONSE_DESC;
		}

	}

	private String getStatusDescriptionFromRef(ApiResponse response, OpenAPI openAPI,
			Map<String, Components> cacheExternalReferences) {
		final String ref = response.get$ref();
		if (ref.startsWith("#")) {
			return openAPI.getComponents().getResponses().get(getParameterComponent(ref)).getDescription();
		} else {
			// Is an http reference -> download yaml and get component.
			final String[] splitedRef = ref.split("#");
			final String url = splitedRef[0];
			if (cacheExternalReferences.get(url) != null) {
				log.debug("getStatusDescriptionFromRef: Returning cached instance for url {}", url);
				return cacheExternalReferences.get(url).getResponses().get(getParameterComponent(splitedRef[1]))
						.getDescription();
			} else {
				log.debug("getStatusDescriptionFromRef: Downloading decriptor from url {}", url);
				final OpenAPI yaml = getOpenApiYaml(url);
				final String description = yaml.getComponents().getResponses().get(getParameterComponent(splitedRef[1]))
						.getDescription();
				cacheExternalReferences.put(url, yaml.getComponents());
				return description;
			}
		}

	}

	private RestApiOperationParamDTO getApiParamFromRef(Parameter parameter, OpenAPI openAPI,
			Map<String, Components> cacheExternalReferences) {

		final RestApiOperationParamDTO paramDTO = new RestApiOperationParamDTO();
		final String ref = parameter.get$ref();
		if (ref.startsWith("#")) {
			final Parameter p = openAPI.getComponents().getParameters().get(getParameterComponent(ref));
			if (p != null) {
				paramDTO.setName(p.getName());
				paramDTO.setType(p.getIn().toUpperCase());
				paramDTO.setRequired(p.getRequired() != null ? p.getRequired() : Boolean.FALSE);
			}
		} else {
			// Is an http reference -> download yaml and get component.
			final String[] splitedRef = ref.split("#");
			final String url = splitedRef[0];
			if (cacheExternalReferences.get(url) != null) {
				log.debug("getApiParamFromRef: Returning cached instance for url {}", url);
				final Parameter p = cacheExternalReferences.get(url).getParameters()
						.get(getParameterComponent(splitedRef[1]));
				paramDTO.setName(p.getName());
				paramDTO.setType(p.getIn().toUpperCase());
				paramDTO.setRequired(p.getRequired() != null ? p.getRequired() : Boolean.FALSE);

				return paramDTO;
			}
			log.debug("getApiParamFromRef: Downloading decriptor from url {}", url);
			final OpenAPI yaml = getOpenApiYaml(url);
			final Parameter p = yaml.getComponents().getParameters().get(getParameterComponent(splitedRef[1]));
			if (p != null) {
				paramDTO.setName(p.getName());
				paramDTO.setType(p.getIn().toUpperCase());
				paramDTO.setRequired(p.getRequired() != null ? p.getRequired() : Boolean.FALSE);
				cacheExternalReferences.put(url, yaml.getComponents());
			}
		}

		return paramDTO;
	}

	private String getParameterComponent(String ref) {
		final String[] paths = ref.split("/");
		return paths[paths.length - 1];
	}

	private OpenAPI getOpenApiYaml(String url) {
		final ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, null, String.class);

		final OpenAPIParser openAPIParser = new OpenAPIParser();
		final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(response.getBody(), null, null);
		return swaggerParseResult.getOpenAPI();
	}

	private Map<String, String> getDefaultStatusCodes() {
		final Map<String, String> statusCodes = new HashMap<>();
		statusCodes.put("200", "OK");
		statusCodes.put("204", "No Content");
		statusCodes.put("400", "Bad Request");
		statusCodes.put("401", "Unauthorized");
		statusCodes.put("501", "Internal Server Error");
		statusCodes.put("???", DEFAULT_RESPONSE_DESC);
		return statusCodes;
	}

	public RestApiInvocationParams getInvocationParamsForSwaggerOperation(Api selectedApi,
			FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		final RestApiInvocationParams resultInvocationParams = new RestApiInvocationParams();
		final Optional<RestApiOperationDTO> op = getOperationsFromOpenAPI(selectedApi.getSwaggerJson(),
				invokeRequest.getOperationName(), invokeRequest.getOperationMethod()).stream().findFirst();
		op.ifPresent(rop -> {
			resultInvocationParams.setMethod(Type.valueOf(invokeRequest.getOperationMethod()));
			resultInvocationParams.setUrl(resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.API).concat("/v")
					.concat(String.valueOf(selectedApi.getNumversion())).concat("/")
					.concat(selectedApi.getIdentification()).concat(rop.getPath()));
			// Parameters
			fillSwaggerInvocationParams(rop.getParams(), selectedApi, invokeRequest, resultInvocationParams);
		});

		return resultInvocationParams;

	}

	private void fillSwaggerInvocationParams(List<RestApiOperationParamDTO> params, Api selectedApi,
			FlowEngineInvokeRestApiOperationRequest invokeRequest, RestApiInvocationParams resultInvocationParams) {
		for (final RestApiOperationParamDTO param : params) {
			// QUERY, PATH, BODY (formData ignore) or HEADER
			String value = "";
			boolean skipParam = false;
			try {
				value = getValueForParam(param.getName(), invokeRequest.getOperationInputParams());
			} catch (final FlowDomainServiceException e) {

				final String msg = "No value was found for parameter " + param.getName() + " in operation ["
						+ invokeRequest.getOperationMethod() + "] - " + invokeRequest.getOperationName() + " from API ["
						+ invokeRequest.getApiVersion() + "] - " + selectedApi.getIdentification() + ".";
				if (param.getRequired()) {
					log.error(msg);
					throw new NoValueForParamIvocationException(msg);
				} else {
					log.debug("Skipping parameter. Optional parameter not received. " + msg);
					skipParam = true;
				}
			}
			if (!skipParam) {
				switch (param.getType().toUpperCase()) {
				case "QUERY":
					resultInvocationParams.getQueryParams().put(param.getName(), value);
					break;
				case "PATH":
					resultInvocationParams.getPathParams().put(param.getName(), value);
					break;
				case "BODY":
					resultInvocationParams.setBody(value);
					break;
				case "HEADER":
					resultInvocationParams.getHeaders().add(param.getName(), value);
					break;
				default:
					break;
				}
			}
		}

	}

	private String getValueForParam(String paramName, List<Map<String, String>> paramValues) {
		String value = "";
		boolean found = false;
		for (final Map<String, String> parameterValues : paramValues) {
			if (parameterValues.get("name") != null && parameterValues.get("name").equals(paramName)) {
				value = parameterValues.get("value");
				found = true;
				break;
			}
		}
		if (!found) {
			log.error("No value was defined for param {}.", paramName);
			throw new FlowDomainServiceException("No value was defined for param '" + paramName + "'.");
		}
		return value;
	}
}
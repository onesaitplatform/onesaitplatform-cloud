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
package com.minsait.onesait.platform.api.processor.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
import com.minsait.onesait.platform.api.cache.ApiCacheService;
import com.minsait.onesait.platform.api.processor.ApiProcessor;
import com.minsait.onesait.platform.api.processor.ScriptProcessorFactory;
import com.minsait.onesait.platform.api.processor.utils.ApiProcessorUtils;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.api.util.OpenAPIUtils;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.User;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExternalJsonApiProcessor implements ApiProcessor {

	@Autowired
	private ApiManagerService apiManagerService;

	@Autowired
	private ApiCacheService apiCacheService;

	@Autowired
	private com.minsait.onesait.platform.config.services.apimanager.ApiManagerService apiManagerServiceConfig;
	final Map<String, Components> cacheExternalReferences = new HashMap<>();

	@Autowired
	private OpenAPIUtils openAPIUtils;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	private void setUp() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	@Autowired
	private ScriptProcessorFactory scriptEngine;

	@Override
	@ApiManagerAuditable
	public Map<String, Object> process(Map<String, Object> data) throws GenericOPException {
		final Api api = (Api) data.get(Constants.API);
		if (api.getApicachetimeout() !=null && data.get(Constants.METHOD).equals("GET")) {
			data = apiCacheService.getCache(data, api.getApicachetimeout());
		}

		if (data.get(Constants.OUTPUT)==null) {
			proxyHttp(data);
			postProcess(data);
		}

		if (api.getApicachetimeout() !=null && data.get(Constants.METHOD).equals("GET")) {
			apiCacheService.putCache(data, api.getApicachetimeout());
		}
		return data;
	}

	@Override
	public List<ApiType> getApiProcessorTypes() {
		return Collections.singletonList(ApiType.EXTERNAL_FROM_JSON);
	}

	private String getUrl(Api api, String pathInfo, Map<String, String[]> queryParams) {
		String url = null;

		final SwaggerParser swaggerParser = new SwaggerParser();
		final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
		if (swagger != null) {
			url = getUrl(swagger, pathInfo);
		} else {
			final OpenAPIParser openAPIParser = new OpenAPIParser();
			final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(api.getSwaggerJson(), null, null);
			final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
			if (!openAPI.getServers().isEmpty()) {
				url = getServerUrl(openAPI, pathInfo);
			}
		}
		url = addExtraQueryParameters(url, queryParams);
		return url;
	}

	private void proxyHttp(Map<String, Object> data) {
		log.debug("Beggining external request");
		final String method = (String) data.get(Constants.METHOD);
		final String pathInfo = (String) data.get(Constants.PATH_INFO);
		final byte[] body = (byte[]) data.get(Constants.BODY);
		final Api api = (Api) data.get(Constants.API);
		final HttpServletRequest request = (HttpServletRequest) data.get(Constants.REQUEST);
		@SuppressWarnings("unchecked")
		final Map<String, String[]> queryParams = (Map<String, String[]>) data.get(Constants.QUERY_PARAMS);
		String url = null;
		ResponseEntity<byte[]> result = null;
		try {
			url = getUrl(api, pathInfo, queryParams);

			final HttpHeaders headers = new HttpHeaders();
			addHeaders(headers, request, api);
			final HttpEntity<?> entity;
			if (ServletFileUpload.isMultipartContent(request)) {
				entity = new HttpEntity<>(addParameters(request, data), headers);
			} else {
				entity = new HttpEntity<>(body, headers);
			}
			log.info("Executing resttemplate");
			switch (method) {
			case "GET":
				result = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
				break;
			case "POST":
				result = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
				break;
			case "PUT":
				result = restTemplate.exchange(url, HttpMethod.PUT, entity, byte[].class);
				break;
			case "DELETE":
				result = restTemplate.exchange(url, HttpMethod.DELETE, entity, byte[].class);
				break;
			default:
				break;

			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);

			data.put(Constants.HTTP_RESPONSE_CODE, e.getStatusCode());

			data.put(Constants.REASON, e.getResponseBodyAsString());
			data.put(Constants.HTTP_RESPONSE_HEADERS, e.getResponseHeaders());

			throw e;
		} catch (final ResourceAccessException e) {
			log.error("Error: accessing resource ", e);
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);

			data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);

			data.put(Constants.REASON, e.getMessage());
			throw e;

		} catch (final Exception e) {
			log.error("Error buildigin request to external endpoint : {},   {}", url, e);
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);
			data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			data.put(Constants.REASON,
					"Error while processing swagger for external endpoint, please review your Swagger client definition (schemes, basePath, etc");

			throw e;
		} finally {
			log.debug("Ending external request");
			deleteTmpFiles(data);
		}

		data.put(Constants.HTTP_RESPONSE_CODE, result.getStatusCode());
		data.put(Constants.OUTPUT, result.getBody());
		data.put(Constants.HTTP_RESPONSE_HEADERS, result.getHeaders());

	}

	private void postProcess(Map<String, Object> data) {
		final Api api = (Api) data.get(Constants.API);
		final String method = (String) data.get(Constants.METHOD);
		if (apiManagerServiceConfig.postProcess(api) && method.equalsIgnoreCase("get")) {
			final User user = (User) data.get(Constants.USER);
			String postProcess = apiManagerServiceConfig.getPostProccess(api);
			postProcess = postProcess.replace(Constants.CONTEXT_USER, user.getUserId());

			if (StringUtils.hasText(postProcess)) {
				try {
					final Object result = scriptEngine.invokeScript(postProcess, data.get(Constants.OUTPUT));
					data.put(Constants.OUTPUT, result);
				} catch (final ScriptException e) {
					log.error("Execution logic for postprocess error", e);
					data.put(Constants.STATUS, ChainProcessingStatus.STOP);
					data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR from Scripting Post Process", "Execution logic for Postprocess error",
							e.getCause().getMessage());
					data.put(Constants.REASON, messageError);

				} catch (final Exception e) {
					data.put(Constants.STATUS, ChainProcessingStatus.STOP);
					data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR from Scripting Post Process", "Exception detected", e.getCause().getMessage());
					data.put(Constants.REASON, messageError);
				}
			}
		}
	}

	private String getUrl(Swagger swagger, String pathInfo) {
		String scheme = Constants.HTTPS.toLowerCase();
		if (swagger.getSchemes() == null || !swagger.getSchemes().stream().map(Enum::name).collect(Collectors.toList())
				.contains(Constants.HTTPS)) {
			scheme = Constants.HTTP.toLowerCase();
		}

		String url = scheme + "://" + swagger.getHost();
		if (swagger.getBasePath() != null) {
			url = url.concat(swagger.getBasePath());
		}
		final String apiIdentifier = apiManagerService.getApiIdentifier(pathInfo);
		final String swaggerPath = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length(),
				pathInfo.length());
		return url.concat(swaggerPath);
	}

	private String addExtraQueryParameters(String url, Map<String, String[]> queryParams) {
		final StringBuilder sb = new StringBuilder(url);
		if (queryParams.size() > 0) {
			sb.append("?");
			queryParams.entrySet().forEach(e -> {
				final String param = e.getKey() + "=" + String.join("", e.getValue());
				sb.append(param).append("&");
			});
		}
		return sb.toString();
	}

	private HttpHeaders addHeaders(HttpHeaders headers, HttpServletRequest request, Api api) {
		final SwaggerParser swaggerParser = new SwaggerParser();
		final Swagger swagger = swaggerParser.parse(api.getSwaggerJson());
		if (swagger != null) {
			swagger.getPaths().entrySet().forEach(e -> {
				final Path path = e.getValue();
				path.getOperationMap().entrySet().forEach(op -> {
					final Operation operation = op.getValue();
					operation.getParameters().stream().filter(p -> p instanceof HeaderParameter).forEach(p -> {
						final String header = request.getHeader(p.getName());
						if (StringUtils.hasText(header) && !headers.containsKey(p.getName())) {
							headers.add(p.getName(), header);
						}
					});
				});
			});
		} else {
			final OpenAPIParser openAPIParser = new OpenAPIParser();
			final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(api.getSwaggerJson(), null, null);
			final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
			openAPI.getPaths().entrySet().forEach(e -> {
				final PathItem path = e.getValue();
				final List<Parameter> parameters = path.getParameters() == null ? new ArrayList<>()
						: path.getParameters();
				path.readOperationsMap().entrySet().forEach(op -> {
					final io.swagger.v3.oas.models.Operation operation = op.getValue();
					if (operation.getParameters() != null) {
						parameters.addAll(operation.getParameters());
					}

					parameters.stream().forEach(p -> {
						String name = null;
						if (p.get$ref() != null) {
							name = getHeaderNameFromRef(openAPI, p, cacheExternalReferences);
						} else {
							if (p instanceof io.swagger.v3.oas.models.parameters.HeaderParameter) {
								name = p.getName();
							}
						}
						if (StringUtils.hasText(name)) {
							final String header = request.getHeader(name);
							if (StringUtils.hasText(header) && !headers.containsKey(name)) {
								headers.add(name, header);
							}
						}
					});

				});
			});
		}
		final String contentType = request.getContentType();
		if (contentType == null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
		} else {
			headers.setContentType(MediaType.valueOf(contentType));
		}
		return headers;
	}

	private String getHeaderNameFromRef(OpenAPI openAPI, Parameter p, Map<String, Components> cacheExternalReferences) {
		final String ref = p.get$ref();
		if (ref.startsWith("#")) {
			final Parameter parameter = openAPI.getComponents().getParameters().get(getParameterComponent(ref));
			if (parameter instanceof io.swagger.v3.oas.models.parameters.HeaderParameter) {
				return parameter.getName();
			}
		} else {
			// Is an http reference -> download yaml and get component.
			final String[] splitedRef = ref.split("#");
			final String url = splitedRef[0];
			if (cacheExternalReferences.get(url) != null) {
				log.debug("getHeaderNameFromRef: Returning cached instance for url {}", url);
				final Parameter parameter = cacheExternalReferences.get(url).getParameters()
						.get(getParameterComponent(splitedRef[1]));
				if (parameter instanceof io.swagger.v3.oas.models.parameters.HeaderParameter) {
					log.debug("returning cached url");
					return parameter.getName();
				}
			} else {
				log.debug("getHeaderNameFromRef: Downloading decriptor from url {}", url);
				final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
				final OpenAPIParser openAPIParser = new OpenAPIParser();
				final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(response.getBody(), null,
						null);
				final OpenAPI yaml = swaggerParseResult.getOpenAPI();

				final Parameter parameter = yaml.getComponents().getParameters()
						.get(getParameterComponent(splitedRef[1]));
				if (parameter instanceof io.swagger.v3.oas.models.parameters.HeaderParameter) {
					cacheExternalReferences.put(url, yaml.getComponents());
					return parameter.getName();
				}

			}
		}
		return null;

	}

	private String getParameterComponent(String ref) {
		final String[] paths = ref.split("/");
		return paths[paths.length - 1];
	}

	private String getServerUrl(OpenAPI openAPI, String pathInfo) {
		// TO-DO rethink how to manage multiple servers
		final String firstServer = openAPIUtils.mapServersToEndpoint(openAPI.getServers()).get(0);
		final String apiIdentifier = apiManagerService.getApiIdentifier(pathInfo);
		final String swaggerPath = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length(),
				pathInfo.length());
		return firstServer + swaggerPath;

	}

	@SuppressWarnings("unchecked")
	private MultiValueMap<String, Object> addParameters(HttpServletRequest request, Map<String, Object> data) {
		return (MultiValueMap<String, Object>) data.get(ApiServiceInterface.FORM_PARAMETER_MAP);
	}

	@SuppressWarnings("unchecked")
	private void deleteTmpFiles(Map<String, Object> data) {
		final HttpServletRequest request = (HttpServletRequest) data.get(ApiServiceInterface.REQUEST);
		if (ServletFileUpload.isMultipartContent(request)) {
			((MultiValueMap<String, Object>) data.get(ApiServiceInterface.FORM_PARAMETER_MAP)).entrySet().forEach(e -> {
				if (e.getValue() != null) {
					final List<Object> params = e.getValue();
					if (params.size() > 0 && params.get(0) instanceof FileSystemResource) {
						((FileSystemResource) e.getValue().get(0)).getFile().delete();
					}
				}

			});
		}
	}
}

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
package com.minsait.onesait.platform.api.processor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExternalJsonApiProcessor implements ApiProcessor {

	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private com.minsait.onesait.platform.config.services.apimanager.ApiManagerService apiManagerServiceConfig;

	@Autowired
	private OpenAPIUtils openAPIUtils;
	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@Autowired
	private ScriptProcessorFactory scriptEngine;

	@Override
	@ApiManagerAuditable
	public Map<String, Object> process(Map<String, Object> data) throws GenericOPException {
		proxyHttp(data);
		postProcess(data);
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
			if (!openAPI.getServers().isEmpty())
				url = getServerUrl(openAPI, pathInfo);
		}
		url = addExtraQueryParameters(url, queryParams);
		return url;
	}

	private void proxyHttp(Map<String, Object> data) {
		final String method = (String) data.get(Constants.METHOD);
		final String pathInfo = (String) data.get(Constants.PATH_INFO);
		final byte[] body = (byte[]) data.get(Constants.BODY);
		final Api api = (Api) data.get(Constants.API);
		final HttpServletRequest request = (HttpServletRequest) data.get(Constants.REQUEST);
		@SuppressWarnings("unchecked")
		final Map<String, String[]> queryParams = (Map<String, String[]>) data.get(Constants.QUERY_PARAMS);
		String url = null;
		byte[] result = null;
		try {
			url = getUrl(api, pathInfo, queryParams);

			final HttpHeaders headers = new HttpHeaders();
			addHeaders(headers, request, api);
			final HttpEntity<?> entity;
			if (ServletFileUpload.isMultipartContent(request))
				entity = new HttpEntity<>(addParameters(request, data), headers);
			else {
				url = addExtraQueryParameters(url, queryParams);
				entity = new HttpEntity<>(body, headers);
			}
			switch (method) {
			case "GET":
				result = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class).getBody();
				break;
			case "POST":
				result = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class).getBody();
				break;
			case "PUT":
				result = restTemplate.exchange(url, HttpMethod.PUT, entity, byte[].class).getBody();
				break;
			case "DELETE":
				result = restTemplate.exchange(url, HttpMethod.DELETE, entity, byte[].class).getBody();
				break;
			default:
				break;

			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);

			data.put(Constants.HTTP_RESPONSE_CODE, e.getStatusCode());

			data.put(Constants.REASON, e.getResponseBodyAsString());

			throw e;
		} catch (final Exception e) {
			log.error("Error buildigin request to external endpoint : {},   {}", url, e);
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);
			data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			data.put(Constants.REASON,
					"Error while processing swagger for external endpoint, please review your Swagger client definition (schemes, basePath, etc");

			throw e;
		} finally {
			deleteTmpFiles(data);
		}

		data.put(Constants.OUTPUT, result);
	}

	private void postProcess(Map<String, Object> data) {
		final Api api = (Api) data.get(Constants.API);
		final String method = (String) data.get(Constants.METHOD);
		if (apiManagerServiceConfig.postProcess(api) && method.equalsIgnoreCase("get")) {
			final User user = (User) data.get(Constants.USER);
			String postProcess = apiManagerServiceConfig.getPostProccess(api);
			postProcess = postProcess.replace(Constants.CONTEXT_USER, user.getUserId());

			if (!StringUtils.isEmpty(postProcess)) {
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
				.contains(Constants.HTTPS))
			scheme = Constants.HTTP.toLowerCase();

		String url = scheme + "://" + swagger.getHost();
		if (swagger.getBasePath() != null)
			url = url.concat(swagger.getBasePath());
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
				sb.append(param).append("&&");
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
						if (!StringUtils.isEmpty(header) && !headers.containsKey(p.getName()))
							headers.add(p.getName(), header);
					});
				});
			});
		} else {
			final OpenAPIParser openAPIParser = new OpenAPIParser();
			final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(api.getSwaggerJson(), null, null);
			final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
			openAPI.getPaths().entrySet().forEach(e -> {
				final PathItem path = e.getValue();
				path.readOperationsMap().entrySet().forEach(op -> {
					final io.swagger.v3.oas.models.Operation operation = op.getValue();
					if (operation.getParameters() != null) {
						operation.getParameters().stream()
								.filter(p -> p instanceof io.swagger.v3.oas.models.parameters.HeaderParameter)
								.forEach(p -> {
									final String header = request.getHeader(p.getName());
									if (!StringUtils.isEmpty(header) && !headers.containsKey(p.getName()))
										headers.add(p.getName(), header);
								});
					}
				});
			});
		}
		final String contentType = request.getContentType();
		if (contentType == null)
			headers.setContentType(MediaType.APPLICATION_JSON);
		else
			headers.setContentType(MediaType.valueOf(contentType));
		return headers;
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
					if (params.size() > 0 && params.get(0) instanceof FileSystemResource)
						((FileSystemResource) e.getValue().get(0)).getFile().delete();
				}

			});
		}
	}
}
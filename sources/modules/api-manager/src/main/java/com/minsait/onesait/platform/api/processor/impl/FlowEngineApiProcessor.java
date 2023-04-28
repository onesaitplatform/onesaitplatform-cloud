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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.libraries.nodered.auth.NoderedAuthenticationService;

import io.swagger.models.Swagger;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowEngineApiProcessor implements ApiProcessor {

	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private com.minsait.onesait.platform.config.services.apimanager.ApiManagerService apiManagerServiceConfig;

	@Autowired
	private NoderedAuthenticationService noderedAuthService;
	@Autowired
	private FlowDomainService domainService;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@Autowired
	private ScriptProcessorFactory scriptEngine;

	@Autowired
	private ApiCacheService apiCacheService;

	@PostConstruct
	void setUTF8Encoding() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	@Override
	@ApiManagerAuditable
	public Map<String, Object> process(Map<String, Object> data) throws GenericOPException {
		Api api = (Api) data.get(Constants.API);
		if (api.getApicachetimeout() !=null && data.get(Constants.METHOD).equals("GET")) {
			data = apiCacheService.getCache(data, api.getApicachetimeout());
		}

		if (data.get(Constants.OUTPUT)==null) {
			data = proxyHttp(data);
			data = postProcess(data);
		}

		if (api.getApicachetimeout() !=null && data.get(Constants.METHOD).equals("GET")) {
			apiCacheService.putCache(data, api.getApicachetimeout());
		}
		return data;
	}

	@Override
	public List<ApiType> getApiProcessorTypes() {
		return Collections.singletonList(ApiType.NODE_RED);
	}

	private Map<String, Object> proxyHttp(Map<String, Object> data) {
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		final String pathInfo = (String) data.get(ApiServiceInterface.PATH_INFO);
		final String pathOperation = apiManagerService.getOperationPath(pathInfo);
		/*
		 * final byte[] requestBody = (byte[]) data.get(ApiServiceInterface.BODY); final
		 * String body = new String(requestBody);
		 */
		final byte[] body = (byte[]) data.get(ApiServiceInterface.BODY);
		final Api api = (Api) data.get(ApiServiceInterface.API);

		@SuppressWarnings("unchecked")
		final Map<String, String[]> queryParamsOrig = (Map<String, String[]>) data
				.get(ApiServiceInterface.QUERY_PARAMS);
		// we did not check whether the original query had params, even if not expected
		final Map<String, String[]> formDataParams = (Map<String, String[]>) data
				.get(ApiServiceInterface.FORM_PARAMETER_MAP);

		final Map<String, String[]> queryParams = new HashMap<>();
		boolean pathParamsDefined = false;

		for (final Entry<String, String[]> entry : queryParamsOrig.entrySet()) {
			final String queryParamName = entry.getKey();
			if (formDataParams != null && formDataParams.get(queryParamName) == null) {
				pathParamsDefined = true;
			}
			queryParams.put(entry.getKey(), entry.getValue());
		}

		final ApiOperation operation = apiManagerService.getFlowEngineApiOperation(pathInfo, api, method, queryParams);

		final HttpServletRequest request = (HttpServletRequest) data.get(ApiServiceInterface.REQUEST);

		final String nodeId = operation.getEndpoint().substring(0, operation.getEndpoint().indexOf('/', 1));
		String url = api.getEndpointExt() + nodeId + pathOperation;
		if (pathParamsDefined) {
			url = addExtraQueryParameters(url, pathInfo, queryParams);

		}

		ResponseEntity<byte[]> result = null;

		final HttpHeaders headers = new HttpHeaders();
		// add the headers
		addHeaders(headers, request);
		// add NodeRED auth (HTTP IN middleware)
		final String[] endpointParts = api.getEndpointExt().split("/");
		final String domain = endpointParts[endpointParts.length - 1];
		final String user = domainService.getFlowDomainByIdentification(domain).getUser().getUserId();
		headers.set("X-OP-NODEKey", noderedAuthService.getNoderedAuthAccessToken(user, domain));

		final HttpEntity<?> entity;

		if (ServletFileUpload.isMultipartContent(request)) {

			entity = new HttpEntity<>(addParameters(request, data), headers);
		} else {
			url = addExtraQueryParameters(url, pathInfo, queryParams);
			entity = new HttpEntity<>(body, headers);
		}

		try {

			switch (method) {

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
			case "GET":
				result = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
				break;
			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			data.put(ApiServiceInterface.STATUS, ChainProcessingStatus.STOP);

			data.put(ApiServiceInterface.HTTP_RESPONSE_CODE, e.getStatusCode());

			data.put(ApiServiceInterface.REASON, e.getResponseBodyAsString());

			throw e;
		}

		data.put(ApiServiceInterface.HTTP_RESPONSE_CODE, result.getStatusCode());
		data.put(Constants.HTTP_RESPONSE_HEADERS, result.getHeaders());
		data.put(ApiServiceInterface.OUTPUT, result.getBody());
		return data;
	}

	private Map<String, Object> postProcess(Map<String, Object> data) {
		final Api api = (Api) data.get(ApiServiceInterface.API);
		final String method = (String) data.get(ApiServiceInterface.METHOD);
		if (apiManagerServiceConfig.postProcess(api) && method.equalsIgnoreCase("get")) {
			final String postProcess = apiManagerServiceConfig.getPostProccess(api);
			if (StringUtils.hasText(postProcess)) {
				try {
					final Object result = scriptEngine.invokeScript(postProcess, data.get(ApiServiceInterface.OUTPUT));
					data.put(ApiServiceInterface.OUTPUT, result);
				} catch (final ScriptException e) {
					log.error("Execution logic for postprocess error", e);
					data.put(ApiServiceInterface.STATUS, ChainProcessingStatus.STOP);
					data.put(ApiServiceInterface.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR from Scripting Post Process", "Execution logic for Postprocess error",
							e.getCause().getMessage());
					data.put(ApiServiceInterface.REASON, messageError);

				} catch (final Exception e) {
					data.put(ApiServiceInterface.STATUS, ChainProcessingStatus.STOP);
					data.put(ApiServiceInterface.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR from Scripting Post Process", "Exception detected", e.getCause().getMessage());
					data.put(ApiServiceInterface.REASON, messageError);
				}
			}
		}

		return data;
	}

	private String getUrl(Swagger swagger, String pathInfo) {
		String scheme = ApiServiceInterface.HTTPS.toLowerCase();
		if (!swagger.getSchemes().stream().map(s -> s.name()).collect(Collectors.toList())
				.contains(ApiServiceInterface.HTTPS)) {
			scheme = ApiServiceInterface.HTTP.toLowerCase();
		}
		final String url = scheme + "://" + swagger.getHost() + swagger.getBasePath();
		final String apiIdentifier = apiManagerService.getApiIdentifier(pathInfo);
		final String swaggerPath = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length(),
				pathInfo.length() - 1);
		return url.concat(swaggerPath);
	}

	private String addExtraQueryParameters(String url, String pathInfo, Map<String, String[]> queryParams) {

		if (url.substring(url.length() - 1, url.length()).equals("/")) {
			url = url.substring(0, url.length() - 1);
		}

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

	private HttpHeaders addHeaders(HttpHeaders headers, HttpServletRequest request) {
		final Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			final String headerName = headerNames.nextElement();
			final String headerValue = request.getHeader(headerName);
			headers.add(headerName, headerValue);
		}
		final String contentType = request.getContentType();
		if (contentType == null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
		} else {
			headers.setContentType(MediaType.valueOf(contentType));
		}
		return headers;
	}

	@SuppressWarnings("unchecked")
	private MultiValueMap<String, Object> addParameters(HttpServletRequest request, Map<String, Object> data) {
		return (MultiValueMap<String, Object>) data.get(ApiServiceInterface.FORM_PARAMETER_MAP);
	}

}

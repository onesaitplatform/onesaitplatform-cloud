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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl.apis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.services.exceptions.FlowDomainServiceException;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NodeREDAPIInvokerInputFile;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiInvocationParams;
import com.minsait.onesait.platform.flowengine.exception.NoValueForParamIvocationException;

import io.swagger.models.Operation;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiInvokerUtils {
	private static final String API_KEY= "X-OP-APIKey";
	@Value("${onesaitplatform.flowengine.apiinvoker.response.utf8.force:false}")
	private boolean forceUtf8Response;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	void setUTF8Encoding() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}


	@SuppressWarnings("unchecked")
	public ResponseEntity<String> callApiOperation(RestApiInvocationParams invocationParams) {
		ResponseEntity<String> result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		HttpEntity<?> entity = new HttpEntity<>(invocationParams.getBody(), invocationParams.getHeaders());
		if (invocationParams.isMultipart()) {
			entity = new HttpEntity<>(invocationParams.getMultipartData(), invocationParams.getHeaders());
		}
		// Add query params
		String url = addExtraQueryParameters(invocationParams.getUrl(), invocationParams.getQueryParams());
		// Add path params
		for (final Entry<String, String> entry : invocationParams.getPathParams().entrySet()) {
			url = url.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue());
		}

		try {
			switch (invocationParams.getMethod()) {
			case GET:
				result = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
				break;
			case POST:
				result = restTemplate.postForEntity(url, entity, String.class);
				break;
			case PUT:
				result = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, entity, String.class);
				break;
			case DELETE:
				result = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, entity, String.class);
				break;
			default:
				break;

			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		} finally {
			if (invocationParams.isMultipart()) {
				((MultiValueMap<String, Object>) entity.getBody()).entrySet().forEach(e -> {
					if (e.getValue() != null) {
						final List<Object> params = e.getValue();
						if (!params.isEmpty() && params.get(0) instanceof FileSystemResource) {
							((FileSystemResource) e.getValue().get(0)).getFile().delete();
						}
					}

				});
			}
		}
		return result;
	}

	public Map<String, String> getDefaultStatusCodes() {
		final Map<String, String> statusCodes = new HashMap<>();
		statusCodes.put("200", "OK");
		statusCodes.put("204", "No Content");
		statusCodes.put("400", "Bad Request");
		statusCodes.put("401", "Unauthorized");
		statusCodes.put("501", "Internal Server Error");
		statusCodes.put("???", "Other status code");
		return statusCodes;
	}

	public String getValueForParam(String paramName, List<Map<String, String>> paramValues) {
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

	public void fillSwaggerInvocationParams(Operation operation, FlowEngineInvokeRestApiOperationRequest invokeRequest,
			RestApiInvocationParams resultInvocationParams) {
		final List<String> consumes = operation.getConsumes();
		if (consumes != null && !consumes.isEmpty()) {
			resultInvocationParams.getHeaders().add(HttpHeaders.CONTENT_TYPE, consumes.get(0));
		}
		//Check if we have the api token key on the invication params
		try {
			String apiTokenKey = getValueForParam(API_KEY, invokeRequest.getOperationInputParams());
			if (!apiTokenKey.isEmpty()) {
				resultInvocationParams.getHeaders().add(API_KEY, apiTokenKey);
			}
		}catch(final FlowDomainServiceException e) {
			log.debug("No api key used");
		}

		for (final Parameter param : operation.getParameters()) {
			// QUERY, PATH, BODY (formData ignore) or HEADER
			String value = "";
			boolean skipParam = false;
			try {
				value = getValueForParam(param.getName(), invokeRequest.getOperationInputParams());
				if(value.isEmpty() && param.getName().equalsIgnoreCase("Authorization")) {
					//if empty we do not use it
					skipParam = true;
				}
			} catch (final FlowDomainServiceException e) {
				final String msg = "No value was found for parameter " + param.getName() + " in operation ["
						+ invokeRequest.getOperationMethod() + "] - " + invokeRequest.getOperationName() + " from API ["
						+ invokeRequest.getApiVersion() + "] - " + invokeRequest.getApiName() + ".";
				if (param.getRequired()) {
					log.error(msg);
					throw new NoValueForParamIvocationException(msg);
				} else {
					log.debug("Skipping parameter. Optional parameter not received. " + msg);
					skipParam = true;
				}

			}
			if (!skipParam) {
				switch (param.getIn().toUpperCase()) {
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
				case "FORMDATA":
					resultInvocationParams.setMultipart(true);
					final FormParameter formParam = (FormParameter) param;
					if (formParam.getType().equalsIgnoreCase("file")) {

						try {
							// transform JSON (NodeJS) buffer to Bytes array
							final ObjectMapper mapper = new ObjectMapper();
							final NodeREDAPIInvokerInputFile nodeFile = mapper.readValue(value,
									NodeREDAPIInvokerInputFile.class);
							final File file = new File("/tmp/" + nodeFile.getFileName());
							Files.write(file.toPath(), nodeFile.getFile().getData());
							resultInvocationParams.getMultipartData().add(param.getName(),
									new FileSystemResource(file));
						} catch (final IOException e1) {
							log.error("Could not create temp file for multipart request");
						}

					} else {
						resultInvocationParams.getMultipartData().add(param.getName(), value);
					}
					break;
				default:
					break;
				}
			}
		}

	}

	private String addExtraQueryParameters(String url, Map<String, String> queryParams) {
		final StringBuilder sb = new StringBuilder(url);

		if (queryParams.size() > 0) {
			sb.append("?");
			queryParams.entrySet().forEach(e -> {
				final String param = e.getKey() + "=" + String.join("", e.getValue());
				sb.append(param).append("&");
			});
		}
		String urlWithQueryParams = sb.toString();
		if (urlWithQueryParams.endsWith("&")) {
			urlWithQueryParams = sb.deleteCharAt(sb.length() - 1).toString();
		}
		return urlWithQueryParams;
	}

}

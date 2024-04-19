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
package com.minsait.onesait.platform.controlpanel.service.serverless;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.controlpanel.controller.serverless.ApplicationCreate;
import com.minsait.onesait.platform.controlpanel.controller.serverless.ApplicationInfo;
import com.minsait.onesait.platform.controlpanel.controller.serverless.ApplicationUpdate;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionCreate;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionInfo;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionUpdate;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServerlessServiceImpl implements ServerlessService {

	private static final String BEARER = "bearer ";
	private static final String API_APPLICATIONS = "/api/applications";
	private static final String API_APPLICATIONS_BY_USER = "/api/applications/users/";
	private static final String API_APPLICATIONS_SELF = "/api/applications/users/self";
	private static final String API_FUNCTIONS = "/functions";
	@Autowired
	private IntegrationResourcesService integrationResourcesService;
	@Autowired
	private AppWebUtils utils;
	private String serverlessBaseURL;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	public void setUp() {
		restTemplate.getInterceptors().add((request, body, execution) -> {
			String token = utils.getCurrentUserOauthToken();
			if (token.toLowerCase().startsWith(BEARER)) {
				token = token.substring(BEARER.length());
			}
			request.getHeaders().add(HttpHeaders.AUTHORIZATION, BEARER + token);
			return execution.execute(request, body);
		});
		serverlessBaseURL = integrationResourcesService.getUrl(Module.SERVERLESS, ServiceUrl.BASE);

	}

	@Override
	public Collection<ApplicationInfo> getApplications() {
		final String url = utils.isAdministrator() ? serverlessBaseURL + API_APPLICATIONS
				: serverlessBaseURL + API_APPLICATIONS_SELF;
		return executeRequest(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<ApplicationInfo>>() {
		}).getBody();

	}

	@Override
	public ApplicationInfo getApplication(String name) {
		try {
			return executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/" + URLEncoder.encode(name, StandardCharsets.UTF_8.name()),
					HttpMethod.GET, null, ApplicationInfo.class).getBody();
		} catch (final Exception e) {
			log.error("Error encoding app name {}", name);
			return new ApplicationInfo();
		}
	}

	private <T> ResponseEntity<T> executeRequest(String url, HttpMethod method, HttpEntity<?> reqEntity,
			Class<T> responseType) {
		try {
			return restTemplate.exchange(url, method, reqEntity, responseType);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("HttpResponse code :{} , cause: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw e;
		}

	}

	private <T> ResponseEntity<List<T>> executeRequest(String url, HttpMethod method, HttpEntity<Object> requestEntity,
			ParameterizedTypeReference<List<T>> parameterizedTypeReference) {
		try {
			return restTemplate.exchange(url, method, requestEntity, parameterizedTypeReference);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("HttpResponse code :{} , cause: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new RuntimeException("Error code " + e.getRawStatusCode() + " with message " + e.getMessage());
		}

	}

	@Override
	public void deployFunction(String appName, String fnName) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()) + "/deploy",
					HttpMethod.POST, null, FunctionInfo.class);
		} catch (final Exception e) {
			log.error("Error deploying function with name {} on app {}", fnName, appName);
		}
	}

	@Override
	public FunctionInfo getFunction(String appName, String fnName) {
		try {
			return executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()),
					HttpMethod.GET, null, FunctionInfo.class).getBody();
		} catch (final Exception e) {
			log.error("Error deploying function with name {} on app {}", fnName, appName);
			return new FunctionInfo();
		}
	}

	@Override
	public void deleteFunction(String appName, String fnName) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()),
					HttpMethod.DELETE, null, String.class);
		} catch (final Exception e) {
			log.error("Error deleting function with name {} on app {}", fnName, appName);
		}

	}

	@Override
	public void updateFunction(String appName, String fnName, FunctionUpdate functionUpdate) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()),
					HttpMethod.PUT, new HttpEntity<>(functionUpdate), FunctionInfo.class);
		} catch (final Exception e) {
			log.error("Error updating function with name {} on app {}", fnName, appName);
		}
	}

	@Override
	public void createFunction(String appName, String fnName, FunctionCreate functionCreate) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS,
					HttpMethod.POST, new HttpEntity<>(functionCreate), FunctionInfo.class);
		} catch (final Exception e) {
			log.error("Error creating function with name {} on app {}", fnName, appName);
		}
	}

	@Override
	public void createApplication(ApplicationCreate appCreate) {
		executeRequest(serverlessBaseURL + API_APPLICATIONS, HttpMethod.POST, new HttpEntity<>(appCreate),
				FunctionInfo.class).getBody();
	}

	@Override
	public void updateApplication(ApplicationUpdate appUpdate) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appUpdate.getName(), StandardCharsets.UTF_8.name()),
					HttpMethod.PUT, new HttpEntity<>(appUpdate), FunctionInfo.class);
		} catch (final Exception e) {
			log.error("Error updating application with name {} ", appUpdate.getName());
		}

	}

	@Override
	public void deleteApplication(String name) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/" + URLEncoder.encode(name, StandardCharsets.UTF_8.name()),
					HttpMethod.DELETE, null, String.class);
		} catch (final Exception e) {
			log.error("Error deleting application with name {} ", name);
		}

	}

	@Override
	public void updateFunction(String appName, String fnName, String version) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()) + "/update-version",
					HttpMethod.PUT, new HttpEntity<>(version), Void.class);
		} catch (final Exception e) {
			log.error("Error updating function: app {} , fn {} , version {} ", appName, fnName, version);
		}

	}

	@Override
	public ObjectNode getEnvironment(String appName, String fnName) {
		try {
			final ResponseEntity<ObjectNode> response = executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()) + "/environment",
					HttpMethod.GET, null, ObjectNode.class);
			return response.getBody();
		} catch (final Exception e) {
			log.error("Error getting function environment: app {} , fn {} , version {} ", appName, fnName);
			return new ObjectMapper().createObjectNode();
		}
	}

	@Override
	public void updateFunctionEnvironment(String appName, String fnName, ObjectNode config) {
		try {
			executeRequest(
					serverlessBaseURL + API_APPLICATIONS + "/"
							+ URLEncoder.encode(appName, StandardCharsets.UTF_8.name()) + API_FUNCTIONS + "/"
							+ URLEncoder.encode(fnName, StandardCharsets.UTF_8.name()) + "/environment",
					HttpMethod.PUT, new HttpEntity<>(config), Void.class);
		} catch (final Exception e) {
			log.error("Error updating function's environment: app {} , fn {} , version {} ", appName, fnName);
		}

	}
}

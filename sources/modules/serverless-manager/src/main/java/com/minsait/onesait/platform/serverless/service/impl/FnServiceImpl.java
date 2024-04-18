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
package com.minsait.onesait.platform.serverless.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.serverless.dto.ApplicationUpdate;
import com.minsait.onesait.platform.serverless.dto.fn.FnApplication;
import com.minsait.onesait.platform.serverless.dto.fn.FnFunction;
import com.minsait.onesait.platform.serverless.dto.fn.FnFunctionWrapper;
import com.minsait.onesait.platform.serverless.dto.fn.FnTriggerWrapper;
import com.minsait.onesait.platform.serverless.exception.FnException;
import com.minsait.onesait.platform.serverless.exception.FnException.Code;
import com.minsait.onesait.platform.serverless.model.Application;
import com.minsait.onesait.platform.serverless.model.Function;
import com.minsait.onesait.platform.serverless.service.FnService;
import com.minsait.onesait.platform.serverless.utils.SSLUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FnServiceImpl implements FnService {

	private static final String EXECUTED_COMMAND_WITH_RESULT = "Executed command {} with result {}";
	private static final String APPS_REST = "/v2/apps";
	private static final String FNS_REST = "/v2/fns";
	private static final String TRIGGERS_REST = "/v2/triggers";
	private static final String FN_CMD = "fn";
	private static final String VERBOSE = "--verbose";
	private static final String COULD_NOT_EXECUTE_COMMAND = "Could not execute command ";
	private static final String LINE_SEPARATOR = "line.separator";
	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
	private static final Pattern DEPLOY_PATTERN = Pattern.compile("Deploying (.*) to app");
	@Value("${onesaitplatform.gateway.secret}")
	private String secret;
	@Value("${onesaitplatform.gateway.user}")
	private String username;
	@Value("${onesaitplatform.serverless.url}")
	private String baseURL;

	@Autowired
	private ObjectMapper mapper;

	@PostConstruct
	public void setRestClientInterceptors() {
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add(HttpHeaders.AUTHORIZATION,
					"Basic " + Base64.getEncoder().encodeToString((username + ":" + secret).getBytes()));
			return execution.execute(request, body);
		});
	}

	@Override
	public boolean create(String appName) {
		if (appExists(appName)) {
			throw new FnException("App with name " + appName + " already exists.", Code.CONFLICT);
		}

		final ProcessBuilder pb = new ProcessBuilder(FN_CMD, VERBOSE, "create", "app", appName);
		pb.redirectErrorStream(true);
		try {
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			if (builder.toString().toLowerCase().contains("error")) {
				log.error(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());
				return false;
			} else {
				log.debug(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());
			}

		} catch (final Exception e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command(), e);
			return false;
		}

		return true;
	}

	@Override
	public void update(ApplicationUpdate application, String appId) {
		try {
			if (application.getEnvironment() != null) {
				final FnApplication fnApp = getApp(appId);
				fnApp.getConfig().clear();
				application.getEnvironment().entrySet().forEach(e -> {
					fnApp.getConfig().put(e.getKey(), e.getValue());
				});
				final HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
				final ResponseEntity<FnFunction> response = restTemplate.exchange(baseURL + APPS_REST + "/" + appId,
						HttpMethod.PUT, new HttpEntity<>(fnApp, headers), FnFunction.class);
				if (log.isDebugEnabled()) {
					log.debug("Updated app {} with result body: {}", appId, mapper.writeValueAsString(response.getBody()));
				}				
			}
		} catch (final HttpClientErrorException e) {
			log.error("Error while updating application for appId {}, errorCode {} , message {}", appId,
					e.getRawStatusCode(), e.getResponseBodyAsString());
		} catch (final JsonProcessingException e1) {
			log.error("Error while serializing response", e1);
		}

	}

	@Override
	public boolean delete(String appName) {
		final ProcessBuilder pb = new ProcessBuilder(FN_CMD, VERBOSE, "delete", "app", appName);
		pb.redirectErrorStream(true);
		try {
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			if (builder.toString().toLowerCase().contains("error")) {
				log.error(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());
				return false;
			} else {
				log.debug(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());
			}

		} catch (final Exception e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command(), e);
			return false;
		}

		return true;
	}

	private boolean appExists(String appName) {
		final ResponseEntity<JsonNode> response = restTemplate.exchange(baseURL + APPS_REST, HttpMethod.GET, null,
				JsonNode.class);
		final ArrayNode items = (ArrayNode) response.getBody().get("items");
		if (items.size() > 0) {
			boolean found = false;
			final Iterator<JsonNode> it = items.elements();
			while (it.hasNext()) {
				final JsonNode n = it.next();
				if (n.get("name").asText().equals(appName)) {
					found = true;
				}
			}
			return found;
		}
		return false;
	}

	@Override
	public String getAppId(String appName) {
		final ResponseEntity<JsonNode> response = restTemplate.exchange(baseURL + APPS_REST, HttpMethod.GET, null,
				JsonNode.class);
		final ArrayNode items = (ArrayNode) response.getBody().get("items");
		String id = null;
		if (items.size() > 0) {
			final Iterator<JsonNode> it = items.elements();
			while (it.hasNext()) {
				final JsonNode n = it.next();
				if (n.get("name").asText().equals(appName)) {
					id = n.get("id").asText();
				}
			}
		}
		return id;
	}

	@Override
	public FnApplication getApp(String appId) {
		try {
			final ResponseEntity<FnApplication> response = restTemplate.exchange(baseURL + APPS_REST + "/" + appId,
					HttpMethod.GET, null, FnApplication.class);
			return response.getBody();
		} catch (final HttpClientErrorException e) {
			log.error("Error while getting app with id {}, errorCode {} , message {}", appId, e.getRawStatusCode(),
					e.getResponseBodyAsString());
		}
		return null;
	}

	@Override
	public FnFunction getFunction(String fnId) {
		try {
			final ResponseEntity<FnFunction> response = restTemplate.exchange(baseURL + FNS_REST + "/" + fnId,
					HttpMethod.GET, null, FnFunction.class);
			final FnFunction fn = response.getBody();
			final FnTriggerWrapper triggers = getTriggers(fnId, fn.getAppId());
			if (triggers != null) {
				fn.setTriggers(triggers.getTriggers());
			}
			return fn;
		} catch (final HttpClientErrorException e) {
			log.error("Error while getting function with id {}, errorCode {} , message {}", fnId, e.getRawStatusCode(),
					e.getResponseBodyAsString());
		}
		return null;
	}

	private FnTriggerWrapper getTriggers(String fnId, String appId) {
		try {
			final ResponseEntity<FnTriggerWrapper> responseTriggers = restTemplate.exchange(
					baseURL + TRIGGERS_REST + "?fn_id=" + fnId + "&app_id=" + appId, HttpMethod.GET, null,
					FnTriggerWrapper.class);
			return responseTriggers.getBody();
		} catch (final HttpClientErrorException e) {
			log.error("Error while getting triggers for fn {}, errorCode {} , message {}", fnId, e.getRawStatusCode(),
					e.getResponseBodyAsString());
		}
		return null;
	}

	private FnFunctionWrapper getFunctions(String appId) {
		try {
			final ResponseEntity<FnFunctionWrapper> responseFunctions = restTemplate
					.exchange(baseURL + FNS_REST + "?app_id=" + appId, HttpMethod.GET, null, FnFunctionWrapper.class);
			return responseFunctions.getBody();
		} catch (final HttpClientErrorException e) {
			log.error("Error while getting functions for app {}, errorCode {} , message {}", appId,
					e.getRawStatusCode(), e.getResponseBodyAsString());
		}
		return null;
	}

	@Override
	public FnFunction deploy(Application app, Function function, String basePath) {
		if (log.isDebugEnabled()) {
			log.debug("Deploying app {}, function {}", app.getName(), function.getName());
		}		
		final StringBuilder builder = new StringBuilder();
		final ProcessBuilder pb = new ProcessBuilder(FN_CMD, VERBOSE, "deploy", "--app", app.getName());
		pb.redirectErrorStream(true);
		pb.directory(new File(basePath.endsWith("/") ? basePath + function.getPathToYaml()
				: basePath + "/" + function.getPathToYaml()));
		try {
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = null;
			p.waitFor(2, TimeUnit.MINUTES);
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}
			if (log.isDebugEnabled()) {
				log.debug("Result of deploy: {}", builder.toString());
			}			
			if (builder.toString().toLowerCase().contains("could not find function file")) {
				log.error(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());
				throw new FnException("Yaml file not found for function", Code.BAD_REQUEST);
			} else {
				log.debug(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());
			}

		} catch (final Exception e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command(), e);
			return null;
		}
		final Matcher matcher = DEPLOY_PATTERN.matcher(builder.toString());

		if (matcher.find()) {
			final String fnName = matcher.group(1);
			final FnFunctionWrapper wrapper = getFunctions(app.getAppId());
			if (wrapper != null) {
				final Optional<FnFunction> fnFunction = wrapper.getFunctions().stream()
						.filter(f -> f.getName().equals(fnName)).findFirst();
				if (fnFunction.isPresent()) {
					return fnFunction.get();
				}
			}
		}
		return null;
	}

	@Override
	public void deleteFunction(String fnId) {
		try {
			restTemplate.exchange(baseURL + FNS_REST + "/" + fnId, HttpMethod.DELETE, null, String.class);

		} catch (final HttpClientErrorException e) {
			log.error("Error while getting triggers for fn {}, errorCode {} , message {}", fnId, e.getRawStatusCode(),
					e.getResponseBodyAsString());
			if (!e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				throw new FnException("Error while deleting function", Code.INTERNAL_ERROR);
			}
		}

	}

	@Override
	public void updateFunction(FnFunction function) {
		try {
			restTemplate.exchange(baseURL + FNS_REST + "/" + function.getId(), HttpMethod.PUT,
					new HttpEntity<>(function), String.class);

		} catch (final HttpClientErrorException e) {
			log.error("Error while getting triggers for fn {}, errorCode {} , message {}", function.getId(),
					e.getRawStatusCode(), e.getResponseBodyAsString());
			if (!e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				throw new FnException("Error while deleting function", Code.INTERNAL_ERROR);
			}
		}

	}

	@Override
	public void removeVar(String appName, String fnName, String var) {
		final ProcessBuilder pb = new ProcessBuilder(FN_CMD, "delete", "cf", "function", appName, fnName, var);
		pb.redirectErrorStream(true);
		try {
			final Process p = pb.start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder builder = new StringBuilder();
			String line = null;
			p.waitFor();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty(LINE_SEPARATOR));
			}

			log.debug(EXECUTED_COMMAND_WITH_RESULT, pb.command(), builder.toString());

		} catch (final Exception e) {
			log.error(COULD_NOT_EXECUTE_COMMAND + pb.command(), e);

		}

	}

}

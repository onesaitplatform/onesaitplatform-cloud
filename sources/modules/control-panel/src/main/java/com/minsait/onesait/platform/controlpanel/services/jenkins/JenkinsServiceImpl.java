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
package com.minsait.onesait.platform.controlpanel.services.jenkins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.jclouds.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.common.Error;
import com.cdancy.jenkins.rest.domain.common.IntegerResponse;
import com.cdancy.jenkins.rest.domain.common.RequestStatus;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.cdancy.jenkins.rest.domain.queue.Executable;
import com.cdancy.jenkins.rest.domain.queue.QueueItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.restplanner.RestPlannerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JenkinsServiceImpl implements JenkinsService {

	private static final String VIEW = "view/";
	private static final String PROPERTY_PATH = "property";
	private static final String CLASS_PATH = "_class";
	private static final String PROPERTY_CLASS = "hudson.model.ParametersDefinitionProperty";
	private static final String PARAM_DEFINITION = "parameterDefinitions";
	private static final String JENKINS_CRUMB_FIELD = "Jenkins-Crumb";
	private static final String CRUMB_JSON_FIELD = "crumb";
	@Autowired
	private RestPlannerService restPlannerService;
	@Value("classpath:static/microservices/templates/jenkins/view.xml")
	private Resource viewXML;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	private void setUp() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	@Override
	public JobInfo getJobInfo(String jenkinsUrl, String username, String token, String jobName, String folderName) {
		return getJenkinsClient(jenkinsUrl, username, token).api().jobsApi().jobInfo(folderName, jobName);
	}

	@Override
	public void createJob(String jenkinsUrl, String username, String token, String jobName, String folderName,
			String jobConfigXML) {
		final RequestStatus status = getJenkinsClient(jenkinsUrl, username, token).api().jobsApi().create(folderName,
				jobName, jobConfigXML);
		if (!status.errors().isEmpty()) {
			throwException(status.errors());
		}
	}

	@Override
	public void createJobFolder(String jenkinsUrl, String username, String token, String folderName,
			String folderConfigXML) {
		final RequestStatus status = getJenkinsClient(jenkinsUrl, username, token).api().jobsApi().create(null,
				folderName, folderConfigXML);
		if (!status.errors().isEmpty()) {
			throwException(status.errors());
		}
	}

	@Override
	public void deleteJob(String jenkinsUrl, String username, String token, String jobName, String folderName) {
		final RequestStatus status = getJenkinsClient(jenkinsUrl, username, token).api().jobsApi().delete(folderName,
				jobName);
		if (!status.errors().isEmpty()) {
			throwException(status.errors());
		}
	}

	@Override
	public int buildWithParameters(String jenkinsUrl, String username, String token, String jobName, String folderName,
			Map<String, List<String>> parameters) {
		final IntegerResponse response = getJenkinsClient(jenkinsUrl, username, token).api().jobsApi()
				.buildWithParameters(folderName, jobName, parameters);
		if (response.value() == null) {
			throwException(response.errors());
		}
		return response.value();

	}

	@Override
	public int buildWithParametersNoAuth(String jenkinsUrl, String jobName, String folderName,
			Map<String, List<String>> parameters) throws Exception {
		final IntegerResponse response = getJenkinsClient(jenkinsUrl).api().jobsApi().buildWithParameters(folderName,
				jobName, parameters);
		if (response.value() == null) {
			throwException(response.errors());
		}
		return response.value();

	}

	@Override
	public Map<String, String> getParametersFromJob(String jenkinsUrl, String username, String token, String jobName) {
		final String url = jenkinsUrl.concat("/job/").concat(jobName).concat("/api/json");
		final JsonNode response = requestJenkins(url, username, token, JsonNode.class).getBody();
		return extractParameters(response);
	}

	private <T> ResponseEntity<T> requestJenkins(String url, String username, String token, Class<T> responseType) {
		final RestTemplate rt = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization",
				"Basic " + Base64.getEncoder().encodeToString(username.concat(":").concat(token).getBytes()));
		final HttpEntity<?> entity = new HttpEntity<>(headers);
		return rt.exchange(url, HttpMethod.GET, entity, responseType);

	}

	@Override
	public JenkinsClient getJenkinsClient(String jenkinsUrl) {
		return JenkinsClient.builder().endPoint(jenkinsUrl).overrides(overrideSSLProperties()).build();
	}

	@Override
	public JenkinsClient getJenkinsClient(String jenkinsUrl, String username, String token) {
		return JenkinsClient.builder().endPoint(jenkinsUrl).credentials(username.concat(":").concat(token))
				.overrides(overrideSSLProperties()).build();
	}

	private Properties overrideSSLProperties() {
		final Properties overrides = new Properties();
		overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
		overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
		return overrides;
	}

	private void throwException(List<Error> errors) {
		final StringBuilder sb = new StringBuilder();
		errors.stream().forEach(
				e -> sb.append(e.exceptionName().concat(" : ").concat(e.message().concat(System.lineSeparator()))));
		throw new JenkinsException(sb.toString());
	}

	private Map<String, String> extractParameters(JsonNode node) {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, String> parameters = new HashMap<>();
		final ArrayNode properties = (ArrayNode) node.get(PROPERTY_PATH);
		final ArrayNode params = mapper.createArrayNode();
		if (properties.size() > 0) {
			properties.forEach(n -> {
				if (n.get(CLASS_PATH).asText().equals(PROPERTY_CLASS)) {
					params.add(n.get(PARAM_DEFINITION));
				}
			});
			if (params.size() > 0) {
				params.get(0).forEach(n -> {
					parameters.put(n.get("name").asText(), n.at("/defaultParameterValue/value").asText());
				});
			}
		}
		return parameters;
	}

	@Override
	public BuildInfo buildInfo(JenkinsConfiguration config, String jobName, String folderName, int queueId) {
		final JenkinsClient client = getJenkinsClient(config.getJenkinsUrl(), config.getUsername(), config.getToken());

		final QueueItem queueItem = client.api().queueApi().queueItem(queueId);
		final Executable executable = queueItem.executable();
		if (executable != null) {
			return client.api().jobsApi().buildInfo(null, jobName, executable.number());
		}

		return null;
	}

	@Override
	public BuildInfo buildInfo(String jenkinsUrl, String jobName, String folderName, int queueId) {
		final JenkinsClient client = getJenkinsClient(jenkinsUrl);

		final QueueItem queueItem = client.api().queueApi().queueItem(queueId);
		final Executable executable = queueItem.executable();
		if (executable != null) {
			return client.api().jobsApi().buildInfo(null, jobName, executable.number());
		}

		return null;
	}

	@Override
	public BuildInfo lastBuildInfo(JenkinsConfiguration config, String jobName, String folderName) {
		return getJenkinsClient(config.getJenkinsUrl(), config.getUsername(), config.getToken()).api().jobsApi()
				.jobInfo(folderName, jobName).lastBuild();
	}

	@Override
	public boolean viewExists(String jenkinsUrl, String username, String token, String viewName) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((username + ":" + token).getBytes()));
		try {
			restTemplate.exchange(getJenkinsViewUrl(jenkinsUrl, viewName), HttpMethod.GET, new HttpEntity<>(headers),
					String.class);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				return false;
			} else {
				throw e;
			}
		}

		return true;
	}

	@Override
	public void createView(String jenkinsUrl, String username, String token, String viewName) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((username + ":" + token).getBytes()));
		headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");

		try {
			final String xml = IOUtils.toString(viewXML.getInputStream());
			restTemplate.exchange(
					getJenkinsCreateViewUrl(jenkinsUrl, viewName, getCrumbCSRF(jenkinsUrl, username, token)),
					HttpMethod.POST, new HttpEntity<>(xml, headers), String.class);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				log.error("Jenkins user is not allowed to create Views", e);
				throw new OPResourceServiceException("Jenkins user is not allowed to create Views");
			} else {
				throw e;
			}
		} catch (final Exception e) {
			log.error("Error while creating view", e);
			throw new OPResourceServiceException("Error while creating view", e);
		}

	}

	@Override
	public void createJobInView(String jenkinsUrl, String username, String token, String jobName, String viewName,
			String jobConfigXML) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((username + ":" + token).getBytes()));
		headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
		try {
			restTemplate.exchange(
					getJenkinsCreateJobInViewURL(jenkinsUrl, viewName, jobName,
							getCrumbCSRF(jenkinsUrl, username, token)),
					HttpMethod.POST, new HttpEntity<>(jobConfigXML, headers), String.class);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error while creating job in view: {}", e.getResponseBodyAsString(), e);
			throw new OPResourceServiceException("Error while creating job in view: " + e.getResponseBodyAsString());
		}

	}

	@Override
	public String getCrumbCSRF(String jenkinsUrl, String username, String token) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((username + ":" + token).getBytes()));
		final ResponseEntity<JsonNode> response = restTemplate.exchange(getJenkinsCrumbURL(jenkinsUrl), HttpMethod.GET,
				new HttpEntity<>(headers), JsonNode.class);
		return response.getBody().get(CRUMB_JSON_FIELD).asText();
	}

	@Override
	public void deleteView(String jenkinsUrl, String username, String token, String viewName) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((username + ":" + token).getBytes()));
		restTemplate.exchange(getJenkinsDeleteViewURL(jenkinsUrl, viewName, getCrumbCSRF(jenkinsUrl, username, token)),
				HttpMethod.POST, new HttpEntity<>(headers), String.class);

	}

	private String getJenkinsDeleteViewURL(String jenkinsUrl, String viewName, String crumb) {
		String finalUrl = jenkinsUrl;
		if (!jenkinsUrl.endsWith("/")) {
			finalUrl = finalUrl + "/";
		}
		try {
			finalUrl = finalUrl + VIEW + URLEncoder.encode(viewName, StandardCharsets.UTF_8.name()) + "/doDelete?"
					+ JENKINS_CRUMB_FIELD + "=" + crumb;
		} catch (final UnsupportedEncodingException e) {
			finalUrl = finalUrl + VIEW + viewName + "/doDelete?" + JENKINS_CRUMB_FIELD + "=" + crumb;
		}
		return finalUrl;
	}

	private String getJenkinsCreateJobInViewURL(String jenkinsUrl, String viewName, String jobName, String crumb) {
		String finalUrl = jenkinsUrl;
		if (!jenkinsUrl.endsWith("/")) {
			finalUrl = finalUrl + "/";
		}
		try {
			finalUrl = finalUrl + VIEW + URLEncoder.encode(viewName, StandardCharsets.UTF_8.name())
					+ "/createItem?name=" + URLEncoder.encode(jobName, StandardCharsets.UTF_8.name()) + "&"
					+ JENKINS_CRUMB_FIELD + "=" + crumb;
		} catch (final UnsupportedEncodingException e) {
			finalUrl = finalUrl + VIEW + viewName + "/createItem?name=" + jobName + "&" + JENKINS_CRUMB_FIELD + "="
					+ crumb;
		}
		return finalUrl;
	}

	private String getJenkinsViewUrl(String jenkinsUrl, String viewName) {
		String finalUrl = jenkinsUrl;
		if (!jenkinsUrl.endsWith("/")) {
			finalUrl = finalUrl + "/";
		}
		try {
			finalUrl = finalUrl + VIEW + URLEncoder.encode(viewName, StandardCharsets.UTF_8.name());
		} catch (final UnsupportedEncodingException e) {
			finalUrl = finalUrl + VIEW + viewName;
		}
		return finalUrl;

	}

	private String getJenkinsCreateViewUrl(String jenkinsUrl, String viewName, String crumb) {
		String finalUrl = jenkinsUrl;
		if (!jenkinsUrl.endsWith("/")) {
			finalUrl = finalUrl + "/";
		}
		try {
			finalUrl = finalUrl + "createView?name=" + URLEncoder.encode(viewName, StandardCharsets.UTF_8.name()) + "&"
					+ JENKINS_CRUMB_FIELD + "=" + crumb;
		} catch (final UnsupportedEncodingException e) {
			finalUrl = finalUrl + "createView?name=" + viewName + "&" + JENKINS_CRUMB_FIELD + "=" + crumb;
		}
		return finalUrl;

	}

	private String getJenkinsCrumbURL(String jenkinsUrl) {
		String finalUrl = jenkinsUrl;
		if (!jenkinsUrl.endsWith("/")) {
			finalUrl = finalUrl + "/";
		}
		return finalUrl + "crumbIssuer/api/json";
	}

}

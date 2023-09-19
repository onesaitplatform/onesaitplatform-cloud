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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.microservice.MicroserviceTemplateUtil;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;

public abstract class MicroservicesGitRestService {

	protected static final String GIT_CURRENT_USER = "/user";
	protected static final String EMAIL_STR = "email";
	protected static final String RESOURCE_PATH_SCAFFOLDING = "static/gitlab/scaffolding-sb-vue.zip";
	protected static final String RESOURCE_PATH_MICROSERVICE_IOT = "static/microservices/microservice.zip";
	protected static final String RESOURCE_PATH_MICROSERVICE_ML = "static/microservices/microservice-ml.zip";
	protected static final String RESOURCE_PATH_MICROSERVICE_NB = "static/microservices/microservice-nb.zip";
	protected static final String PUSH_BRANCH = "master";
	protected static final String GITHUB = "github";

	@Autowired
	protected UserService userService;
	@Autowired
	protected ConfigurationService configurationService;

	@Autowired
	protected MicroserviceTemplateUtil microserviceTemplateUtil;

	public abstract String getOauthToken(String url, String user, String password) throws GitlabException;

	public abstract JsonNode createProject(String url, String token, String name, int namespaceId,
			boolean isPrivateToken) throws GitlabException;

	public abstract void deleteGitlabProject(String projectName, String namespace, String url, String privateToken)
			throws GitlabException;

	public abstract Map<String, Integer> authorizeUsers(String url, String token, int projectId, List<String> users,
			boolean isPrivateToken) throws IOException, GitlabException, URISyntaxException;

	public abstract String createGitlabProject(String gitlabConfigId, String projectName, List<String> users,
			String url, boolean scaffolding) throws GitlabException;

	public abstract String createGitlabProject(Microservice microservice, boolean scaffolding, File file,
			MSConfig config, GitlabConfiguration cloneConfig) throws GitlabException;

	public abstract int createNamespace(String url, String name, String token, boolean isPrivateToken)
			throws GitlabException;

	abstract int getNamespace(String url, String name, String token, boolean isPrivateToken) throws GitlabException;

	abstract boolean namespaceExists(String url, String name, String token, boolean isPrivateToken)
			throws GitlabException;

	public abstract void deleteProject(String url, String token, int projectId, boolean isPrivateToken,
			String projectName) throws GitlabException;

	public abstract GitlabConfiguration getGitlabConfigurationFromPrivateToken(String url, String privateToken)
			throws GitlabException;

	abstract void deleteProjectGroup(String url, String token, int groupId, boolean isPrivateToken)
			throws GitlabException;

	abstract void unprotectBranch(String url, String token, int projectId, boolean isPrivateToken, String branch)
			throws GitlabException;

	protected void scafold(File file, Microservice microservice, MSConfig config, GitlabConfiguration gitConfig,
			JsonNode projectInfo, GitlabConfiguration cloneConfig) throws GitlabException {
		final GitlabConfiguration mainConfig = microservice.getGitlabConfiguration();
		if (file == null) {
//			if (microservice.getTemplateType().equals(TemplateType.ML_MODEL_ARCHETYPE.toString())) {
//				microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_MICROSERVICE_ML, false,
//						config.getSources(), config.getDocker());
//				microserviceTemplateUtil.generateScaffolding(projectInfo, gitConfig, microservice.getTemplateType(),
//						config.getOntology(), config.getNotebook(), microservice.getContextPath(), 0);
//			} else if (microservice.getTemplateType().equals(TemplateType.NOTEBOOK_ARCHETYPE.toString())) {
//				microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_MICROSERVICE_NB, false,
//						config.getSources(), config.getDocker());
//				microserviceTemplateUtil.generateScaffolding(projectInfo, gitConfig, microservice.getTemplateType(),
//						config.getOntology(), config.getNotebook(), microservice.getContextPath(), 0);
//			} else 
			if (microservice.getTemplateType().equals(TemplateType.IMPORT_FROM_GIT.toString())) {
				microserviceTemplateUtil.cloneAndPush(projectInfo, mainConfig, cloneConfig, true, config.getSources(),
						config.getDocker());
			} else {
				microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_MICROSERVICE_IOT, false,
						config.getSources(), config.getDocker());
				microserviceTemplateUtil.generateScaffolding(projectInfo, gitConfig, microservice.getTemplateType(),
						config.getOntology(), config.getNotebook(), microservice.getContextPath(), 0);
			}
		} else {
			microserviceTemplateUtil.createAndExtractFiles(file.getAbsolutePath(), true, config.getSources(),
					config.getDocker());
			microserviceTemplateUtil.generateScaffolding(projectInfo, gitConfig, microservice.getTemplateType(),
					config.getOntology(), config.getNotebook(), null, 0);
		}
	}

	protected ResponseEntity<JsonNode> sendHttp(String url, HttpMethod httpMethod, String body, String token,
			boolean isPrivateToken) throws URISyntaxException {

		RestTemplate restTemplate = null;

		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.hasText(token)) {
			if (!isPrivateToken) {
				headers.add("Authorization", "Bearer " + token);
			} else {
				headers.add("Private-Token", token);
			}
		}

		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		final ResponseEntity<JsonNode> response = restTemplate.exchange(new URI(url), httpMethod, request,
				JsonNode.class);

		final HttpHeaders responseHeaders = new HttpHeaders();
		final MediaType mType = response.getHeaders().getContentType();
		if (mType != null) {
			responseHeaders.set("Content-Type", mType.toString());
		}
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	public abstract boolean supports(GitlabConfiguration gitlabConfig);

}

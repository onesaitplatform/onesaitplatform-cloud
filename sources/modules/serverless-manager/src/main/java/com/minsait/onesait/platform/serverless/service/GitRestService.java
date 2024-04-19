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
package com.minsait.onesait.platform.serverless.service;

import java.io.File;
import java.net.URISyntaxException;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.serverless.dto.git.GitlabConfiguration;
import com.minsait.onesait.platform.serverless.utils.SSLUtil;

public abstract class GitRestService {

	protected static final String GITHUB = "github";
	protected static final String GIT_CURRENT_USER = "/user";
	protected static final String EMAIL_STR = "email";
	protected static final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	public abstract ResponseEntity<JsonNode> sendHttp(String url, HttpMethod httpMethod, String body, String token)
			throws URISyntaxException;

	public abstract GitlabConfiguration getGitlabConfigurationFromPrivateToken(String url, String privateToken);

	public abstract void deleteProject(String url, String token, int projectId, String projectName);

	public abstract JsonNode createProject(String url, String token, String name);

	public abstract String createGitlabProject(boolean scaffolding, File file, GitlabConfiguration gitlabConfig, String projectName);

	public abstract boolean supports(String gitURL);
}

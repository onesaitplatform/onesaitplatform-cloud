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
package com.minsait.onesait.platform.serverless.service.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.serverless.dto.git.GitlabConfiguration;
import com.minsait.onesait.platform.serverless.exception.GitException;
import com.minsait.onesait.platform.serverless.service.GitRestService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitlabRestService extends GitRestService {

	private static final String GITLAB_API_PATH = "/api/v4";
	private static final String GITLAB_PROJECTS = "/projects";
	private static final String GITLAB_GROUPS = "/groups";
	private static final String USERNAME_STR = "username";

	@Override
	public GitlabConfiguration getGitlabConfigurationFromPrivateToken(String url, String privateToken) {
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GIT_CURRENT_USER),
					HttpMethod.GET, null, privateToken);
			return GitlabConfiguration.builder().site(url).email(response.getBody().get(EMAIL_STR).asText())
					.user(response.getBody().get(USERNAME_STR).asText()).privateToken(privateToken).build();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not retrieve user info from private token {}", e.getResponseBodyAsString());
			throw new GitException("Could not retrieve user info from private token" + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException("Could not retrieve user info from private token" + e.getMessage());
		}
	}

	@Override
	public void deleteProject(String url, String token, int projectId, String projectName) {
		try {
			sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS).concat("/").concat(String.valueOf(projectId)),
					HttpMethod.DELETE, "", token);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException("Could not delete project" + e.getMessage());
		}

	}

	@Override
	public JsonNode createProject(String url, String token, String name) {
		return createProject(url, token, name, -1);
	}

	@Override
	public String createGitlabProject(boolean scaffolding, File file, GitlabConfiguration gitlabConfig,
			String projectName) {
		int projectId = 0;
		boolean projectCreated = false;
		boolean namespaceCreated = false;
		int namespaceId = 0;
		String webUrl = gitlabConfig.getSite();
		try {

			// First generate gitlab config for convenience
			final GitlabConfiguration config = getGitlabConfigurationFromPrivateToken(gitlabConfig.getSite(),
					gitlabConfig.getPrivateToken());
			if (!StringUtils.isEmpty(gitlabConfig.getGitlabGroup())) {
				if (namespaceExists(config.getSite(), gitlabConfig.getGitlabGroup(), config.getPrivateToken())) {
					namespaceId = getNamespace(config.getSite(), gitlabConfig.getGitlabGroup(), config.getPrivateToken());
				} else {
					namespaceId = createNamespace(config.getSite(), gitlabConfig.getGitlabGroup(), config.getPrivateToken());
					namespaceCreated = true;
				}
			} else {
				namespaceId = createNamespace(config.getSite(), projectName, config.getPrivateToken());
				namespaceCreated = true;
			}

			final JsonNode projectInfo = createProject(config.getSite(), config.getPrivateToken(), projectName,
					namespaceId);
			projectId = projectInfo.get("id").asInt();
			webUrl = projectInfo.get("web_url").asText();
			projectCreated = true;
			unprotectBranch(config.getSite(), config.getPrivateToken(), projectId, config.getBranch());

		} catch (final Exception e) {
			log.error("Could not create Gitlab project {}", e.getMessage());
			if (projectCreated) {
				log.error(
						"Project was created in gitlab but something went wrong, rolling back and destroying project {}",
						projectName);
				deleteProject(gitlabConfig.getSite(), gitlabConfig.getPrivateToken(), projectId, null);
				if (namespaceCreated) {
					deleteProjectGroup(gitlabConfig.getSite(), gitlabConfig.getPrivateToken(), namespaceId);
				}

			}
			throw e;
		}

		return webUrl;
	}

	private void unprotectBranch(String url, String token, int projectId, String branch) throws GitException {
		try {
			sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS) + "/" + projectId + "/protected_branches/"
					+ branch, HttpMethod.DELETE, null, token);
		} catch (final Exception e) {
			// NO-OP
		}
	}

	private boolean namespaceExists(String url, String name, String token) throws GitException {
		return getNamespace(url, name, token) > 0;
	}

	private int getNamespace(String url, String name, String token) throws GitException {
		int namespaceId = 0;
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS) + "/"
					+ URLEncoder.encode(name, StandardCharsets.UTF_8.name()), HttpMethod.GET, null, token);
			namespaceId = response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				log.warn("Gitlab Group {} not found, will create it", name);
				return -1;
			}
			log.error("Could not get Gitlab Group {}: {}", name, e.getResponseBodyAsString());
			throw new GitException("Could not get Gitlab Group " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException("Could not get Gitlab Group " + e.getMessage());
		}
		return namespaceId;
	}

	private int createNamespace(String url, String name, String token) throws GitException {
		String body = "{\"name\":\"" + name + "\",\"path\":\"" + name.toLowerCase().replace(" ", "-")
				+ "\", \"visibility\":\"private\"}";
		int namespaceId = 0;
		if (name.contains("/")) {
			if (log.isDebugEnabled()) {
				log.debug("parsing subgroups for {}", name);
			}			
			final String[] subgroups = name.split("/");
			final String directParentGroup = name.substring(0,
					name.length() - subgroups[subgroups.length - 1].length() - 1);
			final int parentId = getNamespace(url, directParentGroup, token);
			if (parentId > 0) {
				body = "{\"name\":\"" + subgroups[subgroups.length - 1] + "\",\"path\":\""
						+ subgroups[subgroups.length - 1].toLowerCase().replace(" ", "-")
						+ "\", \"visibility\":\"private\", \"parent_id\":" + parentId + "}";
			} else {
				throw new GitException("Parent group " + directParentGroup + " does not exist in this GitLab.");
			}
		}
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS),
					HttpMethod.POST, body, token);
			namespaceId = response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				log.warn("Not allowed to create groups in this Gitlab repository, returning self user namespace");
				return -1;
			}
			log.error("Could not create namespace for project {}", e.getResponseBodyAsString());
			throw new GitException("Could not create namespace for project" + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException("Could not create namespace for project" + e.getMessage());
		}
		return namespaceId;
	}

	private JsonNode createProject(String url, String token, String name, int namespaceId) throws GitException {
		String body = "{\"name\":\"" + name + "\",\"visibility\":\"private\", \"namespace_id\":" + namespaceId + "}";
		if (namespaceId == -1) {
			body = "{\"name\":\"" + name + "\",\"visibility\":\"private\"}";
		}
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS),
					HttpMethod.POST, body, token);
			return response.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create project {}", e.getResponseBodyAsString());
			throw new GitException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException(e.getMessage());
		}

	}

	private void deleteProjectGroup(String url, String token, int groupId) throws GitException {
		try {
			if (groupId != -1) {
				sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS).concat("/").concat(String.valueOf(groupId)),
						HttpMethod.DELETE, "", token);
			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException("Could not delete project" + e.getMessage());
		}
	}

	@Override
	public boolean supports(String gitURL) {
		return !gitURL.toLowerCase().contains(GITHUB);
	}

	@Override
	public ResponseEntity<JsonNode> sendHttp(String url, HttpMethod httpMethod, String body, String token)
			throws URISyntaxException {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (!StringUtils.isEmpty(token)) {

			headers.add("Private-Token", token);

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

}

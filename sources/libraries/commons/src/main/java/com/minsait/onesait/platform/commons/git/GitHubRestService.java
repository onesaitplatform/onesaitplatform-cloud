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
package com.minsait.onesait.platform.commons.git;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitHubRestService extends GitRestService {
	private static final String GITHUB_BASE_URL = "https://api.github.com";
	private static final String USERNAME_STR = "login";
	private static final String USER_REPOS = "/user/repos";
	private static final String REPOS = "/repos/%s/%s";
	private static final String REPO_BASE = "/repos";
	private static final String COMMITS = "/commits";
	private static final String CONTENTS = "/contents";

	@Override
	public GitlabConfiguration getGitlabConfigurationFromPrivateToken(String url, String privateToken) {
		try {
			final ResponseEntity<JsonNode> response = sendHttp(GITHUB_BASE_URL.concat(GIT_CURRENT_USER), HttpMethod.GET,
					null, privateToken);
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
		final GitlabConfiguration gitConfig = getGitlabConfigurationFromPrivateToken(GITHUB_BASE_URL, token);
		try {
			sendHttp(GITHUB_BASE_URL + String.format(REPOS, gitConfig.getUser(), projectName), HttpMethod.DELETE, null,
					token);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException(e.getMessage());
		}

	}

	@Override
	public JsonNode createProject(String url, String token, String name) {
		final String body = "{\"name\":\"" + name + "\",\"visibility\":\"private\",\"private\":\"true\"}";

		try {
			final ResponseEntity<JsonNode> response = sendHttp(GITHUB_BASE_URL + USER_REPOS, HttpMethod.POST, body,
					token);
			return response.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create project {}", e.getResponseBodyAsString());
			throw new GitException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitException(e.getMessage());
		}
	}

	@Override
	public String createGitlabProject(boolean scaffolding, File file, GitlabConfiguration gitlabConfig,
			String projectName) {
		int projectId = 0;
		boolean projectCreated = false;
		String webUrl = gitlabConfig.getSite();

		try {
			// GitConfig for username
			final GitlabConfiguration gitConfig = getGitlabConfigurationFromPrivateToken(GITHUB_BASE_URL,
					gitlabConfig.getPrivateToken());
			gitConfig.setSite(gitlabConfig.getSite());
			final JsonNode projectInfo = createProject(GITHUB_BASE_URL, gitlabConfig.getPrivateToken(), projectName);

			projectId = projectInfo.get("id").asInt();
			webUrl = projectInfo.get("html_url").asText();
			projectCreated = true;

		} catch (final Exception e) {
			log.error("Could not create Gitlab project {}", e.getMessage());
			if (projectCreated) {
				log.error(
						"Project was created in GitHub but something went wrong, rolling back and destroying repository {}",
						projectName);
				deleteProject(GITHUB_BASE_URL, gitlabConfig.getPrivateToken(), projectId, projectName);

			}
			throw e;
		}

		return webUrl;

	}

	@Override
	public boolean supports(String gitURL) {
		return gitURL.toLowerCase().contains(GITHUB);
	}

	@Override
	public List<CommitWrapper> getCommitsForFile(GitlabConfiguration gitConfiguration, String filePath, String branch) {
		try {
			String url = GITHUB_BASE_URL
					+ String.format(REPOS, gitConfiguration.getUser(), gitConfiguration.getProjectName()) + COMMITS
					+ "?path=" + URLEncoder.encode(filePath, StandardCharsets.UTF_8.name());
			if (StringUtils.hasText(branch)) {
				url = url + "&sha=" + URLEncoder.encode(branch, StandardCharsets.UTF_8.name());
			}
			final ResponseEntity<List<CommitWrapper>> response = execute(url, HttpMethod.GET, null,
					gitConfiguration.getPrivateToken(), new ParameterizedTypeReference<List<CommitWrapper>>() {
					});
			return response.getBody();
		} catch (final UnsupportedEncodingException e) {
			throw new GitException("Invalid filepath name: " + filePath, e);
		}
	}

	@Override
	public ResponseEntity<JsonNode> sendHttp(String url, HttpMethod httpMethod, String body, String token)
			throws URISyntaxException {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.hasText(token)) {
			headers.add("Authorization", "Bearer " + token);
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

	private <T> ResponseEntity<T> execute(String url, HttpMethod method, Object body, String token,
			ParameterizedTypeReference<T> clazz) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			return restTemplate.exchange(new URI(url), method, new HttpEntity<>(body, headers), clazz);
		} catch (final URISyntaxException e) {
			log.error("Error on request {}", url, e);
			throw new GitException("URISyntaxException on url " + url + " :" + e.getMessage());
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error on request {}, code: {}, cause: {}", url, e.getRawStatusCode(),
					e.getResponseBodyAsString());
			throw e;
		}
	}

	@Override
	public List<String> getRepoDirectories(GitlabConfiguration gitConfiguration, String branch) {

		try {
			final List<String> directories = new ArrayList<>();
			final ResponseEntity<JsonNode> response = sendHttp(
					GITHUB_BASE_URL + REPO_BASE + "/" + getProjectPathFromURL(gitConfiguration.getProjectURL())
							+ CONTENTS + "?ref=" + gitConfiguration.getBranch(),
					HttpMethod.GET, null, gitConfiguration.getPrivateToken());
			response.getBody().forEach(n -> {
				if (n.get("type").asText().equals("dir")) {
					directories.add(n.get("name").asText());
				}
			});
			return directories;
		} catch (final Exception e) {
			log.error("Error while getting repo directories", e);
		}
		return null;
	}

	@Override
	public String getBase64ForFile(GitlabConfiguration gitConfiguration, String branch, String filePath) {
		try {
			final ResponseEntity<JsonNode> response = sendHttp(
					GITHUB_BASE_URL + REPO_BASE + "/" + getProjectPathFromURL(gitConfiguration.getProjectURL())
							+ CONTENTS + "/" + filePath + "?ref=" + gitConfiguration.getBranch(),
					HttpMethod.GET, null, gitConfiguration.getPrivateToken());
			return response.getBody().get("content").asText().replace("\n", "");
		} catch (final Exception e) {
			log.error("Error while getting repo directories", e);
		}
		return null;
	}

}

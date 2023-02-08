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
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MicroservicesGitHubRestServiceImpl extends MicroservicesGitRestService {

	private static final String GITHUB_BASE_URL = "https://api.github.com";
	private static final String USERNAME_STR = "login";
	private static final String USER_REPOS = "/user/repos";
	private static final String REPOS_DELETE = "/repos/%s/%s";

	@Override
	public String getOauthToken(String url, String user, String password) throws GitlabException {
		throw new NotImplementedException();
	}

	@Override
	public JsonNode createProject(String url, String token, String name, int namespaceId, boolean isPrivateToken)
			throws GitlabException {
		final String body = "{\"name\":\"" + name + "\",\"visibility\":\"public\"}";

		try {
			final ResponseEntity<JsonNode> response = sendHttp(GITHUB_BASE_URL + USER_REPOS, HttpMethod.POST, body,
					token, isPrivateToken);
			return response.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException(e.getMessage());
		}
	}

	@Override
	public void deleteGitlabProject(String projectName, String namespace, String url, String privateToken)
			throws GitlabException {
		throw new NotImplementedException();
	}

	@Override
	public Map<String, Integer> authorizeUsers(String url, String token, int projectId, List<String> users,
			boolean isPrivateToken) throws IOException, GitlabException, URISyntaxException {
		throw new NotImplementedException();
	}

	@Override
	public String createGitlabProject(String gitlabConfigId, String projectName, List<String> users, String url,
			boolean scaffolding) throws GitlabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createGitlabProject(Microservice microservice, boolean scaffolding, File file, MSConfig config,
			GitlabConfiguration cloneConfig) throws GitlabException {
		int projectId = 0;
		boolean projectCreated = false;
		final GitlabConfiguration mainConfig = microservice.getGitlabConfiguration();
		String webUrl = mainConfig.getSite();
		String projectName = microservice.getIdentification();
		try {

			// // First generate gitlab config for convenience
			final GitlabConfiguration gitConfig = getGitlabConfigurationFromPrivateToken(GITHUB_BASE_URL,
					mainConfig.getPrivateToken());
			gitConfig.setSite(mainConfig.getSite());

			final JsonNode projectInfo = createProject(GITHUB_BASE_URL, mainConfig.getPrivateToken(),
					microservice.getIdentification(), 0, false);

			projectId = projectInfo.get("id").asInt();
			webUrl = projectInfo.get("html_url").asText();
			projectName = projectInfo.get("name").asText();
			projectCreated = true;
			// unprotectBranch(mainConfig.getSite(), mainConfig.getPrivateToken(),
			// projectId, true, PUSH_BRANCH);
			if (scaffolding) {
				scafold(file, microservice, config, gitConfig, projectInfo, cloneConfig);
			}

		} catch (final Exception e) {
			log.error("Could not create Gitlab project {}", e.getMessage());
			if (projectCreated) {
				log.error(
						"Project was created in GitHub but something went wrong, rolling back and destroying repository {}",
						microservice.getIdentification());
				deleteProject(GITHUB_BASE_URL, mainConfig.getPrivateToken(), projectId, false, projectName);

			}
			throw e;
		}

		return webUrl;

	}

	@Override
	public int createNamespace(String url, String name, String token, boolean isPrivateToken) throws GitlabException {
		throw new NotImplementedException();
	}

	@Override
	public int getNamespace(String url, String name, String token, boolean isPrivateToken) throws GitlabException {
		throw new NotImplementedException();
	}

	@Override
	public boolean namespaceExists(String url, String name, String token, boolean isPrivateToken)
			throws GitlabException {
		throw new NotImplementedException();
	}

	@Override
	public void deleteProject(String url, String token, int projectId, boolean isPrivateToken, String projectName)
			throws GitlabException {
		final GitlabConfiguration gitConfig = getGitlabConfigurationFromPrivateToken(GITHUB_BASE_URL, token);
		try {
			sendHttp(GITHUB_BASE_URL + String.format(REPOS_DELETE, gitConfig.getUser(), projectName), HttpMethod.DELETE,
					null, token, isPrivateToken);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException(e.getMessage());
		}

	}

	@Override
	public GitlabConfiguration getGitlabConfigurationFromPrivateToken(String url, String privateToken)
			throws GitlabException {
		try {
			final ResponseEntity<JsonNode> response = sendHttp(GITHUB_BASE_URL.concat(GIT_CURRENT_USER), HttpMethod.GET,
					null, privateToken, false);
			return GitlabConfiguration.builder().site(url).email(response.getBody().get(EMAIL_STR).asText())
					.user(response.getBody().get(USERNAME_STR).asText()).privateToken(privateToken).build();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not retrieve user info from private token {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not retrieve user info from private token" + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not retrieve user info from private token" + e.getMessage());
		}
	}

	@Override
	public void deleteProjectGroup(String url, String token, int groupId, boolean isPrivateToken)
			throws GitlabException {
		throw new NotImplementedException();
	}

	@Override
	public void unprotectBranch(String url, String token, int projectId, boolean isPrivateToken, String branch)
			throws GitlabException {
		throw new NotImplementedException();

	}

	@Override
	public boolean supports(GitlabConfiguration gitlabConfig) {
		return gitlabConfig.getSite().toLowerCase().contains(GITHUB);
	}

}

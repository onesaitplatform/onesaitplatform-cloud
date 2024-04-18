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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.model.MicroserviceTemplate;
import com.minsait.onesait.platform.config.model.MicroserviceTemplate.Language;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.mstemplates.MicroserviceTemplatesService;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MicroservicesGitlabRestServiceImpl extends MicroservicesGitRestService {

	private static final String GITLAB_API_PATH = "/api/v4";
	private static final String GITLAB_OAUTH = "/oauth/token";
	private static final String GITLAB_PROJECTS = "/projects";
	private static final String GITLAB_GROUPS = "/groups";
	private static final String GITLAB_USERS = "/users?per_page=1000";
	private static final String GITLAB_MEMBERS = "/members";
	private static final String USERNAME_STR = "username";

	@Value("${onesaitplatform.gitlab.scaffolding.directory:/tmp/scaffolding}")
	private String directoryScaffolding;

	@Autowired
	private MicroserviceTemplatesService mstemplateService;
	
	@Override
	public String createGitlabProject(String gitlabConfigId, String projectName, List<String> users, String url,
			boolean scaffolding) throws GitlabException {
		final GitlabConfiguration gitlab = configurationService.getGitlabConfiguration(gitlabConfigId);
		String webUrl = "";
		if (gitlab != null) {
			final String urlGitlab = StringUtils.hasText(url) ? url : gitlab.getSite();
			final String user = gitlab.getUser();
			final String password = gitlab.getPassword();
			boolean projectCreated = false;
			int projectId = 0;
			String accessToken = "";
			if (StringUtils.hasText(urlGitlab) && StringUtils.hasText(user) && StringUtils.hasText(password)) {
				try {
					accessToken = getOauthToken(urlGitlab, user, password);
					if (StringUtils.hasText(accessToken)) {
						final int namespaceId = createNamespace(urlGitlab, projectName, accessToken, false);
						log.info("Namespace created with id: " + namespaceId);
						log.info("Project is going to be created with parameters, url: " + urlGitlab + " accessToken: "
								+ accessToken + " projectName: " + projectName + " namespaceId: " + namespaceId);
						final JsonNode projectInfo = createProject(urlGitlab, accessToken, projectName, namespaceId,
								false);
						projectId = projectInfo.get("id").asInt();
						webUrl = projectInfo.get("web_url").asText();
						projectCreated = true;
						try {
							authorizeUsers(urlGitlab, accessToken, projectId, users, false);
						} catch (final GitlabException e) {
							log.error("Could not add users to project");
						}
						if (scaffolding) {
							microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_SCAFFOLDING, false, null,
									null);
						}

						microserviceTemplateUtil.generateScaffolding(projectInfo, gitlab, null, null, null, null, 0);

						return webUrl;
					}
				} catch (final Exception e) {
					log.error("Could not create Gitlab project {}", e.getMessage());
					if (projectCreated) {
						log.error(
								"Project was created in gitlab but something went wrong, rolling back and destroying project {}",
								projectName);
						deleteProject(urlGitlab, accessToken, projectId, false, null);
					}
					throw new GitlabException(e.getMessage());
				}
			}

		} else {
			throw new GitlabException("No configuration found for Gitlab");
		}
		return webUrl;

	}

	@Override
	public String createGitlabProject(Microservice microservice, boolean scaffolding, File file, MSConfig config,
			GitlabConfiguration cloneConfig) throws GitlabException {
		int projectId = 0;
		boolean projectCreated = false;
		boolean namespaceCreated = false;
		int namespaceId = 0;
		final GitlabConfiguration mainConfig = microservice.getGitlabConfiguration();
		String webUrl = mainConfig.getSite();
		try {

			// First generate gitlab config for convenience
			final GitlabConfiguration gitlabConfig = getGitlabConfigurationFromPrivateToken(mainConfig.getSite(),
					mainConfig.getPrivateToken());
			if (StringUtils.hasText(config.getGitlabGroup())) {
				if (namespaceExists(mainConfig.getSite(), config.getGitlabGroup(), mainConfig.getPrivateToken(),
						true)) {
					namespaceId = getNamespace(mainConfig.getSite(), config.getGitlabGroup(),
							mainConfig.getPrivateToken(), true);
				} else {
					namespaceId = createNamespace(mainConfig.getSite(), config.getGitlabGroup(),
							mainConfig.getPrivateToken(), true);
					namespaceCreated = true;
				}
			} else {
				namespaceId = createNamespace(mainConfig.getSite(), microservice.getIdentification(),
						mainConfig.getPrivateToken(), true);
				namespaceCreated = true;
			}

			final JsonNode projectInfo = createProject(mainConfig.getSite(), mainConfig.getPrivateToken(),
					microservice.getIdentification(), namespaceId, true);
			projectId = projectInfo.get("id").asInt();
			webUrl = projectInfo.get("web_url").asText();
			projectCreated = true;
			unprotectBranch(mainConfig.getSite(), mainConfig.getPrivateToken(), projectId, true, PUSH_BRANCH);
			// TO-DO authorize users into the project
			if (scaffolding) {
				if (file == null) {
					MicroserviceTemplate mstemplate = mstemplateService.getMsTemplateByIdentification(microservice.getTemplateType(), microservice.getUser().getUserId());
//					if (microservice.getTemplateType().equals(TemplateType.ML_MODEL_ARCHETYPE.toString())) {
//						microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_MICROSERVICE_ML, false,
//								config.getSources(), config.getDocker());
//						microserviceTemplateUtil.generateScaffolding(projectInfo, gitlabConfig,
//								microservice.getTemplateType(), config.getOntology(), config.getNotebook(),
//								microservice.getContextPath(), microservice.getPort());
//					} else if (microservice.getTemplateType().equals(TemplateType.NOTEBOOK_ARCHETYPE.toString())) {
//						microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_MICROSERVICE_NB, false,
//								config.getSources(), config.getDocker());
//						microserviceTemplateUtil.generateScaffolding(projectInfo, gitlabConfig,
//								microservice.getTemplateType(), config.getOntology(), config.getNotebook(),
//								microservice.getContextPath(), microservice.getPort());
//					} else 
					if (microservice.getTemplateType().equals(TemplateType.IMPORT_FROM_GIT.toString())) {
						microserviceTemplateUtil.cloneAndPush(projectInfo, mainConfig, cloneConfig, true,
								config.getSources(), config.getDocker());
					} else if(mstemplate != null ) {
						if(mstemplate.getLanguage().equals(Language.ML_MODEL_ARCHETYPE)) {
							microserviceTemplateUtil.cloneProcessMLAndPush(projectInfo, mainConfig, mstemplate, true);
						} else if(mstemplate.getLanguage().equals(Language.NOTEBOOK_ARCHETYPE)) {
							microserviceTemplateUtil.cloneProcessNBAndPush(projectInfo, mainConfig, mstemplate, true, config.getNotebook());
						} else if(mstemplate.getLanguage().equals(Language.IOT_CLIENT_ARCHETYPE)) {
							microserviceTemplateUtil.cloneProcessIOTAndPush(projectInfo, mainConfig, mstemplate, true, microservice.getPort(), microservice.getContextPath(), config.getOntology());
						} else {
							microserviceTemplateUtil.cloneAndPushWithTemplate(projectInfo, mainConfig, mstemplate, true);
						}
					} else {
						microserviceTemplateUtil.createAndExtractFiles(RESOURCE_PATH_MICROSERVICE_IOT, false,
								config.getSources(), config.getDocker());
						microserviceTemplateUtil.generateScaffolding(projectInfo, gitlabConfig,
								microservice.getTemplateType(), config.getOntology(), config.getNotebook(),
								microservice.getContextPath(), microservice.getPort());
					}
				} else {
					microserviceTemplateUtil.createAndExtractFiles(file.getAbsolutePath(), true, config.getSources(),
							config.getDocker());
					microserviceTemplateUtil.generateScaffolding(projectInfo, gitlabConfig,
							microservice.getTemplateType(), config.getOntology(), config.getNotebook(),
							microservice.getContextPath(), microservice.getPort());
				}
			}

		} catch (final Exception e) {
			log.error("Could not create Gitlab project {}", e.getMessage());
			if (projectCreated) {
				log.error(
						"Project was created in gitlab but something went wrong, rolling back and destroying project {}",
						microservice.getIdentification());
				deleteProject(mainConfig.getSite(), mainConfig.getPrivateToken(), projectId, true, null);
				if (namespaceCreated) {
					deleteProjectGroup(mainConfig.getSite(), mainConfig.getPrivateToken(), namespaceId, true);
				}

			}
			throw e;
		}

		return webUrl;
	}

	@Override
	public GitlabConfiguration getGitlabConfigurationFromPrivateToken(String url, String privateToken)
			throws GitlabException {
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GIT_CURRENT_USER),
					HttpMethod.GET, null, privateToken, true);
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
	public String getOauthToken(String url, String user, String password) throws GitlabException {
		final String body = "{\"grant_type\":\"password\",\"username\":\"" + user + "\",\"password\":\"" + password
				+ "\"}";
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_OAUTH), HttpMethod.POST, body, null,
					false);
			return response.getBody().get("access_token").asText();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not get authentication token {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not get authentication token", e);
		} catch (final Exception e) {
			throw new GitlabException("Could not get authentication token", e);
		}

	}

	@Override
	public int createNamespace(String url, String name, String token, boolean isPrivateToken) throws GitlabException {
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
			final int parentId = getNamespace(url, directParentGroup, token, isPrivateToken);
			if (parentId > 0) {
				body = "{\"name\":\"" + subgroups[subgroups.length - 1] + "\",\"path\":\""
						+ subgroups[subgroups.length - 1].toLowerCase().replace(" ", "-")
						+ "\", \"visibility\":\"private\", \"parent_id\":" + parentId + "}";
			} else {
				throw new GitlabException("Parent group " + directParentGroup + " does not exist in this GitLab.");
			}
		}
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS),
					HttpMethod.POST, body, token, isPrivateToken);
			namespaceId = response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
				log.warn("Not allowed to create groups in this Gitlab repository, returning self user namespace");
				return -1;
			}
			log.error("Could not create namespace for project {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not create namespace for project" + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not create namespace for project" + e.getMessage());
		}
		return namespaceId;
	}

	@Override
	public int getNamespace(String url, String name, String token, boolean isPrivateToken) throws GitlabException {
		int namespaceId = 0;
		try {
			final ResponseEntity<JsonNode> response = sendHttp(
					url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS) + "/"
							+ URLEncoder.encode(name, StandardCharsets.UTF_8.name()),
							HttpMethod.GET, null, token, isPrivateToken);
			namespaceId = response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
				log.warn("Gitlab Group {} not found, will create it", name);
				return -1;
			}
			log.error("Could not get Gitlab Group {}: {}", name, e.getResponseBodyAsString());
			throw new GitlabException("Could not get Gitlab Group " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not get Gitlab Group " + e.getMessage());
		}
		return namespaceId;
	}

	@Override
	public boolean namespaceExists(String url, String name, String token, boolean isPrivateToken)
			throws GitlabException {
		return getNamespace(url, name, token, isPrivateToken) > 0;
	}

	@Override
	public void unprotectBranch(String url, String token, int projectId, boolean isPrivateToken, String branch)
			throws GitlabException {
		try {
			sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS) + "/" + projectId + "/protected_branches/"
					+ branch, HttpMethod.DELETE, null, token, isPrivateToken);
		} catch (final Exception e) {
			// NO-OP
		}
	}

	@Override
	public JsonNode createProject(String url, String token, String name, int namespaceId, boolean isPrivateToken)
			throws GitlabException {
		String body = "{\"name\":\"" + name + "\",\"visibility\":\"private\", \"namespace_id\":" + namespaceId + "}";
		if (namespaceId == -1) {
			body = "{\"name\":\"" + name + "\",\"visibility\":\"private\"}";
		}
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS),
					HttpMethod.POST, body, token, isPrivateToken);
			return response.getBody();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException(e.getMessage());
		}

	}

	@Override
	public void deleteProject(String url, String token, int projectId, boolean isPrivateToken, String projectName)
			throws GitlabException {
		try {
			sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS).concat("/").concat(String.valueOf(projectId)),
					HttpMethod.DELETE, "", token, isPrivateToken);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not delete project" + e.getMessage());
		}

	}

	@Override
	public void deleteProjectGroup(String url, String token, int groupId, boolean isPrivateToken)
			throws GitlabException {
		try {
			if (groupId != -1) {
				sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_GROUPS).concat("/").concat(String.valueOf(groupId)),
						HttpMethod.DELETE, "", token, isPrivateToken);
			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not delete project {}", e.getResponseBodyAsString());
			throw new GitlabException(e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not delete project" + e.getMessage());
		}
	}

	@Override
	public Map<String, Integer> authorizeUsers(String url, String token, int projectId, List<String> users,
			boolean isPrivateToken) throws IOException, GitlabException, URISyntaxException {
		final ArrayNode repoUsers = (ArrayNode) getRepositoryUsers(url, token, false);
		final List<String> existingUsers = new ArrayList<>();
		try {
			for (final JsonNode user : repoUsers) {
				log.info("Authorize user: " + user.get(USERNAME_STR).asText());
				if (users.contains(user.get(USERNAME_STR).asText())) {
					authorizeUser(url, projectId, user.get("id").asInt(), token, isPrivateToken);
					existingUsers.add(user.get(USERNAME_STR).asText());
					log.info("User authorized!! " + user.get(USERNAME_STR).asText());
				}
			}
			final List<String> newUsers = users.stream().filter(s -> !existingUsers.contains(s))
					.collect(Collectors.toList());
			for (final String user : newUsers) {
				try {
					final int newUserId = createNewUser(url, token, userService.getUser(user), false);
					authorizeUser(url, projectId, newUserId, token, false);
				} catch (final GitlabException e) {
					log.error("Could not create user {}, cause:", user, e.getMessage());
				}

			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not get authentication token {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not authorize users " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not authorize users " + e.getMessage());
		}
		return null;
	}

	private int createNewUser(String url, String token, User user, boolean isPrivateToken) throws GitlabException {
		final String body = "{\"email\":\"" + user.getEmail() + "\", \"username\":\"" + user.getUserId()
		+ "\",\"name\":\"" + user.getFullName() + "\",\"reset_password\": true}";
		try {
			final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_USERS),
					HttpMethod.POST, body, token, isPrivateToken);
			return response.getBody().get("id").asInt();
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Could not create user {}", e.getResponseBodyAsString());
			throw new GitlabException("Could not create user " + e.getResponseBodyAsString());
		} catch (final Exception e) {
			throw new GitlabException("Could not create user, your access level is not Administrator", e);
		}
	}

	private void authorizeUser(String url, int projectId, int userId, String token, boolean isPrivateToken)
			throws URISyntaxException {
		final String body = "{\"id\": " + projectId + ", \"access_level\": 30 , \"user_id\":" + userId + "}";
		sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS).concat("/").concat(String.valueOf(projectId))
				.concat(GITLAB_MEMBERS), HttpMethod.POST, body, token, isPrivateToken);
	}

	private JsonNode getRepositoryUsers(String url, String token, boolean isPrivateToken) throws URISyntaxException {
		final ResponseEntity<JsonNode> response = sendHttp(url.concat(GITLAB_API_PATH).concat(GITLAB_USERS),
				HttpMethod.GET, "", token, isPrivateToken);
		return response.getBody();
	}

	@Override
	public void deleteGitlabProject(String projectName, String namespace, String url, String privateToken)
			throws GitlabException {
		String urlEncoded;
		try {
			urlEncoded = URLEncoder.encode(url.concat(GITLAB_API_PATH).concat(GITLAB_PROJECTS).concat("/")
					.concat(namespace + "/" + projectName), StandardCharsets.UTF_8.displayName());
			sendHttp(urlEncoded, HttpMethod.DELETE, null, privateToken, true);
		} catch (final UnsupportedEncodingException e) {
			log.error("Could not delete gitlabrepo. Wrong namespace or projectname {}", e);
		} catch (final Exception e) {
			log.error("Could not delete gitlabrepo {}", e);
		}

	}

	@Override
	public boolean supports(GitlabConfiguration gitlabConfig) {
		return !gitlabConfig.getSite().toLowerCase().contains(GITHUB);
	}

}

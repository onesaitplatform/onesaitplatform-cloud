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
package com.minsait.onesait.platform.serverless.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.serverless.dto.git.GitlabConfiguration;
import com.minsait.onesait.platform.serverless.exception.GitException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitServiceManager {

	@Autowired
	private List<GitRestService> gitServices;
	@Autowired
	private GitOperations gitOperations;

	private static final String TMP_DIR = "/tmp/";
	private static final String DEFAULT_BRANCH = "master";

	public GitRestService dispatchService(String gitURL) {
		return gitServices.stream().filter(s -> s.supports(gitURL)).findFirst().orElse(null);
	}

	public String createGitProject(String projectName, GitlabConfiguration gitConfiguration) {
		final GitRestService restService = dispatchService(gitConfiguration.getSite());
		final String projectURL = restService.createGitlabProject(false, null, gitConfiguration, projectName);
		gitOperations.deleteDirectory(TMP_DIR + projectName);
		gitOperations.createDirectory(TMP_DIR + projectName);
		gitOperations.configureGitAndInit(gitConfiguration.getUser(), gitConfiguration.getEmail(),
				TMP_DIR + projectName);
		log.debug("Git project configured for user {} with email {}", gitConfiguration.getUser(),
				gitConfiguration.getEmail());
		gitOperations.addOrigin(projectURL, TMP_DIR + projectName, false);
		log.debug("Origin added {}", projectURL);
		gitOperations.createReadme("OSP Serverless Application: " + projectName, TMP_DIR + projectName);
		log.debug("Created README.MD");
		gitOperations.addAll(TMP_DIR + projectName);
		log.debug("Added all files");
		gitOperations.commit("OSP Serverless Application", TMP_DIR + projectName);
		log.debug("Initial commit");
		gitOperations.createBranch(gitConfiguration.getBranch() == null ? DEFAULT_BRANCH : gitConfiguration.getBranch(),
				TMP_DIR + projectName);
		gitOperations.push(projectURL, gitConfiguration.getUser(), gitConfiguration.getPrivateToken(),
				gitConfiguration.getBranch() == null ? DEFAULT_BRANCH : gitConfiguration.getBranch(),
				TMP_DIR + projectName, false);
		log.debug("Pushed to: " + projectURL);
		gitOperations.deleteDirectory(TMP_DIR + projectName);
		log.debug("Deleting temp directory {}", TMP_DIR + projectName);
		log.debug("END scafolding project generation");
		return projectURL;
	}

	public void deleteProject(GitlabConfiguration gitConfig) {
		final GitRestService restService = dispatchService(gitConfig.getSite());
		String projectName = null;
		try {
			projectName = gitConfig.getProjectURL().split("/")[gitConfig.getProjectURL().split("/").length - 1];
		} catch (final Exception e) {
			log.error("Invalid git project URL {}, could not extract project name", gitConfig.getProjectURL());
			throw new GitException(
					"Invalid git project URL " + gitConfig.getProjectURL() + ", could not extract project name");
		}
		restService.deleteProject(gitConfig.getProjectURL(), gitConfig.getPrivateToken(), 0, projectName);

	}

	public String cloneRepo(String projectName, GitlabConfiguration gitConfig) {
		gitOperations.deleteDirectory(TMP_DIR + projectName);
		gitOperations.createDirectory(TMP_DIR + projectName);
		gitOperations.cloneRepository(TMP_DIR + projectName, gitConfig);
		gitOperations.checkout(gitConfig.getBranch() == null ? DEFAULT_BRANCH : gitConfig.getBranch(),
				TMP_DIR + projectName);
		return TMP_DIR + projectName;
	}

	public void addAllAndPush(String projectName, GitlabConfiguration gitConfig) {
		String email = null;
		try {
			email = dispatchService(gitConfig.getSite())
					.getGitlabConfigurationFromPrivateToken(gitConfig.getSite(), gitConfig.getPrivateToken())
					.getEmail();
		} catch (final Exception e) {
			log.error("Could not get email from config");
		}
		gitOperations.configureGit(gitConfig.getUser(), email != null ? email : gitConfig.getEmail(),
				TMP_DIR + projectName);
		gitOperations.addAll(TMP_DIR + projectName);
		gitOperations.commit("Bump function yaml version for deployments", TMP_DIR + projectName);
		gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
				gitConfig.getBranch() == null ? DEFAULT_BRANCH : gitConfig.getBranch(), TMP_DIR + projectName, false);
		gitOperations.deleteDirectory(TMP_DIR + projectName);
	}

	public void deleteClonedRepo(String projectName) {
		gitOperations.deleteDirectory(TMP_DIR + projectName);
	}

}

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
package com.minsait.onesait.platform.config.versioning;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.git.GitException;
import com.minsait.onesait.platform.commons.git.GitOperations;
import com.minsait.onesait.platform.commons.git.GitSyncException;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VersioningManagerImpl implements VersioningManager {

	@Autowired
	private VersioningIOService versioningIOService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GitOperations gitOperations;
	@Autowired
	private ActiveProfileDetector profileDetector;
	@Autowired
	private ConfigurationRepository configurationRepository;
	@Autowired
	@Lazy
	private VersioningTxBusinessService versioningTxBusinessService;

	private GitlabConfiguration gitConfig;

	@Value("${spring.application.name:no-name}")
	private String applicationName;

	@PostConstruct
	public void loadGitConfig() {
		if (applicationName.contains("control-panel")) {
			final Configuration config = configurationRepository.findByTypeAndEnvironment(Configuration.Type.VERSIONING,
					profileDetector.getActiveProfile());
			if (config != null) {
				try {
					gitConfig = new YAMLMapper().readValue(config.getYmlConfig(), GitlabConfiguration.class);
				} catch (final IOException e) {
					log.warn("Error while loading versioning git config: {}", e.getMessage());
				}
			}
		}
	}

	@Override
	public <T> void serialize(Versionable<T> versionable) {
		versioningIOService.serializeToFileSystem(versionable);
	}

	@Override
	@Async
	public synchronized <T> void serialize(Versionable<T> versionable, String userId, String message,
			EventType eventType) {
		versioningIOService.serializeToFileSystem(versionable);
		commit(versionable, userId, message, eventType);
	}

	@Override
	public <T> void removeSerialization(Versionable<T> versionable) {
		versioningIOService.removeFromFileSystem(versionable);
	}

	@Override
	@Async
	public synchronized <T> void removeSerialization(Versionable<T> versionable, String userId, String message,
			EventType eventType) {
		versioningIOService.removeFromFileSystem(versionable);
		commit(versionable, userId, message, eventType);
	}

	@Override
	public <T> void restoreSerialization(Versionable<T> versionable, String commitId) {
		restoreSerialization(versionable, commitId, null, null);
	}

	@Override
	public <T> void restoreSerialization(Versionable<T> versionable, String commitId, String userId, String message) {
		gitOperations.rollbackFile(VersioningIOService.DIR, commitId, versioningIOService.relativePath(versionable));
		if (versionable.zipFileNames() != null) {
			versionable.zipFileNames().forEach(s -> gitOperations.rollbackFile(VersioningIOService.DIR, commitId,
					s.substring(VersioningIOService.DIR.length())));
		}
		versioningIOService.restoreFromFileSystem(versionable);
		if (StringUtils.isEmpty(userId)) {
			if (StringUtils.isEmpty(message)) {
				message = String.format(DEFAULT_RESTORE_COMMIT_MESSAGE_NO_USER, versionable.fileName(), commitId);
			}
			gitOperations.commit(message, VersioningIOService.DIR);
			pushToRemote();
		} else {
			if (StringUtils.isEmpty(message)) {
				message = String.format(DEFAULT_RESTORE_COMMIT_MESSAGE, versionable.fileName(), userId, commitId);
			}
			gitOperations.commit(message, VersioningIOService.DIR, getGitAuthor(
					userRepository.findFullNameByUserId(userId), userRepository.findEmailByUserId(userId)));
			pushToRemote();
		}
	}

	@Override
	public <T> void commit(Versionable<T> versionable, String userId, String message, EventType eventType) {
		if (StringUtils.isEmpty(userId)) {
			commit(versionable, message);
		} else {
			if (StringUtils.isEmpty(message)) {
				switch (eventType) {
				case DELETE:
					message = String.format(DEFAULT_COMMIT_MESSAGE_DELETE, versionable.fileName(), userId);
					break;
				case CREATE:
					message = String.format(DEFAULT_COMMIT_MESSAGE_CREATE, versionable.fileName(), userId);
					break;
				case UPDATE:
				default:
					message = String.format(DEFAULT_COMMIT_MESSAGE_UPDATE, versionable.fileName(), userId);
					break;
				}
			}
			final String author = getGitAuthor(userRepository.findFullNameByUserId(userId),
					userRepository.findEmailByUserId(userId));
			log.debug("Commiting file {} for GIT user {} with message {}", versionable.fileName(), author, message);
			try {
				String pathToFile = versionable.pathToVersionable(false);
				if (pathToFile.endsWith(versionable.getClass().getSimpleName())) {
					pathToFile = versioningIOService.absolutePath(versionable);
				}
				gitOperations.addFile(VersioningIOService.DIR, pathToFile);
				gitOperations.commit(message, VersioningIOService.DIR, author);
				pushToRemote();
			} catch (final GitException e) {
				log.error("Error checking out versionable");
			}
		}
	}

	@Override
	public <T> void commit(Versionable<T> versionable, String message) {
		if (StringUtils.isEmpty(message)) {
			message = String.format(DEFAULT_COMMIT_MESSAGE_NO_USER, versionable.fileName());
		}
		log.debug("Commiting file {} with message {}", versionable.fileName(), message);
		try {
			gitOperations.addFile(VersioningIOService.DIR, versioningIOService.absolutePath(versionable));
			gitOperations.commit(message, VersioningIOService.DIR);
			pushToRemote();
		} catch (final GitException e) {
			log.error("Error checking out versionable");
		}

	}

	@Override
	public boolean isActive() {
		if (gitConfig != null) {
			try {
				return gitConfig.getEnable();
			} catch (final Exception e) {
				log.error("Could not read enable property on Git configuration", e);
			}
		}
		return false;
	}

	@Override
	public GitlabConfiguration getGitConfiguration() {
		return gitConfig;
	}

	@Override
	public void saveGitConfiguration(GitlabConfiguration gitConfiguration) {
		Configuration config = configurationRepository.findByTypeAndEnvironment(Configuration.Type.VERSIONING,
				profileDetector.getActiveProfile());
		try {
			if (config != null) {
				config.setYmlConfig(new YAMLMapper().writeValueAsString(gitConfiguration));
				configurationRepository.save(config);

			} else {
				config = new Configuration();
				config.setType(Type.VERSIONING);
				config.setEnvironment(profileDetector.getActiveProfile());
				config.setUserJson(SecurityContextHolder.getContext().getAuthentication().getName());
				config.setIdentification("Versioning Git Configuration");
				config.setYmlConfig(new YAMLMapper().writeValueAsString(gitConfiguration));
				configurationRepository.save(config);
			}
		} catch (final JsonProcessingException e) {
			throw new VersioningException("could not save new Git configuration");
		}
		gitConfig = gitConfiguration;

	}

	@Override
	public void enableFeature(boolean enable) {
		if (gitConfig != null) {
			gitConfig.setEnable(enable);
			saveGitConfiguration(gitConfig);
		}
	}

	private String getGitAuthor(String fullName, String email) {
		return fullName + " <" + email + ">";
	}

	private void pushToRemote() {
		if (isActive()) {
			try {
				gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
						gitConfig.getBranch(), VersioningIOService.DIR, false);
			} catch (final Exception e) {
				syncOriginAndDB();
				try {
					gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
							gitConfig.getBranch(), VersioningIOService.DIR, false);
				} catch (final GitSyncException e1) {
					log.error("Failed to push after rebase", e1);
				}
			}
			updateLastCommitProcessed();
		}
	}

	@Override
	public void syncOriginAndDB() {
		versioningTxBusinessService.syncOriginAndDB();
	}

	@Override
	public void updateLastCommitProcessed() {
		gitConfig.setLastCommitSHA(gitOperations.getCurrentSHA(VersioningIOServiceImpl.DIR));
	}

	@Override
	public void removeGitConfiguration() {
		final Configuration config = configurationRepository.findByTypeAndEnvironment(Configuration.Type.VERSIONING,
				profileDetector.getActiveProfile());
		if (config != null) {
			gitConfig = null;
			configurationRepository.deleteById(config.getId());
		}
	}

	@Override
	public Optional<String> getGitConfigId() {
		if (isActive()) {
			final Configuration config = configurationRepository.findByTypeAndEnvironment(Configuration.Type.VERSIONING,
					profileDetector.getActiveProfile());
			return Optional.of(config.getId());
		} else {
			return Optional.empty();
		}
	}

}

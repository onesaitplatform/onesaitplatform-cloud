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
package com.minsait.onesait.platform.config.versioning;

import java.util.Optional;

import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

public interface VersioningManager {

	public static final String DEFAULT_COMMIT_MESSAGE_UPDATE = "Changed file %s by user %s";
	public static final String DEFAULT_COMMIT_MESSAGE_CREATE = "Created file %s by user %s";
	public static final String DEFAULT_COMMIT_MESSAGE_DELETE = "Removed file %s by user %s";
	public static final String DEFAULT_COMMIT_MESSAGE_NO_USER = "Changed file %s";
	public static final String DEFAULT_RESTORE_COMMIT_MESSAGE = "Restored file %s by user %s from commit %s";
	public static final String DEFAULT_RESTORE_COMMIT_MESSAGE_NO_USER = "Restored file %s from commit %s";

	public enum EventType{
		UPDATE, CREATE, DELETE
	}

	public <T> void serialize(Versionable<T> versionable);

	public <T> void serialize(Versionable<T> versionable, String userId, String message, EventType eventType);

	public <T> void removeSerialization(Versionable<T> versionable);

	public <T> void removeSerialization(Versionable<T> versionable, String userId, String message, EventType eventType);

	public <T> void restoreSerialization(Versionable<T> versionable, String commitId);

	public <T> void restoreSerialization(Versionable<T> versionable, String commitId,String userId, String message);

	public <T> void commit(Versionable<T> versionable, String userId, String message, EventType eventType);

	public <T> void commit(Versionable<T> versionable, String message);

	public boolean isActive();

	public void saveGitConfiguration(GitlabConfiguration gitConfiguration);

	public void removeGitConfiguration();

	public GitlabConfiguration getGitConfiguration();

	public void enableFeature(boolean enable);

	public Optional<String> getGitConfigId();

	public void syncOriginAndDB();

	void updateLastCommitProcessed();

}

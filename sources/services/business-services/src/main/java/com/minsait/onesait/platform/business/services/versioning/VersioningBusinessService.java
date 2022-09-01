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
package com.minsait.onesait.platform.business.services.versioning;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.commons.git.CommitWrapper;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.interfaces.Versionable.SpecialVersionable;
import com.minsait.onesait.platform.config.versioning.RestorePlatformDTO;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface VersioningBusinessService {

	public Map<String, String> getVersionableClases();

	public <T extends AuditableEntity> Collection<Versionable<T>> getEntitiesForUser(String userId, String clazz);

	public <T extends AuditableEntity> Versionable<T> findById(Object id, String clazz);

	public <T> List<CommitWrapper> getCommitsForVersionable(Versionable<T> versionable);

	public RestoreReport restoreFile(RestoreRequestDTO restoreRequest,  RestoreReport report);

	public String getFileContent(String file, String commitId);

	public String getFileContent(Versionable<?> versionable, String commitId);

	public boolean isActive();

	public GitlabConfiguration getGitConfiguration();

	public void createGitConfiguration(GitlabConfiguration gitConfiguration, boolean createGit);

	public void removeGitConfiguration();

	public void enableFeature(boolean enable);

	public void restorePlatform(RestorePlatformDTO restoreDTO,  RestoreReport report);

	public void generateSnapShot(String tagName, RestoreReport report);

	public RestoreReport saveFileChangesToEntity(SaveFileToEntityDTO saveFileToEntityDTO);

	public void reinitializeGitDir();

	public void syncGitAndDB();

	public boolean isTagValid(String tagName);

	public RestoreReport getReport(String executionId);

	public void commitSpecialVersionable(SpecialVersionable versionable, Object id, String commitMessage);

	public List<VersionableVO> versionablesVO();

	public List<String> getVersionableSimpleClassNames();

}

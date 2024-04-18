/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.project;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

public interface ProjectService {

	public Project createProject(ProjectDTO project);

	public void deleteProject(String projectId);

	public List<Project> getAllProjects();

	public Project getById(String id);

	public ProjectList getByIdForList(String id);

	public void updateProject(Project project);

	public void updateWithParameters(ProjectDTO project);

	public List<ProjectUserDTO> getProjectMembers(String projectId);

	public List<App> getAvailableRealmsForUser(String userId);

	public List<Project> getProjectsForUser(String userId);

	public void addUserToProject(String userId, String projectId);

	public void removeUserFromProject(String userId, String projectId);

	public void setRealm(String realmId, String projectId);

	public void unsetRealm(String realmId, String projectId);

	public void addWebProject(String webProjectId, String projectId, String userId);

	public void removeWebProject(String projectId);

	public boolean isUserInProject(String userId, String projecid);

	public Set<ProjectResourceAccess> getResourcesAccessesForUser(String projectId, String userId);

	public Set<ProjectResourceAccess> getResourcesAccessesForAppRole(String projectId, String name);

	public Set<OPResource> getResourcesForProjectAndUser(String projectId, String userId);

	public Set<OPResource> getResourcesForUser(String userId);

	public <T> Set<T> getResourcesForUserOfType(String userId, Class<T> clazz);

	public Set<AppRole> getProjectRoles(String projectId);

	public boolean isUserAuthorized(String projectId, String userId);

	public Project getByName(String identification);

	public ProjectList getByNameForList(String identification);

	public void deleteResourceFromProjects(String resourceId);

	public List<Project> getProjectsWithResource(String resourceId);

	public Set<ProjectResourceAccess> getAllResourcesAccesses(String projectId);

	public Set<User> getUsersInProject(String projectId);

	public Map<AppRole, Set<AppUser>> getAllRoleUsersInProject(Set<AppRole> projectRoles);

	boolean isUserInProjectWithoutOwner(String userId, String projectId);

}

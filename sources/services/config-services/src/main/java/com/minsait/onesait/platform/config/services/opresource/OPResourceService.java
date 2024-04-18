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
package com.minsait.onesait.platform.config.services.opresource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.base.OPResource.Resources;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

public interface OPResourceService {

	public Collection<OPResource> getResources(String userId, String identification);

	public void createUpdateAuthorization(ProjectResourceAccess projectResourceAccess);

	public OPResource getResourceById(String id);

	public void removeAuthorization(String id, String projectId, String userId) throws GenericOPException;

	public boolean hasAccess(String userId, String resourceId, ResourceAccessType access);

	public ResourceAccessType getResourceAccess(String userId, String resourceId);

	public Map<String, ResourceAccessType> getResourcesAccessMapByUserAndResourceIdList(User user,
			List<String> resourceIdList);

	public void insertAuthorizations(Set<ProjectResourceAccess> accesses);

	public boolean isResourceSharedInAnyProject(OPResource resource);

	public Collection<OPResource> getResourcesForUserAndType(User user, String type);

	public boolean isUserAuthorized(String userId, String resourceId);

	OPResource getResourceByIdentificationAndType(String identification, Resources type);

	List<String> createAuthorizations(String projectName, List<String> userIds, List<String> resources,
			List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes, String currentUser);

	List<String> deleteAuthorizations(String projectName, List<String> userIds, List<String> resources,
			List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes, String currentUser);

	void deleteUserAccess(ProjectResourceAccess projectUserAcc, Project project);

	void deleteUserAccess(String projectResourceAccessId);

	public List<String> createRealmAuthorizations(String projectId, List<String> realmIds, List<String> roleIds,
			List<String> resources, List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes,
			String currentUser);

	public List<String> deleteRealmAuthorizations(String projectId, List<String> realmIds, List<String> roleIds,
			List<String> resources, List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes,
			String currentUser);

	public Collection<OPResource> getResourcesByType(String userId, String type);

	public Collection<Versionable<?>> getResourcesVersionablesForUserAndType(User user, Class<?> type);

	public Collection<Versionable<?>> getResourcesVersionablesByType(User user, Class<?> type);

	public Collection<OPResource> getAllResourcesVersionablesVOs();

	OPResource getResourceByIdentification(String identification);

	List<ProjectResourceAccess> getProjectsByResource(OPResource resource);

}

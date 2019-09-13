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
package com.minsait.onesait.platform.config.services.opresource;

import java.util.Collection;
import java.util.Set;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

public interface OPResourceService {

	public Collection<OPResource> getResources(String userId, String identification);

	public void createUpdateAuthorization(ProjectResourceAccess projectResourceAccess);

	public OPResource getResourceById(String id);

	public void removeAuthorization(String id, String projectId, String userId) throws GenericOPException;

	public boolean hasAccess(String userId, String resourceId, ResourceAccessType access);

	public ResourceAccessType getResourceAccess(String userId, String resourceId);

	public void insertAuthorizations(Set<ProjectResourceAccess> accesses);

	public boolean isResourceSharedInAnyProject(OPResource resource);

	public Collection<OPResource> getResourcesForUserAndType(User user, String type);

	public boolean isUserAuthorized(String userId, String resourceId);
}

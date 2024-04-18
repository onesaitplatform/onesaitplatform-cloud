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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.ProjectUserAccess;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

public interface ProjectResourceAccessRepository extends JpaRepository<ProjectResourceAccess, String> {

	public ProjectResourceAccess findByResourceAndProjectAndUser(OPResource resource, Project project, User user);

	public ProjectResourceAccess findByResourceAndProjectAndAppRole(OPResource resource, Project project, AppRole role);

	public int countByResource(OPResource resource);

	@Transactional
	public void deleteByResource(OPResource resource);

	@Query("SELECT p FROM Project p INNER JOIN p.projectResourceAccesses pra WHERE pra.resource.id= :resourceId")
	public List<Project> findProjectsWithResourceId(@Param("resourceId") String resourceId);

	public List<ProjectResourceAccess> findByResource(OPResource resource);

	public List<ProjectResourceAccess> findByUser(User user);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.ProjectUserAccess(pra.resource.id, pra.access ) FROM com.minsait.onesait.platform.config.model.ProjectResourceAccess as pra WHERE pra.user = :user and pra.resource.id in :resourceIdList")
	public List<ProjectUserAccess> findUserAccessByUserAndResourceIds(@Param("user") User user, @Param("resourceIdList") List<String> resourceIdList);

	public List<ProjectResourceAccess> findByAppRole(AppRole role);
}

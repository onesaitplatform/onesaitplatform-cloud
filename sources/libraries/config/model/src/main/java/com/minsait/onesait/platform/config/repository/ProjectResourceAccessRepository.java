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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.ProjectUserAccess;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessVersioning;
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

	@Query("SELECT new com.minsait.onesait.platform.config.dto.ProjectUserAccess(pra.resource.id, pra.access ) FROM com.minsait.onesait.platform.config.model.ProjectResourceAccess as pra WHERE (pra.user = :user or pra IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) and pra.resource.id in :resourceIdList")
	public List<ProjectUserAccess> findUserAccessByUserAndResourceIds(@Param("user") User user,
			@Param("resourceIdList") List<String> resourceIdList);

	public List<ProjectResourceAccess> findByAppRole(AppRole role);

	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )"
			+ " AND (pra.resource.id= :resourceId AND pra.access= :accessType)")
	public List<ProjectResourceAccess> findByUserIdAndResourceIdAndAccessType(@Param("userId") String userId,
			@Param("resourceId") String resourceId, @Param("accessType") ResourceAccessType accessType);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )"
			+ " AND (pra.resource.identification= :identification)")
	public List<ProjectResourceAccessList> findByUserIdAndResourceId(@Param("userId") String userId,
			@Param("identification") String identification);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )")
	public List<ProjectResourceAccessList> findByUserId(@Param("userId") String userId);

	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )")
	public List<ProjectResourceAccess> findByUserIdAccess(@Param("userId") String userId);

	@Query("SELECT pra FROM ProjectResourceAccessVersioning pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessVersioning prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )")
	public List<ProjectResourceAccessVersioning> findByUserIdVersioning(@Param("userId") String userId);

	@Query("SELECT pra.resource FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )")
	public List<OPResource> findResourcesByUserId(@Param("userId") String userId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )"
			+ " AND (pra.resource.id= :resourceId)")
	public List<ProjectResourceAccessList> findByUserIdAndResourceIdentification(@Param("userId") String userId,
			@Param("resourceId") String resourceId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )"
			+ " AND (pra.resource.id= :resourceId)")
	public List<ProjectResourceAccessList> findByUserIdAndResourceIdAndAccessView(@Param("userId") String userId,
			@Param("resourceId") String resourceId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ) )"
			+ " AND (pra.resource.id= :resourceId AND pra.access='MANAGE')")
	public List<ProjectResourceAccessList> findByUserIdAndResourceIdAndAcessManage(@Param("userId") String userId,
			@Param("resourceId") String resourceId);

	@Query("SELECT prar FROM ProjectResourceAccess prar LEFT JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId "
			+ " AND prar.resource.id= :resourceId AND prar.access= :accessType")
	public List<ProjectResourceAccess> findByUserIdInRoleAndResourceIdAndAccessType(@Param("userId") String userId,
			@Param("resourceId") String resourceId, @Param("accessType") ResourceAccessType accessType);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE pra.project.id= :projectId AND pra.resource.id= :resourceId AND pra.user.userId= :userId")
	public ProjectResourceAccessList findByResourceListAndProjectAndUserId(@Param("resourceId") String resourceId,
			@Param("projectId") String projectId, @Param("userId") String userId);

	@Override
	@Transactional
	@Modifying
	@Query("delete from ProjectResourceAccess p where p.id = :id")
	void deleteById(@Param("id") String id);

	@Transactional
	@Modifying
	@Query("delete from ProjectResourceAccess p where p.project.id = :id")
	void deleteByProjectId(@Param("id") String id);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE pra.resource.id= :id")
	public ProjectResourceAccessList getResource_id(@Param("id") String id);

}

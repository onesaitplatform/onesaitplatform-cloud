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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE pra.resource.id= :#{#resource.id} AND pra.project.id= :#{#project.id} AND pra.appRole.id= :#{#role.id}")
	public ProjectResourceAccess findByResourceAndProjectAndAppRole(@Param("resource") OPResource resource,
			@Param("project") Project project, @Param("role") AppRole role);

	@Query("SELECT COUNT(pra) FROM ProjectResourceAccess pra WHERE pra.resource.id= :#{#resource.id}")
	public int countByResource(@Param("resource") OPResource resource);

	@Query("SELECT p FROM Project p INNER JOIN p.projectResourceAccesses pra WHERE pra.resource.id= :resourceId")
	public List<Project> findProjectsWithResourceId(@Param("resourceId") String resourceId);

	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE pra.resource.id= :#{#resource.id}")
	public List<ProjectResourceAccess> findByResource(@Param("resource") OPResource resource);

	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE (pra.user.userId= :#{#user.userId} OR pra.access_all=TRUE OR "
			+ "pra IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :#{#user.userId} ))")
	public List<ProjectResourceAccess> findByUser(@Param("user") User user);
	
	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE pra.project.id= :projectId AND pra.resource.id= :resourceId AND pra.access_all = TRUE")
	public ProjectResourceAccessList findByResourceAndProjectAndAccessALL(@Param("resourceId") String resourceId, @Param("projectId") String projectId);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.ProjectUserAccess(pra.resource.id, pra.access) FROM com.minsait.onesait.platform.config.model.ProjectResourceAccess as pra WHERE "
			+ "((pra.user = :user AND pra.resource.id in :resourceIdList) OR "
			+ "(pra IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE (au.user= :user OR prar.access_all = TRUE) AND prar.resource.id in :resourceIdList)) OR "
			+ "(pra IN (SELECT prapr FROM ProjectResourceAccessList prapr JOIN prapr.project.users pru WHERE (pru=:user OR prapr.access_all = TRUE) AND prapr.resource.id in :resourceIdList)))")
	public List<ProjectUserAccess> findUserAccessByUserAndResourceIds(@Param("user") User user,	@Param("resourceIdList") List<String> resourceIdList);

	public List<ProjectResourceAccess> findByAppRole(AppRole role);

	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE (pra.user.userId= :userId OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE (au.user.userId= :userId OR prar.access_all = TRUE)) )"
			+ " AND (pra.resource.id= :resourceId AND pra.access= :accessType)")
	public List<ProjectResourceAccess> findByUserIdAndResourceIdAndAccessType(@Param("userId") String userId,
			@Param("resourceId") String resourceId, @Param("accessType") ResourceAccessType accessType);
	
	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE "
			+ "((pra.user.userId= :userId AND pra.resource.id= :resourceId) OR "
			+ "(pra IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE (au.user.userId= :userId OR prar.access_all = TRUE) AND prar.resource.id= :resourceId)) OR "
			+ "(pra IN (SELECT prapr FROM ProjectResourceAccessList prapr JOIN prapr.project.users pru WHERE (pru.userId=:userId OR prapr.access_all = TRUE) AND prapr.resource.id= :resourceId)))")
	public List<ProjectResourceAccessList> findByUserIdAndResourceId(@Param("userId") String userId, @Param("resourceId") String resourceId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE (pra.user.userId= :userId OR pra.access_all=TRUE OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId ))")
	public List<ProjectResourceAccessList> findByUserId(@Param("userId") String userId);
	
	@Query("SELECT pra FROM ProjectResourceAccess pra WHERE ((pra.user.userId= :userId) OR "
			+ "(pra IN (SELECT prar FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId OR prar.access_all = TRUE )) OR "
			+ "(pra IN (SELECT prapr FROM ProjectResourceAccessList prapr JOIN prapr.project.users pru WHERE pru.userId=:userId OR prapr.access_all = TRUE)))")
	public List<ProjectResourceAccess> findByUserIdAccess(@Param("userId") String userId);
	
	@Query("SELECT pra FROM ProjectResourceAccessVersioning pra WHERE (pra.user.userId= :userId OR pra.accessAll=TRUE OR pra "
			+ "IN (SELECT prar FROM ProjectResourceAccessVersioning prar JOIN prar.appRole.appUsers au WHERE au.user.userId= :userId OR prar.accessAll = TRUE ) )")
	public List<ProjectResourceAccessVersioning> findByUserIdVersioning(@Param("userId") String userId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE pra.project.id= :projectId AND pra.resource.id= :resourceId AND (pra.user.userId= :userId OR pra.access_all=TRUE)")
	public ProjectResourceAccessList findByResourceListAndProjectAndUserId(@Param("resourceId") String resourceId, @Param("projectId") String projectId, @Param("userId") String userId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE ((pra.user.userId= :userId AND pra.resource.id= :resourceId) OR "
			+ "(pra IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE (au.user.userId= :userId OR prar.access_all = TRUE) AND prar.resource.id= :resourceId)) OR "
			+ "(pra IN (SELECT prapr FROM ProjectResourceAccessList prapr JOIN prapr.project.users pru WHERE (pru.userId=:userId OR prapr.access_all = TRUE) AND prapr.resource.id= :resourceId)))")
	public List<ProjectResourceAccessList> findByUserIdAndResourceIdOrALLAndAccessView(@Param("userId") String userId, @Param("resourceId") String resourceId);
	
	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE ((pra.user.userId= :userId AND pra.resource.id= :resourceId) OR "
			+ "(pra IN (SELECT prar FROM ProjectResourceAccessList prar JOIN prar.appRole.appUsers au WHERE (au.user.userId= :userId OR pra.access_all = TRUE) AND prar.access= 'MANAGE' AND prar.resource.id= :resourceId)) OR "
			+ "(pra IN (SELECT prapr FROM ProjectResourceAccessList prapr JOIN prapr.project.users pru WHERE (pru.userId=:userId OR prapr.access_all = TRUE) AND prapr.access= 'MANAGE' AND prapr.resource.id= :resourceId)))")
	public List<ProjectResourceAccessList> findByUserIdAndResourceIdOrALLAndAccessManage(@Param("userId") String userId, @Param("resourceId") String resourceId);
	
	@Override
	@Transactional
	@Modifying
	@Query("delete from ProjectResourceAccess p where p.id = :id")
	void deleteById(@Param("id") String id);

	@Transactional
	@Modifying
	@Query("delete from ProjectResourceAccess p where p.project.id = :id")
	void deleteByProjectId(@Param("id") String id);
	
	@Transactional
	@Modifying
	@Query("DELETE FROM ProjectResourceAccess pra WHERE pra.project.id = :projectId AND  pra.resource.id= :resourceId")
	void deleteByProjectIdAndResourceId(@Param("projectId") String projectId, @Param("resourceId") String resourceId);

	@Query("SELECT pra FROM ProjectResourceAccessList pra WHERE pra.resource.id= :id")
	public ProjectResourceAccessList getResource_id(@Param("id") String id);

}

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.DashboardForList;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;
import com.minsait.onesait.platform.config.model.User;

public interface DashboardRepositoryPageable extends PagingAndSortingRepository<Dashboard, String>{

	List<Dashboard> findByIdentificationOrTypeEquals(String identification, DashboardType type, Pageable pageable);
	List<Dashboard> findByIdentificationAndTypeEquals(String identification, DashboardType type);
	
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "LOWER(o.identification) LIKE LOWER('%' || :identification || '%') AND  (o.type = :typeDashboard or o.type is null) ")
	List<DashboardForList> findByIdentificationDashboarContainingAndType(@Param("identification") String identification,
			@Param("typeDashboard") Dashboard.DashboardType typeDashboard,Pageable pageable);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "LOWER(o.identification) LIKE LOWER('%' || :identification || '%') AND  o.type = :typeDashboard")
	List<DashboardForList> findByIdentificationSynopticsContainingAndType(@Param("identification") String identification,
			@Param("typeDashboard") Dashboard.DashboardType typeDashboard, Pageable pageable);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(LOWER(o.identification) LIKE LOWER('%' || :identification || '%') AND (o.type = :type or o.type is null)) ")
	
	List<DashboardForList> findByUserAndPermissionsDashboards(@Param("user") User user,
			@Param("identification") String identification, @Param("type") Dashboard.DashboardType type, Pageable pageable);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(LOWER(o.identification) LIKE LOWER('%' || :identification || '%') AND o.type = :type )")
	
	List<DashboardForList> findByUserAndPermissionsSynoptics(@Param("user") User user,
			@Param("identification") String identification, @Param("type") Dashboard.DashboardType type, Pageable pageable);
	
	@Query("SELECT COUNT(*) "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "LOWER(o.identification) LIKE LOWER('%' || :identification || '%')  AND  (o.type = :typeDashboard or o.type is null)")
	Integer countByIdentificationDashboardContainingAndType(@Param("identification") String identification,
			@Param("typeDashboard") Dashboard.DashboardType typeDashboard);
	
	@Query("SELECT COUNT(*) "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "LOWER(o.identification) LIKE LOWER('%' || :identification || '%')  AND  o.type = :typeDashboard")
	Integer countByIdentificationSynopticsContainingAndType(@Param("identification") String identification,
			@Param("typeDashboard") Dashboard.DashboardType typeDashboard);
	
	@Query("SELECT COUNT(*) "
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(LOWER(o.identification) LIKE LOWER('%' || :identification || '%')  AND (o.type = :type or o.type is null)) ")
	
	Integer countByUserAndPermissionsDashboards(@Param("user") User user,
			@Param("identification") String identification, @Param("type") Dashboard.DashboardType type);
	
	@Query("SELECT COUNT(*) "
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(LOWER(o.identification) LIKE LOWER('%' || :identification || '%')  AND o.type = :type )")
	
	Integer countByUserAndPermissionsSynoptycs(@Param("user") User user,
			@Param("identification") String identification, @Param("type") Dashboard.DashboardType type);
	
	@Override
	Page<Dashboard> findAll(Pageable pageable);

}

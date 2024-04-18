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

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.DashboardForList;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.User;

public interface DashboardRepository extends JpaRepository<Dashboard, String> {

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE " + "o.id = :id")
	DashboardForList findForListById(@Param("id") String id);

	List<Dashboard> findByUser(User user);

	List<Dashboard> findByUserOrderByIdentificationAsc(User user);

	List<Dashboard> findByUserOrderByIdentificationDesc(User user);

	List<Dashboard> findByUserOrderByCreatedAtAsc(User user);

	List<Dashboard> findByUserOrderByCreatedAtDesc(User user);

	List<Dashboard> findByUserOrderByUpdatedAtAsc(User user);

	List<Dashboard> findByUserOrderByUpdatedAtDesc(User user);

	List<Dashboard> findByIdentification(String identification);

	List<Dashboard> findByDescription(String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "o.identification like %:identification% AND o.description like %:description% ORDER BY o.identification ASC")
	List<DashboardForList> findByIdentificationContainingAndDescriptionContaining(
			@Param("identification") String identification, @Param("description") String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "o.identification like %:identification% AND  o.type = :type ORDER BY o.identification ASC")
	List<DashboardForList> findByIdentificationContainingAndType(@Param("identification") String identification,
			@Param("type") Dashboard.DashboardType type);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image,  o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "o.identification like %:identification% AND ( o.type = :type or o.type is null ) ORDER BY o.identification ASC")
	List<DashboardForList> findByIdentificationContainingAndTypeOrNull(@Param("identification") String identification,
			@Param("type") Dashboard.DashboardType type);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "o.identification like %:identification% AND o.type = null OR o.type = 'DASHBOARD' ORDER BY o.identification ASC")
	List<DashboardForList> findDashboardByIdentificationContainingAndType(
			@Param("identification") String identification);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') "
			+ "FROM Dashboard AS o " + "WHERE "
			+ "o.identification like %:identification% ORDER BY o.identification ASC")
	List<DashboardForList> findByIdentificationContainingFofList(@Param("identification") String identification);

	List<Dashboard> findByIdentificationContaining(String identification);

	List<Dashboard> findByDescriptionContaining(String description);

	List<Dashboard> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	List<Dashboard> findByUserAndIdentificationContaining(User user, String identification);

	List<Dashboard> findByUserAndDescriptionContaining(User user, String description);

	List<Dashboard> findAllByOrderByIdentificationAsc();

	@Query("SELECT o.identification FROM Dashboard AS o ORDER BY o.identification ASC")
	List<String> findAllIdentificationsByOrderByIdentificationAsc();

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, o.description, o.createdAt, o.updatedAt, o.user, 'DASHBOARD', 0) FROM Dashboard AS o WHERE (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<OPResourceDTO> findAllDto(@Param("identification") String identification,
			@Param("description") String description);

	List<Dashboard> findAllByOrderByIdentificationDesc();
	
	List<Dashboard> findAllByOrderByCreatedAtAsc();
	
	List<Dashboard> findAllByOrderByCreatedAtDesc();
	
	List<Dashboard> findAllByOrderByUpdatedAtAsc();
	
	List<Dashboard> findAllByOrderByUpdatedAtDesc();

	List<Dashboard> findByIdentificationAndDescriptionAndUser(String identification, String description, User user);

	List<Dashboard> findByIdentificationAndDescription(String identification, String description);
	
	Dashboard findByIdentificationOrId(String identification, String id);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<DashboardForList> findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(
			@Param("user") User user, @Param("identification") String identification,
			@Param("description") String description);

	@Query("SELECT o.identification FROM Dashboard AS o " + "WHERE (o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo "
			+ "WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<String> findIdentificationsByUserAndPermissions(@Param("user") User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, o.description, o.createdAt, o.updatedAt, o.user, 'DASHBOARD', 0)"
			+ "FROM Dashboard AS o " + "WHERE (o.user=:user OR " + "o.id IN (SELECT uo.dashboard.id "
			+ "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user)) AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<OPResourceDTO> findDtoByUserAndPermissions(@Param("user") User user,
			@Param("identification") String identification, @Param("description") String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(o.identification like %:identification% AND o.type = :type) ORDER BY o.identification ASC")
	List<DashboardForList> findByUserAndPermissionsANDIdentificationContainingAndTypeForList(@Param("user") User user,
			@Param("identification") String identification, @Param("type") Dashboard.DashboardType type);
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(o.identification like %:identification% AND (o.type = :type or o.type is null)) ORDER BY o.identification ASC")
	
	List<DashboardForList> findByUserAndPermissionsANDIdentificationContainingAndTypeForListOrNull(@Param("user") User user,
			@Param("identification") String identification, @Param("type") Dashboard.DashboardType type);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user )) AND "
			+ "(o.identification like %:identification% AND o.type = null) ORDER BY o.identification ASC")
	List<DashboardForList> findDashboardByUserAndPermissionsANDIdentificationContaining(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.image, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')"
			+ "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user) OR "
			+ "o.id IN (SELECT pra.resource.id " + "FROM ProjectResourceAccess AS pra " + "WHERE pra.user=:user) OR "
			+ "o.id IN (SELECT prar.resource.id FROM ProjectResourceAccess prar JOIN prar.appRole.appUsers au WHERE au.user= :user)) AND "
			+ "(o.identification like %:identification%) ORDER BY o.identification ASC")
	List<DashboardForList> findByUserAndPermissionsANDIdentificationContaining(@Param("user") User user,
			@Param("identification") String identification);

	long countByIdentification(String identification);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.identification ASC")
	List<Dashboard> findByUserPermissionOrderByIdentificationAsc(@Param("user") User user);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.identification ASC")
	List<Dashboard> findByUserPermission(@Param("user") User user);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.identification DESC")
	List<Dashboard> findByUserPermissionOrderByIdentificationDesc(@Param("user") User user);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.createdAt ASC")
	List<Dashboard> findByUserPermissionOrderByCreatedAtAsc(@Param("user") User user);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.createdAt DESC")
	List<Dashboard> findByUserPermissionOrderByCreatedAtDesc(@Param("user") User user);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.updatedAt ASC")
	List<Dashboard> findByUserPermissionOrderByUpdatedAtAsc(@Param("user") User user);

	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.updatedAt DESC")
	List<Dashboard> findByUserPermissionOrderByUpdatedAtDesc(@Param("user") User user);

	@Modifying
	@Transactional
	@Query("UPDATE Dashboard d SET d.model = :model WHERE d.id = :id")
	void saveModel(@Param("model") String model, @Param("id") String id);

	@Modifying
	@Transactional
	@Query("UPDATE Dashboard d SET d.headerlibs = :headerlibs WHERE d.id = :id")
	void saveHeaderLibs(@Param("headerlibs") String headerlibs, @Param("id") String id);

	Dashboard findByUserAndIdentification(User user, String identification);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM DASHBOARD_USER_ACCESS", nativeQuery = true)
	void deleteUserAccesess();

	@Modifying
	@Transactional
	@Query("DELETE FROM Dashboard AS p WHERE p.id NOT IN :ids")
	void deleteByIdNotInCustom(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	default void deleteByIdNotIn(Collection<String> ids) {
		deleteUserAccesess();
		deleteByIdNotInCustom(ids);
	}

}

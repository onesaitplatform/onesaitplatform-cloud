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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.DashboardForList;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.User;

public interface DashboardRepository extends JpaRepository<Dashboard, String> {

	Dashboard findById(String id);

	List<Dashboard> findByUser(User user);

	List<Dashboard> findByUserOrderByIdentificationAsc(User user);

	List<Dashboard> findByUserOrderByIdentificationDesc(User user);

	List<Dashboard> findByUserOrderByCreatedAtAsc(User user);

	List<Dashboard> findByUserOrderByCreatedAtDesc(User user);

	List<Dashboard> findByUserOrderByUpdatedAtAsc(User user);

	List<Dashboard> findByUserOrderByUpdatedAtDesc(User user);

	List<Dashboard> findByIdentification(String identification);

	List<Dashboard> findByDescription(String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.isPublic, o.createdAt, o.updatedAt, 'EDIT') " + "FROM Dashboard AS o " + "WHERE "
			+ "o.identification like %:identification% AND o.description like %:description% ORDER BY o.identification ASC")
	List<DashboardForList> findByIdentificationContainingAndDescriptionContaining(@Param("identification") String identification, @Param("description") String description);

	List<Dashboard> findByIdentificationContaining(String identification);

	List<Dashboard> findByDescriptionContaining(String description);

	List<Dashboard> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	List<Dashboard> findByUserAndIdentificationContaining(User user, String identification);

	List<Dashboard> findByUserAndDescriptionContaining(User user, String description);

	List<Dashboard> findAllByOrderByIdentificationAsc();

	List<Dashboard> findByIdentificationAndDescriptionAndUser(String identification, String description, User user);

	List<Dashboard> findByIdentificationAndDescription(String identification, String description);
	
	@Query("SELECT new  com.minsait.onesait.platform.config.dto.DashboardForList(o.id, o.identification, o.description, o.type, o.user, o.isPublic, o.createdAt, o.updatedAt, 'EDIT')" + "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user)) AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<DashboardForList> findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(
			@Param("user") User user, @Param("identification") String identification,
			@Param("description") String description);

	long countByIdentification(String identification);
	
	
	@Query("SELECT o FROM Dashboard AS o  WHERE (o.isPublic=TRUE OR o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id  FROM DashboardUserAccess AS uo  WHERE uo.user=:user)) "
			+ " ORDER BY o.identification ASC")	
	List<Dashboard> findByUserPermissionOrderByIdentificationAsc(@Param("user") User user);

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
			+ " ORDER BY o.updatedAt ASC")	
	List<Dashboard> findByUserPermissionOrderByUpdatedAtDesc(@Param("user") User user);
}

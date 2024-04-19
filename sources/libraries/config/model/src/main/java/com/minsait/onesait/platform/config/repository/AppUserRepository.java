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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserListOauth;
import com.minsait.onesait.platform.config.model.User;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

	@Query("SELECT ur FROM AppUser AS ur WHERE ur.user.userId = :user and ur.role.app.identification = :identification")
	List<AppUser> findByUserId(@Param("user") String user, @Param("identification") String identification);

	@Query("SELECT ur FROM AppUser AS ur WHERE ur.user.userId = :user and ur.role.app.identification = :id")
	List<AppUser> findByUserAndIdentification(@Param("user") String user, @Param("id") String id);

	List<AppUser> findByUser(User user);

	@Query("SELECT ur FROM AppUserListOauth AS ur WHERE ur.user.userId = :user and ur.role.app.identification = :id")
	List<AppUserListOauth> findAppUserListByUserAndIdentification(@Param("user") String user, @Param("id") String id);

	@Query("SELECT ur FROM AppUserListOauth AS ur WHERE ur.user.userId = :user and ur.role.app.id = :appId and ur.role.id= :roleId" )
	List<AppUserListOauth> findAppUserListOauthByUserAndAppIdAndRoleName(@Param("user") String user, @Param("appId") String appId, @Param("roleId") String roleId);

	@Query("SELECT ur FROM AppUserListOauth AS ur WHERE ur.user.userId = :user")
	List<AppUserListOauth> findAppUserListByUser(@Param("user") String user);

	@Query("SELECT ur FROM AppUserListOauth AS ur WHERE ur.role.app.identification = :identification")
	List<AppUserListOauth> findAppUserListByAppIdentification(@Param("identification") String identification);

	@Query("SELECT ur FROM AppUserListOauth AS ur WHERE ur.role.app.identification = :identification AND ur.user.userId LIKE :userIdLike")
	List<AppUserListOauth> findAppUserListByAppIdentificationAndUserIdLike(@Param("identification") String identification, @Param("userIdLike") String userIdLike);

	@Query("SELECT count(ur) FROM AppUserListOauth AS ur WHERE ur.role.app.identification = :identification")
	long countAppUserListByAppIdentification(@Param("identification") String identification);

	@Query("SELECT ur.role.name FROM AppUserList ur WHERE (ur.user.userId= :userId AND ur.role.app.identification= :appIdentification)")
	List<String> findRoleNamesByUserIdAndAppIdentification(@Param("userId") String userId, @Param("appIdentification") String appIdentification);

	@Transactional
	@Modifying
	@Query("DELETE FROM AppUser a WHERE a.id = :userApp")
	void deleteByQuery(@Param("userApp") String userApp);

	@Transactional
	@Modifying
	@Query("DELETE FROM AppUser a WHERE a.user.userId = :userId")
	void deleteByUserId(@Param("userId") String userId);

	@Transactional
	@Modifying
	@Query("DELETE FROM AppUser a WHERE a.role.id = :appRoleId AND a.user.userId= :userId")
	void deleteAppUserByAppRoleAndUserId(@Param("appRoleId") String appRoleId, @Param("userId") String userId);

	@Query("SELECT aul FROM AppUserListOauth aul WHERE aul.user.userId= :userId AND aul.role.name= :role AND aul.role.app.identification= :appIdentification")
	List<AppUserListOauth> findAppUserByUserIdAndRoleAndApp(@Param("userId") String userId, @Param("role") String role,@Param("appIdentification") String appIdentification);

	@Query("SELECT aul FROM AppUserListOauth aul WHERE aul.user.userId= :userId AND aul.role.app.identification= :appIdentification")
	List<AppUserListOauth> findAppUserByUserIdAndApp(@Param("userId") String userId,@Param("appIdentification") String appIdentification);

	@Transactional
	@Modifying
	@Query("DELETE FROM AppUser a WHERE a.id = :id")
	void deleteAppUserById(@Param("id") String id);

	@Query("SELECT ur.role.app.identification FROM AppUserList AS ur WHERE ur.user.userId = :user")
	List<String> findAppListListByUser(@Param("user") String user);


}
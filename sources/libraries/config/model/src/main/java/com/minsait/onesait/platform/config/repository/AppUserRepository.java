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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserList;
import com.minsait.onesait.platform.config.model.User;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

	@Query("SELECT ur FROM AppUser AS ur WHERE ur.user.userId = :user and ur.role.app.identification = :identification")
	List<AppUser> findByUserId(@Param("user") String user, @Param("identification") String identification);

	@Query("SELECT ur FROM AppUser AS ur WHERE ur.user.userId = :user and ur.role.app.identification = :id")
	List<AppUser> findByUserAndIdentification(@Param("user") String user, @Param("id") String id);

	List<AppUser> findByUser(User user);

	@Query("SELECT ur FROM AppUserList AS ur WHERE ur.user.userId = :user and ur.role.app.identification = :id")
	List<AppUserList> findAppUserListByUserAndIdentification(@Param("user") String user, @Param("id") String id);

	@Transactional
	@Modifying
	@Query("DELETE FROM AppUser a WHERE a.id = :userApp")
	void deleteByQuery(@Param("userApp") String userApp);

}
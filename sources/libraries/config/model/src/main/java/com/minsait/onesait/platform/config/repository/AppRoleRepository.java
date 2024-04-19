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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppRoleListOauth;

public interface AppRoleRepository extends JpaRepository<AppRole, String> {

	@Query("SELECT arl FROM AppRoleList arl WHERE id IN (SELECT roles.id FROM AppRole ar JOIN ar.childRoles roles WHERE ar.id= :id )")
	List<AppRoleList> findChildAppRoleListById(@Param("id") String id);

	@Query("SELECT arl FROM AppRoleListOauth arl WHERE id IN (SELECT roles.id FROM AppRoleListOauth ar JOIN ar.childRoles roles WHERE ar.id= :id )")
	List<AppRoleListOauth> findChildAppRoleListOauthById(@Param("id") String id);

	@Query("SELECT arl FROM AppRoleList arl WHERE arl.app.identification= :appIdentification")
	List<AppRoleList> findAppRoleListByAppIdentification(@Param("appIdentification") String appIdentification);

	@Query("SELECT arl FROM AppRoleListOauth arl WHERE arl.app.identification= :appIdentification")
	List<AppRoleListOauth> findAppRoleListOauthByAppIdentification(@Param("appIdentification") String appIdentification);

	@Query("SELECT arl FROM AppRoleList arl WHERE arl.app.identification= :appIdentification AND arl.name= :name")
	List<AppRoleList> findAppRoleListByAppIdentificationAndRoleName(
			@Param("appIdentification") String appIdentification, @Param("name") String name);

	@Query("SELECT arl FROM AppRoleListOauth arl WHERE arl.app.identification= :appIdentification AND arl.name= :name")
	List<AppRoleListOauth> findAppRoleListOauthByAppIdentificationAndRoleName(
			@Param("appIdentification") String appIdentification, @Param("name") String name);

	@Query("SELECT o FROM AppRoleList AS o WHERE (o.id = :id)")
	AppRoleList findAppRoleListById(@Param("id") String id);


	@Query("SELECT o FROM AppRoleListOauth As o")
	List<AppRoleListOauth> findAllRolesList();

}

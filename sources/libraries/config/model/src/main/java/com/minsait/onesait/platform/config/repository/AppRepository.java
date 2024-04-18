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

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface AppRepository extends JpaRepository<App, String> {

	@Query("SELECT o FROM App AS o WHERE (o.identification = :identification) ORDER BY o.identification ASC")
	App findByIdentification(@Param("identification") String identification);

	@Query("SELECT o FROM AppList AS o WHERE (o.identification like %:identification%) ORDER BY o.identification ASC")
	List<AppList> findByIdentificationLike(@Param("identification") String identification);

	@Query("SELECT o FROM App AS o WHERE o.identification=:identification")
	App findByIdentificationEquals(@Param("identification") String identification);

	@Query("SELECT o FROM AppList AS o WHERE (o.user=:user AND o.identification like %:identification%) ORDER BY o.identification ASC")
	List<AppList> findByUserANDIdentification(@Param("user") User user, @Param("identification") String identification);

	@Query("SELECT o FROM AppList As o")
	List<AppList> findAllList();

	@Query("SELECT o FROM AppList AS o WHERE (o.identification = :identification) ORDER BY o.identification ASC")
	AppList findAppListByIdentification(@Param("identification") String identification);

	@Query("SELECT o FROM AppList AS o WHERE (o.id = :id)")
	AppList findAppListById(@Param("id") String id);

	@Query("SELECT r.name FROM AppList o JOIN o.appRoles r WHERE o.id= :id")
	List<String> findRolesListByAppId(@Param("id") String id);

	@Query("SELECT c.identification FROM AppList o JOIN o.childApps c WHERE (o.identification = :identification)")
	List<String> findChildAppsList(@Param("identification") String identification);

	List<App> findByUser(User user);

	@Query("SELECT o FROM App As o where o.user=:user")
	List<App> findAppsByUser(@Param("user") User user);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM APP_ASSOCIATED WHERE APP_ASSOCIATED.PARENT_APP NOT IN :ids OR APP_ASSOCIATED.CHILD_APP NOT IN :ids", nativeQuery = true)
	void deleteAppAssociatedWhereIdNotIn(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM APP_ASSOCIATED_ROLES", nativeQuery = true)
	void deleteAppAssociatedRoles();

	@Modifying
	@Transactional
	@Query("DELETE FROM App AS p WHERE p.id NOT IN :ids")
	void deleteByIdNotInCustom(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	default void deleteByIdNotIn(Collection<String> ids) {
		deleteAppAssociatedWhereIdNotIn(ids);
		deleteAppAssociatedRoles();
		deleteByIdNotInCustom(ids);
	}

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.identification, o.id, 'App') FROM App AS o")
	public List<VersionableVO> findVersionableViews();

}
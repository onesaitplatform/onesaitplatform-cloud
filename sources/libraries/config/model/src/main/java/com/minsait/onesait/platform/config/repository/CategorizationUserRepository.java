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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.config.model.User;

public interface CategorizationUserRepository extends JpaRepository<CategorizationUser, String> {

	List<CategorizationUser> findByCategorization(Categorization categorization);

	@Query("SELECT o FROM CategorizationUser AS o WHERE o.user!=:user AND o.categorization=:categorization")
	List<CategorizationUser> findByCategorizationNotOwn(@Param("user") User user,
			@Param("categorization") Categorization categorization);

	@Query("SELECT o FROM CategorizationUser AS o WHERE o.user=:user")
	List<CategorizationUser> findByUserAndAuth(@Param("user") User user);

	@Query("SELECT o FROM CategorizationUser AS o WHERE o.user=:user AND o.categorization=:categorization")
	CategorizationUser findByUserAndCategorization(@Param("user") User user,
			@Param("categorization") Categorization categorization);

	@Query("SELECT o FROM CategorizationUser AS o WHERE o.user=:user AND o.active=TRUE")
	List<CategorizationUser> findByUserAndActive(@Param("user") User user);
	
	@Query("SELECT cu FROM CategorizationUser AS cu WHERE ((cu.user=:user) OR ((cu.user!=:user) AND cu.authorizationType='OWNER' AND cu.categorization NOT IN (SELECT cuadm.categorization FROM CategorizationUser AS cuadm WHERE (cuadm.user=:user))))")
	List<CategorizationUser> findAllOwnerAndAuth(@Param("user") User user);

	@SuppressWarnings("unchecked")
	@Override
	CategorizationUser save(CategorizationUser entity);

}

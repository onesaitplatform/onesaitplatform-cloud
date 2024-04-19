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

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.GadgetFavorite;
import com.minsait.onesait.platform.config.model.User;

public interface GadgetFavoriteRepository extends JpaRepository<GadgetFavorite, String> {

	List<GadgetFavorite> findByUser(User user);

	List<GadgetFavorite> findByType(String type);

	List<GadgetFavorite> findByTypeOrderByIdentificationAsc(String type);

	List<GadgetFavorite> findByUserAndIdentificationContaining(User user, String identification);

	List<GadgetFavorite> findByUserAndTypeContaining(User user, String type);

	List<GadgetFavorite> findByIdentificationAndTypeAndUser(String identification, String type, User user);

	List<GadgetFavorite> findByIdentificationAndType(String identification, String type);

	List<GadgetFavorite> findAllByOrderByIdentificationAsc();

	List<GadgetFavorite> findByIdentificationContainingAndTypeContaining(String identification, String type);

	List<GadgetFavorite> findByIdentificationContaining(String identification);

	List<GadgetFavorite> findByTypeContaining(String type);

	List<GadgetFavorite> findByUserAndIdentificationContainingAndTypeContaining(User user, String identification,
			String type);

	GadgetFavorite findByIdentification(String identification);

	@Query("SELECT g " + "FROM GadgetFavorite AS g "
			+ "WHERE (g.user=:user AND g.type=:type)  ORDER BY g.identification ASC")
	List<GadgetFavorite> findByUserAndType(@Param("user") User user, @Param("type") String type);

	@Query("SELECT g " + "FROM GadgetFavorite AS g "
			+ "WHERE ( g.identification=:identification)  ORDER BY g.identification ASC")
	List<GadgetFavorite> existByIdentification(@Param("identification") String identification);

	@Query("SELECT g " + "FROM GadgetFavorite AS g "
			+ "WHERE (g.user=:user AND g.identification=:identification)  ORDER BY g.identification ASC")
	List<GadgetFavorite> existByIdentificationUser(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT distinct(g.type) FROM GadgetFavorite AS g ORDER BY g.type")
	List<String> findGadgetFavoriteTypes();

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}

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

import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.model.User;

public interface MapsStyleRepository extends JpaRepository<MapsStyle, String> {

	@SuppressWarnings("unchecked")
	@Override
	MapsStyle save(MapsStyle style);

	@Override
	void delete(MapsStyle style);

	public List<MapsStyle> findByIdentification(String identification);

	@Query("SELECT o FROM MapsStyle AS o Where o.id = :id")
	public List<MapsStyle> findByIdForList(@Param("id") String id);

	@Query("SELECT o FROM MapsStyle AS o Where o.identification = :identification")
	public List<MapsStyle> findByIdentificationForList(@Param("identification") String identification);

	List<MapsStyle> findByUser(User user);

	@Query("SELECT o  FROM MapsStyle AS o  WHERE  o.user=:user AND o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsStyle> findByUserIdentificationContaining(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT o  FROM MapsStyle AS o  WHERE   o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsStyle> findByIdentificationContaining(@Param("identification") String identification);

	@Modifying
	@Transactional
	@Query("DELETE FROM MapsStyle AS p WHERE p.id= :id")
	void deleteByIdCustom(@Param("id") String id);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}

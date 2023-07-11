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

import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.model.User;

public interface MapsLayerRepository extends JpaRepository<MapsLayer, String> {

	@SuppressWarnings("unchecked")
	@Override
	MapsLayer save(MapsLayer style);

	@Override
	void delete(MapsLayer style);

	public List<MapsLayer> findByIdentification(String identification);

	@Query("SELECT o FROM MapsLayer AS o Where o.id = :id")
	public List<MapsLayer> findByIdForList(@Param("id") String id);

	@Query("SELECT o FROM MapsLayer AS o Where o.identification = :identification")
	public List<MapsLayer> findByIdentificationForList(@Param("identification") String identification);

	List<MapsLayer> findByUser(User user);

	@Query("SELECT o  FROM MapsLayer AS o  WHERE  o.user=:user AND o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsLayer> findByUserIdentificationContaining(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT o  FROM MapsLayer AS o  WHERE   o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsLayer> findByIdentificationContaining(@Param("identification") String identification);

	@Modifying
	@Transactional
	@Query("DELETE FROM MapsLayer AS p WHERE p.id= :id")
	void deleteByIdCustom(@Param("id") String id);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}

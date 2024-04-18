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

import com.minsait.onesait.platform.config.model.MapsProject;
import com.minsait.onesait.platform.config.model.User;

public interface MapsProjectRepository extends JpaRepository<MapsProject, String> {

	@SuppressWarnings("unchecked")
	@Override
	MapsProject save(MapsProject project);

	@Override
	void delete(MapsProject project);

	public List<MapsProject> findByIdentification(String identification);

	@Query("SELECT o FROM MapsProject AS o Where o.id = :id")
	public List<MapsProject> findByIdForList(@Param("id") String id);

	@Query("SELECT o FROM MapsProject AS o Where o.identification = :identification")
	public List<MapsProject> findByIdentificationForList(@Param("identification") String identification);

	List<MapsProject> findByUser(User user);

	@Query("SELECT o  FROM MapsProject AS o  WHERE  o.user=:user OR o.isPublic=TRUE ORDER BY o.identification ASC")
	List<MapsProject> findByUserOrPublic(@Param("user") User user);

	@Query("SELECT o  FROM MapsProject AS o  WHERE  (o.user=:user OR o.isPublic=TRUE) AND o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsProject> findByUserIdentificationContainingOrPublic(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT o  FROM MapsProject AS o  WHERE  o.user=:user AND o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsProject> findByUserIdentificationContaining(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT o  FROM MapsProject AS o  WHERE   o.identification like %:identification% ORDER BY o.identification ASC")
	List<MapsProject> findByIdentificationContaining(@Param("identification") String identification);

	@Modifying
	@Transactional
	@Query("DELETE FROM MapsProject AS p WHERE p.id= :id")
	void deleteByIdCustom(@Param("id") String id);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}

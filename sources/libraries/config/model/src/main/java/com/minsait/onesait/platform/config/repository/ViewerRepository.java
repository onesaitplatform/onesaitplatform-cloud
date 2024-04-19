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

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;

public interface ViewerRepository extends JpaRepository<Viewer, String> {

	List<Viewer> findByIdentification(String identification);

	List<Viewer> findByDescription(String description);

	List<Viewer> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Viewer> findByIdentificationContaining(String identification);

	List<Viewer> findByDescriptionContaining(String description);

	List<Viewer> findAllByOrderByIdentificationAsc();

	List<Viewer> findByIdentificationAndDescription(String identification, String description);

	List<Viewer> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	List<Viewer> findByIsPublicTrueOrUser(User user);

	List<Viewer> findByUserOrderByIdentificationAsc(User user);

	@Modifying
	@Transactional
	@Query("DELETE FROM Viewer AS p WHERE p.id NOT IN :ids")
	void deleteByIdNotInCustom(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM LAYER_VIEWER", nativeQuery = true)
	void deleteLayerViewer();

	@Modifying
	@Transactional
	default void deleteByIdNotIn(Collection<String> ids) {
		deleteLayerViewer();
		deleteByIdNotInCustom(ids);
	}
}

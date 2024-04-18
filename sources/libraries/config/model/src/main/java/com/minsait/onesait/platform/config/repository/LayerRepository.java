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

import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.User;

public interface LayerRepository extends JpaRepository<Layer, String> {

	List<Layer> findByIdentification(String identification);

	List<Layer> findByDescription(String description);

	List<Layer> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Layer> findByIdentificationContaining(String identification);

	List<Layer> findByDescriptionContaining(String description);

	List<Layer> findAllByOrderByIdentificationAsc();

	@Query("select c.identification from Layer as c order by c.identification asc")
	List<String> findIdentificationOrderByIdentificationAsc();

	List<Layer> findByIdentificationAndDescription(String identification, String description);

	List<Layer> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	List<Layer> findByUserOrIsPublicTrue(User user);

	@Query("select c.identification from Layer as c where c.user=:user or c.isPublic=TRUE order by c.identification asc")
	List<String> findIdentificationByUserOrIsPublicTrue(@Param("user") User user);

	List<Layer> findByUserOrderByIdentificationAsc(User user);

	@Query("SELECT o.identification FROM Layer AS o where o.ontology.identification=:ontology ORDER BY o.identification ASC")
	List<String> findIdentificationByOntology(@Param("ontology") String ontology);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}

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

import com.minsait.onesait.platform.config.model.OntologyAI;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface OntologyAIRepository extends JpaRepository<OntologyAI, String>{

	@Query("SELECT oai FROM OntologyAI AS oai WHERE oai.ontology.user=:user")
	List<OntologyAI> findByUser(@Param("user") User user);

	OntologyAI findByPredictor(String predictor);

	@Transactional
	@Modifying
	void deleteByPredictor(String predictor);

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.predictor, o.id, 'OntologyAI') FROM OntologyAI AS o")
	public List<VersionableVO> findVersionableViews();

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.User;

public interface OntologyUserAccessRepository extends JpaRepository<OntologyUserAccess, String> {

	OntologyUserAccess findByOntologyAndUser(Ontology ontology, User user);

	List<OntologyUserAccess> findByUser(User user);

	List<OntologyUserAccess> findByUserAndOntologyUserAccessType(User user,
			OntologyUserAccessType ontologyUserAccessType);

	OntologyUserAccess findById(String id);

	List<OntologyUserAccess> findByOntologyUserAccessType(OntologyUserAccessType ontologyUserAccessType);

	List<OntologyUserAccess> findByOntology(Ontology ontology);

	@Override

	<S extends OntologyUserAccess> S save(S entity);

	@Modifying
	@Transactional

	void deleteByOntology(Ontology ontology);

}

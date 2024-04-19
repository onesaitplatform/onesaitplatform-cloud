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
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyDataAccess;
import com.minsait.onesait.platform.config.model.User;

public interface OntologyDataAccessRepository extends JpaRepository<OntologyDataAccess, String> {

	@SuppressWarnings("unchecked")
	@Override
	public OntologyDataAccess save(OntologyDataAccess entity);

	public List<OntologyDataAccess> findByOntology(Ontology ontology);

	public Optional<OntologyDataAccess> findById(String id);
	
	@Query("SELECT o FROM OntologyDataAccess AS o WHERE (o.ontology=:ontology AND o.user =:user)")
	public OntologyDataAccess findByOntologyAndUser(@Param("ontology") Ontology ontology, @Param("user") User user);
	
	@Query("SELECT o FROM OntologyDataAccess AS o WHERE (o.ontology=:ontology AND o.appRole =:role)")
	public OntologyDataAccess findByOntologyAndRole(@Param("ontology") Ontology ontology, @Param("role") AppRole role);
	
	@Query("SELECT o FROM OntologyDataAccess AS o WHERE (o.ontology.identification=:ontology AND (o.user.userId =:user OR o.appRole.id IN (SELECT au.role.id FROM AppUser au WHERE au.user.userId =:user)))")
	public List<OntologyDataAccess> findUserAccessByOntologyAndUser(@Param("ontology") String ontology, @Param("user") String user);

	@Query("SELECT o FROM OntologyDataAccess AS o WHERE (o.user.userId =:user OR o.appRole.id IN (SELECT au.role.id FROM AppUser au WHERE au.user.userId =:user))")
	public List<OntologyDataAccess> findUserAccessByUser(@Param("user") String user);
	
	public List<OntologyDataAccess> findAllByOntologyAndUser(String ontology, String user);
	
}

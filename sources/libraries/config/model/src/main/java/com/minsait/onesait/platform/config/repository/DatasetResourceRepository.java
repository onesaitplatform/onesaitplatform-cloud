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

import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;

public interface DatasetResourceRepository extends JpaRepository<DatasetResource, String> {
	
	@Query("SELECT o  "
			+ "FROM DatasetResource AS o "
			+ "WHERE o.id IN (:ids) ")
	List<DatasetResource> findResourcesByIdsList(@Param("ids") List<String> ids);
	
	@Query("SELECT  o  " 
			+ "FROM DatasetResource AS o " 
			+ "WHERE o.id = :id ")
	DatasetResource findResourceById(@Param("id") String id);
	
	List<DatasetResource> findByOntology(Ontology ontology);
	
	@Query("SELECT o.identification FROM DatasetResource AS o where o.ontology.identification=:ontology ORDER BY o.identification ASC")
    List<String> findIdentificationByOntology(@Param("ontology") String ontology);

}

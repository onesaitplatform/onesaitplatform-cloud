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
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;

public interface OntologyTimeSeriesWindowRepository extends JpaRepository<OntologyTimeSeriesWindow, String> {

	Set<OntologyTimeSeriesWindow> findByOntologyTimeSeries(OntologyTimeSeries ontology);

	void deleteByOntologyTimeSeries(OntologyTimeSeries ontology);

	@Override
	void deleteById(String id);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = { "OntologyTimeSeriesWindowRepositoryByOntologyIdentification" }, allEntries = true)
	OntologyTimeSeriesWindow save(OntologyTimeSeriesWindow entity);

	@Modifying
	@Query("delete from OntologyTimeSeriesWindow o where o.id = :id")
	@CacheEvict(cacheNames = { "OntologyTimeSeriesWindowRepositoryByOntologyIdentification" }, allEntries = true)
	void deleteByMyId(@Param("id") String id);

	@Query("SELECT o FROM OntologyTimeSeriesWindow AS o WHERE o.ontologyTimeSeries.ontology.identification=:identification")
	@Cacheable(cacheNames = "OntologyTimeSeriesWindowRepositoryByOntologyIdentification", unless = "#result==null or #result.size()==0")
	List<OntologyTimeSeriesWindow> findByOntologyIdentificaton(@Param("identification") String identification);

}

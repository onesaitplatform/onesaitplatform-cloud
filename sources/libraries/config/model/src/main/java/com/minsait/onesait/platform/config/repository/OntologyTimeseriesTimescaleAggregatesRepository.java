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

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;

public interface OntologyTimeseriesTimescaleAggregatesRepository extends JpaRepository<OntologyTimeseriesTimescaleAggregates, String> {

	Set<OntologyTimeseriesTimescaleAggregates> findByOntologyTimeSeries(OntologyTimeSeries ontology);

	void deleteByOntologyTimeSeries(OntologyTimeSeries ontology);

	@Override
	void deleteById(String id);
	
	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = { "OntologyTimeseriesTimescaleAggregatesRepositoryByOntologyIdentification" }, allEntries = true)
	OntologyTimeseriesTimescaleAggregates save(OntologyTimeseriesTimescaleAggregates entity);
	
	@Modifying
	@Query("delete from OntologyTimeseriesTimescaleAggregates o where o.id = :id")
	@CacheEvict(cacheNames = { "OntologyTimeseriesTimescaleAggregatesRepositoryByOntologyIdentification" }, allEntries = true)
	void deleteByMyId(@Param("id") String id);
	
	@Query("SELECT o FROM OntologyTimeseriesTimescaleAggregates AS o WHERE o.ontologyTimeSeries.ontology.identification=:identification")
	@Cacheable(cacheNames = "OntologyTimeseriesTimescaleAggregatesRepositoryByOntologyIdentification", unless = "#result==null or #result.size()==0")
	List<OntologyTimeseriesTimescaleAggregates> findByOntologyIdentificaton(@Param("identification") String identification);
	

	@Query("SELECT o FROM OntologyTimeseriesTimescaleAggregates AS o WHERE o.ontologyTimeSeries.id=:timeSeriesOntologyId and o.name=:name")
	@Cacheable(cacheNames = "OntologyTimeseriesTimescaleAggregatesRepositoryByOntologyIdentification", unless = "#result==null")
	OntologyTimeseriesTimescaleAggregates findByNameAndTimeSeriesOntologyId(@Param("name") String name, @Param("timeSeriesOntologyId") String timeSeriesOntologyId);
	
	@Cacheable(cacheNames = "OntologyTimeseriesTimescaleAggregatesRepositoryByOntologyIdentification", unless = "#result==null")
	OntologyTimeseriesTimescaleAggregates findByIdentification(String identification);
}

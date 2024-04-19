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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface OntologyTimeSeriesRepository extends JpaRepository<OntologyTimeSeries, String> {

	@Query("SELECT o FROM OntologyTimeSeries AS o WHERE o.ontology.user=:user")
	List<OntologyTimeSeries> findByUser(@Param("user") User user);

	List<OntologyTimeSeries> findByOntology(Ontology ontology);

	@CacheEvict(cacheNames = { "IsOntologyTimeSeriesByIdentification",
	"OntologyRepositoryByIdentification" }, allEntries = true, beforeInvocation = true)
	void deleteByOntology(Ontology ontology);

	@Override
	@CacheEvict(cacheNames = { "IsOntologyTimeSeriesByIdentification" }, allEntries = true)
	void deleteById(String id);

	@Query("SELECT o FROM OntologyTimeSeries AS o WHERE o.ontology.identification=:identification")
	OntologyTimeSeries findByOntologyIdentificaton(@Param("identification") String identification);

	@Cacheable(cacheNames = "IsOntologyTimeSeriesByIdentification", unless = "#result == null")
	default Boolean isTimeSeries(String ontologyIdentification) {
		final OntologyTimeSeries result = findByOntologyIdentificaton(ontologyIdentification);
		return result != null;

	}

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = { "IsOntologyTimeSeriesByIdentification",
			"OntologyTimeSeriesPropertyRepositoryByOntologyIdentification","OntologyTimeSeriesWindowRepositoryByOntologyIdentification" }, allEntries = true)
	OntologyTimeSeries save(OntologyTimeSeries entity);

	@Modifying
	@Transactional
	@CacheEvict(cacheNames = { "IsOntologyTimeSeriesByIdentification",
			"OntologyTimeSeriesPropertyRepositoryByOntologyIdentification","OntologyTimeSeriesWindowRepositoryByOntologyIdentification" }, allEntries = true)
	void deleteByIdNotIn(Collection<String> ids);

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.ontology.identification, o.id, 'OntologyTimeSeries') FROM OntologyTimeSeries AS o")
	public List<VersionableVO> findVersionableViews();

}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleProperties;

public interface OntologyTimeseriesTimescalePropetiesRepository extends JpaRepository<OntologyTimeseriesTimescaleProperties, String> {

	/*/@Query("SELECT o FROM OntologyTimeseriesTimescaleProperties AS o WHERE o.ontology.identification=:identification")
	@Cacheable(cacheNames = "OntologyTimeSeriesTimescalePropertyRepositoryByOntologyIdentification", unless = "#result==null or #result.size()==0")
	OntologyTimeseriesTimescaleProperties findByOntologyIdentificaton(@Param("identification") String identification);
	
	@SuppressWarnings("unchecked")
	@Modifying
	@Override
	@CacheEvict(cacheNames = { "OntologyTimeSeriesTimescalePropertyRepositoryByOntologyIdentification" }, allEntries = true)
	OntologyTimeseriesTimescaleProperties save(OntologyTimeseriesTimescaleProperties entity);


	@CacheEvict(cacheNames = { "OntologyTimeSeriesTimescalePropertyRepositoryByOntologyIdentification" }, allEntries = true)
	void deleteByOntologyTimeSeries(OntologyTimeSeries ontology);
	

	@CacheEvict(cacheNames = { "OntologyTimeSeriesTimescalePropertyRepositoryByOntologyIdentification" }, allEntries = true)
	void delete(OntologyTimeseriesTimescaleProperties timescaleProperties);
*/
}

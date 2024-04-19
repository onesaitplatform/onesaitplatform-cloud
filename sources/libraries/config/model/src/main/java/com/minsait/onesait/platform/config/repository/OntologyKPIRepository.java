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
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface OntologyKPIRepository extends JpaRepository<OntologyKPI, String> {

	@Override
	@Cacheable(cacheNames = "OntologyKPIRepository", unless = "#result == null")
	Optional<OntologyKPI> findById(String id);

	@Cacheable(cacheNames = "OntologyKPIRepositoryByUser", unless = "#result == null", key = "#p0.userId")
	List<OntologyKPI> findByUser(User user);

	@Cacheable(cacheNames = "OntologyKPIRepositoryByOntology", unless = "#result == null", key = "#p0.id")
	List<OntologyKPI> findByOntology(Ontology ontology);

	@Cacheable(cacheNames = "OntologyKPIRepositoryByJobName", unless = "#result == null", key = "#p0")
	OntologyKPI findByJobName(String jobName);

	@CacheEvict(cacheNames = { "OntologyKPIRepository", "OntologyKPIRepositoryByUser",
			"OntologyKPIRepositoryByOntology", "OntologyKPIRepositoryByJobName", "OntologyRepository",
	"OntologyRepositoryByIdentification" }, allEntries = true)
	@Transactional
	void deleteByOntology(Ontology ontology);

	@Override
	@CacheEvict(cacheNames = { "OntologyKPIRepository", "OntologyKPIRepositoryByUser",
			"OntologyKPIRepositoryByOntology", "OntologyKPIRepositoryByJobName", "OntologyRepository",
	"OntologyRepositoryByIdentification" }, allEntries = true)
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = { "OntologyKPIRepository", "OntologyKPIRepositoryByUser",
			"OntologyKPIRepositoryByOntology", "OntologyKPIRepositoryByJobName", "OntologyRepository",
	"OntologyRepositoryByIdentification" }, allEntries = true)
	@Transactional
	void delete(OntologyKPI entity);

	@Override
	@CacheEvict(cacheNames = { "OntologyKPIRepository", "OntologyKPIRepositoryByUser",
			"OntologyKPIRepositoryByOntology", "OntologyKPIRepositoryByJobName", "OntologyRepository",
	"OntologyRepositoryByIdentification" }, allEntries = true)
	OntologyKPI save(OntologyKPI datamodel);

	@Override
	@CacheEvict(cacheNames = { "OntologyKPIRepository", "OntologyKPIRepositoryByUser",
			"OntologyKPIRepositoryByOntology", "OntologyKPIRepositoryByJobName", "OntologyRepository",
	"OntologyRepositoryByIdentification" }, allEntries = true)
	void flush();

	@Modifying
	@Transactional
	@CacheEvict(cacheNames = { "OntologyKPIRepository", "OntologyKPIRepositoryByUser",
			"OntologyKPIRepositoryByOntology", "OntologyKPIRepositoryByJobName", "OntologyRepository",
	"OntologyRepositoryByIdentification" }, allEntries = true)
	void deleteByIdNotIn(Collection<String> ids);

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.ontology.identification, o.id, 'OntologyKPI') FROM OntologyKPI AS o")
	public List<VersionableVO> findVersionableViews();
}

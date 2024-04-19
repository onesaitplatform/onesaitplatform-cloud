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

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.minsait.onesait.platform.config.model.QueryTemplate;

public interface QueryTemplateRepository extends JpaRepository<QueryTemplate, String> {

	@Cacheable(cacheNames = "QueryTemplateRepositoryByName", unless = "#result == null", key = "#p0")
	public QueryTemplate findByName(String name);
	
	@Cacheable(cacheNames = "QueryTemplateRepositoryByNameContaining", unless = "#result == null", key = "#p0")
	public List<QueryTemplate> findByNameContaining(String name);

	@Cacheable(cacheNames = "QueryTemplateRepositoryByOntologyIdentification", unless = "#result == null", key = "#p0")
	public List<QueryTemplate> findByOntologyIdentification(String ontology);

	public List<QueryTemplate> findByOntologyIdentificationIsNull();

	@Override
	@Cacheable(cacheNames = "QueryTemplateRepositoryById", unless = "#result == null", key = "#p0")
	public Optional<QueryTemplate> findById(String id);

	@Override
	@CacheEvict(cacheNames = { "QueryTemplateRepositoryByOntologyIdentification", "QueryTemplateRepositoryByName",
			"QueryTemplateRepositoryById" }, allEntries = true)
	@Modifying
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = { "QueryTemplateRepositoryByOntologyIdentification", "QueryTemplateRepositoryByName",
			"QueryTemplateRepositoryById" }, allEntries = true)
	@Modifying
	@Transactional
	void delete(QueryTemplate entity);

	@Override
	@CacheEvict(cacheNames = { "QueryTemplateRepositoryByOntologyIdentification", "QueryTemplateRepositoryByName",
			"QueryTemplateRepositoryById" }, allEntries = true)
	QueryTemplate save(QueryTemplate datamodel);

	@Override
	@CacheEvict(cacheNames = { "QueryTemplateRepositoryByOntologyIdentification", "QueryTemplateRepositoryByName",
			"QueryTemplateRepositoryById" }, allEntries = true)
	void flush();

}

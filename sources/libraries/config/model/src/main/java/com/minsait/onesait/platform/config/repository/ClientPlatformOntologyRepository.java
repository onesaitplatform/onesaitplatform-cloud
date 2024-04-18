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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;

public interface ClientPlatformOntologyRepository extends JpaRepository<ClientPlatformOntology, String> {

	@Cacheable(cacheNames = "ClientPlatformOntologyRepositoryByOntologyAndClientPlatform", unless = "#result == null")
	@Query("SELECT o " + "FROM ClientPlatformOntology AS o " + "WHERE o.ontology.identification = :ontologId AND "
			+ "o.clientPlatform.identification = :clientPlatformId")
	ClientPlatformOntology findByOntologyAndClientPlatform(@Param("ontologId") String ontologId,
			@Param("clientPlatformId") String clientPlatformId);

	@Cacheable(cacheNames = "ClientPlatformOntologyRepository", unless = "#result==null or #result.size()==0", key = "#p0.id")
	List<ClientPlatformOntology> findByClientPlatform(ClientPlatform clientPlatform);
	
	@Cacheable(cacheNames = "ClientPlatformOntologyRepositoryByClientPlatformAndInsertAccess", unless = "#result == null")
	@Query("SELECT o " + "FROM ClientPlatformOntology AS o " + "WHERE o.access != 'QUERY' AND "
			+ "o.clientPlatform.identification = :clientPlatformId")
	List<ClientPlatformOntology> findByClientPlatformAndInsertAccess(@Param("clientPlatformId") String clientPlatformId);

	@Cacheable(cacheNames = "ClientPlatformOntologyRepository", unless = "#result==null or #result.size()==0", key = "#p0.id")
	List<ClientPlatformOntology> findById(String id);

	@Cacheable(cacheNames = "ClientPlatformOntologyRepositoryByOntology", unless = "#result==null or #result.size()==0", key = "#p0.id")
	List<ClientPlatformOntology> findByOntology(Ontology ontology);

	@Transactional
	@CacheEvict(cacheNames = { "ClientPlatformOntologyRepository",
			"ClientPlatformOntologyRepositoryByOntologyAndClientPlatform",
			"ClientPlatformOntologyRepositoryByOntology" }, allEntries = true)
	@Modifying
	void deleteByOntology(Ontology ontology);

	@Transactional
	@CacheEvict(cacheNames = { "ClientPlatformOntologyRepository",
			"ClientPlatformOntologyRepositoryByOntologyAndClientPlatform",
			"ClientPlatformOntologyRepositoryByOntology" }, allEntries = true)
	@Modifying
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = { "ClientPlatformOntologyRepository",
			"ClientPlatformOntologyRepositoryByOntologyAndClientPlatform",
			"ClientPlatformOntologyRepositoryByOntology" }, allEntries = true)
	ClientPlatformOntology save(ClientPlatformOntology entity);

	@Override
	@CacheEvict(cacheNames = { "ClientPlatformOntologyRepository",
			"ClientPlatformOntologyRepositoryByOntologyAndClientPlatform",
			"ClientPlatformOntologyRepositoryByOntology" }, allEntries = true)
	@Modifying
	@Transactional
	void delete(ClientPlatformOntology entity);

	@Override
	@CacheEvict(cacheNames = { "ClientPlatformOntologyRepository",
			"ClientPlatformOntologyRepositoryByOntologyAndClientPlatform",
			"ClientPlatformOntologyRepositoryByOntology" }, allEntries = true)
	@Modifying
	@Transactional
	void delete(String id);

	@Override
	@CacheEvict(cacheNames = { "ClientPlatformOntologyRepository",
			"ClientPlatformOntologyRepositoryByOntologyAndClientPlatform",
			"ClientPlatformOntologyRepositoryByOntology" }, allEntries = true)
	void flush();

}

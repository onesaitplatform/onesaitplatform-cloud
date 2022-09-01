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
package com.minsait.onesait.platform.multitenant.config.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.Vertical;

public interface VerticalRepository extends JpaRepository<Vertical, String> {

	public static final String VERTICAL_REPOSITORY_SCHEMA = "VerticalRepositorySchema";
	public static final String VERTICAL_REPOSITORY = "VerticalRepository";

	@Cacheable(cacheNames = VERTICAL_REPOSITORY, unless = "#result == null")
	Vertical findByName(String name);

	@Query("SELECT c FROM Vertical c WHERE c.name=:value OR c.schema=:value")
	@Cacheable(cacheNames = VERTICAL_REPOSITORY, unless = "#result == null")
	Vertical findByNameOrSchema(@Param("value") String value);

	@Cacheable(cacheNames = VERTICAL_REPOSITORY, unless = "#result == null")
	Vertical findBySchema(String schema);

	@Override
	@CacheEvict(cacheNames = { VERTICAL_REPOSITORY_SCHEMA, VERTICAL_REPOSITORY }, allEntries = true)
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = { VERTICAL_REPOSITORY_SCHEMA, VERTICAL_REPOSITORY }, allEntries = true)
	@Transactional
	void delete(Vertical vertical);

	@Override
	@CacheEvict(cacheNames = { VERTICAL_REPOSITORY_SCHEMA, VERTICAL_REPOSITORY }, allEntries = true)
	void flush();

	@Override
	@CacheEvict(cacheNames = { VERTICAL_REPOSITORY_SCHEMA, VERTICAL_REPOSITORY }, allEntries = true)
	<S extends Vertical> S saveAndFlush(S entity);

	@Override
	@CacheEvict(cacheNames = { VERTICAL_REPOSITORY_SCHEMA, VERTICAL_REPOSITORY,
			MasterUserRepository.MASTER_USER_REPOSITORY, MasterUserRepositoryLazy.MASTER_USER_REPOSITORY_LAZY,
			TenantRepositoryLazy.TENANT_LAZY_REPOSITORY , TenantRepository.TENANT_REPOSITORY}, allEntries = true)
	<S extends Vertical> S save(S entity);

	@Override
	@CacheEvict(cacheNames = { VERTICAL_REPOSITORY_SCHEMA, VERTICAL_REPOSITORY }, allEntries = true)
	@Transactional
	void deleteAll();

	@Query("SELECT c.schema FROM Vertical c WHERE c.name=:value OR c.schema=:value")
	@Cacheable(cacheNames = VERTICAL_REPOSITORY_SCHEMA, unless = "#result == null")
	String findSchemaByNameOrSchema(@Param("value") String value);

}

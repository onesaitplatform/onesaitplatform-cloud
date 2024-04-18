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
package com.minsait.onesait.platform.multitenant.config.repository;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;

public interface TenantRepository extends JpaRepository<Tenant, String> {
	public static final String TENANT_REPOSITORY = "TenantRepository";

	List<Tenant> findByVerticalsIn(List<Vertical> verticals);

	@Cacheable(cacheNames = TENANT_REPOSITORY, unless = "#result == null", key = "#p0")
	Tenant findByName(String name);

	@Override
	@CacheEvict(cacheNames = TENANT_REPOSITORY, key = "#p0.name")
	@Transactional
	void delete(Tenant entity);

	@Override
	@CacheEvict(cacheNames = TENANT_REPOSITORY, allEntries = true)
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = TENANT_REPOSITORY, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = TENANT_REPOSITORY, key = "#p0.name", unless = "#result == null")
	<S extends Tenant> S saveAndFlush(S entity);

	@Override
	@CachePut(cacheNames = TENANT_REPOSITORY, key = "#p0.name", unless = "#result == null")
	@CacheEvict(cacheNames = VerticalRepository.VERTICAL_REPOSITORY, allEntries = true)
	<S extends Tenant> S save(S entity);

	@Override
	@CacheEvict(cacheNames = TENANT_REPOSITORY, allEntries = true)
	@Transactional
	void deleteAll();

	@Query("SELECT size(t.users) FROM Tenant t WHERE t.name= :tenantName ")
	long countUsersByTenantName(@Param("tenantName") String tenantName);

}

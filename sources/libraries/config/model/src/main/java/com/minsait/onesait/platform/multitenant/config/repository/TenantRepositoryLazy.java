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
package com.minsait.onesait.platform.multitenant.config.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.multitenant.config.model.TenantLazy;

public interface TenantRepositoryLazy extends JpaRepository<TenantLazy, String> {

	public static final String TENANT_LAZY_REPOSITORY = "TenantLazyRepository";

	@Override
	@CacheEvict(cacheNames = TENANT_LAZY_REPOSITORY, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = TENANT_LAZY_REPOSITORY, key = "#p0.name", unless = "#result == null")
	<S extends TenantLazy> S saveAndFlush(S entity);

	@Override
	@CachePut(cacheNames = TENANT_LAZY_REPOSITORY, key = "#p0.name", unless = "#result == null")
	<S extends TenantLazy> S save(S entity);

	@Query("SELECT t FROM TenantLazy t WHERE t.name= :tenantName ")
	@Cacheable(cacheNames = TENANT_LAZY_REPOSITORY, unless = "#result == null", key = "#p0")
	TenantLazy findLazyByName(@Param("tenantName") String tenantName);

}

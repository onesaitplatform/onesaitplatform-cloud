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

import static com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository.MASTER_USER_REPOSITORY;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;
public interface MasterUserRepositoryLazy extends JpaRepository<MasterUserLazy, String> {

	public static final String MASTER_USER_REPOSITORY_LAZY = "MasterUserRepositoryLazy";

	@Modifying
	@Transactional
	@Query("DELETE FROM MasterUserLazy u WHERE u.userId= :userId")
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, key="{#p0.toLowerCase()}")
	void deleteByUserId(@Param("userId") String userId);


	@Cacheable(cacheNames=MASTER_USER_REPOSITORY_LAZY, unless = "#result == null", key="{#p0.toLowerCase()}")
	@Query("SELECT u FROM MasterUserLazy u WHERE u.userId= :userId")
	public MasterUserLazy findByUserId(@Param("userId") String userId);

	@Override
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, key="{#p0.toLowerCase()}")
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = MASTER_USER_REPOSITORY_LAZY, key = "{#p0.userId.toLowerCase()}", unless = "#result == null")
	@CacheEvict(cacheNames = { MASTER_USER_REPOSITORY}, key = "{#p0.userId.toLowerCase()}")
	<S extends MasterUserLazy> S saveAndFlush(S entity);

	@Override
	@CachePut(cacheNames = MASTER_USER_REPOSITORY_LAZY, key = "{#p0.userId.toLowerCase()}", unless = "#result == null")
	@CacheEvict(cacheNames = { MASTER_USER_REPOSITORY}, key = "{#p0.userId.toLowerCase()}")
	<S extends MasterUserLazy> S save(S entity);

	@Override
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, key = "{#p0.userId.toLowerCase()}")
	void delete(MasterUserLazy entity);


}

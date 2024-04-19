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

import static com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepositoryLazy.MASTER_USER_REPOSITORY_LAZY;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;

public interface MasterUserRepository extends JpaRepository<MasterUser, String> {
	public static final String MASTER_USER_REPOSITORY = "MasterUserRepository";

	@Cacheable(cacheNames = MASTER_USER_REPOSITORY, unless = "#result == null", key="{#p0.toLowerCase()}")
	public MasterUser findByUserId(String userId);

	@Override
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, key="{#p0.toLowerCase()}")
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = MASTER_USER_REPOSITORY, key = "{#p0.userId.toLowerCase()}", unless = "#result == null")
	@CacheEvict(cacheNames = { MASTER_USER_REPOSITORY_LAZY}, key = "{#p0.userId.toLowerCase()}")
	<S extends MasterUser> S saveAndFlush(S entity);

	@Override
	@CachePut(cacheNames = MASTER_USER_REPOSITORY, key = "{#p0.userId.toLowerCase()}", unless = "#result == null")
	@CacheEvict(cacheNames = { MASTER_USER_REPOSITORY_LAZY}, key = "{#p0.userId.toLowerCase()}")
	<S extends MasterUser> S save(S entity);

	@Override
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, key = "{#p0.userId.toLowerCase()}")
	void delete(MasterUser entity);

	@Query("SELECT u FROM MasterUser u WHERE u.tenant.id IN (SELECT t.id FROM Tenant t JOIN t.verticals v WHERE v.schema = :vertical OR v.name = :vertical)")
	public List<MasterUser> findByVertical(@Param("vertical") String vertical);

	@Query("SELECT u FROM MasterUser u WHERE u.tenant.id IN (SELECT t.id FROM Tenant t JOIN t.verticals v WHERE v.schema = :vertical OR v.name = :vertical) AND u.active = :active")
	public List<MasterUser> findByVerticalAndActive(@Param("vertical") String vertical,
			@Param("active") boolean active);

	@Transactional
	@Modifying
	@Query("UPDATE MasterUser u SET u.password= :newPass WHERE u.password= :oldPass AND u.userId= :userId")
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, key="{#p0.toLowerCase()}")
	int updateMasterUserPassword(@Param("userId") String userId, @Param("oldPass") String oldPass,
			@Param("newPass") String newPass);

	@Transactional
	@Modifying
	@Query("UPDATE MasterUser u SET u.password= :newPass WHERE u.password= :oldPass")
	@CacheEvict(cacheNames = {MASTER_USER_REPOSITORY_LAZY, MASTER_USER_REPOSITORY}, allEntries = true)
	int updateMasterUserPassword(@Param("oldPass") String oldPass, @Param("newPass") String newPass);


	@Query("SELECT u FROM MasterUserLazy u WHERE u.userId= :userId")
	public MasterUserLazy findLazyByUserId(@Param("userId") String userId);

}

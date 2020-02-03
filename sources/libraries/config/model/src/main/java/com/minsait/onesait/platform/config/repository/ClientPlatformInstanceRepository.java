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

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;

public interface ClientPlatformInstanceRepository extends JpaRepository<ClientPlatformInstance, String> {

	@Override
	@CacheEvict(cacheNames = "ClientPlatformInstanceRepository", allEntries = true)
	<S extends ClientPlatformInstance> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "ClientPlatformInstanceRepository", allEntries = true)
	void flush();

	@Override
	@CacheEvict(cacheNames = "ClientPlatformInstanceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	<S extends ClientPlatformInstance> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "ClientPlatformInstanceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	ClientPlatformInstance save(ClientPlatformInstance entity);

	@Modifying
	@CacheEvict(cacheNames = "DeviceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	@Query("DELETE ClientPlatformInstance d WHERE d= :clientPlatformInstance")
	@Transactional
	void deleteById(@Param("clientPlatformInstance") ClientPlatformInstance clientPlatformInstance);

	@Override
	List<ClientPlatformInstance> findAll();

	@Override
	@CacheEvict(cacheNames = "ClientPlatformInstanceRepository", allEntries = true)
	@Transactional
	void deleteAll();

	List<ClientPlatformInstance> findByClientPlatform(ClientPlatform clientPlatform);

	@Cacheable(cacheNames = "ClientPlatformInstanceRepository", key = "#a0.identification.concat('-').concat(#a1)", unless = "#result == null")
	ClientPlatformInstance findByClientPlatformAndIdentification(ClientPlatform clientPlatform, String identification);

	@Modifying
	@Query("UPDATE ClientPlatformInstance c SET c.connected = :connected, c.disabled = :disabled WHERE c.updatedAt < :date")
	int updateClientPlatformInstanceStatusByUpdatedAt(@Param("connected") boolean connected,
			@Param("disabled") boolean disabled, @Param("date") Date date);

	@Modifying
	@Query("UPDATE ClientPlatformInstance c SET c.identification= :identification, c.clientPlatform= :clientPlatform, c.sessionKey = :sessionKey, c.protocol = :protocol, c.location = :location, c.updatedAt = :updatedAt, c.status = :status, c.connected = :connected, c.disabled = :disabled WHERE c.id = :id")
	int updateClientPlatformInstance(@Param("clientPlatform") ClientPlatform clientPlatform,
			@Param("identification") String identification, @Param("sessionKey") String sessionKey,
			@Param("protocol") String protocol, @Param("location") double[] location,
			@Param("updatedAt") Date updatedAt, @Param("status") String status, @Param("connected") boolean connected,
			@Param("disabled") boolean disabled, @Param("id") String id);

	ClientPlatformInstance findById(String id);

}

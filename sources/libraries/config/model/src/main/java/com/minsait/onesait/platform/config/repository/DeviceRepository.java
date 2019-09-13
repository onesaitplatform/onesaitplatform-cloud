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
import com.minsait.onesait.platform.config.model.Device;

public interface DeviceRepository extends JpaRepository<Device, String> {

	@Override
	@CacheEvict(cacheNames = "DeviceRepository", allEntries = true)
	<S extends Device> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = "DeviceRepository", allEntries = true)
	void flush();

	@Override
	@CacheEvict(cacheNames = "DeviceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	<S extends Device> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "DeviceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	Device save(Device entity);

	@Modifying
	@CacheEvict(cacheNames = "DeviceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	@Query("DELETE Device d WHERE d= :device")
	@Transactional
	void deleteById(@Param("device") Device device);

	@Override
	List<Device> findAll();

	@Override
	@CacheEvict(cacheNames = "DeviceRepository", allEntries = true)
	@Transactional
	void deleteAll();

	List<Device> findByClientPlatform(ClientPlatform clientPlatform);

	@Cacheable(cacheNames = "DeviceRepository", key = "#a0.identification.concat('-').concat(#a1)", unless = "#result == null")
	Device findByClientPlatformAndIdentification(ClientPlatform clientPlatform, String identification);

	@Modifying
	@Query("UPDATE Device d SET d.connected = :connected, d.disabled = :disabled WHERE d.updatedAt < :date")
	int updateDeviceStatusByUpdatedAt(@Param("connected") boolean connected, @Param("disabled") boolean disabled,
			@Param("date") Date date);

	@Modifying
	@Query("UPDATE Device d SET d.identification= :identification, d.clientPlatform= :clientPlatform,d.sessionKey = :sessionKey, d.protocol = :protocol, d.location = :location, d.updatedAt = :updatedAt, d.status = :status,d.connected = :connected, d.disabled = :disabled WHERE d.id = :id")
	int updateDevice(@Param("clientPlatform") ClientPlatform clientPlatform,
			@Param("identification") String identification, @Param("sessionKey") String sessionKey,
			@Param("protocol") String protocol, @Param("location") double[] location,
			@Param("updatedAt") Date updatedAt, @Param("status") String status, @Param("connected") boolean connected,
			@Param("disabled") boolean disabled, @Param("id") String id);

	Device findById(String id);

}

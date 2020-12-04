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
package com.minsait.onesait.platform.multitenant.config.repository;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;

public interface IoTSessionRepository extends JpaRepository<IoTSession, String> {

	public static final String SESSIONS_REPOSITORY = "IoTSessionRepository";

	@Override
	@CacheEvict(cacheNames = SESSIONS_REPOSITORY, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = SESSIONS_REPOSITORY, key = "#p0.sessionKey")
	<S extends IoTSession> S saveAndFlush(S entity);

	@Override
	@CachePut(cacheNames = SESSIONS_REPOSITORY, key = "#p0.sessionKey")
	<S extends IoTSession> S save(S entity);

	@Override
	@CacheEvict(cacheNames = SESSIONS_REPOSITORY, key = "#p0.sessionKey")
	@Transactional
	void delete(IoTSession id);

	@CacheEvict(cacheNames = SESSIONS_REPOSITORY, key = "#p0")
	@Transactional
	void deleteBySessionKey(String sessionKey);

	// @Override
	@Override
	@CacheEvict(cacheNames = SESSIONS_REPOSITORY, allEntries = true)
	@Transactional
	void deleteById(String id);

	@Override
	List<IoTSession> findAll();

	@Override
	@CacheEvict(cacheNames = SESSIONS_REPOSITORY, allEntries = true)
	@Transactional
	void deleteAll();

	@Cacheable(cacheNames = SESSIONS_REPOSITORY, unless = "#result == null", key = "#p0")
	IoTSession findBySessionKey(String sessionKey);
	
	
	//the method calling this one must deal with the cache updated
		//for example, a method that returns the session can use this annotation: @CachePut(cacheNames = IoTSessionRepository.SESSIONS_REPOSITORY, key = "#p0.sessionKey", unless = "#result == null")
		//where the key is the sessionkey of the session passed as parameter.
	@Transactional
	@Modifying
	@Query("UPDATE IoTSession i SET "
			+ "i.sessionKey = :sessionKey, "
			+ "i.clientPlatform = :clientPlatform, "
			+ "i.clientPlatformID = :clientPlatformID, "
			+ "i.device = :device, "
			+ "i.token = :token, "
			+ "i.userID = :userID, "
			+ "i.userName = :userName, "
			+ "i.expiration = :expiration, "
			+ "i.lastAccess = :lastAccess,"
			+ "i.updatedAt = :updatedAt "
		+  "WHERE i.id = :id AND i.updatedAt < :updatedAt" )
	public int updateSession(
			@Param("id") String id, 
			@Param("sessionKey") String sessionkey, 
			@Param("clientPlatform") String clientPlatform, 
			@Param("clientPlatformID") String clientPlatformID, 
			@Param("device") String device, 
			@Param("token") MasterDeviceToken token, 
			@Param("userID") String userID, 
			@Param("userName") String userName, 
			@Param("expiration") long expiration, 
			@Param("lastAccess") ZonedDateTime lastAccess, 
			@Param("updatedAt") Date updatedAt);
	

}

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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.IoTSession;

public interface IoTSessionRepository extends JpaRepository<IoTSession, String> {

	@Override
	@CacheEvict(cacheNames = "IoTSessionRepository", allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = "IoTSessionRepository", key = "#p0.sessionKey")
	<S extends IoTSession> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CachePut(cacheNames = "IoTSessionRepository", key = "#p0.sessionKey")
	IoTSession save(IoTSession entity);

	@Override
	@CacheEvict(cacheNames = "IoTSessionRepository", key = "#p0.sessionKey")
	@Transactional
	void delete(IoTSession id);

	@CacheEvict(cacheNames = "IoTSessionRepository", key = "#p0")
	@Transactional
	void deleteBySessionKey(String sessionKey);

	@Override
	@CacheEvict(cacheNames = "IoTSessionRepository", allEntries = true)
	@Transactional
	void delete(String id);

	@Override
	List<IoTSession> findAll();

	@Override
	@CacheEvict(cacheNames = "IoTSessionRepository", allEntries = true)
	@Transactional
	void deleteAll();

	@Cacheable(cacheNames = "IoTSessionRepository", unless = "#result == null", key = "#p0")
	IoTSession findBySessionKey(String sessionKey);

}

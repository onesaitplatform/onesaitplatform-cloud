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
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.ClientPlatformSimplifiedDTO;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.User;

public interface ClientPlatformRepository extends JpaRepository<ClientPlatform, String> {

	List<ClientPlatform> findByIdentificationAndDescription(String identification, String description);

	List<ClientPlatform> findByUserAndIdentificationAndDescription(User user, String identification,
			String description);

	ClientPlatform findByUserAndIdentification(User user, String identification);

	long countByIdentification(String identification);

	List<ClientPlatform> countByIdentificationLike(String identification);

	long countByUser(User user);

	@Cacheable(cacheNames = "ClientPlatformRepository", key = "#p0", unless = "#result == null")
	ClientPlatform findByIdentification(String identification);

	@SuppressWarnings("unchecked")
	@Override
	@Caching( evict = {
			@CacheEvict(cacheNames = "ClientPlatformRepository", key = "#p0.identification"),
			@CacheEvict(cacheNames = "ClientPlatformSimplified", key = "#p0.identification")
	})
	ClientPlatform save(ClientPlatform clientPlatform);

	@Override
	@Caching( evict = {
			@CacheEvict(cacheNames = "ClientPlatformRepository", key = "#p0.identification"),
			@CacheEvict(cacheNames = "ClientPlatformSimplified", key = "#p0.identification")
	})
	@Transactional
	void delete(ClientPlatform clientPlatform);

	List<ClientPlatform> findByIdentificationLike(String identification);

	List<ClientPlatform> findByUser(User user);
	
	@Cacheable(cacheNames = "ClientPlatformSimplified", key = "#p0")
	@Query("Select new com.minsait.onesait.platform.config.dto.ClientPlatformSimplifiedDTO(cp.id, cp.identification) "
			+ "FROM ClientPlatform cp "
			+ "WHERE cp.identification = :identification")
	ClientPlatformSimplifiedDTO findClientPlatformIdByIdentification(@Param("identification") String identification);

}

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
package com.minsait.onesait.platform.config.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.ClientPlatformTokenDTO;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;

public interface TokenRepository extends JpaRepository<Token, String> {

	List<Token> findByClientPlatform(ClientPlatform clientPlatform);

	@Cacheable(cacheNames = "TokenRepository", key = "#p0")
	Token findByTokenName(String token);

	@Override
	@Caching( evict = {
			@CacheEvict(cacheNames = "TokenRepository", key = "#p0.tokenName"),
			@CacheEvict(cacheNames = "TokenAndClientPlatform", key = "#p0.tokenName")
	})
	void delete(Token token);

	@Override
	@Caching( evict = {
			@CacheEvict(cacheNames = "TokenRepository", allEntries = true),
			@CacheEvict(cacheNames = "TokenAndClientPlatform", allEntries = true)
	})
	void deleteById(String id);

	@SuppressWarnings("unchecked")
	@Override
	@Caching( evict = {
			@CacheEvict(cacheNames = "TokenRepository", key = "#p0.tokenName"),
			@CacheEvict(cacheNames = "TokenAndClientPlatform", key = "#p0.tokenName")
	})
	Token save(Token token);

	@Override
	Optional<Token> findById(String id);

	//This method is using a DTO because projection interfaces does not worked with hazelcast. Projections are interfaces
	//     that spring instantiate using a proxy, so is normal that they does not work as a normal java object.
	@Cacheable(cacheNames = "TokenAndClientPlatform", key = "#p0")
	@Query("SELECT new com.minsait.onesait.platform.config.dto.ClientPlatformTokenDTO(cp.id, cp.identification,"
			+ "t.tokenName, t.active, u.userId, u.fullName) "
			+ "FROM Token t INNER JOIN t.clientPlatform cp INNER JOIN cp.user u "
			+ "WHERE t.tokenName = :tokenName" )
	ClientPlatformTokenDTO findClientPlatformIdByTokenName(@Param("tokenName") String tokenName);

	@Query("SELECT t FROM Token t WHERE t.clientPlatform.user= :user")
	List<Token> findByUser(@Param("user") User user);
	
	
	


}

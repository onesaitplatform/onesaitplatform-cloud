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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Token;

public interface TokenRepository extends JpaRepository<Token, String> {

	List<Token> findByClientPlatform(ClientPlatform clientPlatform);

	@Cacheable(cacheNames = "TokenRepository", key = "#p0")
	Token findByTokenName(String token);

	@Override
	@CacheEvict(cacheNames = "TokenRepository", key = "#p0.tokenName")
	void delete(Token token);

	@Override
	@CacheEvict(cacheNames = "TokenRepository", allEntries = true)
	void delete(String id);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = "TokenRepository", key = "#p0.tokenName")
	Token save(Token token);

	Token findById(String id);

}

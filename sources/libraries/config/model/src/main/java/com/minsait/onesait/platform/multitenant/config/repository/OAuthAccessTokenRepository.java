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

import java.util.Collection;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.OAuthAccessToken;

@Transactional
public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, String> {

	public static final String OAUTH_ACCESS_TOKEN_REPOSITORY = "OauthAccessTokenRepository";

	Collection<OAuthAccessToken> findByClientId(String clientId);

	Collection<OAuthAccessToken> findByUserName(String userName);

	@Cacheable(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, unless = "#result == null", key = "#p0")
	OAuthAccessToken findByAuthenticationId(String authenticationId);

	@Cacheable(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, unless = "#result == null", key = "#p0")
	OAuthAccessToken findByTokenId(String tokenId);

	@Modifying
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, key = "#p0"),
			@CacheEvict(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, key = "#p1") })
	@Query("delete from OAuthAccessToken o where o.tokenId= :tokenId or o.authenticationId= :authenticationId")
	void deleteByTokenId(@Param("tokenId") String tokenId, @Param("authenticationId") String authenticationId);

	@Modifying
	@Transactional
	@CacheEvict(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, key = "#p0", allEntries = true)
	void deleteByRefreshToken(String refreshToken);

	@Override
	@Caching(put = {
			@CachePut(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, unless = "#result == null", key = "#p0.tokenId"),
			@CachePut(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, unless = "#result == null", key = "#p0.authenticationId") })
	<S extends OAuthAccessToken> S save(S entity);

	@Transactional
	@Modifying
	@CacheEvict(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, allEntries = true)
	Long deleteByClientId(String clientId);

	@Transactional
	@Modifying
	@CacheEvict(cacheNames = OAUTH_ACCESS_TOKEN_REPOSITORY, allEntries = true)
	Long deleteByClientIdAndUserName(String clientId, String username);

}

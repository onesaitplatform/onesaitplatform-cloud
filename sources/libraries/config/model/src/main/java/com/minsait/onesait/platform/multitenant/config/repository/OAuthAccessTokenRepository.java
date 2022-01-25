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

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.OAuthAccessToken;

@Transactional
public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, String> {

	Collection<OAuthAccessToken> findByClientId(String clientId);

	Collection<OAuthAccessToken> findByUserName(String userName);

	OAuthAccessToken findByAuthenticationId(String authenticationId);

	@Transactional
	@Modifying
	//	@Query("DELETE FROM OauthAccessToken o WHERE o.clientId= :clientId")
	Long deleteByClientId(String clientId);

	@Transactional
	@Modifying
	//	@Query("DELETE FROM OauthAccessToken o WHERE o.clientId= :clientId AND o.userName= :username")
	Long deleteByClientIdAndUserName(String clientId, String username);

}

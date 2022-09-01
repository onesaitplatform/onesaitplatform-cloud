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
package com.minsait.onesait.platform.serverless.security.oauth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.serverless.exception.UserNotFoundException;
import com.minsait.onesait.platform.serverless.model.User;
import com.minsait.onesait.platform.serverless.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OauthAuthenticator {

	@Autowired
	private UserInfoTokenServices userInfoTokenServices;
	@Autowired
	private UserService userService;

	public Authentication loadOauthAuthentication(String token) {
		try {
			final OAuth2Authentication auth = userInfoTokenServices.loadAuthentication(token);
			if (auth != null) {
				try {
					@SuppressWarnings("unchecked")
					final Map<String, Object> claims = (Map<String, Object>) auth.getUserAuthentication().getDetails();
					final String userId = (String) claims.get(OauthPrincipalExtractor.USERNAME);
					createUser(userId);
				} catch (final Exception e) {
					log.error("Could not extract claims", e);
				}
			}
			return auth;
		} catch (final Exception e) {
			return null;
		}

	}

	private void createUser(String userId) {
		try {
			userService.getUser(userId);
		} catch (final UserNotFoundException e) {
			log.debug("User {} not found, creating local reference...");
			final User user = new User();
			user.setUserId(userId);
			userService.create(user);
		}
	}

}

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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.exception.NotAuthorizedException;
import com.minsait.onesait.platform.flowengine.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FlowEngineValidationNodeServiceImpl implements FlowEngineValidationNodeService {

	private static final String USER_NOT_EXIST = "Requested user does not exist";

	@Autowired
	private UserService userService;

	@Override
	public User validateUserCredentials(String userId, String credentials) {
		if (userId == null || credentials == null || userId.isEmpty() || credentials.isEmpty()) {
			log.error("User or password cannot be empty.");
			throw new IllegalArgumentException("User or credentials cannot be empty.");
		}

		final User sofia2User = userService.getUser(userId);
		if (sofia2User == null) {
			log.error(USER_NOT_EXIST);
			throw new ResourceNotFoundException(USER_NOT_EXIST);
		}
		if (!sofia2User.getPassword().equals(credentials)) {
			log.error("Credentials for user " + userId + " does not match.");
			throw new NotAuthorizedException("Credentials for user " + userId + " does not match.");
		}
		return sofia2User;
	}

	@Override
	public User validateUser(String userId) {
		if (userId == null || userId.isEmpty()) {
			log.error("User cannot be empty.");
			throw new IllegalArgumentException("User cannot be empty.");
		}

		final User sofia2User = userService.getUser(userId);
		if (sofia2User == null) {
			log.error(USER_NOT_EXIST);
			throw new ResourceNotFoundException(USER_NOT_EXIST);
		}
		return sofia2User;
	}

	@Override
	public DecodedAuthentication decodeAuth(String authentication) {
		try {
			return new DecodedAuthentication(authentication);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Authentication is null or cannot be decoded.");
		}
	}

}

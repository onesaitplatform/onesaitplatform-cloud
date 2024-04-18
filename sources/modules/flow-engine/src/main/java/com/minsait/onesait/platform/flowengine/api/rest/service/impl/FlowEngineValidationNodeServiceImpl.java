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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.exception.NotAuthorizedException;
import com.minsait.onesait.platform.flowengine.exception.ResourceNotFoundException;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FlowEngineValidationNodeServiceImpl implements FlowEngineValidationNodeService {

	private static final String USER_NOT_EXIST = "Requested user does not exist";
	private static final String DOMAIN_NOT_EXIST = "Requested domain does not exist";

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UserService userService;

	@Autowired
	private FlowDomainService domainService;

	@Override
	public User validateUserCredentials(String userId, String credentials) {
		if (userId == null || credentials == null || userId.isEmpty() || credentials.isEmpty()) {
			log.error("User or password cannot be empty.");
			throw new IllegalArgumentException("User or credentials cannot be empty.");
		}
		User sofia2User = null;
		try {
			userDetailsService.loadUserByUsername(userId);
			sofia2User = userService.getUser(userId);
			if (sofia2User == null) {
				log.error(USER_NOT_EXIST);
				throw new ResourceNotFoundException(USER_NOT_EXIST);
			}
			if (!sofia2User.getPassword().equals(credentials)) {
				log.error("Credentials for user " + userId + " does not match.");
				throw new NotAuthorizedException("Credentials for user " + userId + " does not match.");
			}
		} catch (final Exception e) {
			log.error(USER_NOT_EXIST);
			throw new ResourceNotFoundException(USER_NOT_EXIST);
		}
		return sofia2User;
	}

	@Override
	public User validateUser(String userId) {
		if (userId == null || userId.isEmpty()) {
			log.error("User cannot be empty.");
			throw new IllegalArgumentException("User cannot be empty.");
		}
		User sofia2User = null;
		try {
			userDetailsService.loadUserByUsername(userId);
			sofia2User = userService.getUser(userId);
			if (sofia2User == null) {
				log.error(USER_NOT_EXIST);
				throw new ResourceNotFoundException(USER_NOT_EXIST);
			}
		} catch (final Exception e) {
			log.error(USER_NOT_EXIST);
			throw new ResourceNotFoundException(USER_NOT_EXIST);
		}
		return sofia2User;
	}
	
	@Override
	public FlowDomain validateDomain(String domainName) {
		if (domainName == null || domainName.isEmpty()) {
			log.error("Domain cannot be empty.");
			throw new IllegalArgumentException("Domain cannot be empty.");
		}
		FlowDomain flowDomain = null;
		try {
			flowDomain = domainService.getFlowDomainByIdentification(domainName);
			if (flowDomain == null) {
				log.error(DOMAIN_NOT_EXIST);
				throw new ResourceNotFoundException(DOMAIN_NOT_EXIST);
			}
		} catch (final Exception e) {
			log.error(DOMAIN_NOT_EXIST);
			throw new ResourceNotFoundException(DOMAIN_NOT_EXIST);
		}
		return flowDomain;
	}

	@Override
	public DecodedAuthentication decodeAuth(String authentication) {
		try {
			final DecodedAuthentication decodedAuth = new DecodedAuthentication(authentication);
			if (decodedAuth.getVerticalSchema() != null)
				MultitenancyContextHolder.setVerticalSchema(decodedAuth.getVerticalSchema());
			return decodedAuth;
		} catch (final Exception e) {
			throw new IllegalArgumentException("Authentication is null or cannot be decoded.");
		}
	}

}

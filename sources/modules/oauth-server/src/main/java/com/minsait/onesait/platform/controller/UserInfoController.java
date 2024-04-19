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
package com.minsait.onesait.platform.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.oauthserver.audit.aop.OauthServerAuditable;
import com.minsait.onesait.platform.security.jwt.ri.TokenController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserInfoController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TokenController tokenController;

	@RequestMapping("/user")
	public Object user(Principal user, @RequestHeader("Authorization") String authorization) {
		if (!StringUtils.isEmpty(authorization)) {
			try {
				log.info("authentication of type {}", user.getClass().getName());
				return tokenController.info(authorization.split(" ")[1].trim()).getOauthInfo();
			} catch (final Exception e) {
				log.error("erorr ", e);
				return user;
			}
		} else {
			return user;
		}
	}

	/*
	 * Endpoint for OIDC principal information retrieval
	 */
	@OauthServerAuditable
	@RequestMapping("/oidc/userinfo")
	public JsonNode userInfo(OAuth2Authentication token) {
		if (token.getUserAuthentication().getPrincipal() instanceof UserPrincipal) {
			final UserPrincipal p = (UserPrincipal) token.getUserAuthentication().getPrincipal();
			MultitenancyContextHolder.setTenantName(p.getTenant());
			MultitenancyContextHolder.setVerticalSchema(p.getVerticalSchema());
		}
		final User principal = userRepository.findByUserId(
				token.getPrincipal() instanceof UserDetails ? ((UserDetails) token.getPrincipal()).getUsername()
						: token.getPrincipal().toString());
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.createObjectNode();
		((ObjectNode) node).put("mail", principal.getEmail());
		((ObjectNode) node).put("username", principal.getUserId());
		((ObjectNode) node).put("sub", principal.getUserId());
		((ObjectNode) node).put("name", principal.getFullName());
		((ObjectNode) node).put("role", principal.getRole().getId());
		((ObjectNode) node).put("userid", "(" + principal.getUserId() + ")");
		((ObjectNode) node).put("extra_fields", principal.getExtraFields());
		MultitenancyContextHolder.clear();
		return node;
	}
}

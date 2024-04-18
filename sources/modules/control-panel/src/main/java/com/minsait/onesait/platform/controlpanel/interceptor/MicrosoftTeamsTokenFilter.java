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
package com.minsait.onesait.platform.controlpanel.interceptor;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.interceptor.InterceptorCommon;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.security.Securityhandler;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MicrosoftTeamsTokenFilter implements Filter {

	private static final String TEAMS_TOKEN_HEADER = "X-Teams-Token";
	private static final String API_PREFIX = "/api/";
	private static final String LOGOUT_PREFIX = "/logout";
	private static final String TEAMS_USERID_ATT = "preferred_username";
	private static final String TEAMS_NAME_ATT = "name";
	private static final String DEFAULT_IMPORT_PASS_WORD = "changeIt2020!";

	private final Securityhandler successHandler;
	private final ConfigDBDetailsService detailsService;
	private final RoleRepository roleRepository;
	private final UserService userService;

	public MicrosoftTeamsTokenFilter() {
		successHandler = BeanUtil.getBean(Securityhandler.class);
		detailsService = BeanUtil.getBean(ConfigDBDetailsService.class);
		roleRepository = BeanUtil.getBean(RoleRepository.class);
		userService = BeanUtil.getBean(UserService.class);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("init filter MicrosoftTeamsTokenFilter");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		if (requiresAuthentication(req, true)) {
			log.debug("Detected header {} in API request, loading temp autenthication", TEAMS_TOKEN_HEADER);
			InterceptorCommon.setPreviousAuthenticationOnSession(req.getSession());
			try {
				final String token = req.getHeader(TEAMS_TOKEN_HEADER);
				authenticateUser(token);
				chain.doFilter(request, response);

			} catch (final Exception e) {
				log.error("Error on Teams REST authentication", e);

			} finally {
				log.debug("Clearing authentication contexts");
				InterceptorCommon.clearContexts(
						(Authentication) req.getSession().getAttribute(InterceptorCommon.SESSION_ATTR_PREVIOUS_AUTH),
						req.getSession());
			}
		} else if (requiresAuthentication(req, false)) {
			log.debug("Detected header {} in API request, loading full autenthication", TEAMS_TOKEN_HEADER);
			try {
				final String token = req.getHeader(TEAMS_TOKEN_HEADER);
				final Authentication auth = authenticateUser(token);
				if (auth != null) {
					successHandler.onAuthenticationSuccess(req, (HttpServletResponse) response, auth);
				}
				chain.doFilter(request, response);
			} catch (final Exception e) {
				log.error("Error on Teams authentication", e);
			}
		} else {
			chain.doFilter(request, response);
		}

	}

	@Override
	public void destroy() {
		log.debug("destroy filter MicrosoftTeamsTokenFilter");

	}

	private boolean requiresAuthentication(HttpServletRequest req, boolean isAPI) {
		if (!isAPI) {
			return !req.getServletPath().startsWith(LOGOUT_PREFIX) && req.getHeader(TEAMS_TOKEN_HEADER) != null
					&& SecurityContextHolder.getContext().getAuthentication() == null;
		} else {
			return req.getServletPath().startsWith(API_PREFIX) && req.getHeader(TEAMS_TOKEN_HEADER) != null;
		}
	}

	private void importTeamsUser(String userId, String name) {
		final User user = new User();
		user.setUserId(userId);
		user.setFullName(name == null ? userId : name);
		user.setEmail(userId);
		user.setActive(true);
		user.setPassword(DEFAULT_IMPORT_PASS_WORD + UUID.randomUUID().toString().substring(1, 5));
		user.setRole(roleRepository.findById(Role.Type.ROLE_USER.name()));
		userService.createUser(user);
	}

	private String extractParamFromHeaderToken(String token, String param) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final String[] jwtSegments = token.split("\\.");
		final String jwtBody = jwtSegments[1];
		final String parsedBody = new String(Base64.getDecoder().decode(jwtBody));
		JsonNode jsonBody = mapper.createObjectNode();

		try {
			jsonBody = mapper.readValue(parsedBody, JsonNode.class);
			final String username = jsonBody.get(param).asText();
			return username;
		} catch (final IOException e) {
			log.error("Unparseable JWT Token");
			throw e;
		}
	}

	private Authentication authenticateUser(String token) throws IOException {
		final String userId = extractParamFromHeaderToken(token, TEAMS_USERID_ATT);
		UserDetails details = null;
		try {
			details = detailsService.loadUserByUsername(userId);
		} catch (final Exception e) {
			log.debug("User doesn't exist creating");
			importTeamsUser(userId, extractParamFromHeaderToken(token, TEAMS_NAME_ATT));
			details = detailsService.loadUserByUsername(userId);
		}
		if (details != null) {
			final Authentication auth = new UsernamePasswordAuthenticationToken(details, details.getPassword(),
					details.getAuthorities());
			InterceptorCommon.setContexts(auth);
			log.debug("Loaded authentication for user {}", auth.getName());
		}

		return SecurityContextHolder.getContext().getAuthentication();
	}

}

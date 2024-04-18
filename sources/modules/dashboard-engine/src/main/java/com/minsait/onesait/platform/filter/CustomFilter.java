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
package com.minsait.onesait.platform.filter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.microsoft.sqlserver.jdbc.StringUtils;
import com.minsait.onesait.platform.business.services.datasources.exception.DashboardEngineException;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;
import com.minsait.onesait.platform.security.token.TokenResponse;

import lombok.extern.slf4j.Slf4j;

@Component(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
@Slf4j
public class CustomFilter extends GenericFilterBean {

	private static final String AUTH_HEADER_KEY = "Authorization";
	private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer "; // with trailing space to separate token
	private static final String AUTH_VALUE_ANONYMOUS = "anonymous";
	private static final String VERTICAL_PARAMETER = "vertical";
	private static final String OAUTH2_QUERYPARAM = "oauthtoken";

	private static final String TENANT_PARAMETER = "tenant";

	private static final String REFERER = "Referer";

	private final String onesaitPlatformTokenAuth;
	private final UserDetailsService userDetailsService;
	private PlugableOauthAuthenticator plugableOauthAuthenticator;

	private final MultitenancyService multitenancyService;

	public CustomFilter(String onesaitPlatformTokenAuth, UserDetailsService userDetailsService,
			MultitenancyService multitenancyService) {
		super();
		this.onesaitPlatformTokenAuth = onesaitPlatformTokenAuth;
		this.userDetailsService = userDetailsService;
		this.multitenancyService = multitenancyService;
		try {
			plugableOauthAuthenticator = BeanUtil.getBean(PlugableOauthAuthenticator.class);
		} catch (final Exception e) {
			// NO-OP
		}

	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
			final FilterChain filterChain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		// Multitenancy
		setMultitenantContext();
		try {
			final String jwt = getBearerToken(httpRequest);
			if (jwt != null && !jwt.isEmpty()) {
				final String userId = validateToken(jwt);
				if (!StringUtils.isEmpty(userId)) {
					generateSecurityContextAuthentication(userId, jwt);
					log.info("Logged in using JWT");
					if (((HttpServletRequest) servletRequest).getServletPath().startsWith("/dsengine/solver")
							|| ((HttpServletRequest) servletRequest).getServletPath().startsWith("/api")
							|| ((HttpServletRequest) servletRequest).getServletPath().startsWith("/loginRest")
							|| ((HttpServletRequest) servletRequest).getServletPath()
									.startsWith("/dsengine/rest/solver")) {
						filterChain.doFilter(servletRequest, servletResponse);
					} else {
						return;
					}
				}
				return;
			} else {
				if (isAnonymous(httpRequest)) {
					setMultitenantContext(httpRequest);
					generateSecurityContextAuthenticationAnonymous();
					if (((HttpServletRequest) servletRequest).getServletPath().startsWith("/dsengine/solver")) {
						filterChain.doFilter(servletRequest, servletResponse);
					}
				} else {
					log.info("No JWT provided, continue chain or user-pass");
					filterChain.doFilter(servletRequest, servletResponse);
				}
			}
		} catch (final Exception e) {
			log.error("Failed logging in", e);
		}
	}

	private void setMultitenantContext(HttpServletRequest httpRequest) {
		final String referer = httpRequest.getHeader(REFERER);
		Optional.ofNullable(extractParamFromReferer(referer, VERTICAL_PARAMETER)).ifPresent(s -> multitenancyService
				.getVertical(s).ifPresent(v -> MultitenancyContextHolder.setVerticalSchema(v.getSchema())));
		Optional.ofNullable(extractParamFromReferer(referer, TENANT_PARAMETER))
				.ifPresent(s -> MultitenancyContextHolder.setTenantName(s));

	}

	/**
	 * Get the bearer token from the HTTP request. The token is in the HTTP request
	 * "oauthtoken" queryparams in the form of: "oauthtoken=[token]" "Authorization"
	 * header in the form of: "Bearer [token]"
	 */
	private String getBearerToken(HttpServletRequest request) {
		final String authParam = request.getParameter(OAUTH2_QUERYPARAM);
		if (authParam != null) {
			return authParam;
		}
		final String authHeader = request.getHeader(AUTH_HEADER_KEY);
		if (authHeader != null && authHeader.startsWith(AUTH_HEADER_VALUE_PREFIX)) {
			return authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length());
		}
		return null;
	}

	private boolean isAnonymous(HttpServletRequest request) {
		final String authParam = request.getParameter(AUTH_VALUE_ANONYMOUS);
		if (authParam != null) {
			return true;
		}
		final String authHeader = request.getHeader(AUTH_HEADER_KEY);
		if (authHeader != null && authHeader.equals(AUTH_VALUE_ANONYMOUS)) {
			return true;
		}
		return false;
	}

	private String validateToken(String token) {
		if (plugableOauthAuthenticator == null) {
			return validateTokenLegacy(token);
		} else {
			final Authentication auth = plugableOauthAuthenticator.loadOauthAuthentication(token);
			if (auth != null) {
				return auth.getName();
			}
		}
		return null;
	}

	@Deprecated
	/*
	 * Will remove this validation method soon as of 2.2.0
	 */
	private String validateTokenLegacy(String token) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		final HttpEntity<String> entity = new HttpEntity<>(token, headers);
		final ResponseEntity<TokenResponse> response = restTemplate.exchange(onesaitPlatformTokenAuth, HttpMethod.POST,
				entity, TokenResponse.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody().getPrincipal();
		} else {
			switch (response.getStatusCode()) {
			case NOT_FOUND:
				throw new DashboardEngineException(DashboardEngineException.Error.NOT_FOUND);
			case FORBIDDEN:
				throw new DashboardEngineException(DashboardEngineException.Error.PERMISSION_DENIED);
			case UNAUTHORIZED:
				throw new DashboardEngineException(DashboardEngineException.Error.INVALID_AUTH);
			default:
				throw new DashboardEngineException("Error: " + response.getStatusCode() + " - " + response.toString());
			}
		}
	}

	private void generateSecurityContextAuthentication(String userId, String token) {

		final UserDetails user = userDetailsService.loadUserByUsername(userId);
		final Authentication auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private void generateSecurityContextAuthenticationAnonymous() {
		final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		final UserPrincipal user = new UserPrincipal(AUTH_VALUE_ANONYMOUS, AUTH_VALUE_ANONYMOUS, grantedAuthorities,
				MultitenancyContextHolder.getVerticalSchema(), MultitenancyContextHolder.getTenantName());
		final Authentication auth = new UsernamePasswordAuthenticationToken(user, AUTH_VALUE_ANONYMOUS,
				grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private void setMultitenantContext() {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && !auth.getPrincipal().equals(AUTH_VALUE_ANONYMOUS)) {
			MultitenancyContextHolder.setVerticalSchema(((UserPrincipal) auth.getPrincipal()).getVerticalSchema());
			MultitenancyContextHolder.setTenantName(((UserPrincipal) auth.getPrincipal()).getTenant());
		}
	}

	private String extractParamFromReferer(String referer, String parameter) {
		if (!StringUtils.isEmpty(referer) && referer.indexOf(VERTICAL_PARAMETER) != -1) {
			final URI uri = URI.create(referer);
			final String query = uri.getQuery();
			final List<NameValuePair> queryMap = new ArrayList<>();
			final String[] params = query.split(Pattern.quote("&"));
			for (final String param : params) {
				final String[] chunks = param.split(Pattern.quote("="));
				final String name = chunks[0];
				String value = null;
				if (chunks.length > 1) {
					value = chunks[1];
				}
				queryMap.add(new BasicNameValuePair(name, value));
			}
			return queryMap.stream().filter(e -> e.getName().equals(parameter)).map(NameValuePair::getValue).findFirst()
					.orElse(null);
		}
		return null;
	}
}

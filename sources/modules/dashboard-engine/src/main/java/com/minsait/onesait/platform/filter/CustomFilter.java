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
package com.minsait.onesait.platform.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.minsait.onesait.platform.security.token.TokenResponse;

import lombok.extern.slf4j.Slf4j;

@Component(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
@Slf4j
public class CustomFilter extends GenericFilterBean {

	private static final String AUTH_HEADER_KEY = "Authorization";
	private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer "; // with trailing space to separate token
	private static final String AUTH_VALUE_ANONYMOUS = "anonymous";

	private String onesaitPlatformTokenAuth;

	public CustomFilter(String onesaitPlatformTokenAuth) {
		super();
		this.onesaitPlatformTokenAuth = onesaitPlatformTokenAuth;
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
			final FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

		try {
			String jwt = getBearerToken(httpRequest);
			if (jwt != null && !jwt.isEmpty()) {
				TokenResponse details = validateToken(jwt);
				generateSecurityContextAuthentication(details);
				log.info("Logged in using JWT");

				return;
			} else {
				if (isAnonymous(httpRequest)) {
					generateSecurityContextAuthenticationAnonymous();
				} else {
					log.info("No JWT provided, continue chain or user-pass");
					filterChain.doFilter(servletRequest, servletResponse);
				}
			}
		} catch (final Exception e) {
			log.error("Failed logging in", e);
		}
	}

	/**
	 * Get the bearer token from the HTTP request. The token is in the HTTP request
	 * "Authorization" header in the form of: "Bearer [token]"
	 */
	private String getBearerToken(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTH_HEADER_KEY);
		if (authHeader != null && authHeader.startsWith(AUTH_HEADER_VALUE_PREFIX)) {
			return authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length());
		}
		return null;
	}

	private boolean isAnonymous(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTH_HEADER_KEY);
		if (authHeader != null && authHeader.equals(AUTH_VALUE_ANONYMOUS)) {
			return true;
		}
		return false;
	}

	private TokenResponse validateToken(String token) {
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(token, headers);
		TokenResponse response = restTemplate.postForObject(onesaitPlatformTokenAuth, entity, TokenResponse.class);
		return response;
	}

	private void generateSecurityContextAuthentication(TokenResponse details) {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		grantedAuthorities.add(new SimpleGrantedAuthority(details.getAuthorities().get(0)));
		Authentication auth = new UsernamePasswordAuthenticationToken(details.getPrincipal(), details.getAccess_token(),
				grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private void generateSecurityContextAuthenticationAnonymous() {
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		Authentication auth = new UsernamePasswordAuthenticationToken(AUTH_VALUE_ANONYMOUS, AUTH_VALUE_ANONYMOUS,
				grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}

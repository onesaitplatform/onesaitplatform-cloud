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
package com.minsait.onesait.platform.controlpanel.interceptor;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.filter.OAuth2AuthenticationFailureEvent;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.interceptor.InterceptorCommon;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BearerTokenFilter implements Filter {

	private final TokenExtractor tokenExtractor = new BearerTokenExtractorPlatform();
	private final TokenStore tokenStore;
	private PlugableOauthAuthenticator plugableOauthAuthenticator;
	private final ConfigDBDetailsService configDBDetailsService;
	private Map<String, Long> revokedTokens;

	final boolean loadFromJWT = System.getenv("LOAD_FROM_JWT") == null ? false
			: Boolean.valueOf(System.getenv("LOAD_FROM_JWT"));


	@SuppressWarnings("unchecked")
	public BearerTokenFilter() {
		tokenStore = BeanUtil.getBean(TokenStore.class);
		configDBDetailsService = BeanUtil.getBean(ConfigDBDetailsService.class);
		try {
			revokedTokens = (Map<String, Long>) BeanUtil.getContext().getBean("revokedTokens");
			plugableOauthAuthenticator = BeanUtil.getBean(PlugableOauthAuthenticator.class);
		} catch (final Exception e) {
			// NO-OP
		}

	}

	private void publish(ApplicationEvent event) {
		try {
			final ApplicationContext eventPublisher = BeanUtil.getContext();
			eventPublisher.publishEvent(event);
		} catch (final Exception e) {
			log.debug("No application event publisher found on Spring Context");
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Init BearerTokenFilter for REST authentication");

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse resp = (HttpServletResponse) response;
		final Authentication auth = tokenExtractor.extract(req);
		boolean hasSession = false;
		if (auth instanceof PreAuthenticatedAuthenticationToken) {
			try {
				// save previous auth
				hasSession = req.getSession(false) != null;
				if (hasSession) {
					InterceptorCommon.setPreviousAuthenticationOnSession(req.getSession(false));
				}
				log.trace("Principal token JWT {}", auth.getPrincipal());
				log.debug("Detected Bearer token in request, loading autenthication");
				Authentication oauth = loadAuthentication(auth);
				if (oauth == null && loadFromJWT) {
					log.debug("Failed to load Auth from DB, trying to decode JWT");
					oauth = loadAuthenticationFromJWT(auth.getPrincipal());
				}
				if (oauth == null) {
					log.error("Could not load oauth authentication, sending redirect with 401 code");
					resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					resp.setContentType("application/json;charset=UTF-8");
					resp.getWriter().write("{\"error\": \"Incorrect or Expired Authorization Header\"}");
					resp.getWriter().flush();
					resp.getWriter().close();
				} else {
					InterceptorCommon.setContexts(oauth);
					log.debug("Loaded authentication for user {}", oauth.getName());
					publish(new AuthenticationSuccessEvent(oauth));
					chain.doFilter(request, response);
				}

			} catch (final Exception e) {
				log.error("Error", e);
				final BadCredentialsException bad = new BadCredentialsException("Could not obtain access token", e);
				publish(new OAuth2AuthenticationFailureEvent(bad));
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.setContentType("application/json;charset=UTF-8");
				resp.getWriter().write("{\"error\": \"Incorrect or Expired Authorization Header\"}");
				resp.getWriter().flush();
				resp.getWriter().close();
			} finally {
				log.debug("Clearing authentication contexts");
				if (hasSession) {
					InterceptorCommon.clearContexts(req.getSession(false));
				} else {
					if (req.getSession(false) != null) {
						req.getSession(false).invalidate();
					}
				}
			}

		} else {
			chain.doFilter(request, response);
		}

	}

	private Authentication loadAuthentication(Authentication auth) {
		if (plugableOauthAuthenticator != null) {
			return plugableOauthAuthenticator.loadFullAuthentication((String) auth.getPrincipal());
		} else {
			return tokenStore.readAuthentication((String) auth.getPrincipal());
		}

	}


	private Authentication loadAuthenticationFromJWT(Object authToken) {
		try {
			if(revokedTokens.get(authToken)!=null) {
				log.info("Token was revoked, not decoding JWT");
				return null;
			}
			final String token = (String) authToken;
			final String[] jwtSegments = token.split("\\.");
			final String jwtBody = jwtSegments[1];
			final String parsedBody = new String(Base64.getDecoder().decode(jwtBody));
			final ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonBody = mapper.createObjectNode();
			try {
				jsonBody = mapper.readValue(parsedBody, JsonNode.class);
			} catch (final IOException e) {
				log.error("Unparseable JWT body");
				return null;
			}
			final String username = jsonBody.get("user_name").asText();
			final long exp = jsonBody.get("exp").asLong();
			if (System.currentTimeMillis()/1000 < exp) {
				final UserDetails details = configDBDetailsService.loadUserByUsername(username);
				if (details != null) {
					return new UsernamePasswordAuthenticationToken(details, details.getPassword(),
							details.getAuthorities());
				}
			}

		} catch (final Exception e) {
			log.error("Could not extract authentication from decoded JWT: {}", e.getMessage());
		}
		return null;
	}

	@Override
	public void destroy() {
		log.debug("Destroying filter XOpAPIKeyFilter");

	}
}

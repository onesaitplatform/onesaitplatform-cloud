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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.filter.OAuth2AuthenticationFailureEvent;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.util.UrlPathHelper;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.user.UserService;

import groovy.util.logging.Slf4j;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CheckSecurityFilter implements Filter {

	private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

	private static final Logger log = LoggerFactory.getLogger(CheckSecurityFilter.class);

	private static final String X_OP_APIKEY = "X-OP-APIKey";

	private TokenExtractor tokenExtractor = new BearerTokenExtractor();

	@Autowired(required = false)
	private ApplicationEventPublisher eventPublisher;

	@Autowired(required = false)
	private JWTService jwtService;

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private UserService userService;

	String[] presets = { "api" };

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initiating CheckSecurityFilter >> ");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		Authentication info = null;
		String firstResult = null;

		final ParamToHeaderHttpRequestWrapper req = new ParamToHeaderHttpRequestWrapper((HttpServletRequest) request);

		final String path = new UrlPathHelper().getPathWithinApplication(req);
		final String[] states = path.split("/");
		if (states.length > 0) {
			final String firstPath = states[1];
			firstResult = Arrays.stream(presets).filter(x -> x.equalsIgnoreCase(firstPath)).findFirst().orElse(null);
			if (firstResult != null && states.length > 2 && states[2].equals("login"))
				firstResult = null;
		}
		final String userToken = req.getHeader(X_OP_APIKEY);
		// Oauth token
		Authentication authentication = tokenExtractor.extract(req);

		// try to get auth from user_token
		if (userToken != null && authentication == null) {
			authentication = extractAuthentication(userToken, req);
		}

		if (authentication == null && firstResult == null) {
			chain.doFilter(request, response); // Goes to default servlet.
		} else if (authentication == null) {

			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			((HttpServletResponse) response).setContentType("application/json;charset=UTF-8");
			((HttpServletResponse) response).getWriter()
					.write("{\"error\": \"Path needs to be Authenticated, but no Authentication Header was found\"}");
			((HttpServletResponse) response).getWriter().flush();
			((HttpServletResponse) response).getWriter().close();

		} else {
			info = getInfo(authentication, req);

			if (info == null) {

				((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				((HttpServletResponse) response).setContentType("application/json;charset=UTF-8");
				((HttpServletResponse) response).getWriter()
						.write("{\"error\": \"Incorrect or Expired Authorization Header, Status is UnAuthorized\"}");
				((HttpServletResponse) response).getWriter().flush();
				((HttpServletResponse) response).getWriter().close();
			} else {
				chain.doFilter(request, response); // Goes to default servlet.
				logout(req);
			}
			// } else if (authentication != null && firstResult == null) {
			// info = getInfo(authentication, req);
			// if (info == null) {
			//
			// ((HttpServletResponse)
			// response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			// ((HttpServletResponse)
			// response).setContentType("application/json;charset=UTF-8");
			// ((HttpServletResponse) response).getWriter()
			// .write("{\"error\": \"Incorrect or Expired Authorization Header, Status is
			// UnAuthorized\"}");
			// ((HttpServletResponse) response).getWriter().flush();
			// ((HttpServletResponse) response).getWriter().close();
			// } else {
			// chain.doFilter(request, response); // Goes to default servlet.
			// logout(req);
			// }
			// ((HttpServletResponse)
			// response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			// ((HttpServletResponse)
			// response).setContentType("application/json;charset=UTF-8");
			// ((HttpServletResponse) response).getWriter().write(
			// "{\"error\": \"Incorrect State, Path not need to be Authenticated but
			// Authorization Header was Found\"}");
			// ((HttpServletResponse) response).getWriter().flush();
			// ((HttpServletResponse) response).getWriter().close();
		}

	}

	Authentication extractAuthentication(String userToken, HttpServletRequest req) {
		final User user = userService.getUserByToken(userToken);
		if (user != null) {
			final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserId());
			final Authentication auth = new UsernamePasswordAuthenticationToken(user.getUserId(),
					userDetails.getPassword(), userDetails.getAuthorities());
			final SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(auth);

			// Create a new session and add the security context.
			final HttpSession session = req.getSession(true);
			session.setAttribute(SPRING_SECURITY_CONTEXT, securityContext);

			publish(new AuthenticationSuccessEvent(auth));
			return auth;
		}
		return null;
	}

	Authentication getInfo(Authentication authentication, HttpServletRequest req) {
		try {

			if (authentication != null) {
				if (authentication instanceof PreAuthenticatedAuthenticationToken) {
					final UsernamePasswordAuthenticationToken authRequest = extractOauthAuthentication(
							(PreAuthenticatedAuthenticationToken) authentication);

					final SecurityContext securityContext = SecurityContextHolder.getContext();
					securityContext.setAuthentication(authRequest);

					// Create a new session and add the security context.
					final HttpSession session = req.getSession(true);
					session.setAttribute(SPRING_SECURITY_CONTEXT, securityContext);

					publish(new AuthenticationSuccessEvent(authRequest));
					return authRequest;
				} else {
					publish(new AuthenticationSuccessEvent(authentication));
					return authentication;
				}

			} else {
				log.error("Authentication is not correct");
			}

		} catch (final Exception e) {
			log.error(e.getMessage());
			final BadCredentialsException bad = new BadCredentialsException("Could not obtain access token", e);
			publish(new OAuth2AuthenticationFailureEvent(bad));

		}
		return null;
	}

	private UsernamePasswordAuthenticationToken extractOauthAuthentication(
			PreAuthenticatedAuthenticationToken authentication) {
		final OAuth2Authentication oauth = (OAuth2Authentication) jwtService
				.getAuthentication((String) authentication.getPrincipal());
		// refreshed token cases
		if (oauth.getUserAuthentication() instanceof PreAuthenticatedAuthenticationToken) {
			return new UsernamePasswordAuthenticationToken(oauth.getUserAuthentication().getPrincipal(),
					oauth.getUserAuthentication().getCredentials(), oauth.getUserAuthentication().getAuthorities());
		}

		else {
			return (UsernamePasswordAuthenticationToken) oauth.getUserAuthentication();
		}
	}

	void logout(HttpServletRequest req) {

		try {
			final HttpSession session = req.getSession();
			session.removeAttribute(SPRING_SECURITY_CONTEXT);
			log.info("Session Disconnected");
		} catch (final Exception e) {
			log.error(e.getMessage(), e);

		}
	}

	@Override
	public void destroy() {
		log.debug("CheckSecurityFilter WebFilter >> ");
	}

	private void publish(ApplicationEvent event) {
		if (eventPublisher != null) {
			eventPublisher.publishEvent(event);
		}
	}

	public TokenExtractor getTokenExtractor() {
		return tokenExtractor;
	}

	public void setTokenExtractor(TokenExtractor tokenExtractor) {
		this.tokenExtractor = tokenExtractor;
	}

}
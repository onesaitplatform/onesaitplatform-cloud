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

import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.BLOCK_PRIOR_LOGIN;
import static com.minsait.onesait.platform.controlpanel.security.SpringSecurityConfig.BLOCK_PRIOR_LOGIN_PARAMS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.thymeleaf.util.StringUtils;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.services.menu.MenuService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.login.LoginManagementController;
import com.minsait.onesait.platform.controlpanel.rest.management.login.model.RequestLogin;
import com.minsait.onesait.platform.controlpanel.security.twofactorauth.TwoFactorAuthService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import jline.internal.Log;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Securityhandler implements AuthenticationSuccessHandler {

	private static final String URI_CONTROLPANEL = "/controlpanel";
	private static final String URI_MAIN = "/main";
	private static final String URI_VERIFY = "/verify";
	private static final String URI_TENANT_PROMOTE = "/promote";

	@Autowired
	private LoginManagementController controller;

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private MenuService menuService;
	@Autowired
	private UserService userService;
	@Value("${onesaitplatform.authentication.twofa.enabled:false}")
	private boolean tfaEnabled;
	@Autowired(required = false)
	private TwoFactorAuthService twoFactorAuthService;

	@Autowired
	private IntegrationResourcesService integrationResourcesService;
	@Autowired
	private MultitenancyService multitenancyService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		final HttpSession session = request.getSession();
		multitenancyService.resetFailedAttemp(authentication.getName());
		if (session != null) {
			loadMenuAndUrlsToSession(request);
			generateTokenOauth2ForControlPanel(request, authentication);
			if (authentication.getAuthorities().toArray()[0].toString()
					.equals(Role.Type.ROLE_PREVERIFIED_ADMINISTRATOR.name())) {
				twoFactorAuthService.newVerificationRequest(authentication.getName());
				response.sendRedirect(request.getContextPath() + URI_VERIFY);
			} else if (authentication.getAuthorities().toArray()[0].toString()
					.equals(Role.Type.ROLE_PREVERIFIED_TENANT_USER.name())) {
				response.sendRedirect(request.getContextPath() + URI_TENANT_PROMOTE);
			}
			final String redirectUrl = (String) session.getAttribute(BLOCK_PRIOR_LOGIN);
			if (redirectUrl != null && !"/error".equalsIgnoreCase(redirectUrl)) {
				// we do not forget to clean this attribute from session
				session.removeAttribute(BLOCK_PRIOR_LOGIN);
				// then we redirect
				response.sendRedirect(request.getContextPath() + redirectUrl.replace(URI_CONTROLPANEL, "")
						.concat(getEncodedParametersFromPreviousRequest(session)));
			} else {
				response.sendRedirect(request.getContextPath() + URI_MAIN);
			}

		} else {
			response.sendRedirect(request.getContextPath() + URI_MAIN);
		}

	}

	private void generateTokenOauth2ForControlPanel(HttpServletRequest request, Authentication authentication) {
		final String password = request.getParameter("password");
		final String username = request.getParameter("username");
		if (!StringUtils.isEmpty(password) && !StringUtils.isEmpty(username)) {
			final RequestLogin oauthRequest = new RequestLogin();
			oauthRequest.setPassword(password);
			oauthRequest.setUsername(username);
			try {
				request.getSession().setAttribute("oauthToken",
						controller.postLoginOauth2(oauthRequest).getBody().getValue());
			} catch (final Exception e) {

				Log.error(e.getMessage());
			}
		} else if (authentication != null && authentication.isAuthenticated()) {
			request.getSession().setAttribute("oauthToken", controller.postLoginOauthNopass(authentication).getValue());
		}
	}

	private void loadMenuAndUrlsToSession(HttpServletRequest request) {
		final String jsonMenu = menuService.loadMenuByRole(userService.getUser(utils.getUserId()));
		// Remove PrettyPrinted
		final String menu = utils.validateAndReturnJson(jsonMenu);
		utils.setSessionAttribute(request, "menu", menu);
		if (request.getSession().getAttribute("apis") == null) {
			utils.setSessionAttribute(request, "apis", integrationResourcesService.getSwaggerUrls());
		}
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.twofa.enabled", havingValue = "true")
	public FilterRegistrationBean preVerifiedUsersFilter() {
		final FilterRegistrationBean filter = new FilterRegistrationBean();
		filter.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null && auth.getAuthorities().toArray()[0].toString()
						.equals(Role.Type.ROLE_PREVERIFIED_ADMINISTRATOR.name())) {
					// FIX-ME CHANGE LOG LEVEL
					log.info("preVerifiedUsersFilter: true, auth: {}, {}", auth);
					response.sendRedirect(request.getContextPath() + URI_VERIFY);
				} else {
					// FIX-ME CHANGE LOG LEVEL
					log.info("preVerifiedUsersFilter: false, auth: {}", auth);
					filterChain.doFilter(request, response);
				}
			}

			@Override
			protected boolean shouldNotFilter(HttpServletRequest request) {
				final String path = request.getServletPath();
				return path.startsWith(URI_VERIFY) || path.startsWith("/login") || path.startsWith("/oauth");

			}
		});

		filter.addUrlPatterns("/*");
		filter.setName("preVerifiedUsersFilter");
		filter.setOrder(Ordered.LOWEST_PRECEDENCE);
		return filter;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.multitenancy.enabled", havingValue = "true")
	public FilterRegistrationBean preverifiedTenantUsersFilter() {
		final FilterRegistrationBean filter = new FilterRegistrationBean();
		filter.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null && auth.getAuthorities().toArray()[0].toString()
						.equals(Role.Type.ROLE_PREVERIFIED_TENANT_USER.name())) {
					log.debug("preverifiedTenantUsersFilter: true, auth: {}, {}", auth);
					response.sendRedirect(request.getContextPath() + URI_TENANT_PROMOTE);
				} else {
					filterChain.doFilter(request, response);
				}
			}

			@Override
			protected boolean shouldNotFilter(HttpServletRequest request) {
				final String path = request.getServletPath();
				return path.startsWith(URI_TENANT_PROMOTE) || path.startsWith("/login") || path.startsWith("/oauth");

			}
		});

		filter.addUrlPatterns("/*");
		filter.setName("preverifiedTenantUsersFilter");
		filter.setOrder(Ordered.LOWEST_PRECEDENCE);
		return filter;
	}

	// added for oauth flows
	@SuppressWarnings("unchecked")
	private String getEncodedParametersFromPreviousRequest(HttpSession session) {
		try {
			log.debug("Retrieving parameters from request to session");
			final Map<String, String[]> params = (Map<String, String[]>) session.getAttribute(BLOCK_PRIOR_LOGIN_PARAMS);
			if (params.isEmpty()) {
				log.debug("No params retrieved from request to session");
				return "";
			}

			final String serializedParams = "?" + URLEncodedUtils.format(params.entrySet().stream()
					.map(e -> new BasicNameValuePair(e.getKey(), e.getValue()[0])).collect(Collectors.toList()),
					StandardCharsets.UTF_8);
			log.debug("Retrieved parameters from request to session: {}", serializedParams);
			return serializedParams;
		} catch (final Exception e) {
			log.debug("Could not retrieve params from session");
			return "";
		}
	}

}

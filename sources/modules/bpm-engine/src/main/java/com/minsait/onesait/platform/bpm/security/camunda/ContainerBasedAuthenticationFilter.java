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
package com.minsait.onesait.platform.bpm.security.camunda;

import static org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;
import org.camunda.bpm.webapp.impl.util.ProcessEngineUtil;
import org.camunda.bpm.webapp.impl.util.ServletContextUtil;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.minsait.onesait.platform.bpm.security.SpringSecurityAuthenticationProvider;
import com.minsait.onesait.platform.config.services.bpm.BPMTenantService;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

public class ContainerBasedAuthenticationFilter implements Filter {

	public static Pattern APP_PATTERN = Pattern.compile("/app/(cockpit|admin|tasklist|welcome)/([^/]+)/");
	public static Pattern API_ENGINE_PATTERN = Pattern.compile("/api/engine/engine/([^/]+)/.*");
	public static Pattern API_STATIC_PLUGIN_PATTERN = Pattern
			.compile("/api/(cockpit|admin|tasklist|welcome)/plugin/[^/]+/static/.*");
	public static Pattern API_PLUGIN_PATTERN = Pattern
			.compile("/api/(cockpit|admin|tasklist|welcome)/plugin/[^/]+/([^/]+)/.*");

	protected AuthenticationProvider authenticationProvider;
	protected AuthenticationService userAuthentications;

	private final BPMTenantService bpmTenantService;
	private final UserDetailsService userDetailsService;
	private final MasterUserRepository masterUserRepository;
	private final VerticalResolver tenantDBResolver;

	public ContainerBasedAuthenticationFilter(BPMTenantService bpmTenantService, UserDetailsService userDetailsService,
			VerticalResolver tenantDBResolver, MasterUserRepository masterUserRepository) {
		this.bpmTenantService = bpmTenantService;
		this.userDetailsService = userDetailsService;
		this.tenantDBResolver = tenantDBResolver;
		this.masterUserRepository = masterUserRepository;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		userAuthentications = new AuthenticationService();

		final String authenticationProviderClassName = filterConfig.getInitParameter(AUTHENTICATION_PROVIDER_PARAM);

		if (authenticationProviderClassName == null) {
			throw new ServletException(
					"Cannot instantiate authentication filter: no authentication provider set. init-param "
							+ AUTHENTICATION_PROVIDER_PARAM + " missing");
		}

		try {
			final Class<?> authenticationProviderClass = Class.forName(authenticationProviderClassName);
			authenticationProvider = (AuthenticationProvider) authenticationProviderClass.newInstance();
			if (authenticationProvider instanceof SpringSecurityAuthenticationProvider) {
				((SpringSecurityAuthenticationProvider) authenticationProvider).setBpmTenantService(bpmTenantService);
				((SpringSecurityAuthenticationProvider) authenticationProvider)
				.setUserDetailsService(userDetailsService);
				((SpringSecurityAuthenticationProvider) authenticationProvider).setTenantDBResolver(tenantDBResolver);
				((SpringSecurityAuthenticationProvider) authenticationProvider)
				.setMasterUserRepository(masterUserRepository);
			}
		} catch (final ClassNotFoundException e) {
			throw new ServletException("Cannot instantiate authentication filter: authentication provider not found",
					e);
		} catch (final InstantiationException e) {
			throw new ServletException(
					"Cannot instantiate authentication filter: cannot instantiate authentication provider", e);
		} catch (final IllegalAccessException e) {
			throw new ServletException("Cannot instantiate authentication filter: constructor not accessible", e);
		} catch (final ClassCastException e) {
			throw new ServletException(
					"Cannot instantiate authentication filter: authentication provider does not implement interface "
							+ AuthenticationProvider.class.getName(),
							e);
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse resp = (HttpServletResponse) response;

		final String engineName = extractEngineName(req);

		if (engineName == null) {
			chain.doFilter(request, response);
			return;
		}

		final ProcessEngine engine = getAddressedEngine(engineName);

		if (engine == null) {
			resp.sendError(404, "Process engine " + engineName + " not available");
			return;
		}

		final AuthenticationResult authenticationResult = authenticationProvider.extractAuthenticatedUser(req, engine);
		if (authenticationResult.isAuthenticated()) {
			final Authentications authentications = Authentications.getFromSession(req.getSession());
			final String authenticatedUser = authenticationResult.getAuthenticatedUser();

			if (!existisAuthentication(authentications, engineName, authenticatedUser)) {
				final List<String> groups = authenticationResult.getGroups();
				final List<String> tenants = authenticationResult.getTenants();

				final Authentication authentication = createAuthentication(engine, authenticatedUser, groups, tenants);
				authentications.addAuthentication(authentication);
			}

			chain.doFilter(request, response);
		} else {
			resp.setStatus(Status.UNAUTHORIZED.getStatusCode());
			authenticationProvider.augmentResponseByAuthenticationChallenge(resp, engine);
		}

	}

	protected String getRequestUri(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		final String contextPath = request.getContextPath();

		final int contextPathLength = contextPath.length();
		if (contextPathLength > 0) {
			requestURI = requestURI.substring(contextPathLength);
		}

		final ServletContext servletContext = request.getServletContext();
		final String applicationPath = ServletContextUtil.getAppPath(servletContext);
		final int applicationPathLength = applicationPath.length();

		if (applicationPathLength > 0) {
			requestURI = requestURI.substring(applicationPathLength);
		}

		return requestURI;
	}

	protected String extractEngineName(HttpServletRequest request) {
		final String requestUri = getRequestUri(request);
		final String requestMethod = request.getMethod();

		final Matcher appMatcher = APP_PATTERN.matcher(requestUri);
		if (appMatcher.matches()) {
			return appMatcher.group(2);
		}

		final Matcher apiEngineMatcher = API_ENGINE_PATTERN.matcher(requestUri);
		if (apiEngineMatcher.matches()) {
			return apiEngineMatcher.group(1);
		}

		final Matcher apiStaticPluginPattern = API_STATIC_PLUGIN_PATTERN.matcher(requestUri);
		if (requestMethod.equals("GET") && apiStaticPluginPattern.matches()) {
			return null;
		}

		final Matcher apiPluginPattern = API_PLUGIN_PATTERN.matcher(requestUri);
		if (apiPluginPattern.matches()) {
			return apiPluginPattern.group(2);
		}

		return null;
	}

	protected ProcessEngine getAddressedEngine(String engineName) {
		return ProcessEngineUtil.lookupProcessEngine(engineName);
	}

	protected boolean existisAuthentication(Authentications authentications, String engineName, String username) {
		// For each process engine, there can be at most one authentication active in a given session.
		final Authentication authentication = authentications.getAuthenticationForProcessEngine(engineName);
		return authentication != null && isAuthenticated(authentication, engineName, username);
	}

	protected boolean isAuthenticated(Authentication authentication, String engineName, String username) {
		final String processEngineName = authentication.getProcessEngineName();
		final String identityId = authentication.getIdentityId();
		return processEngineName.equals(engineName) && identityId.equals(username);
	}

	protected Authentication createAuthentication(ProcessEngine processEngine, String username, List<String> groups, List<String> tenants) {
		return userAuthentications.createAuthenticate(processEngine, username, groups, tenants);
	}


}

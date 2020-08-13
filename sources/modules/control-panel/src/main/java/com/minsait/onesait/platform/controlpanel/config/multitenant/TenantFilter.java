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
package com.minsait.onesait.platform.controlpanel.config.multitenant;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.microsoft.sqlserver.jdbc.StringUtils;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

@Configuration
public class TenantFilter {

	private static final String VERTICAL_PARAMETER = "vertical";

	private static final String TENANT_PARAMETER = "tenant";

	private static final String REFERER = "Referer";

	@Bean
	public FilterRegistrationBean setTenantIfPossible(VerticalResolver tenantDBResolver,
			MultitenancyService masterUserService) {
		final FilterRegistrationBean filter = new FilterRegistrationBean();
		filter.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
					MultitenancyContextHolder
							.setVerticalSchema(((UserPrincipal) auth.getPrincipal()).getVerticalSchema());
					MultitenancyContextHolder.setTenantName(((UserPrincipal) auth.getPrincipal()).getTenant());

				}
				filterChain.doFilter(request, response);
				MultitenancyContextHolder.clear();

			}

		});
		filter.addUrlPatterns("/*");
		filter.setName("setTenantIfPossible");
		filter.setOrder(Ordered.LOWEST_PRECEDENCE);
		return filter;
	}

	@Bean
	public FilterRegistrationBean setTenantFromParameterForPublicURL(VerticalResolver tenantDBResolver,
			MultitenancyService masterUserService) {
		final FilterRegistrationBean filter = new FilterRegistrationBean();
		filter.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				final String vertical = request.getParameter(VERTICAL_PARAMETER);
				final String referer = extractParamFromReferer(request.getHeader(REFERER), VERTICAL_PARAMETER);
				if (!StringUtils.isEmpty(vertical)) {
					masterUserService.getVertical(vertical)
							.ifPresent(v -> MultitenancyContextHolder.setVerticalSchema(v.getSchema()));
				} else if (referer != null) {
					masterUserService.getVertical(referer)
							.ifPresent(v -> MultitenancyContextHolder.setVerticalSchema(v.getSchema()));
					Optional.ofNullable(extractParamFromReferer(request.getHeader(REFERER), TENANT_PARAMETER))
							.ifPresent(MultitenancyContextHolder::setTenantName);
				}

				filterChain.doFilter(request, response);
				MultitenancyContextHolder.clear();

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
					return queryMap.stream().filter(e -> e.getName().equals(parameter)).map(NameValuePair::getValue)
							.findFirst().orElse(null);
				}
				return null;
			}

		});
		filter.addUrlPatterns("/dashboards/view/*", "/dashboards/model/*", "/dashboards/editfulliframe/*",
				"/dashboards/viewiframe/*", "/viewers/view/*", "/viewers/viewiframe/*", "/gadgets/*", "/viewers/*",
				"/datasources/*", "/layer/*", "/files/*", "/binary-repository/*");
		filter.setName("setTenantFromParameterForPublicURL");
		filter.setOrder(Ordered.LOWEST_PRECEDENCE);
		return filter;
	}

}

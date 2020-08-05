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
package com.minsait.onesait.platform.security.jwt.custom;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientIdParameterProxyFilter extends GenericFilterBean {

	private static String clientId = "client_id";

	private static final String VERTICAL = "vertical";
	private static final String TENANT = "tenant";
	private static final String ERROR_MSG_MULTITENANCY = "Request must contain vertical and tenant parameters.";
	private static final String ERROR_MSG_VERTICAL = "Request must contain vertical and tenant parameters.";
	@Value("${onesaitplatform.multitenancy.enabled:false}")
	private boolean multitenancyEnabled;

	@Autowired
	private MultitenancyService multitenancyService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (multitenancyEnabled && !StringUtils.isEmpty(request.getParameter(VERTICAL))) {

			final Optional<Vertical> vertical = multitenancyService.getVertical(request.getParameter(VERTICAL));
			vertical.ifPresent(v -> {
				MultitenancyContextHolder.setVerticalSchema(v.getSchema());
				MultitenancyContextHolder.setForced(true);
			});

		}

		if (!StringUtils.isEmpty(request.getParameter(clientId)))
			chain.doFilter(new CustomHttpServletRequestWrapper((HttpServletRequest) request), response);
		else
			chain.doFilter(request, response);

		MultitenancyContextHolder.clear();
	}

}

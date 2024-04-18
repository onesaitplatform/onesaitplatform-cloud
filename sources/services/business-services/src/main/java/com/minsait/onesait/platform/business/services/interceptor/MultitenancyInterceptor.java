/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.business.services.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MultitenancyInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private MultitenancyService multitenancyService;

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
			throws Exception {

		final String vertical = request.getHeader(Tenant2SchemaMapper.VERTICAL_HTTP_HEADER);
		if (!StringUtils.isEmpty(vertical)) {
			multitenancyService.getVerticalSchema(vertical).ifPresent(v -> {
				final String tenant = request.getHeader(Tenant2SchemaMapper.TENANT_HTTP_HEADER);
				MultitenancyContextHolder.setTenantName(tenant);
				MultitenancyContextHolder.setVerticalSchema(v);
			});
		}
		return true;
	}

	@Override
	public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
			final Object handler, final Exception ex) throws Exception {
		MultitenancyContextHolder.clear();
	}

	public static void addMultitenantHeaders(HttpHeaders headers) {
		try {
			headers.add(Tenant2SchemaMapper.VERTICAL_HTTP_HEADER, MultitenancyContextHolder.getVerticalSchema());
			headers.add(Tenant2SchemaMapper.TENANT_HTTP_HEADER, MultitenancyContextHolder.getTenantName());
		} catch (final Exception e) {
			log.error("No tenant or vertical set");
		}
	}

}

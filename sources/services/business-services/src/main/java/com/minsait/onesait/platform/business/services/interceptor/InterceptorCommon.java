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
package com.minsait.onesait.platform.business.services.interceptor;

import static com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterceptorCommon {

	public static final String SESSION_ATTR_PREVIOUS_AUTH = "PREVIOUS_AUTH";

	private InterceptorCommon() {
		throw new IllegalStateException();
	}

	public static void setContexts(Authentication auth) {
		SecurityContextHolder.getContext().setAuthentication(auth);
		try {
			log.debug("setContexts for authentication {}, of class: {} with principal class: {}", auth.getName(), auth.getClass().getCanonicalName(), auth.getPrincipal().getClass().getCanonicalName());
			MultitenancyContextHolder.setVerticalSchema(((UserPrincipal) auth.getPrincipal()).getVerticalSchema());
			MultitenancyContextHolder.setTenantName(((UserPrincipal) auth.getPrincipal()).getTenant());
		}catch (final Exception e) {
			log.error("Error in setContexts with auth {}", auth.getPrincipal() );
			MultitenancyContextHolder.setVerticalSchema(Tenant2SchemaMapper.DEFAULT_SCHEMA);
			MultitenancyContextHolder.setTenantName(Tenant2SchemaMapper.defaultTenantName(DEFAULT_VERTICAL_NAME));
		}
	}

	public static void clearContexts(HttpSession session) {
		if (session != null) {
			final Authentication auth = (Authentication) session
					.getAttribute(InterceptorCommon.SESSION_ATTR_PREVIOUS_AUTH);
			MultitenancyContextHolder.clear();
			SecurityContextHolder.getContext().setAuthentication(auth);
			session.removeAttribute(SESSION_ATTR_PREVIOUS_AUTH);
		}
	}

	public static void clearMultitenancyContext() {
		MultitenancyContextHolder.clear();
	}

	public static void setPreviousAuthenticationOnSession(HttpSession session) {
		if (session != null && session.getAttribute(SESSION_ATTR_PREVIOUS_AUTH) == null) {
			session.setAttribute(InterceptorCommon.SESSION_ATTR_PREVIOUS_AUTH,
					SecurityContextHolder.getContext().getAuthentication());
		}
	}

}

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

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class InterceptorCommon {

	public static final String SESSION_ATTR_PREVIOUS_AUTH = "PREVIOUS_AUTH";

	private InterceptorCommon() {
		throw new IllegalStateException();
	}

	public static void setContexts(Authentication auth) {
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	public static void clearContexts(Authentication auth, HttpSession session) {
		SecurityContextHolder.getContext().setAuthentication(auth);
		session.removeAttribute(SESSION_ATTR_PREVIOUS_AUTH);
	}

	public static void setPreviousAuthenticationOnSession(HttpSession session) {
		if (session.getAttribute(SESSION_ATTR_PREVIOUS_AUTH) == null)
			session.setAttribute(InterceptorCommon.SESSION_ATTR_PREVIOUS_AUTH,
					SecurityContextHolder.getContext().getAuthentication());
	}
}

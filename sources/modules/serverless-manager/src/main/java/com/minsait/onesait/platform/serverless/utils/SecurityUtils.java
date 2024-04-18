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
package com.minsait.onesait.platform.serverless.utils;

import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtils {

	public static String getCurrentUser() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public static String getCurrentRole() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
	}

	public static boolean isAdmin() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority().contains("ADMIN");
	}
}

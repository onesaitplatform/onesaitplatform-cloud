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
package com.minsait.onesait.platform.bpm.util;

import java.util.List;

import org.camunda.bpm.engine.authorization.Groups;

import com.minsait.onesait.platform.config.model.Role;

public class RoleConverterUtil {

	private static final String ADMIN_ROLE = "administrator";
	private static final String CAMUNDA_PREFIX = "camunda-";
	private static final String ROLE_PREFIX = "ROLE_";

	private RoleConverterUtil() throws IllegalAccessException {
		throw new IllegalAccessException("Util class, do not instantiate");
	}

	public static String opRoleToCamunda(String role) {
		boolean platformRole = false;
		if (List.of(Role.Type.values()).stream().map(t -> t.name()).toList().contains(role)) {
			platformRole = true;
		}

		if (platformRole) {
			final String roleSuffix = role.toUpperCase().contains(ROLE_PREFIX) ? role.substring(5).toLowerCase() : role;
			if (roleSuffix.equalsIgnoreCase(ADMIN_ROLE)) {
				return Groups.CAMUNDA_ADMIN;
			} else {
				return CAMUNDA_PREFIX + roleSuffix;
			}
		} else {
			return role;
		}

	}

}

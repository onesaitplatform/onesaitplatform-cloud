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
package com.minsait.onesait.platform.controlpanel.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Service
public class SecurityService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AppWebUtils utils;

	public boolean hasAnyRole(List<String> roles) {
		final Role role = roleRepository.findById(utils.getRole()).orElse(null);
		if (role == null)
			return false;
		for (final String roleName : roles) {
			final Role roleAuth = roleRepository.findById(roleName).orElse(null);
			if (roleAuth == null)
				return false;
			if (role.getId().equalsIgnoreCase(roleAuth.getId()) || (role.getRoleParent() != null
					&& role.getRoleParent().getId().equalsIgnoreCase(roleAuth.getId()))) {
				return true;
			}
		}
		return false;
	}

}

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
package com.minsait.onesait.platform.bpm.security;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.minsait.onesait.platform.bpm.security.camunda.ContainerBasedAuthenticationProvider;
import com.minsait.onesait.platform.bpm.util.RoleConverterUtil;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class SpringSecurityAuthenticationProvider extends ContainerBasedAuthenticationProvider {

	@Setter
	private UserDetailsService userDetailsService;
	@Setter
	private VerticalResolver tenantDBResolver;
	@Setter
	private MasterUserRepository masterUserRepository;
	@Setter
	private IdentityService identityService;

	public SpringSecurityAuthenticationProvider(UserDetailsService userDetailsService,
			MasterUserRepository masterUserRepository) {
		this.userDetailsService = userDetailsService;
		this.masterUserRepository = masterUserRepository;
	}

	@Override
	public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine) {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return AuthenticationResult.unsuccessful();
		}

		final String name = authentication.getName();
		if (name == null || name.isEmpty()) {
			return AuthenticationResult.unsuccessful();
		}

		final AuthenticationResult authenticationResult = new AuthenticationResult(name, true);
		authenticationResult.setGroups(getUserGroups(authentication));

		authenticationResult.setTenants(
				identityService.createTenantQuery().userMember(name).list().stream().map(Tenant::getId).toList());
		if (log.isDebugEnabled()) {
			log.debug("Authentication success webapp ROLE: {}, TENANTS: {}",
				String.join(",", authenticationResult.getGroups()),
				String.join(",", authenticationResult.getTenants()));
		}
		return authenticationResult;
	}

	private List<String> getUserGroups(Authentication authentication) {

		List<String> groupIds;

		groupIds = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.map(RoleConverterUtil::opRoleToCamunda).collect(Collectors.toList());
		return groupIds;

	}

}
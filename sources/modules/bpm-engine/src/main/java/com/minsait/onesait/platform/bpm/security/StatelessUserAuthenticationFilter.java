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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.rest.util.EngineUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.minsait.onesait.platform.bpm.util.RoleConverterUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatelessUserAuthenticationFilter implements Filter {

	private final IdentityService identityService;

	public StatelessUserAuthenticationFilter(IdentityService identityService) {
		this.identityService = identityService;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// Current limitation: Only works for the default engine
		final ProcessEngine engine = EngineUtil.lookupProcessEngine("default");

		final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String username;

		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername();
		} else {
			username = principal.toString();
		}

		try {
			engine.getIdentityService().setAuthentication(username, getUserGroups(), identityService.createTenantQuery()
					.userMember(username).list().stream().map(Tenant::getId).toList());
			log.info("Authentication success webapp ROLE: {}, TENANTS: {}", String.join(",", getUserGroups()),
					String.join(",", identityService.createTenantQuery().userMember(username).list().stream()
							.map(Tenant::getId).toList()));
			chain.doFilter(request, response);
		} finally {
			clearAuthentication(engine);

		}

	}

	@Override
	public void destroy() {
		// noop

	}

	private void clearAuthentication(ProcessEngine engine) {
		engine.getIdentityService().clearAuthentication();
	}

	private List<String> getUserGroups() {

		List<String> groupIds;

		final org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();

		groupIds = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.map(RoleConverterUtil::opRoleToCamunda).collect(Collectors.toList());

		return groupIds;

	}

}

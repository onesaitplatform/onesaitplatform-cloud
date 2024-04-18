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
package com.minsait.onesait.platform.security.ri;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserTokenRepository;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConfigDBDetailsService implements UserDetailsService {

	@Autowired
	private MasterUserRepository masterUserRepository;
	@Autowired
	private MasterUserTokenRepository masterUserTokenRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VerticalResolver tenantDBResolver;

	@Value("${onesaitplatform.authentication.twofa:false}")
	private boolean tfaEnabled;

	@Override
	public UserDetails loadUserByUsername(String username) {

		final MasterUser user = masterUserRepository.findByUserId(username);

		if (user == null) {
			log.info("LoadUserByUserName: User not found by name: {}", username);
			throw new UsernameNotFoundException("User not found by name: " + username);
		}

		String role = Role.Type.ROLE_PREVERIFIED_TENANT_USER.name();
		Vertical vertical = null;
		final String tenant = user.getTenant().getName();
		if (tenantDBResolver.hasSingleTenantSchemaAssociated(user) || MultitenancyContextHolder.isForced()) {

			vertical = tenantDBResolver.getSingleVerticalIfPossible(user);
			MultitenancyContextHolder.setVerticalSchema(vertical.getSchema());
			MultitenancyContextHolder.setTenantName(tenant);
			role = userRepository.findByUserId(user.getUserId()).getRole().getId();

		}

		return toUserDetails(user.getUserId(), user.getPassword(), vertical, tenant, role);
	}

	public UserDetails loadUserByUserToken(String token) {

		final MasterUserToken userToken = masterUserTokenRepository.findByToken(token);

		if (userToken == null) {
			log.info("LoadUserByUserToken: User not found by token: {}", token);
			return null;
		}

		MultitenancyContextHolder.setVerticalSchema(userToken.getVertical().getSchema());
		MultitenancyContextHolder.setTenantName(userToken.getTenant().getName());

		return toUserDetails(userToken.getMasterUser().getUserId(), userToken.getMasterUser().getPassword(),
				userToken.getVertical(), userToken.getTenant().getName(),
				userRepository.findByUserId(userToken.getMasterUser().getUserId()).getRole().getId());
	}

	private org.springframework.security.core.userdetails.User toUserDetails(String username, String password,
			Vertical vertical, String tenant, String role) {
		return new UserPrincipal(username, password, Arrays.asList(new SimpleGrantedAuthority(role)), vertical, tenant);

	}

}

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
package com.minsait.onesait.platform.controlpanel.security;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
public class SAMLUserDetails implements SAMLUserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Value("${onesaitplatform.authentication.saml.admin_user_id}")
	private String admin;
	@Value("${onesaitplatform.authentication.default_password:changeIt9900!}")
	private String defaultPassword;

	@Autowired
	private VerticalResolver tenantDBResolver;
	@Autowired
	private MasterUserRepository masterUserRepository;

	@Override
	public Object loadUserBySAML(SAMLCredential credential) {
		log.debug("New user logged from SAML server {}", credential.getNameID().getValue());
		final String username = credential.getNameID().getValue();
		MasterUser user = masterUserRepository.findByUserId(username);
		if (user == null) {
			log.info("LoadUserByUserName: User not found by name: {}", username);
			log.info("Creating SAML user on default vertical");
			final User newUser = importUserToDB(username);
			user = masterUserRepository.findByUserId(username);
			if (user == null)
				throw new UsernameNotFoundException("Could not create CAS user: " + username);
			return toUserDetails(user.getUserId(), user.getPassword(),
					tenantDBResolver.getSingleVerticalIfPossible(user), user.getTenant().getName(),
					newUser.getRole().getId());
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

	private org.springframework.security.core.userdetails.User toUserDetails(String username, String password,
			Vertical vertical, String tenant, String role) {
		return new UserPrincipal(username, password, Arrays.asList(new SimpleGrantedAuthority(role)), vertical, tenant);

	}

	private User importUserToDB(String username) {
		final User user = new User();
		if (username.equals(admin))
			user.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.name()));
		else
			user.setRole(roleRepository.findById(Role.Type.ROLE_USER.name()));
		user.setUserId(username);
		user.setActive(true);
		user.setEmail(username.concat("@domain.com"));
		user.setPassword(defaultPassword + UUID.randomUUID().toString().substring(1, 5));
		user.setFullName(username);
		return userRepository.save(user);

	}
}

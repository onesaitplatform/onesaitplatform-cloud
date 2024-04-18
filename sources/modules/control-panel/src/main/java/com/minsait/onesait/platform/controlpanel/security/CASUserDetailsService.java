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
package com.minsait.onesait.platform.controlpanel.security;

import java.util.Arrays;
import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "cas")
public class CASUserDetailsService extends AbstractCasAssertionUserDetailsService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private MasterUserRepository masterUserRepository;
	@Autowired
	private ObjectMapper mapper;
	@Value("${onesaitplatform.authentication.cas.attributes.mail:mail}")
	private String mail;
	@Value("${onesaitplatform.authentication.cas.attributes.mail_suffix:onesaitplatform.com}")
	private String mailSuffix;
	@Value("${onesaitplatform.authentication.cas.attributes.fullName:name}")
	private String fullName;
	@Value("${onesaitplatform.authentication.default_password:changeIt9900!}")
	private String defaultPassword;
	@Autowired
	private VerticalResolver tenantDBResolver;

	@Override
	protected UserDetails loadUserDetails(Assertion assertion) {
		if (log.isDebugEnabled()) {
			log.debug("New user logged from CAS server {}", assertion.getPrincipal().getName());
		}		
		final String username = assertion.getPrincipal().getName();
		MasterUser user = masterUserRepository.findByUserId(username);

		if (user == null) {
			log.info("LoadUserByUserName: User not found by name: {}", username);
			log.info("Creating CAS user on default vertical");
			final User newUser = importUserToDB(username, assertion.getPrincipal().getAttributes());
			user = masterUserRepository.findByUserId(username);
			if (user == null) {
				throw new UsernameNotFoundException("Could not create CAS user: " + username);
			}
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

	private UserDetails toUserDetails(User user) {
		return org.springframework.security.core.userdetails.User.withUsername(user.getUserId()).password("")
				.authorities(new SimpleGrantedAuthority(user.getRole().getId())).build();
	}

	private User importUserToDB(String username, Map<String, Object> attributes) {
		final User user = new User();
		log.info("Attributes are: ");
		attributes.entrySet().forEach(e -> log.info("Key {} with value {}", e.getKey(), e.getValue()));

		user.setRole(roleRepository.findById(Role.Type.ROLE_USER.name()).orElse(null));
		user.setUserId(username);
		user.setActive(true);
		if (attributes.get(mail) == null || !((String) attributes.get(mail)).contains("@") ) {
			log.warn("No mail from CAS attributes, setting mail to: {}", username + "@"+ mailSuffix);
			user.setEmail(username + "@"+ mailSuffix);
		} else {
			user.setEmail((String) attributes.get(mail));
		}
		user.setPassword(defaultPassword);
		user.setFullName((String) attributes.get(fullName));
		try {
			user.setExtraFields(mapper.writeValueAsString(attributes));
		} catch (final JsonProcessingException e) {
			log.warn("could not add extrafields for user: {}", username);
		}
		return userRepository.save(user);

	}

	private org.springframework.security.core.userdetails.User toUserDetails(String username, String password,
			Vertical vertical, String tenant, String role) {
		return new UserPrincipal(username, password, Arrays.asList(new SimpleGrantedAuthority(role)), vertical, tenant);

	}

}

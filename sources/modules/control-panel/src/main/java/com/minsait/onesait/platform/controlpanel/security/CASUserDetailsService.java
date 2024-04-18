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

import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

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
	private ObjectMapper mapper;
	@Value("${onesaitplatform.authentication.cas.attributes.mail:mail}")
	private String mail;
	@Value("${onesaitplatform.authentication.cas.attributes.fullName:name}")
	private String fullName;
	@Value("${onesaitplatform.authentication.default_password:changeIt9900!}")
	private String defaultPassword;

	@Override
	protected UserDetails loadUserDetails(Assertion assertion) {
		log.debug("New user logged from CAS server {}", assertion.getPrincipal().getName());

		if (userRepository.findByUserId(assertion.getPrincipal().getName()) == null)
			return toUserDetails(
					importUserToDB(assertion.getPrincipal().getName(), assertion.getPrincipal().getAttributes()));
		else
			return toUserDetails(userRepository.findByUserId(assertion.getPrincipal().getName()));
	}

	private UserDetails toUserDetails(User user) {
		return org.springframework.security.core.userdetails.User.withUsername(user.getUserId()).password("")
				.authorities(new SimpleGrantedAuthority(user.getRole().getId())).build();
	}

	private User importUserToDB(String username, Map<String, Object> attributes) {
		final User user = new User();

		user.setRole(roleRepository.findById(Role.Type.ROLE_USER.name()));
		user.setUserId(username);
		user.setActive(true);
		user.setEmail((String) attributes.get(mail));
		user.setPassword(defaultPassword);
		user.setFullName((String) attributes.get(fullName));
		try {
			user.setExtraFields(mapper.writeValueAsString(attributes));
		} catch (final JsonProcessingException e) {
			log.warn("could not add extrafields for user: {}", username);
		}
		return userRepository.save(user);

	}

}

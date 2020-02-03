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

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

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

	@Override
	public Object loadUserBySAML(SAMLCredential credential) {

		User user = userRepository.findByUserId(credential.getNameID().getValue());
		if (user == null) {
			log.info("LoadUserByUserName: User not found by name: " + credential.getNameID().getValue());
			log.info("Importing user {} to configdb", credential.getNameID().getValue());
			user = importUserToDB(credential.getNameID().getValue());
			// throw new UsernameNotFoundException("User not found by name: " +
			// credential.getNameID().getValue());
		}

		return toUserDetails(user);
	}

	private UserDetails toUserDetails(User userObject) {
		return org.springframework.security.core.userdetails.User.withUsername(userObject.getUserId())
				.password(userObject.getPassword())
				.authorities(new SimpleGrantedAuthority(userObject.getRole().getId())).build();
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
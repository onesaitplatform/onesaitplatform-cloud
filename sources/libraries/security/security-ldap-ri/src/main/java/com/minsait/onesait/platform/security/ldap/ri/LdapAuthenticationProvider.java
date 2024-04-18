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
package com.minsait.onesait.platform.security.ldap.ri;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.security.PasswordEncoder;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.security.ldap.ri.component.LdapUserMapper;
import com.minsait.onesait.platform.security.ldap.ri.config.LdapConfig;
import com.minsait.onesait.platform.security.ldap.ri.service.LdapUserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "ldap")
public class LdapAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	@Qualifier(LdapConfig.LDAP_TEMPLATE_BASE)
	private LdapTemplate ldapTemplate;
	@Autowired
	private LdapUserService ldapUserService;
	@Autowired
	private UserRepository userRepository;
	@Value("${ldap.base-auth}")
	private String ldapBaseAuth;

	@Override
	public Authentication authenticate(Authentication authentication) {
		final String userId = authentication.getName();
		final Object credentials = authentication.getCredentials();
		log.info("redentials class: {} ", credentials.getClass());
		if (!(credentials instanceof String)) {
			return null;
		}
		final String password = credentials.toString();
		final AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "person"));
		filter.and(new EqualsFilter("uid", userId));
		final boolean authenticated = ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
		final List<User> matches = ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(),
				new LdapUserMapper());

		if (authenticated) {
			User user = userRepository.findByUserId(userId);
			if (user == null) {
				user = matches.get(0);
				user.setUserId(userId);
				user.setPassword(password);
				user = ldapUserService.createUser(user);
			}

			final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
			grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole().getId()));
			return new UsernamePasswordAuthenticationToken(userId, password, grantedAuthorities);
		} else {

			if (!matches.isEmpty()) {
				throw new BadCredentialsException("Wrong password for user " + userId);
			} else {
				final User user = userRepository.findByUserId(userId);
				if (user != null && user.isActive()) {
					log.debug("User {} does not exist in LDAP, but exists in the ConfigDB, validating password...",
							userId);
					try {
						if (user.getPassword().equals(PasswordEncoder.getInstance().encodeSHA256(password))) {
							final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
							grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole().getId()));
							return new UsernamePasswordAuthenticationToken(userId, password, grantedAuthorities);
						} else {
							throw new BadCredentialsException("Wrong password for user " + userId);
						}
					} catch (final Exception e) {
						throw new BadCredentialsException(
								"Could not authenticate user " + userId + " failed to encrypt the password");
					}

				} else {
					throw new BadCredentialsException("User " + userId + " does not exist or is inactive");
				}
			}

		}

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}

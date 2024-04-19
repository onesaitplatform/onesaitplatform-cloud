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

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.security.PasswordEncoder;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.security.ldap.ri.component.LdapUserMapper;
import com.minsait.onesait.platform.security.ldap.ri.config.LdapConfig;
import com.minsait.onesait.platform.security.ldap.ri.service.LdapUserService;

import lombok.extern.slf4j.Slf4j;

@Component("ldapAuthenticationProvider")
@Slf4j
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "ldap")
public class LdapAuthenticationProvider implements AuthenticationProvider {
	private static final String OBJECT_CLASS_STR = "objectClass";
	private static final String PERSON_STR = "person";
	@Autowired
	@Qualifier(LdapConfig.LDAP_TEMPLATE_BASE)
	private LdapTemplate ldapTemplateBase;
	@Autowired
	private LdapUserService ldapUserService;
	@Autowired
	private UserRepository userRepository;
	@Value("${ldap.attributesMap.userId}")
	private String userIdAtt;
	@Value("${ldap.attributesMap.mail}")
	private String userMailAtt;
	@Value("${ldap.attributesMap.cn}")
	private String userCnAtt;
	@Value("${ldap.rolesmemberattribute}")
	private String memberAtt;
	@Autowired
	private UserDetailsService userDetails;

	// TO-DO logica de creado de usuarios multitenant

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		final String userId = authentication.getName();
		final Object credentials = authentication.getCredentials();
		log.info("credentials class: " + credentials.getClass());
		if (!(credentials instanceof String)) {
			return null;
		}
		final String password = credentials.toString();
		final AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(OBJECT_CLASS_STR, PERSON_STR));
		filter.and(new EqualsFilter(userIdAtt, userId));
		boolean authenticated = false;
		try {
			authenticated = ldapTemplateBase.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
		} catch (final Exception e) {
			log.error("Error authenticating with LDAP", e);
			throw e;
		}
		final List<User> matches;

		try {
			matches = ldapTemplateBase.search(LdapUtils.emptyLdapName(), filter.encode(),
					new LdapUserMapper(userIdAtt, userMailAtt, userCnAtt));
		} catch (final Exception e) {
			log.error("Could not map user from LDAP", e);
			throw e;
		}

		if (authenticated) {
			final List<String> groups = ldapTemplateBase
					.search(LdapUtils.emptyLdapName(), filter.encode(), (AttributesMapper<List<String>>) attributes -> {
						final Enumeration<String> enMember = (Enumeration<String>) attributes.get(memberAtt).getAll();
						return Collections.list(enMember);
					}).get(0);

			User user = userRepository.findByUserId(userId);
			if (user == null) {
				user = ldapUserService.createUser(matches.get(0), password, groups);
			} else {
				ldapUserService.updateUserRole(user, groups);
			}

			final UserDetails details = userDetails.loadUserByUsername(userId);
			return new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities());
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
							final UserDetails details = userDetails.loadUserByUsername(userId);
							return new UsernamePasswordAuthenticationToken(details, details.getPassword(),
									details.getAuthorities());
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
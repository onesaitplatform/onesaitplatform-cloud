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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.security.PasswordEncoder;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j

@ConditionalOnExpression("'${onesaitplatform.authentication.provider}' == 'cas' or '${onesaitplatform.authentication.provider}' == 'configdb'")
@Qualifier("configDBAuthenticationProvider")
public class ConfigDBAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Value("${onesaitplatform.authentication.twofa.enabled:false}")
	private boolean tfaEnabled;

	@Override
	public Authentication authenticate(Authentication authentication) {

		final String name = authentication.getName();
		final Object credentials = authentication.getCredentials();
		log.trace("credentials class: " + credentials.getClass());
		if (!(credentials instanceof String)) {
			return null;
		}
		final String password = credentials.toString();

		final User user = userRepository.findByUserId(name);

		if (user == null) {
			log.info("authenticate: User not exist: {}", name);
			throw new BadCredentialsException("Authentication failed. User not exists: " + name);
		}

		if (!user.isActive()) {
			log.info("authenticate: User not active: {}", name);
			throw new BadCredentialsException("Authentication failed. User deactivated: " + name);
		}
		String hashPassword = null;
		try {
			hashPassword = PasswordEncoder.getInstance().encodeSHA256(password);
		} catch (final Exception e) {
			log.error("Authenticate: Error encoding: ",e.getMessage());
			throw new BadCredentialsException("Authentication failed. Error authenticating.");
		}
		if (!hashPassword.equals(user.getPassword())) {
			log.info("authenticate: Password incorrect: {} ", name);
			publishFailureCredentials(user, authentication);
			throw new BadCredentialsException("Authentication failed. Password incorrect for " + name);
		}
		String role = user.getRole().getId();
		if (tfaEnabled && role.equals(Role.Type.ROLE_ADMINISTRATOR.name()) && authentication.getDetails() != null
				&& authentication.getDetails() instanceof WebAuthenticationDetails)
			role = Role.Type.ROLE_PREVERIFIED_ADMINISTRATOR.name();
		final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		grantedAuthorities.add(new SimpleGrantedAuthority(role));

		final Authentication auth = new UsernamePasswordAuthenticationToken(user.getUserId(), password,
				grantedAuthorities);

		publishSuccess(auth);
		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	@Async
	private void publishSuccess(Authentication auth) {
		applicationEventPublisher.publishEvent(new AuthenticationSuccessEvent(auth));
	}

	@Async
	private void publishFailureCredentials(User user, Authentication authentication) {
		final Authentication auth = new UsernamePasswordAuthenticationToken(user.getUserId(), "", new ArrayList<>());

		applicationEventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(auth,
				new BadCredentialsException("Authentication failed. Password incorrect for " + user.getUserId())));
	}

}

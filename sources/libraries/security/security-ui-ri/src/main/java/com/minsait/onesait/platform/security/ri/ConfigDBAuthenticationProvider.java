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
package com.minsait.onesait.platform.security.ri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.security.PasswordEncoder;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserParent;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepositoryLazy;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j

@ConditionalOnExpression("'${onesaitplatform.authentication.provider}' == 'cas' or '${onesaitplatform.authentication.provider}' == 'configdb' or '${onesaitplatform.authentication.provider}' == 'saml'")
@Qualifier("configDBAuthenticationProvider")
public class ConfigDBAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private MasterUserRepositoryLazy masterUserRepository;

	@Autowired
	private ConfigDBDetailsService userDetails;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Value("${onesaitplatform.authentication.twofa.enabled:false}")
	private boolean tfaEnabled;

	@Value("${onesaitplatform.authentication.configdb.acl.enabled:false}")
	private boolean aclEnabled;

	private Set<String> acl;

	@Autowired
	public void setAcl(@Value("${onesaitplatform.authentication.configdb.acl.list}") final String strs) {
		final List<String> clList = Arrays.asList(strs.split(","));
		acl = new HashSet<>(clList);
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		final long start = System.currentTimeMillis();
		log.debug("Starting configDB authentication");
		final String name = authentication.getName();
		final Object credentials = authentication.getCredentials();
		log.trace("credentials class: " + credentials.getClass());
		if (!(credentials instanceof String)) {
			return null;
		}
		final String password = credentials.toString();

		if (aclEnabled && !acl.contains(name)) {
			log.warn("authenticate: User is not allowed to make login: {}", name);
			throw new BadCredentialsException("Authentication failed. User is not in the ACL: " + name);
		}
		MasterUserLazy user;
		synchronized (this) {
			user = masterUserRepository.findByUserId(name);
		}

		if (user == null) {
			log.warn("authenticate: User not exist: {}", name);
			throw new BadCredentialsException("Authentication failed. User not exists: " + name);
		}

		if (!user.isActive()) {
			log.warn("authenticate: User not active: {}", name);
			throw new BadCredentialsException("Authentication failed. User deactivated: " + name);
		}
		String hashPassword = null;
		try {
			hashPassword = PasswordEncoder.getInstance().encodeSHA256(password);
		} catch (final Exception e) {
			log.error("Authenticate: Error encoding: ", e.getMessage());
			throw new BadCredentialsException("Authentication failed. Error authenticating.");
		}
		if (!hashPassword.equals(user.getPassword())) {
			log.warn("Authentication failed. Password incorrect for {} ", name);
			publishFailureCredentials(user, authentication);
			throw new BadCredentialsException("Authentication failed. Password incorrect for " + name);
		}
		final UserDetails details = userDetails.loadUserByUsername(user);
		Collection<? extends GrantedAuthority> grantedAuthorities = details.getAuthorities();
		if (tfaEnabled
				&& grantedAuthorities.iterator().next().getAuthority().equals(Role.Type.ROLE_ADMINISTRATOR.name())
				&& authentication.getDetails() != null
				&& authentication.getDetails() instanceof WebAuthenticationDetails) {
			grantedAuthorities = Arrays
					.asList(new SimpleGrantedAuthority(Role.Type.ROLE_PREVERIFIED_ADMINISTRATOR.name()));
		}

		final Authentication auth = new UsernamePasswordAuthenticationToken(details, details.getPassword(),
				grantedAuthorities);
		resetFailedAttemp(user);
		publishSuccess(auth);
		if (log.isDebugEnabled()) {
			log.debug("End configDB authentication, time: {}", System.currentTimeMillis() - start);
		}		
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

	@Async
	private void publishFailureCredentials(MasterUserParent user, Authentication authentication) {
		final Authentication auth = new UsernamePasswordAuthenticationToken(user.getUserId(), "", new ArrayList<>());

		applicationEventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(auth,
				new BadCredentialsException("Authentication failed. Password incorrect for " + user.getUserId())));
	}


	private MasterUserParent resetFailedAttemp(MasterUserLazy masterUser) {

		if (masterUser != null && masterUser.getFailedAtemps() != null && masterUser.getFailedAtemps() > 0) {
			masterUser.setFailedAtemps(0);
			masterUser.setLastLogin(new Date());
			masterUserRepository.save(masterUser);
		}
		return masterUser;

	}

}

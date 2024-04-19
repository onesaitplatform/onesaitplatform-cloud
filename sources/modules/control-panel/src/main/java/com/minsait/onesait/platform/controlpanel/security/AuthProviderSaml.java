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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import lombok.extern.slf4j.Slf4j;

//@Configuration
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthProviderSaml extends WebSecurityConfigurerAdapter {

	@Bean
	@Primary
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public AuthenticationProvider samlAuthenticationProvider() {
		final SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
		samlAuthenticationProvider.setUserDetails(new SAMLUserDetailsService() {
			// TO-DO userdetailsservice
			@Override
			public Object loadUserBySAML(SAMLCredential credential) {
				final String userID = credential.getNameID().getValue();

				final List<GrantedAuthority> authorities = new ArrayList<>();
				final GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
				authorities.add(authority);

				// In a real scenario, this implementation has to locate user in a arbitrary
				// dataStore based on information present in the SAMLCredential and
				// returns such a date in a form of application specific UserDetails object.
				return new User(userID, "<abc123>", true, true, true, true, authorities);
			}
		});
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		return samlAuthenticationProvider;
	}

}

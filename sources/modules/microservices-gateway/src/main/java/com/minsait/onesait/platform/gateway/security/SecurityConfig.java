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
package com.minsait.onesait.platform.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Value("${onesaitplatform.urls.insecure:null}")
	private String insecureUrls;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			@Autowired @Qualifier("CustomBearerTokenConverter") ServerAuthenticationConverter jwtConverter) {
		if (insecureUrls != null) {
			final String[] insecure = insecureUrls.split(",");
			http.authorizeExchange().pathMatchers(insecure).permitAll();
		}

		http.authorizeExchange().pathMatchers("/fn/invoke/**", "/fn/t/**").authenticated();

		http.csrf().disable().authorizeExchange().pathMatchers("/controlpanel/**", "/dashboardengine/**", "/", "/fn/**")
		.permitAll().anyExchange().authenticated().and().oauth2ResourceServer()
		.bearerTokenConverter(jwtConverter).opaqueToken();

		http.httpBasic();

		return http.build();
	}

	@Bean
	public MapReactiveUserDetailsService userDetailsRepository(@Value("${spring.security.user.name}") String username,
			@Value("${spring.security.user.password}") String password) {
		final UserDetails user = User.withDefaultPasswordEncoder().username(username).password(password)
				.roles("OPERATIONS").build();
		return new MapReactiveUserDetailsService(user);
	}

}

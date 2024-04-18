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
package com.minsait.onesait.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.minsait.onesait.platform.filter.CustomFilter;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.security.CustomBasicAuthenticationEntryPoint;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${onesaitplatform.dashboardengine.auth.token.endpoint:'http://localhost:18000/controlpanel/api/login/info'}")
	private String onesaitPlatformTokenAuth;

	@Autowired
	private CustomBasicAuthenticationEntryPoint authenticationEntryPoint;

	@Autowired
	@Qualifier("configDBAuthenticationProvider")
	AuthenticationProvider configDBAuthenticationProvider;

	@Autowired
	MultitenancyService multitenancyService;

	@Autowired
	private UserDetailsService detailsService;

	/**
	 * Authentication beans
	 */

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	/*
	 * @Bean public ConfigDBAuthenticationProvider
	 * authenticationProviderOnesaitPlatform() { final
	 * ConfigDBAuthenticationProvider bean = new ConfigDBAuthenticationProvider();
	 * return bean; }
	 */

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().authorizeRequests().antMatchers("/**").authenticated().and().httpBasic()
				.authenticationEntryPoint(authenticationEntryPoint).and()
				.authenticationProvider(configDBAuthenticationProvider);

		http.csrf().disable();

		http.headers().frameOptions().disable();

		http.addFilterAfter(new CustomFilter(onesaitPlatformTokenAuth, detailsService, multitenancyService),
				BasicAuthenticationFilter.class);

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

}

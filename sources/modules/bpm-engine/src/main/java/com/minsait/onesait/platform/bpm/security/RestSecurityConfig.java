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
package com.minsait.onesait.platform.bpm.security;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 20)
public class RestSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().antMatcher("/engine-rest/**").authorizeRequests().anyRequest().authenticated().and()
				.addFilterBefore(new BearerAuthenticationFilter(), BasicAuthenticationFilter.class);

	}

	@Bean
	public FilterRegistrationBean statelessUserAuthenticationFilter(ProcessEngine processEngine) {
		final FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new StatelessUserAuthenticationFilter(processEngine.getIdentityService()));
		filterRegistration.setOrder(102);
		filterRegistration.addUrlPatterns("/engine-rest/**");
		return filterRegistration;
	}

}

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
package com.minsait.onesait.platform.monitoring.configs;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;

@Configuration
// @EnableOAuth2Sso
@EnableOAuth2Client
// @Order(5)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationProvider customProvider;
	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	@Autowired
	private ActiveProfileDetector profileDetector;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin().loginPage("/login.html").loginProcessingUrl("/login").permitAll();
		http.csrf().disable();
		http.logout();
		http.authorizeRequests().antMatchers("/login**", "/**/*.css", "/img/**", "/third-party/**").permitAll();

		http.authorizeRequests().antMatchers("/**").authenticated();
		http.httpBasic();
		http.headers().frameOptions().disable();
		http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
		// if (!profileDetector.getActiveProfile().equalsIgnoreCase(DEFAULT_PROFILE))
		// http.requiresChannel().antMatchers("/login*").requiresSecure();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(customProvider);
	}

	private Filter ssoFilter() {
		final OAuth2ClientAuthenticationProcessingFilter oauthFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/oauth");
		final OAuth2RestTemplate oauthTemplate = new OAuth2RestTemplate(getClient(), oauth2ClientContext);
		oauthFilter.setRestTemplate(oauthTemplate);
		oauthFilter
				.setTokenServices(new UserInfoTokenServices(getResource().getUserInfoUri(), getClient().getClientId()));
		return oauthFilter;
	}

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean
	@ConfigurationProperties("security.oauth2.client")
	public AuthorizationCodeResourceDetails getClient() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("security.oauth2.resource")
	public ResourceServerProperties getResource() {
		return new ResourceServerProperties();
	}

}

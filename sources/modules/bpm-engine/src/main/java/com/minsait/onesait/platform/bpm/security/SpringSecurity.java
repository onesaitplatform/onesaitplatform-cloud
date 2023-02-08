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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.OAuth2AccessTokenSupport;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.request.RequestContextListener;

import com.minsait.onesait.platform.bpm.security.camunda.ContainerBasedAuthenticationFilter;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.services.bpm.BPMTenantService;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

@Configuration
@EnableOAuth2Client
@Order(SecurityProperties.BASIC_AUTH_ORDER - 15)
public class SpringSecurity extends WebSecurityConfigurerAdapter {
	private static final String AUTHENTICATION_PROVIDER_CLASS = "com.minsait.onesait.platform.bpm.security.SpringSecurityAuthenticationProvider";
	private static final String CSRF_PREVENTION_FILTER = "CsrfPreventionFilter";
	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	@Autowired
	private SuccessHandler successHandler;
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private VerticalResolver tenantDBResolver;
	@Autowired
	private MasterUserRepository masterUserRepository;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/actuator/**", "/login/**").permitAll().and()
		.antMatcher("/**").authorizeRequests().anyRequest().authenticated().and().exceptionHandling()
		.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/oauth")).and()
		.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
		.addFilterBefore(new BearerAuthenticationFilter(), BasicAuthenticationFilter.class);
	}

	@Bean
	public FilterRegistrationBean containerBasedAuthenticationFilter(BPMTenantService bpmnTenantService) {

		final FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		filterRegistration.setFilter(new ContainerBasedAuthenticationFilter(bpmnTenantService, userDetailsService,
				tenantDBResolver, masterUserRepository));
		filterRegistration
		.setInitParameters(Collections.singletonMap("authentication-provider", AUTHENTICATION_PROVIDER_CLASS));
		filterRegistration.setOrder(101); // make sure the filter is registered after the Spring Security Filter Chain
		filterRegistration.addUrlPatterns("/camunda/*");
		return filterRegistration;
	}
	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}

	@Autowired
	private UserInfoTokenServices userInfoTokenServices;
	@Qualifier("oauthClient")
	@Autowired
	private AuthorizationCodeResourceDetails authorizationCodeResourceDetails;

	private Filter ssoFilter() {
		final OAuth2ClientAuthenticationProcessingFilter oauthFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/oauth");
		final OAuth2RestTemplate oauthTemplate = new OAuth2RestTemplate(authorizationCodeResourceDetails,
				oauth2ClientContext);
		final OAuth2AccessTokenSupport authAccessProvider = new AuthorizationCodeAccessTokenProvider();
		authAccessProvider.setRequestFactory(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		final AccessTokenProvider accessTokenProvider = new AccessTokenProviderChain(Arrays.<AccessTokenProvider>asList(
				(AuthorizationCodeAccessTokenProvider) authAccessProvider, new ImplicitAccessTokenProvider(),
				new ResourceOwnerPasswordAccessTokenProvider(), new ClientCredentialsAccessTokenProvider()));
		oauthTemplate.setAccessTokenProvider(accessTokenProvider);
		oauthTemplate.setRequestFactory(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		oauthFilter.setRestTemplate(oauthTemplate);
		userInfoTokenServices.setRestTemplate(oauthTemplate);
		oauthFilter.setTokenServices(userInfoTokenServices);
		oauthFilter.setAuthenticationSuccessHandler(successHandler);
		return oauthFilter;
	}

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@ConditionalOnMissingBean(AuthorizationCodeResourceDetails.class)
	@Bean
	@ConfigurationProperties("security.oauth2.client")
	public AuthorizationCodeResourceDetails oauthClient() {
		return new AuthorizationCodeResourceDetails();
	}

	@ConditionalOnMissingBean(ResourceServerProperties.class)
	@Bean
	@ConfigurationProperties("security.oauth2.resource")
	public ResourceServerProperties getResource() {
		return new ResourceServerProperties();
	}

	@ConditionalOnMissingBean(UserInfoTokenServices.class)
	@Bean
	public UserInfoTokenServices userInfoTokenServices() {
		return new UserInfoTokenServices(getResource().getUserInfoUri(), oauthClient().getClientId());
	}

	@Bean
	public TokenStore tokenStore(DataSource ds) {
		return new JdbcTokenStore(ds);
	}

	@Bean
	public ServletContextInitializer csrfOverwrite() {

		return servletContext -> servletContext.addFilter(CSRF_PREVENTION_FILTER, new Filter() {

			@Override
			public void init(FilterConfig filterConfig) throws ServletException {

			}

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
					throws IOException, ServletException {
				chain.doFilter(request, response);

			}

			@Override
			public void destroy() {

			}
		});
	}

}
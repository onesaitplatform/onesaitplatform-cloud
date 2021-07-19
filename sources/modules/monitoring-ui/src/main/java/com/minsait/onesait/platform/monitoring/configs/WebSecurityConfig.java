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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.monitoring.filter.OperationsLoginFilter;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

@Configuration
// @EnableOAuth2Sso
@EnableOAuth2Client
// @Order(5)
@EnableSpringHttpSession
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationProvider customProvider;
	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	@Autowired(required = false)
	private PlugableOauthAuthenticator plugableOauthAuthenticator;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin().loginPage("/login.html").loginProcessingUrl("/login").successHandler(new SecurityHandler())
				.permitAll();
		http.csrf().disable();
		http.logout();
		http.authorizeRequests()
				.antMatchers("/login**", "/**/*.js", "/**/*.css", "/img/**", "/third-party/**", "/assets/**")
				.permitAll();

		http.authorizeRequests().antMatchers("/**").authenticated();
		http.httpBasic();
		http.headers().frameOptions().disable();

		http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class).addFilterBefore(
				new OperationsLoginFilter(tokenServices(), plugableOauthAuthenticator),
				AnonymousAuthenticationFilter.class);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(customProvider);
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
		oauthFilter.setAuthenticationSuccessHandler(new SecurityHandler());
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

	class SecurityHandler implements AuthenticationSuccessHandler {

		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication) throws IOException, ServletException {
			response.sendRedirect(request.getContextPath());

		}
	}

	// @Bean
	// public ServletContextInitializer servletContextInitializer(
	// @Value("${onesaitplatform.secure.cookie}") boolean secure) {
	// return servletContext -> {
	// servletContext.getSessionCookieConfig().setSecure(secure);
	// servletContext.getSessionCookieConfig().setHttpOnly(true);
	// };
	// }

	@Bean
	public MapSessionRepository sessionRepository() {
		return new MapSessionRepository(new ConcurrentHashMap<>());
	}

	@Bean
	public CookieSerializer cookieSerializer(@Value("${onesaitplatform.secure.cookie}") boolean secure) {
		final DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		if (secure) {
			serializer.setSameSite("None");
		}
		serializer.setUseHttpOnlyCookie(true);
		serializer.setUseSecureCookie(secure);
		return serializer;
	}

	@Value("${security.oauth2.resource.checkTokenEndpoint}")
	String checkTokenEndpoint;
	@Value("${security.oauth2.client.clientId}")
	String clientId;
	@Value("${security.oauth2.client.clientSecret}")
	String clientSecret;

	@Primary
	@Bean
	public RemoteTokenServices tokenServices() {
		final RemoteTokenServices tokenService = new RemoteTokenServices();
		tokenService.setCheckTokenEndpointUrl(checkTokenEndpoint);
		tokenService.setClientId(clientId);
		tokenService.setClientSecret(clientSecret);
		return tokenService;
	}

}

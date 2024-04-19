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
package com.minsait.onesait.platform.security.jwt.ri;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnMissingBean(PlugableOauthAuthenticator.class)
@EnableAuthorizationServer
@Slf4j
public class OAuth2AuthorizationServerConfigJwt extends AuthorizationServerConfigurerAdapter {

	@Value("${security.jwt.client-id}")
	private String clientId;

	@Value("${security.jwt.client-secret}")
	private String clientSecret;

	@Value("${security.jwt.grant-type}")
	private String grantType;

	@Value("${security.jwt.scopes}")
	private String scopes;

	@Autowired(required = false)
	@Qualifier("configDBAuthenticationProvider")
	private AuthenticationProvider authProvider;
	@Autowired(required = false)
	@Qualifier("ldapAuthenticationProvider")
	private AuthenticationProvider authProviderLdap;


	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private TokenStore tokenStore;

	@Autowired
	private JwtAccessTokenConverter jwtAccessTokenConverter;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("permitAll()").allowFormAuthenticationForClients();
	}


	PreAuthenticatedAuthenticationProvider preauthAuthProvider() {
		final PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
		provider.setPreAuthenticatedUserDetailsService(userDetailsServiceWrapper());
		return provider;
	}


	UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> userDetailsServiceWrapper() {
		final UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>();
		wrapper.setUserDetailsService(userDetailsService);
		return wrapper;
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter));

		endpoints.tokenEnhancer(tokenEnhancerChain);
		endpoints.authenticationManager(new ProviderManager(
				Stream.of(authProvider, authProviderLdap, preauthAuthProvider()).filter(Objects::nonNull).collect(Collectors.toList())));
		endpoints.userDetailsService(userDetailsService);
		endpoints.tokenStore(tokenStore);
		endpoints.accessTokenConverter(jwtAccessTokenConverter);
		endpoints.reuseRefreshTokens(false);
		endpoints.tokenServices(defaultTokenServices(tokenStore, tokenEnhancerChain));

	}

	@Bean
	@Primary
	DefaultTokenServices defaultTokenServices(TokenStore tokenStore, TokenEnhancerChain tokenEnhancerChain) {
		final DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setTokenStore(tokenStore);
		tokenServices.setTokenEnhancer(tokenEnhancerChain);
		tokenServices.setClientDetailsService(clientDetailsService());
		tokenServices.setAuthenticationManager(new ProviderManager(
				Stream.of(authProvider, authProviderLdap, preauthAuthProvider()).filter(Objects::nonNull).collect(Collectors.toList())));
		tokenServices.setReuseRefreshToken(false);
		tokenServices.setSupportRefreshToken(true);
		return tokenServices;
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

		clients.withClientDetails(clientDetailsService());
	}

	@Bean
	public ClientDetailsService clientDetailsService() {
		return new CustomClientDetailsService();
	}

	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new ExtendedTokenEnhancer();
	}

}
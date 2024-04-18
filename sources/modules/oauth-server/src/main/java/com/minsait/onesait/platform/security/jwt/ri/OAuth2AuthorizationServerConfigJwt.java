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
package com.minsait.onesait.platform.security.jwt.ri;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.minsait.onesait.platform.config.services.app.AppService;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfigJwt extends AuthorizationServerConfigurerAdapter {

	@Value("${security.jwt.client-id}")
	private String platformClientId;

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

	@Autowired
	private AppService appService;

	private static final String PSW_INCORRECT = "Password incorrect";
	private static final String USER_NOT_FOUND = "User not exists";

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("permitAll()").allowFormAuthenticationForClients();
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter));

		endpoints.tokenEnhancer(tokenEnhancerChain);
		endpoints.authenticationManager(new ProviderManager(
				Stream.of(authProvider, authProviderLdap).filter(Objects::nonNull).collect(Collectors.toList())));
		endpoints.userDetailsService(userDetailsService);
		endpoints.tokenStore(tokenStore);
		endpoints.accessTokenConverter(jwtAccessTokenConverter);
		endpoints.exceptionTranslator(webResponseExceptionTranslator());
		endpoints.addInterceptor(userInRealmApplication());

		endpoints.reuseRefreshTokens(false);

	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService());
	}

	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new ExtendedTokenEnhancer();
	}

	@Bean
	public ClientDetailsService clientDetailsService() {
		return new CustomClientDetailsService();
	}

	@Bean
	public WebResponseExceptionTranslator<OAuth2Exception> webResponseExceptionTranslator() {
		return new DefaultWebResponseExceptionTranslator() {

			@Override
			public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
				final ResponseEntity<OAuth2Exception> responseEntity = super.translate(e);
				final OAuth2Exception body = responseEntity.getBody();
				final HttpHeaders headers = new HttpHeaders();
				headers.setAll(responseEntity.getHeaders().toSingleValueMap());

				HttpStatus responseCode = responseEntity.getStatusCode();
				if (body.getMessage() == null) {
					return new ResponseEntity<>(body, headers, responseCode);
				}

				if (body.getMessage().contains(PSW_INCORRECT))
					responseCode = HttpStatus.BAD_REQUEST;
				else if (body.getMessage().contains(USER_NOT_FOUND))
					responseCode = HttpStatus.NOT_FOUND;
				else
					responseCode = HttpStatus.UNAUTHORIZED;
				return new ResponseEntity<>(body, headers, responseCode);
			}
		};
	}

	public WebRequestInterceptor userInRealmApplication() {
		return new WebRequestInterceptor() {

			@Override
			public void preHandle(WebRequest request) throws Exception {
				final Map<String, String[]> map = request.getParameterMap();
				// only grant_type password scope
				final boolean isInterceptable = map.entrySet().stream().filter(e -> e.getKey().equals("grant_type"))
						.anyMatch(e -> Arrays.asList(e.getValue()).contains("password"));
				if (isInterceptable) {
					// prioritize client_id parameter over principal client for non authenticated
					// requests such as gravitee SSO
					final String oAuthClientId = map.entrySet().stream().filter(e -> e.getKey().equals("client_id"))
							.map(e -> e.getValue()[0]).findFirst().orElse(request.getUserPrincipal().getName());
					final String username = request.getParameter("username");

					if (!oAuthClientId.equals(platformClientId) && !appService.isUserInApp(username, oAuthClientId)) {
						throw OAuth2Exception.create(OAuth2Exception.ACCESS_DENIED, "User is not in clientId/Realm");
					}
				}
			}

			@Override
			public void postHandle(WebRequest request, ModelMap model) throws Exception {

			}

			@Override
			public void afterCompletion(WebRequest request, Exception ex) throws Exception {

			}
		};
	}

}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAuthorizationServer
@Slf4j
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

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
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ServiceUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;

	private static final String PSW_INCORRECT = "Password incorrect";
	private static final String USER_NOT_FOUND = "User not exists";


	@Autowired
	private TokenEnhancer tokenEnhancer;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer, jwtAccessTokenConverter));

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
				log.error("Handling Oauth error, code:{}, body:{}, exception:{}", responseCode, body, e.getMessage());
				if (body.getMessage() == null) {
					return new ResponseEntity<>(body, headers, responseCode);
				}
				if(e.getMessage().contains("v_token1_bearer_ERROR"))
				{
					return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if (body.getMessage().contains(PSW_INCORRECT)) {
					responseCode = HttpStatus.BAD_REQUEST;
					final ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
							.currentRequestAttributes();
					authenticationFailure(attr.getRequest().getParameter("username"));
				} else if (body.getMessage().contains(USER_NOT_FOUND)) {
					responseCode = HttpStatus.NOT_FOUND;
				} else if (body.getMessage().contains("User is not in clientId")) {
					responseCode = HttpStatus.FORBIDDEN;
				} else {
					responseCode = HttpStatus.UNAUTHORIZED;
				}
				return new ResponseEntity<>(body, headers, responseCode);
			}
		};
	}

	private void authenticationFailure(String userName) {
		// if user exist increase failed attemps
		final MasterUser masterUser = multitenancyService.increaseFailedAttemp(userName);
		if (masterUser != null) {

			// validate limit
			final com.minsait.onesait.platform.config.model.Configuration configuration = configurationService
					.getConfiguration(com.minsait.onesait.platform.config.model.Configuration.Type.EXPIRATIONUSERS,
							"default", null);
			final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
					.fromYaml(configuration.getYmlConfig()).get("Authentication");
			final int limitFailedAttemp = (Integer) ymlExpirationUsersPassConfig.get("limitFailedAttemp");

			if (masterUser.getFailedAtemps() >= limitFailedAttemp && limitFailedAttemp >= 0) {
				// if the limit is equal or higher
				if (userService.deactivateUser(userName)) {
					// send mail
					try {
						final String defaultTitle = "[Onesait Plaform] User account locked";
						final String defaultMessage = "Your account has been blocked contact your administrator.";
						final String emailTitle = utils.getMessage("user.attemp.bloqued.mail.title", defaultTitle);
						final String emailMessage = utils.getMessage("user.attemp.bloqued.mail.body", defaultMessage);
						mailService.sendMail(masterUser.getEmail(), emailTitle, emailMessage);
					} catch (final Exception e) {

					}
				}
			}
		}

	}

	public WebRequestInterceptor userInRealmApplication() {
		return new WebRequestInterceptor() {

			@Override
			public void preHandle(WebRequest request) throws Exception {
				final long start = System.currentTimeMillis();
				log.debug("New preHandle process");
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
						throw OAuth2Exception.create(OAuth2Exception.ACCESS_DENIED,
								"User is not in clientId/Realm. Username: " + username + " , clientId: "
										+ oAuthClientId);
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("End preHandle process, time: {}", System.currentTimeMillis() - start);
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

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
package com.minsait.onesait.platform.serverless.security.oauth;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.serverless.utils.SSLUtil;

@Configuration
public class OauthConfiguration {

	private static final String API_KEY_HEADER = "X-OP-APIKey";
	@Value("${onesaitplatform.api-key}")
	private String apiKey;
	@Value(value = "${onesaitplatform.authentication.oauth.preEstablishedRedirectUri:}")
	private String preEstablishedRedirectUri;

	@Bean
	public PrincipalExtractor principalExtractor() {
		return new OauthPrincipalExtractor();
	}

	@Bean
	public AuthoritiesExtractor authoritiesExtractor() {
		return new OauthAuthoritiesExtractor();
	}

	@Bean
	@Primary
	public UserInfoTokenServices userInfoTokenServices()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		final AuthorizationCodeResourceDetails oauthClient = oauthClient();
		if (!StringUtils.isEmpty(preEstablishedRedirectUri)) {
			oauthClient.setPreEstablishedRedirectUri(preEstablishedRedirectUri);
		}
		final UserInfoTokenServices tokenServices = new UserInfoTokenServices(oauthResource().getUserInfoUri(),
				oauthClient.getClientId());
		tokenServices.setAuthoritiesExtractor(authoritiesExtractor());
		tokenServices.setPrincipalExtractor(principalExtractor());
		final OAuth2RestTemplate oauthTemplate = new OAuth2RestTemplate(oauthClient);
		oauthTemplate.setRequestFactory(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		tokenServices.setRestTemplate(oauthTemplate);
		return tokenServices;
	}

	@Bean
	@ConfigurationProperties("onesaitplatform.authentication.oauth.client")
	@Primary
	public AuthorizationCodeResourceDetails oauthClient() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("onesaitplatform.authentication.oauth.resource")
	@Primary
	public ResourceServerProperties oauthResource() {
		return new ResourceServerProperties();
	}

	@Bean("pluginRestTemplate")
	public RestTemplate restTemplate() {

		final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.getInterceptors().add((r, b, e) -> {
			r.getHeaders().add(API_KEY_HEADER, apiKey);
			return e.execute(r, b);
		});
		return restTemplate;
	}

}

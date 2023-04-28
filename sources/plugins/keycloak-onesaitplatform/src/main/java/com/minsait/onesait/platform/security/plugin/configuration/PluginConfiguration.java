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
package com.minsait.onesait.platform.security.plugin.configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.security.plugin.condition.PluginLoadCondition;
import com.minsait.onesait.platform.security.plugin.impl.UserInfoServices;
import com.minsait.onesait.platform.security.plugin.mappers.KeycloakAuthoritiesExtractor;
import com.minsait.onesait.platform.security.plugin.mappers.KeycloakPrincipalExtractor;

@Configuration
@Conditional(PluginLoadCondition.class)
@ComponentScan("com.minsait.onesait")
public class PluginConfiguration {

	private static final String API_KEY_HEADER = "X-OP-APIKey";
	@Value("${api-key}")
	private String apiKey;
	@Value(value = "${security.oauth2.client.preEstablishedRedirectUri:}")
	private String preEstablishedRedirectUri;

	@Bean
	public PrincipalExtractor principalExtractor() {
		return new KeycloakPrincipalExtractor();
	}

	@Bean
	public AuthoritiesExtractor authoritiesExtractor() {
		return new KeycloakAuthoritiesExtractor();
	}

	@Bean
	@Primary
	public UserInfoTokenServices userInfoTokenServices()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		final AuthorizationCodeResourceDetails oauthClient = oauthClient();
		if (StringUtils.hasText(preEstablishedRedirectUri)) {
			oauthClient.setPreEstablishedRedirectUri(preEstablishedRedirectUri);
		}
		final UserInfoServices tokenServices = new UserInfoServices(oauthResource().getUserInfoUri(),
				oauthClient.getClientId());
		tokenServices.setAuthoritiesExtractor(authoritiesExtractor());
		tokenServices.setPrincipalExtractor(principalExtractor());
		final OAuth2RestTemplate oauthTemplate = new OAuth2RestTemplate(oauthClient);
		oauthTemplate.setRequestFactory(getIgnoreSSLRequestFactory());
		tokenServices.setRestTemplate(oauthTemplate);
		return tokenServices;
	}

	@Bean
	@ConfigurationProperties("oauth2.client")
	@Primary
	public AuthorizationCodeResourceDetails oauthClient() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("oauth2.resource")
	@Primary
	public ResourceServerProperties oauthResource() {
		return new ResourceServerProperties();
	}

	@Bean("pluginRestTemplate")
	public RestTemplate restTemplate() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {

		final RestTemplate restTemplate = new RestTemplate(getIgnoreSSLRequestFactory());
		restTemplate.getInterceptors().add((r, b, e) -> {
			r.getHeaders().add(API_KEY_HEADER, apiKey);
			return e.execute(r, b);
		});
		return restTemplate;
	}

	private HttpComponentsClientHttpRequestFactory getIgnoreSSLRequestFactory()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		final TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		final SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
				.loadTrustMaterial(null, acceptingTrustStrategy).build();

		final SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		final CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE).setSSLSocketFactory(csf).build();

		final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);

		return requestFactory;
	}
}

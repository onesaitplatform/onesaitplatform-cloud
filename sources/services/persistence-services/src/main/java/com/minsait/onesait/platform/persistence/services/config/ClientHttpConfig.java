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
package com.minsait.onesait.platform.persistence.services.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ClientHttpConfig {
	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@Value("${onesaitplatform.database.mongodb.quasar.default-keep-alive-time-millis:20000}")
	private int DEFAULT_KEEP_ALIVE_TIME_MILLIS;

	@Value("${onesaitplatform.database.mongodb.quasar.close-idle-connection-wait-time-secs:20}")
	private int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS;

	@Value("${onesaitplatform.database.mongodb.quasar.maxHttpConnections:10}")
	private int MAX_TOTAL_CONNECTIONS;

	PoolingHttpClientConnectionManager connectionManager() {
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
		return cm;
	}

	HttpClient httpClient() throws GenericOPException {
		final TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;

		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new GenericOPException("Problem configuring SSL verification", e);
		}

		final SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,
				NoopHostnameVerifier.INSTANCE);

		final RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(getTimeout())
				.setConnectTimeout(getTimeout()).setSocketTimeout(getTimeout()).build();

		return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager())
				.setSSLSocketFactory(csf).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
	}

	public HttpComponentsClientHttpRequestFactory requestFactory() throws GenericOPException {
		final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClient());
		return httpRequestFactory;
	}

	@Bean("dataHubRest")
	public RestTemplate restTemplate() throws GenericOPException {
		final RestTemplate rt = new RestTemplate(requestFactory());
		rt.setErrorHandler(new ClientHttpErrorHandler());
		return rt;
	}

	private int getTimeout() {
		return ((Integer) integrationResourcesService.getGlobalConfiguration().getEnv().getDatabase()
				.get("mongodb-quasar-timeout")).intValue();
	}

	@PostConstruct
	public void debug() {
		log.info("Resttemplate config done");
	}
}

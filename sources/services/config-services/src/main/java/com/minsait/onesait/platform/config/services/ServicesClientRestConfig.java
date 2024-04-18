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
package com.minsait.onesait.platform.config.services;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ServicesClientRestConfig {

	// Determines the timeout in milliseconds until a connection is established.
	@Value("${onesaitplatform.services.client.rest.connect-timeout:30000}")
	private int connectTimeout;

	// The timeout when requesting a connection from the connection manager.
	@Value("${onesaitplatform.services.client.rest.request-timeout:30000}")
	private int requestTimeout;

	// The timeout for waiting for data
	@Value("${onesaitplatform.services.client.rest.socket-timeout:60000}")
	private int socketTimeout;

	@Value("${onesaitplatform.services.client.rest.max-total-connections:100}")
	private int maxTotalConnections;

	@Value("${onesaitplatform.services.client.rest.default-keep-alive-time-millis:20000}")
	private int defaultKeepAliveTimeMillis;

	@Value("${onesaitplatform.services.client.rest.close-idle-connection-wait-time-secs:30}")
	private int closeIdleConnectionWaitTimeSecs;

	@Bean
	PoolingHttpClientConnectionManager connectionManager() {
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(maxTotalConnections);
		cm.setDefaultMaxPerRoute(maxTotalConnections);
		return cm;
	}

	@Bean
	public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
		return (response, context) -> defaultKeepAliveTimeMillis;
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

		final RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(requestTimeout)
				.setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();

		return (HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager())
				.setSSLSocketFactory(csf).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build());

	}

	public HttpComponentsClientHttpRequestFactory requestFactory() throws GenericOPException {
		final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClient());
		return httpRequestFactory;
	}

	@Bean("serviceClientRest")
	public RestTemplate restTemplate() throws GenericOPException {
		return new RestTemplate(requestFactory());
	}

	@Bean
	public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
		return new Runnable() {
			@Override
			@Scheduled(fixedDelay = 10000)
			public void run() {
				try {
					if (connectionManager != null) {
						log.trace("run IdleConnectionMonitor - Closing expired and idle connections...");
						connectionManager.closeExpiredConnections();
						connectionManager.closeIdleConnections(closeIdleConnectionWaitTimeSecs, TimeUnit.SECONDS);
					} else {
						log.trace("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
					}
				} catch (final Exception e) {
					log.error("run IdleConnectionMonitor - Exception occurred. msg={}, e={}", e.getMessage(), e);
				}
			}
		};
	}

}

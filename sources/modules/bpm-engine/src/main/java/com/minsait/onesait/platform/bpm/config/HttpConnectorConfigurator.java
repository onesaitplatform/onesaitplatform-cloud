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
package com.minsait.onesait.platform.bpm.config;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.impl.AbstractHttpConnector;
import org.camunda.connect.spi.ConnectorConfigurator;
import org.springframework.stereotype.Component;

import connectjar.org.apache.http.impl.client.CloseableHttpClient;
import connectjar.org.apache.http.impl.client.HttpClients;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpConnectorConfigurator implements ConnectorConfigurator<HttpConnector> {

	@Override
	public Class<HttpConnector> getConnectorClass() {
		return HttpConnector.class;
	}

	@Override
	public void configure(HttpConnector connector) {
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				return;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				return;
			}
		} };

		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
		} catch (final Exception e) {
			throw new RuntimeException("Could not change SSL TrustManager to accept arbitray certificates", e);
		}

		final HostnameVerifier hv = (urlHostName, session) -> {
			if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
				log.warn("URL host {} is different to SSLSession host {} .", urlHostName, session.getPeerHost());
			}
			return true;
		};

		final CloseableHttpClient client = HttpClients.custom().setSSLContext(sc).setSSLHostnameVerifier(hv).build();
		((AbstractHttpConnector) connector).setHttpClient(client);
	}

}

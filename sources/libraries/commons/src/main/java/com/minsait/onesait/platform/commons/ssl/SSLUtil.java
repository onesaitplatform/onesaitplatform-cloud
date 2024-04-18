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
package com.minsait.onesait.platform.commons.ssl;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

public final class SSLUtil {

	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		@Override
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
			// This function is empty
		}

		@Override
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
			// This function is empty
		}
	} };

	public static void turnOffSslChecking() throws NoSuchAlgorithmException, KeyManagementException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);
	}

	public static HttpComponentsClientHttpRequestFactory getHttpRequestFactoryAvoidingSSLVerification() {
		final TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;

		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new GenericRuntimeOPException("Problem configuring SSL verification", e);
		}

		final SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,
				NoopHostnameVerifier.INSTANCE);

		final CloseableHttpClient httpClient = closeableHttpClientWithProxySettings(csf);

		final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClient);
		return httpRequestFactory;
	}

	private static CloseableHttpClient closeableHttpClientWithProxySettings(SSLConnectionSocketFactory csf) {
		CloseableHttpClient httpClient;

		boolean configureSystemProperties = false;

		final String httpProxyHost = System.getenv("http.proxyHost");
		final String httpProxyPort = System.getenv("http.proxyPort");
		final String httpNonProxyHosts = System.getenv("http.nonProxyHosts");

		if (httpProxyHost != null && httpProxyHost.trim().length() > 0 && httpProxyPort != null
				&& httpProxyPort.trim().length() > 0) {
			System.setProperty("http.proxyHost", httpProxyHost);
			System.setProperty("http.proxyPort", httpProxyPort);
			if (httpNonProxyHosts != null && httpNonProxyHosts.trim().length() > 0) {
				System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
			}
			configureSystemProperties = true;
		}

		final String httpsProxyHost = System.getenv("https.proxyHost");
		final String httpsProxyPort = System.getenv("https.proxyPort");
		if (httpsProxyHost != null && httpsProxyHost.trim().length() > 0 && httpsProxyPort != null
				&& httpsProxyPort.trim().length() > 0) {
			System.setProperty("https.proxyHost", httpsProxyHost);
			System.setProperty("https.proxyPort", httpsProxyPort);
			if (httpNonProxyHosts != null && httpNonProxyHosts.trim().length() > 0) { // No existe https.nonProxyHosts
				System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
			}
			configureSystemProperties = true;
		}

		if (configureSystemProperties) {
			httpClient = HttpClients.custom().disableCookieManagement().useSystemProperties().setMaxConnPerRoute(200)
					.setMaxConnTotal(200).setSSLSocketFactory(csf).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
					.build();
		} else {
			httpClient = HttpClients.custom().disableCookieManagement().setMaxConnPerRoute(200).setMaxConnTotal(200)
					.setSSLSocketFactory(csf).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		}

		return httpClient;
	}

	private SSLUtil() {
		throw new UnsupportedOperationException("Do not instantiate libraries.");
	}
}

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
package com.minsait.onesait.platform.router.service.app.service.advice;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.router.client.RouterClient;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("adviceServiceImpl")
public class AdviceServiceImpl
		implements AdviceService, RouterClient<NotificationCompositeModel, OperationResultModel> {

	@Override
	public OperationResultModel advicePostProcessing(NotificationCompositeModel input) {
		return execute(input);
	}

	@Value("${onesaitplatform.router.avoidsslverification:false}")
	private boolean avoidSSLVerification;

	@Value("${onesaitplatform.router.notifications.pool.nodered:10}")
	private int MAX_TOTAL_CONNECTIONS;

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() throws KeyManagementException, NoSuchAlgorithmException, GenericOPException {
		if (avoidSSLVerification) {
			SSLUtil.turnOffSslChecking();
		}

		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClient());

		this.restTemplate = new RestTemplate(httpRequestFactory);
		this.restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("admin", "admin"));

	}

	private HttpClient httpClient() throws GenericOPException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;

		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new GenericOPException("Problem configuring SSL verification", e);
		}

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(10000).setConnectTimeout(10000)
				.setSocketTimeout(10000).build();

		return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager())
				.setSSLSocketFactory(csf).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
	}

	PoolingHttpClientConnectionManager connectionManager() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);
		return cm;
	}

	@Override
	public OperationResultModel execute(NotificationCompositeModel input) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		ObjectMapper mapper = new ObjectMapper();
		HttpEntity<String> domainToStart;
		try {
			domainToStart = new HttpEntity<>(mapper.writeValueAsString(input), headers);
			OperationResultModel quote = restTemplate.postForObject(input.getUrl(), domainToStart,
					OperationResultModel.class);
			if (quote != null) {
				log.debug(quote.toString());
			}
			return quote;
		} catch (Exception e) {
			log.error("Error while sending notification. Unable to parse notification to JSON. Cause = {}, message={}.",
					e.getCause(), e.getMessage());
			OperationResultModel resultModel = new OperationResultModel();
			resultModel.setErrorCode("500");
			resultModel.setMessage("Error while sending notification. Unable to parse notification to JSON.");
			return resultModel;
		}

	}

}

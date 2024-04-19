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
package com.minsait.onesait.platform.iotbroker.subscription.notificator;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.util.SSAPMessageGenerator;
import com.minsait.onesait.platform.iotbroker.subscription.model.SubscriptorClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RestNotificatorImpl implements Notificator {

	private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

	private RestTemplate restTemplate;

	@Value("${onesaitplatform.router.notifications.pool.subscription.attemps:2}")
	private int numMaxAttemps;

	private int numAttemps = 0;

	@PostConstruct
	public void init() {
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.setRequestFactory(getRestTemplateRequestFactory());
	}

	@Override
	public boolean notify(String dataToNotify, SubscriptorClient client) {
		try {

			SSAPMessage<SSAPBodyIndicationMessage> response = SSAPMessageGenerator
					.generateResponseIndicationMessage(client.getClientId(), dataToNotify, client.getSessionKey());

			final HttpEntity<SSAPMessage<SSAPBodyIndicationMessage>> data = new HttpEntity<>(response);

			log.info("Attemp to notify data to subsciptor {}. to callback enpoint {}", client.getClientId(),
					client.getCallbackEndpoint());
			numAttemps++;
			final ResponseEntity<String> resp = restTemplate.exchange(client.getCallbackEndpoint(), HttpMethod.POST,
					data, String.class);

			if (resp.getStatusCode() == HttpStatus.OK) {
				log.info("Notified data to subsciptor {}. to callback enpoint {} SUCCESSFULLY.", client.getClientId(),
						client.getCallbackEndpoint());
				numAttemps = 0;
				return true;
			} else {
				log.warn("HTTP code {} notifing data to subsciptor {}. to callback enpoint {}", resp.getStatusCode(),
						client.getClientId(), client.getCallbackEndpoint());
				log.warn("Broker message {}", resp.getBody());
				if (numAttemps < numMaxAttemps) {
					this.notify(dataToNotify, client);
				} else {
					log.warn("The Subscriptor with id {} is not available. The subscription is going to be removed.",
							client.getClientId());
					numAttemps = 0;
					return false;
				}
			}

		} catch (final Exception e) {
			if (numAttemps < numMaxAttemps) {
				this.notify(dataToNotify, client);
			} else {
				log.warn("The Subscriptor with id {} is not available. The subscription is going to be removed.",
						client.getClientId());
				numAttemps = 0;
				return false;
			}
		}
		numAttemps = 0;
		return false;
	}

	private ClientHttpRequestFactory getRestTemplateRequestFactory() {
		final RequestConfig config = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
				.setConnectionRequestTimeout(TIMEOUT).build();

		final CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		return new HttpComponentsClientHttpRequestFactory(client);
	}
}

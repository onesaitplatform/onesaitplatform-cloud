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
package com.minsait.onesait.platform.router.subscription.notificator;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.subscription.model.SubscriptorClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificatorImpl implements Notificator {

	private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

	private RestTemplate restTemplate;

	@Value("${onesaitplatform.urls.iotbroker.advice:http://localhost:19000/iot-broker/advice}")
	private String iotbrokerEndpoint;

	@PostConstruct
	public void init() {
		restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.setRequestFactory(getRestTemplateRequestFactory());
	}

	@Override
	public boolean notify(String dataToNotify, SubscriptorClient client) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			NotificationCompositeModel model = new NotificationCompositeModel();
			NotificationModel notificationModel = new NotificationModel();
			OperationModel operation = new OperationModel();
			operation.setBody(dataToNotify);
			operation.setOperationType(OperationType.POST);
			operation.setSource(Source.INTERNAL_ROUTER);
			operation.setClientConnection(mapper.writeValueAsString(client));
			notificationModel.setOperationModel(operation);
			model.setNotificationModel(notificationModel);

			final HttpEntity<NotificationCompositeModel> data = new HttpEntity<>(model);

			log.info("Attemp to notify to iot-broker.");
			final ResponseEntity<OperationResultModel> resp = restTemplate.exchange(iotbrokerEndpoint, HttpMethod.POST,
					data, OperationResultModel.class);

			if (resp.getStatusCode() == HttpStatus.OK) {
				if (!resp.getBody().getResult().equalsIgnoreCase("Error")) {
					log.info("Notified data to iot-broker SUCCESSFULLY.");
					return true;
				} else {
					log.error("Error notifyng subscriptor.");
					return false;
				}
			} else {
				log.warn("HTTP code {} notifing data to iot-broker", resp.getStatusCode());
				log.warn("Broker message {}", resp.getBody().getMessage());
				return false;
			}

		} catch (final Exception e) {
			log.error("Error notifying iot-broker. {}", e);
			return false;
		}
	}

	private ClientHttpRequestFactory getRestTemplateRequestFactory() {
		final RequestConfig config = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
				.setConnectionRequestTimeout(TIMEOUT).build();

		final CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		return new HttpComponentsClientHttpRequestFactory(client);
	}
}

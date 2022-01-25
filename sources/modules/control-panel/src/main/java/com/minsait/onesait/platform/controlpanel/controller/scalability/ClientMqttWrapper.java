/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.scalability;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;

import com.minsait.onesait.platform.client.MQTTClient;
import com.minsait.onesait.platform.client.configuration.MQTTSecureConfiguration;
import com.minsait.onesait.platform.client.exception.MqttClientException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientMqttWrapper implements Client {

	private MQTTClient client;

	private String token;
	private String clientPlatform;
	private String clientPlatformInstance;
	private boolean avoidSSLValidation;
	private int attempts = 0;
	private static final int MAX_ATTEMPTS = 100;
	private static final int ATTEMPTS_DELAY = 500;

	public ClientMqttWrapper(String url) throws MqttClientException {
		createClient(url);
	}

	@Override
	public void createClient(String url) throws MqttClientException {
		MQTTClient mqttClient;
		final String protocol = url.substring(0, 3);
		if (protocol.equalsIgnoreCase("tcp")) {
			mqttClient = new MQTTClient(url, false);
		} else {
			final ClassPathResource classPathResource = new ClassPathResource("clientdevelkeystore.jks");
			String keystore;
			try {
				keystore = classPathResource.getFile().getAbsolutePath();
			} catch (final IOException e) {
				log.error("Error creating client", e);
				throw new GenericRuntimeOPException("Error opening clientdevelkeystore.jks");
			}
			final MQTTSecureConfiguration sslConfig = new MQTTSecureConfiguration(keystore, "changeIt!");
			mqttClient = new MQTTClient(url, sslConfig);
		}

		if (client != null) {
			client.disconnect();
		}
		client = mqttClient;
	}

	@Override
	public void connect(String token, String clientPlatform, String clientPlatformInstance, boolean avoidSSLValidation)
			throws MqttClientException {
		this.token = token;
		this.clientPlatform = clientPlatform;
		this.clientPlatformInstance = clientPlatformInstance;
		this.avoidSSLValidation = avoidSSLValidation;
		client.connect(token, clientPlatform, clientPlatformInstance, 10);
		client.setTimeout(10);
	}

	private void reconnect() throws InterruptedException {
		Thread.sleep(ATTEMPTS_DELAY);
		if (MAX_ATTEMPTS > attempts) {
			attempts++;
			try {
				connect(token, clientPlatform, clientPlatformInstance, avoidSSLValidation);
			} catch (final MqttClientException e) {
				reconnect();
			}
		} else {
			throw new GenericRuntimeOPException("Impossible to reconnect with the server");
		}
	}

	@Override
	public void insertInstance(String ontology, String instance) {
		try {
			client.insert(ontology, instance);
		} catch (final MqttClientException e) {
			try {
				reconnect();
			} catch (final Exception e1) {
				throw new GenericRuntimeOPException("Error sleeping task");
			}
		}
	}

	@Override
	public void disconnect() throws MqttClientException {
		client.disconnect();
	}

	@Override
	public String getProtocol() {
		return "mqtt";
	}

}

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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt;

import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Ignore
public class MoquetteSSLTest {

	String topic = "/message";
	String content = "Message from MqttPublishSample";
	int qos = 2;
	String broker_url = "ssl://s4citiespro.westeurope.cloudapp.azure.com:8883";
	String clientId = "JavaSample";
	String jksWithCorrectCertificate = "src/main/resources/clientdevelkeystore.jks";
	String jksWithWrongCertificate = "src/main/resources/nocertclientdevelkeystore.jks";
	String clientkeyStorePassword = "changeIt!";
	String clientkeyManagerPassword = "changeIt!";
	MemoryPersistence persistence = new MemoryPersistence();

	@Before
	public void setUp() {

	}

	@Test
	public void given_MqttBrokerWithSSLSupport_When_ClientWithCorrentCredential_Then_ConnectionIsGranted()
			throws Exception {
		final MqttClient client = new MqttClient(broker_url, clientId, persistence);

		final SSLSocketFactory ssf = configureSSLSocketFactory(jksWithCorrectCertificate);
		final MqttConnectOptions options = new MqttConnectOptions();
		options.setSocketFactory(ssf);
		client.connect(options);
		Assert.assertTrue(client.isConnected());

	}

	@Test(expected = MqttException.class)
	public void given_MqttBrokerWithSSLSupport_When_ClientWithCorrentCredential_Then_ConnectionIsRevoked()
			throws Exception {
		final MqttClient client = new MqttClient(broker_url, clientId, persistence);

		final SSLSocketFactory ssf = configureSSLSocketFactory(jksWithWrongCertificate);
		final MqttConnectOptions options = new MqttConnectOptions();
		options.setSocketFactory(ssf);
		client.connect(options);
		fail("Expected an MqttException to be thrown");
	}

	private SSLSocketFactory configureSSLSocketFactory(String keyStore) throws Exception {
		final KeyStore ks = KeyStore.getInstance("JKS");
		final InputStream jksInputStream = new FileInputStream(keyStore);
		ks.load(jksInputStream, clientkeyStorePassword.toCharArray());

		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, clientkeyStorePassword.toCharArray());

		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		final SSLContext sc = SSLContext.getInstance("TLS");
		final TrustManager[] trustManagers = tmf.getTrustManagers();
		sc.init(kmf.getKeyManagers(), trustManagers, null);

		final SSLSocketFactory ssf = sc.getSocketFactory();
		return ssf;
	}

}
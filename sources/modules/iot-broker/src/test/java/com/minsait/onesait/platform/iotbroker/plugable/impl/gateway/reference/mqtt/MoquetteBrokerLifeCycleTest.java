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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MoquetteBrokerLifeCycleTest {

	@Test
	public void given_OneMoquetteBroker_When_InitIsInvoked_Then_ItIsAbleToAccetoConnections() throws MqttException {
		final MoquetteBroker broker = new MoquetteBroker();
		broker.setHost("localhost");
		broker.setPort("11883");
		broker.setPool(1);
		broker.setStore("moquette_tes.mamdb");
		broker.init();

		final String broker_url = "tcp://localhost:11883";
		final MemoryPersistence persistence = new MemoryPersistence();
		final MqttClient client = new MqttClient(broker_url, "test", persistence);
		final MqttConnectOptions connOpts = new MqttConnectOptions();

		connOpts.setCleanSession(true);
		client.connect(connOpts);

		broker.stopServer();
	}
}

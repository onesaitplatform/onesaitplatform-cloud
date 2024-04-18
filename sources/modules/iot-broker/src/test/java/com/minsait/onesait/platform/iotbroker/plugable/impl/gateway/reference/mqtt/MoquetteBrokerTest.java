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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.json.SSAPJsonParser;
import com.minsait.onesait.platform.iotbroker.mock.pojo.Person;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.mock.router.RouterServiceGenerator;
import com.minsait.onesait.platform.iotbroker.mock.ssap.SSAPMessageGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.iotbroker.processor.DeviceManager;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MoquetteBrokerTest {

	String topic = "/message";
	String content = "Message from MqttPublishSample";
	int qos = 2;
	String broker_url = "tcp://localhost:1883";
	String clientId = "JavaSample";
	MemoryPersistence persistence = new MemoryPersistence();

	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext wac;
	private ResultActions resultAction;
	private final String URL_ADVICE_PATH = "/advice";
	private final String URL_COMMAND_PATH = "/commandAsync";
	@Autowired
	ObjectMapper mapper;

	@Value("${local.server.port}")
	private int port;

	@Autowired
	MoquetteBroker broker;

	@Mock
	SecurityPluginManager securityPluginManager;

	@Mock
	DeviceManager deviceManager;

	private CompletableFuture<String> completableFutureMessage;
	private CompletableFuture<String> completableFutureIndication;
	private CompletableFuture<String> completableFutureCommand;
	private IoTSession session = null;

	Person subject;

	private void securityMocks() {

		session = PojoGenerator.generateSession();

		when(deviceManager.registerActivity(any(), any(), any(), any())).thenReturn(true);
		when(securityPluginManager.authenticate(any(), any(), any(), any())).thenReturn(Optional.of(session));
		when(securityPluginManager.getSession(anyString())).thenReturn(Optional.of(session));
		when(securityPluginManager.checkSessionKeyActive(anyString())).thenReturn(true);
		when(securityPluginManager.checkAuthorization(any(), any(), any())).thenReturn(true);
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

		subject = PojoGenerator.generatePerson();
		completableFutureMessage = new CompletableFuture<>();
		completableFutureIndication = new CompletableFuture<>();
		completableFutureCommand = new CompletableFuture<>();
		securityMocks();
	}

	@Test
	public void given_OneMqttClientConnection_When_ACommandIsTrigger_Then_ItGetsTheCommand() throws Exception {

		final MqttClient client = new MqttClient(broker_url, clientId, persistence);
		final MqttConnectOptions connOpts = new MqttConnectOptions();

		connOpts.setCleanSession(true);
		client.connect(connOpts);

		client.subscribe("/topic/message/" + client.getClientId(), new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				final String response = new String(message.getPayload());
				completableFutureMessage.complete(response);
			}
		});

		client.subscribe("/topic/command/" + session.getSessionKey(), new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				final String response = new String(message.getPayload());
				completableFutureCommand.complete(response);
			}
		});

		// Send join message
		completableFutureMessage = new CompletableFuture<>();
		final SSAPMessage<SSAPBodyJoinMessage> join = SSAPMessageGenerator.generateJoinMessageWithToken();
		final String joinStr = SSAPJsonParser.getInstance().serialize(join);
		final MqttMessage message = new MqttMessage(joinStr.getBytes());
		message.setQos(qos);
		client.publish(topic, message);

		// Get join message response
		final String responseStr = completableFutureMessage.get(5, TimeUnit.SECONDS);
		final SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance().deserialize(responseStr);
		Assert.assertNotNull(response);

		completableFutureCommand = new CompletableFuture<>();

		// Command indication simulated by calling advice IotBroker rest service
		final StringBuilder url = new StringBuilder(URL_COMMAND_PATH);
		url.append("/test_command/?sessionKey=" + session.getSessionKey());

		resultAction = mockMvc.perform(
				MockMvcRequestBuilders.post(url.toString()).accept(org.springframework.http.MediaType.APPLICATION_JSON)
						.content("{}").contentType(org.springframework.http.MediaType.APPLICATION_JSON));

		final String responseCommandStr = completableFutureCommand.get(5, TimeUnit.SECONDS);
		final SSAPMessage<SSAPBodyIndicationMessage> responseCommand = SSAPJsonParser.getInstance()
				.deserialize(responseCommandStr);
		Assert.assertNotNull(responseCommand);

	}

	@Test
	public void given_OneMqttClientConnection_When_ItSubscribesToATopicAndSendsMessage_Then_ItGetsTheMessage()
			throws Exception {

		final MqttClient client = new MqttClient(broker_url, clientId, persistence);
		final MqttConnectOptions connOpts = new MqttConnectOptions();

		connOpts.setCleanSession(true);
		client.connect(connOpts);

		client.subscribe("/topic/message/" + client.getClientId(), new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				final String response = new String(message.getPayload());
				completableFutureMessage.complete(response);
			}
		});

		client.subscribe("/topic/subscription/" + session.getSessionKey(), new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				final String response = new String(message.getPayload());
				completableFutureIndication.complete(response);
			}
		});

		// Send join message
		completableFutureMessage = new CompletableFuture<>();
		final SSAPMessage<SSAPBodyJoinMessage> join = SSAPMessageGenerator.generateJoinMessageWithToken();
		final String joinStr = SSAPJsonParser.getInstance().serialize(join);
		MqttMessage message = new MqttMessage(joinStr.getBytes());
		message.setQos(qos);
		client.publish(topic, message);

		// Get join message response
		String responseStr = completableFutureMessage.get(5, TimeUnit.SECONDS);
		SSAPMessage<SSAPBodyReturnMessage> response = SSAPJsonParser.getInstance().deserialize(responseStr);
		Assert.assertNotNull(response);

//		// Send subscription message
//		completableFutureMessage = new CompletableFuture<>();
//		final SSAPMessage<SSAPBodySubscribeMessage> subscription = SSAPMessageGenerator.generateSubscriptionMessage(
//				Person.class.getSimpleName(), session.getSessionKey(), SSAPQueryType.SQL, "SELECT * FROM Person");
//		final String subscriptionStr = SSAPJsonParser.getInstance().serialize(subscription);
//		message = new MqttMessage(subscriptionStr.getBytes());
//		message.setQos(qos);
//		client.publish(topic, message);

		// Get subscription message response
		responseStr = completableFutureMessage.get(5, TimeUnit.SECONDS);
		response = SSAPJsonParser.getInstance().deserialize(responseStr);
		Assert.assertNotNull(response);

		// Avice indication simulated by calling advice IotBroker rest service
		final NotificationCompositeModel model = RouterServiceGenerator.generateNotificationCompositeModel(
				response.getBody().getData().at("/subscriptionId").asText(), subject, session);
		final String content = mapper.writeValueAsString(model);
		resultAction = mockMvc.perform(
				MockMvcRequestBuilders.post(URL_ADVICE_PATH).accept(org.springframework.http.MediaType.APPLICATION_JSON)
						.content(content).contentType(org.springframework.http.MediaType.APPLICATION_JSON));

		resultAction.andExpect(status().is2xxSuccessful());
		final OperationResultModel result = mapper
				.readValue(resultAction.andReturn().getResponse().getContentAsString(), OperationResultModel.class);

		// Waits to recieve indication
		final String indicationStr = completableFutureIndication.get(5, TimeUnit.SECONDS);
		final SSAPMessage<SSAPBodyIndicationMessage> indication = SSAPJsonParser.getInstance()
				.deserialize(indicationStr);
		Assert.assertNotNull(indication);

		client.disconnect();
	}

	// @Test
	// public void
	// given_OneMqttClientConnection_When_ItSendsAsubscribeMessagaAndIndicationOccurs_Then_ItGetsTheIndication()
	// throws InterruptedException, SSAPParseException, ExecutionException,
	// TimeoutException, MqttPersistenceException, MqttException {
	//
	// final SSAPMessage<SSAPBodyJoinMessage> request =
	// SSAPMessageGenerator.generateJoinMessageWithToken();
	// final String requestStr = SSAPJsonParser.getInstance().serialize(request);
	// final MqttClient client = new MqttClient(broker_url, clientId, persistence);
	// final MqttConnectOptions connOpts = new MqttConnectOptions();
	//
	// connOpts.setCleanSession(true);
	// client.connect(connOpts);
	//
	// client.subscribe("/topic/message/" + client.getClientId(), new
	// IMqttMessageListener() {
	// @Override
	// public void messageArrived(String topic, MqttMessage message) throws
	// Exception {
	// final String response = new String(message.getPayload());
	// completableFutureMessage.complete(response);
	// }
	// });
	//
	// client.subscribe("/topic/subscription/" + client.getClientId(), new
	// IMqttMessageListener() {
	// @Override
	// public void messageArrived(String topic, MqttMessage message) throws
	// Exception {
	// final String response = new String(message.getPayload());
	// completableFutureMessage.complete(response);
	// }
	// });
	//
	// final MqttMessage message = new MqttMessage(requestStr.getBytes());
	// message.setQos(qos);
	// client.publish(topic, message);
	//
	// final String responseStr = completableFutureMessage.get(5, TimeUnit.SECONDS);
	// final SSAPMessage<SSAPBodyReturnMessage> response =
	// SSAPJsonParser.getInstance().deserialize(responseStr);
	//
	// Assert.assertNotNull(responseStr);
	// Assert.assertEquals(SSAPMessageDirection.RESPONSE, response.getDirection());
	// Assert.assertNotNull(response.getSessionKey());
	// Assert.assertEquals(session.getSessionKey(), response.getSessionKey());
	//
	//
	// client.disconnect();
	// }

}

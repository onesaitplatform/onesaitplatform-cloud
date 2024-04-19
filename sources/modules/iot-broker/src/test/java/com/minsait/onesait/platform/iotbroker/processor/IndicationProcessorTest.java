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
///**
// * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
// * 2013-2019 SPAIN
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *      http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.minsait.onesait.platform.iotbroker.processor;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.io.IOException;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
//import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
//import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
//import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
//import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
//import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
//import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
//import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;
//import com.minsait.onesait.platform.config.repository.SuscriptionModelRepository;
//import com.minsait.onesait.platform.iotbroker.audit.aop.IotBrokerAuditableAspect;
//import com.minsait.onesait.platform.iotbroker.mock.pojo.Person;
//import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
//import com.minsait.onesait.platform.iotbroker.mock.router.RouterServiceGenerator;
//import com.minsait.onesait.platform.iotbroker.mock.ssap.SSAPMessageGenerator;
//import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
//import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
//import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
//import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
//import com.minsait.onesait.platform.router.service.app.service.RouterService;
//import com.minsait.onesait.platform.router.service.app.service.RouterSubscriptionService;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Ignore
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Slf4j
//public class IndicationProcessorTest {
//	private MockMvc mockMvc;
//	@Autowired
//	private WebApplicationContext wac;
//	private ResultActions resultAction;
//	private final String URL_BASE_PATH = "/advice";
//	@Autowired
//	ObjectMapper mapper;
//
//	@Autowired
//	MessageProcessorDelegate processor;
//
//	@Autowired
//	GatewayNotifier notifier;
//
//	@MockBean
//	SecurityPluginManager securityPluginManager;
//	@MockBean
//	RouterService routerService;
//	@MockBean
//	RouterSubscriptionService routerSuscriptionService;
//
//	//
//	// @Autowired
//	// MockMongoOntologies mockOntologies;
//
//	@Autowired
//	SuscriptionModelRepository repositoy;
//
//	SSAPMessage<SSAPBodySubscribeMessage> ssapSbuscription;
//
//	Person subject = PojoGenerator.generatePerson();
//
//	IoTSession session;
//
//	SSAPMessage<SSAPBodyInsertMessage> ssapInsertOperation;
//
//	String subjectSubscriptionId;
//
//	@MockBean
//	DeviceManager deviceManager;
//
//	@MockBean
//	SuscriptionModelRepository suscriptionModelRepository;
//
//	@MockBean
//	IotBrokerAuditableAspect iotBrokerAuditableAspect;
//
//	private void auditMocks() {
//		try {
//			doNothing().when(iotBrokerAuditableAspect).processTx(any(), any(), any(), any());
//			doNothing().when(iotBrokerAuditableAspect).doRecoveryActions(any(), any(), any(), any(), any());
//
//		} catch (final Throwable e) {
//			log.error(e.getMessage());
//		}
//	}
//
//	private void securityMocks() {
//		session = PojoGenerator.generateSession();
//		when(deviceManager.registerActivity(any(), any(), any(), any())).thenReturn(true);
//
//		when(securityPluginManager.getSession(anyString())).thenReturn(Optional.of(session));
//		when(securityPluginManager.checkSessionKeyActive(anyString())).thenReturn(true);
//		when(securityPluginManager.checkAuthorization(any(), any(), any())).thenReturn(true);
//	}
//
//	@Before
//	public void setUp() throws IOException, Exception {
//		// repositoy.deleteByOntologyName(Person.class.getSimpleName());
//		securityMocks();
//		auditMocks();
//		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
//		// mockOntologies.createOntology(Person.class);
//		//
//		ssapSbuscription = SSAPMessageGenerator.generateSubscriptionMessage(Person.class.getSimpleName(),
//				session.getSessionKey(), SSAPQueryType.SQL, "select * from Person");
//		//
//		subject = PojoGenerator.generatePerson();
//		ssapInsertOperation = SSAPMessageGenerator.generateInsertMessage(Person.class.getSimpleName(), subject);
//
//		final SuscriptionNotificationsModel sus = new SuscriptionNotificationsModel();
//		sus.setSessionKey(session.getSessionKey());
//
//		when(suscriptionModelRepository.findAllBySuscriptionId(any())).thenReturn(sus);
//
//	}
//
//	@After
//	public void tearDown() {
//		// repositoy.deleteBySuscriptionId(subjectSubscriptionId);
//		// repositoy.deleteByOntologyName(Person.class.getSimpleName());
//		// mockOntologies.deleteOntology(Person.class);
//	}
//
//	// @Ignore
//	@Test
//	public void given_OneIndication_When_ItIsDelivered_Then_ItsProcessedAndDeliveredToClient() throws Exception {
//		final CompletableFuture<SSAPMessage<SSAPBodyIndicationMessage>> completableFuture = new CompletableFuture<>();
//		SSAPMessage<SSAPBodyIndicationMessage> indication = new SSAPMessage<>();
//
//		notifier.addSubscriptionListener("test_subscriptor", m -> {
//			completableFuture.complete(m);
//		});
//
//		ssapSbuscription.getBody().setQuery("db.Person.find({})");
//		ssapSbuscription.getBody().setQueryType(SSAPQueryType.NATIVE);
//		ssapSbuscription.setSessionKey(session.getSessionKey());
//		final OperationResultModel value = RouterServiceGenerator.generateSubscriptionOk(UUID.randomUUID().toString());
//		when(routerSuscriptionService.suscribe(any())).thenReturn(value);
//
//		final SSAPMessage<SSAPBodyReturnMessage> responseSubscription = processor.process(ssapSbuscription,
//				PojoGenerator.generateGatewayInfo());
//		final String subscriptionId = responseSubscription.getBody().getData().at("/subscriptionId").asText();
//		subjectSubscriptionId = subscriptionId;
//
//		final NotificationCompositeModel model = RouterServiceGenerator
//				.generateNotificationCompositeModel(subscriptionId, subject, session);
//
//		final String content = mapper.writeValueAsString(model);
//		resultAction = mockMvc.perform(
//				MockMvcRequestBuilders.post(URL_BASE_PATH).accept(org.springframework.http.MediaType.APPLICATION_JSON)
//						.content(content).contentType(org.springframework.http.MediaType.APPLICATION_JSON));
//
//		resultAction.andExpect(status().is2xxSuccessful());
//		final OperationResultModel result = mapper
//				.readValue(resultAction.andReturn().getResponse().getContentAsString(), OperationResultModel.class);
//
//		indication = completableFuture.get(5, TimeUnit.SECONDS);
//		Assert.assertNotNull(indication);
//		Assert.assertEquals(session.getSessionKey(), indication.getSessionKey());
//		Assert.assertTrue(SSAPMessageDirection.REQUEST.equals(indication.getDirection()));
//		Assert.assertTrue(indication.getBody().getSubsciptionId().equals(subscriptionId));
//		Assert.assertTrue(indication.getBody().getData().isArray());
//		Assert.assertTrue(indication.getBody().getData().size() == 1);
//	}
//
//	@Ignore
//	@Test
//	public void given_OneSubsctiptionToOntologyThenWhenAninsertOccursThenAnIndicationIsReceived() throws Exception {
//
//		// final CompletableFuture<SSAPMessage<SSAPBodyIndicationMessage>> future = new
//		// CompletableFuture<>();
//
//		ssapSbuscription.getBody().setQuery("db.Person.find({})");
//		ssapSbuscription.getBody().setQueryType(SSAPQueryType.NATIVE);
//		final SSAPMessage<SSAPBodyReturnMessage> responseSubscription = processor.process(ssapSbuscription,
//				PojoGenerator.generateGatewayInfo());
//
//		final String oid = UUID.randomUUID().toString();
//		final OperationResultModel value = RouterServiceGenerator.generateInserOk(oid);
//		when(routerService.insert(any())).thenReturn(value);
//
//		final SSAPMessage<SSAPBodyReturnMessage> responseInsert = processor.process(ssapInsertOperation,
//				PojoGenerator.generateGatewayInfo());
//
//		try {
//			Thread.sleep(600000);
//		} catch (final InterruptedException e) {
//			log.error(e.getMessage());
//		}
//
//	}
//
//}

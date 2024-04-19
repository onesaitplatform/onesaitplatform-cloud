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
/// **
// * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
// * 2013-2019 SPAIN
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
// package com.minsait.onesait.platform.iotbroker.processor;
//
// import static org.mockito.Matchers.any;
// import static org.mockito.Matchers.anyString;
// import static org.mockito.Mockito.when;
//
// import java.io.IOException;
// import java.util.Optional;
//
// import org.junit.After;
// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Ignore;
// import org.junit.Test;
// import org.junit.experimental.categories.Category;
// import org.junit.runner.RunWith;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.context.junit4.SpringRunner;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ArrayNode;
// import com.minsait.onesait.platform.commons.testing.IntegrationTest;
// import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
// import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
// import
/// com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
// import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
// import com.minsait.onesait.platform.config.services.ontology.OntologyService;
// import com.minsait.onesait.platform.iotbroker.mock.pojo.Person;
// import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
// import
/// com.minsait.onesait.platform.iotbroker.mock.router.RouterServiceGenerator;
// import com.minsait.onesait.platform.iotbroker.mock.ssap.SSAPMessageGenerator;
// import
/// com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
// import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
// import
/// com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
// import
/// com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
// import com.minsait.onesait.platform.router.service.app.service.RouterService;
// import
/// com.minsait.onesait.platform.router.service.app.service.RouterSuscriptionService;
//
// import lombok.extern.slf4j.Slf4j;
//
// @RunWith(SpringRunner.class)
// @SpringBootTest
// @Category(IntegrationTest.class)
// @Ignore
// @Slf4j
// public class QueryProcessorTest {
// @Autowired
// ObjectMapper objectMapper;
//
// @Autowired
// MessageProcessorDelegate queryProcessor;
//
// @Autowired
// MongoBasicOpsDBRepository repository;
//
// @MockBean
// SecurityPluginManager securityPluginManager;
// @MockBean
// OntologyService ontologyService;
//
// // @Autowired
// // MockMongoOntologies mockOntologies;
//
// @MockBean
// RouterService routerService;
// @MockBean
// RouterSuscriptionService routerSuscriptionService;
//
// Person subject = PojoGenerator.generatePerson();
// String subjectId;
//
// SSAPMessage<SSAPBodyQueryMessage> ssapQuery;
// @MockBean
// DeviceManager deviceManager;
//
// // @MockBean
// // IotBrokerAuditableAspect iotBrokerAuditableAspect;
//
// private void auditMocks() {
//
// }
//
// private void securityMocks() {
// final IoTSession session = PojoGenerator.generateSession();
// when(deviceManager.registerActivity(any(), any(), any(),
/// any())).thenReturn(true);
//
// when(securityPluginManager.getSession(anyString())).thenReturn(Optional.of(session));
// when(securityPluginManager.checkSessionKeyActive(anyString())).thenReturn(true);
// when(securityPluginManager.checkAuthorization(any(), any(),
/// any())).thenReturn(true);
//
// when(ontologyService.hasUserPermissionForQuery(any(String.class),
/// any(String.class))).thenReturn(true);
// when(ontologyService.hasClientPlatformPermisionForQuery(any(String.class),
/// any(String.class))).thenReturn(true);
// }
//
// @Before
// public void setUp() throws IOException, Exception {
//
// // mockOntologies.createOntology(Person.class);
//
// subject = PojoGenerator.generatePerson();
// final String subjectInsertResult =
/// repository.insert(Person.class.getSimpleName(), "",
// objectMapper.writeValueAsString(subject));
// subjectId = subjectInsertResult;
// ssapQuery =
/// SSAPMessageGenerator.generateQueryMessage(Person.class.getSimpleName(),
/// SSAPQueryType.NATIVE, "");
//
// securityMocks();
// auditMocks();
// log.info("setUp OK");
// }
//
// @After
// public void tearDown() {
// // mockOntologies.deleteOntology(Person.class);
// }
//
// @Test
// public void
/// given_OneQueryProcessor_When_ACorrectNativeQueryIsUsed_Then_TheResponseReturnsTheResults()
// throws Exception {
// ssapQuery.getBody().setQuery("db.Person.find({})");
// SSAPMessage<SSAPBodyReturnMessage> responseMessage;
//
// final OperationResultModel value =
/// RouterServiceGenerator.generateInserOk("[{},{}]");
// when(routerService.query(any())).thenReturn(value);
// responseMessage = queryProcessor.process(ssapQuery,
/// PojoGenerator.generateGatewayInfo());
//
// Assert.assertNotNull(responseMessage);
// Assert.assertNotNull(responseMessage.getBody());
// //
/// Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.RESPONSE));
// Assert.assertNotNull(responseMessage.getBody().getData());
// Assert.assertTrue(responseMessage.getBody().getData().isArray());
// final ArrayNode array = (ArrayNode) responseMessage.getBody().getData();
// Assert.assertTrue(array.size() > 0);
//
// }
// }

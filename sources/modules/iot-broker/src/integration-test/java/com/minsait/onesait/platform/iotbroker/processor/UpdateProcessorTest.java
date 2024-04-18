/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.iotbroker.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyUpdateMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.iotbroker.mock.pojo.Person;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.mock.router.RouterServiceGenerator;
import com.minsait.onesait.platform.iotbroker.mock.ssap.SSAPMessageGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
@Ignore
public class UpdateProcessorTest {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MessageProcessorDelegate updateProcessor;

	@Autowired
	MongoBasicOpsDBRepository repository;

	@MockBean
	SecurityPluginManager securityPluginManager;

	// @Autowired
	// MockMongoOntologies mockOntologies;

	@MockBean
	RouterService routerService;

	Person subject = PojoGenerator.generatePerson();
	String subjectId;

	SSAPMessage<SSAPBodyUpdateMessage> ssapUpdate;
	SSAPMessage<SSAPBodyUpdateByIdMessage> ssapUpdateById;

	@MockBean
	DeviceManager deviceManager;

	// @MockBean
	// IotBrokerAuditableAspect iotBrokerAuditableAspect;

	private void auditMocks() {

	}

	private void securityMocks() {
		final IoTSession session = PojoGenerator.generateSession();
		when(deviceManager.registerActivity(any(), any(), any(), any())).thenReturn(true);

		when(securityPluginManager.getSession(anyString())).thenReturn(Optional.of(session));
		when(securityPluginManager.checkSessionKeyActive(any())).thenReturn(true);
		when(securityPluginManager.checkAuthorization(any(), any(), any())).thenReturn(true);
	}

	@Before
	public void setUp() throws IOException, Exception {

		subject = PojoGenerator.generatePerson();
		final String subjectInsertResult = repository.insert(Person.class.getSimpleName(),
				objectMapper.writeValueAsString(subject));
		subjectId = subjectInsertResult;
		ssapUpdate = SSAPMessageGenerator.generateUpdateMessage(Person.class.getSimpleName(), "");
		final Person subjectModified = PojoGenerator.generatePerson();
		ssapUpdateById = SSAPMessageGenerator.generateUpdateByIdtMessage(Person.class.getSimpleName(),
				objectMapper.valueToTree(subjectModified));
		ssapUpdateById.getBody().setId(subjectId);

		securityMocks();
		auditMocks();
	}

	@After
	public void tearDown() {
		// mockOntologies.deleteOntology(Person.class);
	}

	@Test
	public void given_OneUpdateProcessor_When_OneOccurrenceIsUpdated_Then_TheResponseIndicatesItIsUpdated()
			throws Exception {

		ssapUpdate.getBody().setQuery(
				"db.Person.update({\"name\":\"" + subject.getName() + "\"},{$set: { \"name\": \"NAME_NEW\" }})");

		final OperationResultModel value = RouterServiceGenerator.generateUpdateDeleteResultOk(1);
		when(routerService.update(any())).thenReturn(value);

		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = updateProcessor.process(ssapUpdate,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.RESPONSE));
		Assert.assertNotNull(responseMessage.getBody().getData());
		Assert.assertEquals(1, responseMessage.getBody().getData().at("/nModified").asInt());

	}

	@Test
	public void given_OneUpdateProcessor_Then_TwoOccurrencesAreUpdated_ThenTheResponseIndicatesTheTwoOccurrencesWereUpdated()
			throws Exception {

		repository.insert(Person.class.getSimpleName(), objectMapper.writeValueAsString(subject));
		repository.insert(Person.class.getSimpleName(), objectMapper.writeValueAsString(subject));

		ssapUpdate.getBody().setQuery(
				"db.Person.update({\"name\":\"" + subject.getName() + "\"},{$set: { \"name\": \"NAME_NEW\" }})");

		final OperationResultModel value = RouterServiceGenerator.generateUpdateDeleteResultOk(3);
		when(routerService.update(any())).thenReturn(value);
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = updateProcessor.process(ssapUpdate,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.RESPONSE));
		Assert.assertNotNull(responseMessage.getBody().getData());
		Assert.assertTrue(responseMessage.getBody().getData().at("/nModified").asInt() > 1);
	}

	@Test
	public void test_upate_no_ocurrences() throws Exception {

		repository.delete(Person.class.getSimpleName(), false);

		ssapUpdate.getBody().setQuery(
				"db.Person.update({\"name\":\"" + subject.getName() + "\"},{$set: { \"name\": \"NAME_NEW\" }})");

		final OperationResultModel value = RouterServiceGenerator.generateUpdateDeleteResultOk(0);
		when(routerService.update(any())).thenReturn(value);

		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = updateProcessor.process(ssapUpdate,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.RESPONSE));
		Assert.assertNotNull(responseMessage.getBody().getData());
		Assert.assertEquals(0, responseMessage.getBody().getData().at("/nModified").asInt());
	}

	@Test
	public void test_update_by_id() throws Exception {

		final OperationResultModel value = RouterServiceGenerator.generateUpdateByIdResultOk("{}");
		when(routerService.update(any())).thenReturn(value);
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = updateProcessor.process(ssapUpdateById,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.RESPONSE));
		Assert.assertTrue(responseMessage.getBody().isOk());
		Assert.assertNotNull(responseMessage.getBody().getData());
	}

	@Test
	public void test_update_by_non_existent_id() throws Exception {

		ssapUpdateById.getBody().setId("5a9b2ef917f81f33589e06d3");
		final OperationResultModel value = RouterServiceGenerator.generateUpdateByIdResultOk("{}");
		when(routerService.update(any())).thenReturn(value);

		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = updateProcessor.process(ssapUpdateById,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.RESPONSE));
		Assert.assertTrue(responseMessage.getBody().isOk());
		Assert.assertNotNull(responseMessage.getBody().getData());
	}

	@Test
	public void test_update_by_malformed_id() throws Exception {

		ssapUpdateById.getBody().setId(UUID.randomUUID().toString());

		final OperationResultModel value = RouterServiceGenerator.generateUpdateByIdResultOk("ERROR");
		when(routerService.update(any())).thenReturn(value);

		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = updateProcessor.process(ssapUpdateById,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertTrue(responseMessage.getDirection().equals(SSAPMessageDirection.ERROR));
		Assert.assertFalse(responseMessage.getBody().isOk());
	}

}

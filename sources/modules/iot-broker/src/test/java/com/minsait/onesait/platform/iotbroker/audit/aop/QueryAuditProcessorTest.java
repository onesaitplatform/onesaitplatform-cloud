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
package com.minsait.onesait.platform.iotbroker.audit.aop;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyQueryMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.minsait.onesait.platform.iotbroker.audit.processor.QueryAuditProcessor;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class QueryAuditProcessorTest {

	@InjectMocks
	private QueryAuditProcessor queryAuditProcessor;

	private final String ONTOLOGY_NAME = "ontology";
	private final String QUERY = "db.test.find({});";

	@Test
	public void given_a_query_message_get_audit_event() {

		IoTSession session = PojoGenerator.generateSession();
		GatewayInfo info = PojoGenerator.generateGatewayInfo();

		SSAPMessage<SSAPBodyQueryMessage> message = new SSAPMessage<SSAPBodyQueryMessage>();
		message.setMessageType(SSAPMessageTypes.QUERY);
		message.setBody(new SSAPBodyQueryMessage());
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.getBody().setOntology(ONTOLOGY_NAME);
		message.getBody().setQuery(QUERY);

		IotBrokerAuditEvent event = queryAuditProcessor.process(message, session, info);

		Assert.assertEquals(OperationType.QUERY.name(), event.getOperationType());
		Assert.assertEquals(ONTOLOGY_NAME, event.getOntology());
		Assert.assertNotNull(event.getQuery());
		Assert.assertNull(event.getData());
		Assert.assertEquals(info, event.getGatewayInfo());
		Assert.assertEquals(Module.IOTBROKER, event.getModule());
	}

	@Test
	public void given_getMessageTypes() {
		List<SSAPMessageTypes> types = queryAuditProcessor.getMessageTypes();
		List<SSAPMessageTypes> expected = Arrays.asList(SSAPMessageTypes.QUERY);
		Assert.assertEquals(expected, types);
	}
}

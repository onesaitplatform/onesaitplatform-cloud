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
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.minsait.onesait.platform.iotbroker.audit.processor.JoinAuditProcessor;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class JoinAuditProcessorTest {

	@InjectMocks
	private JoinAuditProcessor joinAuditProcessor;

	@Test
	public void given_a_join_message_get_audit_event() {

		IoTSession session = PojoGenerator.generateSession();
		GatewayInfo info = PojoGenerator.generateGatewayInfo();

		SSAPMessage<SSAPBodyJoinMessage> message = new SSAPMessage<SSAPBodyJoinMessage>();
		message.setMessageType(SSAPMessageTypes.JOIN);
		message.setBody(new SSAPBodyJoinMessage());
		message.setDirection(SSAPMessageDirection.REQUEST);

		IotBrokerAuditEvent event = joinAuditProcessor.process(message, session, info);

		Assert.assertEquals(OperationType.JOIN.name(), event.getOperationType());
		Assert.assertNull(event.getOntology());
		Assert.assertNull(event.getQuery());
		Assert.assertNull(event.getData());
		Assert.assertEquals(info, event.getGatewayInfo());
		Assert.assertEquals(Module.IOTBROKER, event.getModule());
	}

	@Test
	public void given_getMessageTypes() {
		List<SSAPMessageTypes> types = joinAuditProcessor.getMessageTypes();
		List<SSAPMessageTypes> expected = Arrays.asList(SSAPMessageTypes.JOIN);
		Assert.assertEquals(expected, types);
	}
}

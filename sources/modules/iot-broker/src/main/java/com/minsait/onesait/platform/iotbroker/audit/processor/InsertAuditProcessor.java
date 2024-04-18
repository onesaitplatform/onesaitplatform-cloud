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
package com.minsait.onesait.platform.iotbroker.audit.processor;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyInsertMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.audit.aop.MessageAuditProcessor;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEventFactory;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InsertAuditProcessor implements MessageAuditProcessor {

	@Override
	public IotBrokerAuditEvent process(SSAPMessage<? extends SSAPBodyMessage> message, IoTSession session,
			GatewayInfo info) {
		log.debug("Processing insert message");
		SSAPBodyInsertMessage insertMessage = (SSAPBodyInsertMessage) message.getBody();
		String insertMessageText = "Insert message on ontology " + insertMessage.getOntology();
		return IotBrokerAuditEventFactory.builder().build().createIotBrokerAuditEvent(insertMessage, insertMessageText,
				session, info);
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.INSERT);
	}

}

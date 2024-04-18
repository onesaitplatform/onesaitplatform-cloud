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
package com.minsait.onesait.platform.iotbroker.audit.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommitTransactionMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptyMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptySessionMandatoryMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.audit.aop.MessageAuditProcessor;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEventFactory;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.impl.TransactionProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TransactionAuditProcessor implements MessageAuditProcessor {

	@Autowired
	private TransactionProcessor transactionProcessor;

	@Override
	public IotBrokerAuditEvent process(SSAPMessage<? extends SSAPBodyMessage> message, IoTSession session,
			GatewayInfo info) {

		log.debug("Processing transaction message");
		IotBrokerAuditEvent event = null;

		switch (message.getMessageType()) {
		case START_TRANSACTION:
			final SSAPBodyEmptySessionMandatoryMessage startMessage = (SSAPBodyEmptySessionMandatoryMessage) message
					.getBody();
			event = IotBrokerAuditEventFactory.builder().build().createIotBrokerAuditEvent(
					OperationType.START_TRANSACTION,
					"Start transaction message by sessionkey: " + message.getSessionKey(), session, info,
					startMessage.getTags());
			break;
		case COMMIT_TRANSACTION:
			final SSAPBodyCommitTransactionMessage commitMessage = (SSAPBodyCommitTransactionMessage) message.getBody();
			event = IotBrokerAuditEventFactory.builder().build().createIotBrokerAuditEvent(
					OperationType.COMMIT_TRANSACTION,
					"Commit transaction message by sessionkey: " + message.getSessionKey(), session, info,
					commitMessage.getTags());
			break;
		case ROLLBACK_TRANSACTION:
			final SSAPBodyEmptyMessage rollbackMessage = (SSAPBodyEmptyMessage) message.getBody();
			event = IotBrokerAuditEventFactory.builder().build().createIotBrokerAuditEvent(
					OperationType.ROLLBACK_TRANSACTION,
					"Rollback transaction message by sessionkey: " + message.getSessionKey(), session, info,
					rollbackMessage.getTags());
			break;
		}

		return event;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return transactionProcessor.getMessageTypes();
	}

}

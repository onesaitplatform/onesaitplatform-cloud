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
package com.minsait.onesait.platform.iotbroker.processor.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommitTransactionMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyEmptySessionMandatoryMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.common.MessageException;
import com.minsait.onesait.platform.iotbroker.common.exception.BaseException;
import com.minsait.onesait.platform.iotbroker.common.exception.OntologySchemaException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.iotbroker.processor.MessageTypeProcessor;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.TransactionModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransactionProcessor implements MessageTypeProcessor {

	private final List<SSAPMessageTypes> supportedTypes = new ArrayList<>();

	@Autowired
	private RouterService routerService;

	@Autowired
	ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		supportedTypes.add(SSAPMessageTypes.START_TRANSACTION);
		supportedTypes.add(SSAPMessageTypes.COMMIT_TRANSACTION);
		supportedTypes.add(SSAPMessageTypes.ROLLBACK_TRANSACTION);
	}

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message, GatewayInfo info, Optional<IoTSession> session)
			throws BaseException {

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		SSAPBodyReturnMessage bodyReturn = new SSAPBodyReturnMessage();

		OperationResultModel transactionResult = new OperationResultModel();

		final TransactionModel model = TransactionModel.builder().clientSession(message.getSessionKey())
				.transactionId(message.getTransactionId()).build();

		switch (message.getMessageType()) {
		case START_TRANSACTION:
			model.setType(TransactionModel.OperationType.START_TRANSACTION);

			transactionResult = routerService.startTransaction(model);

			try {
				bodyReturn.setData(objectMapper
						.readTree(String.format("{\"transactionId\": \"%s\"}", transactionResult.getResult())));
			} catch (JsonProcessingException e) {
				log.error("Error processing JSON in START_TRANSACTION. {}", e);
			} catch (IOException e) {
				log.error("Error writing data in START_TRANSACTION. {}", e);
			}
			break;
		case COMMIT_TRANSACTION:
			model.setType(TransactionModel.OperationType.COMMIT_TRANSACTION);

			model.setLockTransaction(((SSAPBodyCommitTransactionMessage) message.getBody()).isLockOntologies());
			transactionResult = routerService.commitTransaction(model);
			try {
				bodyReturn.setData(
						objectMapper.readTree(String.format("{\"result\": \"%s\"}", transactionResult.getResult())));
			} catch (JsonProcessingException e) {
				log.error("Error processing JSON in COMMIT_TRANSACTION. {}", e);
			} catch (IOException e) {
				log.error("Error writing data in COMMIT_TRANSACTION. {}", e);
			}
			break;
		case ROLLBACK_TRANSACTION:
			model.setType(TransactionModel.OperationType.ROLLBACK_TRANSACTION);

			transactionResult = routerService.rollbackTransaction(model);
			try {
				bodyReturn.setData(
						objectMapper.readTree(String.format("{\"result\": \"%s\"}", transactionResult.getResult())));
			} catch (JsonProcessingException e) {
				log.error("Error processing JSON in ROLLBACK_TRANSACTION. {}", e);
			} catch (IOException e) {
				log.error("Error writing data in ROLLBACK_TRANSACTION. {}", e);
			}
			break;
		}
		bodyReturn.setOk(transactionResult.isStatus());

		if (!transactionResult.isStatus()) {
			bodyReturn.setError(transactionResult.getMessage());
			bodyReturn.setErrorCode(SSAPErrorCode.AUTHORIZATION);
			responseMessage.setDirection(SSAPMessageDirection.ERROR);
		}
		responseMessage.setBody(bodyReturn);

		return responseMessage;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return supportedTypes;
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message)
			throws OntologySchemaException, BaseException {
		if (SSAPMessageTypes.START_TRANSACTION.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyEmptySessionMandatoryMessage> startTxMessage = (SSAPMessage<SSAPBodyEmptySessionMandatoryMessage>) message;
			return validateStartTransaction(startTxMessage);
		}

		if (SSAPMessageTypes.COMMIT_TRANSACTION.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyEmptySessionMandatoryMessage> commitTxMessage = (SSAPMessage<SSAPBodyEmptySessionMandatoryMessage>) message;
			return validateTransactionIdExists(commitTxMessage);
		}

		if (SSAPMessageTypes.ROLLBACK_TRANSACTION.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyEmptySessionMandatoryMessage> rollbackTxMessage = (SSAPMessage<SSAPBodyEmptySessionMandatoryMessage>) message;
			return validateTransactionIdExists(rollbackTxMessage);
		}

		return false;
	}

	private boolean validateStartTransaction(SSAPMessage<SSAPBodyEmptySessionMandatoryMessage> message)
			throws SSAPProcessorException {
		return true;
	}

	private boolean validateTransactionIdExists(SSAPMessage<SSAPBodyEmptySessionMandatoryMessage> message)
			throws SSAPProcessorException {
		if (!StringUtils.hasText(message.getTransactionId())) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "transactionId",
					message.getMessageType().name()));
		}
		return true;
	}

}

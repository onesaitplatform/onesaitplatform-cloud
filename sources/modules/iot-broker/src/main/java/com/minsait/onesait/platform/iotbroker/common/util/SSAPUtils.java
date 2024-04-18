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
package com.minsait.onesait.platform.iotbroker.common.util;

import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;

public class SSAPUtils {

	public static SSAPMessage<SSAPBodyReturnMessage> generateErrorMessage(SSAPMessage message, SSAPErrorCode code,
			String error) {
		if (message == null) {
			return generateErrorMessage(code, error);
		}

		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setDirection(SSAPMessageDirection.ERROR);
		responseMessage.setMessageId(message.getMessageId());
		responseMessage.setMessageType(message.getMessageType());
		// responseMessage.setOntology(message.getOntology());
		responseMessage.setSessionKey(message.getSessionKey());
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(false);
		responseMessage.getBody().setErrorCode(code);
		responseMessage.getBody().setError(error);

		return responseMessage;
	}

	public static SSAPMessage<SSAPBodyReturnMessage> generateErrorMessage(SSAPErrorCode code, String error) {
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setDirection(SSAPMessageDirection.ERROR);
		// responseMessage.setMessageId(message.getMessageId());
		// responseMessage.setMessageType(message.getMessageType());
		// responseMessage.setOntology(message.getOntology());
		// responseMessage.setSessionKey(message.getSessionKey());
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(false);
		responseMessage.getBody().setErrorCode(code);
		responseMessage.getBody().setError(error);

		return responseMessage;
	}
}

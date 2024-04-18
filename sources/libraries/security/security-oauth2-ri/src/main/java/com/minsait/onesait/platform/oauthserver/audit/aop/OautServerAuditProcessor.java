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
package com.minsait.onesait.platform.oauthserver.audit.aop;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.oauthserver.audit.bean.OauthServerAuditEvent;

@Service
public class OautServerAuditProcessor {

	public OauthServerAuditEvent genetateAuditEvent(String user, String messageId, String message, OperationType operationType, String response) {
		final Date today = new Date();
		OauthServerAuditEvent auditEvent = OauthServerAuditEvent.builder().id(UUID.randomUUID().toString())
				.timeStamp(today.getTime()).formatedTimeStamp(CalendarUtil.builder().build().convert(today)).user(user)
				.message(message).module(Module.OAUTHSERVER).type(EventType.SECURITY).operationType(operationType.name())
				.resultOperation(ResultOperationType.SUCCESS).infoMessage(messageId).response(response).build();
		return auditEvent;
	}
	
	public OauthServerAuditEvent genetateErrorEvent(String user, String messageId, String message, OperationType operationType, String response) {
		final Date today = new Date();
		OauthServerAuditEvent auditEvent = OauthServerAuditEvent.builder().id(UUID.randomUUID().toString())
				.timeStamp(today.getTime()).formatedTimeStamp(CalendarUtil.builder().build().convert(today)).user(user)
				.message(message).module(Module.OAUTHSERVER).type(EventType.SECURITY).operationType(operationType.name())
				.resultOperation(ResultOperationType.ERROR).infoMessage(messageId).response(response).build();
		return auditEvent;
	}

}

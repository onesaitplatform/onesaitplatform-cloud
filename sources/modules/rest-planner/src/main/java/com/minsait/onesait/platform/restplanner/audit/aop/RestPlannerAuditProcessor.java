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
package com.minsait.onesait.platform.restplanner.audit.aop;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.restplanner.audit.bean.RestPlannerAuditEvent;

@Service
public class RestPlannerAuditProcessor {

	public RestPlannerAuditEvent getExecutionInfo(String user, String message, String response) {
		final Date today = new Date();
		RestPlannerAuditEvent auditEvent = RestPlannerAuditEvent.builder().id(UUID.randomUUID().toString())
				.timeStamp(today.getTime()).formatedTimeStamp(CalendarUtil.builder().build().convert(today)).user(user)
				.message("REST_PLANNER_CRON_EXECUTION").module(Module.PLANNER).type(EventType.QUERY)
				.operationType(OPAuditEvent.OperationType.EXECUTION.name()).resultOperation(ResultOperationType.SUCCESS)
				.otherType("RestPlannerCRONExecution").infoMessage(message).response(response).build();
		return auditEvent;
	}

	public OPAuditError getErrorEvent(String user, String message, Exception ex) {

		final String errMessage = "Exception detected while doing HTTP Request for user: " + user + " ; Message: "
				+ message;

		return OPEventFactory.builder().build().createAuditEventError(errMessage, Module.PLANNER, ex);

	}

}

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
package com.minsait.onesait.platform.audit.aop;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.DashboardEngineAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;

@Component
public class DashboardEngineAuditProcessor {

	public DashboardEngineAuditEvent genetateAuditEvent(String user, String messageId, String message,
			OperationType operationType, String response, String dashboard, String datasource,String datasourceParameters) {
		final Date today = new Date();
		return DashboardEngineAuditEvent.builder().id(UUID.randomUUID().toString()).timeStamp(today.getTime())
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).user(user).message(message)
				.module(Module.DASHBOARDENGINE).type(EventType.SECURITY).operationType(operationType.name())
				.resultOperation(ResultOperationType.SUCCESS).infoMessage(messageId).response(response)
				.dashboard(dashboard).datasource(datasource).build();

	}

	public DashboardEngineAuditEvent completeAuditEventWithError(DashboardEngineAuditEvent event, Throwable e) {
		event.setResultOperation(ResultOperationType.ERROR);
		event.setMessage(e.getMessage());
		return event;
	}

	public DashboardEngineAuditEvent completeAuditEvent(DashboardEngineAuditEvent event, String response) {
		event.setResultOperation(ResultOperationType.SUCCESS);
		event.setMessage(response);
		return event;
	}

	public DashboardEngineAuditEvent genetateErrorEvent(String user, String messageId, String message,
			OperationType operationType, String response, String dashboard, String datasource,String datasourceParameters) {
		final Date today = new Date();
		return DashboardEngineAuditEvent.builder().id(UUID.randomUUID().toString()).timeStamp(today.getTime())
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).user(user).message(message)
				.module(Module.DASHBOARDENGINE).type(EventType.SECURITY).operationType(operationType.name())
				.resultOperation(ResultOperationType.ERROR).infoMessage(messageId).response(response)
				.dashboard(dashboard).datasource(datasource).build();

	}

}

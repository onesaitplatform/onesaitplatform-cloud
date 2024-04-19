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
package com.minsait.onesait.platform.audit.bean;

import java.util.Date;
import java.util.UUID;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.security.core.context.SecurityContextHolder;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;

import lombok.Builder;

@Builder
public class OPEventFactory {

	public OPAuditError createAuditEventError(String userId, String message, String remoteAddress, Module module,
			Exception e) {
		final OPAuditError event = createAuditEventError(userId, message, module, e);
		event.setRemoteAddress(remoteAddress);
		return event;
	}

	public OPAuditError createAuditEventError(String userId, String message, Module module, Exception e) {

		final OPAuditError event = createAuditEventError(message, module, e);
		event.setUser(userId);

		return event;

	}

	public OPAuditError createAuditEventError(String message, Module module, Exception e) {

		final OPAuditError event = createAuditEventError(message);
		setErrorDetails(event, e);
		event.setModule(module);
		event.setErrorMessage(e.getMessage());
		return createAuditEventError(event, message);
	}

	public OPAuditError createAuditEventError(String message) {
		final OPAuditError event = new OPAuditError();
		return createAuditEventError(event, message);
	}

	public OPAuditError createAuditEventError(OPAuditError event, String message) {
		final Date today = new Date();
		event.setId(UUID.randomUUID().toString());
		event.setTimeStamp(today.getTime());
		event.setFormatedTimeStamp(CalendarUtil.builder().build().convert(today));
		event.setMessage(message);
		event.setType(EventType.ERROR);
		event.setOperationType("");
		setSecurityData(event);
		return event;
	}

	public OPAuditError createAuditEventWarning(String message) {
        final OPAuditError event = new OPAuditError();
	    final Date today = new Date();
        event.setId(UUID.randomUUID().toString());
        event.setTimeStamp(today.getTime());
        event.setFormatedTimeStamp(CalendarUtil.builder().build().convert(today));
        event.setMessage(message);
        event.setType(EventType.WARNING);
        event.setOperationType("");
        setSecurityData(event);
        return event;
    }
	
	public OPAuditEvent createAuditEvent(AuditApplicationEvent actualAuditEvent, EventType type, String message) {
		final OPAuditEvent event = new OPAuditEvent();

		final AuditEvent audit = actualAuditEvent.getAuditEvent();

		setSecurityData(event);

		event.setUser(audit.getPrincipal());
		event.setTimeStamp(audit.getTimestamp().toEpochMilli());
		event.setFormatedTimeStamp(audit.getTimestamp().toString());

		event.setMessage(message);
		event.setOtherType(audit.getType());
		event.setExtraData(audit.getData());
		event.setType(type);
		return event;
	}

	public OPAuditEvent createAuditEvent(EventType type, String message) {
		final OPAuditEvent event = new OPAuditEvent();
		return createAuditEvent(event, type, message);
	}

	public OPAuthAuditEvent createAuditAuthEvent(EventType type, String message) {
		final OPAuthAuditEvent event = new OPAuthAuditEvent();
		return createAuditAuthEvent(event, type, message);
	}

	public OPAuthAuditEvent createAuditAuthEvent(OPAuthAuditEvent event, EventType type, String message) {

		event.setType(type);
		final Date today = new Date();
		event.setTimeStamp(today.getTime());
		event.setFormatedTimeStamp(CalendarUtil.builder().build().convert(today));
		event.setMessage(message);
		event.setId(UUID.randomUUID().toString());
		setSecurityData(event);
		return event;
	}

	public OPAuditEvent createAuditEvent(OPAuditEvent event, EventType type, String message) {
		event.setType(type);

		final Date today = new Date();

		event.setTimeStamp(today.getTime());
		event.setFormatedTimeStamp(CalendarUtil.builder().build().convert(today));
		event.setMessage(message);
		event.setId(UUID.randomUUID().toString());
		setSecurityData(event);
		return event;
	}

	private void setSecurityData(OPAuditEvent event) {

		if (SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null) {

			event.setUser(SecurityContextHolder.getContext().getAuthentication().getName());

		}
	}

	public void setErrorDetails(OPAuditError event, final Throwable cause) {
		if (cause != null) {
			Throwable rootCause = cause;
			while (rootCause.getCause() != null && rootCause.getCause() != rootCause)
				rootCause = rootCause.getCause();

			event.setClassName(rootCause.getStackTrace()[0].getClassName());
			event.setMethodName(rootCause.getStackTrace()[0].getMethodName());
			event.setType(EventType.ERROR);
		}

	}

}

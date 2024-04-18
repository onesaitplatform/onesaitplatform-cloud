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
package com.minsait.onesait.platform.router.audit.aop;

import org.aspectj.lang.JoinPoint;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;

public class Sofia2RouterEventFactory {

	public static OPAuditEvent createAuditEvent(JoinPoint joinPoint, Auditable auditable, EventType type,
			String message) {
		OPAuditEvent event = new OPAuditEvent();
		return OPEventFactory.builder().build().createAuditEvent(event, type, message);
	}
}

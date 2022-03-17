/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class OPAuditError extends OPAuditRemoteEvent {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String errorMessage;

	@Getter
	@Setter
	private String className;

	@Getter
	@Setter
	private String methodName;

	public OPAuditError() {
		super();
	}

	public OPAuditError(String message, String id, EventType type, long timeStamp, String formatedTimeStamp,
			String user, String ontology, String operationType, Module module, Map<String, Object> extraData,
			String otherType, String remoteAddress, ResultOperationType resultOperation) {

		super(message, id, type, timeStamp, formatedTimeStamp, user, ontology, operationType, module, extraData,
				otherType, remoteAddress, resultOperation);

	}

	@Override
	public String toString() {

		return "OPAuditError [message=" + message + ", id=" + id + ", type=" + type + ", timeStamp=" + timeStamp
				+ ", user=" + user + ", ontology=" + ontology + ", operationType=" + operationType + ", module="
				+ module + ", errorMessage=" + errorMessage + ", extraData=" + extraData + ", otherType=" + otherType + ", className="
				+ className + ", methodName=" + methodName + "]";
	}

}

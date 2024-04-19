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
package com.minsait.onesait.platform.controlpanel.rest.management.audit;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonInclude(value = Include.NON_NULL)
public class OPAuditEventDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum EventType {
		USER, SECURITY, ERROR, DATA, GENERAL, IOTBROKER, APIMANAGER, FLOWENGINE, BATCH, QUERY;
	}


	public enum ResultOperationType {
		ERROR, SUCCESS, WARNING
	}

	@Getter
	@Setter
	protected String message;

	@Getter
	@Setter
	protected EventType type;

	@Getter
	@Setter
	protected long timeStamp;

	@Getter
	@Setter
	protected String formatedTimeStamp;

	@Getter
	@Setter
	protected String operationType;

	@Getter
	@Setter
	protected String otherType;

	@Getter
	@Setter
	protected ResultOperationType resultOperation;

	@Getter
	@Setter
	protected String ontology;

	public OPAuditEventDTO() {
		super();
	}

	public OPAuditEventDTO(String message, String id, EventType type, long timeStamp, String formatedTimeStamp,
			String operationType, Map<String, Object> extraData, String otherType, ResultOperationType resultOperation) {
		super();
		this.message = message;
		this.type = type;
		this.timeStamp = timeStamp;
		this.formatedTimeStamp = formatedTimeStamp;
		this.operationType = operationType;
		this.otherType = otherType;
		this.resultOperation = resultOperation;
	}

	@Override
	public String toString() {
		return "OPAuditEvent [message=" + message + ", type=" + type + ", timeStamp=" + timeStamp
				+ ", operationType=" + operationType + ", otherType=" + otherType
				+ ", result=" + resultOperation	+ "]";
	}

	public String toJson() {

		final String json = "";
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException e) {
			log.error("Error parsing audit event ", e);
		}

		return json;
	}

}
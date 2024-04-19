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
public class OPAuditEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum EventType {
		USER, SECURITY, ERROR, DATA, GENERAL, IOTBROKER, APIMANAGER, FLOWENGINE, BATCH, QUERY, SYSTEM
	}

	public enum Module {
		CONTROLPANEL, APIMANAGER, IOTBROKER, FLOWENGINE, ROUTER, RTDBMAINTAINER, SQLENGINE, PLANNER, OAUTHSERVER, DATAFLOW, NOTEBOOK, JAVACLIENT, PYTHONCLIENT, PERSISTENCE, DASHBOARDENGINE, IDENTITY_MANAGER
	}

	public enum OperationType {

		LOGIN, LOGIN_OAUTH, LOGOUT, JOIN, LEAVE, INSERT, UPDATE, DELETE, QUERY, SUBSCRIBE, UNSUBSCRIBE, INDICATION, COMMAND, START, STOP, LOG, START_TRANSACTION, COMMIT_TRANSACTION, ROLLBACK_TRANSACTION, EXECUTION, OAUTH_TOKEN_GENERATION, OAUTH_TOKEN_CHECK, OAUTH_TOKEN_REFRESH, OAUTH_TOKEN_REVOCATION, OAUTH_TOKEN_OIDC, NOTEBOOK_INVOCATION, SEND_MAIL, API_INVOCATION, START_DATAFLOW, STOP_DATAFLOW, CKECK_STATUS_DATAFLOW, KPI_EXECUTION, SOLVER_DS, WEBSOCKET_CONNECTED, PLUGIN_VALIDATION_SERVICE

	}

	public enum ResultOperationType {
		ERROR, SUCCESS, WARNING
	}

	@Getter
	@Setter
	protected String message;

	@Getter
	@Setter
	protected String id;

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
	protected String user;

	@Getter
	@Setter
	protected String ontology;

	@Getter
	@Setter
	protected String operationType;

	@Getter
	@Setter
	protected Module module;

	@Getter
	@Setter
	protected transient Map<String, Object> extraData;

	@Getter
	@Setter
	protected String otherType;

	@Getter
	@Setter
	protected ResultOperationType resultOperation;

	public OPAuditEvent() {
		super();
	}

	public OPAuditEvent(String message, String id, EventType type, long timeStamp, String formatedTimeStamp,
			String user, String ontology, String operationType, Module module, Map<String, Object> extraData,
			String otherType, ResultOperationType resultOperation) {
		super();
		this.message = message;
		this.id = id;
		this.type = type;
		this.timeStamp = timeStamp;
		this.formatedTimeStamp = formatedTimeStamp;
		this.user = user;
		this.ontology = ontology;
		this.operationType = operationType;
		this.module = module;
		this.extraData = extraData;
		this.otherType = otherType;
		this.resultOperation = resultOperation;
	}

	@Override
	public String toString() {
		return "OPAuditEvent [message=" + message + ", id=" + id + ", type=" + type + ", timeStamp=" + timeStamp
				+ ", user=" + user + ", ontology=" + ontology + ", operationType=" + operationType + ", module="
				+ module + ", extraData=" + extraData + ", otherType=" + otherType + ", result=" + resultOperation
				+ "]";
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public String toJson() {

		final String json = "";
		try {
			return mapper.writeValueAsString(this);
		} catch (final JsonProcessingException e) {
			log.error("Error parsing audit event ", e);
		}

		return json;
	}

}

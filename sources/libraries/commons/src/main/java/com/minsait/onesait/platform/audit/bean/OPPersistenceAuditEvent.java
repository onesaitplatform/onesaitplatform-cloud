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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
@Slf4j
public class OPPersistenceAuditEvent extends OPAuditEvent {

	private static final long serialVersionUID = 1L;

	private String vertical;
	private String tenant;
	private String entity;
	private String entityId;
	private String entityValue;
	private String table;
	private String loggedUser;
	private static final ObjectMapper mapper = new ObjectMapper();

	public OPPersistenceAuditEvent(String message, String id, EventType type, long timeStamp, String formatedTimeStamp,
			String user, String ontology, String operationType, Module module, Map<String, Object> extraData,
			String otherType, ResultOperationType resultOperation, String vertical, String tenant, String entity,
			String entityId, String entityValue, String table, String loggedUser) {
		super();
		this.message = message;
		this.id = id;
		this.type = type;
		this.timeStamp = timeStamp;
		this.formatedTimeStamp = formatedTimeStamp;
		this.mongoTimestamp = MongoDate.builder().date(formatedTimeStamp).build();
		this.user = user;
		this.ontology = ontology;
		this.operationType = operationType;
		this.module = module;
		this.extraData = extraData;
		this.otherType = otherType;
		this.resultOperation = resultOperation;
		this.vertical = vertical;
		this.tenant = tenant;
		this.entity = entity;
		this.entityId = entityId;
		this.entityValue = entityValue;
		this.table = table;
		this.loggedUser = loggedUser;
	}

	@Override
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

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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "SUSCRIPTION_MODEL")
public class SuscriptionNotificationsModel extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum OperationType {
		SUSCRIBE, UNSUSCRIBE;
	}

	public enum QueryType {
		SQLLIKE, NATIVE;
	}

	@Column(name = "OPERATION_TYPE", length = 255)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private OperationType operationType;

	@Column(name = "QUERY_TYPE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private QueryType queryType;

	@Column(name = "ONTOLOGY_NAME", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String ontologyName;

	@Column(name = "SESSION_KEY", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String sessionKey;

	@Column(name = "SUSCRIPTION_ID", length = 512, nullable = false, unique = true)
	@NotNull
	@Getter
	@Setter
	private String suscriptionId;

	@Column(name = "QUERY", length = 1024, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String query;

	@Column(name = "USER", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String user;

}

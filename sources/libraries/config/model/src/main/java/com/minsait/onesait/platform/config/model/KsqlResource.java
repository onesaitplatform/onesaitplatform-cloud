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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "KSQL_RESOURCE")

public class KsqlResource extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum KsqlResourceType {
		STREAM, TABLE, INSERT;
	}

	public enum FlowResourceType {
		ORIGIN, PROCESS, DESTINY;
	}

	@NotNull
	@Getter
	@Setter
	@Column(name = "IDENTIFICATION", length = 255, unique = true, nullable = false)
	private String identification;

	@Column(name = "DESCRIPTION", length = 512, nullable = false)
	@Getter
	@Setter
	private String description;

	@Column(name = "KSQL_TYPE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private KsqlResourceType ksqlType;

	@Column(name = "RESOURCE_TYPE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FlowResourceType resourceType;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private Ontology ontology;

	@NotNull
	@Lob
	@Getter
	@Setter
	@Column(name = "STATEMENT_TEXT", nullable = false)
	@Type(type = "org.hibernate.type.TextType")
	private String statementText;

	@NotNull
	@Getter
	@Setter
	@Column(name = "KAFKA_TOPIC", length = 255, unique = false, nullable = false)
	private String kafkaTopic;

	@NotNull
	@Getter
	@Setter
	@Column(name = "IS_CREATED_AS", unique = false, nullable = false)
	private boolean isCreatedAs;

}

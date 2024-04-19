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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_REST_OPERATION_PARAM")
public class OntologyRestOperationParam extends AuditableEntityWithUUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ParamOperationType {
		PATH, QUERY
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "OPERATION_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private OntologyRestOperation operationId;

	@Column(name = "INDEX_PARAM")
	@Getter
	@Setter
	private Integer indexParam;

	@Column(name = "NAME", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String name;

	@Column(name = "FIELD", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String field;

	@Column(name = "TYPE", nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private ParamOperationType type;

}

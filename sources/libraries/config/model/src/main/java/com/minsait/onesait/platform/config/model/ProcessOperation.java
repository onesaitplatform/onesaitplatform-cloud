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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configurable
@Entity
@Table(name = "PROCESS_OPERATION")
@EntityListeners(AuditEntityListener.class)
@ToString
public class ProcessOperation extends AuditableEntityWithUUID implements Comparable<ProcessOperation> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		INSERT, UPDATE, DELETE, QUERY;
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "PROCESS_TRACE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private ProcessTrace processTraceId;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private Ontology ontologyId;

	@Column(name = "POSITION")
	@Getter
	@Setter
	private Integer position;

	@Column(name = "NUM_EXECUTIONS")
	@Getter
	@Setter
	private Integer numExecutions;

	@Column(name = "SOURCES", length = 125)
	@Getter
	@Setter
	private String sources;

	@Column(name = "FIELD_ID", length = 125)
	@Getter
	@Setter
	private String fieldId;

	@Column(name = "FIELD_VALUE", length = 125)
	@Getter
	@Setter
	private String fieldValue;

	@Column(name = "process_type")
	@Getter
	@Setter
	private Type type;

	@Override
	public int compareTo(ProcessOperation op) {
		if (op == null)
			return -1;

		return this.position.compareTo(op.getPosition());
	}

}

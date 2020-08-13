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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
@Table(name = "QUERY_TEMPLATE")
public class QueryTemplate extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum QueryType {
		SQL, NATIVE;
	}

	@Getter
	@Setter
	@NotNull
	@Column(name = "NAME")
	private String name;

	@Getter
	@Setter
	@Column(name = "DESCRIPTION")
	private String description;

	@Getter
	@Setter
	@Lob
	@Column(name = "QUERY_SELECTOR")
	@NotNull
	private String querySelector;

	@Getter
	@Setter
	@Lob
	@Column(name = "QUERY_GENERATOR")
	@NotNull
	private String queryGenerator;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Ontology ontology;

	@Getter
	@Setter
	@Column(name = "TYPE")
	@NotNull
	private QueryType type;

}

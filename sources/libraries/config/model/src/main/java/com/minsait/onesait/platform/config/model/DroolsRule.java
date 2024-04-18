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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DROOLS_RULE", uniqueConstraints = { @UniqueConstraint(columnNames = { "IDENTIFICATION" }),
		@UniqueConstraint(columnNames = { "USER_ID", "SOURCE_ONTOLOGY_ID" }) })
@Configurable
@Getter
@Setter
public class DroolsRule extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ONTOLOGY, REST
	}

	@Lob
	@NotNull
	@Column(name = "DRL", nullable = false)
	private String DRL;

	@Column(name = "TYPE", nullable = true)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private Type type;

	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinColumn(name = "TARGET_ONTOLOGY_ID", referencedColumnName = "ID")
	private Ontology targetOntology;

	@ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
	@JoinColumn(name = "SOURCE_ONTOLOGY_ID", referencedColumnName = "ID")
	private Ontology sourceOntology;

	@Column(name = "ACTIVE", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	private boolean active;
}

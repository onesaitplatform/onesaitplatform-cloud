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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_ELASTIC")
public class OntologyElastic extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;
	

	public enum PatternFunctionType {
		NONE, SUBSTR, YEAR, YEAR_MONTH, YEAR_MONTH_DAY, MONTH, DAY;
	}

	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private Ontology ontologyId;

	@Column(name = "SHARDS", columnDefinition = "integer default 5")
	@Getter
	@Setter
	private Integer shards;
	

	@Column(name = "REPLICAS", columnDefinition = "integer default 0")
	@Getter
	@Setter
	private Integer replicas;
	
	@Column(name = "CUSTOM_CONFIG")
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	private Boolean customConfig;
	
	@Column(name = "TEMPLATE_CONFIG")
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	private Boolean templateConfig;
	
	@Column(name = "PATTERN_FIELD", length = 256)
	@Getter
	@Setter
	private String patternField;
	
	@Column(name = "PATERN_FUNCTION", length = 255)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private PatternFunctionType patternFunction = PatternFunctionType.NONE;
	
	@Column(name = "SUBSTRING_START", columnDefinition = "integer default 0")
	@Getter
	@Setter
	private Integer substringStart;
	

	@Column(name = "SUBSTRING_END", columnDefinition = "integer default -1")
	@Getter
	@Setter
	private Integer substringEnd;
	
	@Column(name = "CUSTOM_ID")
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@Getter
	@Setter
	private Boolean customIdConfig;
	
	@Column(name = "CUSTOM_ID_FIELD", length = 256)
	@Getter
	@Setter
	private String idField;
	
	@Column(name = "ALLOW_UPSERT_BY_ID")
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@Getter
	@Setter
	private Boolean allowsUpsertById;
}

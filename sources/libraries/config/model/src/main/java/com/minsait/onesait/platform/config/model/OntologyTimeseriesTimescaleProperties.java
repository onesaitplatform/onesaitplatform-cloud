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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.RetentionUnit;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_TIMESERIES_TIMESCALE_PROPERTIES")
public class OntologyTimeseriesTimescaleProperties extends AuditableEntityWithUUID {
	private static final long serialVersionUID = 1L;


	@OneToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_TIMESERIES_ID", referencedColumnName = "ID")
	@JsonIgnore
	@Getter
	@Setter
	private OntologyTimeSeries ontologyTimeSeries;

	@Column(name = "CHUNK_INTERVAL")
	@Getter
	@Setter
	@NotNull
	private Integer chunkInterval;

	@Column(name = "CHUNK_INTERVAL_UNIT", length = 20)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit chunkIntervalUnit;

	@Column(name = "FRECUENCY")
	@Getter
	@Setter
	@NotNull
	private Integer frecuency;

	@Column(name = "FRECUENCY_UNIT", length = 20)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit frecuencyUnit;

	@Column(name = "HYPERTABLE_QUERY")
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String hypertableQuery;

	@Column(name = "COMPRESSION_ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean compressionActive;

	@Column(name = "COMPRESSION_AFTER")
	@Getter
	@Setter
	private Integer compressionAfter;

	@Column(name = "COMPRESSION_UNIT", length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private RetentionUnit compressionUnit;

	@Column(name = "COMPRESSION_QUERY")
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String compressionQuery;


	@Column(name = "RETENTION_BEFORE")
	@Getter
	@Setter
	private Integer retentionBefore;

	@Column(name = "RETENTION_UNIT", length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private RetentionUnit retentionUnit;

	@Column(name = "RETENTION_ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean retentionActive;

}

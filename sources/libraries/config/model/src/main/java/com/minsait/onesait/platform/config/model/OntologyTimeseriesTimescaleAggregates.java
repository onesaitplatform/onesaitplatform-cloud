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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_TIMESERIES_TIMESCALE_AGGREGATES", uniqueConstraints = @UniqueConstraint(columnNames = {
"IDENTIFICATION" }))
public class OntologyTimeseriesTimescaleAggregates extends AuditableEntityWithUUID {
	private static final long serialVersionUID = 1L;

	@OneToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_TIMESERIES_ID", referencedColumnName = "ID", nullable = false)
	@JsonIgnore
	@Getter
	@Setter
	private OntologyTimeSeries ontologyTimeSeries;

	@Column(name = "NAME", length = 100, nullable = false)
	@Getter
	@Setter
	private String name;

	@Column(name = "IDENTIFICATION", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "BUCKET_FREQUENCY")
	@Getter
	@Setter
	private Integer bucketFrequency;

	@Column(name = "BUCKET_FREQUENCY_UNIT", length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit bucketFrequencyUnit;

	@Column(name = "SCHEDULER_FREQUENCY")
	@Getter
	@Setter
	private Integer schedulerFrequency;

	@Column(name = "SCHEDULER_FREQUENCY_UNIT", length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit schedulerFrequencyUnit;

	@Column(name = "START_OFFSET")
	@Getter
	@Setter
	private Integer startOffset;

	@Column(name = "START_OFFSET_UNIT", length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit startOffsetUnit;

	@Column(name = "END_OFFSET")
	@Getter
	@Setter
	private Integer endOffset;

	@Column(name = "END_OFFSET_UNIT", length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit endOffsetUnit;

	@Column(name = "AGGREGATE_QUERY", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String aggregateQuery;
}

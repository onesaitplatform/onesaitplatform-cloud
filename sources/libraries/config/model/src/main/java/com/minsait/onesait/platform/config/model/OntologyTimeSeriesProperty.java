/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_TIMESERIES_PROPERTY")
public class OntologyTimeSeriesProperty extends AuditableEntityWithUUID {

	public enum PropertyType {
		TAG, SERIE_FIELD, FIELD_OPTIONAL
	}

	public enum PropertyDataType {
		STRING, INTEGER, NUMBER, OBJECT, ARRAY, TIMESTAMP, BOOLEAN
	}

	public enum AggregationFunction {
		NONE, MAX, MIN, FIRST, LAST, SUM, PUSH
	}
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_TIMESERIES_ID", referencedColumnName = "ID", nullable = false)
	@JsonIgnore
	@Getter
	@Setter
	private OntologyTimeSeries ontologyTimeSeries;

	@Column(name = "PROPERTY_TYPE", length = 20)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private PropertyType propertyType;

	@Column(name = "PROPERTY_NAME", length = 200)
	@Getter
	@Setter
	@NotNull
	private String propertyName;

	@Column(name = "PROPERTY_DATA_TYPE", length = 20)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private PropertyDataType propertyDataType;

	@Column(name = "PROPERTY_AGGREGATION_TYPE", nullable = true, length = 20)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private AggregationFunction propertyAggregationType;

	@Column(name = "PROPERTY_PUSH_SIGNAL", nullable = true, length = 200)
	@Getter
	@Setter
	private String propertyPushSignal;
}

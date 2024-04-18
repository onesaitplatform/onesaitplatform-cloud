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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_TIMESERIES")
public class OntologyTimeSeries extends AuditableEntityWithUUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@JsonBackReference
	@Getter
	@Setter
	private Ontology ontology;

	@OneToMany(mappedBy = "ontologyTimeSeries", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<OntologyTimeSeriesWindow> timeSeriesWindows = new HashSet<>();

	@OneToMany(mappedBy = "ontologyTimeSeries", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<OntologyTimeSeriesProperty> timeSeriesProperties = new HashSet<>();

}

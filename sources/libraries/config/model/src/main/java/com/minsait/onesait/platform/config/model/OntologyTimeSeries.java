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

import java.util.HashSet;
import java.util.Map;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_TIMESERIES")
public class OntologyTimeSeries extends AuditableEntityWithUUID implements Versionable<OntologyTimeSeries> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@OneToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
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

	@OneToOne(mappedBy = "ontologyTimeSeries", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private OntologyTimeseriesTimescaleProperties timeSeriesTimescaleProperties;

	@OneToMany(mappedBy = "ontologyTimeSeries", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<OntologyTimeseriesTimescaleAggregates> timeSeriesTimescaleAgregates = new HashSet<>();

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (StringUtils.hasText(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			o.setOntologyTimeSeries(this);
			ontology = o;
		}
	}

	@JsonSetter("timeSeriesWindows")
	public void setTimeSeriesWindowsJson(Set<OntologyTimeSeriesWindow> timeSeriesWindows) {
		timeSeriesWindows.forEach(t -> {
			t.setOntologyTimeSeries(this);
			this.timeSeriesWindows.add(t);
		});
	}

	@JsonSetter("timeSeriesProperties")
	public void setTimeSeriePropertiesJson(Set<OntologyTimeSeriesProperty> timeSeriesProperties) {
		timeSeriesProperties.forEach(t -> {
			t.setOntologyTimeSeries(this);
			this.timeSeriesProperties.add(t);
		});
	}

	@JsonSetter("timeSeriesTimescaleProperties")
	public void setTimeSeriesTimescalePropertiesJson(
			OntologyTimeseriesTimescaleProperties timeSeriesTimescaleProperties) {
		timeSeriesTimescaleProperties.setOntologyTimeSeries(this);
		this.timeSeriesTimescaleProperties = timeSeriesTimescaleProperties;
	}

	@JsonSetter("timeSeriesTimescaleAgregates")
	public void setTimeSeriesTimescaleAgregatesJson(
			Set<OntologyTimeseriesTimescaleAggregates> timeSeriesTimescaleAgregates) {
		timeSeriesTimescaleAgregates.forEach(t -> {
			t.setOntologyTimeSeries(this);
			this.timeSeriesTimescaleAgregates.add(t);
		});
	}

	@JsonGetter("ontology")
	public String getOntologyJSON() {
		return ontology == null ? null : ontology.getId();
	}

	@Override
	public String fileName() {
		return ontology.getIdentification() + ".yaml";
	}

	@Override
	@JsonIgnore
	public String getUserJson() {
		if (ontology != null) {
			return ontology.getUserJson();
		} else {
			return null;
		}
	}

	@Override
	public Versionable<OntologyTimeSeries> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<OntologyTimeSeries> o = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (o != null && ontology != null && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
				&& excludedIds.get(Ontology.class.getSimpleName()).contains(ontology.getId())) {
			addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
			o = null;
		}
		return o;
	}

}

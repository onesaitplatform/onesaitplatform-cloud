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
package com.minsait.onesait.platform.config.services.ontology.dto;

import java.util.HashSet;
import java.util.Set;

import org.apache.kafka.connect.data.Timestamp;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.User;

import lombok.Getter;
import lombok.Setter;

public class OntologyTimeSeriesDTO {

	private static final long serialVersionUID = 1L;

	// Timeseries fields

	@Getter
	@Setter
	private boolean stats;

	@Getter
	@Setter
	private boolean lastVal;

	@Getter
	@Setter
	private Timestamp timestamp;

	@Getter
	@Setter
	private Set<OntologyTimeSeriesWindow> timeSeriesWindow;

	@Getter
	@Setter
	private Set<OntologyTimeSeriesProperty> timeSeriesProperties;

	@Getter
	@Setter
	private OntologyKPI ontologyKPI;

	@Getter
	@Setter
	private String[] tags;

	@Getter
	@Setter
	private String[] fieldnames;

	@Getter
	@Setter
	private String[] fieldtypes;

	@Getter
	@Setter
	private String[] windowtypes;

	@Getter
	@Setter
	private String[] freqtypes;

	@Getter
	@Setter
	private String[] aggtypes;

	@Getter
	@Setter
	private String[] deletepolicies;

	// Timeseries fields

	// ontology fields

	@Getter
	@Setter
	private String jsonSchema;

	@Getter
	@Setter
	private boolean rtdbToHdb;

	@Getter
	@Setter
	private boolean allowsCreateTopic;

	@Getter
	@Setter
	private User user;

	@Getter
	@Setter
	private DataModel dataModel;

	@Getter
	@Setter
	private String rtdbDatasource;

	@Getter
	@Setter
	private String rtdbCleanLapse;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String metainf;

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private boolean active;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private boolean isNewOntology;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private boolean allowsCypherFields;

	@Getter
	@Setter
	private boolean rtdbClean;

	// ontology fields

	public void setTimeSeriesProperties() {
		this.timeSeriesProperties = new HashSet<>();
		String name;
		String datatype;
		OntologyTimeSeriesProperty otsproperty;
		for (int i = 0; i < this.tags.length; i++) {
			name = this.tags[i].split("-")[0].split(":")[1].trim();
			datatype = this.tags[i].split("-")[1].split(":")[1].trim();
			otsproperty = new OntologyTimeSeriesProperty();
			otsproperty.setPropertyType(PropertyType.TAG);
			otsproperty.setPropertyDataType(OntologyTimeSeriesProperty.PropertyDataType.valueOf(datatype));
			otsproperty.setPropertyName(name);
			this.timeSeriesProperties.add(otsproperty);
		}
		for (int i = 0; i < this.fieldnames.length; i++) {
			name = this.fieldnames[i];
			datatype = this.fieldtypes[i];
			otsproperty = new OntologyTimeSeriesProperty();
			otsproperty.setPropertyType(PropertyType.SERIE_FIELD);
			otsproperty.setPropertyDataType(OntologyTimeSeriesProperty.PropertyDataType.valueOf(datatype));
			otsproperty.setPropertyName(name);
			this.timeSeriesProperties.add(otsproperty);
		}
	}

	public void setTimeSeriesWindow() {
		this.timeSeriesWindow = new HashSet<>();
		String window;
		String frequnit;
		Integer freqnum;
		String aggfunc;
		Integer retnum;
		String retunit;
		OntologyTimeSeriesWindow otswindow;
		for (int i = 0; i < this.windowtypes.length; i++) {
			otswindow = new OntologyTimeSeriesWindow();
			window = this.windowtypes[i];
			freqnum = Integer.parseInt(this.freqtypes[i].split(" ")[0]);
			frequnit = this.freqtypes[i].split(" ")[1];
			aggfunc = this.aggtypes[i];
			if (this.deletepolicies[i].equals("No")) {
				otswindow.setBdh(false);
			} else {
				otswindow.setBdh(true);
				retnum = Integer.parseInt(this.deletepolicies[i].split(" ")[0]);
				retunit = this.deletepolicies[i].split(" ")[1];
				otswindow.setRetentionBefore(retnum);
				otswindow.setRetentionUnit(OntologyTimeSeriesWindow.RetentionUnit.valueOf(retunit));

			}

			otswindow.setWindowType(OntologyTimeSeriesWindow.WindowType.valueOf(window));
			otswindow.setFrecuency(freqnum);
			otswindow.setFrecuencyUnit(OntologyTimeSeriesWindow.FrecuencyUnit.valueOf(frequnit));
			otswindow.setAggregationFunction(OntologyTimeSeriesWindow.AggregationFunction.valueOf(aggfunc));

			this.timeSeriesWindow.add(otswindow);
		}
	}

}

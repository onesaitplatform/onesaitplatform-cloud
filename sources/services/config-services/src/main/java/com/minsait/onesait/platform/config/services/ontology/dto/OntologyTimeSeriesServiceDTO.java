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
package com.minsait.onesait.platform.config.services.ontology.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import org.apache.kafka.connect.data.Timestamp;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.RetentionUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleProperties;
import com.minsait.onesait.platform.config.model.User;

import lombok.Getter;
import lombok.Setter;

public class OntologyTimeSeriesServiceDTO {

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
	private String[] fieldAggregations;

	@Getter
	@Setter
	private String[] fieldPushSignals;

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
	private boolean allowsCreateNotificationTopic;

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

	@Getter
	@Setter
	private boolean contextDataEnabled;

	// ontology fields

	// TimescaleDB
	@Getter
	@Setter
	private OntologyTimeseriesTimescaleProperties timeSeriesTimescaleProperties;
	

	@Getter
	@Setter
	private Set<OntologyTimeseriesTimescaleAggregates> timeSeriesTimescaleAggregates;
	
	@Getter
	@Setter
	private String[] buckettypes;

	@Getter
	@Setter
	private String hypertableQuery;

	@Getter
	@Setter
	private boolean compressionActive;

	@Getter
	@Setter
	private String compressionConfig;

	@Getter
	@Setter
	private String compressionQuery;
	
    @Getter
    @Setter
    private boolean supportsJsonLd;
    
    @Getter
    @Setter
    private String jsonLdContext;

	public void setTimeSeriesProperties() {
		this.timeSeriesProperties = new HashSet<>();
		String name;
		String datatype;
		String aggregationFunction;
		String pushSignal;
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
			if (this.rtdbDatasource.equals(RtdbDatasource.MONGO.toString())) {
				aggregationFunction = "NONE";
				pushSignal = null;
			} else {
				aggregationFunction = this.fieldAggregations[i];
				pushSignal = this.fieldPushSignals[i].trim().isEmpty() ? null : this.fieldPushSignals[i];
			}
			otsproperty = new OntologyTimeSeriesProperty();
			otsproperty.setPropertyType(PropertyType.SERIE_FIELD);
			otsproperty.setPropertyDataType(OntologyTimeSeriesProperty.PropertyDataType.valueOf(datatype));
			otsproperty.setPropertyName(name);
			if (aggregationFunction.isEmpty()) {
				aggregationFunction = "NONE";
			}
			otsproperty.setPropertyAggregationType(
					OntologyTimeSeriesProperty.AggregationFunction.valueOf(aggregationFunction));
			otsproperty.setPropertyPushSignal(pushSignal);
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

	public OntologyTimeseriesTimescaleProperties getTimescaleProperties() {
		final OntologyTimeseriesTimescaleProperties ontologyTimescaleProperties = new OntologyTimeseriesTimescaleProperties();
		// Chunks
		ontologyTimescaleProperties.setChunkInterval(Integer.valueOf(this.buckettypes[0].trim().split(" ")[0]));
		ontologyTimescaleProperties
				.setChunkIntervalUnit(FrecuencyUnit.valueOf(this.buckettypes[0].trim().split(" ")[1].toUpperCase()));
		// Frequency
		if (this.freqtypes[0].equalsIgnoreCase("NONE")) {
			ontologyTimescaleProperties.setFrecuency(0);
			ontologyTimescaleProperties.setFrecuencyUnit(OntologyTimeSeriesWindow.FrecuencyUnit.valueOf("NONE"));
		} else {
			int freqnum = Integer.parseInt(this.freqtypes[0].trim().split(" ")[0]);
			String frequnit = this.freqtypes[0].trim().split(" ")[1];
			ontologyTimescaleProperties.setFrecuency(freqnum);
			ontologyTimescaleProperties.setFrecuencyUnit(OntologyTimeSeriesWindow.FrecuencyUnit.valueOf(frequnit));
		}
		// Query
		ontologyTimescaleProperties.setHypertableQuery(hypertableQuery);
		// Compression
		ontologyTimescaleProperties.setCompressionActive(compressionActive);
		int compressionAfter = 0;
		String compressionUnit = "days";
		if (compressionActive) {
			compressionAfter = Integer.parseInt(this.compressionConfig.trim().split(" ")[0]);
			compressionUnit = this.compressionConfig.trim().split(" ")[1];
			if (!compressionUnit.endsWith("s")) {
				compressionUnit = compressionUnit + "s";
			}
		}
		ontologyTimescaleProperties.setCompressionAfter(compressionAfter);
		ontologyTimescaleProperties.setCompressionUnit(RetentionUnit.valueOf(compressionUnit.toUpperCase()));
		ontologyTimescaleProperties.setCompressionQuery(compressionQuery);
		// Deletion
		ontologyTimescaleProperties.setRetentionActive(rtdbClean);
		int retentionBefore = 0;
		String retentionUnit = "days";
		if (rtdbClean) {
			retentionBefore = Integer.parseInt(this.rtdbCleanLapse.trim().split(" ")[0]);
			retentionUnit = this.rtdbCleanLapse.trim().split(" ")[1];
			ontologyTimescaleProperties.setRetentionBefore(retentionBefore);
			if (!retentionUnit.endsWith("s")) {
				retentionUnit = retentionUnit + "s";
			}
		}
		ontologyTimescaleProperties.setRetentionUnit(RetentionUnit.valueOf(retentionUnit.toUpperCase()));
		return ontologyTimescaleProperties;
	}
	
}

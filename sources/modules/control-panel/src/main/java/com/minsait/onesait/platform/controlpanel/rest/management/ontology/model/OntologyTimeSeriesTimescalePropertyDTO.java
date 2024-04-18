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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.RetentionUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyTimeSeriesTimescalePropertyDTO {

	@Getter
	@Setter
	@NotNull
	private Integer chunkInterval;

	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit chunkIntervalUnit;

	@Getter
	@Setter
	@NotNull
	private Integer frecuency;

	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit frecuencyUnit;

	@NotNull
	@Getter
	@Setter
	private String hypertableQuery;

	@NotNull
	@Getter
	@Setter
	private boolean compressionActive;

	@Column(name = "COMPRESSION_AFTER")
	@Getter
	@Setter
	private Integer compressionAfter;

	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private RetentionUnit compressionUnit;

	@NotNull
	@Getter
	@Setter
	private String compressionQuery;

	@Getter
	@Setter
	private Integer retentionBefore;

	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private RetentionUnit retentionUnit;

	@NotNull
	@Getter
	@Setter
	private boolean retentionActive;

	public OntologyTimeSeriesTimescalePropertyDTO(OntologyTimeseriesTimescaleProperties property) {
		if (property != null) {
			this.chunkInterval = property.getChunkInterval();
			this.chunkIntervalUnit = property.getChunkIntervalUnit();
			this.frecuency = property.getFrecuency();
			this.frecuencyUnit = property.getFrecuencyUnit();
			this.hypertableQuery = property.getHypertableQuery();
			this.compressionActive = property.isCompressionActive();
			this.compressionAfter = property.getCompressionAfter();
			this.compressionUnit = property.getCompressionUnit();
			this.compressionQuery = property.getCompressionQuery();
			this.retentionBefore = property.getRetentionBefore();
			this.retentionUnit = property.getRetentionUnit();
			this.retentionActive = property.isRetentionActive();
		}
	}
}

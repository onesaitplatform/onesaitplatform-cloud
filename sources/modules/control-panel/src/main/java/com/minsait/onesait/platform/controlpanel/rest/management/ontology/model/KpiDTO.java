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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import java.text.SimpleDateFormat;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.OntologyKPI;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO {

	@NotNull
	@Getter
	@Setter
	private String cron;
	
	@Getter
	@Setter
	private String dateFrom;
	
	@Getter
	@Setter
	private String dateTo;
	
	@NotNull
	@Getter
	@Setter
	private String jobName;
	
	@NotNull
	@Getter
	@Setter
	private String postProcess;
	
	@NotNull
	@Getter
	@Setter
	private String query;
	
	public KpiDTO(OntologyKPI ontologyKpi) {
		this.cron = ontologyKpi.getCron();
		if (ontologyKpi.getDateFrom() != null) {
			this.dateFrom = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(ontologyKpi.getDateFrom());
		} else {
			this.dateFrom = null;
		}
		if (ontologyKpi.getDateTo() != null) {
			this.dateTo = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(ontologyKpi.getDateTo());
		} else {
			this.dateTo = null;
		}
		this.jobName = ontologyKpi.getJobName();
		this.postProcess = ontologyKpi.getPostProcess();
		this.query = ontologyKpi.getQuery();
	}
	
}

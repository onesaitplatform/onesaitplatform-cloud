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

import java.util.Date;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

public class OntologyKPIDTO {

	private static final long serialVersionUID = 1L;

	// KPI fields

	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private String cron;

	@Getter
	@Setter
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private Date dateFrom;

	@Getter
	@Setter
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private Date dateTo;

	// KPI fields

	// ontology fields

	@Getter
	@Setter
	private String schema;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String datasource;

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String metainf;

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
	private String jobName;

	@Getter
	@Setter
	private boolean allowsCypherFields;

	@Getter
	@Setter
	private boolean contextDataEnabled;

	@Getter
	@Setter
	private String postProcess;

    @Getter
    @Setter
    private boolean supportsJsonLd;
    
    @Getter
    @Setter
    private String jsonLdContext;
    
	// ontology fields

	@Override
	public String toString() {
		return getName();
	}

}

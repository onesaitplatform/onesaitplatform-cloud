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
package com.minsait.onesait.platform.config.services.opendata.dto;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class OpenDataResourceDTO {
	
	@Getter
	@Setter
	private String id;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String creationType;
	
	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String format;

	@Getter
	@Setter
	private String dataset;
	
	@Getter
	@Setter
	private String jsonData;
	
	@Getter
	@Setter
	private String url;
	
	@Getter
	@Setter
	private String ontology;
	
	@Getter
	@Setter
	private String query;
	
	@Getter
	@Setter
	private String ontologyDescription;
	
	@Getter
	@Setter
	private String ontologySchema;
	
	@Getter
	@Setter
	private boolean datastore;
	
	@Getter
	@Setter
	private String platformResource;
	
	@Getter
	@Setter
	private String dashboardId;
	
	@Getter
	@Setter
	private String viewerId;
	
	@Getter
	@Setter
	private String apiId;
	
	@Getter
	@Setter
	private boolean platformResourcePublic;
	
	@Getter
	@Setter
	private boolean graviteeSwagger;
	
	@Getter
	@Setter
	private Date createdAt;
	
	@Getter
	@Setter
	private Date updatedAt;
	
	@Getter
	@Setter
	private String role;

	@Getter
	@Setter
	private boolean newApi;
	
	@Getter
	@Setter
	private ApiMultipart api;
	
	public OpenDataResourceDTO(String name, String description, String creationType, String format, String dataset) {
		this.name = name;
		this.description = description;
		this.creationType = creationType;
		this.format = format;
		this.dataset = dataset;
	}
}

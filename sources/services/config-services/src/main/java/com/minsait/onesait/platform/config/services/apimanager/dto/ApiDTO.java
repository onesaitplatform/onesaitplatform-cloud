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
package com.minsait.onesait.platform.config.services.apimanager.dto;

import java.io.Serializable;
import java.util.ArrayList;

import com.minsait.onesait.platform.config.model.Api.ApiStates;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ApiDTO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Schema(description= "API Identification")
	@Getter
	@Setter
	private String identification;

	@Schema(description= "GraviteeId")
	@Getter
	@Setter
	private String graviteeId;

	@Schema(description= "API Version Number")
	@Getter
	@Setter
	private Integer version;

	@Schema(description= "API Type")
	@Getter
	@Setter
	private String type;

	@Schema(description= "API Public/Private")
	@Getter
	@Setter
	private Boolean isPublic;

	@Schema(description= "API Category")
	@Getter
	@Setter
	private String category;

	@Schema(description= "API External")
	@Getter
	@Setter
	private Boolean externalApi;

	@Schema(description= "Ontology Identification for OntologyAPI")
	@Getter
	@Setter
	private String ontologyId;

	@Schema(description= "QPS API limit")
	@Getter
	@Setter
	private Integer apiLimit;

	@Schema(description= "Endpoint for API Invocation")
	@Getter
	@Setter
	private String endpoint;

	@Schema(description= "External Endpoint for invoking API")
	@Getter
	@Setter
	private String endpointExt;

	@Schema(description= "API Description")
	@Getter
	@Setter
	private String description;

	@Schema(description= "Tags Meta-inf for API")
	@Getter
	@Setter
	private String metainf;

	@Schema(description= "Image Type")
	@Getter
	@Setter
	private String imageType;

	@Schema(description= "API Status")
	@Getter
	@Setter
	private ApiStates status;

	@Schema(description= "creation Date")
	@Getter
	@Setter
	private String creationDate;

	@Schema(description= "API Propietary")
	@Getter
	@Setter
	private String userId;

	@Schema(description= "API Operations")
	@Getter
	@Setter
	private ArrayList<OperacionDTO> operations;

	@Schema(description= "API Authentication")
	@Getter
	@Setter
	private ArrayList<AutenticacionAtribDTO> authentication;

}

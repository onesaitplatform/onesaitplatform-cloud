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
package com.minsait.onesait.platform.api.rest.api.dto;

import java.io.Serializable;
import java.util.ArrayList;

import com.minsait.onesait.platform.config.model.Api.ApiStates;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class ApiDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "API Identification")
	@Getter
	@Setter
	private String identification;

	@ApiModelProperty(value = "API Version Number")
	@Getter
	@Setter
	private Integer version;

	@ApiModelProperty(value = "API Type")
	@Getter
	@Setter
	private String type;

	@ApiModelProperty(value = "API Public/Private")
	@Getter
	@Setter
	private Boolean isPublic;

	@ApiModelProperty(value = "API Category")
	@Getter
	@Setter
	private String category;

	@ApiModelProperty(value = "API External")
	@Getter
	@Setter
	private Boolean externalApi;

	@ApiModelProperty(value = "Ontology Identification for OntologyAPI")
	@Getter
	@Setter
	private String ontologyId;

	@ApiModelProperty(value = "QPS API limit")
	@Getter
	@Setter
	private Integer apiLimit;

	@ApiModelProperty(value = "Endpoint for API Invocation")
	@Getter
	@Setter
	private String endpoint;

	@ApiModelProperty(value = "External Endpoint for invoking API")
	@Getter
	@Setter
	private String endpointExt;

	@ApiModelProperty(value = "API Description")
	@Getter
	@Setter
	private String description;

	@ApiModelProperty(value = "Tags Meta-inf for API")
	@Getter
	@Setter
	private String metainf;

	@ApiModelProperty(value = "Image Type")
	@Getter
	@Setter
	private String imageType;

	@ApiModelProperty(value = "API Status")
	@Getter
	@Setter
	private ApiStates status;

	@ApiModelProperty(value = "creation Date")
	@Getter
	@Setter
	private String creationDate;

	@ApiModelProperty(value = "API Propietary")
	@Getter
	@Setter
	private String userId;

	@ApiModelProperty(value = "API Operations")
	@Getter
	@Setter
	private ArrayList<OperacionDTO> operations;

	@ApiModelProperty(value = "API Authentication")
	@Getter
	@Setter
	private ArrayList<AutenticacionAtribDTO> authentication;

}

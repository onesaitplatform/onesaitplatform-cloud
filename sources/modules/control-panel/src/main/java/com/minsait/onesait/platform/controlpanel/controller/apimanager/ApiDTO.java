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
package com.minsait.onesait.platform.controlpanel.controller.apimanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.UserApi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class ApiDTO implements Serializable {

	public ApiDTO() {
	}

	public ApiDTO(Api api, List<ApiOperation> apiops, List<UserApi> usersapi) {
		this.id = api.getId();
		this.identification = api.getIdentification();
		this.version = api.getNumversion();
		this.type = api.getApiType().toString();
		this.isPublic = api.isPublic();
		this.category = api.getCategory().toString();
		if (type.contains("EXTERNAL")) {
			this.externalApi = true;
			this.ontologyId = null;
		} else {
			this.externalApi = false;
			if (type.equals(Api.ApiType.NODE_RED.toString()) && api.getOntology() == null) {
				this.ontologyId = null;
			} else {
				this.ontologyId = api.getOntology().getId();
			}
		}
		this.apiLimit = api.getApilimit();
		this.endpoint = api.getEndpoint();
		this.endpointExt = api.getEndpointExt();
		this.description = api.getDescription();
		this.metainf = api.getMetaInf();
		this.imageType = api.getImageType();
		this.status = api.getState().toString();
		this.creationDate = api.getCreatedAt().toString();
		this.userId = api.getUser().getUserId();
		this.swaggerJson = api.getSwaggerJson();
		this.authentications = new ArrayList<>();
		this.operations = new ArrayList<>();
		for (UserApi apiauth : usersapi) {
			UserApiDTO userapiDTO = new UserApiDTO(apiauth);
			this.authentications.add(userapiDTO);
		}
		for (ApiOperation apiop : apiops) {
			ApiOperationDTO apiopDTO = new ApiOperationDTO(apiop);
			this.operations.add(apiopDTO);
		}

	}

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "API Id")
	@Getter
	@Setter
	private String id;

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
	private String status;

	@ApiModelProperty(value = "creation Date")
	@Getter
	@Setter
	private String creationDate;

	@ApiModelProperty(value = "API Propietary")
	@Getter
	@Setter
	private String userId;

	@ApiModelProperty(value = "API Swagger Json")
	@Getter
	@Setter
	private String swaggerJson;

	@ApiModelProperty(value = "API Operations")
	@Getter
	@Setter
	private ArrayList<ApiOperationDTO> operations;

	@ApiModelProperty(value = "API Authentication")
	@Getter
	@Setter
	private ArrayList<UserApiDTO> authentications;

}

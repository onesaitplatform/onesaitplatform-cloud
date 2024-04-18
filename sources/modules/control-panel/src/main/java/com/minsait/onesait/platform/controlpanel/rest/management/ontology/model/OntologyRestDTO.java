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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRest.SecurityType;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyRestDTO extends OntologyDTO {

	@NotNull
	@Getter
	@Setter
	private String baseUrl;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private SecurityType securityType;
	
	@NotNull
	@Getter
	@Setter
	private String swaggerUrl;
	
	@NotNull
	@Getter
	@Setter
	private List<OntologyRestHeaderDTO> headers;
	
	@NotNull
	@Getter
	@Setter
	private OntologyRestSecurityDTO security;
	
	@NotNull
	@Getter
	@Setter
	private List<OntologyRestOperationDTO> operations;
	
	public OntologyRestDTO(OntologyRest ontologyRest) {
		super(ontologyRest.getOntologyId());
		this.baseUrl = ontologyRest.getBaseUrl();
		this.securityType = ontologyRest.getSecurityType();
		this.swaggerUrl = ontologyRest.getSwaggerUrl();
		this.headers = restHeadersToRestHeaderDTO(ontologyRest.getHeaderId());
		this.security = new OntologyRestSecurityDTO(ontologyRest.getSecurityId());
		this.operations = operationsToOperationsDTO(ontologyRest.getOperations());
	}
	
	public List<OntologyRestHeaderDTO> restHeadersToRestHeaderDTO(OntologyRestHeaders headers) {
		List<OntologyRestHeaderDTO> headersDTO = new ArrayList<>();
		JsonArray jsonArray = new JsonParser().parse(headers.getConfig()).getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			headersDTO.add(new OntologyRestHeaderDTO(jsonArray.get(i).getAsJsonObject().toString()));
		}
		return headersDTO;
	}
	public List<OntologyRestOperationDTO> operationsToOperationsDTO(Set<OntologyRestOperation> operations) {
		List<OntologyRestOperationDTO> operationsDTO = new ArrayList<>();
		for (OntologyRestOperation operation: operations) {
			operationsDTO.add(new OntologyRestOperationDTO(operation));
		}
		return operationsDTO;
	}
	
	public JsonArray headersToJsonArray() {
		JsonArray jArray = new JsonArray();
		for (OntologyRestHeaderDTO header: headers) {
			jArray.add(header.toJson());
		}
		return jArray;
		
	}
	
	public JsonArray operationsToJsonArray() {
		JsonArray jArray = new JsonArray();
		for (OntologyRestOperationDTO operation: operations) {
			jArray.add(operation.toJson());
		}
		return jArray;
		
	}
	
}

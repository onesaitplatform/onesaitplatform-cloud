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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestOperation.DefaultOperationType;
import com.minsait.onesait.platform.config.model.OntologyRestOperation.OperationType;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam.ParamOperationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyRestOperationDTO {

	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private DefaultOperationType defaultOperationType;
	
	@Getter
	@Setter
	private String description = "";
	
	@NotNull
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String origin = "Manually";
	
	@NotNull
	@Getter
	@Setter
	private String path;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private OperationType type;
	
	@NotNull
	@Getter
	@Setter
	private List<OntologyRestOperationParamDTO> params = new ArrayList<>();
	
	public OntologyRestOperationDTO(OntologyRestOperation operation) {
		this.defaultOperationType = operation.getDefaultOperationType();
		this.name = operation.getName();
		this.origin = operation.getOrigin();
		this.path = operation.getPath();
		this.type = operation.getType();
		this.params = parametersToParametersDTO(operation.getParameters());
	}
	
	public List<OntologyRestOperationParamDTO> parametersToParametersDTO(Set<OntologyRestOperationParam> params) {
		List<OntologyRestOperationParamDTO> paramsDTO = new ArrayList<>();
		for (OntologyRestOperationParam param: params) {
			paramsDTO.add(new OntologyRestOperationParamDTO(param));
		}
		return paramsDTO;
	}
	
	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", this.name);
		jsonObject.addProperty("path", this.path);
		jsonObject.addProperty("type", this.type.name());
		jsonObject.addProperty("defaultOperationType", this.defaultOperationType.name());
		jsonObject.addProperty("description", this.description);
		jsonObject.addProperty("origin", this.origin);
		jsonObject.add("pathParams", paramsToJsonArray(ParamOperationType.PATH));
		jsonObject.add("queryParams", paramsToJsonArray(ParamOperationType.QUERY));
		return jsonObject;
	}
	
	public JsonArray paramsToJsonArray(ParamOperationType paramType) {
		JsonArray paramsOfType = new JsonArray();
		for (OntologyRestOperationParamDTO param: params) {
			if (param.getType().name().equals(paramType.name())) {
				paramsOfType.add(param.toJson());
			}
		}
		return paramsOfType;
	}
	
}

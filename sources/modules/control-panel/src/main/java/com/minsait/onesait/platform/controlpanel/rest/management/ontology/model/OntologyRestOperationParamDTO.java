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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam.ParamOperationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyRestOperationParamDTO {

	@NotNull
	@Getter
	@Setter
	private String field;
	
	@NotNull
	@Getter
	@Setter
	private Integer indexParam;
	
	@NotNull
	@Getter
	@Setter
	private String name;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private ParamOperationType type;
	
	public OntologyRestOperationParamDTO(OntologyRestOperationParam param) {
		this.field = param.getField();
		this.indexParam = param.getIndexParam();
		this.name = param.getName();
		this.type = param.getType();
	}
	
	public JsonObject toJson () {
		JsonObject jsonObj = new JsonObject();
		if (this.type.name().equals(ParamOperationType.PATH.name())) {
			jsonObj.addProperty("indexes", this.indexParam);
			jsonObj.addProperty("namesPaths", this.name);
			jsonObj.addProperty("fieldsPaths", this.field);
		}
		else if (this.type.name().equals(ParamOperationType.QUERY.name())) {
			jsonObj.addProperty("namesQueries", this.name);
			jsonObj.addProperty("fieldsQueries", this.field);
		}
		return jsonObj;
	}
	
}

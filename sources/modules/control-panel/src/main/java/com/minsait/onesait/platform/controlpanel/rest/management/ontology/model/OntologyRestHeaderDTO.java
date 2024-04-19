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

import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyRestHeaderDTO {

	@NotNull
	@Getter
	@Setter
	private String key;
	
	@NotNull
	@Getter
	@Setter
	private String value;

	
	public OntologyRestHeaderDTO(OntologyRestHeaders headers) {
		// headers.config[index] = {"key":"<key>", "value":"<value>"}
		this(headers.getConfig());
	}
	
	public OntologyRestHeaderDTO(String stringJson) {
		// {"key":"<key>", "value":"<value>"}
		JsonObject jsonObject = new JsonParser().parse(stringJson).getAsJsonObject();
		this.key = jsonObject.get("key").getAsString();
		this.value = jsonObject.get("value").getAsString();
	}
	
	
	public JsonObject toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("key", this.key);
		jsonObject.addProperty("value", this.value);
		return jsonObject;
	}


}

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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.config.model.OntologyRestSecurity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyRestSecurityDTO {

	@Getter
	@Setter
	private String header = "";
	
	@Getter
	@Setter
	private String token = "";
	
	@Getter
	@Setter
	private String user = "";
	
	@Getter
	@Setter
	private String password = "";
	
	
	public OntologyRestSecurityDTO(OntologyRestSecurity security) {
		this(security.getConfig());
	}
	
	public OntologyRestSecurityDTO(String stringJson) {
		// {"password":"<password>","user":"<user>","header":"<header>","token":"<token>"}
		JsonObject jsonObject = new JsonParser().parse(stringJson).getAsJsonObject();
		if (jsonObject.has("header")) {
			this.header = jsonObject.get("header").getAsString();
		}
		if (jsonObject.has("token")) {
			this.token = jsonObject.get("token").getAsString();
		}
		if (jsonObject.has("user")) {
			this.user = jsonObject.get("user").getAsString();
		}
		if (jsonObject.has("password")) {
			this.password = jsonObject.get("password").getAsString();
		}
	}
	
	
	public String toJsonString() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("header", this.header);
		jsonObject.addProperty("token", this.token);
		jsonObject.addProperty("user", this.user);
		jsonObject.addProperty("password", this.password);
		return jsonObject.toString();
	}


}

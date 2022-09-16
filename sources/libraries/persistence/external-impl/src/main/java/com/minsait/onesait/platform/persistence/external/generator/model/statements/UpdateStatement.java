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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateStatement implements SQLStatement {
	@NotNull
	@Size(min = 1)
	private String ontology;
	@NotNull
	private List<WhereStatement> where;
	@NotNull
	private Map<String, String> values;
	private SQLGenerator sqlGenerator;

	public UpdateStatement() {
	}

	public UpdateStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public String getOntology() {
		return ontology;
	}

	public UpdateStatement setOntology(final String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()){
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public List<WhereStatement> getWhere() {
		return where;
	}

	public UpdateStatement setWhere(final List<WhereStatement> where) {
		if(where != null && !where.isEmpty()){
			this.where = where;
			return this;
		} else {
			throw new IllegalArgumentException("Where in model can't be null or empty");
		}
	}

	public Map<String, String> getValues() {
		return values;
	}

	public UpdateStatement setValues(final Map<String, String> values) {
		if(values != null && !values.isEmpty()){
			this.values = values;
			return this;
		} else {
			throw new IllegalArgumentException("Values in model can't be null or empty");
		}
	}

	public UpdateStatement setValuesForJson(final String json){
		final JsonElement jsonElement = new JsonParser().parse(json);
		final JsonObject jsonObject;
		if(jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() == 1 && jsonElement.getAsJsonArray().get(0).isJsonObject()){
			jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
		} else if (jsonElement.isJsonObject()){
			jsonObject = jsonElement.getAsJsonObject();
		} else {
			throw new IllegalArgumentException("The data passed as set statement is not an json object or the array has more than one element");
		}

		values = getValuesForJsonObject(jsonObject);
		return this;
	}

	private Map<String, String> getValuesForJsonObject(final JsonObject jsonObject){
		final Map<String, String> map = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			map.put(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
		}
		return map;
	}

	private String getValueForJsonElement(JsonElement jsonElement){
		if(jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
			throw new IllegalArgumentException("Nested arrays or object not supported in relational databases");
		} else {
			if(jsonElement.isJsonPrimitive()){
				final JsonPrimitive tempPrimitive = jsonElement.getAsJsonPrimitive();
				if(tempPrimitive.isBoolean()) {
					return tempPrimitive.getAsBoolean() ? "1" : "0";
				} else {
					return tempPrimitive.getAsString();
				}
			} else {
				return null;
			}
		}
	}

	@Override
	public PreparedStatement generate(boolean withParams) {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this, withParams);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}

}

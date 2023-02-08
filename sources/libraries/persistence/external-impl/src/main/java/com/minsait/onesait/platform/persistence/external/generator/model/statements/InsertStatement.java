/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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

import com.google.gson.*;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

public class InsertStatement implements SQLStatement {
	@NotNull
	@Size(min = 1)
	private String ontology;
	private List<String> columns; // Total columns (n)
	private List<Map<String, String>> values; // Columns + values -> size <= n
	private SQLGenerator sqlGenerator;

	public InsertStatement() {
	}

	public InsertStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public String getOntology() {
		return ontology;
	}

	public InsertStatement setOntology(final String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()){
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public List<String> getColumns() {
		return columns;
	}

	public InsertStatement setValuesAndColumnsForInstances(final List<String> instances){
		final JsonParser jsonParser = new JsonParser();
		final List<Map<String, String>> valuesList = new ArrayList<>();

		if(instances != null && !instances.isEmpty()) {
			// It assumes the documents structure is always the same, so it gets the first one to create the columns
			columns = generateSQLColumns(jsonParser.parse(instances.get(0)).getAsJsonObject());

			for (String instance : instances) {
				final JsonObject jsonInstance = jsonParser.parse(instance).getAsJsonObject();
				final LinkedHashMap<String, String> mapValues = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> entry : jsonInstance.entrySet()) {
					mapValues.put(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
				}
				valuesList.add(mapValues);
			}

			values = valuesList;
			return this;
		} else {
			throw new IllegalArgumentException("Instance list can't be null or empty");
		}
	}

	public List<Map<String,String>> getValues() {
		return values;
	}

	private List<String> generateSQLColumns(final JsonObject jsonObject) {
		return jsonObject.entrySet().stream()
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
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

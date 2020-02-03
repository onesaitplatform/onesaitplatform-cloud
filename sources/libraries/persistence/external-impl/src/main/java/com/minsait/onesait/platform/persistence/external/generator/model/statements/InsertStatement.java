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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import com.google.gson.*;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

	public InsertStatement setColumns(final List<String> columns) {
		if(columns != null && !columns.isEmpty()){
			this.columns = columns;
			return this;
		} else {
			throw new IllegalArgumentException("Columns can't be null or empty");
		}
	}

	public InsertStatement setValuesAndColumnsForJson(final List<String> instances){
		final String finalInstances = instances
				.stream()
				.collect(Collectors.joining(",","[","]"));
		return setValuesAndColumnsForJson(finalInstances);
	}

	public InsertStatement setValuesAndColumnsForJson(final String json) {
		if(json != null && !json.isEmpty()){
			final JsonElement jsonElement = new JsonParser().parse(json);
			if(jsonElement.isJsonObject()) {
				columns = generateSQLColumns(jsonElement.getAsJsonObject());
			} else if(jsonElement.isJsonArray()
					&& jsonElement.getAsJsonArray().size() > 0
					&& jsonElement.getAsJsonArray().get(0).isJsonObject()) {
				final JsonArray jsonArray = jsonElement.getAsJsonArray();
				columns = StreamSupport.stream(jsonArray.spliterator(),false)
						.map(JsonElement::getAsJsonObject)
						.map(this::generateSQLColumns)
						.flatMap(List::stream)
						.distinct()
						.collect(Collectors.toList());
			} else {
				throw new IllegalArgumentException("The json is not an object nor an array of objects");
			}
			values = generateSQLValues(jsonElement, columns);
			return this;
		} else {
			throw new IllegalArgumentException("The json string can't be null or empty");
		}
	}

	public List<Map<String,String>> getValues() {
		return values;
	}

	private Map<String, String> setSingleValues(final Map<String, String> values, final Map<String,String> baseMap) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			baseMap.putIfAbsent(entry.getKey(), entry.getValue());
		}
		return baseMap;
	}

	public InsertStatement setValues(final List<Map<String, String>> values) {
		if(columns == null || columns.isEmpty()) {
			throw new IllegalStateException("Set columns first, they are needed to create the values in proper order");
		} else {
			if(values != null && !values.isEmpty()) {
				final Map<String, String> baseMap = new LinkedHashMap<>();
				columns.forEach(column -> baseMap.put(column, null));

				this.values = values.stream()
						.map( value -> this.setSingleValues(value, new LinkedHashMap<>(baseMap)))
						.collect(Collectors.toList());
				return this;
			} else {
				throw new IllegalArgumentException("Values can't be null or empty");
			}
		}
	}

	private List<String> generateSQLColumns(final JsonObject jsonObject) {
		return jsonObject.entrySet().stream()
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	private List<Map<String, String>> generateSQLValues(final JsonElement jsonElement, final List<String> columns){
		final Map<String, String> baseMap = new LinkedHashMap<>();
		columns.forEach(column -> baseMap.put(column, null));

		if(jsonElement.isJsonObject()){
			return Collections.singletonList(getValuesForJsonObject(jsonElement.getAsJsonObject(), baseMap));
		} else if(jsonElement.isJsonArray()){
			final JsonArray array = jsonElement.getAsJsonArray();
			final List<Map<String, String>> valuesList = new ArrayList<>();
			for (final JsonElement element : array) {
				if(element.isJsonObject()) {
					valuesList.add(this.getValuesForJsonObject(element.getAsJsonObject(), new LinkedHashMap<>(baseMap)));
				} else {
					throw new IllegalArgumentException("Nested elements in json array are not objects");
				}
			}
			return valuesList;
		} else {
			throw new IllegalArgumentException("Json element is not an object or array");
		}
	}

	private Map<String, String> getValuesForJsonObject(final JsonObject jsonObject, final Map<String,String> baseMap){
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			baseMap.putIfAbsent(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
		}
		return baseMap;
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
	public String generate() {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
}

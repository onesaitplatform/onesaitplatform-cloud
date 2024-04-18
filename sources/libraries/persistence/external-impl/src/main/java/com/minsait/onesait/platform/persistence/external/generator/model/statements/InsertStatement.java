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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InsertStatement implements SQLStatement {

	private static final JsonParser jsonParser = new JsonParser();
	@NotNull
	@Size(min = 1)
	private String ontology;
	@Setter
	private List<String> columns; // Total columns (n)
	@Setter
	private List<Map<String, String>> values; // Columns + values -> size <= n
	private SQLGenerator sqlGenerator;
	// related ontologies
	@Getter
	private final Map<String, List<String>> relatedColumns = new LinkedHashMap<>();
	@Getter
	private final Map<String, List<Map<String, String>>> relatedValues = new LinkedHashMap<>();
	@Getter
	private final OntologyDataService ontologyDataService;

	public InsertStatement() {
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
	}

	public InsertStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
	}

	public String getOntology() {
		return ontology;
	}

	public InsertStatement setOntology(final String ontology) {
		if (ontology != null && !ontology.trim().isEmpty()) {
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public List<String> getColumns() {
		return columns;
	}

	public InsertStatement setValuesAndColumnsForInstances(final List<String> instances) {

		if (instances != null && !instances.isEmpty()) {
			try {
				final Set<OntologyRelation> relations = ontologyDataService.getOntologyReferences(this.ontology);
				if (relations.isEmpty()) {
					setValuesAndColumnsNoRelated(instances);
				} else {
					setValuesAndColumnsRelated(relations, instances);
				}
				return this;
			} catch (final IOException e) {
				log.error("Error while parsing entity references", e);
				throw new IllegalArgumentException("Error while parsing entity references: " + e.getMessage());
			}

		} else {
			throw new IllegalArgumentException("Instance list can't be null or empty");
		}
	}

	public void setValuesAndColumnsNoRelated(List<String> instances) {
		final List<Map<String, String>> valuesList = new ArrayList<>();
		columns = generateSQLColumns(jsonParser.parse(instances.get(0)).getAsJsonObject());
		// It assumes the documents structure is always the same, so it gets the first
		// one to create the columns
		for (final String instance : instances) {
			final JsonObject jsonInstance = jsonParser.parse(instance).getAsJsonObject();
			final LinkedHashMap<String, String> mapValues = new LinkedHashMap<>();
			for (final Map.Entry<String, JsonElement> entry : jsonInstance.entrySet()) {
				mapValues.put(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
			}
			valuesList.add(mapValues);
		}

		values = valuesList;
	}

	public void setValuesAndColumnsRelated(Set<OntologyRelation> relations, List<String> instances) throws IOException {
		final List<Map<String, String>> valuesList = new ArrayList<>();
		columns = generateSQLColumns(jsonParser.parse(instances.get(0)).getAsJsonObject());
		// It assumes the documents structure is always the same, so it gets the first
		// one to create the columns
		populateValuesList(relations, instances, valuesList, columns);
		values = valuesList;
	}

	private void populateValuesList(Set<OntologyRelation> relations, List<String> instances,
			List<Map<String, String>> valuesList, List<String> cols) throws IOException {
		final List<String> targetEntities = relations.stream().map(OntologyRelation::getDstOntology).toList();
		for (final String instance : instances) {
			final JsonObject jsonInstance = jsonParser.parse(instance).getAsJsonObject();
			final LinkedHashMap<String, String> mapValues = new LinkedHashMap<>();
			for (final Map.Entry<String, JsonElement> entry : jsonInstance.entrySet()) {
				if (targetEntities.contains(entry.getKey())) {
					cols.remove(entry.getKey());
					if (entry.getValue() != null) {
						// meto columnas una vez
						if (!relatedColumns.containsKey(entry.getKey())) {
							final JsonObject object = entry.getValue().isJsonArray()
									? entry.getValue().getAsJsonArray().get(0).getAsJsonObject()
									: entry.getValue().getAsJsonObject();
							relatedColumns.put(entry.getKey(), generateSQLColumns(object));
						}
						if (!relatedValues.containsKey(entry.getKey())) {
							relatedValues.put(entry.getKey(), new ArrayList<>());
						}
						final List<String> objectInstances = new ArrayList<>();
						if (entry.getValue().isJsonArray()) {
							entry.getValue().getAsJsonArray().forEach(e -> objectInstances.add(e.toString()));
						}
						populateValuesList(ontologyDataService.getOntologyReferences(entry.getKey()), objectInstances,
								relatedValues.get(entry.getKey()), relatedColumns.get(entry.getKey()));
					}

				} else {
					mapValues.put(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
				}
			}
			valuesList.add(mapValues);
		}
	}

	public List<Map<String, String>> getValues() {
		return values;
	}

	private List<String> generateSQLColumns(final JsonObject jsonObject) {
		return jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
	}

	private String getValueForJsonElement(JsonElement jsonElement) {
		if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
			throw new IllegalArgumentException("Nested arrays or object not supported in relational databases");
		} else {
			if (jsonElement.isJsonPrimitive()) {
				final JsonPrimitive tempPrimitive = jsonElement.getAsJsonPrimitive();
				if (tempPrimitive.isBoolean()) {
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
		if (sqlGenerator != null) {
			return sqlGenerator.generate(this, withParams);
		} else {
			throw new IllegalStateException(
					"SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
}

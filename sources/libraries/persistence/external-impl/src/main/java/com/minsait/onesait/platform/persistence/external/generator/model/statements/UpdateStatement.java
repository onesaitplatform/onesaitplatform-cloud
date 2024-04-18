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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.model.common.RelatedEntityValues;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateStatement implements SQLStatement {
	@NotNull
	@Size(min = 1)
	private String ontology;
	@NotNull
	private List<WhereStatement> where;
	@NotNull
	private Map<String, String> values;
	private SQLGenerator sqlGenerator;
	// related ontologies
	@Getter
	private final Map<String, RelatedEntityValues> relatedValues = new LinkedHashMap<>();
	@Getter
	private final OntologyDataService ontologyDataService;
	private final OntologyVirtualRepository ontologyVirtualRepository;

	@Getter
	@Setter
	private String uniqueID;

	@Getter
	@Setter
	private String uniqueIDValue;

	public UpdateStatement() {
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
		this.ontologyVirtualRepository = BeanUtil.getBean(OntologyVirtualRepository.class);
	}

	public UpdateStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
		this.ontologyVirtualRepository = BeanUtil.getBean(OntologyVirtualRepository.class);
	}

	public String getOntology() {
		return ontology;
	}

	public UpdateStatement setOntology(final String ontology) {
		if (ontology != null && !ontology.trim().isEmpty()) {
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public List<WhereStatement> getWhere() {
		return where;
	}

	public UpdateStatement setUniqueIDWithValue(final String uniqueIdentifier) {
		if (values != null && !values.isEmpty() && values.get(uniqueIdentifier) != null) {
			this.uniqueIDValue = values.get(uniqueIdentifier);
			this.uniqueID = uniqueIdentifier;
			return setWhere(Collections.singletonList(new WhereStatement(this.uniqueID, "=", this.uniqueIDValue)));
		} else {
			throw new IllegalArgumentException("OID could not be set");
		}
	}

	public UpdateStatement setWhere(final List<WhereStatement> where) {
		if (where != null && !where.isEmpty()) {
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
		if (values != null && !values.isEmpty()) {
			this.values = values;
			return this;
		} else {
			throw new IllegalArgumentException("Values in model can't be null or empty");
		}
	}

	public UpdateStatement setValuesForJson(final String json) {
		// TO-DO aqui si se puede meter array cuando haya relaciones (revisar)
		final JsonElement jsonElement = new JsonParser().parse(json);
		final JsonObject jsonObject;
		if (jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() == 1
				&& jsonElement.getAsJsonArray().get(0).isJsonObject()) {
			jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
		} else if (jsonElement.isJsonObject()) {
			jsonObject = jsonElement.getAsJsonObject();
		} else {
			throw new IllegalArgumentException(
					"The data passed as set statement is not an json object or the array has more than one element");
		}
		Set<OntologyRelation> relations = null;
		try {
			relations = ontologyDataService.getOntologyReferences(this.ontology);
		} catch (final IOException e) {
			log.error("Could not get relations");
		}
		if (relations != null && relations.isEmpty()) {
			values = getValuesForJsonObject(jsonObject);
		} else {
			values = getValuesForJsonObjectWithRelated(relations, jsonObject);
			// collect values to delete
		}

		return this;
	}

	private Map<String, String> getValuesForJsonObjectWithRelated(Set<OntologyRelation> relations,
			final JsonObject jsonObject) {
		final Map<String, String> map = new HashMap<>();
		final List<String> targetEntities = relations.stream().map(OntologyRelation::getDstOntology).toList();
		for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			if (targetEntities.contains(entry.getKey())) {
				try {
					// probar a meter array vacio - delete
					// si es objeto vacio lanzo error?
					if (!relatedValues.containsKey(entry.getKey())) {
						final OntologyRelation relation = relations.stream()
								.filter(or -> or.getDstOntology().equals(entry.getKey())).findAny().orElse(null);
						final String fk = relation.getDstAttribute();
						final String fkValue = jsonObject.get(fk).getAsString();
						relatedValues.put(entry.getKey(), new RelatedEntityValues(fk, fkValue));
					}
					if (entry.getValue().isJsonArray() && entry.getValue().getAsJsonArray().size() > 0) {
						for (final JsonElement i : entry.getValue().getAsJsonArray()) {
							final Map<String, String> childValues = getValuesForJsonObjectWithRelated(
									ontologyDataService.getOntologyReferences(entry.getKey()), i.getAsJsonObject());

							relatedValues.get(entry.getKey()).getColumnsAndValues().add(childValues);
						}
					} else if (!entry.getValue().isJsonArray() && entry.getValue().getAsJsonArray().size() != 0) {
						final Map<String, String> childValues = getValuesForJsonObjectWithRelated(
								ontologyDataService.getOntologyReferences(entry.getKey()),
								entry.getValue().getAsJsonObject());
						relatedValues.get(entry.getKey()).getColumnsAndValues().add(childValues);
					}

				} catch (final IOException e) {
					log.error("Skipping childValues, ignoring related entity data: {}", entry.getKey());
				}

			} else {
				map.put(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
			}
		}
		return map;
	}

	private Map<String, String> getValuesForJsonObject(final JsonObject jsonObject) {
		final Map<String, String> map = new HashMap<>();
		for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			map.put(entry.getKey(), this.getValueForJsonElement(entry.getValue()));
		}
		return map;
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

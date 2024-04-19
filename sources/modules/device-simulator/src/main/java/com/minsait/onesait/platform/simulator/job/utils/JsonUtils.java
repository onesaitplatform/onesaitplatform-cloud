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
package com.minsait.onesait.platform.simulator.job.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

@Component
@Deprecated
public class JsonUtils {
	@Autowired
	private OntologyService ontologyService;
	private String pathData = "datos";
	private static final String PATH_PROPERTIES = "properties";
	private static final String DATE_VAR = "$date";
	private static final String NUMBER_VAR = "number";
	private static final String INTEGER_VAR = "integer";
	private static final String STRING_VAR = "string";
	private static final String OBJECT_VAR = "object";
	private static final String ARRAY_VAR = "array";
	private static final String BOOLEAN_VAR = "boolean";
	private static final String FORMAT_VAR = "format";
	private JsonNode schema;

	public JsonNode generateJson(String ontology, String user) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		JsonNode ontologySchema = mapper
				.readTree(ontologyService.getOntologyByIdentification(ontology, user).getJsonSchema());
		schema = ontologySchema;
		final JsonNode fieldsSchema = mapper.createObjectNode();
		// PATH DATA MAY BE DIFFERENT FROM DATOS
		pathData = refJsonSchema(ontologySchema);
		final String pathToProperties = (!ontologySchema.path(pathData).isMissingNode() ? pathData : PATH_PROPERTIES);

		final Iterator<String> fields = !ontologySchema.path(pathData).isMissingNode()
				? ontologySchema.path(pathToProperties).path(PATH_PROPERTIES).fieldNames()
				: ontologySchema.path(PATH_PROPERTIES).fieldNames();
		ontologySchema = !ontologySchema.path(pathData).isMissingNode()
				? ontologySchema.path(pathData).path(PATH_PROPERTIES)
				: ontologySchema.path(PATH_PROPERTIES);
		while (fields.hasNext()) {
			final String field = fields.next();
			final String currentFieldType = ontologySchema.path(field).get("type").asText();
			if (currentFieldType.equals(STRING_VAR))
				if (!ontologySchema.path(field).path("enum").isMissingNode())
					((ObjectNode) fieldsSchema).put(field, ontologySchema.path(field).get("enum").get(0).asText());
				else {
					if (field.equals(DATE_VAR) || ontologySchema.path(field).get(FORMAT_VAR) != null)
						((ObjectNode) fieldsSchema).put(field, getCurrentDate());
					else
						((ObjectNode) fieldsSchema).put(field, "");
				}
			else if (currentFieldType.equals(NUMBER_VAR) || currentFieldType.equals(INTEGER_VAR))
				((ObjectNode) fieldsSchema).put(field, 0);
			else if (currentFieldType.equals(BOOLEAN_VAR))
				((ObjectNode) fieldsSchema).put(field, true);
			else if (currentFieldType.equals(OBJECT_VAR)) {
				final JsonNode object = createObject(ontologySchema.path(field));
				((ObjectNode) fieldsSchema).set(field, object);
			} else if (currentFieldType.equals(ARRAY_VAR)) {
				final JsonNode object = createArray(ontologySchema.path(field));
				final JsonNode arrayNode = mapper.createArrayNode();
				((ArrayNode) arrayNode).add(object);
				((ObjectNode) fieldsSchema).set(field, arrayNode);
			}

		}

		if (!pathData.equals("")) {
			final String context = schema.get(PATH_PROPERTIES).fields().next().getKey();
			return mapper.createObjectNode().set(context, fieldsSchema);
		} else {
			return fieldsSchema;

		}

	}

	private JsonNode createObject(JsonNode fieldNode) {

		final ObjectMapper mapper = new ObjectMapper();
		JsonNode objectNode = mapper.createObjectNode();

		if (!fieldNode.path(PATH_PROPERTIES).isMissingNode()) {
			fieldNode = fieldNode.path(PATH_PROPERTIES);
			final Iterator<String> fields = fieldNode.fieldNames();

			while (fields.hasNext()) {
				final String field = fields.next();
				final String currentFieldType = fieldNode.path(field).get("type").asText();
				if (currentFieldType.equals(STRING_VAR))
					if (!fieldNode.path(field).path("enum").isMissingNode())
						((ObjectNode) objectNode).put(field, fieldNode.path(field).get("enum").get(0).asText());
					else {
						if (field.equals(DATE_VAR) || fieldNode.path(field).get(FORMAT_VAR) != null)
							((ObjectNode) objectNode).put(field, getCurrentDate());
						else
							((ObjectNode) objectNode).put(field, "");
					}
				else if (currentFieldType.equals(NUMBER_VAR) || currentFieldType.equals(INTEGER_VAR))
					((ObjectNode) objectNode).put(field, 0);
				else if (currentFieldType.equals(BOOLEAN_VAR))
					((ObjectNode) objectNode).put(field, true);
				else if (currentFieldType.equals(OBJECT_VAR)) {
					final JsonNode object = createObject(fieldNode.path(field));
					((ObjectNode) objectNode).set(field, object);
				} else if (currentFieldType.equals(ARRAY_VAR)) {
					final JsonNode object = createArray(fieldNode.path(field));
					if (!object.isArray()) {
						final JsonNode arrayNode = mapper.createArrayNode();
						((ArrayNode) arrayNode).add(object);
						((ObjectNode) objectNode).set(field, arrayNode);
					} else {
						((ObjectNode) objectNode).set(field, object);
					}

				}

			}
		} else {
			final Iterator<String> fields = fieldNode.fieldNames();
			while (fields.hasNext()) {
				final String objectType = fields.next();
				if (objectType.equals("oneOf") || objectType.equals("anyOf")) {
					final String property = fieldNode.get(objectType).get(0).get("$ref").asText().replace("#/", "");
					objectNode = createObject(schema.path(property));
				}
			}

		}

		return objectNode;
	}

	private JsonNode createArray(JsonNode fieldNode) {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode objectNode = mapper.createObjectNode();

		if (!fieldNode.path(PATH_PROPERTIES).isMissingNode()) {
			fieldNode = fieldNode.path(PATH_PROPERTIES);
			final Iterator<String> fields = fieldNode.fieldNames();

			while (fields.hasNext()) {
				final String field = fields.next();
				final String currentFieldType = fieldNode.path(field).get("type").asText();
				if (currentFieldType.equals(STRING_VAR))
					if (!fieldNode.path(field).path("enum").isMissingNode())
						((ObjectNode) objectNode).put(field, fieldNode.path(field).get("enum").get(0).asText());
					else {
						if (field.equals(DATE_VAR) || fieldNode.path(field).get(FORMAT_VAR) != null)
							((ObjectNode) objectNode).put(field, getCurrentDate());
						else
							((ObjectNode) objectNode).put(field, "");
					}
				else if (currentFieldType.equals(NUMBER_VAR) || currentFieldType.equals(INTEGER_VAR))
					((ObjectNode) objectNode).put(field, 0);
				else if (currentFieldType.equals(BOOLEAN_VAR))
					((ObjectNode) objectNode).put(field, true);
				else if (currentFieldType.equals(OBJECT_VAR)) {
					return createObject(fieldNode.path(field));
				}

			}

		} else if (!fieldNode.path("items").isMissingNode()) {

			fieldNode = fieldNode.path("items");
			final int size = fieldNode.size();
			final ArrayNode nodeArray = mapper.createArrayNode();

			for (int i = 0; i < size; i++) {
				String type;

				if (fieldNode.isArray()) {
					type = fieldNode.get(i).get("type").asText();
					for (int j = 0; j < size; j++) {
						final JsonNode nodeAux = mapper.createObjectNode();
						if (type.equals(STRING_VAR)) {

							((ObjectNode) nodeAux).put(String.valueOf(j), "");
							nodeArray.add("");
						} else if (type.equals(NUMBER_VAR) || type.equals(INTEGER_VAR)) {

							((ObjectNode) nodeAux).put(String.valueOf(j), 0);
							nodeArray.add(0);

						} else if (type.equals(OBJECT_VAR)) {
							return createObject(fieldNode);
						} else if (type.equals(BOOLEAN_VAR)) {
							((ObjectNode) nodeAux).put(String.valueOf(j), true);
							nodeArray.add(true);
						}

					}
					if (!nodeArray.isNull())
						return nodeArray;

				} else {
					type = fieldNode.get("type").asText();

				}
				if (type.equals(STRING_VAR)) {

					nodeArray.add("");

				} else if (type.equals(NUMBER_VAR) || type.equals(INTEGER_VAR)) {
					nodeArray.add(0);

				} else if (type.equals(BOOLEAN_VAR)) {
					nodeArray.add(true);
				} else if (type.equals(OBJECT_VAR)) {
					return createObject(fieldNode);
				} else if (type.equals(ARRAY_VAR)) {
					final JsonNode object = createArray(fieldNode.path(i));
					final JsonNode arrayNode = mapper.createArrayNode();
					((ArrayNode) arrayNode).add(object);
					((ObjectNode) objectNode).set(String.valueOf(i), arrayNode);
				}

			}
			if (!nodeArray.isNull())
				return nodeArray;

		}

		return objectNode;
	}

	private String getCurrentDate() {
		final DateFormat dfr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dfr.format(new Date());
	}

	public String refJsonSchema(JsonNode schema) {
		final Iterator<Entry<String, JsonNode>> elements = schema.path(PATH_PROPERTIES).fields();
		String reference = "";
		while (elements.hasNext()) {
			final Entry<String, JsonNode> entry = elements.next();
			if (!entry.getValue().path("$ref").isMissingNode()) {
				final String ref = entry.getValue().path("$ref").asText();
				reference = ref.substring(ref.lastIndexOf("#/")).substring(1);
			}
		}
		return reference.replace("/", "");
	}

}

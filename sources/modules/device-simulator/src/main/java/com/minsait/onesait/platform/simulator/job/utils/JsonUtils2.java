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
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

@Component
public class JsonUtils2 {

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ObjectMapper mapper;
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
	private JsonNode auxNodeArray;
	private JsonNode auxNodeObject;

	public JsonNode generateJson(String ontology, String user) throws IOException {
	    String pathData = "datos";
	    final JsonNode ontologySchema = mapper
				.readTree(ontologyService.getOntologyByIdentification(ontology, user).getJsonSchema());
		schema = ontologySchema;
		final JsonNode fieldsSchema = mapper.createObjectNode();
		pathData = refJsonSchema(ontologySchema);

		final JsonNode subSchema = !ontologySchema.path(pathData).isMissingNode()
				? ontologySchema.path(pathData).path(PATH_PROPERTIES)
				: ontologySchema.path(PATH_PROPERTIES);

		subSchema.fieldNames().forEachRemaining(f -> {
			final String currentFieldType = subSchema.path(f).get("type").asText();
			switch (currentFieldType) {
			case STRING_VAR:
				if (!subSchema.path(f).path("enum").isMissingNode())
					((ObjectNode) fieldsSchema).put(f, subSchema.path(f).get("enum").get(0).asText());
				else {
					if (f.equals(DATE_VAR) || subSchema.path(f).get(FORMAT_VAR) != null)
						((ObjectNode) fieldsSchema).put(f, getCurrentDate());
					else
						((ObjectNode) fieldsSchema).put(f, "");
				}
				break;
			case NUMBER_VAR:
			case INTEGER_VAR:
				((ObjectNode) fieldsSchema).put(f, 0);
				break;
			case BOOLEAN_VAR:
				((ObjectNode) fieldsSchema).put(f, true);
				break;
			case OBJECT_VAR:
				((ObjectNode) fieldsSchema).set(f, createObject(subSchema.path(f)));
				break;
			case ARRAY_VAR:
				final JsonNode arrayNode = createArray(subSchema.path(f));
				if (!arrayNode.isArray()) {
					final ArrayNode array = mapper.createArrayNode();
					array.add(arrayNode);
					((ObjectNode) fieldsSchema).set(f, array);
				} 
				else
					((ObjectNode) fieldsSchema).set(f, arrayNode);
				break;
			default:
				break;
			}

		});

		if (!pathData.equals("")) {
			final String context = schema.get(PATH_PROPERTIES).fields().next().getKey();
			return mapper.createObjectNode().set(context, fieldsSchema);
		} else {
			return fieldsSchema;

		}

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

	private String getCurrentDate() {
		final DateFormat dfr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dfr.format(new Date());
	}

	private JsonNode createObject(JsonNode fieldNode) {
		auxNodeObject = null;
		final JsonNode objectNode = mapper.createObjectNode();

		if (!fieldNode.path(PATH_PROPERTIES).isMissingNode()) {
			final JsonNode subFieldNode = fieldNode.path(PATH_PROPERTIES);

			subFieldNode.fieldNames().forEachRemaining(f -> {
				final String currentFieldType = subFieldNode.path(f).get("type").asText();
				switch (currentFieldType) {
				case STRING_VAR:
					if (!subFieldNode.path(f).path("enum").isMissingNode())
						((ObjectNode) objectNode).put(f, subFieldNode.path(f).get("enum").get(0).asText());
					else {
						if (f.equals(DATE_VAR) || subFieldNode.path(f).get(FORMAT_VAR) != null)
							((ObjectNode) objectNode).put(f, getCurrentDate());
						else
							((ObjectNode) objectNode).put(f, "");
					}
					break;
				case INTEGER_VAR:
				case NUMBER_VAR:
					((ObjectNode) objectNode).put(f, 0);
					break;

				case BOOLEAN_VAR:
					((ObjectNode) objectNode).put(f, true);
					break;
				case OBJECT_VAR:
					((ObjectNode) objectNode).set(f, createObject(subFieldNode.path(f)));
					break;
				case ARRAY_VAR:
					final JsonNode object = createArray(subFieldNode.path(f));
					if (!object.isArray()) {
						final JsonNode arrayNode = mapper.createArrayNode();
						((ArrayNode) arrayNode).add(object);
						((ObjectNode) objectNode).set(f, arrayNode);
					} 
					else
						((ObjectNode) objectNode).set(f, object);
					break;
				default:
					break;
				}
			});

		} else {

			fieldNode.fieldNames().forEachRemaining(f -> {

				if (f.equals("oneOf") || f.equals("anyOf")) {
					final String property = fieldNode.get(f).get(0).get("$ref").asText().replace("#/", "");
					auxNodeObject = createObject(schema.path(property));
				}
			});
			return auxNodeObject;

		}
		return objectNode;
	}

	private JsonNode createArray(JsonNode fieldNode) {
		auxNodeArray = null;
		final JsonNode objectNode = mapper.createObjectNode();
		if (!fieldNode.path(PATH_PROPERTIES).isMissingNode()) {
			final JsonNode subFieldNode = fieldNode.path(PATH_PROPERTIES);
			subFieldNode.fieldNames().forEachRemaining(f -> {
				final String currentFieldType = subFieldNode.path(f).get("type").asText();
				switch (currentFieldType) {
				case STRING_VAR:
					if (!subFieldNode.path(f).path("enum").isMissingNode())
						((ObjectNode) objectNode).put(f, subFieldNode.path(f).get("enum").get(0).asText());
					else {
						if (f.equals(DATE_VAR) || subFieldNode.path(f).get(FORMAT_VAR) != null)
							((ObjectNode) objectNode).put(f, getCurrentDate());
						else
							((ObjectNode) objectNode).put(f, "");
					}
					break;
				case NUMBER_VAR:
				case INTEGER_VAR:
					((ObjectNode) objectNode).put(f, 0);
					break;
				case BOOLEAN_VAR:
					((ObjectNode) objectNode).put(f, true);
					break;
				case OBJECT_VAR:
					auxNodeArray = createObject(subFieldNode.path(f));
					break;
				default:
					break;
				}
			});
			return auxNodeArray;
		} 
		else if (!fieldNode.path("items").isMissingNode()) {
			final JsonNode subFieldNode = fieldNode.path("items");
			final ArrayNode nodeArray = mapper.createArrayNode();
			if (subFieldNode.isArray()) {
				IntStream.range(0, subFieldNode.size()).forEach(i -> {
					final String type = subFieldNode.get(i).get("type").asText();
					switch (type) {
					case STRING_VAR:
						nodeArray.add("");
						break;
					case NUMBER_VAR:
					case INTEGER_VAR:
						nodeArray.add(0);
						break;
					case BOOLEAN_VAR:
						nodeArray.add(true);
						break;
					case OBJECT_VAR:
						// TO-DO return value
						nodeArray.add(createObject(subFieldNode.get(i)));
						break;

					default:
						break;
					}
				});

			} 
			else {
				final String type = subFieldNode.get("type").asText();
				switch (type) {
				case STRING_VAR:
					nodeArray.add("");
					break;
				case NUMBER_VAR:
				case INTEGER_VAR:
					nodeArray.add(0);
					break;
				case OBJECT_VAR:
					nodeArray.add(createObject(subFieldNode));
					break;
				case ARRAY_VAR:
					nodeArray.add(createArray(subFieldNode));
					break;
				default:
					break;
				}
			}
			return nodeArray;
		}
		return objectNode;
	}

}

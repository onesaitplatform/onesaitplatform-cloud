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
package com.minsait.onesait.platform.controlpanel.rest.management.importtool;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class JsonSchemaGenerator {
	
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    public static String outputAsString(String title, String description,
                                        String json) throws IOException {
        return cleanup(outputAsString(title, description, json, null));
    }

    private static String outputAsString(String title, String description,
                                         String json, JsonNodeType type) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(json);
        StringBuilder output = new StringBuilder();
        output.append("{");

        if (type == null) output.append(
                "\"title\": \"" +
                        title + "\", \"description\": \"" +
                        description + "\", \"$schema\": \"http://json-schema.org/draft-04/schema#\","
                        		+ " \"type\": \"object\", \"properties\": {");

        for (Iterator<String> iterator = jsonNode.fieldNames(); iterator.hasNext();) {
            String fieldName = iterator.next();
            JsonNodeType nodeType = jsonNode.get(fieldName).getNodeType();
            output.append(convertNodeToStringSchemaNode(jsonNode, nodeType, fieldName));
        }

        if (type == null) output.append("}");
        output.append("}");
        return output.toString();
    }

    private static String convertNodeToStringSchemaNode(
            JsonNode jsonNode, JsonNodeType nodeType, String key) throws IOException {
        StringBuilder result = new StringBuilder("\"" + key + "\": { \"type\": \"");
        JsonNode node = null;
        switch (nodeType) {
            case ARRAY :
                node = jsonNode.get(key).get(0);
                result.append("array\", \"items\": { \"properties\":");
                result.append(outputAsString(null, null, node.toString(), JsonNodeType.ARRAY));
                result.append("}},");
                break;
            case BOOLEAN:
                result.append("boolean\" },");
                break;
            case NUMBER:
                result.append("number\" },");
                break;
            case OBJECT:
                node = jsonNode.get(key);
                result.append("object\", \"properties\": ");
                result.append(outputAsString(null, null, node.toString(), JsonNodeType.OBJECT));
                result.append("},");
                break;
            case STRING:
                result.append("string\" },");
                break;
        }

        return result.toString();
    }

    private static String cleanup(String dirty) {
        JSONObject rawSchema = new JSONObject(new JSONTokener(dirty));
        return rawSchema.toString();
    }

}

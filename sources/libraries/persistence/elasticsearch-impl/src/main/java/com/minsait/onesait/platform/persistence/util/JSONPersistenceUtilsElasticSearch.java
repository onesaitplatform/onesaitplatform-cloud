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
package com.minsait.onesait.platform.persistence.util;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.opensearch.client.opensearch._types.mapping.BooleanProperty;
import org.opensearch.client.opensearch._types.mapping.ByteNumberProperty;
import org.opensearch.client.opensearch._types.mapping.DateProperty;
import org.opensearch.client.opensearch._types.mapping.DoubleNumberProperty;
import org.opensearch.client.opensearch._types.mapping.FloatNumberProperty;
import org.opensearch.client.opensearch._types.mapping.GeoShapeProperty;
import org.opensearch.client.opensearch._types.mapping.HalfFloatNumberProperty;
import org.opensearch.client.opensearch._types.mapping.IntegerNumberProperty;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch._types.mapping.LongNumberProperty;
import org.opensearch.client.opensearch._types.mapping.NestedProperty;
import org.opensearch.client.opensearch._types.mapping.ObjectProperty;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.Property.Kind;
import org.opensearch.client.opensearch._types.mapping.ScaledFloatNumberProperty;
import org.opensearch.client.opensearch._types.mapping.ShortNumberProperty;
import org.opensearch.client.opensearch._types.mapping.TextProperty;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSONPersistenceUtilsElasticSearch {

    private JSONPersistenceUtilsElasticSearch() {

    }

    private static final String PARSING_SCHEMA_ERROR = "There was an error parsing the schema.";
    private static final String PROPERTIES = "properties";
    private static final String ITEMS = "items";
    private static final String PROPERTY = "Property ";
    private static final String FORMAT = "format";
    private static final List<String> geoShapes = Arrays.asList("point", "linestring", "polygon", "multipoint",
            "multilinestring", "multipolygon", "geometrycollection", "envelope", "circle");

    /**
     * Check if the json contains $schema field
     * 
     * @param schemaString
     * @return boolean
     * @throws JsonSyntaxException
     */
    public static boolean isJSONSchema(final String schemaString) {
        try {
            final JsonObject jsonSchema = new JsonParser().parse(schemaString).getAsJsonObject();
            return (jsonSchema.has("$schema"));
        } catch (final Exception e) {
            log.error(PARSING_SCHEMA_ERROR, e);
            throw new JsonSyntaxException(PARSING_SCHEMA_ERROR);
        }
    }

    /**
     * Gets the elastic search index from json schema
     * 
     * @param schemaString
     * @return String containing a json with the elastic search index
     * @throws JsonSyntaxException
     * @throws JsonParseException
     */
    public static String getElasticSearchSchemaFromJSONSchema(final String schemaString) {
        try {
            final JsonObject jsonSchema = new JsonParser().parse(schemaString).getAsJsonObject();
            if (jsonSchema.has(PROPERTIES) && jsonSchema.get(PROPERTIES).isJsonObject()
                    && !jsonSchema.getAsJsonObject(PROPERTIES).entrySet().isEmpty())
                return getElasticIndexFromObject(jsonSchema, jsonSchema, PROPERTIES).toString();
            else
                return "";
        } catch (final JsonParseException e) {
            log.error(PARSING_SCHEMA_ERROR, e);
            throw e;
        }
    }
    
    /**
     * Gets the elastic search index from json schema
     * 
     * @param schemaString
     * @return String containing a json with the elastic search index
     * @throws JsonSyntaxException
     * @throws JsonParseException
     */
    public static Map<String, Property> getOpenSearchSchemaFromJSONSchema(final String schemaString) {
        try {
            final JsonObject jsonSchema = new JsonParser().parse(schemaString).getAsJsonObject();
            if (jsonSchema.has(PROPERTIES) && jsonSchema.get(PROPERTIES).isJsonObject()
                    && !jsonSchema.getAsJsonObject(PROPERTIES).entrySet().isEmpty()) {
                final JsonObject jsonMapping = getElasticIndexFromObject(jsonSchema, jsonSchema, PROPERTIES);
            	return getOpenSearchMapping(jsonMapping, "properties");
            }else {
                return new HashMap<String, Property>();
            }
        } catch (final JsonParseException e) {
            log.error(PARSING_SCHEMA_ERROR, e);
            throw e;
        }
    }

    private static final List<String> stringType = Arrays.asList("text", "keyword");
    private static final List<String> numberType = Arrays.asList("float", "double", "half_float", "scaled_float");
    private static final List<String> integerType = Arrays.asList("integer", "long", "short", "byte");
    private static final List<String> arrayType = Arrays.asList("nested");

    /**
     * Gets the elastic search type based on the json type schema or the custom
     * 
     * @param type
     * @return string containing the elastic search type
     */
    public static String getElasticType(final String type, final String elasticsearchType) {
        switch (type) {
        case "string":
            return stringType.contains(elasticsearchType) ? elasticsearchType : "keyword";
        case "number":
            return numberType.contains(elasticsearchType) ? elasticsearchType : "float";
        case "integer":
            return integerType.contains(elasticsearchType) ? elasticsearchType : "integer";
        case "array":
            return arrayType.contains(elasticsearchType) ? elasticsearchType : null;
        case "object":
        case "boolean":
            return type;
        default:
            throw new JsonSyntaxException(MessageFormat.format("The type {0} is not supported by elasticsearch", type));
        }
    }

    /**
     * Creates recursively the elastic search index based on the json schema
     * 
     * @param jsonSchema - the full json schema in GSON JsonObject
     * @param jsonObject - a json object in GSON JsonObject
     * @param propName   - the property name
     * @return - a json object in GSON representing a elastic search field
     * @throws JsonSyntaxException
     */
    // TODO support recursive schema
    public static JsonObject getElasticIndexFromObject(final JsonObject jsonSchema, final JsonObject jsonObject,
            final String propName) {
        final JsonObject property = jsonObject.has("$ref") ? resolveSchemaRef(jsonSchema, jsonObject) : jsonObject;
        final JsonObject elasticProperty = new JsonObject();
        final JsonObject elasticProperties = new JsonObject();

        if (property.has("type") && !property.get("type").isJsonNull() && property.get("type").isJsonPrimitive()
                && property.get("type").getAsJsonPrimitive().isString()) {
            final String propType = property.get("type").getAsString().toLowerCase().trim();

            switch (propType) {
            case "array":
                if (property.has(ITEMS)) {
                    // Items is array...
                    if (property.get(ITEMS).isJsonArray() && property.getAsJsonArray(ITEMS).size() > 0) {

                        final List<JsonObject> itemsObjects = StreamSupport
                                .stream(property.get(ITEMS).getAsJsonArray().spliterator(), false).map(it -> {
                                    if (it.isJsonObject()) {
                                        return it.getAsJsonObject().has("$ref")
                                                ? resolveSchemaRef(jsonSchema, it.getAsJsonObject())
                                                : it.getAsJsonObject();
                                    } else {
                                        throw new JsonSyntaxException(PROPERTY + propName
                                                + " is type array, but an items's field is not an object.");
                                    }
                                }).collect(Collectors.toList());

                        final int itemsSize = itemsObjects.stream().map(itemJsonArray -> {
                            if (itemJsonArray.has("type") && itemJsonArray.get("type").isJsonPrimitive()
                                    && itemJsonArray.get("type").getAsJsonPrimitive().isString()) {
                                return itemJsonArray.get("type").getAsString();
                            } else {
                                throw new JsonSyntaxException(
                                        PROPERTY + propName + " is type array, but an item's type is not string.");
                            }
                        }).distinct().collect(Collectors.toList()).size();

                        if (itemsSize > 1) {
                            throw new JsonSyntaxException(PROPERTY + propName
                                    + " is type array and has differents types in it. Elastic search does not support multitype mapping into arrays.");
                        }
                        
                        return getElasticIndexFromObject(jsonSchema, itemsObjects.get(0), propName);

                        // Items is object...
                    } else if (property.get(ITEMS).isJsonObject()) {
                        String elasticsearchType = getElasticsearchTypeFromJsonSchema(property);
                        if (elasticsearchType != null) {
                            elasticProperties.addProperty("type", getElasticType(propType, elasticsearchType));
                        }
                       
                        JsonObject internalObject = getElasticIndexFromObject(jsonSchema, property.getAsJsonObject(ITEMS), propName);
                        internalObject.entrySet().forEach(e -> elasticProperties.add(e.getKey(), e.getValue()));
                        return elasticProperties;
                    } else {
                        throw new JsonSyntaxException(
                                "Property '" + propName + "' is type array but field items is empty.");
                    }
                } else {
                    throw new JsonSyntaxException(
                            "Property '" + propName + "' is type array, but no field items was found.");
                }
            case "object":
                if (property.has(PROPERTIES) && property.get(PROPERTIES).isJsonObject()
                        && !property.getAsJsonObject(PROPERTIES).entrySet().isEmpty()) {
                    final JsonObject objectProperties = property.getAsJsonObject(PROPERTIES);

                    if (objectProperties.entrySet().size() == 2 && objectProperties.has("coordinates")
                            && objectProperties.has("type") && objectProperties.get("coordinates").isJsonObject()
                            && objectProperties.get("type").isJsonObject()
                            && objectProperties.getAsJsonObject("type").has("enum")
                            && objectProperties.getAsJsonObject("type").get("enum").isJsonArray()
                            && objectProperties.getAsJsonObject("type").get("enum").getAsJsonArray().size() == 1
                            && objectProperties.getAsJsonObject("type").get("enum").getAsJsonArray().get(0)
                                    .isJsonPrimitive()
                            && objectProperties.getAsJsonObject("type").get("enum").getAsJsonArray().get(0)
                                    .getAsJsonPrimitive().isString()
                            && geoShapes.contains(objectProperties.getAsJsonObject("type").get("enum").getAsJsonArray()
                                    .get(0).getAsString().toLowerCase())) {

                        elasticProperties.addProperty("type", "geo_shape");

                    } else {
                        objectProperties.entrySet().stream().forEach(pro -> {
                            if (pro.getValue().isJsonObject())
                                elasticProperty.add(pro.getKey(), getElasticIndexFromObject(jsonSchema,
                                        pro.getValue().getAsJsonObject(), pro.getKey()));
                            else
                                throw new JsonSyntaxException(PROPERTY + pro.getKey() + " of property " + propName
                                        + " is not an json object.");
                        });

                        elasticProperties.add(PROPERTIES, elasticProperty);
                    }

                } else {
                    elasticProperties.addProperty("type", "object");
                }

                break;
            case "string":
                // If is a date format...
                if (property.has(FORMAT) && property.get(FORMAT).isJsonPrimitive()
                        && property.getAsJsonPrimitive(FORMAT).isString()) {
                                       
                    elasticProperties.addProperty("type", "date");
                    
                    String format = property.getAsJsonPrimitive(FORMAT).getAsString();
                    switch (format) {
                    case "date":
                        elasticProperties.addProperty("format", "strict_date");
                        break;
                    case "date-time":
                        elasticProperties.addProperty("format", "strict_date_time");
                    default:
                        break;
                    }
                } else {
                    elasticProperties.addProperty("type", getElasticType(propType, getElasticsearchTypeFromJsonSchema(property)));                    
                }
                break;
            default:
                String elasticsearchType = getElasticsearchTypeFromJsonSchema(property);
                elasticProperties.addProperty("type", getElasticType(propType, elasticsearchType));
                addAdditionalParameters(elasticProperties, property, elasticsearchType);

                break;
            }

        } else {
            throw new JsonSyntaxException("Type of property '" + propName + "' is not a string or does not exist.");
        }

        return elasticProperties;
    }

    public static Map<String, Property> getOpenSearchMapping(final JsonObject jsonObject, final String property) {
    	Map<String, Property> mapping=new HashMap<>();
    	//TODO: transform each JsonObject into a MAP of properties.
    	Set<Entry<String, JsonElement>> elements = jsonObject.getAsJsonObject(property).entrySet();
    	for(Entry<String, JsonElement> entry:elements) {
    		if(entry.getValue().isJsonObject()) {
    			if(entry.getValue().getAsJsonObject().has("properties")) {
    				//is an object with more properties
    				final Map<String, Property> newProps = getOpenSearchMapping(entry.getValue().getAsJsonObject(),"properties");
    				Property complexObj = new Property(new ObjectProperty.Builder().properties(newProps).build());
    				mapping.put(entry.getKey(),complexObj);
    			}
    			else if(entry.getValue().getAsJsonObject().has("type")) {
    				//is a field with data type
    				final String format = entry.getValue().getAsJsonObject().get("format")==null?null:entry.getValue().getAsJsonObject().get("format").getAsString();
    				mapping.put(entry.getKey(), getOpenSearchMappingType(entry.getValue().getAsJsonObject().get("type").getAsString(),format));
    			}
    		}
    	}
    	return mapping;
    }
    
    private static Property getOpenSearchMappingType(String propType,String format) {
    	
    	switch(propType) {
    	case "text":
    		return new Property(new TextProperty.Builder().build());
    	case "keyword":
    		return new Property(new KeywordProperty.Builder().build());
    	case "float":
    		return new Property(new FloatNumberProperty.Builder().build());
    	case "double":
    		return new Property(new DoubleNumberProperty.Builder().build());
    	case "half_float":
    		return new Property(new HalfFloatNumberProperty.Builder().build());
    	case "scaled_float":
    		return new Property(new ScaledFloatNumberProperty.Builder().build());
    	case "integer":
    		return new Property(new IntegerNumberProperty.Builder().build());
    	case "long":
    		return new Property(new LongNumberProperty.Builder().build());
    	case "short":
    		return new Property(new ShortNumberProperty.Builder().build());
    	case "byte":
    		return new Property(new ByteNumberProperty.Builder().build());
    	case "nested":
    		return new Property(new NestedProperty.Builder().build());
    	case "object":
    		return new Property(new ObjectProperty.Builder().build());
    	case "boolean":
    		return new Property(new BooleanProperty.Builder().build());
    	case "geo_shape":
    		return new Property(new GeoShapeProperty.Builder().build());
    	case "date": 
    		if(format!=null)
    			return new Property(new DateProperty.Builder().format(format).build());
    		return new Property(new DateProperty.Builder().build());
    	default:
    		throw new JsonSyntaxException(MessageFormat.format("The type {0} is not supported by OpenSearch", propType));
    	}
    }
    
    private static void addAdditionalParameters(JsonObject elasticProperties, JsonObject property,
            String elasticsearchType) {
        if (elasticsearchType != null && "scaled_float".equals(elasticsearchType)
                && property.has("elasticsearch_scaling_factor")) {
            JsonElement elasticTypeJson = property.get("elasticsearch_scaling_factor");
            if (elasticTypeJson.isJsonPrimitive()) {
                elasticProperties.add("scaling_factor", elasticTypeJson);
            }
        }
    }

    private static String getElasticsearchTypeFromJsonSchema(JsonObject property) {
        if (property.has("elasticsearch_type")) {
            JsonElement elasticTypeJson = property.get("elasticsearch_type");
            if (elasticTypeJson.isJsonPrimitive() && elasticTypeJson.getAsJsonPrimitive().isString()) {
                return elasticTypeJson.getAsString();
            }
        }
        return null;
    }

    /**
     * Resolves $ref from json schemas
     * 
     * @param jsonSchema - the complete json schema in GSON JsonObject
     * @param obj        - the json object containing $ref param in GSON JsonObject
     * @return - the json referenced locally in GSON JsonObject
     */
    private static JsonObject resolveSchemaRef(final JsonObject jsonSchema, final JsonObject obj) {
        if (obj.has("$ref")) {
            final String ref = obj.get("$ref").getAsString();
            if (ref.startsWith("#/")) {
                final String jsonPath = ref.substring(2).replaceAll("/", "\\.");
                final JsonElement jsonElement = getJsonElement(jsonSchema, jsonPath);
                if (jsonElement.isJsonObject())
                    return jsonElement.getAsJsonObject();
                else
                    throw new JsonSyntaxException(
                            "Can not resolve reference. Reference '" + ref + "' not found or is not an object");
            } else {
                throw new JsonSyntaxException("Can not resolve reference. It does not starts with #/.");
            }
        } else {
            throw new JsonSyntaxException("Object does not contain $ref field.");
        }
    }

    /**
     * Returns a JSON sub-element from the given JsonElement and the given path
     *
     * @param json - a Gson JsonElement
     * @param path - a JSON path, e.g. a.b.c[2].d
     * @return - a sub-element of json according to the given path
     */
    private static JsonElement getJsonElement(JsonElement json, String path) {
        final String[] parts = path.split("\\.|\\[|\\]");
        JsonElement result = json;

        for (String key : parts) {

            key = key.trim();
            if (key.isEmpty())
                continue;

            if (result == null) {
                result = JsonNull.INSTANCE;
                break;
            }

            if (result.isJsonObject()) {
                result = ((JsonObject) result).get(key);
            } else if (result.isJsonArray()) {
                final int ix = Integer.valueOf(key) - 1;
                result = ((JsonArray) result).get(ix);
            } else {
                break;
            }
        }

        return result;
    }

}

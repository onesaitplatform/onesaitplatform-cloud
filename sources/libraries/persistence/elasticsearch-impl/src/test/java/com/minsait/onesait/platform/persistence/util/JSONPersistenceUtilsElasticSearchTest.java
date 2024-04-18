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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;

@RunWith(MockitoJUnitRunner.class)
public class JSONPersistenceUtilsElasticSearchTest {
    
    private ObjectMapper mapper = new ObjectMapper();
    
    private static String jsonSchemaBasicTypesWithElasticsearchFields;
    private static String jsonSchemaObjectType;
    private static String jsonSchemaArrayType;
    private static String jsonSchemaArrayNestedType;
    private static String jsonSchemaArrayWithMultipleTypes;
    private static String jsonSchemaDateTypes;
    private static String jsonSchemaGeoJson;

    @BeforeClass
    public static void loadSchemas() throws IOException {
        ClassLoader classLoader = JSONPersistenceUtilsElasticSearchTest.class.getClassLoader();
        File fileBasic = new File(classLoader.getResource("ontology-schemas/json-schema-basic-types-with-elasticsearch-fields.json").getFile());
        jsonSchemaBasicTypesWithElasticsearchFields = new String(Files.readAllBytes(fileBasic.toPath()), StandardCharsets.UTF_8);
    
        File fileObject = new File(classLoader.getResource("ontology-schemas/json-schema-object-type.json").getFile());
        jsonSchemaObjectType = new String(Files.readAllBytes(fileObject.toPath()), StandardCharsets.UTF_8);
        
        File fileArray = new File(classLoader.getResource("ontology-schemas/json-schema-array-type.json").getFile());
        jsonSchemaArrayType = new String(Files.readAllBytes(fileArray.toPath()), StandardCharsets.UTF_8);
        
        File fileArrayNested = new File(classLoader.getResource("ontology-schemas/json-schema-array-nested-type.json").getFile());
        jsonSchemaArrayNestedType = new String(Files.readAllBytes(fileArrayNested.toPath()), StandardCharsets.UTF_8);
        
        File fileArrayMultipleTypes = new File(classLoader.getResource("ontology-schemas/json-schema-array-with-multiple-types.json").getFile());
        jsonSchemaArrayWithMultipleTypes = new String(Files.readAllBytes(fileArrayMultipleTypes.toPath()), StandardCharsets.UTF_8);
        
        File fileDateTypes = new File(classLoader.getResource("ontology-schemas/json-schema-date-types.json").getFile());
        jsonSchemaDateTypes = new String(Files.readAllBytes(fileDateTypes.toPath()), StandardCharsets.UTF_8);
        
        File fileGeoJson = new File(classLoader.getResource("ontology-schemas/json-schema-geojson-objects.json").getFile());
        jsonSchemaGeoJson = new String(Files.readAllBytes(fileGeoJson.toPath()), StandardCharsets.UTF_8);
    }
    
    @Test
    public void given_jsonSchemaWithBasicTypes_When_ElasticsearchSchemaIsGenerated_Then_CorrectTypesAreUsed() throws JsonProcessingException, IOException {
        JsonNode mapping = getMapping(jsonSchemaBasicTypesWithElasticsearchFields);
        JsonNode properties = mapping.get("properties").get("ontology").get("properties");
        
        checkProperty(properties, "valor_boolean", "type", "boolean");
        checkProperty(properties, "valor_boolean_default", "type", "boolean");

        checkProperty(properties, "valor_integer", "type", "integer");
        checkProperty(properties, "valor_integer_default", "type", "integer");
        checkProperty(properties, "valor_long", "type", "long");
        checkProperty(properties, "valor_short", "type", "short");
        checkProperty(properties, "valor_byte", "type", "byte");        
        
        checkProperty(properties, "valor_keyword", "type", "keyword");
        checkProperty(properties, "valor_keyword_default", "type", "keyword");
        checkProperty(properties, "valor_text", "type", "text");
                
        checkProperty(properties, "valor_float", "type", "float");
        checkProperty(properties, "valor_float_default", "type", "float");
        checkProperty(properties, "valor_double", "type", "double");
        checkProperty(properties, "valor_half_float", "type", "half_float");        
        checkProperty(properties, "valor_scaled_float", "type", "scaled_float");
        checkProperty(properties, "valor_scaled_float", "scaling_factor", 10.0);        
    }
    
    @Test
    public void given_jsonSchemaWithObjectType_When_ElasticsearchSchemaIsGenerated_CorrectTypesAreUsed() throws JsonProcessingException, IOException {
        JsonNode mapping = getMapping(jsonSchemaObjectType);
        JsonNode properties = mapping.get("properties").get("ontology").get("properties").get("complex_value").get("properties");
        checkProperty(properties, "value1", "type", "keyword");
        checkProperty(properties, "value2", "type", "keyword");               
    }
    
    @Test
    public void given_jsonSchemaWithArrayOfBasicType_When_ElasticsarchSchemaIsGenerated_CorrectTypesAreUsed() throws JsonProcessingException, IOException {
        JsonNode mapping = getMapping(jsonSchemaArrayType);
        JsonNode properties = mapping.get("properties").get("ontology").get("properties");
        checkProperty(properties, "value1", "type", "keyword");
        checkProperty(properties, "value2", "type", "keyword"); 
    }
    
    @Test
    public void given_jsonSchemaWithArrayOfNestedObjectType_When_ElasticsarchSchemaIsGenerated_CorrectTypesAreUsed() throws JsonProcessingException, IOException {
        JsonNode mapping = getMapping(jsonSchemaArrayNestedType);
        JsonNode properties = mapping.get("properties").get("ontology").get("properties");
        checkProperty(properties, "value1", "type", "keyword");
        checkProperty(properties, "value2", "type", "nested");
        JsonNode internalObject = properties.get("value2").get("properties");
        checkProperty(internalObject, "value2-1", "type", "keyword");
        checkProperty(internalObject, "value2-2", "type", "keyword");
    }
    
    @Test(expected = JsonSyntaxException.class)
    public void given_jsonSchemaWithArrayOfMultipleTypes_When_ElasticsearchSchemaIsGenerated_ItMustFail() throws JsonProcessingException, IOException {
        getMapping(jsonSchemaArrayWithMultipleTypes);
        fail("Should have thrown JsonSyntaxException");
    }
    
    @Test
    public void given_jsonSchemaWithStringsWithDateFormat_When_ElasticsearchSchemaIsGenerated_CorrectDateFormatIsUsed() throws JsonProcessingException, IOException {
        JsonNode mapping = getMapping(jsonSchemaDateTypes);
        JsonNode properties = mapping.get("properties").get("ontology").get("properties");
        checkProperty(properties, "date", "type", "date");
        checkProperty(properties, "date", "format", "strict_date");
        checkProperty(properties, "timestamp", "type", "date");
        checkProperty(properties, "timestamp", "format", "strict_date_time");
    }
    
    @Test
    public void given_jsonSchemaWithGeoJsonObjects_When_ElasticsearchSchemaIsGenerated_NativeElasticsearchTypesAreUsed() throws JsonProcessingException, IOException {
        JsonNode mapping = getMapping(jsonSchemaGeoJson);
        JsonNode properties = mapping.get("properties").get("ontology").get("properties");
        checkProperty(properties, "point", "type", "geo_shape");
        checkProperty(properties, "linestring", "type", "geo_shape");
        checkProperty(properties, "polygon", "type", "geo_shape");
        checkProperty(properties, "myltipoint", "type", "geo_shape");
        checkProperty(properties, "multilinestring", "type", "geo_shape");
        checkProperty(properties, "multipolygon", "type", "geo_shape");        
    }
    
    private JsonNode getMapping(String schema) throws JsonProcessingException, IOException {
        String generatedMapping = JSONPersistenceUtilsElasticSearch.getElasticSearchSchemaFromJSONSchema(schema);
        assertNotNull("Mapping should not be null", generatedMapping);
        return mapper.readTree(generatedMapping);
    }
    
    private <T> void checkProperty(JsonNode properties, String property, String fieldName, T fieldValue) {
        JsonNode field = properties.get(property);
        assertNotNull(property + " should not be null", field);
        
        Object value = null;
        if (fieldValue instanceof String) {
            value = field.get(fieldName).asText();
        } else if (fieldValue instanceof Double) {
            value = field.get(fieldName).asDouble();
        }
        StringBuilder msg = new StringBuilder()
                .append(property)
                .append(" have field ")
                .append(fieldName)
                .append(" with value ")
                .append(value)
                .append(" instead of ")
                .append(fieldValue);
        assertTrue(msg.toString(), fieldValue.equals(value));
    }
}

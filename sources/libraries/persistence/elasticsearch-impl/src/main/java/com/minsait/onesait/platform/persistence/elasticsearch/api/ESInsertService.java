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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESInsertService {

	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private ObjectMapper mapper;
    @Autowired
    private RestHighLevelClient hlClient;

	private static final String GEOMERY_STR = "geometry";
	private static final String GEOMERYCOLLECTION_STR = "geometrycollection";
	private static final String POLYGON_STR = "polygon";
	private static final String MULTIPOLYGON_STR = "multipolygon";
	private static final String MULTIPOINT_STR = "multipoint";
	private static final String POINT_STR = "point";
	private static final String LINE_STR = "linestring";
	private static final String MULTILINE_STR = "multilinestring";
	private static final String FEATURE_STR = "feature";
	private static final String FEATURECOLLECTION_STR = "featurecollection";
	private static final String DATE = "$date";
	private static final String SOURCE = "source";

	private String fixPosibleNonCapitalizedGeometryPoint(String s, String index) {
		try {
			final JsonObject o = new JsonParser().parse(s).getAsJsonObject();
			if (o.get(SOURCE) != null && o.get(SOURCE).getAsString().equals("AUDIT"))
				return s;
			s = s.replaceAll("Point|POINT", POINT_STR)
					.replaceAll("MultiPoint|MULTIPOINT|multiPoint|Multipoint", MULTIPOINT_STR)
					.replaceAll("Polygon|POLYGON", POLYGON_STR)
					.replaceAll("MultiPolygon|MULTIPOLYGON|Multipolygon|multiPolygon", MULTIPOLYGON_STR)
					.replaceAll("Geometry|GEOMETRY", GEOMERY_STR)
					.replaceAll("GeometryCollection|GEOMETRYCOLLECTION", GEOMERYCOLLECTION_STR)
					.replaceAll("LineString|LINESTRING|lineString|Linestring", LINE_STR)
					.replaceAll("MultiLineString|MULTILINESTRING|MultiLineString", MULTILINE_STR)
					.replaceAll("FeatureCollection|FEATURECOLLECTION|featureCollection|Featurecollection",
							FEATURECOLLECTION_STR)
					.replaceAll("Feature|FEATURE", FEATURE_STR);

		} catch (final Exception e) {
			log.warn("Error fixing non capitalized geometry point for audit message: " + s, e);
		}
		return s;

	}

	private String fixPosibleDollarDates(String s, String schema, String index) {
		final JsonObject instance = new JsonParser().parse(s).getAsJsonObject();
		if (instance.get(SOURCE) != null && instance.get(SOURCE).getAsString().equals("AUDIT"))
			return s;
		if (s.contains(DATE)) {
			try {
				final String elasticSchema = JSONPersistenceUtilsElasticSearch
						.getElasticSearchSchemaFromJSONSchema(schema);

				final JsonObject elasticSchemaObject = new JsonParser().parse(elasticSchema).getAsJsonObject();
				final JsonObject properties = elasticSchemaObject.get(index).getAsJsonObject().get("properties")
						.getAsJsonObject();
				final String refSchema = ontologyDataService.refJsonSchema(mapper.readTree(schema));

				properties.entrySet().forEach(e -> {
					try {
						if (e.getValue().getAsJsonObject().get("type").getAsString().equals("date")) {
							if (!StringUtils.isEmpty(refSchema)) {
								final String parentNode = mapper.readTree(schema).get("properties").fieldNames().next();
								final JsonObject date = instance.get(parentNode).getAsJsonObject().get(e.getKey())
										.getAsJsonObject();
								instance.get(parentNode).getAsJsonObject().remove(e.getKey());
								instance.get(parentNode).getAsJsonObject().add(e.getKey(), date.get(DATE));
							} else {
								final JsonObject date = instance.get(e.getKey()).getAsJsonObject();
								instance.remove(e.getKey());
								instance.add(e.getKey(), date.get(DATE));
							}
						}
					} catch (final Exception e1) {
						log.warn("Error fixing Dollar Dates for audit message: " + s, e1);
					}

				});
				return instance.toString();

			} catch (final Exception e1) {
				log.warn("Error fixing Dollar Dates for audit message: " + s, e1);
			}

		}
		return s;

	}
	
	private BulkResponse bulkInsert(String index, List<String> jsonDocs) {
	    BulkRequest bulkRequest = new BulkRequest();
	    for (String json : jsonDocs) {
	        bulkRequest.add(new IndexRequest(index).source(json, XContentType.JSON));
	    }
	    return executeBulkInsert(bulkRequest);
	}
	
	private BulkResponse executeBulkInsert(BulkRequest bulkRequest) {
	    try {
            return hlClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Error inserting bulk documents ", e);
            return null;
        }
    }

    public ComplexWriteResult bulkInsert(String index, List<String> jsonDocs, String jsonSchema) {
        	    	   
		List<String> parsedJson = new ArrayList<>(jsonDocs.size());	

		for (String s : jsonDocs) {

			s = s.replaceAll("\\n", "");
			s = s.replaceAll("\\r", "");

			s = fixPosibleNonCapitalizedGeometryPoint(s, index);
			s = fixPosibleDollarDates(s, jsonSchema, index);
			parsedJson.add(s);
		}

		BulkResponse bulkResponse = bulkInsert(index, parsedJson);
		
		List<BulkWriteResult> listResult = new ArrayList<>();
		
		for (BulkItemResponse bulkItemResponse : bulkResponse) { 
		    DocWriteResponse itemResponse = bulkItemResponse.getResponse(); 
		    final BulkWriteResult bulkr = new BulkWriteResult();		    
		    bulkr.setId(itemResponse.getId());
		    bulkr.setErrorMessage(itemResponse.toString());
		    bulkr.setOk(itemResponse.getResult().equals(Result.CREATED));
		    
//		    switch (bulkItemResponse.getOpType()) {
//		    case INDEX:    
//		    case CREATE:
//		        IndexResponse indexResponse = (IndexResponse) itemResponse;		        
//                bulkr.setId(indexResponse.getId());
//                bulkr.setErrorMessage(indexResponse.toString());
//                bulkr.setOk(indexResponse.getResult().equals(Result.CREATED));
//                listResult.add(bulkr);
//		        break;
//		    case UPDATE:   
//		        UpdateResponse updateResponse = (UpdateResponse) itemResponse;		        
//		        break;
//		    case DELETE:   
//		        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
//		    }
		    
		    listResult.add(bulkr);
		}

		ComplexWriteResult complexWriteResult = new ComplexWriteResult();
		complexWriteResult.setType(ComplexWriteResultType.BULK);
		complexWriteResult.setData(listResult);

		return complexWriteResult;
	}

}
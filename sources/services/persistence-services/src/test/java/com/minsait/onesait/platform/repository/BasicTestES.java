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
package com.minsait.onesait.platform.repository;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.persistence.elasticsearch.api.ESInsertService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class BasicTestES {

	@Autowired
	ESInsertService service;

	private final String schema = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Audit_developer\",\"type\":\"object\",\"properties\":{\"extraData\":{\"type\":\"string\"},\"module\":{\"type\":\"string\"},\"message\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"otherType\":{\"type\":\"string\"},\"contextData\":{\"type\":\"object\",\"properties\":{\"timestampMillis\":{\"type\":\"number\"},\"clientConnection\":{\"type\":\"string\"},\"clientSession\":{\"type\":\"string\"},\"timezoneId\":{\"type\":\"string\"},\"device\":{\"type\":\"string\"},\"user\":{\"type\":\"string\"},\"deviceTemplate\":{\"type\":\"string\"},\"timestamp\":{\"type\":\"string\"}}},\"timeStamp\":{\"type\":\"number\"},\"formatedTimeStamp\":{\"type\":\"string\"},\"operationType\":{\"type\":\"string\"},\"id\":{\"type\":\"string\"},\"resultOperation\":{\"type\":\"string\"},\"user\":{\"type\":\"string\"},\"ontology\":{\"type\":\"string\"},\"remoteAddress\":{\"type\":\"string\"},\"query\":{\"type\":\"string\"},\"data\":{\"type\":\"string\"},\"domain\":{\"type\":\"string\"},\"sessionKey\":{\"type\":\"string\"},\"gatewayInfo\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":[\"number\",\"string\"]},\"protocol\":{\"type\":\"string\"}}},\"clientPlatform\":{\"type\":\"string\"},\"clientPlatformInstance\":{\"type\":\"string\"},\"ex\":{\"type\":\"object\",\"properties\":{}},\"className\":{\"type\":\"string\"},\"methodName\":{\"type\":\"string\"}},\"description\":\"System Ontology. Auditory of operations between user and Platform for user: administrator\"}";
	private final String index = "audit_developer";
	private final String message = "{\"message\":\"Executed insert on ontology CLM_Social_Network\",\"id\":\"e715a68f-8624-4d02-a51e-6e5b3e345f96\",\"type\":\"FLOWENGINE\",\"timeStamp\":1551476981974,\"formatedTimeStamp\":\"2019-03-01T21:49:41.974Z\",\"user\":\"citylandscapemanager\",\"ontology\":\"CLM_Social_Network\",\"operationType\":\"INSERT\",\"module\":\"FLOWENGINE\",\"resultOperation\":\"SUCCESS\",\"data\":\"{\\\"SocialNetwork\\\":{\\\"user\\\":\\\"Carnaval LPGC\\\",\\\"timestamp\\\":{\\\"$date\\\":\\\"2019-03-01T21:49:36.810Z\\\"},\\\"comment\\\":\\\"RT @Carnaval_lp: Así vivimos la maravillosa obertura de la Gala de la Reina de Las Palmas de Gran Canaria 2019 #GalaReinaLPGC #ReinaLPGC #C…\\\",\\\"picture\\\":\\\"\\\",\\\"sentiment\\\":0,\\\"socialNetwork\\\":\\\"Twitter\\\",\\\"standardLocation\\\":{\\\"iso3166_1\\\":\\\"ES\\\",\\\"iso3166_2\\\":\\\"ES-GC\\\",\\\"un_LOCODE\\\":\\\"ES LPA\\\",\\\"district\\\":\\\"\\\"},\\\"userPicture\\\":\\\"https://pbs.twimg.com/profile_images/1068093843688566784/Y-TZfVSf_normal.jpg\\\"}}\",\"contextData\":{\"deviceTemplate\":null,\"device\":null,\"clientConnection\":null,\"clientSession\":null,\"user\":\"citylandscapemanager\",\"timezoneId\":\"UTC\",\"timestamp\":\"2019-03-01T21:49:41Z\",\"timestampMillis\":1551476981981,\"source\":\"AUDIT\"}}";
	private final String instanceGeometry = "{\n" + "    \"asset\": {\n" + "        \"id\": \"root\",\n"
			+ "        \"name\": \"root asset\",\n" + "        \"description\": \"root asset\",\n"
			+ "        \"ancestors\": [],\n" + "        \"parent\": null,\n" + "        \"location\": {\n"
			+ "            \"type\": \"Point\",\n" + "            \"coordinates\": [\n" + "                42.431134,\n"
			+ "                -8.645587\n" + "            ]\n" + "        }\n" + "    }\n" + " }";
	private final String indexGeo = "assets";

	private final String schemaGeo = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Assets\",\"type\":\"object\",\"required\":[\"asset\"],\"properties\":{\"asset\":{\"$ref\":\"#/datos\"}},\"datos\":{\"description\":\"Info Asset\",\"type\":\"object\",\"required\":[\"id\",\"location\",\"name\",\"description\",\"ancestors\",\"parent\",\"shortname\"],\"properties\":{\"id\":{\"description\":\"Identifies the System/Algorithm/HW/Equipment name that has published the message.\",\"type\":\"string\"},\"name\":{\"description\":\"Name of the asset\",\"type\":\"string\"},\"description\":{\"descripton\":\"Description of the asset\",\"type\":\"string\"},\"ancestors\":{\"description\":\"Hierarchical ancestors of this asset\",\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"parent\":{\"description\":\"Direct ancestor of this asset\",\"type\":[\"string\",\"null\"]},\"signals\":{\"description\":\"Signals of this asset\",\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"location\":{\"$ref\":\"#/definitions/geojson\"},\"shortname\":{\"description\":\"Short name of the asset\",\"type\":\"string\"}}},\"definitions\":{\"geojson\":{\"title\":\"GeoJSON\",\"oneOf\":[{\"$ref\":\"#/definitions/point\"},{\"$ref\":\"#/definitions/lineString\"},{\"$ref\":\"#/definitions/polygon\"},{\"$ref\":\"#/definitions/multiPoint\"},{\"$ref\":\"#/definitions/multiLineString\"},{\"$ref\":\"#/definitions/multiPolygon\"},{\"$ref\":\"#/definitions/geometryCollection\"},{\"$ref\":\"#/definitions/feature\"},{\"$ref\":\"#/definitions/featureCollection\"}]},\"featureCollection\":{\"title\":\"GeoJSON FeatureCollection\",\"type\":\"object\",\"required\":[\"type\",\"features\"],\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"FeatureCollection\"]},\"features\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/feature\"}},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"feature\":{\"title\":\"GeoJSON Feature\",\"type\":\"object\",\"required\":[\"type\",\"properties\",\"geometry\"],\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"Feature\"]},\"properties\":{\"oneOf\":[{\"type\":\"null\"},{\"type\":\"object\"}]},\"geometry\":{\"$ref\":\"#/definitions/geometry\"},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"geometry\":{\"title\":\"geometry\",\"description\":\"One geometry as defined by GeoJSON\",\"type\":\"object\",\"oneOf\":[{\"$ref\":\"#/definitions/point\"},{\"$ref\":\"#/definitions/lineString\"},{\"$ref\":\"#/definitions/polygon\"},{\"$ref\":\"#/definitions/multiPoint\"},{\"$ref\":\"#/definitions/multiLineString\"},{\"$ref\":\"#/definitions/multiPolygon\"},{\"$ref\":\"#/definitions/geometryCollection\"}]},\"point\":{\"title\":\"Point\",\"required\":[\"type\",\"coordinates\"],\"additionalProperties\":false,\"properties\":{\"type\":{\"enum\":[\"Point\"]},\"coordinates\":{\"$ref\":\"#/definitions/position\"},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"multiPoint\":{\"title\":\"MultiPoint\",\"required\":[\"type\",\"coordinates\"],\"additionalProperties\":false,\"properties\":{\"type\":{\"enum\":[\"MultiPoint\"]},\"coordinates\":{\"$ref\":\"#/definitions/positionArray\"},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"lineString\":{\"title\":\"LineString\",\"required\":[\"type\",\"coordinates\"],\"additionalProperties\":false,\"properties\":{\"type\":{\"enum\":[\"LineString\"]},\"coordinates\":{\"$ref\":\"#/definitions/lineStringInternal\"},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"multiLineString\":{\"title\":\"MultiLineString\",\"required\":[\"type\",\"coordinates\"],\"additionalProperties\":false,\"properties\":{\"type\":{\"enum\":[\"MultiLineString\"]},\"coordinates\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/lineStringInternal\"}},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"polygon\":{\"title\":\"Polygon\",\"required\":[\"type\",\"coordinates\"],\"additionalProperties\":false,\"properties\":{\"type\":{\"enum\":[\"Polygon\"]},\"coordinates\":{\"$ref\":\"#/definitions/polygonInternal\"},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"multiPolygon\":{\"title\":\"MultiPolygon\",\"required\":[\"type\",\"coordinates\"],\"additionalProperties\":false,\"properties\":{\"type\":{\"enum\":[\"MultiPolygon\"]},\"coordinates\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/polygonInternal\"}},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"geometryCollection\":{\"title\":\"GeoJSON GeometryCollection\",\"type\":\"object\",\"required\":[\"type\",\"geometries\"],\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"GeometryCollection\"]},\"geometries\":{\"type\":\"array\",\"items\":{\"oneOf\":[{\"$ref\":\"#/definitions/point\"},{\"$ref\":\"#/definitions/lineString\"},{\"$ref\":\"#/definitions/polygon\"},{\"$ref\":\"#/definitions/multiPoint\"},{\"$ref\":\"#/definitions/multiLineString\"},{\"$ref\":\"#/definitions/multiPolygon\"}]}},\"bbox\":{\"$ref\":\"#/definitions/boundingBox\"}}},\"position\":{\"description\":\"A single position\",\"type\":\"array\",\"minItems\":2,\"items\":[{\"type\":\"number\"},{\"type\":\"number\"}],\"additionalItems\":false},\"positionArray\":{\"description\":\"An array of positions\",\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/position\"}},\"lineStringInternal\":{\"description\":\"An array of two or more positions\",\"allOf\":[{\"$ref\":\"#/definitions/positionArray\"},{\"minItems\":2}]},\"linearRing\":{\"description\":\"An array of four positions where the first equals the last\",\"allOf\":[{\"$ref\":\"#/definitions/positionArray\"},{\"minItems\":4}]},\"polygonInternal\":{\"description\":\"An array of linear rings\",\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/linearRing\"}},\"boundingBox\":{\"type\":\"array\",\"minItems\":4,\"items\":{\"type\":\"number\"}}},\"description\":\"Ontology to store Asset information\",\"additionalProperties\":true}";

	@Test
	public void testReplaces() {
		service.bulkInsert(index, Arrays.asList(message), schema);
		service.bulkInsert(indexGeo, Arrays.asList(instanceGeometry), schemaGeo);
	}

}

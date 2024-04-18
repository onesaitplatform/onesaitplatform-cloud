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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.persistence.util.JSONPersistenceUtilsElasticSearch;

import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESInsertService {

	@Autowired
	ESBaseApi connector;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private ObjectMapper mapper;

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

	private String fixPosibleNonCapitalizedGeometryPoint(String s, String schema, String index) {
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

	public ComplexWriteResult load(String index, String type, List<String> jsonDoc, String jsonSchema) {

		final List<BulkWriteResult> listResult = new ArrayList<>();

		final List<Index> list = new ArrayList<>();
		for (String s : jsonDoc) {

			s = s.replaceAll("\\n", "");
			s = s.replaceAll("\\r", "");

			s = fixPosibleNonCapitalizedGeometryPoint(s, jsonSchema, index);
			s = fixPosibleDollarDates(s, jsonSchema, index);
			final Index i = new Index.Builder(s).index(index).type(type).build();
			list.add(i);
		}

		final Bulk bulk = new Bulk.Builder().addAction(list).build();
		BulkResult result;
		try {
			result = connector.getHttpClient().execute(bulk);

			final JsonArray object = result.getJsonObject().get("items").getAsJsonArray();

			for (int i = 0; i < object.size(); i++) {
				final JsonElement element = object.get(i);
				final JsonObject o = element.getAsJsonObject();
				final JsonObject the = o.get("index").getAsJsonObject();
				final String id = the.get("_id").getAsString();
				final String created = the.get("result").getAsString();

				final BulkWriteResult bulkr = new BulkWriteResult();
				bulkr.setId(id);
				bulkr.setErrorMessage(created);
				bulkr.setOk(true);
				listResult.add(bulkr);
			}
		} catch (final IOException e) {
			log.error("Error executing http request " + e.getMessage(), e);
		} catch (final Exception e) {
			log.error("Error Loading document " + e.getMessage(), e);
		}

		log.info("Documents have been inserted..." + listResult.size());

		ComplexWriteResult complexWriteResult = new ComplexWriteResult();
		complexWriteResult.setType(ComplexWriteResultType.BULK);
		complexWriteResult.setData(listResult);

		return complexWriteResult;

	}

	public static String readAllBytes(String filePath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
		return content;
	}

	public static List<String> readLines(File file) {
		if (!file.exists()) {
			return new ArrayList<>();
		}

		final List<String> results = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file));) {

			String line = reader.readLine();
			while (line != null) {
				results.add(line);
				line = reader.readLine();
			}
			return results;
		} catch (final Exception e) {
			return new ArrayList<>();
		}

	}

}
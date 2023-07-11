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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.persistence.ElasticsearchEnabledCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(ElasticsearchEnabledCondition.class)
@Slf4j
public class ESInsertService {

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
	private static final String SOURCE = "source";
	private static final String PATTERN_SEPARATOR = "-";

	private String fixPosibleNonCapitalizedGeometryPoint(String s) {
		try {
			final JsonObject o = new JsonParser().parse(s).getAsJsonObject();
			if (o.get(SOURCE) != null && o.get(SOURCE).getAsString().equals("AUDIT")) {
				return s;
			}
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

	private BulkResponse executeBulkInsert(BulkRequest bulkRequest) {
		try {
			return hlClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		} catch (final IOException e) {
			log.error("Error inserting bulk documents ", e);
			return null;
		}
	}

	public ComplexWriteResult bulkInsert(OntologyElastic ontology, List<String> jsonDocs) {
		final BulkRequest bulkRequest = new BulkRequest();

		for (String s : jsonDocs) {
			// fix instance
			s = s.replace("\\n", "");
			s = s.replace("\\r", "");
			s = fixPosibleNonCapitalizedGeometryPoint(s);

			// Get index and ID if defined
			String index = ontology.getOntologyId().getIdentification().toLowerCase();
			String idValue;

			// Default indexRequest. This can change for templates and/or custom IDs
			final IndexRequest indexRequest = new IndexRequest(index).source(s, XContentType.JSON);

			if (Boolean.TRUE.equals(ontology.getCustomIdConfig())
					|| Boolean.TRUE.equals(ontology.getTemplateConfig())) {
				// We need to parse the instance to extract the index or the ID
				final JsonObject instanceObject = new JsonParser().parse(s).getAsJsonObject();
				if (Boolean.TRUE.equals(ontology.getTemplateConfig())) {
					index = getIndexFromInstance(ontology, instanceObject);
					// IF template, then set the index according to the instance
					indexRequest.index(index);
				}

				if (Boolean.TRUE.equals(ontology.getCustomIdConfig())) {
					idValue = getValueFromInstanceField(instanceObject, ontology.getIdField());
					// IF custom ID, set the ID Value and UPSERT

					indexRequest.id(idValue)
							.create(ontology.getAllowsUpsertById() == null ? true : !ontology.getAllowsUpsertById());
				}
			}

			// Create Request
			bulkRequest.add(indexRequest);
		}

		final BulkResponse bulkResponse = executeBulkInsert(bulkRequest);

		final List<BulkWriteResult> listResult = new ArrayList<>();

		for (final BulkItemResponse bulkItemResponse : bulkResponse) {
			final BulkWriteResult bulkr = new BulkWriteResult();
			bulkr.setId(bulkItemResponse.getId());

			final DocWriteResponse itemResponse = bulkItemResponse.getResponse();
			if (itemResponse == null || bulkItemResponse.getFailure() != null) {
				// there is an error
				bulkr.setErrorMessage(bulkItemResponse.getFailureMessage());
				bulkr.setOk(false);
			} else {
				bulkr.setErrorMessage(itemResponse.toString());
				bulkr.setOk(itemResponse.getResult().equals(Result.CREATED)
						|| itemResponse.getResult().equals(Result.UPDATED));
			}
			listResult.add(bulkr);
		}

		final ComplexWriteResult complexWriteResult = new ComplexWriteResult();
		complexWriteResult.setType(ComplexWriteResultType.BULK);
		complexWriteResult.setData(listResult);

		return complexWriteResult;

	}

	private String getIndexFromInstance(OntologyElastic ontology, JsonObject instanceObject) {
		String index = "";
		DateTime dateTime = null;
		final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
				.withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC());
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ontology.getOntologyId().getIdentification()).append(PATTERN_SEPARATOR);

		// get value from field
		String fieldValue = getValueFromInstanceField(instanceObject, ontology.getPatternField());

		switch (ontology.getPatternFunction()) {
		case NONE:
			index = stringBuilder.append(fieldValue).toString().toLowerCase();
			break;
		case SUBSTR:
			if (fieldValue.length() <= ontology.getSubstringEnd()) {
				// TODO: THrow exception out of bounds index
			}
			final int endIndex = ontology.getSubstringEnd() == -1 ? fieldValue.length() : ontology.getSubstringEnd();
			fieldValue = fieldValue.substring(ontology.getSubstringStart(), endIndex);
			index = stringBuilder.append(fieldValue).toString().toLowerCase();
			break;
		case YEAR:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(dateTime.getYear()).toString().toLowerCase();
			break;
		case YEAR_MONTH:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(dateTime.getYear()).append(PATTERN_SEPARATOR)
					.append(String.format("%02d", dateTime.getMonthOfYear())).toString().toLowerCase();
			break;
		case YEAR_MONTH_DAY:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(dateTime.getYear()).append(PATTERN_SEPARATOR)
					.append(String.format("%02d", dateTime.getMonthOfYear())).append(PATTERN_SEPARATOR)
					.append(String.format("%02d", dateTime.getDayOfMonth())).toString().toLowerCase();
			break;
		case MONTH:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(String.format("%02d", dateTime.getMonthOfYear())).toString().toLowerCase();
			break;
		case DAY:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(String.format("%03d", dateTime.getDayOfYear())).toString().toLowerCase();
			break;
		default:
			// TODO: Throw exception
		}
		return index;
	}

	private String getValueFromInstanceField(JsonObject instanceObject, String field) {
		final String[] fields = field.split("\\.");
		for (int i = 0; i < fields.length - 1; i++) {
			instanceObject = instanceObject.getAsJsonObject(fields[i]);
		}
		return instanceObject.get(fields[fields.length - 1]).getAsString();

	}

}
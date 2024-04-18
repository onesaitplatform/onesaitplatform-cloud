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
package com.minsait.onesait.platform.persistence.opensearch.api;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.core.bulk.UpdateOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.persistence.OpensearchEnabledCondition;

import jakarta.json.spi.JsonProvider;
import lombok.extern.slf4j.Slf4j;

@Service
@Conditional(OpensearchEnabledCondition.class)
@Slf4j
public class OSInsertService {

	@Autowired
	private OpenSearchClient javaClient;

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
			return javaClient.bulk(bulkRequest);
		} catch (final IOException e) {
			log.error("Error inserting bulk documents ", e);
			return null;
		}
	}

	public ComplexWriteResult bulkInsert(OntologyElastic ontology, List<String> jsonDocs) {

		final List<BulkOperation> bulkOpsList = new ArrayList<>();
		for (String s : jsonDocs) {
			// fix instance
			s = s.replace("\\n", "");
			s = s.replace("\\r", "");
			s = fixPosibleNonCapitalizedGeometryPoint(s);
			// Get index and ID if defined
			String index = ontology.getOntologyId().getIdentification().toLowerCase();
			String idValue;

			// Default indexRequest. This can change for templates and/or custom IDs
			JsonpMapper jsonpMapper = javaClient._transport().jsonpMapper();
			JsonProvider jsonProvider = jsonpMapper.jsonProvider();
			Reader reader = new StringReader(s);
			JsonData jd = JsonData.from(jsonProvider.createParser(reader), jsonpMapper);

			IndexOperation.Builder<JsonData> indexOpBuilder = new IndexOperation.Builder<JsonData>().document(jd)
					.index(index);
			UpdateOperation.Builder<JsonData> updateOpBuilder = null;

			if (Boolean.TRUE.equals(ontology.getCustomIdConfig())
					|| Boolean.TRUE.equals(ontology.getTemplateConfig())) {
				// We need to parse the instance to extract the index or the ID
				final JsonObject instanceObject = new JsonParser().parse(s).getAsJsonObject();
				if (Boolean.TRUE.equals(ontology.getTemplateConfig())) {
					index = OSTemplateHelper.getIndexFromInstance(ontology, instanceObject);
					// IF template, then set the index according to the instance
					indexOpBuilder.index(index);
				}

				if (Boolean.TRUE.equals(ontology.getCustomIdConfig())) {
					idValue = OSTemplateHelper.getValueFromInstanceField(instanceObject, ontology.getIdField());
					// IF custom ID, set the ID Value and UPSERT
					if (ontology.getAllowsUpsertById() == null ? false : ontology.getAllowsUpsertById()) {
						// requires upsert by id as it is a different BulkOperation Object
						updateOpBuilder = new UpdateOperation.Builder<JsonData>().id(idValue).docAsUpsert(true)
								.document(jd).index(index);
					}
				}
			}

			// Create Request
			if (updateOpBuilder == null) {
				bulkOpsList.add(new BulkOperation.Builder().index(indexOpBuilder.build()).build());
			} else {
				bulkOpsList.add(new BulkOperation.Builder().update(updateOpBuilder.build()).build());
			}
		}
		final BulkRequest bulkReq = new BulkRequest.Builder().operations(bulkOpsList).build();
		final BulkResponse bulkResponse = executeBulkInsert(bulkReq);

		final List<BulkWriteResult> listResult = new ArrayList<>();

		for (final BulkResponseItem bulkItemResponse : bulkResponse.items()) {
			final BulkWriteResult bulkr = new BulkWriteResult();
			bulkr.setId(bulkItemResponse.id());
			bulkItemResponse.result();
			if (bulkItemResponse == null || bulkItemResponse.error() != null) {
				// there is an error
				bulkr.setErrorMessage(bulkItemResponse.error().reason());
				bulkr.setOk(false);
			} else {
				bulkr.setErrorMessage(bulkItemResponse.result());
				// check if status is a valid http response status code
				bulkr.setOk(bulkItemResponse.status() >= 200 && bulkItemResponse.status() <= 299);
			}
			listResult.add(bulkr);
		}

		final ComplexWriteResult complexWriteResult = new ComplexWriteResult();
		complexWriteResult.setType(ComplexWriteResultType.BULK);
		complexWriteResult.setData(listResult);

		return complexWriteResult;

	}
}

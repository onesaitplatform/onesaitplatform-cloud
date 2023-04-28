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
package com.minsait.onesait.platform.persistence.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.mustachejava.Mustache;
import com.google.gson.Gson;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.persistence.services.util.MustacheUtil;
import com.minsait.onesait.platform.persistence.services.util.WktToGeoJsonConverter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeoSpatialOpsService {

	private static final String DEFAULT_FIELD = "geometry";
	private static final String CLASSPATH_MUSTACHE_TEMPLATES_MUSTACHE = "classpath*:mustache-templates/*.mustache";
	private static final String ONTOLOGY_STR = "ontology";
	private static final String FIELD_STR = "field";

	public enum GeoQueries {
		INTERSECTS, NEAR, WITHIN
	}

	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsFacade;

	@Autowired
	private ApplicationContext applicationContext;

	private Map<String, Mustache> templates = new ConcurrentHashMap<>();

	@PostConstruct
	private void context() {

		try {
			Resource[] resource = applicationContext.getResources(CLASSPATH_MUSTACHE_TEMPLATES_MUSTACHE);
			if (resource != null) {
				for (Resource r : resource) {
					log.info("Loading Mustache Template :" + r.getFilename() + " " + r.getURL());
					loadTemplate(r.getInputStream(), r.getFilename());
				}
			}
		} catch (Exception e) {
		    log.error("" + e);
		}
	}

	public void loadTemplate(InputStream is, String name) {
		Reader targetReader = new InputStreamReader(is);
		try {
			Mustache m = MustacheUtil.getMustacheFactory().compile(targetReader, name);
			templates.put(name, m);
			log.info("Template Definition Loaded: " + name);
		} catch (Exception e) {
			log.error("Something happens loading template ", e);
		}
	}

	public String getKey(String ontology, GeoQueries type) {
		RtdbDatasource dataSource = basicOpsFacade.getOntologyDataSource(ontology);
		return "geo." + type.name() + "." + dataSource.name().toLowerCase() + ".mustache";
	}

	public Mustache getTemplate(String ontology, GeoQueries type) {
		return templates.get(getKey(ontology, type));
	}

	private String convertGeoJsonPartialtoNativeWithOntology(String partial, String ontology) {
		RtdbDatasource dataSource = basicOpsFacade.getOntologyDataSource(ontology);
		return convertGeoJsonPartialtoNative(partial, dataSource);
	}

	private String convertGeoJsonPartialtoNative(String partial, RtdbDatasource dataSource) {

		Geometry g = new Gson().fromJson(partial, Geometry.class);
		double[][][] coordinates = g.geometry.coordinates;
		String type = g.geometry.type;

		String partialConverted = "";

		if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH)) {
			partialConverted = String.format("{\"type\":\"%s\", \"coordinates\": %s}", type.toLowerCase(),
					Arrays.deepToString(coordinates));
			return partialConverted;
		} else if (dataSource.equals(RtdbDatasource.MONGO)) {
			partialConverted = String.format("type:\"%s\", coordinates: %s", StringUtils.capitalize(type),
					Arrays.deepToString(coordinates));
			return partialConverted;
		} else {
			return WktToGeoJsonConverter.toGeoJson(partial);
		}
	}

	public List<String> near(String ontology, String maxDistance, String latitude, String longitude)
			throws IOException {
		Map<String, Object> context = new HashMap<>();
		context.put(ONTOLOGY_STR, ontology);
		context.put(FIELD_STR, DEFAULT_FIELD);
		context.put("maxDistance", maxDistance);
		context.put("latitude", latitude);
		context.put("longitude", longitude);

		Mustache m = getTemplate(ontology, GeoQueries.NEAR);
		String query = MustacheUtil.executeTemplate(m, context);

		return basicOpsFacade.queryNative(ontology, query);
	}

	public List<String> intersects(String ontology, String partial) throws IOException {
		return geoShapeQuery(ontology, DEFAULT_FIELD, partial, GeoQueries.INTERSECTS);
	}

	public List<String> within(String ontology, String partial) throws IOException {
		return geoShapeQuery(ontology, DEFAULT_FIELD, partial, GeoQueries.WITHIN);
	}

	private List<String> geoShapeQuery(String ontology, String field, String partial, GeoQueries type)
			throws IOException {
		Map<String, Object> context = new HashMap<>();
		context.put(ONTOLOGY_STR, ontology);
		context.put(FIELD_STR, field);

		String convertedPartial = convertGeoJsonPartialtoNativeWithOntology(partial, ontology);

		context.put("partial", convertedPartial);

		Mustache m = getTemplate(ontology, type);
		String query = MustacheUtil.executeTemplate(m, context);

		return basicOpsFacade.queryNative(ontology, query);
	}

	public String getQuery(GeoQueries type, String ontology, String field, String latitude, String longitude,
			String maxDistance) throws IOException {
		Map<String, Object> context = new HashMap<>();
		context.put(ONTOLOGY_STR, ontology);
		context.put(FIELD_STR, field);
		context.put("maxDistance", maxDistance);
		context.put("latitude", latitude);
		context.put("longitude", longitude);

		Mustache m = getTemplate(ontology, type);
		return MustacheUtil.executeTemplate(m, context);
	}

	public String getQuery(GeoQueries type, String ontology, String field, String partial) throws IOException {
		Map<String, Object> context = new HashMap<>();
		context.put(ONTOLOGY_STR, ontology);
		context.put(FIELD_STR, field);

		String convertedPartial = convertGeoJsonPartialtoNativeWithOntology(partial, ontology);

		context.put("partial", convertedPartial);

		Mustache m = getTemplate(ontology, type);
		return MustacheUtil.executeTemplate(m, context);
	}

	class Geometry {
		GeometryData geometry;

		@Override
		public String toString() {
			return "Geometry [geometry=" + geometry + "]";
		}
	}

	class GeometryData {
		String type;
		double[][][] coordinates;

		@Override
		public String toString() {
			return "GeometryData [type=" + type + ", coordinates=" + ArrayUtils.toString(coordinates) + "]";
		}
	}

}

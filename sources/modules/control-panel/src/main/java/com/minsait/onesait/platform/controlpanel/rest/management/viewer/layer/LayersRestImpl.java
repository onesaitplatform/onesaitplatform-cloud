/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.gis.layer.LayerService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.Geometry;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.GeometryLinestring;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.GeometryMultiLineString;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.GeometryMultiPolygon;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.GeometryPoint;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.GeometryPolygon;
import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.GeometryType;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

@RestController
@EnableAutoConfiguration
@Slf4j
public class LayersRestImpl implements LayersRest {

	private static final String FEATURE = "Feature";
	private static final String FEATURES = "features";
	private static final String FEATURE_COLLECTION = "FeatureCollection";
	private static final String PARAM = "param";
	private static final String TYPE = "type";
	private static final String STRING = "STRING";
	private static final String DATE = "DATE";
	private static final String NUMBER = "NUMBER";
	private static final String BOOLEAN = "BOOLEAN";
	private static final String WRONG_PARAMETER_TYPE = "com.indra.sofia2.api.service.wrongparametertype";
	private static final String RASTER = "raster";
	private static final String ATTRIBUTE = "attribute";
	private static final String STRINGLITERAL = "#%02x%02x%02x";
	private static final String ADMINISTRATOR = "administrator";
	private static final String LINE_STRING = "LineString";
	private static final String POLYLINE = "Polyline";
	private static final String COORDINATES = "coordinates";

	private Map<String, String> mapFields;

	@Autowired
	private UserService userService;

	@Autowired
	private LayerService layerService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	private ManageDBRepositoryFactory manageFactory;

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private String defaultQuery;

	@PostConstruct
	private void postConstruct() {
		boolean quasarActive = ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
				.get("mongodb-use-quasar")).booleanValue();

		defaultQuery = "select * from ";
		if (quasarActive)
			defaultQuery = "select _id,c from ";
	}

	@Override
	public ResponseEntity<?> getLayerData(HttpServletRequest request) {

		mapFields = new HashMap<>();
		HeatMap heatMap = new HeatMap();
		List<Feature> featureList = new ArrayList<>();
		User user = userService.getUser(ADMINISTRATOR);

		FeatureCollection featureCollection = new FeatureCollection();
		try {

			String layerIdentification = request.getParameter("layer");
			Layer layer = layerService.getLayerByIdentification(layerIdentification, user);

			if (layer != null && (layer.getUser().equals(user) || userService.isUserAdministrator(user))) {

				String root = getRootField(layer);
				String features = null;

				String query = layer.getQuery();
				if (query != null) {
					try {
						query = buildQuery(query, layer, request);
						if (query == null) {
							return new ResponseEntity<>("Error building the query " + layer.getQuery(),
									HttpStatus.BAD_REQUEST);
						}
						if (!query.contains("{$")) {
							features = runQuery(ADMINISTRATOR, layer.getOntology().getIdentification(), query);
						} else {
							return new ResponseEntity<>(
									"Missing query parameters to execute the query of the layer " + layerIdentification,
									HttpStatus.BAD_REQUEST);
						}

					} catch (BadRequestException e) {
						return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
					}
				} else {
					features = runQuery(ADMINISTRATOR, layer.getOntology().getIdentification(), null);
				}

				JSONArray jsonArray = new JSONArray(features);
				if (jsonArray != null && jsonArray.length() > 0) {
					String geometryField = layer.getGeometryField().split("\\.")[0];

					if (jsonArray.getJSONObject(0).has(TYPE)
							&& jsonArray.getJSONObject(0).getString(TYPE).equals(FEATURE_COLLECTION)) {
						return new ResponseEntity<>(this.buildFeatureCollection(layer, jsonArray), HttpStatus.OK);
					}
					Boolean includeAllProperties = getPropertiesForFeatures(query, layer, ADMINISTRATOR);

					for (Integer i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);

						JSONObject rootObject = null;

						Feature feature = new Feature();

						String fieldGeometry = null;
						if (obj.has("_id")) {
							String oid;
							try {
								oid = obj.getString("_id");
							} catch (JSONException e) {
								oid = obj.getJSONObject("_id").getString("$oid");
							}
							feature.setOid(oid);
						} else {
							ObjectId objOid = new ObjectId();
							String oid = objOid.toString();
							feature.setOid(oid);
						}
						OntologyVirtual virtual = ontologyService.getOntologyVirtualByOntologyId(layer.getOntology());
						if (virtual != null && obj.has(virtual.getObjectGeometry())) {
							rootObject = obj;
							fieldGeometry = virtual.getObjectGeometry();
						} else if (!includeAllProperties) {
							// Have Query with select params
							rootObject = obj;
							fieldGeometry = mapFields.get(geometryField);
						} else {
							rootObject = root != null ? obj.getJSONObject(root) : obj;
							fieldGeometry = geometryField;
						}

						if (fieldGeometry == null) {
							return new ResponseEntity<>("No property geometry found.", HttpStatus.BAD_REQUEST);
						}
						JSONArray geo = null;
						if (virtual != null) {
							JSONObject geoObject = new JSONObject(rootObject.getString(fieldGeometry));
							geo = geoObject.getJSONArray(COORDINATES);
						} else {
							geo = rootObject.getJSONObject(fieldGeometry).getJSONArray(COORDINATES);
						}

						Geometry geometry = this.buildGeometry(layer, geo);
						feature.setGeometry(geometry);

						Map<String, String> mapProperties = this.buildProperties(layer, rootObject);

						feature.setProperties(mapProperties);
						feature.setType(FEATURE);

						featureList.add(feature);

					}
				}

				if (layer.isHeatMap()) {
					heatMap.setRadius(layer.getHeatMapRadius());
					heatMap.setMax(layer.getHeatMapMax());
					heatMap.setMin(layer.getHeatMapMin());
				}

				Symbology symbology = buildSymbology(layer);

				featureCollection.setHeatMap(heatMap);
				featureCollection.setName(layer.getIdentification());
				featureCollection.setSymbology(symbology);
				featureCollection.setType(FEATURE_COLLECTION);
				if (layer.getGeometryType().equals("LineString")) {
					featureCollection.setTypeGeometry("Polyline");
				} else {
					featureCollection.setTypeGeometry(layer.getGeometryType());
				}

				featureCollection.setFeatures(featureList);

				return new ResponseEntity<>(featureCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Layer not found for this user.", HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			log.error("Error processing request", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private Geometry buildGeometry(Layer layer, JSONArray geo) {
		if ((layer.getGeometryType().equalsIgnoreCase(GeometryType.POINT.getName()))
				|| layer.getGeometryType().equalsIgnoreCase(RASTER)) {

			GeometryPoint geometry = new GeometryPoint();
			List<Double> list = new ArrayList<>();

			for (int y = 0; y < geo.length(); y++) {
				list.add(geo.getDouble(y));
			}

			geometry.setCoordinates(list.toArray(new Double[list.size()]));
			return geometry;
		} else if (layer.getGeometryType().equalsIgnoreCase(GeometryType.POLYGON.getName())) {

			GeometryPolygon geometry = new GeometryPolygon();
			List<List<Double[]>> geoFinal = new ArrayList<>();

			for (int y = 0; y < geo.length(); y++) {
				JSONArray geoAux = geo.getJSONArray(y);
				List<Double[]> listAux = new ArrayList<>();
				for (int z = 0; z < geoAux.length(); z++) {
					JSONArray g = geoAux.getJSONArray(z);
					List<Double> listDouble = new ArrayList<>();
					for (int x = 0; x < g.length(); x++) {
						listDouble.add(g.getDouble(x));
					}
					listAux.add(listDouble.toArray(new Double[listDouble.size()]));
				}
				geoFinal.add(listAux);
			}

			geometry.setCoordinates(geoFinal);
			return geometry;
		} else if (layer.getGeometryType().equalsIgnoreCase(GeometryType.MULTI_POLYGON.getName())) {

			GeometryMultiPolygon geometry = new GeometryMultiPolygon();
			List<List<List<Double[]>>> geoFinal = new ArrayList<>();

			for (int y = 0; y < geo.length(); y++) {
				JSONArray geoAux = geo.getJSONArray(y);
				List<List<Double[]>> listAux = new ArrayList<>();
				for (int z = 0; z < geoAux.length(); z++) {
					JSONArray g = geoAux.getJSONArray(z);
					List<Double[]> listAuxBis = new ArrayList<>();
					for (int x = 0; x < g.length(); x++) {
						JSONArray h = g.getJSONArray(x);
						List<Double> listDouble = new ArrayList<>();
						for (int t = 0; t < h.length(); t++) {
							listDouble.add(h.getDouble(t));
						}
						listAuxBis.add(listDouble.toArray(new Double[listDouble.size()]));
					}
					listAux.add(listAuxBis);
				}
				geoFinal.add(listAux);
			}

			geometry.setCoordinates(geoFinal);
			return geometry;
		} else if (layer.getGeometryType().equalsIgnoreCase(GeometryType.LINE_STRING.getName())) {

			GeometryLinestring geometry = new GeometryLinestring();
			List<Double[]> geoFinal = new ArrayList<>();

			for (int y = 0; y < geo.length(); y++) {
				JSONArray geoAux = geo.getJSONArray(y);

				List<Double> listDouble = new ArrayList<>();
				for (int x = 0; x < geoAux.length(); x++) {
					listDouble.add(geoAux.getDouble(x));
				}
				geoFinal.add(listDouble.toArray(new Double[listDouble.size()]));

			}

			geometry.setCoordinates(geoFinal);
			return geometry;
		} else if (layer.getGeometryType().equalsIgnoreCase(GeometryType.MULTILINE_STRING.getName())) {

			GeometryMultiLineString geometry = new GeometryMultiLineString();
			List<List<Double[]>> geoFinal = new ArrayList<>();

			for (int z = 0; z < geo.length(); z++) {
				JSONArray g = geo.getJSONArray(z);
				List<Double[]> listAuxBis = new ArrayList<>();
				for (int x = 0; x < g.length(); x++) {
					JSONArray h = g.getJSONArray(x);
					List<Double> listDouble = new ArrayList<>();
					for (int t = 0; t < h.length(); t++) {
						listDouble.add(h.getDouble(t));
					}
					listAuxBis.add(listDouble.toArray(new Double[listDouble.size()]));
				}
				geoFinal.add(listAuxBis);
			}

			geometry.setCoordinates(geoFinal);
			return geometry;
		}
		return null;
	}

	private String buildQuery(String query, Layer layer, HttpServletRequest request) {
		JSONArray jsonParams;
		try {
			jsonParams = new JSONArray(layer.getQueryParams());

			Map<String, String[]> paramsMap = request.getParameterMap();
			for (Entry<String, String[]> entry : paramsMap.entrySet()) {
				String param = entry.getKey();
				String value = entry.getValue()[0];
				JSONObject obj = null;
				if (jsonParams.length() != 0) {
					obj = jsonParams.getJSONObject(0);

					if (obj.getString(PARAM).equals(param)) {
						String type = obj.getString(TYPE);
						if (type.equals(DATE)) {
							try {
								final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
								df.parse(value);
								value = "\"" + value + "\"";
							} catch (final Exception e) {
								throw new BadRequestException(
										"com.indra.sofia2.api.service.wrongparametertype " + param);
							}
						} else if (type.equals(STRING)) {
							try {
								value = "\"" + value + "\"";
							} catch (final Exception e) {
								throw new BadRequestException(WRONG_PARAMETER_TYPE + param);
							}
						} else if (type.equals(NUMBER)) {
							try {
								Double.parseDouble(value);
							} catch (final Exception e) {
								throw new BadRequestException(WRONG_PARAMETER_TYPE + param);
							}
						} else if (type.equals(BOOLEAN)
								&& (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))) {
							throw new BadRequestException(WRONG_PARAMETER_TYPE + param);
						}

						query = query.replace("{$" + param + "}", value);
					}
				}
			}

			return query;
		} catch (JSONException e1) {
			log.error("Error building the query for layer: {}", layer.getIdentification(), e1.getMessage());
			return null;
		}
	}

	private Boolean getPropertiesForFeatures(String query, Layer layer, String userId) {
		Boolean includeAllProperties = true;
		try {
			if (query == null) {
				query = "select c from " + layer.getOntology().getIdentification() + " as c";
			}
			CCJSqlParserManager parserManager = new CCJSqlParserManager();
			Select select = (Select) parserManager.parse(new StringReader(query));

			PlainSelect plain = (PlainSelect) select.getSelectBody();
			List<SelectItem> selectItems = plain.getSelectItems();

			for (SelectItem item : selectItems) {
				if (!item.toString().equals("c") && !item.toString().equals("*")) {
					includeAllProperties = false;
					String[] split = item.toString().split("AS");

					Expression expression = ((SelectExpressionItem) item).getExpression();

					Column col = (Column) expression;

					mapFields.put(col.getColumnName(), split[1].trim());
				} else {
					Map<String, String> mapFields = ontologyService
							.getOntologyFields(layer.getOntology().getIdentification(), userId);

					for (Map.Entry<String, String> entry : mapFields.entrySet()) {
						String field = entry.getKey();
						if (!field.contains(".")) {
							this.mapFields.put(field, field);
						} else {
							String fieldAux = field.split("\\.")[0];
							if (!this.mapFields.containsKey(fieldAux)) {
								this.mapFields.put(fieldAux, fieldAux);
							}
						}
					}
				}
			}
		} catch (JSQLParserException e) {
			log.error("Error parsing the query for layer: {}", layer.getIdentification(), e);

		} catch (IOException e) {
			log.error("Error getting ontology fields of the ontology: {}", layer.getOntology().getIdentification(),
					e.getMessage());

		}
		return includeAllProperties;
	}

	private Symbology buildSymbology(Layer layer) {
		Symbology symbology = new Symbology();
		try {
			String innerColor = layer.getInnerColor();
			ColorRGB innerColorRGB = new ColorRGB();
			String innerColorHex = null;
			String innerColorAlpha = null;
			if (innerColor.startsWith("#")) {
				innerColorHex = innerColor;
				Color color = Color.decode(innerColor);

				innerColorRGB.setBlue(color.getBlue());
				innerColorRGB.setRed(color.getRed());
				innerColorRGB.setGreen(color.getGreen());
				innerColorAlpha = "0.99";
			} else if (innerColor.startsWith("rgb")) {
				innerColor = innerColor.substring(innerColor.indexOf('(') + 1, innerColor.indexOf(')'));
				String[] split = innerColor.split(",");
				innerColorHex = String.format(STRINGLITERAL, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
						Integer.parseInt(split[2]));
				innerColorAlpha = split[3];

				Color color = Color.decode(innerColorHex);

				innerColorRGB.setBlue(color.getBlue());
				innerColorRGB.setRed(color.getRed());
				innerColorRGB.setGreen(color.getGreen());

			}

			String outerColor = layer.getOuterColor();
			ColorRGB outerColorRGB = new ColorRGB();
			String outerColorHex = null;
			String outerColorAlpha = null;
			if (outerColor.startsWith("#")) {
				outerColorHex = outerColor;
				Color color = Color.decode(outerColor);

				outerColorRGB.setBlue(color.getBlue());
				outerColorRGB.setRed(color.getRed());
				outerColorRGB.setGreen(color.getGreen());
				outerColorAlpha = "0.99";
			} else if (outerColor.startsWith("rgb")) {
				outerColor = outerColor.substring(outerColor.indexOf('(') + 1, outerColor.indexOf(')'));
				String[] split = outerColor.split(",");
				outerColorHex = String.format(STRINGLITERAL, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
						Integer.parseInt(split[2]));
				outerColorAlpha = split[3];

				Color color = Color.decode(outerColorHex);

				outerColorRGB.setBlue(color.getBlue());
				outerColorRGB.setRed(color.getRed());
				outerColorRGB.setGreen(color.getGreen());

			}

			if (layer.getGeometryType().equals("Polygon")) {
				symbology.setName("simbPolygonBasic");
			} else if (layer.getGeometryType().equals("LineString")) {
				symbology.setName("simbPolylineBasic");
			} else if (layer.getGeometryType().equals("Point")) {
				symbology.setName("simbPointBasic");
			}

			symbology.setPixelSize(layer.getSize());
			symbology.setInnerColorAlpha(innerColorAlpha);
			symbology.setInnerColorHEX(innerColorHex);
			symbology.setInnerColorRGB(innerColorRGB);
			symbology.setOutlineColorHEX(outerColorHex);
			symbology.setOutlineColorRGB(outerColorRGB);
			symbology.setOuterColorAlpha(outerColorAlpha);
			symbology.setPixelSize(layer.getSize());
			symbology.setOutlineWidth(layer.getOuterThin());

			if (layer.getFilters() != null) {
				JSONArray filters = new JSONArray(layer.getFilters());
				List<Filter> filtersList = new ArrayList<>();

				for (int i = 0; i < filters.length(); i++) {
					JSONObject filterObj = filters.getJSONObject(i);
					String c = filterObj.getString("color");
					String colorHex = null;
					ColorRGB colorRGB = new ColorRGB();
					if (c.startsWith("#")) {
						colorHex = c;
						Color color = Color.decode(c);

						colorRGB.setBlue(color.getBlue());
						colorRGB.setRed(color.getRed());
						colorRGB.setGreen(color.getGreen());
					} else if (c.startsWith("rgb")) {
						c = c.substring(c.indexOf('(') + 1, c.indexOf(')'));
						String[] split = c.split(",");
						colorHex = String.format(STRINGLITERAL, Integer.parseInt(split[0]), Integer.parseInt(split[1]),
								Integer.parseInt(split[2]));

						Color color = Color.decode(colorHex);

						colorRGB.setBlue(color.getBlue());
						colorRGB.setRed(color.getRed());
						colorRGB.setGreen(color.getGreen());

					}

					String operation = filterObj.getString("operation");
					String infobox = layer.getInfoBox();
					if (infobox != null) {
						JSONArray array = new JSONArray(infobox);

						for (int x = 0; x < array.length(); x++) {
							JSONObject obj = array.getJSONObject(x);
							String field = obj.getString("field");
							String attribute = obj.getString(ATTRIBUTE);

							operation = checkOperation(operation, field, attribute);

						}

					}

					Filter filter = new Filter();

					filter.setOperation(filterObj.getString("operation"));
					filter.setColorRGB(colorRGB);
					filter.setColorHEX(colorHex);
					filter.setOperation(operation);

					filtersList.add(filter);
				}
				symbology.setFilters(filtersList);
			} else {
				symbology.setFilters(new ArrayList<Filter>());
			}

		} catch (Exception e) {
			return null;
		}
		return symbology;

	}

	private String checkOperation(String operation, String field, String attribute) {
		if (operation.contains("==")) {
			String[] split = operation.split("==");
			String fieldAux = split[0];
			String value = split[1];
			if (field.equals(fieldAux)) {
				operation = attribute + "==" + value;
			}
		} else if (operation.contains("!=")) {
			String[] split = operation.split("!=");
			String fieldAux = split[0];
			String value = split[1];
			if (field.equals(fieldAux)) {
				operation = attribute + "!=" + value;
			}
		} else if (operation.contains(">")) {
			String[] split = operation.split(">");
			String fieldAux = split[0];
			String value = split[1];
			if (field.equals(fieldAux)) {
				operation = attribute + ">" + value;
			}
		}
		return operation;
	}

	private String getRootField(Layer layer) {
		try {
			String schema = layer.getOntology().getJsonSchema();
			JSONObject jsonschema = new JSONObject(schema);
			Iterator<String> iterator = jsonschema.keys();
			String root = null;
			while (iterator.hasNext()) {
				String prop = iterator.next();
				try {
					Iterator<String> iteratorAux = jsonschema.getJSONObject(prop).keys();
					while (iteratorAux.hasNext()) {
						String p = iteratorAux.next();
						if (jsonschema.getJSONObject(prop).getJSONObject(p).has("$ref")) {
							root = p;
							break;
						}
					}
				} catch (Exception e) {
				}
			}

			return root;
		} catch (JSONException e) {
			return null;
		}
	}

	private String runQuery(String userId, String ontologyIdentification, String query)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {

		if (ontologyService.hasUserPermissionForQuery(userId, ontologyIdentification)) {
			final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
			if (manageDB.getListOfTables4Ontology(ontologyIdentification).isEmpty()) {
				manageDB.createTable4Ontology(ontologyIdentification, "{}", null);
			}
			String queryResult = null;
			if (query == null) {
				queryResult = queryToolService.querySQLAsJson(userId, ontologyIdentification,
						defaultQuery + ontologyIdentification + " as c", 0);
			} else {
				queryResult = queryToolService.querySQLAsJson(userId, ontologyIdentification, query, 0);
			}

			return queryResult;

		} else {
			return utils.getMessage("querytool.ontology.access.denied.json",
					"You don't have permissions for this ontology");
		}
	}

	private FeatureCollection buildFeatureCollection(Layer layer, JSONArray jsonArray) {
		HeatMap heatMap = new HeatMap();
		List<Feature> featureList = new ArrayList<>();
		FeatureCollection featureCollection = new FeatureCollection();
		if (layer.isHeatMap()) {
			heatMap.setRadius(layer.getHeatMapRadius());
			heatMap.setMax(layer.getHeatMapMax());
			heatMap.setMin(layer.getHeatMapMin());
		}
		Symbology symbology = buildSymbology(layer);

		featureCollection.setHeatMap(heatMap);
		featureCollection.setName(layer.getIdentification());
		featureCollection.setSymbology(symbology);
		featureCollection.setType(FEATURE_COLLECTION);
		if (layer.getGeometryType().equals(LINE_STRING)) {
			featureCollection.setTypeGeometry(POLYLINE);
		} else {
			featureCollection.setTypeGeometry(layer.getGeometryType());
		}
		ObjectId objOid = new ObjectId();
		String oid = objOid.toString();
		for (Integer i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);
			JSONArray featuresArray = obj.getJSONArray(FEATURES);
			for (Integer x = 0; x < featuresArray.length(); x++) {
				JSONObject objAux = featuresArray.getJSONObject(x);
				JSONArray geo = objAux.getJSONObject(layer.getGeometryField()).getJSONArray(COORDINATES);

				Geometry geometry = this.buildGeometry(layer, geo);

				JSONObject prop = objAux.getJSONObject("properties");
				Iterator<String> keys = prop.keys();
				Map<String, String> mapProperties = new HashMap<>();

				while (keys.hasNext()) {
					String key = keys.next();
					mapProperties.put(key, prop.get(key).toString());
				}

				Feature feature = new Feature(oid, geometry, mapProperties);
				featureList.add(feature);
			}
		}

		featureCollection.setFeatures(featureList);

		return featureCollection;
	}

	private Map<String, String> buildProperties(Layer layer, JSONObject rootObject) {
		Map<String, String> mapProperties = new HashMap<>();
		JSONObject rootObjectCopy = null;
		Boolean existInfoBox = false;

		if (layer.getInfoBox() != null) {
			JSONArray properties = new JSONArray(layer.getInfoBox());
			if (properties.length() != 0) {
				existInfoBox = true;
			}
		}
		if (!layer.isHeatMap() && existInfoBox) {

			JSONArray properties = new JSONArray(layer.getInfoBox());

			for (int x = 0; x < properties.length(); x++) {
				rootObjectCopy = rootObject;
				Object value = null;
				JSONObject json = properties.getJSONObject(x);

				String[] splitAux = json.getString("field").split("\\.");
				for (int j = 0; j < splitAux.length - 1; j++) {
					if (j + 1 == splitAux.length - 1) {
						try {
							JSONArray rootObjectArray = rootObjectCopy.getJSONArray(splitAux[j]);
							value = rootObjectArray.get(Integer.parseInt(splitAux[splitAux.length - 1]));
							mapProperties.put(json.getString(ATTRIBUTE), value.toString());
							break;
						} catch (Exception e) {
							log.error("Error mapping json, {}", e);
						}
					}
					rootObjectCopy = rootObject.getJSONObject(splitAux[j]);
				}
				if (value == null) {
					value = rootObjectCopy.get(splitAux[splitAux.length - 1]);
					mapProperties.put(json.getString(ATTRIBUTE), value.toString());
				}
			}
		} else if (layer.isHeatMap()) {
			String value = rootObject.get(layer.getWeightField()).toString();
			mapProperties.put("value", value);

		} else if (layer.getQuery() != null) {
			for (Map.Entry<String, String> entry : mapFields.entrySet()) {
				if (!entry.getValue().equals(layer.getGeometryField())) {

					Object value = rootObject.get(entry.getValue());
					mapProperties.put(entry.getValue(), value.toString());
				}
			}
		}
		return mapProperties;
	}

}

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
package com.minsait.onesait.platform.config.services.gis.layer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.exceptions.LayerServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@Service
public class LayerServiceImpl implements LayerService {

	@Autowired
	private UserService userService;

	@Autowired
	private LayerRepository layerRepository;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	OntologyService ontologyService;

	private static final String USER_NOT_AUTHORIZED = "The user is not authorized";
	private static final String PROPERTIES = "properties";
	private static final String URL_CONCAT = "{\"url\":\"";

	@Override
	public List<Layer> findAllLayers(String userId) {
		List<Layer> layers = null;
		final User sessionUser = userService.getUser(userId);

		if (userService.isUserAdministrator(sessionUser)) {
			layers = layerRepository.findAll();
		} else {
			layers = layerRepository.findByUserOrIsPublicTrue(sessionUser);
		}
		return layers;
	}

	@Override
	public List<String> getAllIdentificationsByUser(String userId) {
		List<String> layers = null;
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			layers = layerRepository.findIdentificationOrderByIdentificationAsc();
		} else {
			layers = layerRepository.findIdentificationByUserOrIsPublicTrue(user);
		}

//		final List<String> identifications = new ArrayList<>();
//		for (final Layer layer : layers) {
//			identifications.add(layer.getIdentification());
//
//		}
		return layers;
	}

	@Override
	public Ontology getOntologyByIdentification(String identification, String sessionUserId) {
		return ontologyRepository.findByIdentification(identification);
	}

	@Override
	public void create(Layer layer) {

		layerRepository.save(layer);
	}

	@Override
	public Boolean checkExist(String layerIdentification) {

		if (!layerRepository.findByIdentification(layerIdentification).isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public Layer findById(String id, String userId) {
		final User user = userService.getUser(userId);
		final Layer layer = layerRepository.findById(id).orElse(null);
		if (userService.isUserAdministrator(user) || layer.getUser().equals(user) || layer.isPublic()) {
			return layer;
		} else {
			throw new LayerServiceException(USER_NOT_AUTHORIZED);
		}
	}

	@Override
	public void deleteLayer(Layer layer, String userId) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user) || layer.getUser().equals(user) || layer.isPublic()) {
			if (!layer.getViewers().isEmpty()) {
				throw new LayerServiceException("This Layer is associated to a Viewer.");
			}
			layerRepository.delete(layer);
		} else {
			throw new LayerServiceException(USER_NOT_AUTHORIZED);
		}
	}

	@Override
	public Map<String, String> getOntologyGeometryFields(String identification, String sessionUserId)
			throws IOException {
		final Map<String, String> fields = new TreeMap<>();
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);

		if (ontology != null) {
			final OntologyVirtual virtual = ontologyService.getOntologyVirtualByOntologyId(ontology);
			final ObjectMapper mapper = new ObjectMapper();

			JsonNode jsonNode = null;
			try {

				jsonNode = mapper.readTree(ontology.getJsonSchema());

			} catch (final Exception e) {
				if (ontology.getJsonSchema().contains("'"))
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
			}
			if (virtual == null) {
				// Predefine Path to data properties

				if (!jsonNode.path("datos").path(PROPERTIES).isMissingNode()) {

					jsonNode = jsonNode.path("datos").path(PROPERTIES);

				} else
					jsonNode = jsonNode.path(PROPERTIES);

				final Iterator<String> iterator = jsonNode.fieldNames();
				String property;
				while (iterator.hasNext()) {
					Boolean hasCoordinates = false;
					Boolean hasType = false;
					property = iterator.next();
					if (!jsonNode.findPath("features").isMissingNode()) {
						fields.put("geometry", jsonNode.path("features").path("items").get(0).path("properties")
								.path("geometry").path("properties").path("type").get("enum").get(0).asText());
					} else {
						if (jsonNode.path(property).get("type").asText().equals("object")) {

							final JsonNode jsonNodeAux = jsonNode.path(property).path(PROPERTIES);

							if (!jsonNodeAux.path("coordinates").isMissingNode()
									&& jsonNodeAux.path("coordinates").get("type").asText().equals("array")) {
								hasCoordinates = true;
							}
							if (!jsonNodeAux.path("type").isMissingNode()
									&& !jsonNodeAux.path("type").path("enum").isMissingNode()
									&& jsonNodeAux.path("type").get("enum").isArray()) {
								hasType = true;
							}
							if (hasCoordinates && hasType) {
								fields.put(property, jsonNodeAux.path("type").get("enum").get(0).asText());
							}

						}
					}
				}
			} else {
				if (virtual.getObjectGeometry() != null && !virtual.getObjectGeometry().isEmpty()) {
					jsonNode = jsonNode.findPath(PROPERTIES);
					fields.put(virtual.getObjectGeometry(), null);
				}
			}
		}

		return fields;
	}

	@Override
	public Layer getLayerByIdentification(String identification, User user) {
		final List<Layer> layers = layerRepository.findByIdentification(identification);
		if (!layers.isEmpty() && (userService.isUserAdministrator(user) || layers.get(0).getUser().equals(user))) {
			return layers.get(0);
		} else if (layers.isEmpty()) {
			throw new LayerServiceException("Layer " + identification + " doesn't exist.");
		} else {
			throw new LayerServiceException(USER_NOT_AUTHORIZED);
		}
	}

	@Override
	public Boolean isLayerInUse(String layerId) {
		final Optional<Layer> layer = layerRepository.findById(layerId);
		if (layer.isPresent()) {
			return !layer.get().getViewers().isEmpty();
		} else {

			return true;
		}
	}

	@Override
	public Layer findByIdentification(String layerIdentification) {
		return layerRepository.findByIdentification(layerIdentification).get(0);
	}

	@Override
	public Map<String, String> getLayersTypes(String userId) {
		final Map<String, String> map = new HashMap<>();
		List<Layer> layers = null;
		final User sessionUser = userService.getUser(userId);

		if (userService.isUserAdministrator(sessionUser)) {
			layers = layerRepository.findAll();
		} else {
			layers = layerRepository.findByUserOrIsPublicTrue(sessionUser);
		}

		for (final Layer layer : layers) {
			if (layer.getOntology() != null && !layer.isHeatMap()) {
				map.put(layer.getIdentification(), "iot");
			} else if (layer.getOntology() != null && layer.isHeatMap()) {
				map.put(layer.getIdentification(), "heat");
			} else if (layer.getExternalType().equalsIgnoreCase("wms")) {
				map.put(layer.getIdentification(), "wms");
			} else if (layer.getExternalType().equalsIgnoreCase("kml")) {
				map.put(layer.getIdentification(), "kml");
			} else if (layer.getExternalType().equalsIgnoreCase("svg_image")) {
				map.put(layer.getIdentification(), "svg_image");
			}
		}

		return map;
	}

	@Override
	public String getLayerWms(String layerIdentification) {
		final Layer layer = layerRepository.findByIdentification(layerIdentification).get(0);
		return URL_CONCAT + layer.getUrl() + "\",\"layerWms\":\"" + layer.getLayerTypeWms() + "\"}";
	}

	@Override
	public String getLayerKml(String layerIdentification) {
		final Layer layer = layerRepository.findByIdentification(layerIdentification).get(0);
		return URL_CONCAT + layer.getUrl() + "\"}";
	}

	@Override
	public String getLayerSvgImage(String layerIdentification) {
		final Layer layer = layerRepository.findByIdentification(layerIdentification).get(0);
		return URL_CONCAT + layer.getUrl() + "\",\"west\":\"" + layer.getWest() + "\" ,\"east\":\"" + layer.getEast()
				+ "\",\"north\":\"" + layer.getNorth() + "\",\"south\":\"" + layer.getSouth() + "\"}";
	}

	@Override
	public List<String> getQueryFields(String query, String ontology, String userId) {
		final List<String> fields = new ArrayList<>();
		final CCJSqlParserManager parserManager = new CCJSqlParserManager();
		Select select;
		try {
			select = (Select) parserManager.parse(new StringReader(query));

			final PlainSelect plain = (PlainSelect) select.getSelectBody();
			final List<SelectItem> selectItems = plain.getSelectItems();

			for (final SelectItem item : selectItems) {

				if (!item.toString().equals("c") && !item.toString().equals("*")) {
					final String[] split = item.toString().split("AS");

					fields.add(split[1].trim());
				} else {
					final Map<String, String> mapFields = ontologyService.getOntologyFields(ontology, userId);

					for (final Map.Entry<String, String> entry : mapFields.entrySet()) {
						final String field = entry.getKey();
						if (!field.contains(".")) {
							fields.add(field);
						} else {
							final String fieldAux = field.split("\\.")[0];
							if (!fields.contains(fieldAux)) {
								fields.add(fieldAux);
							}
						}
					}
				}

			}
		} catch (final JSQLParserException e) {
			Log.error("Error parsing query of layer. {} - {}", query, e.getMessage());
		} catch (final IOException e) {
			Log.error("Error getting ontology fields from query of layer. {} - {}", query, e.getMessage());
		}
		return fields;
	}

	@Override
	public String getQueryParamsAndRefresh(String layerIdentification) {
		final Layer layer = layerRepository.findByIdentification(layerIdentification).get(0);

		return "{\"params\":" + layer.getQueryParams() + ",\"refresh\":" + layer.getRefreshTime() + "}";
	}

	@Override
	public List<Layer> checkAllLayersByCriteria(String userId, String identification, String description) {
		List<Layer> allLayers = new ArrayList<>();
		final User sessionUser = userService.getUser(userId);

		if (identification != null && description != null) {
			allLayers = layerRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
			if (sessionUser.isAdmin()) {
				return allLayers;
			} else {
				return getLayersWithPermission(allLayers, sessionUser);
			}
		} else if (identification != null) {
			allLayers = layerRepository.findByIdentificationContaining(identification);
			if (sessionUser.isAdmin()) {
				return allLayers;
			} else {
				return getLayersWithPermission(allLayers, sessionUser);
			}
		} else {
			allLayers = layerRepository.findByDescriptionContaining(description);
			if (sessionUser.isAdmin()) {
				return allLayers;
			} else {
				return getLayersWithPermission(allLayers, sessionUser);
			}
		}
	}

	private List<Layer> getLayersWithPermission(List<Layer> allLayers, User user) {
		final List<Layer> layersWithAuth = layerRepository.findByUserOrIsPublicTrue(user);
		final List<Layer> layers = new ArrayList<>();
		for (final Layer layer : allLayers) {
			if (layersWithAuth.contains(layer)) {
				layers.add(layer);
			}
		}
		return layers;
	}

}

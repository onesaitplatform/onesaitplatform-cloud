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
package com.minsait.onesait.platform.controlpanel.rest.management.layer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.LayerServiceException;
import com.minsait.onesait.platform.config.services.gis.layer.LayerService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.gis.layer.LayerDTO;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Layer Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/layers")
@Slf4j
public class LayerRestController {

	@Autowired
	private LayerService layerService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private RouterService routerService;

	private static final String ERROR_USER_NOT_ALLOWED = "User is not authorized";
	private static final String ERROR_ONTOLOGY_NOT_FOUND = "Ontology not found to create the layer";
	private static final String ERROR_LAYER_NOT_FOUND = "Layer not found";
	private static final String ERROR_INSERT_DATA = "Error inserting data on layer";
	private static final String ERROR_UPDATE_DATA = "Error updating data on layer";
	private static final String ERROR_DELETE_DATA = "Error deleting data on layer";
	private static final String ERROR_GET_DATA = "Error getting data layer";
	private static final String RASTER = "raster";

	@Operation(summary = "Create a new layer")
	@PostMapping("/")
	public ResponseEntity<String> create(@RequestBody(required = true) LayerDTO layerDto)
			throws JsonProcessingException {
		if (log.isDebugEnabled()) {
			log.debug("Recieved request to create a new layer {}", layerDto.getIdentification());
		}

		User user = userService.getUserByIdentification(utils.getUserId());

		if (!layerDto.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		try {
			Layer layer = new Layer();
			if (layerDto.getOntology() != null) {

				Ontology ontology = ontologyService.getOntologyByIdentification(layerDto.getOntology(),
						utils.getUserId());

				if (ontology == null) {
					log.error("Ontology {} not found for the user {} to create the layer {}", layerDto.getOntology(),
							user.getFullName(), layerDto.getIdentification());
					return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
				layer = this.buildLayerFromDto(layerDto, ontology, layer, user);
			} else {
				layer = this.buildLayerForExternalLayer(layerDto, user, layer);
			}
			layerService.create(layer);
			return ResponseEntity.ok().build();
		} catch (LayerServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Operation(summary = "Delete layer by identification")
	@DeleteMapping("/{identification}/")
	public ResponseEntity<?> deleteLayer(
			@Parameter(description = "Layer identification", required = true) @PathVariable("identification") String identification) {
		try {
			final User user = userService.getUser(utils.getUserId());
			if (!layerService.hasUserPermission(identification, user.getUserId())) {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
			layerService.deleteLayerByIdentification(identification, user.getUserId());
		} catch (final LayerServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Layer deleted successfully", HttpStatus.OK);
	}

	@Operation(summary = "Get all user identification layers")
	@GetMapping("/identifications")
	public ResponseEntity<List<String>> getAllIdentificationLayers() {
		log.debug("Get all identifications of layers");
		try {
			List<String> values = layerService.getAllIdentificationsByUser(utils.getUserId());
			return ResponseEntity.ok().body(values);
		} catch (LayerServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Get layer by identification")
	@GetMapping("/{identification}")
	public ResponseEntity<LayerDTO> getLayer(@PathVariable("identification") String identification) {
		if (log.isDebugEnabled()) {
			log.debug("Get layer {}", identification);
		}
		try {
			Layer layer = layerService.findByIdentification(identification);
			LayerDTO dto = new LayerDTO();
			if (layer.getOntology() != null) {
				dto = buildLayerDtoFromLayer(layer);
			} else {
				dto = buildDtoForExternalLayer(layer);
			}
			return ResponseEntity.ok().body(dto);
		} catch (LayerServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Get ontology schema from layer identification")
	@GetMapping("/{identification}/schema")
	public ResponseEntity<String> getOntologySchema(@PathVariable("identification") String identification) {
		if (log.isDebugEnabled()) {
			log.debug("Get Ontology schema from layer {}", identification);
		}
		try {
			Layer layer = layerService.findByIdentification(identification);
			if (layer == null) {
				log.error("Layer {} not found for the identification {} ", identification);
				return new ResponseEntity<>(ERROR_LAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			if (layer.getOntology() == null) {
				log.error("Ontology {} not found for the layer {}", identification);
				return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			return ResponseEntity.ok().body(layer.getOntology().getJsonSchema());
		} catch (LayerServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Get all user layers")
	@GetMapping("/")
	public ResponseEntity<List<LayerDTO>> getAllLayers() {
		log.debug("Get all user layers");
		try {
			List<Layer> layers = layerService.findAllLayers(utils.getUserId());
			List<LayerDTO> dtos = new ArrayList<>();
			layers.stream().forEach((c) -> {
				if (c.getOntology() != null) {
					dtos.add(buildLayerDtoFromLayer(c));
				} else {
					dtos.add(buildDtoForExternalLayer(c));
				}
			});
			return ResponseEntity.ok().body(dtos);
		} catch (LayerServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Update layer by identification")
	@PutMapping("/{identification}")
	public ResponseEntity<String> update(@RequestBody(required = true) LayerDTO layerDto,
			@PathVariable("identification") String identification) throws JsonProcessingException {
		Layer layer = layerService.findByIdentification(identification);
		User user = userService.getUser(utils.getUserId());
		if (layer == null) {
			log.error(ERROR_LAYER_NOT_FOUND);
			return new ResponseEntity<>(ERROR_LAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		if (!layerService.hasUserPermission(layer.getIdentification(), user.getUserId())) {
			log.error(ERROR_USER_NOT_ALLOWED);
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}

		if (layer.getOntology() != null) {

			Ontology ontology = ontologyService.getOntologyByIdentification(layerDto.getOntology(), utils.getUserId());

			if (ontology == null) {
				log.error("Ontology {} not found for the user {} to create the layer {}", layerDto.getOntology(),
						user.getFullName(), layerDto.getIdentification());
				return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			layerService.create(this.buildLayerFromDto(layerDto, ontology, layer, user));
			resourcesInUseService.removeByUser(layer.getId(), utils.getUserId());

		} else {
			layerService.create(this.buildLayerForExternalLayer(layerDto, user, layer));
			resourcesInUseService.removeByUser(layer.getId(), utils.getUserId());
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Create crud of the layer")
	@PostMapping(value = { "/crud" }, produces = "text/plain")
	public ResponseEntity<String> insert(String layerIdentification, @RequestBody Object data) {

		try {
			Layer layer = layerService.findByIdentification(layerIdentification);

			if (layer == null) {
				log.error(ERROR_LAYER_NOT_FOUND);
				return new ResponseEntity<>(ERROR_LAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			if (layer.getOntology() == null) {
				log.error(ERROR_ONTOLOGY_NOT_FOUND);
				return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			return ResponseEntity.ok().body(processQuery("", layer.getOntology().getIdentification(),
					ApiOperation.Type.POST.name(), new ObjectMapper().writeValueAsString(data), "", utils.getUserId()));
		} catch (final Exception e) {
			log.error(ERROR_INSERT_DATA);
			return new ResponseEntity<>(ERROR_INSERT_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update the crud of the layer by layerIdentification and oid")
	@PutMapping(value = { "/crud" }, produces = "text/plain")
	public ResponseEntity<String> update(String layerIdentification, @RequestBody Object data, String oid) {

		try {
			Layer layer = layerService.findByIdentification(layerIdentification);
			if (layer == null) {
				log.error(ERROR_LAYER_NOT_FOUND);
				return new ResponseEntity<>(ERROR_LAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			if (layer.getOntology() == null) {
				log.error(ERROR_ONTOLOGY_NOT_FOUND);
				return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			return ResponseEntity.ok().body(processQuery("", layer.getOntology().getIdentification(),
					ApiOperation.Type.PUT.name(), new ObjectMapper().writeValueAsString(data), oid, utils.getUserId()));
		} catch (final Exception e) {
			log.error(ERROR_UPDATE_DATA);
			return new ResponseEntity<>(ERROR_UPDATE_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	@Operation(summary = "Delete the crud of the layer by layerIdentification and oid")
	@DeleteMapping(value = { "/crud" }, produces = "text/plain")
	public ResponseEntity<String> delete(String layerIdentification, String oid) {

		try {
			Layer layer = layerService.findByIdentification(layerIdentification);
			if (layer == null) {
				log.error(ERROR_LAYER_NOT_FOUND);
				return new ResponseEntity<>(ERROR_LAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			if (layer.getOntology() == null) {
				log.error(ERROR_ONTOLOGY_NOT_FOUND);
				return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			return ResponseEntity.ok().body(processQuery("", layer.getOntology().getIdentification(),
					ApiOperation.Type.DELETE.name(), "", oid, utils.getUserId()));
		} catch (final Exception e) {
			log.error(ERROR_DELETE_DATA);
			return new ResponseEntity<>(ERROR_DELETE_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get crud of the layer by layerIdentification")
	@GetMapping(value = { "/crud" }, produces = "text/plain")
	public ResponseEntity<String> list(String layerIdentification) {

		try {
			Layer layer = layerService.findByIdentification(layerIdentification);
			if (layer == null) {
				log.error(ERROR_LAYER_NOT_FOUND);
				return new ResponseEntity<>(ERROR_LAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			if (layer.getOntology() == null) {
				log.error(ERROR_ONTOLOGY_NOT_FOUND);
				return new ResponseEntity<>(ERROR_ONTOLOGY_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
			return ResponseEntity.ok().body(processQuery("select * from " + layer.getOntology().getIdentification(),
					layer.getOntology().getIdentification(), ApiOperation.Type.GET.name(), "", "", utils.getUserId()));
		} catch (final Exception e) {
			log.error(ERROR_GET_DATA);
			return new ResponseEntity<>(ERROR_GET_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Layer buildLayerFromDto(LayerDTO layerDto, Ontology ontology, Layer layer, User user) {

		if (layer.getIdentification() == null) {
			layer.setIdentification(layerDto.getIdentification());
			layer.setUser(user);
		}
		layer.setRefreshTime(layerDto.getRefreshTime());
		layer.setDescription(layerDto.getDescription());
		layer.setGeometryField(layerDto.getGeometryField());
		layer.setGeometryType(layerDto.getGeometryType());
		layer.setPublic(layerDto.getIsPublic());
		layer.setOntology(ontology);
		layer.setFilter(layerDto.getIsFilter());
		layer.setVirtual(ontologyService.getOntologyVirtualByOntologyId(ontology) != null);

		if (layerDto.getIsHeatMap()) {
			layer.setGeometryType(RASTER);
			layer.setHeatMap(layerDto.getIsHeatMap());
			layer.setWeightField(layerDto.getWeightField());
			layer.setHeatMapMax(Integer.valueOf(layerDto.getHeatMapMax()));
			layer.setHeatMapMin(Integer.valueOf(layerDto.getHeatMapMin()));
			layer.setHeatMapRadius(Integer.valueOf(layerDto.getHeatMapRadius()));
		} else {
			layer.setHeatMap(false);
			layer.setInnerColor(layerDto.getInnerColor());
			layer.setOuterColor(layerDto.getOuterColor());
			layer.setOuterThin(layerDto.getOuterThin());
			layer.setSize(layerDto.getSize());
			layer.setInfoBox(layerDto.getInfoBox());
		}
		if (layerDto.getIsFilter()) {
			layer.setFilters(layerDto.getFilters());
		} else {
			layer.setFilters(null);
		}

		if (layerDto.getQuery() != null) {
			layer.setQuery(layerDto.getQuery());
			layer.setQueryParams(layerDto.getQueryParams());
		} else {
			layer.setQuery(null);
			layer.setQueryParams(null);
		}
		return layer;
	}

	private LayerDTO buildLayerDtoFromLayer(Layer layer) {
		LayerDTO dto = new LayerDTO();
		dto.setId(layer.getId());
		dto.setIdentification(layer.getIdentification());

		dto.setRefreshTime(layer.getRefreshTime());
		dto.setDescription(layer.getDescription());
		dto.setGeometryField(layer.getGeometryField());
		dto.setGeometryType(layer.getGeometryType());
		dto.setIsPublic(layer.isPublic());
		dto.setOntology(layer.getOntology().getIdentification());
		dto.setIsFilter(layer.isFilter());
		dto.setIsVirtual(ontologyService.getOntologyVirtualByOntologyId(layer.getOntology()) != null);

		if (layer.isHeatMap()) {
			dto.setGeometryType(RASTER);
			dto.setIsHeatMap(layer.isHeatMap());
			dto.setWeightField(layer.getWeightField());
			dto.setHeatMapMax(layer.getHeatMapMax().toString());
			dto.setHeatMapMin(layer.getHeatMapMin().toString());
			dto.setHeatMapRadius(layer.getHeatMapRadius().toString());
		} else {
			dto.setIsHeatMap(false);
			dto.setInnerColor(layer.getInnerColor());
			dto.setOuterColor(layer.getOuterColor());
			dto.setOuterThin(layer.getOuterThin());
			dto.setSize(layer.getSize());
			dto.setInfoBox(layer.getInfoBox());
		}
		if (layer.isFilter()) {
			dto.setFilters(layer.getFilters());
		} else {
			dto.setFilters(null);
		}

		if (layer.getQuery() != null) {
			dto.setQuery(layer.getQuery());
			dto.setQueryParams(layer.getQueryParams());
		} else {
			dto.setQuery(null);
			dto.setQueryParams(null);
		}
		return dto;
	}

	private Layer buildLayerForExternalLayer(LayerDTO layerDto, User user, Layer layer) {

		if (layer.getIdentification() == null) {
			layer.setIdentification(layerDto.getIdentification());
			layer.setUser(user);
		}

		layer.setDescription(layerDto.getDescription());
		layer.setPublic(layerDto.getIsPublic());
		layer.setExternalType(layerDto.getExternalType());
		layer.setUrl(layerDto.getUrl());
		layer.setLayerTypeWms(layerDto.getLayerTypeWms());
		if (layerDto.getExternalType().equalsIgnoreCase("svg_image")) {
			layer.setEast(Double.parseDouble(layerDto.getEast()));
			layer.setWest(Double.parseDouble(layerDto.getWest()));
			layer.setSouth(Double.parseDouble(layerDto.getSouth()));
			layer.setNorth(Double.parseDouble(layerDto.getNorth()));
		}

		return layer;
	}

	private LayerDTO buildDtoForExternalLayer(Layer layer) {
		LayerDTO layerDto = new LayerDTO();
		layerDto.setId(layer.getId());
		layerDto.setIdentification(layer.getIdentification());
		layerDto.setDescription(layer.getDescription());
		layerDto.setIsPublic(layer.isPublic());
		layerDto.setExternalType(layer.getExternalType());
		layerDto.setUrl(layer.getUrl());
		layerDto.setLayerTypeWms(layer.getLayerTypeWms());
		if (layer.getExternalType().equalsIgnoreCase("svg_image")) {
			layerDto.setEast(layer.getEast().toString());
			layerDto.setWest(layer.getWest().toString());
			layerDto.setSouth(layer.getSouth().toString());
			layerDto.setNorth(layer.getNorth().toString());
		}

		return layerDto;
	}

	private String processQuery(String query, String ontologyID, String method, String body, String objectId,
			String userId) {

		final User user = userService.getUser(userId);
		OperationType operationType = null;
		if (method.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
			body = query;
			operationType = OperationType.QUERY;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
			operationType = OperationType.INSERT;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
			operationType = OperationType.UPDATE;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
			operationType = OperationType.DELETE;
		} else {
			operationType = OperationType.QUERY;
		}

		final OperationModel model = OperationModel
				.builder(ontologyID, OperationType.valueOf(operationType.name()), user.getUserId(),
						OperationModel.Source.INTERNAL_ROUTER)
				.body(body).queryType(QueryType.SQL).objectId(objectId).deviceTemplate("").build();
		final NotificationModel modelNotification = new NotificationModel();

		modelNotification.setOperationModel(model);

		final OperationResultModel result = routerService.query(modelNotification);

		if (result != null) {
			if ("ERROR".equals(result.getResult())) {
				return "{\"error\":\"" + result.getMessage() + "\"}";
			}

			String output = result.getResult();

			if (operationType == OperationType.INSERT) {
				final JSONObject obj = new JSONObject(output);
				if (obj.has(InsertResult.DATA_PROPERTY)) {
					output = obj.getJSONObject(InsertResult.DATA_PROPERTY).toString();
				}
			}
			return output;
		} else {
			return null;
		}

	}

}

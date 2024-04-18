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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.opendata.OpenDataPermissions;
import com.minsait.onesait.platform.business.services.resources.ResourceService;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;
import com.minsait.onesait.platform.config.services.gis.viewer.ViewerService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataField;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPlatformResourceType;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResourceDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.importtool.JsonSchemaGenerator;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataFileResourceCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataOntologyResourceCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataOntologyResourceResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataPlatformResourceCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataPlatformResourceResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataPlatformResourceUpdateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataResourceResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataResourceType;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataResourceUpdateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataUrlResourceCreateDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Resources Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),  @ApiResponse(responseCode = "401", description = "Unathorized"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/opendata/resources")
@Slf4j
public class OpenDataResourcesManagementController {

	private static final String ERROR_CREATING_RESOURCE = "Error creating resource";
	private static final String MSG_ERROR_JSON_RESPONSE = "{\"error\":\"%s\"}";
	private static final String MSG_OK_JSON_RESPONSE = "{\"ok\":\"%s\"}";
	private static final String MSG_USER_UNAUTHORIZED = "User is unauthorized";
	private static final String MSG_RESOURCE_NOT_EXIST = "Resource does not exist";
	private static final String MSG_RESOURCE_EXISTS = "Resource already exists";
	private static final String MSG_RESOURCE_DELETED = "Resource has been deleted succesfully";
	private static final String MSG_ONTOLOGY_NOT_EXIST = "Ontology does not exist";
	private static final String MSG_ONTOLOGY_EXISTS = "Ontology already exists";
	private static final String MSG_DATASET_NOT_EXIST = "Dataset does not exist";
	private static final String MSG_DASHBOARD_NOT_EXIST = "Dashboard does not exist";
	private static final String MSG_API_NOT_EXIST = "Api does not exist";
	private static final String MSG_GIS_VIEWER_NOT_EXIST = "GIS viewer does not exist";
	private static final String MSG_RESOURCE_DATA_UPDATED = "Resource data has been updated succesfully";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private OpenDataPermissions openDataPermissions;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private ViewerService viewerService;

	private final ObjectMapper mapper = new ObjectMapper();

	@Operation(summary = "Get all resources")
	@GetMapping("")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "Ok", content=@Content(schema=@Schema(implementation=OpenDataResourceResponseDTO.class))))
	public ResponseEntity<Object> getAll() {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken);
			if (datasets == null || datasets.isEmpty()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			final List<OpenDataResourceResponseDTO> resourcesResponse = new ArrayList<>();
			for (final OpenDataPackage dataset : datasets) {
				final List<OpenDataResource> resources = dataset.getResources();
				for (final OpenDataResource resource : resources) {
					OpenDataResourceResponseDTO resourceResponse = null;
					final DatasetResource datasetResource = datasetService.getConfigResource(resource);
					if (datasetResource != null) {
						resourceResponse = getOntologyResourceResponse(userToken, resource.getId(), dataset);
					} else {
						resourceResponse = getPlatformResourceResponse(userToken, resource.getId(), dataset);
					}
					resourcesResponse.add(resourceResponse);
				}
			}

			return new ResponseEntity<>(resourcesResponse, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Error getting resource list: %s ", e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get resource by id")
	@GetMapping("/{id}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataResourceResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> get(
			@Parameter(description= "Resource id", required = true) @PathVariable("id") String resourceId) {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final OpenDataResource resource = resourceService.getResourceById(userToken, resourceId);
			if (resource == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToShowResource(resource)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}

			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, resource.getPackage_id());
			if (dataset != null) {
				OpenDataResourceResponseDTO resourceResponse = null;
				final DatasetResource datasetResource = datasetService.getConfigResource(resource);
				if (datasetResource != null) {
					resourceResponse = getOntologyResourceResponse(userToken, resource.getId(), dataset);
				} else {
					resourceResponse = getPlatformResourceResponse(userToken, resource.getId(), dataset);
				}
				return new ResponseEntity<>(resourceResponse, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Error getting resource"), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Error getting resource: %s ", e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new platform resource")
	@PostMapping("/platform")
	public ResponseEntity<Object> createPlatformResource(
			@Parameter(description= "ResourceCreate", required = true) @Valid @RequestBody OpenDataPlatformResourceCreateDTO resourceCreate) {

		try {
			if (resourceCreate.getIdentification() == null || resourceCreate.getDatasetId() == null || resourceCreate.getPlatformResourceType() == null ||
					resourceCreate.getPlatformResourceIdentification() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [identification, datasetId, platformResourceType, platformResourceIdentificaion]"),HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String datasetId = resourceCreate.getDatasetId();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST), HttpStatus.BAD_REQUEST);
			}

			final OpenDataResourceDTO resourceDTO = createObjectFromPlatformResourceCreate(resourceCreate, utils.getUserId());
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, resourceDTO)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			if (resourceService.existsResource(resourceDTO, userToken)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_EXISTS), HttpStatus.BAD_REQUEST);
			}

			final String resourceUrl = resourceService.getPlatformResourceUrl(resourceDTO, userToken);
			final String format = resourceService.getPlatformResourceFormat(resourceDTO);

			final String resourceId = resourceService.createResource(resourceDTO, userToken, resourceUrl, format);
			if (resourceId != null) {
				if (!resourceDTO.getPlatformResource().equals(OpenDataPlatformResourceType.API.toString())) {
					resourceService.createWebView(resourceId, userToken);
				}
				final OpenDataResourceResponseDTO newResource = getPlatformResourceResponse(userToken, resourceId, dataset);
				return new ResponseEntity<>(newResource, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, ERROR_CREATING_RESOURCE), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot create resource %s: %s", resourceCreate.getIdentification(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new resource from ontology")
	@PostMapping("/ontology")
	public ResponseEntity<Object> createResourceFromOntology(
			@Parameter(description= "PlatformResourceCreate", required = true) @Valid @RequestBody OpenDataOntologyResourceCreateDTO resourceCreate) {
		try {
			if (resourceCreate.getIdentification() == null || resourceCreate.getDatasetId() == null || resourceCreate.getOntology() == null ||
					resourceCreate.getQuery() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [identification, dataset, ontology, query]"),HttpStatus.BAD_REQUEST);
			}
			checkOntologyAndQuery(utils.getUserId(), resourceCreate.getOntology(), resourceCreate.getQuery());

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final String datasetId = resourceCreate.getDatasetId();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST), HttpStatus.BAD_REQUEST);
			}

			final OpenDataResourceDTO resourceDTO = createObjectFromOntologyResourceCreate(resourceCreate);
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, resourceDTO)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			if (resourceService.existsResource(resourceDTO, userToken)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_EXISTS), HttpStatus.BAD_REQUEST);
			}

			final List<Map<String, Object>> records = resourceService.executeQuery(resourceDTO.getOntology(), resourceDTO.getQuery(),
					utils.getUserId());

			final String resourceId = resourceService.createResourceIteration(resourceDTO, userToken, records, new ArrayList<>());
			if (resourceId != null) {
				resourceService.persistResource(resourceDTO.getOntology(), resourceDTO.getQuery(), resourceId, resourceDTO.getName(),
						userService.getUser(utils.getUserId()));
				final OpenDataResourceResponseDTO newResource = getOntologyResourceResponse(userToken, resourceId, dataset);
				return new ResponseEntity<>(newResource, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, ERROR_CREATING_RESOURCE), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot create resource %s: %s", resourceCreate.getIdentification(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new resource from file")
	@PostMapping("/file")
	public ResponseEntity<Object> createResourceFromFile(
			@RequestParam(value = "Resource identification", required = true) String identification,
			@RequestParam(value = "Resource description", required = false) String description,
			@RequestParam(value = "Dataset id", required = true) String datasetId,
			@RequestParam(value = "Is new ontology", required = true) Boolean isNewOntology,
			@RequestParam(value = "Ontology identification", required = true) String ontology,
			@RequestParam(value = "Ontology description", required = false) String ontologyDescription,
			@RequestParam(value = "File format", required = true) String fileFormat,
			@RequestPart(value = "file", required = true) MultipartFile file) {

		try {
			if (isNewOntology && ontologyDescription == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [ontologyDescription]"),HttpStatus.BAD_REQUEST);
			}

			checkOntologyAndNew(utils.getUserId(), ontology, isNewOntology);

			final String jsonData = resourceService.getJsonFromFile(file);
			if (jsonData == null || jsonData.equals("")) {
				return new ResponseEntity<>("Invalid file type. Only CSV, XML and JSON files are acceptable", HttpStatus.NOT_ACCEPTABLE);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST), HttpStatus.BAD_REQUEST);
			}

			final String query = "select * from " + ontology;
			final OpenDataFileResourceCreateDTO resourceCreate = new OpenDataFileResourceCreateDTO(identification, description,
					datasetId, isNewOntology, ontology, ontologyDescription, query, fileFormat);
			final OpenDataResourceDTO resourceDTO = createObjectFromFileResourceCreate(resourceCreate);
			if (resourceService.existsResource(resourceDTO, userToken)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_EXISTS), HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, resourceDTO)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}

			final List<Map<String, Object>> records = new ArrayList<>();
			records.addAll(mapper.readValue(jsonData, new TypeReference<List<Map<String, Object>>>() {
			}));
			resourceDTO.setJsonData(jsonData);
			createOntologyIfNewAndInsertData(isNewOntology, ontology, resourceDTO, jsonData);

			final List<OpenDataField> fields = resourceService.getResourceFields(resourceCreate.getOntology(), utils.getUserId());
			final String resourceId = resourceService.createResourceIteration(resourceDTO, userToken, records, fields);
			if (resourceId != null) {
				resourceService.persistResource(resourceCreate.getOntology(), query, resourceId, resourceDTO.getName(),
						userService.getUser(utils.getUserId()));
				final OpenDataResourceResponseDTO newResource = getOntologyResourceResponse(userToken, resourceId, dataset);

				return new ResponseEntity<>(newResource, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, ERROR_CREATING_RESOURCE), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot create resource %s: %s", identification, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new resource from url")
	@PostMapping("/url")
	public ResponseEntity<Object> createResourceFromURL(
			@Parameter(description= "ResourceCreate", required = true) @Valid @RequestBody OpenDataUrlResourceCreateDTO resourceCreate) {
		try {
			if (resourceCreate.getIdentification() == null || resourceCreate.getDatasetId() == null || resourceCreate.getOntology() == null ||
					resourceCreate.getIsNewOntology() == null || resourceCreate.getUrl() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [identification, dataset, ontology, isNewOntology, url]"),HttpStatus.BAD_REQUEST);
			}
			if (resourceCreate.getIsNewOntology() && resourceCreate.getOntologyDescription() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [ontologyDescription]"),HttpStatus.BAD_REQUEST);
			}

			checkOntologyAndNew(utils.getUserId(), resourceCreate.getOntology(), resourceCreate.getIsNewOntology());

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String datasetId = resourceCreate.getDatasetId();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST), HttpStatus.BAD_REQUEST);
			}

			final OpenDataResourceDTO resourceDTO = createObjectFromUrlResourceCreate(resourceCreate);
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateResource(userToken, resourceDTO)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			if (resourceService.existsResource(resourceDTO, userToken)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_EXISTS), HttpStatus.BAD_REQUEST);
			}

			final String query = "select * from " + resourceCreate.getOntology();
			final List<Map<String, Object>>  records = resourceService.getResourceFromUrl(resourceDTO.getUrl(), new HashMap<>());
			final String jsonData = mapper.writeValueAsString(records);

			createOntologyIfNewAndInsertData(resourceCreate.getIsNewOntology(), resourceCreate.getOntology(), resourceDTO, jsonData);
			final List<OpenDataField> fields = resourceService.getResourceFields(resourceCreate.getOntology(), utils.getUserId());
			final String resourceId = resourceService.createResourceIteration(resourceDTO, userToken, records, fields);
			if (resourceId != null) {
				resourceService.persistResource(resourceCreate.getOntology(), query, resourceId, resourceDTO.getName(),
						userService.getUser(utils.getUserId()));

				final OpenDataResourceResponseDTO newResource = getOntologyResourceResponse(userToken, resourceId, dataset);
				return new ResponseEntity<>(newResource, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, ERROR_CREATING_RESOURCE), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot create resource %s: %s", resourceCreate.getIdentification(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing resource")
	@PutMapping
	public ResponseEntity<Object> update(
			@Parameter(description= "ResourceUpdate", required = true) @Valid @RequestBody OpenDataResourceUpdateDTO resourceUpdate) {
		try {
			if (resourceUpdate.getId() == null || resourceUpdate.getIdentification() == null || resourceUpdate.getDescription() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [id, identification, description]"),HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final OpenDataResource resource = resourceService.getResourceById(userToken, resourceUpdate.getId());
			if (resource == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			final OpenDataResourceDTO resourceDTO = createObjectForUpdate(resource, resourceUpdate);
			resourceService.updateResource(resourceDTO, resource, userToken);

			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, resource.getPackage_id());
			if (dataset != null) {
				OpenDataResourceResponseDTO resourceResponse = null;
				final DatasetResource datasetResource = datasetService.getConfigResource(resource);
				if (datasetResource != null) {
					resourceResponse = getOntologyResourceResponse(userToken, resource.getId(), dataset);
				} else {
					resourceResponse = getPlatformResourceResponse(userToken, resource.getId(), dataset);
				}
				return new ResponseEntity<>(resourceResponse, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Error updating resource"), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot update resource %s: %s", resourceUpdate.getId(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Update an existing platform resource")
	@PutMapping("/platform")
	public ResponseEntity<Object> updatePlatformResource(
			@Parameter(description= "ResourceUpdate", required = true) @Valid @RequestBody OpenDataPlatformResourceUpdateDTO resourceUpdate) {
		try {
			if (resourceUpdate.getId() == null || resourceUpdate.getIdentification() == null || resourceUpdate.getPlatformResourceIdentification() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [id, identification, platformResourceIdentification]"),HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final OpenDataResource resource = resourceService.getResourceById(userToken, resourceUpdate.getId());
			if (resource == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			final OpenDataResourceDTO resourceDTO = createObjectForUpdatePlatformResource(resource, resourceUpdate, utils.getUserId());
			final String resourceUrl = resourceService.getPlatformResourceUrl(resourceDTO, userToken);
			resourceService.updatePlatformResource(resourceDTO, resource, resourceUrl, userToken);

			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, resource.getPackage_id());
			if (dataset != null) {
				OpenDataResourceResponseDTO resourceResponse = null;
				final DatasetResource datasetResource = datasetService.getConfigResource(resource);
				if (datasetResource != null) {
					resourceResponse = getOntologyResourceResponse(userToken, resource.getId(), dataset);
				} else {
					resourceResponse = getPlatformResourceResponse(userToken, resource.getId(), dataset);
				}
				return new ResponseEntity<>(resourceResponse, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Error updating resource"), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot update resource %s: %s", resourceUpdate.getId(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@Operation(summary = "Delete resource by id")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(
			@Parameter(description= "Resource id", required = true) @PathVariable("id") String resourceId) {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final OpenDataResource resource = resourceService.getResourceById(userToken, resourceId);
			if (resource == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			resourceService.deleteResource(userToken, resource.getId());
			final DatasetResource datasetResource = datasetService.getConfigResource(resource);
			if (datasetResource != null) {
				resourceService.persistResource(resource.getId());
			}

			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_RESOURCE_DELETED), HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot delete resource %s: %s", resourceId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Synchronize resource data")
	@GetMapping("/data/{id}")
	public ResponseEntity<String> updateData(
			@Parameter(description= "Resource id", required = true) @PathVariable("id") String resourceId) {
		try {

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataResource resource = resourceService.getResourceById(userToken, resourceId);
			if (resource == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_RESOURCE_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateResource(userToken, resource)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED), HttpStatus.FORBIDDEN);
			}
			final DatasetResource datasetResource = datasetService.getConfigResource(resource);
			if (datasetResource == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Unable to execute query"), HttpStatus.BAD_REQUEST);
			}
			final String ontology = datasetResource.getOntology().getIdentification();
			final String query = datasetResource.getQuery();
			checkOntologyAndQuery(userId, ontology, null);
			resourceService.cleanAllRecords(resourceId, userToken);
			final List<Map<String, Object>> records = resourceService.executeQuery(ontology, query, userId);

			final OpenDataResourceDTO resourceDTO = resourceService.getDTOFromResource(resource, datasetResource, resource.getPackage_id());
			resourceService.createResourceIteration(resourceDTO, userToken, records, new ArrayList<>());

			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_RESOURCE_DATA_UPDATED), HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot update resource data %s: %s", resourceId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void checkOntologyAndQuery(String userId, String identification, String query) {
		if (!ontologyService.existsOntology(identification)) {
			throw new OpenDataServiceException(MSG_ONTOLOGY_NOT_EXIST);
		}
		if (!ontologyService.hasUserPermissionForQuery(userId, identification)) {
			throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED + " doing query");
		}
		if (query != null) {
			final String ontologyQueryIdentification = ontologyService.getOntologyFromQuery(query);
			if (!ontologyQueryIdentification.equals(identification)) {
				throw new OpenDataServiceException("Ontology identification and ontology query does not match");
			}
		}
	}

	private void checkOntologyAndNew(String userId, String identification, Boolean newOntology) {
		if (newOntology) {
			if (ontologyService.existsOntology(identification)) {
				throw new OpenDataServiceException(MSG_ONTOLOGY_EXISTS);
			}
		} else {
			checkOntologyAndQuery(userId, identification, null);
		}
	}

	private OpenDataResourceDTO createObjectFromOntologyResourceCreate(OpenDataOntologyResourceCreateDTO resource) {
		final OpenDataResourceDTO newResource = new OpenDataResourceDTO(resource.getIdentification(), resource.getDescription(),
				OpenDataResourceType.QUERY.toString(), "JSON", resource.getDatasetId());
		newResource.setOntology(resource.getOntology());
		newResource.setQuery(resource.getQuery());
		return newResource;
	}

	private OpenDataResourceResponseDTO getOntologyResourceResponse(String userToken, String resourceId, OpenDataPackage dataset) {
		final OpenDataResource resource = resourceService.getResourceById(userToken, resourceId);
		final DatasetResource datasetResource = datasetService.getConfigResource(resource);
		return new OpenDataOntologyResourceResponseDTO(resource, datasetResource, dataset);
	}

	private OpenDataResourceDTO createObjectFromUrlResourceCreate(OpenDataUrlResourceCreateDTO resource) {
		final OpenDataResourceDTO newResource = new OpenDataResourceDTO(resource.getIdentification(), resource.getDescription(),
				OpenDataResourceType.URL.toString(), "JSON", resource.getDatasetId());
		newResource.setOntology(resource.getOntology());
		newResource.setOntologyDescription(resource.getOntologyDescription());
		newResource.setUrl(resource.getUrl());
		return newResource;
	}

	private OpenDataResourceDTO createObjectFromFileResourceCreate(OpenDataFileResourceCreateDTO resource) {
		final OpenDataResourceDTO newResource = new OpenDataResourceDTO(resource.getIdentification(), resource.getDescription(),
				OpenDataResourceType.FILE.toString(), resource.getFileFormat(), resource.getDatasetId());
		newResource.setOntology(resource.getOntology());
		newResource.setOntologyDescription(resource.getOntologyDescription());
		return newResource;
	}

	private OpenDataResourceDTO createObjectFromPlatformResourceCreate(OpenDataPlatformResourceCreateDTO resource, String userId) {
		final User user = userService.getUser(userId);
		final OpenDataResourceDTO newResource = new OpenDataResourceDTO(resource.getIdentification(), resource.getDescription(),
				OpenDataResourceType.PLATFORM.toString(), "", resource.getDatasetId());
		newResource.setPlatformResource(resource.getPlatformResourceType().toString());
		switch(resource.getPlatformResourceType()) {
		case DASHBOARD:
			final Dashboard dashboard = dashboardService.getDashboardByIdentification(resource.getPlatformResourceIdentification(), userId);
			if (dashboard == null) {
				throw new OpenDataServiceException(MSG_DASHBOARD_NOT_EXIST);
			}
			if (!user.isAdmin() && !dashboard.getUser().equals(user)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}
			newResource.setDashboardId(dashboard.getId());
			break;
		case API:
			final com.minsait.onesait.platform.config.model.Api api = apiManagerService.getApiByIdentificationVersionOrId(resource.getPlatformResourceIdentification(),
					resource.getPlatformResourceApiVersion());
			if (api == null) {
				throw new OpenDataServiceException(MSG_API_NOT_EXIST);
			}
			if (!user.isAdmin() && !api.getUser().equals(user)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}
			newResource.setApiId(api.getId());
			if (resource.getPublishGraviteeSwagger() == null) {
				newResource.setGraviteeSwagger(false);
			} else {
				newResource.setGraviteeSwagger(resource.getPublishGraviteeSwagger());
			}
			break;
		case VIEWER:
			final Viewer viewer = viewerService.getViewerByIdentification(resource.getPlatformResourceIdentification());
			if (viewer == null) {
				throw new OpenDataServiceException(MSG_GIS_VIEWER_NOT_EXIST);
			}
			if (!user.isAdmin() && !viewer.getUser().equals(user)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}
			newResource.setViewerId(viewer.getId());
			break;
		default:
			break;
		}
		return newResource;
	}

	private OpenDataResourceResponseDTO getPlatformResourceResponse(String userToken, String resourceId, OpenDataPackage dataset) {
		final OpenDataResource resource = resourceService.getResourceById(userToken, resourceId);
		final String resourceUrl = resource.getUrl();

		final Dashboard dashboard = resourceService.checkDashboardResource(resourceUrl);
		if (dashboard != null) {
			return new OpenDataPlatformResourceResponseDTO(resource, OpenDataPlatformResourceType.DASHBOARD, dashboard.getIdentification(), null, dataset);
		}
		final com.minsait.onesait.platform.config.model.Api api = resourceService.checkApiResource(resourceUrl);
		if (api != null) {
			return new OpenDataPlatformResourceResponseDTO(resource, OpenDataPlatformResourceType.API, api.getIdentification(), api.getNumversion(), dataset);
		}
		final Viewer viewer = resourceService.checkViewerResource(resourceUrl);
		if (viewer != null) {
			return new OpenDataPlatformResourceResponseDTO(resource, OpenDataPlatformResourceType.VIEWER, viewer.getIdentification(), null, dataset);
		}
		return new OpenDataResourceResponseDTO(resource, dataset);
	}

	private OpenDataResourceDTO createObjectForUpdate(OpenDataResource resource, OpenDataResourceUpdateDTO resourceUpdate) {
		final DatasetResource datasetResource = datasetService.getConfigResource(resource);
		final OpenDataResourceDTO newResource = resourceService.getDTOFromResource(resource, datasetResource, resource.getId());
		newResource.setDescription(resourceUpdate.getDescription());
		return newResource;
	}


	private OpenDataResourceDTO createObjectForUpdatePlatformResource(OpenDataResource resource, OpenDataPlatformResourceUpdateDTO resourceUpdate, String userId) {
		final DatasetResource datasetResource = null;
		final OpenDataResourceDTO newResource = resourceService.getDTOFromResource(resource, datasetResource, resource.getId());
		final String resourceUrl = resource.getUrl();
		final User user = userService.getUser(userId);
		final OpenDataPlatformResourceType platformResourceType = resourceService.getPlatformResourceTypeFromUrl(resourceUrl);
		if (platformResourceType == null) {
			throw new OpenDataServiceException("Resource is not a platform resource");
		}
		switch(platformResourceType) {
		case DASHBOARD:
			final Dashboard dashboard = dashboardService.getDashboardByIdentification(resourceUpdate.getPlatformResourceIdentification(), userId);
			if (dashboard == null) {
				throw new OpenDataServiceException(MSG_DASHBOARD_NOT_EXIST);
			}
			if (!user.isAdmin() && !dashboard.getUser().equals(user)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}
			newResource.setDashboardId(dashboard.getId());
			break;
		case API:
			final com.minsait.onesait.platform.config.model.Api api = apiManagerService.getApiByIdentificationVersionOrId(resourceUpdate.getPlatformResourceIdentification(),
					resourceUpdate.getPlatformResourceApiVersion());
			if (api == null) {
				throw new OpenDataServiceException(MSG_API_NOT_EXIST);
			}
			if (!user.isAdmin() && !api.getUser().equals(user)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}
			newResource.setApiId(api.getId());
			if (resourceUpdate.getPublishGraviteeSwagger() == null) {
				newResource.setGraviteeSwagger(false);
			} else {
				newResource.setGraviteeSwagger(resourceUpdate.getPublishGraviteeSwagger());
			}
			break;
		case VIEWER:
			final Viewer viewer = viewerService.getViewerByIdentification(resourceUpdate.getPlatformResourceIdentification());
			if (viewer == null) {
				throw new OpenDataServiceException(MSG_GIS_VIEWER_NOT_EXIST);
			}
			if (!user.isAdmin() && !viewer.getUser().equals(user)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}
			newResource.setViewerId(viewer.getId());
			break;
		default:
			break;
		}
		newResource.setPlatformResource(platformResourceType.toString());
		if (resourceUpdate.getDescription() != null) {
			newResource.setDescription(resourceUpdate.getDescription());
		} else {
			newResource.setDescription(resource.getDescription());
		}

		return newResource;
	}

	private void createOntologyIfNewAndInsertData(Boolean isNewOntology, String ontology, OpenDataResourceDTO resourceDTO, String jsonData) throws IOException, OntologyBusinessServiceException {
		if (isNewOntology) {
			if (!resourceDTO.getOntology().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				throw new OpenDataServiceException ("Invalid ontology name");
			}
			final String firstJson = resourceService.getFirstElement(jsonData);
			final String jsonSchema = JsonSchemaGenerator.outputAsString(ontology, "Info " + ontology, firstJson);
			resourceDTO.setOntologySchema(jsonSchema);
			final Ontology newOntology = resourceService.createOntology(resourceDTO.getOntology(), resourceDTO.getOntologyDescription(), resourceDTO.getOntologySchema(), utils.getUserId());
			ontologyBusinessService.createOntology(newOntology, newOntology.getUser().getUserId(), null);
		}

		final String bulkInsert = resourceService.insertDataIntoOntology(ontology, jsonData, utils.getUserId()).getMessage();
		if (!bulkInsert.equals("OK")) {
			throw new OpenDataServiceException ("Cannot insert data into ontology");
		}
	}
}

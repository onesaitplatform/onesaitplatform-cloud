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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.opendata.OpenDataPermissions;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.business.services.resources.ResourceService;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;
import com.minsait.onesait.platform.config.services.opendata.binaryFiles.BinaryFilesDatasetService;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageList;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyDatasetService;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.BinaryFileResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataDatasetCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataDatasetListResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataDatasetResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataDatasetUpdateDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Datasets Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),  @ApiResponse(responseCode = "401", description = "Unathorized"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/opendata/datasets")
@Slf4j
public class OpenDataDatasetsManagementController {

	private static final String MSG_ERROR_JSON_RESPONSE = "{\"error\":\"%s\"}";
	private static final String MSG_OK_JSON_RESPONSE = "{\"ok\":\"%s\"}";
	private static final String MSG_USER_UNAUTHORIZED = "User is unauthorized";
	private static final String MSG_DATASET_NOT_EXIST = "Dataset does not exist";
	private static final String MSG_OR_USER_UNAUTHORIZED = " or user unauthorized";
	private static final String MSG_DATASET_EXISTS = "Dataset already exists";
	private static final String MSG_DATASET_DELETED = "Dataset has been deleted successfully";
	private static final String MSG_ORGANIZATION_NOT_EXIST = "Organization does not exist";
	private static final String MSG_TYPOLOGY_NOT_EXIST = "Typology does not exist ";


	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private OpenDataPermissions openDataPermissions;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private TypologyService typologyService;
	@Autowired
	private TypologyDatasetService typologyDatasetService;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private BinaryFilesDatasetService binaryFileDatasetService;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	@Operation(summary = "Get all datasets")
	@GetMapping("")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataDatasetListResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> getAll(
			@Parameter(description= "Example: organization:organization-1 AND tags:test")
			@RequestParam(required = false, value = "query") String query,
			@Parameter(description= "Example: name asc, relevance desc")
			@RequestParam(required = false, value = "sort") String sort,
			@Parameter(description= "Max number of results")
			@RequestParam(required = false, value = "rows") Integer rows,
			@Parameter(description = "Offset of results")
			@RequestParam(required = false, value = "start") Integer start,
			@Parameter(description = "Filter for typologies (separated by commas)")
			@RequestParam(required = false, value = "typologies") String typologies,
			@Parameter(description= "Set to true if you want to include resources array in each dataset")
			@RequestParam(required = false, defaultValue = "false") boolean showResources,
			@Parameter(description= "Set to true if you want to include groups array in each dataset")
			@RequestParam(required = false, defaultValue = "false") boolean showGroups
			) {
		try {
			String[] typologiesList = new String[0];
			if (typologies != null && !typologies.isEmpty()) {
				typologiesList = typologies.split("(?<!\\\\),");
			}
			OpenDataPackageList datasetsFromUser;

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			if (typologies != null && !typologies.isEmpty()) {
				datasetsFromUser = datasetService.getDatasetsListByUser(userToken, query, sort, null, null);
			} else {
				datasetsFromUser = datasetService.getDatasetsListByUser(userToken, query, sort, rows, start);
			}
			if (datasetsFromUser == null || datasetsFromUser.getResults().isEmpty()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST), HttpStatus.NOT_FOUND);
			}
			final List<OpenDataDatasetResponseDTO> datasets = new ArrayList<>();
			OpenDataDatasetListResponseDTO response;
			for (OpenDataPackage dataset: datasetsFromUser.getResults()) {
				final String typology = typologyDatasetService.getTypologyIdentificationByDatasetId(dataset.getId());
				datasets.add(new OpenDataDatasetResponseDTO(dataset, typology, showResources, showGroups));
			}
			if (typologies != null && !typologies.isEmpty()) {
				response = getDatasetsListFilteredByTypology(datasets, typologiesList, rows, start);
				if (response.getResults().isEmpty()) {
					return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST), HttpStatus.NOT_FOUND);
				}
			} else {
				response = new OpenDataDatasetListResponseDTO(datasetsFromUser.getCount(), datasets);
			}

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Error getting dataset list: %s ", e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get dataset by id")
	@GetMapping("/{id}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataDatasetResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> get(
			@Parameter(description= "Dataset id", required = true) @PathVariable("id") String datasetId) {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST + MSG_OR_USER_UNAUTHORIZED), HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToShowDataset(dataset)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}
			final String typology = typologyDatasetService.getTypologyIdentificationByDatasetId(dataset.getId());
			final List<BinaryFile> files = binaryFileDatasetService.getBinaryFilesObjectByDatasetId(dataset.getId());
			final List<BinaryFileResponseDTO> filesResponse = convertBinaryFileToResponse(files);
			final OpenDataDatasetResponseDTO datasetResponse = new OpenDataDatasetResponseDTO(dataset, typology, filesResponse);

			return new ResponseEntity<>(datasetResponse, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Error getting dataset %s: %s", datasetId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new dataset")
	@PostMapping
	public ResponseEntity<Object> create(
			@Parameter(description= "DatasetCreate", required = true) @Valid @RequestBody OpenDataDatasetCreateDTO datasetCreate) {
		try {
			if (datasetCreate.getIdentification() == null || datasetCreate.getOrganizationId() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Missing required fields. Required = [identification, organizationId]"),HttpStatus.BAD_REQUEST);
			}
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken, datasetCreate.getOrganizationId());
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST), HttpStatus.BAD_REQUEST);
			}

			checkTypology(datasetCreate.getTypology());
			checkBinaryFiles(utils.getUserId(), datasetCreate.getFilesIds());

			final OpenDataPackageDTO dataset = createDatasetCreateObject(datasetCreate, organization);
			if (datasetService.existsDataset(dataset, userToken)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_EXISTS),HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateDataset(userToken, dataset)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}

			final OpenDataPackage newDataset = datasetService.createDataset(dataset, userToken);
			final String typology = typologyDatasetService.getTypologyIdentificationByDatasetId(newDataset.getId());
			final List<BinaryFile> files = binaryFileDatasetService.getBinaryFilesObjectByDatasetId(newDataset.getId());
			final List<BinaryFileResponseDTO> filesResponse = convertBinaryFileToResponse(files);
			final OpenDataDatasetResponseDTO datasetResponseDTO = new OpenDataDatasetResponseDTO(newDataset, typology, filesResponse);

			return new ResponseEntity<>(datasetResponseDTO, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot create dataset %s: %s", datasetCreate.getIdentification(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing dataset")
	@PutMapping
	public ResponseEntity<Object> update(
			@Parameter(description= "DatasetUpdate", required = true) @Valid @RequestBody OpenDataDatasetUpdateDTO datasetUpdate) {
		try {
			if (datasetUpdate.getId() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, "Missing required fields. Required = [id]"),HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetUpdate.getId());
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST + MSG_OR_USER_UNAUTHORIZED), HttpStatus.BAD_REQUEST);
			}

			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateDataset(userToken, dataset)) {
				throw new OpenDataServiceException(MSG_USER_UNAUTHORIZED);
			}

			final String organizationId = datasetUpdate.getOrganizationId();
			OpenDataOrganization organization;
			if (organizationId != null) {
				organization = organizationService.getOrganizationById(userToken, organizationId);
				if (organization == null) {
					throw new OpenDataServiceException(MSG_ORGANIZATION_NOT_EXIST);
				}
			} else {
				organization = organizationService.getOrganizationById(userToken, dataset.getOrganization().getId());
				if (organization == null) {
					throw new OpenDataServiceException(MSG_ORGANIZATION_NOT_EXIST);
				}
			}

			checkTypology(datasetUpdate.getTypology());
			checkBinaryFiles(utils.getUserId(), datasetUpdate.getFilesIds());

			final OpenDataPackageDTO datasetDTO = createDatasetUpdateObject(dataset, datasetUpdate, organization);
			final OpenDataPackage newDataset = datasetService.updateDataset(datasetDTO, userToken);
			if (datasetDTO.getIsPublic() && dataset.getIsPrivate()) {
				resourceService.updatePlatformResourcesFromDataset(dataset, userService.getUser(utils.getUserId()));
			}

			final String typology = typologyDatasetService.getTypologyIdentificationByDatasetId(dataset.getId());
			final List<BinaryFile> files = binaryFileDatasetService.getBinaryFilesObjectByDatasetId(dataset.getId());
			final List<BinaryFileResponseDTO> filesResponse = convertBinaryFileToResponse(files);
			final OpenDataDatasetResponseDTO datasetResponseDTO = new OpenDataDatasetResponseDTO(newDataset, typology, filesResponse);

			return new ResponseEntity<>(datasetResponseDTO, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			if (e.getMessage().equals(MSG_USER_UNAUTHORIZED)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot update dataset %s: %s", datasetUpdate.getId(), e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Delete dataset by id")
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> delete(
			@Parameter(description= "Dataset id", required = true) @PathVariable("id") String datasetId) {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
			if (dataset == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_DATASET_NOT_EXIST + MSG_OR_USER_UNAUTHORIZED), HttpStatus.BAD_REQUEST);
			}

			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken, dataset.getOrganization().getId());
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST), HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateDataset(userToken, dataset)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),HttpStatus.FORBIDDEN);
			}

			final List<OpenDataResource> resources = dataset.getResources();
			for (final OpenDataResource resource : resources) {
				final DatasetResource datasetResource = datasetService.getConfigResource(resource);
				if (datasetResource != null) {
					resourceService.persistResource(resource.getId());
				}
			}
			datasetService.deleteDataset(userToken, datasetId);
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_DATASET_DELETED), HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot delete dataset %s: %s", datasetId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private OpenDataPackageDTO createDatasetCreateObject(OpenDataDatasetCreateDTO datasetCreate, OpenDataOrganization organization) {
		final String datasetId = datasetService.getDatasetId(datasetCreate.getIdentification());
		final OpenDataPackageDTO newDataset = new OpenDataPackageDTO(datasetId, datasetCreate.getIdentification(), organization.getId());

		newDataset.setDescription(datasetCreate.getDescription());
		if (datasetCreate.getTypology() == null) {
			newDataset.setTypology(datasetCreate.getTypology());
		} else {
			newDataset.setTypology(typologyService.getTypologyIdByTypologyIdentification(datasetCreate.getTypology()));
		}

		if (datasetCreate.getTags() == null) {
			newDataset.setTags(new ArrayList<>());
		} else {
			newDataset.setTags(datasetCreate.getTags());
		}

		if (datasetCreate.getIsPublic() == null) {
			newDataset.setIsPublic(false);
		} else {
			newDataset.setIsPublic(datasetCreate.getIsPublic());
		}

		if (datasetCreate.getLicense() == null) {
			newDataset.setLicense("notspecified");
		} else {
			newDataset.setLicense(datasetService.getLicenseIdByLicenseTitle(datasetCreate.getLicense()));
		}

		if (datasetCreate.getFilesIds() == null) {
			newDataset.setFiles(new ArrayList<>());
		} else {
			newDataset.setFiles(datasetCreate.getFilesIds());
		}

		return newDataset;
	}

	private OpenDataPackageDTO createDatasetUpdateObject(OpenDataPackage oldDataset, OpenDataDatasetUpdateDTO datasetUpdate, OpenDataOrganization organization) {
		final List<String> files = binaryFileDatasetService.getBinaryFileIdsByDatasetId(oldDataset.getId());
		final OpenDataPackageDTO newDataset = new OpenDataPackageDTO(oldDataset.getId(), oldDataset.getName(), oldDataset.getTitle(),
				organization.getId());

		if (datasetUpdate.getIsPublic() == null) {
			newDataset.setIsPublic(!oldDataset.getIsPrivate());
		} else {
			newDataset.setIsPublic(datasetUpdate.getIsPublic());
		}

		if (datasetUpdate.getLicense() == null) {
			newDataset.setLicense(oldDataset.getLicense_id());
		} else {
			newDataset.setLicense(datasetService.getLicenseIdByLicenseTitle(datasetUpdate.getLicense()));
		}

		if (datasetUpdate.getDescription() == null) {
			newDataset.setDescription(oldDataset.getNotes());
		} else {
			newDataset.setDescription(datasetUpdate.getDescription());
		}

		if (datasetUpdate.getTags() == null) {
			final List<String> tags = new ArrayList<>();
			oldDataset.getTags().forEach(tag -> tags.add(tag.getName()));
			newDataset.setTags(tags);
		} else {
			newDataset.setTags(datasetUpdate.getTags());
		}

		if (datasetUpdate.getTypology() == null) {
			newDataset.setTypology(typologyDatasetService.getTypologyIdByDatasetId(oldDataset.getId()));
		} else {
			newDataset.setTypology(typologyService.getTypologyIdByTypologyIdentification(datasetUpdate.getTypology()));
		}

		if (datasetUpdate.getFilesIds() == null) {
			newDataset.setFiles(files);
		} else {
			newDataset.setFiles(datasetUpdate.getFilesIds());
		}

		return newDataset;
	}

	private void checkTypology(String typology) {
		if (typology != null && !typology.isEmpty() && !typologyService.typologyExists(typology)) {
			throw new OpenDataServiceException(MSG_TYPOLOGY_NOT_EXIST);
		}
	}

	private void checkBinaryFiles(String userId, List<String> files) {
		if (files != null && !files.isEmpty()) {
			final User user = userService.getUser(userId);
			for (final String fileId: files) {
				final BinaryFile file = binaryFileService.getFile(fileId);
				if (file == null) {
					throw new OpenDataServiceException(String.format("File %s does not exist", fileId));
				}
				if (!(binaryFileService.hasUserPermissionRead(fileId, user) || binaryFileService.hasUserPermissionWrite(fileId, user))) {
					throw new OpenDataServiceException(String.format("User does not have permissions for file %s", fileId));
				}
			}
		}
	}

	private List<BinaryFileResponseDTO> convertBinaryFileToResponse(List<BinaryFile> files) {
		final List<BinaryFileResponseDTO> filesResponse = new ArrayList<>();
		if (files != null) {
			files.forEach(file -> filesResponse.add(new BinaryFileResponseDTO(file.getId(), file.getFileName(), basePath + "/files/")));
		}
		return filesResponse;
	}

	private OpenDataDatasetListResponseDTO getDatasetsListFilteredByTypology(List<OpenDataDatasetResponseDTO> datasets, 
			String[] typologies, Integer rows, Integer start) {
		final List<OpenDataDatasetResponseDTO> datasetsFiltered = datasets
				.stream().filter(d -> Arrays.asList(typologies).contains(d.getTypology())).collect(Collectors.toList());
		if (!datasetsFiltered.isEmpty()) {
			if (rows != null && start != null && (rows+start) < datasetsFiltered.size()) {
				return new OpenDataDatasetListResponseDTO(datasetsFiltered.size(), 
						datasetsFiltered.subList(start, rows + start));
			} else if (start != null) {
				return new OpenDataDatasetListResponseDTO(datasetsFiltered.size(), 
						datasetsFiltered.subList(start, datasetsFiltered.size()));
			} else if (rows != null && rows < datasetsFiltered.size()) {
				return new OpenDataDatasetListResponseDTO(datasetsFiltered.size(), 
						datasetsFiltered.subList(0, rows));
			} else if (rows != null) {
				return new OpenDataDatasetListResponseDTO(datasetsFiltered.size(), 
						datasetsFiltered.subList(0, datasetsFiltered.size()));
			}
		}
		
		return new OpenDataDatasetListResponseDTO(datasetsFiltered.size(), datasetsFiltered);
	}

}

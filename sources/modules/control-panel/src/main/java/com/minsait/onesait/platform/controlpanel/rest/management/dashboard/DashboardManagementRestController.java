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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardExportDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardImportResponsetDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardOrder;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardSimplifiedDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardUserAccessDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.CommandDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.UpdateCommandDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.rest.management.dashboard.fiql.DashboardFIQL;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard Management")
@RequestMapping("api/dashboards")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
public class DashboardManagementRestController {

	private static final String PATH = "/dashboard";
	private static final String CONSTANT_DASHBOARD_NOT_FOUND = "\"Dashboard not found\"";
	private static final String CONSTANT_DASHBOARD_NOT_FOUND_OR_UNAUTHORIZED = "\"Dashboard not found or unauthorized\"";
	private static final String USER_NOT_AUTHORIZED = "\"User not authorized\"";
	private static final String MSG_ERROR_JSON_RESPONSE = "{\"ERROR\":%s}";
	private static final String MSG_OK_JSON_RESPONSE = "{\"OK\":%s}";
	private static final String IMAGE_SUCCESSFULLY_UPLOADED = "\"Image successfully uploaded\"";
	private static final String IMAGE_SUCCESSFULLY_DELETED = "\"Image successfully deleted\"";
	private static final String FILE_SIZE_IS_LARGER_THAN_MAX_ALLOWED = "\"File size is larger than max allowed\"";
	private static final String FILE_EXTENSION_NOT_ALLOWED = "\"File Extension not allowed\"";
	private static final String ATTACHMENT_COULDN_T_BE_FOUND = "\"Attachment couldn't be found\"";
	private static final String MSG_ERROR_5_CHARACTERS = "\"The identifier must have at least 5 characters\"";

	@Autowired
	private DashboardFIQL dashboardFIQL;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subCategoryService;

	@Autowired
	private CategoryRelationService categoryRelationService;

	@Autowired
	private AppWebUtils utils;

	@Value("${onesaitplatform.dashboardengine.url}")
	private String url;

	@Value("${onesaitplatform.dashboardengine.url.view}")
	private String viewUrl;

	protected ObjectMapper objectMapper;

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO[].class))))
	@Operation(summary = "Get dashboards")
	@GetMapping
	public ResponseEntity<DashboardDTO[]> getAll(
			@RequestParam(value = "orderType", required = false) DashboardOrder order,
			@RequestParam(value = "includeImage", required = false) boolean includeImage,
			@RequestParam(value = "type", required = false) Dashboard.DashboardType type) {
		order = order == null ? DashboardOrder.IDENTIFICATION_ASC : order;
		final List<Dashboard> dashboards = dashboardService.getByUserIdOrdered(utils.getUserId(), order);
		final List<DashboardDTO> dashboardsDTO = new ArrayList<>();
		for (final Dashboard dashboard : dashboards) {
			if (type == null || type.equals(Dashboard.DashboardType.DASHBOARD) && dashboard.getType() == null
					|| type.equals(dashboard.getType())) {
				final CategoryRelation categoryRelationship = categoryRelationService.getByIdType(dashboard.getId());
				String categoryIdentification = null;
				String subCategoryIdentification = null;
				if (categoryRelationship != null) {
					final Category category = categoryService.getCategoryById(categoryRelationship.getCategory());
					Subcategory subcategory = new Subcategory();
					if (categoryRelationship.getSubcategory() != null) {
						subcategory = subCategoryService.getSubcategoryById(categoryRelationship.getSubcategory());
					}
					categoryIdentification = category.getIdentification();
					subCategoryIdentification = subcategory.getIdentification();
				}

				final int ngadgets = dashboardService.getNumGadgets(dashboard);

				final List<DashboardUserAccess> dashaccesses = new ArrayList<>();

				if (dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
					dashaccesses.addAll(dashboardService.getDashboardUserAccesses(dashboard));
				}

				final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboard, url, viewUrl,
						categoryIdentification, subCategoryIdentification, ngadgets, dashaccesses);

				if (!includeImage) {
					dashboardDTO.setImage(null);
				}
				dashboardsDTO.add(dashboardDTO);
			}
		}

		return new ResponseEntity<>(dashboardsDTO.toArray(new DashboardDTO[0]), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO.class))))
	@Operation(summary = "Get dashboard by identification or id")
	@GetMapping(PATH + "/{identification}")
	public ResponseEntity<DashboardDTO> getDashboardByIdentification(
			@Parameter(description = "dashboard identification or id", required = true) @PathVariable("identification") String identification,
			HttpServletResponse response) {
		utils.cleanInvalidSpringCookie(response);
		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		}

		if (!dashboardService.hasUserViewPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		final CategoryRelation categoryRelationship = categoryRelationService.getByIdType(dashboard.getId());
		String categoryIdentification = null;
		String subCategoryIdentification = null;
		if (categoryRelationship != null) {
			final Category category = categoryService.getCategoryById(categoryRelationship.getCategory());
			Subcategory subcategory = new Subcategory();
			if (categoryRelationship.getSubcategory() != null) {
				subcategory = subCategoryService.getSubcategoryById(categoryRelationship.getSubcategory());
			}
			categoryIdentification = category.getIdentification();
			subCategoryIdentification = subcategory.getIdentification();
		}

		final int ngadgets = dashboardService.getNumGadgets(dashboard);

		final List<DashboardUserAccess> dashaccesses = new ArrayList<>();

		if (dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			dashaccesses.addAll(dashboardService.getDashboardUserAccesses(dashboard));
		}

		final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboard, url, viewUrl, categoryIdentification,
				subCategoryIdentification, ngadgets, dashaccesses);

		return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO.class))))
	@Operation(summary = "Create new dashboard")
	@PostMapping
	public ResponseEntity<?> create(
			@Parameter(description = "CommandDTO", required = true) @Valid @RequestBody CommandDTO commandDTO,
			Errors errors) {
		try {
			final DashboardCreateDTO dashboardCreateDTO = dashboardFIQL.fromCommandToDashboardCreate(commandDTO, null,
					utils.getUserId());

			if (!dashboardCreateDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN_SPACES)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_', ' '",
						HttpStatus.BAD_REQUEST);
			}

			final String dashboardId = dashboardService.createNewDashboard(dashboardCreateDTO, utils.getUserId());

			final Dashboard dashboardCreated = dashboardService.getDashboardById(dashboardId, utils.getUserId());

			dashboardService.generateDashboardImage(dashboardId, utils.getCurrentUserOauthToken());

			final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboardCreated, url, viewUrl,
					dashboardCreateDTO.getCategory(), dashboardCreateDTO.getSubcategory(),
					dashboardService.getNumGadgets(dashboardCreated),
					dashboardService.getDashboardUserAccesses(dashboardCreated));

			return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);

		} catch (final GadgetDatasourceServiceException | DashboardServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete dashboard by identification or id")
	@DeleteMapping("/{identification}")
	public ResponseEntity<?> delete(
			@Parameter(description = "dashboard identification or id", example = "dashboardId", required = true) @PathVariable("identification") String identification) {
		try {
			Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
			if (dashboard == null) {
				dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
				if (dashboard == null) {
					return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
			}

			if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
				return new ResponseEntity<>(USER_NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
			}

			dashboardService.deleteDashboard(dashboard.getId(), utils.getUserId());
			return new ResponseEntity<>("\"Dashboard deleted successfully\"", HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO.class))))
	@Operation(summary = "Update dashboard")
	@PutMapping("/{identification}")
	public ResponseEntity<?> update(
			@Parameter(description = "dashboard identification or id") @PathVariable(value = "identification") String identification,
			@Parameter(description = "CommandDTO", required = true) @Valid @RequestBody UpdateCommandDTO updateDTO,
			Errors errors) {
		try {

			Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());

			if (dashboard == null) {
				dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
				if (dashboard == null) {
					return new ResponseEntity<>("Dashboard does not exist.", HttpStatus.OK);
				}
			}

			if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
				return new ResponseEntity<>(USER_NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
			}

			if (updateDTO.getInformation() == null
					&& (updateDTO.getDescription() != null || updateDTO.getIdentification() != null)) {

				if (!dashboard.getIdentification().equals(updateDTO.getIdentification()) && dashboardService
						.getDashboardByIdentification(updateDTO.getIdentification(), utils.getUserId()) != null) {
					return new ResponseEntity<>("The identification is already used by another dashboard",
							HttpStatus.BAD_REQUEST);
				}
				// retrocompatibility, allowing to define is public as a field
				final DashboardSimplifiedDTO dashSimplified = new DashboardSimplifiedDTO();
				if (updateDTO.getDescription() != null) {
					dashSimplified.setDescription(updateDTO.getDescription());
				}
				if (updateDTO.getIdentification() != null) {
					dashSimplified.setIdentification(updateDTO.getIdentification());
				}
				if (updateDTO.getIsPublic() != null) {
					dashSimplified.setPublic(updateDTO.getIsPublic());
				} else {
					dashSimplified.setPublic(false);
				}

				dashboardService.updateDashboardSimplified(dashboard.getId(), dashSimplified, utils.getUserId());
				final Dashboard dashboardUpdated = dashboardService.getDashboardById(dashboard.getId(),
						utils.getUserId());
				dashboardService.generateDashboardImage(dashboard.getId(), utils.getCurrentUserOauthToken());
				final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboardUpdated, url, viewUrl, null,
						null, dashboardService.getNumGadgets(dashboardUpdated),
						dashboardService.getDashboardUserAccesses(dashboardUpdated));
				return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
			} else {

				final DashboardCreateDTO dashboardCreateDTO = dashboardFIQL.fromCommandToDashboardCreate(
						dashboardFIQL.fromUpdateToCommand(updateDTO), dashboard.getId(), utils.getUserId());

				if (!dashboard.getIdentification().equals(dashboardCreateDTO.getIdentification())
						&& dashboardService.getDashboardByIdentification(dashboardCreateDTO.getIdentification(),
								utils.getUserId()) != null) {
					return new ResponseEntity<>("The identification is already used by another dashboard",
							HttpStatus.BAD_REQUEST);
				}
				final String dashboardId = dashboardService.updatePublicDashboard(dashboardCreateDTO,
						utils.getUserId());

				final Dashboard dashboardUpdated = dashboardService.getDashboardById(dashboardId, utils.getUserId());

				dashboardService.generateDashboardImage(dashboard.getId(), utils.getCurrentUserOauthToken());

				final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboardUpdated, url, viewUrl,
						dashboardCreateDTO.getCategory(), dashboardCreateDTO.getSubcategory(),
						dashboardService.getNumGadgets(dashboardUpdated),
						dashboardService.getDashboardUserAccesses(dashboardUpdated));

				return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
			}
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardExportDTO.class))))
	@Operation(summary = "Export dashboard by id")
	@GetMapping("/export/{id}")
	public ResponseEntity<?> exportDashboard(
			@Parameter(description = "dashboard id", required = true) @PathVariable("id") String dashboardId) {
		DashboardExportDTO dashboardExportDTO;
		try {
			dashboardExportDTO = dashboardService.exportDashboardDTO(dashboardId, utils.getUserId());

		} catch (final DashboardServiceException e) {
			switch (e.getErrorType()) {
			case NOT_FOUND:
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND_OR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			default:
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		return new ResponseEntity<>(dashboardExportDTO, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO.class))))
	@Operation(summary = "Import dashboard")
	@PostMapping("/import")
	public ResponseEntity<?> importDashboard(
			@Parameter(description = "Overwrite Dashboard if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@Parameter(description = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@Parameter(description = "DashboardDTO", required = true) @Valid @RequestBody DashboardExportDTO dashboardimportDTO,
			Errors errors) {

		if (!dashboardimportDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN_SPACES)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_', ' '",
					HttpStatus.BAD_REQUEST);
		}

		final DashboardImportResponsetDTO dashboardResutl = dashboardService.importDashboard(dashboardimportDTO,
				utils.getUserId(), overwrite, importAuthorizations);
		dashboardResutl.setIdentification(dashboardimportDTO.getIdentification());
		if(dashboardResutl.getErrorOntologies().size() != 0) {
		 List<HashMap<String, String>> newArray = new ArrayList<>();
	        for (HashMap<String, String> objeto : dashboardResutl.getErrorOntologies()) {
	        	HashMap<String, String> newObject = new HashMap<>();
	            for (HashMap.Entry<String, String> entry : objeto.entrySet()) {
	   
	            	newObject.put("Datasource", entry.getKey());
	            	newObject.put("Ontology", entry.getValue());
	            }
	            newArray.add(newObject);
	        }
	        dashboardResutl.setErrorOntologies(newArray);
		}
	      
		if (dashboardResutl.getId() != null) {
			dashboardService.generateDashboardImage(dashboardResutl.getId(), utils.getCurrentUserOauthToken());
			
			return new ResponseEntity<>(dashboardResutl, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(dashboardResutl, HttpStatus.FORBIDDEN);
		}
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = byte[].class))))
	@Operation(summary = "Generate image of dashboard")
	@GetMapping(PATH + "/generateDashboardImage/{identification}")
	public ResponseEntity<byte[]> generateDashboardImage(@RequestHeader(value = "Authorization") String bearerToken,
			@Parameter(description = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@Parameter(description = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@Parameter(description = "Render Height", required = true) @RequestParam("height") int height,
			@Parameter(description = "Render Width", required = true) @RequestParam("width") int width,
			@Parameter(description = "Fullpage", required = false) @RequestParam(value = "fullpage", defaultValue = "false") Boolean fullpage,
			@Parameter(description = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {
		final Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else if (!dashboardService.hasUserViewPermission(id, utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return dashboardService.generateImgFromDashboardId(id, waittime, height, width,
				fullpage == null ? false : fullpage, params, prepareRequestToken(bearerToken));
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = byte[].class))))
	@Operation(summary = "Generate PDF of dashboard")
	@GetMapping(PATH + "/generatePDFImage/{identification}")
	public ResponseEntity<byte[]> generatePDFImage(@RequestHeader(value = "Authorization") String bearerToken,
			@Parameter(description = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@Parameter(description = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@Parameter(description = "Render Height", required = true) @RequestParam("height") int height,
			@Parameter(description = "Render Width", required = true) @RequestParam("width") int width,
			@Parameter(description = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {
		final Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else if (!dashboardService.hasUserViewPermission(id, utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return dashboardService.generatePDFFromDashboardId(id, waittime, height, width, params,
				prepareRequestToken(bearerToken));
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardUserAccessDTO.class))))
	@Operation(summary = "Get dashboard authorizations by identification")
	@GetMapping(PATH + "/{identification}/authorizations")
	public ResponseEntity<?> getDashboardAuthorizationsByIdentification(
			@Parameter(description = "dashboard identification", required = true) @PathVariable("identification") String identification) {

		final Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return new ResponseEntity<>(USER_NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		final List<DashboardUserAccess> dashAccesses = dashboardService.getDashboardUserAccesses(dashboard);
		final List<DashboardUserAccessDTO> dashAccessesDto = dashboardFIQL.dashAuthstoDTO(dashAccesses);

		return new ResponseEntity<>(dashAccessesDto, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO.class))))
	@Operation(summary = "Create dashboard authorization")
	@PostMapping(PATH + "/{identification}/authorizations")
	public ResponseEntity<?> createDashboardAuthorizations(
			@Parameter(description = "dashboard identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "UserAccessDTO", required = true) @Valid @RequestBody List<DashboardUserAccessDTO> usersAccessDTO,
			Errors errors) {

		final Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(String.format("The dashboard with identification %s does not exist.", identification));
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format(
					"The user %s is not allowed to create authorizations for the dashboard with identification %s",
					utils.getUserId(), identification));
		}

		return new ResponseEntity<>(dashboardService.insertDashboardUserAccess(dashboard, usersAccessDTO, false),
				HttpStatus.OK);

	}

	@Operation(summary = "Get all Gadgets from a list of Dashboards")
	@PostMapping(PATH + "/gadgetsFromDashboards")
	public ResponseEntity<?> gadgetsFromDashboards(
			@Parameter(description = "Dashboards id list", required = true) @RequestBody List<String> dashboardsList,
			Errors errors) {
		JSONArray result = new JSONArray();
		result = dashboardService.getGadgets(dashboardsList, utils.getUserId());

		return new ResponseEntity<>(result.toString(), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DashboardDTO.class))))
	@Operation(summary = "Update dashboard authorization")
	@PutMapping(PATH + "/{identification}/authorizations")
	public ResponseEntity<?> updateDashboardAuthorizations(
			@Parameter(description = "dashboard id or identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "UserAccessDTO", required = true) @Valid @RequestBody List<DashboardUserAccessDTO> uADTOs,
			Errors errors) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null) {
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.OK);
			}
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format(
					"The user %s is not allowed to create authorizations for the dashboard with identification %s",
					utils.getUserId(), identification));
		}
		return new ResponseEntity<>(dashboardService.insertDashboardUserAccess(dashboard, uADTOs, true), HttpStatus.OK);

	}

	@Operation(summary = "Delete dashboard authorization")
	@DeleteMapping("/{identification}/authorizations/{userId}")
	public ResponseEntity<?> deleteDashboardAuthorization(
			@Parameter(description = "dashboard id or identification", example = "developer", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "userId", required = true) @PathVariable("userId") String userId) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null) {
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.OK);
			}
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format(
					"The user %s is not allowed to delete user authorizations for the dashboard with identification %s",
					utils.getUserId(), dashboard.getIdentification()));
		}

		final DashboardUserAccessDTO uADTO = new DashboardUserAccessDTO();
		uADTO.setUserId(userId);
		final List<DashboardUserAccessDTO> uADTOs = new ArrayList<DashboardUserAccessDTO>();
		uADTOs.add(uADTO);
		return new ResponseEntity<>(
				dashboardService.deleteDashboardUserAccess(uADTOs, dashboard.getIdentification(), true), HttpStatus.OK);

	}

	@Operation(summary = "Delete several dashboard authorizations")
	@DeleteMapping("/{identification}/authorizations")
	public ResponseEntity<?> deleteDashboardAuthorizations(
			@Parameter(description = "dashboard identification or id", example = "developer", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "UserAccessDTO", required = true) @Valid @RequestBody List<DashboardUserAccessDTO> uADTOs,
			Errors errors) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null) {
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.OK);
			}

		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format(
					"The user %s is not allowed to delete user authorizations for the dashboard with identification %s",
					utils.getUserId(), identification));
		}

		return new ResponseEntity<>(
				dashboardService.deleteDashboardUserAccess(uADTOs, dashboard.getIdentification(), false),
				HttpStatus.OK);

	}

	private String prepareRequestToken(String rawToken) {
		return rawToken.substring("Bearer ".length()).trim();
	}

	@Operation(summary = "Set image to dashboard")
	@PostMapping(value = "/{identification}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> setImage(
			@Parameter(description = "dashboard identification or id", required = true) @PathVariable("identification") String identification,
			@RequestParam("file") MultipartFile imageFile) throws IOException {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, CONSTANT_DASHBOARD_NOT_FOUND),
						HttpStatus.NOT_FOUND);
			}
		}
		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, USER_NOT_AUTHORIZED),
					HttpStatus.UNAUTHORIZED);
		}
		if (imageFile.getSize() <= 0) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, ATTACHMENT_COULDN_T_BE_FOUND),
					HttpStatus.BAD_REQUEST);
		}
		if (utils.isFileExtensionForbidden(imageFile)) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, FILE_EXTENSION_NOT_ALLOWED),
					HttpStatus.BAD_REQUEST);
		}
		if (imageFile.getSize() > utils.getMaxFileSizeAllowed().longValue()) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, FILE_SIZE_IS_LARGER_THAN_MAX_ALLOWED),
					HttpStatus.BAD_REQUEST);
		}

		try {
			dashboardService.setImage(dashboard, imageFile.getBytes());
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, IMAGE_SUCCESSFULLY_UPLOADED),
					HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete image from dashboard")
	@DeleteMapping("/{identification}/image")
	public ResponseEntity<String> deleteImage(
			@Parameter(description = "dashboard identification or id", required = true) @PathVariable("identification") String identification) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, CONSTANT_DASHBOARD_NOT_FOUND),
						HttpStatus.NOT_FOUND);
			}
		}
		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, USER_NOT_AUTHORIZED),
					HttpStatus.UNAUTHORIZED);
		}

		try {
			dashboardService.setImage(dashboard, null);
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, IMAGE_SUCCESSFULLY_DELETED), HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Clone Dashboard by identification")
	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> cloneDashboard(
			@Parameter(description = "Dashboard identifier to clone") @RequestParam(required = true) String identification,
			@Parameter(description = "New dashboard identifier") @RequestParam(required = true) String newIdentification) {

		try {
			if (newIdentification == null || newIdentification.trim().length() < 5) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ERROR_5_CHARACTERS),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			String id = "";
			final String userId = utils.getUserId();

			id = dashboardService.cloneDashboard(dashboardService.getDashboardByIdentification(identification, userId),
					newIdentification, userId);

			if (!dashboardService.dashboardExistsById(id)) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, id), HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

}

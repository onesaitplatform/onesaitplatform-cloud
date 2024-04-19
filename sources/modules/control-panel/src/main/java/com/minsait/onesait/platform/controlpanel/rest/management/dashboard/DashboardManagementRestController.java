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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "Dashboard Management", tags = { "Dashoard management service" })
@RequestMapping("api/dashboards")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
public class DashboardManagementRestController {

	private static final String DASHBOARDNOTFOUND_STR = "\"Dashboard not found\"";
	private static final String USER_NOT_AUTHORIZED = "\"User not authorized\"";

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

	private static final String PATH = "/dashboard";
	private static final String CONSTANT_DASHBOARD_NOT_FOUND = "\"Dashboard not found\"";
	private static final String CONSTANT_DASHBOARD_NOT_FOUND_OR_UNAUTHORIZED = "\"Dashboard not found or unauthorized\"";

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO[].class))
	@ApiOperation(value = "Get dashboards")
	@GetMapping
	public ResponseEntity<DashboardDTO[]> getAll(
			@RequestParam(value = "orderType", required = false) DashboardOrder order) {
		order = order == null ? DashboardOrder.IDENTIFICATION_ASC : order;
		final List<Dashboard> dashboards = dashboardService.getByUserIdOrdered(utils.getUserId(), order);
		final DashboardDTO[] dashboardsDTO = new DashboardDTO[dashboards.size()];
		int i = 0;
		for (final Dashboard dashboard : dashboards) {
			final CategoryRelation categoryRelationship = categoryRelationService.getByIdType(dashboard.getId());
			String categoryIdentification = null;
			String subCategoryIdentification = null;
			if (categoryRelationship != null) {
				final Category category = categoryService.getCategoryById(categoryRelationship.getCategory());
				final Subcategory subcategory = subCategoryService
						.getSubcategoryById(categoryRelationship.getSubcategory());
				categoryIdentification = category.getIdentification();
				subCategoryIdentification = subcategory.getIdentification();
			}

			final int ngadgets = dashboardService.getNumGadgets(dashboard);

			final List<DashboardUserAccess> dashaccesses = new ArrayList<>();

			if (dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
				dashaccesses.addAll(dashboardService.getDashboardUserAccesses(dashboard));
			}

			DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboard, url, viewUrl, categoryIdentification,
					subCategoryIdentification, ngadgets, dashaccesses);

			dashboardsDTO[i] = dashboardDTO;
			i++;
		}

		return new ResponseEntity<>(dashboardsDTO, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Get dashboard by identification or id")
	@GetMapping(PATH + "/{identification}")
	public ResponseEntity<DashboardDTO> getDashboardByIdentification(
			@ApiParam(value = "dashboard identification or id", required = true) @PathVariable("identification") String identification) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null)
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		if (!dashboardService.hasUserViewPermission(dashboard.getId(), utils.getUserId()))
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

		CategoryRelation categoryRelationship = categoryRelationService.getByIdType(dashboard.getId());
		String categoryIdentification = null;
		String subCategoryIdentification = null;
		if (categoryRelationship != null) {
			Category category = categoryService.getCategoryById(categoryRelationship.getCategory());
			Subcategory subcategory = subCategoryService.getSubcategoryById(categoryRelationship.getSubcategory());
			categoryIdentification = category.getIdentification();
			subCategoryIdentification = subcategory.getIdentification();
		}

		final int ngadgets = dashboardService.getNumGadgets(dashboard);

		final List<DashboardUserAccess> dashaccesses = new ArrayList<>();

		if (dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			dashaccesses.addAll(dashboardService.getDashboardUserAccesses(dashboard));
		}

		DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboard, url, viewUrl, categoryIdentification,
				subCategoryIdentification, ngadgets, dashaccesses);

		return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Create new dashboard")
	@PostMapping
	public ResponseEntity<?> create(
			@ApiParam(value = "CommandDTO", required = true) @Valid @RequestBody CommandDTO commandDTO, Errors errors) {
		try {
			final DashboardCreateDTO dashboardCreateDTO = dashboardFIQL.fromCommandToDashboardCreate(commandDTO, null);

			if (!dashboardCreateDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN_SPACES)) {
				return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_', ' '",
						HttpStatus.BAD_REQUEST);
			}

			String dashboardId = dashboardService.createNewDashboard(dashboardCreateDTO, utils.getUserId());

			final Dashboard dashboardCreated = dashboardService.getDashboardById(dashboardId, utils.getUserId());

			final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboardCreated, url, viewUrl,
					dashboardCreateDTO.getCategory(), dashboardCreateDTO.getSubcategory(),
					dashboardService.getNumGadgets(dashboardCreated),
					dashboardService.getDashboardUserAccesses(dashboardCreated));

			return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);

		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Delete dashboard by identification or id")
	@DeleteMapping("/{identification}")
	public ResponseEntity<?> delete(
			@ApiParam(value = "dashboard identification or id", example = "dashboardId", required = true) @PathVariable("identification") String identification) {
		try {
			Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
			if (dashboard == null) {
				dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
				if (dashboard == null)
					return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId()))
				return new ResponseEntity<>(USER_NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);

			dashboardService.deleteDashboard(dashboard.getId(), utils.getUserId());
			return new ResponseEntity<>("\"Dashboard deleted successfully\"", HttpStatus.OK);
		} catch (final GadgetDatasourceServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Update dashboard")
	@PutMapping("/{identification}")
	public ResponseEntity<?> update(
			@ApiParam(value = "dashboard identification or id") @PathVariable(value = "identification") String identification,
			@ApiParam(value = "CommandDTO", required = true) @Valid @RequestBody UpdateCommandDTO updateDTO,
			Errors errors) {
		try {

			Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());

			if (dashboard == null) {
				dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
				if (dashboard == null)
					return new ResponseEntity<>("Dashboard does not exist.", HttpStatus.OK);
			}

			if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId()))
				return new ResponseEntity<>(USER_NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);

			if (updateDTO.getInformation() == null
					&& (updateDTO.getDescription() != null || updateDTO.getIdentification() != null)) {

				if (!dashboard.getIdentification().equals(updateDTO.getIdentification()) && dashboardService
						.getDashboardByIdentification(updateDTO.getIdentification(), utils.getUserId()) != null) {
					return new ResponseEntity<>("The identification is already used by another dashboard",
							HttpStatus.BAD_REQUEST);
				}
				// retrocompatibility, allowing to define is public as a field
				DashboardSimplifiedDTO dashSimplified = new DashboardSimplifiedDTO();
				if (updateDTO.getDescription() != null)
					dashSimplified.setDescription(updateDTO.getDescription());
				if (updateDTO.getIdentification() != null)
					dashSimplified.setIdentification(updateDTO.getIdentification());
				if (updateDTO.getIsPublic() != null)
					dashSimplified.setPublic(updateDTO.getIsPublic());
				else
					dashSimplified.setPublic(false);

				dashboardService.updateDashboardSimplified(dashboard.getId(), dashSimplified, utils.getUserId());
				Dashboard dashboardUpdated = dashboardService.getDashboardById(dashboard.getId(), utils.getUserId());
				final DashboardDTO dashboardDTO = dashboardFIQL.toDashboardDTO(dashboardUpdated, url, viewUrl, null,
						null, dashboardService.getNumGadgets(dashboardUpdated),
						dashboardService.getDashboardUserAccesses(dashboardUpdated));
				return new ResponseEntity<>(dashboardDTO, HttpStatus.OK);
			} else {

				final DashboardCreateDTO dashboardCreateDTO = dashboardFIQL
						.fromCommandToDashboardCreate(dashboardFIQL.fromUpdateToCommand(updateDTO), dashboard.getId());

				if (!dashboard.getIdentification().equals(dashboardCreateDTO.getIdentification())
						&& dashboardService.getDashboardByIdentification(dashboardCreateDTO.getIdentification(),
								utils.getUserId()) != null) {
					return new ResponseEntity<>("The identification is already used by another dashboard",
							HttpStatus.BAD_REQUEST);
				}
				String dashboardId = dashboardService.updatePublicDashboard(dashboardCreateDTO, utils.getUserId());

				final Dashboard dashboardUpdated = dashboardService.getDashboardById(dashboardId, utils.getUserId());

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

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardExportDTO.class))
	@ApiOperation(value = "Export dashboard by id")
	@GetMapping("/export/{id}")
	public ResponseEntity<?> exportDashboard(
			@ApiParam(value = "dashboard id", required = true) @PathVariable("id") String dashboardId) {
		DashboardExportDTO dashboardExportDTO;
		try {
			dashboardExportDTO = dashboardService.exportDashboardDTO(dashboardId, utils.getUserId());

		} catch (DashboardServiceException e) {
			switch (e.getErrorType()) {
			case NOT_FOUND:
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND_OR_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			default:
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		}
		return new ResponseEntity<>(dashboardExportDTO, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Import dashboard")
	@PostMapping("/import")
	public ResponseEntity<?> importDashboard(
			@ApiParam(value = "Overwrite Dashboard if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@ApiParam(value = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@ApiParam(value = "DashboardDTO", required = true) @Valid @RequestBody DashboardExportDTO dashboardimportDTO,
			Errors errors) {

		if (!dashboardimportDTO.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN_SPACES)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_', ' '",
					HttpStatus.BAD_REQUEST);
		}

		DashboardImportResponsetDTO dashboardResutl = dashboardService.importDashboard(dashboardimportDTO,
				utils.getUserId(), overwrite, importAuthorizations);
		dashboardResutl.setIdentification(dashboardimportDTO.getIdentification());
		if (dashboardResutl.getId() != null) {
			return new ResponseEntity<>(dashboardResutl, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(dashboardResutl, HttpStatus.FORBIDDEN);
		}
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = byte[].class))
	@ApiOperation(value = "Generate image of dashboard")
	@GetMapping(PATH + "/generateDashboardImage/{identification}")
	public ResponseEntity<byte[]> generateDashboardImage(@RequestHeader(value = "Authorization") String bearerToken,
			@ApiParam(value = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@ApiParam(value = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@ApiParam(value = "Render Height", required = true) @RequestParam("height") int height,
			@ApiParam(value = "Render Width", required = true) @RequestParam("width") int width,
			@ApiParam(value = "Fullpage", required = false, defaultValue = "false") @RequestParam("fullpage") Boolean fullpage,
			@ApiParam(value = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {
		Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else if (!dashboardService.hasUserViewPermission(id, utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return dashboardService.generateImgFromDashboardId(id, waittime, height, width,
				(fullpage == null ? false : fullpage), params, prepareRequestToken(bearerToken));
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = byte[].class))
	@ApiOperation(value = "Generate PDF of dashboard")
	@GetMapping(PATH + "/generatePDFImage/{identification}")
	public ResponseEntity<byte[]> generatePDFImage(@RequestHeader(value = "Authorization") String bearerToken,
			@ApiParam(value = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@ApiParam(value = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@ApiParam(value = "Render Height", required = true) @RequestParam("height") int height,
			@ApiParam(value = "Render Width", required = true) @RequestParam("width") int width,
			@ApiParam(value = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {
		Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else if (!dashboardService.hasUserViewPermission(id, utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return dashboardService.generatePDFFromDashboardId(id, waittime, height, width, params,
				prepareRequestToken(bearerToken));
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardUserAccessDTO.class))
	@ApiOperation(value = "Get dashboard authorizations by identification")
	@GetMapping(PATH + "/{identification}/authorizations")
	public ResponseEntity<?> getDashboardAuthorizationsByIdentification(
			@ApiParam(value = "dashboard identification", required = true) @PathVariable("identification") String identification) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId()))
			return new ResponseEntity<>(USER_NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);

		final List<DashboardUserAccess> dashAccesses = dashboardService.getDashboardUserAccesses(dashboard);
		final List<DashboardUserAccessDTO> dashAccessesDto = dashboardFIQL.dashAuthstoDTO(dashAccesses);

		return new ResponseEntity<>(dashAccessesDto, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Create dashboard authorization")
	@PostMapping(PATH + "/{identification}/authorizations")
	public ResponseEntity<?> createDashboardAuthorizations(
			@ApiParam(value = "dashboard identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "UserAccessDTO", required = true) @Valid @RequestBody List<DashboardUserAccessDTO> usersAccessDTO,
			Errors errors) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
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

	@ApiOperation(value = "Get all Gadgets from a list of Dashboards")
	@PostMapping(PATH + "/gadgetsFromDashboards")
	public ResponseEntity<?> gadgetsFromDashboards(
			@ApiParam(value = "Dashboards id list", required = true) @RequestBody List<String> dashboardsList,
			Errors errors) {
		JSONArray result = new JSONArray();
		result = dashboardService.getGadgets(dashboardsList, utils.getUserId());

		return new ResponseEntity<>(result.toString(), HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = DashboardDTO.class))
	@ApiOperation(value = "Update dashboard authorization")
	@PutMapping(PATH + "/{identification}/authorizations")
	public ResponseEntity<?> updateDashboardAuthorizations(
			@ApiParam(value = "dashboard id or identification", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "UserAccessDTO", required = true) @Valid @RequestBody List<DashboardUserAccessDTO> uADTOs,
			Errors errors) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null)
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.OK);
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format(
					"The user %s is not allowed to create authorizations for the dashboard with identification %s",
					utils.getUserId(), identification));
		}
		return new ResponseEntity<>(dashboardService.insertDashboardUserAccess(dashboard, uADTOs, true), HttpStatus.OK);

	}

	@ApiOperation(value = "Delete dashboard authorization")
	@DeleteMapping("/{identification}/authorizations/{userId}")
	public ResponseEntity<?> deleteDashboardAuthorization(
			@ApiParam(value = "dashboard id or identification", example = "developer", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "userId", required = true) @PathVariable("userId") String userId) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null)
				return new ResponseEntity<>(DASHBOARDNOTFOUND_STR, HttpStatus.OK);
			return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.OK);
		}

		if (!dashboardService.hasUserEditPermission(dashboard.getId(), utils.getUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format(
					"The user %s is not allowed to delete user authorizations for the dashboard with identification %s",
					utils.getUserId(), dashboard.getIdentification()));
		}

		DashboardUserAccessDTO uADTO = new DashboardUserAccessDTO();
		uADTO.setUserId(userId);
		List<DashboardUserAccessDTO> uADTOs = new ArrayList<DashboardUserAccessDTO>();
		uADTOs.add(uADTO);
		return new ResponseEntity<>(
				dashboardService.deleteDashboardUserAccess(uADTOs, dashboard.getIdentification(), true), HttpStatus.OK);

	}

	@ApiOperation(value = "Delete several dashboard authorizations")
	@DeleteMapping("/{identification}/authorizations")
	public ResponseEntity<?> deleteDashboardAuthorizations(
			@ApiParam(value = "dashboard identification or id", example = "developer", required = true) @PathVariable("identification") String identification,
			@ApiParam(value = "UserAccessDTO", required = true) @Valid @RequestBody List<DashboardUserAccessDTO> uADTOs,
			Errors errors) {

		Dashboard dashboard = dashboardService.getDashboardByIdentification(identification, utils.getUserId());
		if (dashboard == null) {
			dashboard = dashboardService.getDashboardById(identification, utils.getUserId());
			if (dashboard == null)
				return new ResponseEntity<>(CONSTANT_DASHBOARD_NOT_FOUND, HttpStatus.OK);

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
}

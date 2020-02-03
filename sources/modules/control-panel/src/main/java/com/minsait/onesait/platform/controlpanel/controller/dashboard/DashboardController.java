/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.dashboard;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.CategoryRelation.Type;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardAccessDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardExportDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.EditorDTO;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dashboards")
@Controller
@Slf4j
public class DashboardController {

	private static final String USERS = "users";
	private static final String CATEGORIES = "categories";
	private static final String DASHBOARD_VALIDATION_ERROR = "dashboard.validation.error";
	private static final String REDIRECT_DASHBOARDS_VIEW = "dashboards/view";
	private static final String REDIRECT_ERROR_403 = "error/403";

	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private SubcategoryService subcategoryService;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private SubcategoryRepository subcategoryRepository;
	@Autowired
	private CategoryRelationRepository categoryRelationRepository;

	@Autowired(required = false)
	private JWTService jwtService;

	private static final String BLOCK_PRIOR_LOGIN = "block_prior_login";
	private static final String DASHBOARD_STR = "dashboard";
	private static final String DASHB_CREATE = "dashboards/create";
	private static final String DASHB_EDIT = "dashboards/edit";
	private static final String REDIRECT_DASHB_EDIT = "redirect:/dashboards/edit/";
	private static final String REDIRECT_DASHB_CREATE = "redirect:/dashboards/create";
	private static final String REDIRECT_DASHB_CREATE_SYNOPTIC = "redirect:/dashboards/createsynoptic";
	private static final String REDIRECT_DASHB_LIST = "redirect:/dashboards/list/";
	private static final String CREDENTIALS_STR = "credentials";
	private static final String HEADERLIBS = "headerlibs";
	private static final String EDITION = "edition";
	private static final String IFRAME = "iframe";
	private static final String SYNOPT = "synop";
	private static final String IOTBROKERURL = "iotbrokerurl";
	private static final String FIXED = "fixed";
	private static final String ERROR_DASHBOARD_IMAGE = "Error generating Dashboard Image";
	private static final String ERROR_DASHBOARD_PDF = "Error generating Dashboard PDF";
	private static final String DATE_PATTERN = "_yyyy_MM_dd_HH_mm_ss";

	@Value("${onesaitplatform.urls.iotbroker}")
	private String IOTRBROKERSERVER;

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}

		final List<DashboardDTO> dashboard = dashboardService
				.findDashboardWithIdentificationAndDescription(identification, description, utils.getUserId());

		uiModel.addAttribute("dashboards", dashboard);

		return "dashboards/list";

	}

	@RequestMapping(value = "/viewerlist", produces = "text/html")
	public String viewerlist(Model uiModel, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}

		final List<DashboardDTO> dashboard = dashboardService
				.findDashboardWithIdentificationAndDescription(identification, description, utils.getUserId());
		uiModel.addAttribute("dashboards", dashboard);
		return "dashboards/viewerlist";

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute(DASHBOARD_STR, new DashboardCreateDTO());
		model.addAttribute(USERS, getUserListDTO());
		model.addAttribute(CATEGORIES, categoryService.findAllCategories());
		model.addAttribute("schema", dashboardConfRepository.findAll());
		return DASHB_CREATE;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/createsynoptic")
	public String createsynoptic(Model model) {
		DashboardCreateDTO dash = new DashboardCreateDTO();
		dash.setType(DashboardType.SYNOPTIC);
		model.addAttribute(DASHBOARD_STR, dash);
		model.addAttribute(USERS, getUserListDTO());
		model.addAttribute(CATEGORIES, categoryService.findAllCategories());
		model.addAttribute("schema", dashboardConfRepository.findByIdentification(FIXED));
		return DASHB_CREATE;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardEditById(id, utils.getUserId()));
		return DASHB_CREATE;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/edit/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardEditById(id, utils.getUserId()));
		return DASHB_EDIT;

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = "/edit/{id}", produces = "text/html")
	public String updateEditDashboardModel(Model model, @PathVariable("id") String id, @Valid Dashboard dashboard,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some DashboardConf properties missing");
			utils.addRedirectMessage(DASHBOARD_VALIDATION_ERROR, redirect);
			return REDIRECT_DASHB_EDIT;
		}
		try {
			if (dashboardService.hasUserEditPermission(id, utils.getUserId())) {
				dashboardService.saveDashboardModel(id, dashboard.getModel(), utils.getUserId());
			} else {
				throw new DashboardServiceException(
						"Cannot update Dashboard that does not exist or don't have permission");
			}
			return REDIRECT_DASHB_LIST;

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return "REDIRECT_DASHB_EDIT" + dashboard.getId();
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> cloneDashboard(Model model, @RequestParam String dashboardId,
			@RequestParam String identification) {

		try {
			String id = "";
			String userId = utils.getUserId();

			id = dashboardService.cloneDashboard(dashboardService.getDashboardById(dashboardId, userId), identification,
					userService.getUser(userId));

			Dashboard dashboard = dashboardRepository.findById(id);
			return new ResponseEntity<>(dashboard.getId(), HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PostMapping(value = { "/create" })
	public String createDashboard(Model model, @Valid DashboardCreateDTO dashboard, BindingResult bindingResult,
			HttpServletRequest request, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(DASHBOARD_VALIDATION_ERROR, redirect);
			return REDIRECT_DASHB_CREATE;
		}

		try {

			final String dashboardId = dashboardService.createNewDashboard(dashboard, utils.getUserId());
			return "redirect:/dashboards/editfull/" + dashboardId;

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return REDIRECT_DASHB_CREATE;
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PostMapping(value = { "/createsynoptic" })
	public String createsynopticDashboard(Model model, @Valid DashboardCreateDTO dashboard, BindingResult bindingResult,
			HttpServletRequest request, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(DASHBOARD_VALIDATION_ERROR, redirect);
			return REDIRECT_DASHB_CREATE_SYNOPTIC;
		}

		try {

			final String dashboardId = dashboardService.createNewDashboard(dashboard, utils.getUserId());
			return "redirect:/dashboards/editfull/" + dashboardId;

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return REDIRECT_DASHB_CREATE_SYNOPTIC;
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PostMapping(value = { "/dashboardconf/{id}" })
	public String saveUpdateDashboard(@PathVariable("id") String id, DashboardCreateDTO dashboard,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(DASHBOARD_VALIDATION_ERROR, redirect);
			return REDIRECT_DASHB_CREATE;
		}

		try {
			if (dashboardService.hasUserEditPermission(id, utils.getUserId())) {
				dashboardService.cleanDashboardAccess(dashboard, utils.getUserId());
				dashboardService.saveUpdateAccess(dashboard, utils.getUserId());
				dashboardService.updatePublicDashboard(dashboard, utils.getUserId());

			} else {
				throw new DashboardServiceException(
						"Cannot update Dashboard that does not exist or don't have permission");
			}
			return REDIRECT_DASHB_LIST;

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return "redirect:/dashboards/dashboardconf/" + dashboard.getId();
		}
	}

	@GetMapping(value = "/dashboardconf/{id}", produces = "text/html")
	public String updateDashboard(Model model, @PathVariable("id") String id) {
		final Dashboard dashboard = dashboardService.getDashboardEditById(id, utils.getUserId());

		if (dashboard != null) {

			final DashboardCreateDTO dashBDTO = new DashboardCreateDTO();

			dashBDTO.setId(id);
			dashBDTO.setIdentification(dashboard.getIdentification());
			dashBDTO.setDescription(dashboard.getDescription());
			dashBDTO.setHeaderlibs(dashboard.getHeaderlibs());
			dashBDTO.setType(dashboard.getType());
			if (null != dashboard.getImage()) {
				dashBDTO.setHasImage(Boolean.TRUE);
			} else {
				dashBDTO.setHasImage(Boolean.FALSE);
			}
			dashBDTO.setPublicAccess(dashboard.isPublic());
			final List<DashboardUserAccess> userAccess = dashboardService.getDashboardUserAccesses(dashboard);
			if (userAccess != null && !userAccess.isEmpty()) {
				final ArrayList<DashboardAccessDTO> list = new ArrayList<>();
				for (final Iterator<DashboardUserAccess> iterator = userAccess.iterator(); iterator.hasNext();) {
					final DashboardUserAccess dua = iterator.next();
					if (userIsActive(dua.getUser().getUserId())) {
						final DashboardAccessDTO daDTO = new DashboardAccessDTO();
						daDTO.setAccesstypes(dua.getDashboardUserAccessType().getName());
						daDTO.setUsers(dua.getUser().getUserId());
						list.add(daDTO);
					}
				}
				final ObjectMapper objectMapper = new ObjectMapper();
				try {
					dashBDTO.setAuthorizations(objectMapper.writeValueAsString(list));
				} catch (final JsonProcessingException e) {
					log.error(e.getMessage());
				}
			}

			final List<CategoryRelation> categoryRelationList = categoryRelationRepository
					.findByTypeIdAndType(dashboard.getId(), Type.DASHBOARD);
			if (categoryRelationList != null && !categoryRelationList.isEmpty()) {
				final Category category = categoryRepository.findById(categoryRelationList.get(0).getCategory());
				dashBDTO.setCategory(category.getIdentification());
				final Subcategory subcategory = subcategoryRepository
						.findById(categoryRelationList.get(0).getSubcategory());
				dashBDTO.setSubcategory(subcategory.getIdentification());
			}

			model.addAttribute(DASHBOARD_STR, dashBDTO);
			model.addAttribute(USERS, getUserListDTO());
			model.addAttribute(CATEGORIES, categoryService.findAllCategories());

			return DASHB_CREATE;
		} else {
			return "redirect:/dashboards/list";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/editor/{id}", produces = "text/html")
	public String editorDashboard(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardById(id, utils.getUserId()));
		model.addAttribute(CREDENTIALS_STR, dashboardService.getCredentialsString(utils.getUserId()));
		return "dashboards/editor";

	}

	@GetMapping(value = "/model/{id}", produces = "application/json")
	public @ResponseBody String getModelById(@PathVariable("id") String id) {
		return dashboardService.getDashboardById(id, utils.getUserId()).getModel();
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/editfull/{id}", produces = "text/html")
	public String editFullDashboard(Model model, @PathVariable("id") String id) {
		if (dashboardService.hasUserEditPermission(id, utils.getUserId())) {
			final Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
			model.addAttribute(DASHBOARD_STR, dashboard);
			model.addAttribute(CREDENTIALS_STR, dashboardService.getCredentialsString(utils.getUserId()));
			model.addAttribute(HEADERLIBS, dashboard.getHeaderlibs());
			model.addAttribute(EDITION, true);
			model.addAttribute(IFRAME, false);
			model.addAttribute(SYNOPT,
					dashboard.getType() != null && dashboard.getType().equals(DashboardType.SYNOPTIC));

			final String url = IOTRBROKERSERVER.concat("/iot-broker/rest");
			model.addAttribute(IOTBROKERURL, url);
			return REDIRECT_DASHBOARDS_VIEW;
		} else {
			return REDIRECT_ERROR_403;
		}
	}

	@GetMapping(value = "/view/{id}", produces = "text/html")
	public String viewerDashboard(Model model, @PathVariable("id") String id, HttpServletRequest request) {
		if (dashboardService.hasUserViewPermission(id, utils.getUserId())) {
			final Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
			model.addAttribute(DASHBOARD_STR, dashboard);
			model.addAttribute(CREDENTIALS_STR, dashboardService.getCredentialsString(utils.getUserId()));
			model.addAttribute(HEADERLIBS, dashboard.getHeaderlibs());
			model.addAttribute(EDITION, false);
			model.addAttribute(IFRAME, false);
			model.addAttribute(SYNOPT,
					dashboard.getType() != null && dashboard.getType().equals(DashboardType.SYNOPTIC));
			final String url = IOTRBROKERSERVER.concat("/iot-broker/rest");
			model.addAttribute(IOTBROKERURL, url);
			request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
			return REDIRECT_DASHBOARDS_VIEW;
		} else {
			request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
			return "redirect:/login";
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PutMapping(value = "/save/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateDashboard(@PathVariable("id") String id,
			@RequestParam("data") Dashboard dashboard) {
		dashboardService.saveDashboard(id, dashboard, utils.getUserId());
		return "ok";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PutMapping(value = "/savemodel/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateDashboardModel(@PathVariable("id") String id, @RequestBody EditorDTO model) {
		dashboardService.saveDashboardModel(id, model.getModel(), utils.getUserId());
		return "{\"ok\":true}";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PutMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String deleteDashboard(@PathVariable("id") String id) {

		try {
			dashboardService.deleteDashboard(id, utils.getUserId());
		} catch (final RuntimeException e) {
			return "{\"ok\":false, \"error\":\"" + e.getMessage() + "\"}";
		}
		return "{\"ok\":true}";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		try {
			dashboardService.deleteDashboard(id, utils.getUserId());
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, ra);
		}
		return REDIRECT_DASHB_LIST;
	}

	@RequestMapping(value = "/{id}/getImage")
	public void showImg(@PathVariable("id") String id, HttpServletResponse response) {
		final byte[] buffer = dashboardService.getImgBytes(id);
		if (buffer.length > 0) {
			try (OutputStream output = response.getOutputStream();) {
				response.setContentLength(buffer.length);
				output.write(buffer);
			} catch (final Exception e) {
				log.error("showImg", e);
			}
		}
	}

	@GetMapping(value = "/getSubcategories/{category}")
	public @ResponseBody List<String> getSubcategories(@PathVariable("category") String category,
			HttpServletResponse response) {
		return subcategoryService
				.findSubcategoriesNamesByCategory(categoryService.getCategoryByIdentification(category));
	}

	@GetMapping(value = "/editfulliframe/{id}", produces = "text/html")
	public String editFullDashboardIframe(Model model, @PathVariable("id") String id,
			@RequestParam("oauthtoken") String userToken) {

		try {
			OAuth2Authentication info = null;
			if (userToken != null) {
				info = (OAuth2Authentication) jwtService.getAuthentication(userToken);

				if (dashboardService.hasUserEditPermission(id, (String) info.getUserAuthentication().getPrincipal())) {
					final Dashboard dashboard = dashboardService.getDashboardById(id,
							(String) info.getUserAuthentication().getPrincipal());
					model.addAttribute(DASHBOARD_STR, dashboard);
					model.addAttribute(CREDENTIALS_STR, dashboardService
							.getCredentialsString((String) info.getUserAuthentication().getPrincipal()));
					model.addAttribute(HEADERLIBS, dashboard.getHeaderlibs());
					model.addAttribute(EDITION, true);
					model.addAttribute(IFRAME, true);
					model.addAttribute(SYNOPT,
							dashboard.getType() != null && dashboard.getType().equals(DashboardType.SYNOPTIC));
					final String url = IOTRBROKERSERVER.concat("/iot-broker/rest");
					model.addAttribute(IOTBROKERURL, url);
					return REDIRECT_DASHBOARDS_VIEW;
				} else {
					return REDIRECT_ERROR_403;
				}

			}
		} catch (final Exception e) {
			log.error("editFullDashboardIframe", e);
			return REDIRECT_ERROR_403;
		}
		return REDIRECT_ERROR_403;

	}

	@GetMapping(value = "/generateDashboardImage/{identification}")
	@ResponseBody
	public ResponseEntity<byte[]> generateDashboardImage(
			@ApiParam(value = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@ApiParam(value = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@ApiParam(value = "Render Height", required = true) @RequestParam("height") int height,
			@ApiParam(value = "Render Width", required = true) @RequestParam("width") int width,
			@ApiParam(value = "Fullpage", required = false, defaultValue = "false") @RequestParam("fullpage") Boolean fullpage,
			@ApiParam(value = "Authorization", required = true) @RequestParam("token") String bearerToken,
			@ApiParam(value = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {

		ResponseEntity<byte[]> responseEntity;

		responseEntity = dashboardService.generateImgFromDashboardId(id, waittime, height, width,
				(fullpage == null ? false : fullpage), params, bearerToken);
		final int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode != 200) {
			log.error(ERROR_DASHBOARD_IMAGE + ' ' + statusCode);
			throw new DashboardServiceException(ERROR_DASHBOARD_IMAGE + ' ' + statusCode);
		}

		final HttpHeaders headers = new HttpHeaders();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
		String date = simpleDateFormat.format(new Date());
		headers.set("Content-Type", "image/png;charset=utf-8");
		headers.set("Content-Disposition", "attachment; filename=\"" + id + "_" + date + ".png\"");
		return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);

	}

	@GetMapping(value = "/generatePDFImage/{identification}")
	@ResponseBody
	public ResponseEntity<byte[]> generateDashboardPDF(
			@ApiParam(value = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@ApiParam(value = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@ApiParam(value = "Render Height", required = true) @RequestParam("height") int height,
			@ApiParam(value = "Render Width", required = true) @RequestParam("width") int width,
			@ApiParam(value = "Authorization", required = true) @RequestParam("token") String bearerToken,
			@ApiParam(value = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {

		ResponseEntity<byte[]> responseEntity;

		responseEntity = dashboardService.generatePDFFromDashboardId(id, waittime, height, width, params, bearerToken);
		final int statusCode = responseEntity.getStatusCodeValue();
		if (statusCode != 200) {
			log.error(ERROR_DASHBOARD_PDF + ' ' + statusCode);
			throw new DashboardServiceException(ERROR_DASHBOARD_PDF + ' ' + statusCode);
		}

		final HttpHeaders headers = new HttpHeaders();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
		String date = simpleDateFormat.format(new Date());
		headers.set("Content-Disposition", "attachment; filename=\"" + id + "_" + date + ".pdf\"");
		headers.setContentType(MediaType.APPLICATION_PDF);
		return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);
	}

	@GetMapping(value = "/viewiframe/{id}", produces = "text/html")
	public String viewerDashboardIframe(Model model, @PathVariable("id") String id,
			@RequestParam("oauthtoken") String userToken, HttpServletRequest request) {

		try {
			OAuth2Authentication info = null;
			if (userToken != null) {
				info = (OAuth2Authentication) jwtService.getAuthentication(userToken);
				if (dashboardService.hasUserViewPermission(id, (String) info.getUserAuthentication().getPrincipal())) {
					final Dashboard dashboard = dashboardService.getDashboardById(id,
							(String) info.getUserAuthentication().getPrincipal());
					model.addAttribute(DASHBOARD_STR, dashboard);
					model.addAttribute(CREDENTIALS_STR, dashboardService
							.getCredentialsString((String) info.getUserAuthentication().getPrincipal()));
					model.addAttribute(HEADERLIBS, dashboard.getHeaderlibs());
					model.addAttribute(EDITION, false);
					model.addAttribute(IFRAME, true);
					model.addAttribute(SYNOPT,
							dashboard.getType() != null && dashboard.getType().equals(DashboardType.SYNOPTIC));
					final String url = IOTRBROKERSERVER.concat("/iot-broker/rest");
					model.addAttribute(IOTBROKERURL, url);
					request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
					return REDIRECT_DASHBOARDS_VIEW;
				} else {
					request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
					return "redirect:/login";
				}
			}
		} catch (final Exception e) {
			log.error("viewerDashboardIframe", e);
			return "redirect:/403";
		}
		return "redirect:/403";
	}

	private ArrayList<UserDTO> getUserListDTO() {
		final List<User> users = userService.getAllActiveUsers();
		final ArrayList<UserDTO> userList = new ArrayList<>();
		if (users != null && !users.isEmpty()) {
			for (final Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
				final User user = iterator.next();
				final UserDTO uDTO = new UserDTO();
				uDTO.setUserId(user.getUserId());
				uDTO.setFullName(user.getFullName());
				userList.add(uDTO);
			}
		}
		return userList;
	}

	private boolean userIsActive(String userId) {
		final User user = userService.getUser(userId);
		return user.isActive();
	}

	@Transactional

	@RequestMapping(value = "/importDashboard", method = RequestMethod.POST)
	@ResponseBody
	public String importDashboard(@RequestBody DashboardExportDTO dashboardimportDTO) {
		try {
			JSONObject response = new JSONObject();
			String identification = dashboardService.importDashboard(dashboardimportDTO, utils.getUserId())
					.getIdentification();
			response.put("status", HttpStatus.OK);
			response.put("message", identification);
			return response.toString();

		} catch (final DashboardServiceException e) {
			log.error("Cannot import dashboard: ", e);
			JSONObject response = new JSONObject();
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			response.put("message", e.getMessage());
			return response.toString();

		} catch (final Exception e) {
			log.error("Cannot import dashboard: ", e);
			JSONObject response = new JSONObject();
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			response.put("message", e.getMessage());
			return response.toString();
		}
	}

	@RequestMapping(value = "/exportDashboard/{id}", method = RequestMethod.GET, produces = "text/html")
	@ResponseBody
	public ResponseEntity<byte[]> exportDashboard(@PathVariable("id") String id, Model uiModel) {
		return dashboardService.exportDashboard(id, utils.getUserId(), utils.getCurrentUserOauthToken());
	}

}

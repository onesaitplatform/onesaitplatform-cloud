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
package com.minsait.onesait.platform.controlpanel.controller.dashboard;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.elasticsearch.core.Map;
import org.json.JSONException;
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
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.I18nResources;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.I18nResourcesRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardAccessDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardExportDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardImportResponsetDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardTablePaginationDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.EditorDTO;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dashboards")
@Controller
@Slf4j
public class DashboardController {

	private static final String USERS = "users";
	private static final String CATEGORIES = "categories";
	private static final String I18NS = "i18ns";
	private static final String I18NJSON = "i18njson";
	private static final String DASHBOARD_VALIDATION_ERROR = "dashboard.validation.error";
	private static final String REDIRECT_DASHBOARDS_VIEW = "dashboards/view";
	private static final String REDIRECT_ERROR_403 = "error/403";

	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private I18nResourcesRepository i18nRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private InternationalizationService internationalizationService;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private SubcategoryRepository subcategoryRepository;
	@Autowired
	private CategoryRelationRepository categoryRelationRepository;

	@Autowired
	private HttpSession httpSession;

	@Autowired()
	private ResourcesInUseService resourcesInUseService;

	@Autowired(required = false)
	private JWTService jwtService;

	private static final String BLOCK_PRIOR_LOGIN = "block_prior_login";
	private static final String DASHBOARD_STR = "dashboard";
	private static final String DASHB_CREATE = "dashboards/create";
	private static final String DASHB_EDIT = "dashboards/edit";
	private static final String REDIRECT_DASHB_EDIT = "redirect:/dashboards/edit/";
	private static final String REDIRECT_DASHB_CREATE = "redirect:/dashboards/create";
	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String REDIRECT_DASHB_CREATE_SYNOPTIC = "redirect:/dashboards/createsynoptic";
	private static final String REDIRECT_DASHB_LIST = "redirect:/dashboards/list/";
	private static final String HEADERLIBS = "headerlibs";
	private static final String EDITION = "edition";
	private static final String IFRAME = "iframe";
	private static final String SYNOPT = "synop";
	private static final String IOTBROKERURL = "iotbrokerurl";
	private static final String RESOURCEINUSE = "resourceinuse";
	private static final String FIXED = "fixed";
	private static final String ERROR_DASHBOARD_IMAGE = "Error generating Dashboard Image";
	private static final String ERROR_DASHBOARD_PDF = "Error generating Dashboard PDF";
	private static final String DATE_PATTERN = "_yyyy_MM_dd_HH_mm_ss";
	private static final String HEARTBEATTIMEOUT = "heartbeatMaxTimeout";
	private static final String PROTOCOL = "protocol";
	private static final String VIEW = "view";
	private static final String VIEWIFRAME = "viewiframe";
	private static final String EDITFULLIFRAME = "editfulliframe";
	private static final String EDITFULL = "editfull";
	private static final String APP_ID = "appId";

	@Value("${onesaitplatform.urls.iotbroker}")
	private String IOTRBROKERSERVER;

	@Value("${onesaitplatform.multitenancy.enabled:false}")
	private boolean multitenancyEnabled;

	@Autowired
	private MultitenancyService multitenancyService;

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request,
			@RequestParam(required = false, name = "name") String identification,
			@RequestParam(required = false, name = "type") String type) {

		// Scaping "" string values for parameters
		String currentTab = request.getParameter("current_tab");
		if (identification == null) {
			identification = "";
		}
		
		final String userRole = utils.getRoleOrParent();
		
		if (multitenancyEnabled) {
			multitenancyService.getVertical(MultitenancyContextHolder.getVerticalSchema()).ifPresent(v -> {
				uiModel.addAttribute("tenant", MultitenancyContextHolder.getTenantName());
				uiModel.addAttribute("vertical", v.getName());
			});

		}
		
		uiModel.addAttribute("userRole", userRole);
	
		uiModel.addAttribute("currentTab", currentTab);

		return "dashboards/list";

	}
	
	@PostMapping(value = "listdashboardpageable")
	public @ResponseBody DashboardTablePaginationDTO listDahboardpageable (HttpServletRequest request , 
			@RequestParam(required = false, name = "name") String identification) {
		
		
		
		Integer page = Integer.valueOf( request.getParameter("start"));
		Integer	limit = Integer.valueOf( request.getParameter("length"));
		Integer	draw = Integer.valueOf( request.getParameter("draw"));
		String filter = request.getParameter("search[value]");
		
		
		String columnIndex = request.getParameter("order[0][column]");
		String columName = request.getParameter("columns[" + columnIndex + "][name]");
		String order = request.getParameter("order[0][dir]");
		
		if(columName == null) {
			columName = "identification";
		}
		if(order == null) {
			order = "ASC";
		}
			
		final List<DashboardDTO> dashboardList  = dashboardService.findDashboardIdentification(filter, columName, order, utils.getUserId(), page, limit);
		final Integer countDashboard  = dashboardService.countDashboardIdentification(filter, utils.getUserId());
		
		
			
		DashboardTablePaginationDTO dashboardTable = new DashboardTablePaginationDTO();
				
		dashboardTable.setITotalRecords(countDashboard);		
		dashboardTable.setITotalDisplayRecords(countDashboard);
		dashboardTable.setDraw(draw);
		dashboardTable.setAaData(dashboardList);
						
		return dashboardTable;
			
			
	}
		
	
	@PostMapping(value = "listsynopticspageable")
	public @ResponseBody DashboardTablePaginationDTO listsynopticspageable (HttpServletRequest request , 
			@RequestParam(required = false, name = "name") String identification){
		
		Integer page = Integer.valueOf( request.getParameter("start"));
		Integer	limit = Integer.valueOf( request.getParameter("length"));
		Integer	draw = Integer.valueOf( request.getParameter("draw"));
		String filter = request.getParameter("search[value]");
		
		String columnIndex = request.getParameter("order[0][column]");
		String columName = request.getParameter("columns[" + columnIndex + "][name]");
		String order = request.getParameter("order[0][dir]");
		
		if(columName == null) {
			columName = "identification";
		}
		if(order == null) {
			order = "ASC";
		}
		
		
		final List<DashboardDTO> synopticsList  = dashboardService.findSynopticsIdentification(filter, columName, order, utils.getUserId(), page, limit);
		final Integer countSynoptics  = dashboardService.countSynopticIdentification(filter, utils.getUserId());
		
		
			
		DashboardTablePaginationDTO synopticsTable = new DashboardTablePaginationDTO();
				
		synopticsTable.setITotalRecords(countSynoptics);		
		synopticsTable.setITotalDisplayRecords(countSynoptics);
		synopticsTable.setDraw(draw);
		synopticsTable.setAaData(synopticsList);
						
		return synopticsTable;
			
			
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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		final DashboardCreateDTO dash = new DashboardCreateDTO();
		dash.setType(DashboardType.DASHBOARD);
		model.addAttribute(DASHBOARD_STR, dash);
		model.addAttribute(USERS, getUserListDTO());
		model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.DASHBOARD));
		model.addAttribute(I18NS, internationalizationService.getByUserIdOrPublic(utils.getUserId()));
		model.addAttribute("schema", dashboardConfRepository.findAllByOrderByIdentificationAsc());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		return DASHB_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/createsynoptic")
	public String createsynoptic(Model model) {
		final DashboardCreateDTO dash = new DashboardCreateDTO();
		dash.setType(DashboardType.SYNOPTIC);
		model.addAttribute(DASHBOARD_STR, dash);
		model.addAttribute(USERS, getUserListDTO());
		model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.DASHBOARD));
		model.addAttribute(I18NS, internationalizationService.getByUserIdOrPublic(utils.getUserId()));
		model.addAttribute("schema", dashboardConfRepository.findByIdentification(FIXED));
		return DASHB_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardEditById(id, utils.getUserId()));
		return DASHB_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/edit/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardEditById(id, utils.getUserId()));
		return DASHB_EDIT;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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
				dashboardService.generateDashboardImage(id, utils.getCurrentUserOauthToken());
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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> cloneDashboard(Model model, @RequestParam String dashboardId,
			@RequestParam String identification) {

		try {
			String id = "";
			final String userId = utils.getUserId();
			id = dashboardService.cloneDashboard(dashboardService.getDashboardById(dashboardId, userId), identification,
					userId);
			final Optional<Dashboard> opt = dashboardRepository.findById(id);
			if (!opt.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			final Dashboard dashboard = opt.get();
			return new ResponseEntity<>(dashboard.getId(), HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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

				dashboardService.createModifyI18nResource(id, dashboard, utils.getUserId());
				dashboardService.generateDashboardImage(id, utils.getCurrentUserOauthToken());

			} else {
				throw new DashboardServiceException(
						"Cannot update Dashboard that does not exist or don't have permission");
			}
			resourcesInUseService.removeByUser(id, utils.getUserId());
			return REDIRECT_DASHB_LIST;

		} catch (final DashboardServiceException e) {
			utils.addRedirectException(e, redirect);
			return "redirect:/dashboards/dashboardconf/" + dashboard.getId();
		}
	}

	@GetMapping(value = "/dashboardconf/{id}", produces = "text/html")
	public String updateDashboard(Model model, @PathVariable("id") String id) {
		final Dashboard dashboard = dashboardService.getDashboardEditById(id, utils.getUserId());
		// dashboardService.getAllInternationalizationJSON(dashboard);
		if (dashboard != null) {

			final DashboardCreateDTO dashBDTO = new DashboardCreateDTO();

			dashBDTO.setId(id);
			dashBDTO.setIdentification(dashboard.getIdentification());
			dashBDTO.setDescription(dashboard.getDescription());
			dashBDTO.setHeaderlibs(dashboard.getHeaderlibs());
			dashBDTO.setType(dashboard.getType());
			dashBDTO.setGenerateImage(dashboard.isGenerateImage());
			if (null != dashboard.getImage() && dashboard.getImage().length > 10 ) {
				dashBDTO.setHasImage(Boolean.TRUE);
			} else {
				dashBDTO.setHasImage(Boolean.FALSE);
			}
			dashBDTO.setPublicAccess(dashboard.isPublic());

			String i18nR = "";
			final List<I18nResources> i18nResources = i18nRepository.findByOPResourceId(dashboard.getId());
			for (int i = 0; i < i18nResources.size(); i++) {
				final I18nResources ir = i18nResources.get(i);
				if (i18nR.isEmpty()) {
					i18nR = ir.getI18n().getId();
				} else {
					i18nR = i18nR + "," + ir.getI18n().getId();
				}
			}
			dashBDTO.setI18n(i18nR);

			final List<DashboardUserAccess> userAccess = dashboardService.getDashboardUserAccesses(dashboard);
			if (userAccess != null && !userAccess.isEmpty()) {
				final ArrayList<DashboardAccessDTO> list = new ArrayList<>();
				for (DashboardUserAccess dua : userAccess) {
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

			final CategoryRelation categoryRelation = categoryRelationRepository.findByTypeIdAndType(dashboard.getId(),
					Category.Type.DASHBOARD);
			if (categoryRelation != null) {
				final Category category = categoryRepository.findById(categoryRelation.getCategory());
				dashBDTO.setCategory(category.getIdentification());
				final Subcategory subcategory = subcategoryRepository.findById(categoryRelation.getSubcategory());
				if (subcategory != null) {
					dashBDTO.setSubcategory(subcategory.getIdentification());
				}
			}

			model.addAttribute(DASHBOARD_STR, dashBDTO);

			String currentUser = utils.getUserId();

			ArrayList<UserDTO> usuarios = new ArrayList<UserDTO>();

			for (UserDTO user : getUserListDTO()) {

				if (!user.getUserId().equals(currentUser)) {

					usuarios.add(user);
				}

			}

			model.addAttribute(USERS, usuarios);

			model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.DASHBOARD));
			model.addAttribute(I18NS, internationalizationService.getByUserIdOrPublic(utils.getUserId()));
			model.addAttribute(I18NJSON, dashboardService.getAllInternationalizationJSON(dashboard).toString());
			model.addAttribute(RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
			resourcesInUseService.put(id, utils.getUserId());
			return DASHB_CREATE;
		} else

		{
			return "redirect:/dashboards/list";
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/editor/{id}", produces = "text/html")
	public String editorDashboard(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_STR, dashboardService.getDashboardById(id, utils.getUserId()));
		return "dashboards/editor";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/headerlibs/{id}", produces = "text/plain")
	public @ResponseBody String getHeaderLibsById(@PathVariable("id") String id) {
		return dashboardService.getDashboardById(id, utils.getUserId()).getHeaderlibs();
	}

	@GetMapping(value = "/isalive", produces = "text/plain")
	public @ResponseBody String isalive() {
		return "OK";
	}

	@GetMapping(value = "/model/{id}", produces = "application/json")
	public @ResponseBody String getModelById(@PathVariable("id") String id) {
		final Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
		final JSONObject dashboardModel = new JSONObject(dashboard.getModel());
		return dashboardModel.toString();
	}

	@GetMapping(value = "/i18n/{id}", produces = "application/json")
	public @ResponseBody String getI18NById(@PathVariable("id") String id, HttpServletResponse response) {
		utils.cleanInvalidSpringCookie(response);
		return dashboardService.getAllInternationalizationJSON(dashboardService.getDashboardById(id, utils.getUserId()))
				.toString();
	}

	@GetMapping(value = "/bunglemodel/{id}", produces = "application/json")
	public @ResponseBody ResponseEntity<DashboardExportDTO> getBungleModelById(@PathVariable("id") String id,
			HttpServletResponse response) {
		utils.cleanInvalidSpringCookie(response);
		ResponseEntity<DashboardExportDTO> dashboardResponse;
		try {
			dashboardResponse = new ResponseEntity<DashboardExportDTO>(
					dashboardService.getBungleDashboardDTO(id, utils.getUserId()), HttpStatus.OK);
		} catch (final DashboardServiceException e) {
			log.error(e.getMessage());
			switch (e.getErrorType()) {
			case NOT_FOUND:
				dashboardResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
				break;
			case UNAUTHORIZED:
				dashboardResponse = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				break;
			default:
				return null;
			}
		} catch (final Exception e) {
			log.error(e.getMessage());
			dashboardResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return dashboardResponse;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/editfull/{id}", produces = "text/html")
	public String editFullDashboard(Model model, @PathVariable("id") String id, HttpServletRequest request) {
		return openDashboard(model, id, null, request, EDITFULL);
	}

	@GetMapping(value = "/editfulliframe/{id}", produces = "text/html")
	public String editFullDashboardIframe(Model model, @PathVariable("id") String id,
			@RequestParam(value = "oauthtoken", required = false) String oauthtoken, HttpServletRequest request) {
		return openDashboard(model, id, oauthtoken, request, EDITFULLIFRAME);

	}

	@GetMapping(value = "/view/{id}", produces = "text/html")
	public String viewerDashboard(Model model, @PathVariable("id") String id, HttpServletRequest request) {
		return openDashboard(model, id, null, request, VIEW);
	}

	@GetMapping(value = "/viewiframe/{id}", produces = "text/html")
	public String viewerDashboardIframe(Model model, @PathVariable("id") String id,
			@RequestParam(value = "oauthtoken", required = false) String oauthtoken, HttpServletRequest request,
			HttpServletResponse response) {
		utils.cleanInvalidSpringCookie(response);
		return openDashboard(model, id, oauthtoken, request, VIEWIFRAME);

	}

	private String openDashboard(Model model, String id, String userToken, HttpServletRequest request, String from) {
		// validate the origin
		if (from.equals(EDITFULL) || from.equals(VIEW) || from.equals(EDITFULLIFRAME) || from.equals(VIEWIFRAME)) {
			try {
				// initialization of the variables that grant the permission
				OAuth2Authentication info = null;
				boolean hasPermission = false;
				String userId;
				// if we access with oauth2
				if (userToken != null && userToken.length() > 0) {
					info = (OAuth2Authentication) jwtService.getAuthentication(userToken);
					userId = info.getUserAuthentication().getName();
				} else {
					// if we access with session
					userId = utils.getUserId();
				}
				// We check the editing and viewing permissions
				hasPermission = (from.equals(EDITFULLIFRAME) || from.equals(EDITFULL))
						&& dashboardService.hasUserEditPermission(id, userId)
						|| (from.equals(VIEW) || from.equals(VIEWIFRAME))
								&& dashboardService.hasUserViewPermission(id, userId);
				if (hasPermission) {
					final Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
					model.addAttribute(DASHBOARD_STR, dashboard);
					model.addAttribute(HEADERLIBS, dashboard.getHeaderlibs());
					model.addAttribute(I18NJSON, dashboardService.getAllInternationalizationJSON(dashboard).toString());
					model.addAttribute(HEARTBEATTIMEOUT, dashboardService.getClientMaxHeartbeatTime());
					model.addAttribute(PROTOCOL, dashboardService.getProtocol());
					model.addAttribute(SYNOPT,
							dashboard.getType() != null && dashboard.getType().equals(DashboardType.SYNOPTIC));
					final String url = IOTRBROKERSERVER.concat("/iot-broker/rest");
					model.addAttribute(IOTBROKERURL, url);
					// we assign the variables to the model based on the origin
					if (request != null) {
						HttpSession session = request.getSession();
						final Object projectId = session.getAttribute(APP_ID);
						if (projectId != null) {
							model.addAttribute(APP_ID, projectId.toString());
						}
					}
					if (from.equals(EDITFULL)) {
						model.addAttribute(EDITION, true);
						model.addAttribute(IFRAME, false);
						// add to the model if a user is using the dashboard
						model.addAttribute(RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
						resourcesInUseService.put(id, utils.getUserId());
						log.info("in use dashboard resource ", id);
					} else if (from.equals(VIEW)) {
						model.addAttribute(EDITION, false);
						model.addAttribute(IFRAME, false);
						request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
					} else if (from.equals(EDITFULLIFRAME)) {
						model.addAttribute(EDITION, true);
						model.addAttribute(IFRAME, true);
					} else if (from.equals(VIEWIFRAME)) {
						model.addAttribute(EDITION, false);
						model.addAttribute(IFRAME, true);
						request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
					}
					return REDIRECT_DASHBOARDS_VIEW;
				} else {
					if (from.equals(VIEW)) {
						request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
						return REDIRECT_LOGIN;
					} else if (from.equals(VIEWIFRAME)) {
						request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
						return REDIRECT_ERROR_403;
					} else {
						return REDIRECT_ERROR_403;
					}
				}
			} catch (final Exception e) {
				log.error("openDashboard", e);
				if (from.equals(VIEW) || from.equals(VIEWIFRAME)) {
					request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
				}
				return REDIRECT_ERROR_403;
			}
		}
		return REDIRECT_ERROR_403;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/save/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateDashboard(@PathVariable("id") String id,
			@RequestParam("data") Dashboard dashboard) {
		dashboardService.saveDashboard(id, dashboard, utils.getUserId());
		dashboardService.generateDashboardImage(id, utils.getCurrentUserOauthToken());
		return "ok";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/savemodel/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateDashboardModel(@PathVariable("id") String id, @RequestBody EditorDTO model) {
		dashboardService.saveDashboardModel(id, model.getModel(), utils.getUserId());
		dashboardService.generateDashboardImage(id, utils.getCurrentUserOauthToken());
		return "{\"ok\":true}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/saveheaderlibs/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody String updateDashboardHeaderLibs(@PathVariable("id") String id,
			@RequestBody String headerlibs) {
		dashboardService.saveDashboardHeaderLibs(id, headerlibs, utils.getUserId());
		dashboardService.generateDashboardImage(id, utils.getCurrentUserOauthToken());
		return "{\"ok\":true}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String deleteDashboard(@PathVariable("id") String id) {

		try {
			dashboardService.deleteDashboard(id, utils.getUserId());
		} catch (final RuntimeException e) {
			return "{\"ok\":false, \"error\":\"" + e.getMessage() + "\"}";
		}
		return "{\"ok\":true}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		try {
			i18nRepository.deleteAll(i18nRepository.findByOPResourceId(id));
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

	@GetMapping(value = "/generateDashboardImage/{identification}")
	@ResponseBody
	public ResponseEntity<byte[]> generateDashboardImage(
			@Parameter(description = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@Parameter(description = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@Parameter(description = "Render Height", required = true) @RequestParam("height") int height,
			@Parameter(description = "Render Width", required = true) @RequestParam("width") int width,
			@Parameter(description = "Fullpage", required = false) @RequestParam(value = "fullpage", defaultValue = "false") Boolean fullpage,
			@Parameter(description = "Authorization", required = true) @RequestParam("token") String bearerToken,
			@Parameter(description = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {

		ResponseEntity<byte[]> responseEntity;

		responseEntity = dashboardService.generateImgFromDashboardId(id, waittime, height, width,
				fullpage == null ? false : fullpage, params, bearerToken);
		final int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode != 200) {
			log.error(ERROR_DASHBOARD_IMAGE + ' ' + statusCode);
			throw new DashboardServiceException(ERROR_DASHBOARD_IMAGE + ' ' + statusCode);
		}

		final HttpHeaders headers = new HttpHeaders();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
		final String date = simpleDateFormat.format(new Date());
		headers.set("Content-Type", "image/png;charset=utf-8");
		headers.set("Content-Disposition", "attachment; filename=\"" + id + "_" + date + ".png\"");
		return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);

	}

	@GetMapping(value = "/generatePDFImage/{identification}")
	@ResponseBody
	public ResponseEntity<byte[]> generateDashboardPDF(
			@Parameter(description = "Dashboard ID", required = true) @PathVariable("identification") String id,
			@Parameter(description = "Wait time (ms) for rendering dashboard", required = true) @RequestParam("waittime") int waittime,
			@Parameter(description = "Render Height", required = true) @RequestParam("height") int height,
			@Parameter(description = "Render Width", required = true) @RequestParam("width") int width,
			@Parameter(description = "Authorization", required = true) @RequestParam("token") String bearerToken,
			@Parameter(description = "Dashboard Params", required = false) @RequestParam(value = "params", required = false) String params) {

		ResponseEntity<byte[]> responseEntity;

		responseEntity = dashboardService.generatePDFFromDashboardId(id, waittime, height, width, params, bearerToken);
		final int statusCode = responseEntity.getStatusCodeValue();
		if (statusCode != 200) {
			log.error(ERROR_DASHBOARD_PDF + ' ' + statusCode);
			throw new DashboardServiceException(ERROR_DASHBOARD_PDF + ' ' + statusCode);
		}

		final HttpHeaders headers = new HttpHeaders();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
		final String date = simpleDateFormat.format(new Date());
		headers.set("Content-Disposition", "attachment; filename=\"" + id + "_" + date + ".pdf\"");
		headers.setContentType(MediaType.APPLICATION_PDF);
		return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);
	}

	private ArrayList<UserDTO> getUserListDTO() {
		final List<User> users = userService.getAllActiveUsers();
		final ArrayList<UserDTO> userList = new ArrayList<>();
		if (users != null && !users.isEmpty()) {
			for (User user : users) {
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
	public String importDashboard(@RequestBody DashboardExportDTO dashboardimportDTO,
			@RequestParam(required = false, defaultValue = "true") boolean overwrite,
			@RequestParam(required = false, defaultValue = "true") boolean importAuthorizations) {
		try {
			
			final JSONObject response = new JSONObject();
			final DashboardImportResponsetDTO dashboardImport = dashboardService.importDashboard(dashboardimportDTO, utils.getUserId(), overwrite, importAuthorizations);
			final String identification = dashboardService.importDashboard(dashboardimportDTO, utils.getUserId(), overwrite, importAuthorizations).getIdentification();
			
			if(dashboardImport.getErrorOntologies().size() == 0) {
				response.put("status", HttpStatus.OK);
				response.put("message", identification);
			} else {
				List<String> messageontologies = new ArrayList<>();	
				List<String> messagedatasources = new ArrayList<>();	
				
				List<HashMap<String, String>> list = dashboardImport.getErrorOntologies();
				for(HashMap<String, String> hashmap : list) {
					for(String clave : hashmap.keySet()) {
						String valor = hashmap.get(clave);
						messagedatasources.add(clave);
						messageontologies.add(valor);
					}
				}
				response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
				response.put("datasources", messagedatasources);
				response.put("ontologyDatasource", messageontologies);
			}
					
			
			return response.toString();

		} catch (final DashboardServiceException e) {
			log.error("Cannot import dashboard: ", e);
			final JSONObject response = new JSONObject();
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			response.put("message", e.getMessage());
			return response.toString();

		} catch (final Exception e) {
			log.error("Cannot import dashboard: ", e);
			final JSONObject response = new JSONObject();
			response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
			response.put("message", e.getMessage());
			return response.toString();
		}
	}

	@RequestMapping(value = "/exportDashboard/{id}", method = RequestMethod.GET, produces = "text/html")
	@ResponseBody
	public ResponseEntity<byte[]> exportDashboard(@PathVariable("id") String id, Model uiModel) {
		String dashboardJSONObject = null;
		DashboardExportDTO dashboardExportDTO;
		final ObjectMapper mapper = new ObjectMapper();
		try {
			dashboardExportDTO = dashboardService.exportDashboardDTO(id, utils.getUserId());
			try {
				dashboardJSONObject = mapper.writeValueAsString(dashboardExportDTO);
			} catch (final JsonProcessingException e) {
				e.printStackTrace();
			}

		} catch (final JSONException e) {
			log.error("Exception parsing answer in download dashboard");
			throw new DashboardServiceException("Exception parsing answer in download dashboard: " + e);
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.set("Content-Disposition",
				"attachment; filename=\"" + dashboardExportDTO.getIdentification() + ".json\"");

		return new ResponseEntity<>(dashboardJSONObject.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);

	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

}

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
package com.minsait.onesait.platform.controlpanel.controller.gadget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetFavorite;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.GadgetServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetMeasureDTO;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.gadgetfavorite.GadgetFavoriteService;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/gadgets")
@Controller
@Slf4j
public class GadgetController {

	private static final String REDIRECT_ERROR = "error/403";
	private static final String MEASURES = "measures";

	@Autowired
	private GadgetService gadgetService;

	@Autowired
	private UserService userService;

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private GadgetTemplateService gadgetTemplateService;

	@Autowired
	private GadgetFavoriteService gadgetFavoriteService;

	@Autowired
	private CategoryRelationService categoryRelationService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subcategoryService;

	@Autowired
	private GadgetRepository gadgetRepository;

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private HttpSession httpSession;
	
	@Autowired
	private OPResourceService resourceService;

	private static final String IFRAME_STR = "iframe";
	private static final String GADGET_STR = "gadget";
	private static final String DATASOURCES_STR = "datasources";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String GADGETS_CREATE = "gadgets/create";
	private static final String GADGETS_SHOW = "gadgets/view";
	private static final String GADGETS_VIEW = "/controlpanel/gadgets/view/";
	private static final String REDIRECT_GADGETS_CREATE = "redirect:/gadgets/create";
	private static final String REDIRECT_GADGETS_LIST = "redirect:/gadgets/list";
	private static final String ERROR_TRUE_STR = "{\"error\":\"true\"}";
	private static final String GADGET_TEMPLATE = "gadgetTemplate";
	private static final String GADGET_INSTANCE_UPDATE = "gadgetinstances/update";
	private static final String GADGET_INSTANCE_CREATE = "gadgetinstances/create";
	private static final String ID = "gadgetId";
	private static final String VID = "gidview";
	private static final String HEADERLIBS = "headerlibs";
	private static final String ELEMENT = "element";
	private static final String CATEGORIES = "categories";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";
	private static final String GADGETTYPE = "gadgetType";
	private static final String APP_USER_ACCESS = "app_user_access";
	private static final String OWNER_USER = "owner";


	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request) {

		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		uiModel.addAttribute("user", utils.getUserId());
		uiModel.addAttribute("userRole", utils.getRole());

		uiModel.addAttribute("gadgetTypes", gadgetTemplateService.getUserGadgetTemplate(utils.getUserId()));

		String identification = request.getParameter("name");
		String type = request.getParameter("type");
		String currentTab = request.getParameter("current_tab");

		String gadgetType = (String) httpSession.getAttribute(GADGETTYPE);

		if (gadgetType!=null) {

		currentTab="tab-templates";

		}
		httpSession.removeAttribute(GADGETTYPE);

		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (type != null && type.equals("")) {
			type = null;
		}

		final List<Gadget> gadget = gadgetService.findGadgetWithIdentificationAndType(identification, type,
				utils.getUserId());
		// gadgets: tiene que coincidir con el del list
		uiModel.addAttribute("gadgets", gadget);

		// Gadget Templates
		String description = request.getParameter("description");
		if (description != null && description.equals("")) {
			description = null;
		}

		List<GadgetTemplate> gadgetTemplate = this.gadgetTemplateService
				.findGadgetTemplateWithIdentificationAndDescription(identification, description, utils.getUserId());
		List<GadgetTemplate> gadgetTemplateList = gadgetTemplate.stream().filter(temp -> !temp.getType().equals("base"))
				.collect(Collectors.toList());
		uiModel.addAttribute("gadgetTemplates", gadgetTemplateList);
		uiModel.addAttribute("currentTab", currentTab);
		return "gadgets/list";

	}

	@RequestMapping(method = RequestMethod.POST, value = "getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return gadgetService.getAllIdentifications();
	}

	@GetMapping(value = "getUserGadgetsByType/{type}")
	public @ResponseBody List<Gadget> getUserGadgetsByType(@PathVariable("type") String type) {
		return gadgetService.getUserGadgetsByType(utils.getUserId(), type);
	}

	@GetMapping(value = "getUserTemplates")
	public @ResponseBody List<String> getUserTemplates() {
		return gadgetService.getGadgetTypes(utils.getUserId());
	}

	@GetMapping(value = "getUserGadgetsAndTemplates")
	public @ResponseBody List<GadgetAndTemplateDTO> getUserGadgetsAndTemplates() {

		List<GadgetAndTemplateDTO> resultList = new ArrayList<GadgetAndTemplateDTO>();
		final List<Gadget> gadget = gadgetService.findGadgetWithIdentificationAndType(null, null, utils.getUserId());

		List<GadgetTemplate> gadgetTemplate = this.gadgetTemplateService
				.findGadgetTemplateWithIdentificationAndDescription(null, null, utils.getUserId());

		List<GadgetFavorite> gadgetFavorites = gadgetFavoriteService.findAll(utils.getUserId());

		for (Iterator iterator = gadget.iterator(); iterator.hasNext();) {
			Gadget gadg = (Gadget) iterator.next();
			GadgetAndTemplateDTO elem = new GadgetAndTemplateDTO();
			elem.setId(gadg.getId());
			elem.setIdentification(gadg.getIdentification());
			elem.setDescription(gadg.getDescription());
			elem.setIsTemplate(false);
			elem.setType(gadg.getType().getIdentification());
			elem.setTypeDescription(gadg.getType().getDescription());
			if (gadg.getType().getType().equals("base")) {
				elem.setTypeElem("predefined");
			} else {
				elem.setTypeElem("custom");
			}
			resultList.add(elem);
		}

		for (Iterator iterator = gadgetTemplate.iterator(); iterator.hasNext();) {
			GadgetTemplate gadgetTemp = (GadgetTemplate) iterator.next();
			GadgetAndTemplateDTO elem = new GadgetAndTemplateDTO();
			elem.setId(gadgetTemp.getId());
			elem.setIdentification(gadgetTemp.getIdentification());
			elem.setDescription(gadgetTemp.getDescription());
			elem.setIsTemplate(true);
			elem.setType(gadgetTemp.getType());
			elem.setConfig(gadgetTemp.getConfig());
			elem.setImage(gadgetTemp.getImage());
			resultList.add(elem);
		}

		for (Iterator iterator = gadgetFavorites.iterator(); iterator.hasNext();) {
			GadgetFavorite gadgetTemp = (GadgetFavorite) iterator.next();
			GadgetAndTemplateDTO elem = new GadgetAndTemplateDTO();
			elem.setId(gadgetTemp.getId());
			elem.setIdentification(gadgetTemp.getIdentification());
			elem.setIsTemplate(false);
			elem.setType(gadgetTemp.getType());
			elem.setTypeElem("favorite");
			resultList.add(elem);
		}

		resultList.sort((h1, h2) -> h1.getIdentification().compareToIgnoreCase(h2.getIdentification()));

		return resultList;
	}

	@GetMapping(value = "getGadgetConfigById/{gadgetId}")
	public @ResponseBody Gadget getGadgetConfigById(@PathVariable("gadgetId") String gadgetId) {
		return gadgetService.getGadgetById(utils.getUserId(), gadgetId);
	}

	@GetMapping(value = "getGadgetMeasuresByGadgetId/{gadgetId}")
	public @ResponseBody List<GadgetMeasure> getGadgetMeasuresByGadgetId(@PathVariable("gadgetId") String gadgetId) {
		return gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createGadget(Model model) {
		model.addAttribute(IFRAME_STR, Boolean.FALSE);
		model.addAttribute(GADGET_STR, new Gadget());
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		return GADGETS_CREATE;

	}

	@GetMapping(value = "/createiframe/{type}", produces = "text/html")
	public String createGadgetWithOrigin(Model model, @PathVariable("type") String type) {
		model.addAttribute("type", type);
		model.addAttribute(IFRAME_STR, Boolean.TRUE);
		model.addAttribute(GADGET_STR, new Gadget());
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		return GADGETS_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/create", produces = "text/html")
	public String saveGadget(@Valid GadgetDTO gadget, BindingResult bindingResult, String jsonMeasures,
			String datasourcesMeasures, Model uiModel, HttpServletRequest httpServletRequest,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some gadget properties missing");
			utils.addRedirectMessage("gadgets.validation.error", redirect);
			return REDIRECT_GADGETS_CREATE;
		}
		try {
			User user = userService.getUser(utils.getUserId());
			gadgetService.createGadget(gadget, datasourcesMeasures, jsonMeasures, user);
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot create gadget datasource");
			utils.addRedirectMessage("gadgetDatasource.create.error", redirect);
			return REDIRECT_GADGETS_CREATE;
		}

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.GADGET.toString());
			httpSession.setAttribute("resourceIdentificationAdded", gadget.getIdentification());
			httpSession.removeAttribute(APP_ID);
			return REDIRECT_PROJECT_SHOW + projectId.toString();
		}

		return REDIRECT_GADGETS_LIST;

	}

	@PostMapping(value = { "/createiframe" }, produces = "application/json")
	public @ResponseBody String saveGadgetIframe(GadgetDTO gadget, String jsonMeasures, String datasourcesMeasures) {
		Gadget g;
		try {
			User user = userService.getUser(utils.getUserId());
			g = gadgetService.createGadget(gadget, datasourcesMeasures, jsonMeasures, user);
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot create gadget");

			return ERROR_TRUE_STR;
		}
		return "{\"id\":\"" + g.getId() + "\",\"identification\":\"" + g.getIdentification() + "\",\"type\":\""
				+ g.getType().getId() + "\"}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{gadgetId}", produces = "text/html")
	public String createGadget(Model model, @PathVariable("gadgetId") String gadgetId) {
		if (!gadgetService.hasUserPermission(gadgetId, utils.getUserId())) {
			return REDIRECT_ERROR;
		}

		Gadget gad = gadgetService.getGadgetById(utils.getUserId(), gadgetId);
		ResourceAccessType resourceAccess = resourceService.getResourceAccess(utils.getUserId(),gadgetId);
		model.addAttribute(APP_USER_ACCESS, resourceAccess);		
		model.addAttribute(OWNER_USER, gad.getUser().getUserId());
		if (gad.isInstance()) {
			model.addAttribute(GADGET_TEMPLATE, gad.getType());
			model.addAttribute(GADGET_STR, mapGadgetToGadgetDTO(gad));
			model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
//			return GADGET_INSTANCE_UPDATE;
			return GADGET_INSTANCE_CREATE;
		} else {
			model.addAttribute(GADGET_STR, mapGadgetToGadgetDTO(gad));
			model.addAttribute(MEASURES, mapGadgetMeasureListToGadgetMeasureDTOList(
					gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId)));
			model.addAttribute(DATASOURCES_STR,
					gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
			model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
			model.addAttribute(IFRAME_STR, Boolean.FALSE);
			model.addAttribute("dataSourceAccessType",
					gadgetDatasourceService.getAccessType(gadgetService
							.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId).get(0).getDatasource().getId(),
							utils.getUserId()));
			model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
					resourcesInUseService.isInUse(gadgetId, utils.getUserId()));
			resourcesInUseService.put(gadgetId, utils.getUserId());
			return GADGETS_CREATE;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/view/{gadgetId}", produces = "text/html")
	public String showGadget(Model model, @PathVariable("gadgetId") String gadgetId) {
		if (!gadgetService.hasUserViewPermission(gadgetId, utils.getUserId()))
			return REDIRECT_ERROR;
		JSONObject elem;
		Gadget gad = gadgetService.getGadgetById(utils.getUserId(), gadgetId);
		model.addAttribute(ID, VID);
		if (gad.isInstance()) {
			model.addAttribute(HEADERLIBS, gad.getType().getHeaderlibs());
			elem = createElementCustomGadgetJSON(gadgetId, gad);

		} else {
			model.addAttribute(HEADERLIBS, "");
			elem = createElementGadgetJSON(gadgetId, gad);
		}
		model.addAttribute(ELEMENT, elem);
		return GADGETS_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/clone" })
	public ResponseEntity<String> cloneGadget(Model model, @RequestParam String gadgetId,
			@RequestParam String identification) {

		try {
			String id = "";
			final String userId = utils.getUserId();

			id = gadgetService.cloneGadget(gadgetService.getGadgetById(userId, gadgetId), identification,
					userService.getUser(userId));
			final Optional<Gadget> opt = gadgetRepository.findById(id);
			if (!opt.isPresent())
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			final Gadget gadget = opt.get();
			return new ResponseEntity<>(gadget.getId(), HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/gadgettemplates/clone" })
	public ResponseEntity<String> cloneGadgetTemplate(Model model, @RequestParam String gadgetId,
			@RequestParam String identification) {
		try {
			httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
			String id = "";
			final String userId = utils.getUserId();

			id = gadgetTemplateService.cloneGadgetTemplate(gadgetTemplateService.getGadgetTemplateById(gadgetId),
					identification, userService.getUser(userId));
			final Optional<GadgetTemplate> opt = gadgetTemplateRepository.findById(id);
			if (!opt.isPresent())
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			final GadgetTemplate gadget = opt.get();
			return new ResponseEntity<>(gadget.getId(), HttpStatus.OK);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

	private JSONObject createElementGadgetJSON(String gadgetId, Gadget gad) {
		JSONObject header = new JSONObject();
		header.put("enable", false);
		JSONObject border = new JSONObject();
		border.put("color", "hsl(0, 0%, 90%)");
		border.put("width", 0);
		border.put("radius", 5);
		JSONObject elem = new JSONObject();
		elem.put("id", gadgetId);
		elem.put("type", gad.getType().getId());
		elem.put("x", 0);
		elem.put("y", 0);
		elem.put("cols", 10);
		elem.put("rows", 10);
		elem.put("backgroundColor", "white");
		elem.put("header", header);
		elem.put("border", border);
		elem.put("hideBadges", true);
		elem.put("notshowDotsMenu", true);
		return elem;
	}

	private JSONObject createElementCustomGadgetJSON(String gadgetId, Gadget gad) {
		JSONObject header = new JSONObject();
		header.put("enable", false);
		JSONObject border = new JSONObject();
		border.put("color", "hsl(0, 0%, 90%)");
		border.put("width", 0);
		border.put("radius", 5);
		JSONObject elem = new JSONObject();
		elem.put("id", VID);
		elem.put("gadgetid", gadgetId);
		elem.put("type", "livehtml");
		elem.put("subtype", gad.getType().getType());
		elem.put("tempId", gad.getType().getId());
		elem.put("template", gad.getType().getIdentification());
		elem.put("x", 0);
		elem.put("y", 0);
		elem.put("cols", 10);
		elem.put("rows", 10);
		elem.put("backgroundColor", "white");
		elem.put("header", header);
		elem.put("border", border);
		elem.put("hideBadges", true);
		elem.put("notshowDotsMenu", true);
		return elem;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/show/{gadgetId}", produces = "text/html")
	public String showFromMenu(Model model, @PathVariable("gadgetId") String gadgetId) {
		model.addAttribute("url", GADGETS_VIEW + gadgetId);
		// return GADGET_INSTANCE_SHOW;
		return "gadgets/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/createcustomgadget/{id}", produces = "text/html")
	public String createcustomgadget(Model model, @PathVariable("id") String id) {
		model.addAttribute(GADGET_TEMPLATE, this.gadgetTemplateService.getGadgetTemplateById(id));
		model.addAttribute(GADGET_STR, new GadgetDTO());
		model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
		return GADGET_INSTANCE_CREATE;
	}

	@GetMapping(value = "/hasUserPermission/{gadgetId}", produces = "application/json")
	@ResponseBody
	public Boolean hasUserPermission(Model model, @PathVariable("gadgetId") String gadgetId) {
		return gadgetService.hasUserPermission(gadgetId, utils.getUserId());
	}

	@GetMapping(value = "/updateiframe/{gadgetId}", produces = "text/html")
	public String updateGadgetIframe(Model model, @PathVariable("gadgetId") String gadgetId) {
		model.addAttribute(GADGET_STR, mapGadgetToGadgetDTO(gadgetService.getGadgetById(utils.getUserId(), gadgetId)));
		model.addAttribute(MEASURES, mapGadgetMeasureListToGadgetMeasureDTOList(
				gadgetService.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId)));
		model.addAttribute(DATASOURCES_STR, gadgetDatasourceService.getUserGadgetDatasourcesForList(utils.getUserId()));
		model.addAttribute(ONTOLOGIES_STR, getOntologiesDTO());
		model.addAttribute(IFRAME_STR, Boolean.TRUE);
		model.addAttribute("dataSourceAccessType",
				gadgetDatasourceService.getAccessType(gadgetService
						.getGadgetMeasuresByGadgetId(utils.getUserId(), gadgetId).get(0).getDatasource().getId(),
						utils.getUserId()));
		model.addAttribute(ResourcesInUseService.RESOURCEINUSEDASHBOARD,
				resourcesInUseService.isInUse(gadgetId, utils.getUserId()));
		resourcesInUseService.put(gadgetId, utils.getUserId());

		return GADGETS_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/updateiframe/{gadgetId}" }, produces = "application/json")
	public @ResponseBody String saveUpadteGadgetIframe(@PathVariable("gadgetId") String gadgetId, GadgetDTO gadget,
			String jsonMeasures, String datasourcesMeasures) {
		if (!gadgetService.hasUserPermission(gadgetId, utils.getUserId())) {
			return REDIRECT_ERROR;
		}

		try {
			User user = userService.getUser(utils.getUserId());
			gadgetService.updateGadget(gadget, datasourcesMeasures, jsonMeasures, user);
			resourcesInUseService.removeByUser(gadgetId, utils.getUserId());
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot updateiframe gadget");

			return ERROR_TRUE_STR;
		}
		return "{\"id\":\"" + gadgetId + "\"}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {

		try {
			gadgetService.deleteGadget(id, utils.getUserId());
		} catch (final RuntimeException | JsonProcessingException e) {
			utils.addRedirectException(e, ra);
		}
		return REDIRECT_GADGETS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping("/gadgettemplates/{id}")
	public String delete(Model model, @PathVariable("id") String id) {
		log.info("Controlador");
		this.gadgetTemplateService.deleteGadgetTemplate(id, utils.getUserId());
		httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
		return REDIRECT_GADGETS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateGadget(Model model, @PathVariable("id") String id, @Valid GadgetDTO gadget, String jsonMeasures,
			String datasourcesMeasures, BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some Gadget properties missing");
			utils.addRedirectMessage("gadgets.validation.error", redirect);
			return "redirect:/gadgets/update/" + id;
		}
		if (!gadgetService.hasUserPermission(id, utils.getUserId()))
			return REDIRECT_ERROR;
		try {
			gadgetService.updateGadget(gadget, datasourcesMeasures, jsonMeasures,
					userService.getUser(utils.getUserId()));
			resourcesInUseService.removeByUser(id, utils.getUserId());
		} catch (final GadgetServiceException e) {
			log.debug("Cannot update gadget datasource");
			utils.addRedirectMessage("gadgetDatasource.update.error", redirect);
			return REDIRECT_GADGETS_CREATE;
		}
		return REDIRECT_GADGETS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/updateconfig/{id}", produces = "text/html")
	public @ResponseBody String updateCustomGadgetConfig(@PathVariable("id") String id,
			@RequestBody String configGadget) {

		if (configGadget == null) {
			return ERROR_TRUE_STR;
		}

		if (!gadgetService.hasUserPermission(id, utils.getUserId()))

			return ERROR_TRUE_STR;
		try {

			gadgetService.updateInstance(id, configGadget);
			resourcesInUseService.removeByUser(id, utils.getUserId());
		} catch (final GadgetServiceException e) {
			log.debug("Cannot update Instance");

			return ERROR_TRUE_STR;
		}
		Gadget g = gadgetService.getGadgetById(utils.getUserId(), id);
		return "{\"id\":\"" + id + "\",\"identification\":\"" + g.getIdentification() + "\",\"type\":\""
				+ g.getType().getId() + "\"}";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/existGadget" }, produces = "application/json")
	public @ResponseBody String existGadget(String identification) {

		try {
			return "{\"exist\":\"" + gadgetService.existGadgetWithIdentification(identification) + "\"}";
		} catch (final GadgetDatasourceServiceException e) {
			log.debug("Cannot create gadget");
			return ERROR_TRUE_STR;
		}
	}

	private List<OntologyDTO> getOntologiesDTO() {
		final Set<com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO> ontologies = new LinkedHashSet<>(
				ontologyService.getAllOntologiesForListWithProjectsAccess(utils.getUserId()));
		return ontologies
				.stream().map(o -> OntologyDTO.builder().identification(o.getIdentification())
						.description(o.getDescription()).user(o.getUser().getUserId()).build())
				.collect(Collectors.toList());

		/**
		 * final List<OntologyDTO> listOntologies = new ArrayList<OntologyDTO>(); final
		 * List<Ontology> ontologies =
		 * ontologyService.getOntologiesByUserId(utils.getUserId()); if (ontologies !=
		 * null && ontologies.size() > 0) { for (final Iterator<Ontology> iterator =
		 * ontologies.iterator(); iterator.hasNext();) { final Ontology ontology =
		 * iterator.next(); final OntologyDTO oDTO = new OntologyDTO();
		 * oDTO.setIdentification(ontology.getIdentification());
		 * oDTO.setDescription(ontology.getDescription());
		 * oDTO.setUser(ontology.getUser().getUserId()); listOntologies.add(oDTO); } }
		 * return listOntologies;
		 **/
	}

	private GadgetDTO mapGadgetToGadgetDTO(Gadget gadget) {
		final GadgetDTO gDTO = new GadgetDTO();
		gDTO.setConfig(gadget.getConfig());
		gDTO.setDescription(gadget.getDescription());
		gDTO.setIdentification(gadget.getIdentification());
		gDTO.setPublic(gadget.isPublic());
		gDTO.setInstance(gadget.isInstance());
		gDTO.setType(gadget.getType().getId());
		gDTO.setId(gadget.getId());

		final CategoryRelation cr = categoryRelationService.getByIdType(gadget.getId());
		if (cr != null) {
			final Category c = categoryService.getCategoryById(cr.getCategory());
			if (c != null)
				gDTO.setCategory(c.getIdentification());
			final Subcategory s = subcategoryService.getSubcategoryById(cr.getSubcategory());
			if (s != null)
				gDTO.setSubcategory(s.getIdentification());
		}
		return gDTO;
	}

	private List<GadgetDatasourceDTO> mapGadgetDataSourceListToGadgetDataSourceDTOList(
			List<GadgetDatasource> listGadgetDataSource) {
		final List<GadgetDatasourceDTO> listGDS = new ArrayList<>();
		if (listGadgetDataSource != null && !listGadgetDataSource.isEmpty()) {
			for (final Iterator<GadgetDatasource> iterator = listGadgetDataSource.iterator(); iterator.hasNext();) {
				final GadgetDatasource gadgetDataSource = iterator.next();
				listGDS.add(mapGadgetDataSourceToGadgetDataSourceDTO(gadgetDataSource));
			}
		}
		return listGDS;

	}

	private GadgetDatasourceDTO mapGadgetDataSourceToGadgetDataSourceDTO(GadgetDatasource gadgetDataSource) {
		final GadgetDatasourceDTO gDTO = new GadgetDatasourceDTO();
		gDTO.setConfig(gadgetDataSource.getConfig());
		gDTO.setDescription(gadgetDataSource.getDescription());
		gDTO.setIdentification(gadgetDataSource.getIdentification());
		gDTO.setId(gadgetDataSource.getId());
		gDTO.setDbtype(gadgetDataSource.getDbtype());
		gDTO.setMaxvalues(gadgetDataSource.getMaxvalues());
		gDTO.setMode(gadgetDataSource.getMode());
		final OntologyDTO oDTO = new OntologyDTO();
		if (gadgetDataSource.getOntology() != null) {
			oDTO.setDescription(gadgetDataSource.getOntology().getDescription());
			oDTO.setIdentification(gadgetDataSource.getOntology().getIdentification());
			oDTO.setUser(gadgetDataSource.getOntology().getUser().getUserId());
			gDTO.setOntology(oDTO);
		}
		gDTO.setQuery(gadgetDataSource.getQuery());
		gDTO.setRefresh(gadgetDataSource.getRefresh());
		return gDTO;

	}

	private List<GadgetMeasureDTO> mapGadgetMeasureListToGadgetMeasureDTOList(List<GadgetMeasure> listGadgetMeasure) {
		final List<GadgetMeasureDTO> listGM = new ArrayList<>();
		if (listGadgetMeasure != null && !listGadgetMeasure.isEmpty()) {
			for (final Iterator<GadgetMeasure> iterator = listGadgetMeasure.iterator(); iterator.hasNext();) {
				final GadgetMeasure gadgetM = iterator.next();
				listGM.add(mapGadgetMeasuretoGadgetMeasureDTO(gadgetM));
			}
		}
		return listGM;

	}

	private GadgetMeasureDTO mapGadgetMeasuretoGadgetMeasureDTO(GadgetMeasure gadgetMeasure) {
		final GadgetMeasureDTO gDTO = new GadgetMeasureDTO();
		gDTO.setId(gadgetMeasure.getId());
		gDTO.setDatasource(mapGadgetDataSourceToGadgetDataSourceDTO(gadgetMeasure.getDatasource()));
		gDTO.setGadget(mapGadgetToGadgetDTO(gadgetMeasure.getGadget()));
		gDTO.setConfig(gadgetMeasure.getConfig());

		return gDTO;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

}

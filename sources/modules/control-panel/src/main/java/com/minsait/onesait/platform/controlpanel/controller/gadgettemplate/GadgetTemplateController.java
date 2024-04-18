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
package com.minsait.onesait.platform.controlpanel.controller.gadgettemplate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetTemplateServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/gadgettemplates")
@Controller
@Slf4j
public class GadgetTemplateController {

	private static final String GADGET_TEMPLATE = "gadgetTemplate";
	private static final String GADGET_TEMPLATE_TYPES = "gadgetTemplateTypes";
	private static final String MESSAGE = "message";
	private static final String CATEGORIES = "categories";
	private static final String DATASOURCES = "datasources";
	private static final String GADGETTYPE = "gadgetType";

	@Autowired
	private GadgetTemplateService gadgetTemplateService;
	
	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ResourcesInUseService resourcesInUseService;
	
	@Autowired 
	private HttpSession httpSession;
	
	@Autowired
	private OPResourceService resourceService;

	private static final String REDIRECT_GADGET_TEMP_LIST = "redirect:/gadgets/list";
	private static final String REDIRECT_SHOW = "redirect:/gadgettemplates/view/";
	private static final String REDIRECT_EDIT = "redirect:/gadgettemplates/update/";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";
	private static final String APP_USER_ACCESS = "app_user_access";

	@RequestMapping(method = RequestMethod.POST, value = "getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.gadgetTemplateService.getAllIdentifications();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createGadget(Model model) {
		
		model.addAttribute(GADGET_TEMPLATE, new GadgetTemplateDTO());
		model.addAttribute(GADGET_TEMPLATE_TYPES, this.gadgetTemplateService.getTemplateTypes());
		model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
		model.addAttribute(DATASOURCES, gadgetDatasourceService.getAllIdentificationsByUser(utils.getUserId()));
		
		httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
		
		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId!=null) {
			model.addAttribute(APP_ID, projectId.toString());
		}
		
		return "gadgettemplates/create";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/create", produces = "text/html")
	public String saveGadget(@Valid GadgetTemplateDTO gadgetTemplate, BindingResult bindingResult, Model model,
			HttpServletRequest httpServletRequest, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some gadgetTemplate properties missing");
			gadgetTemplate.setId(null);
			model.addAttribute(GADGET_TEMPLATE, gadgetTemplate);
			model.addAttribute(MESSAGE, utils.getMessage("gadgets.validation.error", ""));
			model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
			return "gadgettemplates/create";
		}
		GadgetTemplate gt = this.gadgetTemplateService
				.getGadgetTemplateByIdentification(gadgetTemplate.getIdentification());
		if (gt != null) {
			gadgetTemplate.setId(null);
			model.addAttribute(GADGET_TEMPLATE, gadgetTemplate);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error.identifier", ""));
			model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
			return "gadgettemplates/create";
		}
		gadgetTemplate.setUser(this.userService.getUser(this.utils.getUserId()));
		try {
			this.gadgetTemplateService.createGadgetTemplate(gadgetTemplate);
		} catch (GadgetTemplateServiceException e) {
			utils.addRedirectMessage("gadgets.validation.error", redirect);
			gadgetTemplate.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("gadgets.validation.error", ""));
			model.addAttribute(GADGET_TEMPLATE, gadgetTemplate);
			model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
			return "gadgettemplates/create";
		}
		
		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId!=null) {
			httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.GADGETTEMPLATE.toString());
			httpSession.setAttribute("resourceIdentificationAdded", gadgetTemplate.getIdentification());
			httpSession.removeAttribute(APP_ID);
			return REDIRECT_PROJECT_SHOW + projectId.toString();
		}

		return REDIRECT_GADGET_TEMP_LIST;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{gadgetTemplateId}", produces = "text/html")
	public String createGadget(Model model, @PathVariable("gadgetTemplateId") String gadgetTemplateId) {
		
		GadgetTemplateDTO gadgetTemplate = this.gadgetTemplateService.getGadgetTemplateDTOById(gadgetTemplateId);
		
		ResourceAccessType resourceAccess = resourceService.getResourceAccess(utils.getUserId(),gadgetTemplate.getId());
		model.addAttribute(APP_USER_ACCESS, resourceAccess);
		
		httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
		model.addAttribute(GADGET_TEMPLATE, gadgetTemplate);
		model.addAttribute(GADGET_TEMPLATE_TYPES, this.gadgetTemplateService.getTemplateTypes());
		model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
				resourcesInUseService.isInUse(gadgetTemplateId, utils.getUserId()));
		resourcesInUseService.put(gadgetTemplateId, utils.getUserId());
		model.addAttribute(CATEGORIES, categoryService.getCategoriesByTypeAndGeneralType(Category.Type.GADGET));
		model.addAttribute(DATASOURCES, gadgetDatasourceService.getAllIdentificationsByUser(utils.getUserId()));
		return "gadgettemplates/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/view/{gadgetTemplateId}", produces = "text/html")
	public String showGadget(Model model, @PathVariable("gadgetTemplateId") String gadgetTemplateId) {
		httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
		
		GadgetTemplateDTO gadgetTemplate = this.gadgetTemplateService.getGadgetTemplateDTOById(gadgetTemplateId);
		
		ResourceAccessType resourceAccess = resourceService.getResourceAccess(utils.getUserId(),gadgetTemplate.getId());
		model.addAttribute(APP_USER_ACCESS, resourceAccess);
		
		model.addAttribute(GADGET_TEMPLATE, gadgetTemplate);
		return "gadgettemplates/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/gadgetViewer", produces = "text/html")
	public String showGadgetViewer(Model model) {
		return "gadgettemplates/gadgetViewer";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/gadgetViewerVue", produces = "text/html")
	public String showGadgetViewerVue(Model model) {
		return "gadgettemplates/gadgetViewerVue";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/gadgetViewerReact", produces = "text/html")
	public String showGadgetViewerReact(Model model) {
		return "gadgettemplates/gadgetViewerReact";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id) {
		httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
		this.gadgetTemplateService.deleteGadgetTemplate(id, utils.getUserId());
		return REDIRECT_GADGET_TEMP_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateGadget(Model model, @PathVariable("id") String id, @Valid GadgetTemplateDTO gadgetTemplate,
			BindingResult bindingResult, RedirectAttributes redirect) {
		httpSession.setAttribute(GADGETTYPE, "gadgetTemplate");
		if (bindingResult.hasErrors()) {
			log.debug("Some GadgetTemplate properties missing");
			utils.addRedirectMessage("gadgets.validation.error", redirect);
			return "redirect:/gadgettemplates/update/" + id;
		}
		if (!gadgetTemplateService.hasUserPermission(id, this.utils.getUserId()))
			return "error/403";

		this.gadgetTemplateService.updateGadgetTemplate(gadgetTemplate);
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_GADGET_TEMP_LIST;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "getUserGadgetTemplate/{type}")
	public @ResponseBody List<GadgetTemplate> getUserGadgetTemplateByType(@PathVariable("type") String type) {
		return this.gadgetTemplateService.getUserGadgetTemplate(utils.getUserId(),type);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "getUserGadgetTemplate")
	public @ResponseBody List<GadgetTemplate> getUserGadgetTemplate() {
		return this.gadgetTemplateService.getUserGadgetTemplate(utils.getUserId());
	}

	//Method can't be with user because public dashboard with templates
	@GetMapping(value = "getGadgetTemplateByIdentification/{identification}")
	public @ResponseBody GadgetTemplate getGadgetTemplateByIdentification(
			@PathVariable("identification") String identification) {
		return this.gadgetTemplateService.getGadgetTemplateByIdentification(identification);
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@GetMapping(value = "/getTemplateTypes")
	public @ResponseBody List<GadgetTemplateType> getTemplateTypes() {
		return gadgetTemplateService.getTemplateTypes();
	}

	@GetMapping(value = "/getTemplateTypeById/{id}")
	public @ResponseBody GadgetTemplateType getTemplateTypeById(@PathVariable("id") String id) {
		return gadgetTemplateService.getTemplateTypeById(id);
	}
}

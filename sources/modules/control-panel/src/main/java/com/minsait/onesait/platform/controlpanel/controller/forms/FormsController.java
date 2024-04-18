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
package com.minsait.onesait.platform.controlpanel.controller.forms;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.config.model.I18nResources;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.repository.I18nResourcesRepository;
import com.minsait.onesait.platform.config.services.form.FormDTO;
import com.minsait.onesait.platform.config.services.form.FormService;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("forms")
@Slf4j
public class FormsController {

	private static final String I18NS = "i18ns";
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private FormService formService;
	@Autowired
	private InternationalizationService internationalizationService;
	@Autowired
	private I18nResourcesRepository i18nRepository;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;
	@Autowired
	private OPResourceService resourceService;

	private static final String DATASOURCES = "datasources";
	private static final String APP_USER_ACCESS = "app_user_access";

	@GetMapping("form")
	public String formIframe(@RequestParam(value = "id", required = false) String formId, Model model) {
		model.addAttribute("controlpanelApiUrl",
				this.resourcesService.getUrl(Module.CONTROLPANEL, ServiceUrl.BASE) + "/api/forms");
		if (StringUtils.hasText(formId)) {
			model.addAttribute("formId", formId);
		}
		return "forms/form";
	}

	@GetMapping("form-show")
	public String formShowIframe(@RequestParam(value = "id", required = false) String formId,
			@RequestParam(value = "oid", required = false) String oid, Model model) {
		model.addAttribute("controlpanelApiUrl",
				this.resourcesService.getUrl(Module.CONTROLPANEL, ServiceUrl.BASE) + "/api/forms");
		if (StringUtils.hasText(formId)) {
			model.addAttribute("formId", formId);
		}
		if (StringUtils.hasText(oid)) {
			model.addAttribute("oid", oid);
		}
		return "forms/form-show";
	}

	@GetMapping("show/{id}")
	public String show(Model model, @PathVariable("id") String id) {
		if (formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.VIEW)) {
			final FormDTO form = formService.getForm(id);
			model.addAttribute("form", form);
			final String path = "/forms/form-show?id=" + form.getCode();
			model.addAttribute("formpath", path);
			return "forms/show";
		} else {
			log.warn("User {} does not have access to form {}", utils.getUserId(), id);
			return "error/403";
		}
	}

	@GetMapping("show/{id}/{oid}")
	public String showOid(Model model, @PathVariable("id") String id, @PathVariable("oid") String oid) {
		if (formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.VIEW)) {
			final FormDTO form = formService.getForm(id);
			model.addAttribute("form", form);
			final String path = "/forms/form-show?id=" + form.getCode() + "&&oid=" + oid;
			model.addAttribute("formpath", path);
			return "forms/show";
		} else {
			log.warn("User {} does not have access to form {}", utils.getUserId(), id);
			return "error/403";
		}
	}

	@GetMapping("form-show/{id}")
	public String formShowIframeOid(Model model, @PathVariable("id") String id) {
		model.addAttribute("controlpanelApiUrl",
				this.resourcesService.getUrl(Module.CONTROLPANEL, ServiceUrl.BASE) + "/api/forms");
		model.addAttribute("formId", id);
		model.addAttribute("oid", null);
		return "forms/form-show";
	}

	@GetMapping("form-show/{id}/{oid}")
	public String formShowIframeOid(Model model, @PathVariable("id") String id, @PathVariable("oid") String oid) {
		model.addAttribute("controlpanelApiUrl",
				this.resourcesService.getUrl(Module.CONTROLPANEL, ServiceUrl.BASE) + "/api/forms");
		model.addAttribute("formId", id);
		model.addAttribute("oid", oid);
		return "forms/form-show";
	}

	@GetMapping("list")
	public String list(Model model) {
		model.addAttribute("forms", formService.getForms(utils.getUserId()));
		return "forms/list";
	}

	@GetMapping("usuarios")
	public String usuarios() {
		return "forms/usuarios";
	}

	@GetMapping("create")
	public String create(Model model) {
		model.addAttribute("entities", ontologyService.getOntologiesByUserId(utils.getUserId()));
		model.addAttribute(I18NS, internationalizationService.getByUserIdOrPublic(utils.getUserId()));
		model.addAttribute("iframe", "");
		model.addAttribute(DATASOURCES, gadgetDatasourceService.getAllIdentificationsByUser(utils.getUserId()));
		return "forms/create";
	}

	@GetMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id) {
		if (formService.hasUserAccess(utils.getUserId(), id, ResourceAccessType.MANAGE)) {
			final FormDTO form = formService.getForm(id);
			model.addAttribute("entities", ontologyService.getOntologiesByUserId(utils.getUserId()));
			model.addAttribute(I18NS, internationalizationService.getByUserIdOrPublic(utils.getUserId()));
			// find internationalization
			String i18nR = "";
			final List<I18nResources> i18nResources = i18nRepository.findByOPResourceId(form.getId());
			for (final I18nResources ir : i18nResources) {
				if (i18nR.isEmpty()) {
					i18nR = ir.getI18n().getId();
				} else {
					i18nR = i18nR + "," + ir.getI18n().getId();
				}
			}
			form.setI18n(i18nR);
			ResourceAccessType resourceAccess = resourceService.getResourceAccess(utils.getUserId(),form.getId());
			model.addAttribute(APP_USER_ACCESS, resourceAccess);
			model.addAttribute("form", form);
			model.addAttribute("iframe", "?id=" + form.getCode());
			model.addAttribute(DATASOURCES, gadgetDatasourceService.getAllIdentificationsByUser(utils.getUserId()));
			return "forms/create";
		} else {
			log.warn("User {} does not have access to form {}", utils.getUserId(), id);
			return "error/403";
		}
	}

	@PostMapping(value = "/clone")
	public ResponseEntity<String> clone(Model model, @RequestParam String elementid,
			@RequestParam String identification) {

		try {
			if (formService.hasUserAccess(utils.getUserId(), elementid, ResourceAccessType.VIEW)) {
				formService.clone(elementid, identification, utils.getUserId());
				return new ResponseEntity<>(identification, HttpStatus.OK);
			} else {
				log.warn("User {} does not have access to form {}", utils.getUserId(), elementid);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		} catch (final Exception e) {

			return new ResponseEntity<>("{\"status\" : \"fail\"}", HttpStatus.BAD_REQUEST);
		}
	}

}

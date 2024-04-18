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
package com.minsait.onesait.platform.controlpanel.controller.mstemplates;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.MicroserviceTemplate;
import com.minsait.onesait.platform.config.model.MicroserviceTemplate.Language;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MicroserviceTemplateRepository;
import com.minsait.onesait.platform.config.services.exceptions.MicroserviceTemplateException;
import com.minsait.onesait.platform.config.services.mstemplates.MicroserviceTemplatesService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@RequestMapping("/mstemplates")
@Controller
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
public class MsTemplatesController {

	@Autowired
	private MicroserviceTemplatesService msTemplateService;
	@Autowired
	private MicroserviceTemplateRepository msTemplateRepository;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private HttpSession httpSession;
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private OntologyService ontologyService;

	private static final String MSTEMPLATE_STR = "mstemplate";
	private static final String MSTEMPLATE_CREATE = "mstemplate/create";
	private static final String REDIRECT_MSTEMPLATE_CREATE = "redirect:/mstemplates/create";
	private static final String USERS = "users";
	private static final String CREDENTIALS_STR = "credentials";
	private static final String EDITION = "edition";
	private static final String REDIRECT_MSTEMPLATE_VIEW = "mstemplate/show";

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
		final List<MicroserviceTemplate> msTemplates;
		if (utils.isAdministrator()) {
			msTemplates = msTemplateService.findMicroserviceTemplatesWithIdentificationAndDescription(identification,
					description, utils.getUserId());
		} else {
			msTemplates = msTemplateRepository
					.findByUserOrIsPublicTrueOrderByIdentificationAsc(userService.getUser(utils.getUserId()));
		}
		uiModel.addAttribute("msTemplates", msTemplates);
		return "mstemplate/list";

	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute(MSTEMPLATE_STR, new MicroserviceTemplate());
		model.addAttribute("language", Language.values());
		model.addAttribute(USERS, getUserListDTO());
		model.addAttribute("schema", msTemplateRepository.findAll());
		final List<Notebook> notebooks = notebookService.getNotebooks(utils.getUserId());
		model.addAttribute("notebooks", notebooks);
		final List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
		model.addAttribute("ontologies", ontologies);
		return MSTEMPLATE_CREATE;
	}

	@PostMapping(value = { "/create" })
	public String createMicroserviceTemplate(Model model, @Valid MicroserviceTemplate msTemplate,
			BindingResult bindingResult, HttpServletRequest request, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("mstemplates.validadtion.error", redirect);
			return REDIRECT_MSTEMPLATE_CREATE;
		}
		try {
			msTemplateService.createNewTemplate(msTemplate, utils.getUserId());
			return "redirect:/mstemplates/list/";

		} catch (final MicroserviceTemplateException e) {
			utils.addRedirectException(e, redirect);
			return REDIRECT_MSTEMPLATE_CREATE;
		}
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		if (msTemplateService.hasUserEditPermission(id, utils.getUserId())) {
			model.addAttribute(MSTEMPLATE_STR, msTemplateService.getMsTemplateEditById(id, utils.getUserId()));
			model.addAttribute("language", Language.values());
			final List<Notebook> notebooks = notebookService.getNotebooks(utils.getUserId());
			model.addAttribute("notebooks", notebooks);
			final List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
			model.addAttribute("ontologies", ontologies);
			return MSTEMPLATE_CREATE;
		} else {
			utils.addRedirectMessage("Not enough rights", ra);
			return "redirect:/mstemplates/list/";
		}
	}

	@PostMapping(value = { "/update/{id}" })
	public String update(@PathVariable("id") String id, MicroserviceTemplate mstemplate, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("mstemplates.validadtion.error", redirect);
			return REDIRECT_MSTEMPLATE_CREATE;
		}
		try {
			if (msTemplateService.hasUserEditPermission(id, utils.getUserId())) {
				msTemplateService.updateMsTemplate(mstemplate, utils.getUserId());

			} else {
				utils.addRedirectMessage("Not enough rights", redirect);
			}
			return "redirect:/mstemplates/list/";

		} catch (final MicroserviceTemplateException e) {
			utils.addRedirectException(e, redirect);
			return "redirect:/mstemplates/update/" + mstemplate.getId();
		}
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String viewerMsTemplate(Model model, @PathVariable("id") String id, HttpServletRequest request,
			RedirectAttributes redirect) {
		if (msTemplateService.hasUserViewPermission(id, utils.getUserId())) {
			final MicroserviceTemplate mstemplate = msTemplateService.getById(id);
			model.addAttribute(MSTEMPLATE_STR, mstemplate);
			model.addAttribute(CREDENTIALS_STR, msTemplateService.getCredentialsString(utils.getUserId()));
			model.addAttribute(EDITION, false);
			return REDIRECT_MSTEMPLATE_VIEW;
		} else {
//			utils.addRedirectMessage("Not enough rights", redirect);
			return "redirect:/mstemplates/list";
		}
	}

	@DeleteMapping("{id}")
	public String delete(@PathVariable("id") String id, RedirectAttributes ra) {
		final MicroserviceTemplate msTemplate = msTemplateService.getById(id);
		if (!msTemplateService.hasUserPermission(msTemplate, userService.getUser(utils.getUserId()))) {
			utils.addRedirectMessage("Not found", ra);
		}
		if (msTemplate == null) {
			utils.addRedirectMessage("Not found", ra);
		}
		msTemplateService.delete(id);
		return "redirect:/mstemplates/list";
	}

	private ArrayList<UserDTO> getUserListDTO() {
		final List<User> users = userService.getAllActiveUsers();
		final ArrayList<UserDTO> userList = new ArrayList<>();
		if (users != null && !users.isEmpty()) {
			for (final User user : users) {
				final UserDTO uDTO = new UserDTO();
				uDTO.setUserId(user.getUserId());
				uDTO.setFullName(user.getFullName());
				userList.add(uDTO);
			}
		}
		return userList;
	}

}

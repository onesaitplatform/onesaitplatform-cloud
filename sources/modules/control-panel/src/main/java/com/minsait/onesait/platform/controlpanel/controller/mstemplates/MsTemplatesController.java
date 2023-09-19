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
package com.minsait.onesait.platform.controlpanel.controller.mstemplates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.MicroserviceTemplate.Language;
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
	private static final String APP_ID = "appId";	
	private static final String CREDENTIALS_STR = "credentials";
	private static final String EDITION = "edition";
	private static final String REDIRECT_MSTEMPLATE_VIEW = "mstemplate/show";
	private static final String BLOCK_PRIOR_LOGIN = "block_prior_login";

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}
		if (description != null && description.equals("")) {
			description = null;
		}
		final List<MicroserviceTemplate> msTemplates;
		if (utils.isAdministrator()) {
			msTemplates = msTemplateService.findMicroserviceTemplatesWithIdentificationAndDescription(identification, description, utils.getUserId());
		} else {
			msTemplates = msTemplateRepository.findByUserOrderByIdentificationAsc(userService.getUser(utils.getUserId()));
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
	public String update(Model model, @PathVariable("id") String id) {
		model.addAttribute(MSTEMPLATE_STR, msTemplateService.getMsTemplateEditById(id, utils.getUserId()));
		model.addAttribute("language", Language.values());
		final List<Notebook> notebooks = notebookService.getNotebooks(utils.getUserId());
		model.addAttribute("notebooks", notebooks);
		final List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
		model.addAttribute("ontologies", ontologies);
		return MSTEMPLATE_CREATE;
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
				throw new MicroserviceTemplateException(
						"Cannot update microservice template that does not exist or don't have permission");
			}
			return "redirect:/mstemplates/list/";

		} catch (final MicroserviceTemplateException e) {
			utils.addRedirectException(e, redirect);
			return "redirect:/mstemplates/update/" + mstemplate.getId();
		}
	}
	
	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String viewerMsTemplate(Model model, @PathVariable("id") String id, HttpServletRequest request) {
		if (msTemplateService.hasUserViewPermission(id, utils.getUserId())) {
			final MicroserviceTemplate mstemplate = msTemplateService.getById(id);
			model.addAttribute(MSTEMPLATE_STR, mstemplate);
			model.addAttribute(CREDENTIALS_STR, msTemplateService.getCredentialsString(utils.getUserId()));
			model.addAttribute(EDITION, false);
			request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
			return REDIRECT_MSTEMPLATE_VIEW;
		} else {
			request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
			return "redirect:/login";
		}
	}

	@DeleteMapping("{id}")
	public String delete(@PathVariable("id") String id, RedirectAttributes ra) {
		final MicroserviceTemplate msTemplate = msTemplateService.getById(id);
		if (!msTemplateService.hasUserPermission(msTemplate, userService.getUser(utils.getUserId()))) {
			throw new MicroserviceTemplateException(HttpStatus.FORBIDDEN.toString());
		}
		if (msTemplate == null) {
			throw new MicroserviceTemplateException(HttpStatus.NOT_FOUND.toString());
		}
		msTemplateService.delete(id);
		return "redirect:/mstemplates/list";
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

}

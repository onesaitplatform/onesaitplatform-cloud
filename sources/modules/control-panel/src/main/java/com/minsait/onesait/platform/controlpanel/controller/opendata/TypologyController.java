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
package com.minsait.onesait.platform.controlpanel.controller.opendata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.ODTypologyDataset;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ODTypologyRepository;
import com.minsait.onesait.platform.config.services.exceptions.ODTypologyServiceException;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyDatasetService;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@RequestMapping("/opendata/typologies")
@Controller
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
public class TypologyController {

	@Autowired
	private TypologyService typologyService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ODTypologyRepository typologyRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private TypologyDatasetService typologyDatasetService;
	@Autowired 
	private HttpSession httpSession;
	
	private static final String APP_ID = "appId";

	private static final String TYPOLOGY_STR = "typology";
	private static final String TYPOLOGY_CREATE = "opendata/typologies/create";
	private static final String REDIRECT_TYPOLOGY_CREATE = "redirect:/opendata/typologies/create";
	private static final String TYPOLOGY_VALIDATION_ERROR = "typology.validation.error";
	private static final String CREDENTIALS_STR = "credentials";
	private static final String EDITION = "edition";
	private static final String REDIRECT_TYPOLOGIES_VIEW = "opendata/typologies/show";
	private static final String BLOCK_PRIOR_LOGIN = "block_prior_login";
	private static final String USERS = "users";
	private static final String REDIRECT_ERROR_403 = "error/403";

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
		final List<ODTypology> typology;
		if (utils.isAdministrator()) {
			typology = typologyService.findTypologyWithIdentificationAndDescription(identification, description,
					utils.getUserId());
		} else {
			typology = typologyRepository.findByUserOrderByIdentificationAsc(userService.getUser(utils.getUserId()));
		}

		uiModel.addAttribute("typologies", typology);
		return "opendata/typologies/list";

	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute(TYPOLOGY_STR, new ODTypology());

		model.addAttribute(USERS, getUserListDTO());
		model.addAttribute("schema", typologyRepository.findAll());
		return TYPOLOGY_CREATE;
	}

	@PostMapping(value = { "/create" })
	public String createTypology(Model model, @Valid ODTypology typology, BindingResult bindingResult,
			HttpServletRequest request, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(TYPOLOGY_VALIDATION_ERROR, redirect);
			return REDIRECT_TYPOLOGY_CREATE;
		}

		try {
			typologyService.createNewTypology(typology, utils.getUserId());
			return "redirect:/opendata/typologies/list";

		} catch (final ODTypologyServiceException e) {
			utils.addRedirectException(e, redirect);
			return REDIRECT_TYPOLOGY_CREATE;
		}
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		model.addAttribute(TYPOLOGY_STR, typologyService.getTypologyEditById(id, utils.getUserId()));
		return TYPOLOGY_CREATE;
	}

	@PostMapping(value = { "/typologyconf/{id}" })
	public String saveUpdateTypology(@PathVariable("id") String id, ODTypology typology, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(TYPOLOGY_VALIDATION_ERROR, redirect);
			return REDIRECT_TYPOLOGY_CREATE;
		}
		try {
			if (typologyService.hasUserEditPermission(id, utils.getUserId())) {
				typologyService.updatePublicTypology(typology, utils.getUserId());

			} else {
				throw new ODTypologyServiceException(
						"Cannot update Typology that does not exist or don't have permission");
			}
			return "redirect:/opendata/typologies/list";

		} catch (final ODTypologyServiceException e) {
			utils.addRedirectException(e, redirect);
			return "redirect:/opendata/typologies/typologyconf/" + typology.getId();
		}
	}

	@GetMapping(value = "/typologyconf/{id}", produces = "text/html")
	public String updateTypology(Model model, @PathVariable("id") String id) {
		try {
			final ODTypology editTypology = typologyService.getTypologyEditById(id, utils.getUserId());

			if (editTypology != null) {

				final ODTypology typology = new ODTypology();

				typology.setId(id);
				typology.setIdentification(editTypology.getIdentification());
				typology.setDescription(editTypology.getDescription());

				model.addAttribute(TYPOLOGY_STR, typology);

				return TYPOLOGY_CREATE;
			} else {
				return "redirect:/opendata/typologies/list";
			}
		} catch (ODTypologyServiceException e) {
			return "redirect:/opendata/typologies/list";
		}
	}

	@GetMapping(value = "/editor/{id}", produces = "text/html")
	public String editorTypology(Model model, @PathVariable("id") String id) {
		model.addAttribute(TYPOLOGY_STR, typologyService.getTypologyById(id));
		return "opendata/typologies/editor";

	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String viewerTypology(Model model, @PathVariable("id") String id, HttpServletRequest request) {
		if (typologyService.hasUserViewPermission(id, utils.getUserId())) {
			final ODTypology typology = typologyService.getTypologyById(id);
			model.addAttribute(TYPOLOGY_STR, typology);
			model.addAttribute(CREDENTIALS_STR, typologyService.getCredentialsString(utils.getUserId()));
			model.addAttribute(EDITION, false);
			request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
			return REDIRECT_TYPOLOGIES_VIEW;
		} else {
			request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
			return "redirect:/login";
		}
	}

	@PutMapping(value = "/save/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String updateTypology(@PathVariable("id") String id,
			@RequestParam("data") ODTypology typology) {
		typologyService.saveTypology(id, typology, utils.getUserId());
		return "ok";
	}

	@PutMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String deleteTypology(@PathVariable("id") String id) {
		try {
			List<ODTypologyDataset> typologyDataset = typologyDatasetService.getTypologyByTypologyID(id);
			if (typologyDataset == null || typologyDataset.isEmpty()) {
				typologyService.deleteTypology(id, utils.getUserId());
			} else {
				throw new ODTypologyServiceException(
						"The typology cannot be removed. There is a dataset with this typology.");
			}
		} catch (final RuntimeException e) {
			throw new ODTypologyServiceException(e.getMessage());
		}
		return "{\"ok\":true}";
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		try {
			List<ODTypologyDataset> typologyDataset = typologyDatasetService.getTypologyByTypologyID(id);
			if (typologyDataset == null || typologyDataset.isEmpty()) {
				typologyService.deleteTypology(id, utils.getUserId());
			} else {
				throw new ODTypologyServiceException(
						"The typology cannot be removed. There is a dataset with this associated typology.");
			}
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, ra);
		}
		return "redirect:/opendata/typologies/list";
	}

	@GetMapping(value = "/editfull/{id}", produces = "text/html")
	public String editFullDashboard(Model model, @PathVariable("id") String id) {
		if (typologyService.hasUserEditPermission(id, utils.getUserId())) {
			final ODTypology typology = typologyService.getTypologyById(id);
			model.addAttribute(TYPOLOGY_STR, typology);
			model.addAttribute(CREDENTIALS_STR, typologyService.getCredentialsString(utils.getUserId()));
			model.addAttribute(EDITION, true);
			return REDIRECT_TYPOLOGIES_VIEW;
		} else {
			return REDIRECT_ERROR_403;
		}
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

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
package com.minsait.onesait.platform.controlpanel.controller.digitaltwin.type;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.digitaltwin.type.DigitalTwinTypeService;
import com.minsait.onesait.platform.config.services.exceptions.DigitalTwinServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/digitaltwintypes")
@Slf4j
public class DigitalTwinTypeController {

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private DigitalTwinTypeService digitalTwinTypeService;

	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;

	private static final String DIG_TWIN_TYPE_VAL_ERROR = "digitaltwintype.validation.error";
	private static final String REDIRECT_DIG_TWIN_TYPE_CREATE = "redirect:/digitaltwintypes/create";
	private static final String REDIRECT_DIG_TWIN_TYPE_LIST = "redirect:/digitaltwintypes/list";
	private static final String ERROR_403 = "error/403";

	@Autowired
	@Qualifier("MongoManageDBRepository")
	private ManageDBRepository mongoManageRepo;

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.digitalTwinTypeService.getAllIdentifications();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		digitalTwinTypeService.populateCreateNewType(model, utils.getUserId());
		return "digitaltwintypes/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		DigitalTwinType type = digitalTwinTypeService.getDigitalTwinTypeById(id);

		if (!utils.isAdministrator() && !type.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		digitalTwinTypeService.getDigitalTwinToUpdate(model, id, utils.getUserId());
		return "digitaltwintypes/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/create")
	@Transactional
	public String createDigitalTwinType(Model model, @Valid DigitalTwinType digitalTwinType,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest httpServletRequest) {
		if (bindingResult.hasErrors()) {
			log.debug("Some digital twin type properties missing");
			utils.addRedirectMessage(DIG_TWIN_TYPE_VAL_ERROR, redirect);
			return REDIRECT_DIG_TWIN_TYPE_CREATE;
		}

		if (!digitalTwinTypeService.isIdValid(digitalTwinType.getIdentification())) {
			log.debug("The digital twin type name is not valid");
			utils.addRedirectMessage(DIG_TWIN_TYPE_VAL_ERROR, redirect);
			return REDIRECT_DIG_TWIN_TYPE_CREATE;
		}
		try {
			User user = userService.getUser(utils.getUserId());
			digitalTwinType.setUser(user);
			log.info("DigitalTwin is going to be created.");
			digitalTwinTypeService.createDigitalTwinType(digitalTwinType, httpServletRequest);

			// Create ontology for shadow

			digitalTwinTypeService.createOntologyForShadow(digitalTwinType, httpServletRequest);

			// Create collections on mongo for actions

			List<String> tables = mongoManageRepo.getListOfTables();
			tables.replaceAll(String::toUpperCase);

			if (!tables.contains(("TwinActions" + digitalTwinType.getIdentification().substring(0, 1).toUpperCase()
					+ digitalTwinType.getIdentification().substring(1)).toUpperCase())) {
				mongoManageRepo.createTable4Ontology(
						"TwinActions" + digitalTwinType.getIdentification().substring(0, 1).toUpperCase()
								+ digitalTwinType.getIdentification().substring(1),
						"{}", null);
			}
		} catch (DigitalTwinServiceException e) {
			log.error("Cannot create digital twin type because of:" + e.getMessage());
			utils.addRedirectException(e, redirect);
			return REDIRECT_DIG_TWIN_TYPE_CREATE;
		}
		return REDIRECT_DIG_TWIN_TYPE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/list")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification) {

		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
			identification = null;
		}

		List<DigitalTwinType> digitaltwinstypes = new ArrayList<>();

		if (identification == null) {
			digitaltwinstypes = digitalTwinTypeService.getDigitalTwinTypesByUserId(utils.getUserId());
		} else {
			digitaltwinstypes = digitalTwinTypeService.getDigitalTwinTypesByUserIdAndIdentification(utils.getUserId(),
					identification);
		}

		model.addAttribute("digitalTwinTypes", digitaltwinstypes);
		return "digitaltwintypes/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		DigitalTwinType type = digitalTwinTypeService.getDigitalTwinTypeById(id);

		if (!utils.isAdministrator() && !type.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		if (type != null) {
			model.addAttribute("digitaltwintype", type);
			model.addAttribute("dproperties", digitalTwinTypeService.getPropertiesByDigitalId(id));
			model.addAttribute("actions", digitalTwinTypeService.getActionsByDigitalId(id));
			model.addAttribute("events", digitalTwinTypeService.getEventsByDigitalId(id));
			model.addAttribute("logic", digitalTwinTypeService.getLogicByDigitalId(id));

			return "digitaltwintypes/show";
		} else {
			utils.addRedirectMessage("digitaltwintype.notfound.error", redirect);
			return REDIRECT_DIG_TWIN_TYPE_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDigitalTwinType(Model model, @PathVariable("id") String id,
			@Valid DigitalTwinType digitalTwinType, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest httpServletRequest) {

		if (bindingResult.hasErrors()) {
			log.debug("Some digital twin type properties missing");
			utils.addRedirectMessage(DIG_TWIN_TYPE_VAL_ERROR, redirect);
			return "redirect:/digitaltwintypes/update/" + id;
		}

		DigitalTwinType type = digitalTwinTypeService.getDigitalTwinTypeById(id);

		if (!utils.isAdministrator() && !type.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		try {
			User user = userService.getUser(utils.getUserId());
			digitalTwinType.setUser(user);
			this.digitalTwinTypeService.updateDigitalTwinType(digitalTwinType, httpServletRequest);
		} catch (DigitalTwinServiceException e) {
			log.debug("Cannot update Digital Twin Type");
			utils.addRedirectMessage("digitaltwintype.update.error", redirect);
			return REDIRECT_DIG_TWIN_TYPE_CREATE;
		}
		return REDIRECT_DIG_TWIN_TYPE_LIST;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		DigitalTwinType digitalTwinType = digitalTwinTypeService.getDigitalTwinTypeById(id);

		if (!utils.isAdministrator() && !digitalTwinType.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		if (digitalTwinType != null) {
			try {
				this.digitalTwinTypeService.deleteDigitalTwinType(digitalTwinType);
			} catch (DigitalTwinServiceException e) {
				utils.addRedirectMessage("digitaltwintype.delete.error", redirect);
				return REDIRECT_DIG_TWIN_TYPE_LIST;
			}
			return REDIRECT_DIG_TWIN_TYPE_LIST;
		} else {
			return REDIRECT_DIG_TWIN_TYPE_LIST;
		}
	}

	@GetMapping("/getNumOfDevices/{type}")
	public @ResponseBody Integer getNumOfDevices(@PathVariable("type") String type) {
		return this.digitalTwinDeviceService.getNumOfDevicesByTypeId(type);
	}

}

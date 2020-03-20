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
package com.minsait.onesait.platform.controlpanel.controller.digitaltwin.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.exceptions.DigitalTwinServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.helper.digitaltwin.device.DigitalTwinDeviceHelper;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/digitaltwindevices")
public class DigitalTwinDeviceController {

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;

	@Autowired
	private DigitalTwinDeviceHelper digitalTwinDeviceHelper;

	@Autowired
	private ConfigurationService configurationService;

	private static final String REDIRECT_DIGITAL_TWIN_DEV_CREATE = "redirect:/digitaltwindevices/create";
	private static final String REDIRECT_DIGITAL_TWIN_DEV_LIST = "redirect:/digitaltwindevices/list";
	private static final String ERROR_403 = "error/403";

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return digitalTwinDeviceService.getAllIdentifications();
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("digitaltwindevice", new DigitalTwinDevice());
		model.addAttribute("logic", "");
		model.addAttribute("typesDigitalTwin", digitalTwinDeviceService.getAllDigitalTwinTypeNames());
		return "digitaltwindevices/create";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/list")
	public String list(Model model) {
		model.addAttribute("digitalTwinDevices", digitalTwinDeviceService.getAllByUserId(utils.getUserId()));
		return "digitaltwindevices/list";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/generateToken")
	public @ResponseBody String generateToken() {
		return digitalTwinDeviceService.generateToken();
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/getLogicFromType/{type}")
	public @ResponseBody String getLogicFromType(@PathVariable("type") String type) {
		return digitalTwinDeviceService.getLogicFromType(type);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PostMapping(value = "/create")
	@Transactional
	public String createDigitalTwinDevice(Model model, @Valid DigitalTwinDevice digitalTwinDevice,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest httpServletRequest) {
		if (bindingResult.hasErrors()) {
			log.debug("Some digital twin device properties missing");
			utils.addRedirectMessage("digitaltwindevice.create.error", redirect);
			return REDIRECT_DIGITAL_TWIN_DEV_CREATE;
		}
		try {
			final User user = userService.getUser(utils.getUserId());
			digitalTwinDevice.setUser(user);
			digitalTwinDeviceService.createDigitalTwinDevice(digitalTwinDevice, httpServletRequest);

		} catch (final DigitalTwinServiceException e) {
			log.error("Cannot create digital twin device because of:" + e.getMessage());
			utils.addRedirectException(e, redirect);
			return REDIRECT_DIGITAL_TWIN_DEV_CREATE;
		}
		return REDIRECT_DIGITAL_TWIN_DEV_LIST;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		final DigitalTwinDevice device = digitalTwinDeviceService.getDigitalTwinDeviceById(id);
		if (device != null) {
			if (!digitalTwinDeviceService.hasUserAccess(id, utils.getUserId()))
				return ERROR_403;
			model.addAttribute("digitaltwindevice", device);
			model.addAttribute("logic", device.getTypeId().getLogic());
			model.addAttribute("defaultGitlab", configurationService.getDefautlGitlabConfiguration() != null);
			return "digitaltwindevices/show";
		} else {
			utils.addRedirectMessage("digitaltwindevice.notfound.error", redirect);
			return REDIRECT_DIGITAL_TWIN_DEV_LIST;
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		if (!digitalTwinDeviceService.hasUserEditAccess(id, utils.getUserId()))
			return ERROR_403;
		digitalTwinDeviceService.getDigitalTwinToUpdate(model, id);
		model.addAttribute("typesDigitalTwin", digitalTwinDeviceService.getAllDigitalTwinTypeNames());
		return "digitaltwindevices/create";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDigitalTwinDevice(Model model, @PathVariable("id") String id,
			@Valid DigitalTwinDevice digitalTwinDevice, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest httpServletRequest) {

		if (bindingResult.hasErrors()) {
			log.debug("Some digital twin device properties missing");
			utils.addRedirectMessage("digitaltwindevice.validation.error", redirect);
			return "redirect:/digitaltwindevices/update/" + id;
		}

		try {
			if (!digitalTwinDeviceService.hasUserEditAccess(id, utils.getUserId()))
				return ERROR_403;

			digitalTwinDeviceService.updateDigitalTwinDevice(digitalTwinDevice, httpServletRequest);
		} catch (final DigitalTwinServiceException e) {
			log.debug("Cannot update Digital Twin Device");
			utils.addRedirectMessage("digitaltwindevice.update.error", redirect);
			return REDIRECT_DIGITAL_TWIN_DEV_CREATE;
		}
		return REDIRECT_DIGITAL_TWIN_DEV_LIST;

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	@Transactional
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final DigitalTwinDevice digitalTwinDevice = digitalTwinDeviceService.getDigitalTwinDeviceById(id);
		if (digitalTwinDevice != null) {
			try {
				digitalTwinDeviceService.deleteDigitalTwinDevice(digitalTwinDevice);
			} catch (final RuntimeException e) {
				utils.addRedirectException(e, redirect);
			} catch (final Exception e) {
				utils.addRedirectMessage("digitaltwindevice.delete.error", redirect);
				return REDIRECT_DIGITAL_TWIN_DEV_LIST;
			}
			return REDIRECT_DIGITAL_TWIN_DEV_LIST;
		} else {
			return REDIRECT_DIGITAL_TWIN_DEV_LIST;
		}
	}

	@GetMapping(value = "/generateProject/{identification}/{compile}/{sensehat}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<InputStreamResource> generateProject(@PathVariable("identification") String identification,
			@PathVariable("compile") Boolean compile, @PathVariable("sensehat") Boolean sensehat)
			throws FileNotFoundException {

		final File zipFile = digitalTwinDeviceHelper.generateProject(identification, compile, sensehat);

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", zipFile.getName());
		respHeaders.setContentLength(zipFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(zipFile));
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	

}

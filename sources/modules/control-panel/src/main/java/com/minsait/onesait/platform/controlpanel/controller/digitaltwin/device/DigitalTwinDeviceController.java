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
package com.minsait.onesait.platform.controlpanel.controller.digitaltwin.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.exceptions.DigitalTwinServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.helper.digitaltwin.device.DigitalTwinDeviceHelper;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
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

	@Autowired
	private ResourcesInUseService resourcesInUseService;
	
	@Autowired 
	private HttpSession httpSession;

	private static final String REDIRECT_DIGITAL_TWIN_DEV_CREATE = "redirect:/digitaltwindevices/create";
	private static final String REDIRECT_DIGITAL_TWIN_DEV_LIST = "redirect:/digitaltwindevices/list";
	private static final String ERROR_403 = "error/403";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return digitalTwinDeviceService.getAllIdentifications();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("digitaltwindevice", new DigitalTwinDevice());
		model.addAttribute("logic", "");
		model.addAttribute("typesDigitalTwin", digitalTwinDeviceService.getAllDigitalTwinTypeNames());
		
		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId!=null) {
			model.addAttribute(APP_ID, projectId.toString());
		}
		
		return "digitaltwindevices/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/list")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "type") String type) {
		
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		// Scaping "" string values for parameters
		if (identification != null && identification.equals("")) {
		   identification = null;
		}
		if (type != null && type.equals("")) {
			type = null;
		}
		
		List<DigitalTwinDevice> digitaltwindevices = new ArrayList<>();
		
		if(identification == null && type == null) {
			digitaltwindevices = digitalTwinDeviceService.getAllByUserId(utils.getUserId());
		}
		
		if(identification != null && type == null) {
			digitaltwindevices = digitalTwinDeviceService.getAllByUserIdAndIdentification(utils.getUserId(), identification);
		}
		
		if(identification == null && type != null) {
			digitaltwindevices = digitalTwinDeviceService.getAllByUserId(utils.getUserId());
		}
		
		if(identification != null && type != null) {
			digitaltwindevices = digitalTwinDeviceService.getAllByUserIdAndIdentification(utils.getUserId(), identification);
		}
		
		model.addAttribute("digitalTwinDevices", digitaltwindevices);
		return "digitaltwindevices/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/generateToken")
	public @ResponseBody String generateToken() {
		return digitalTwinDeviceService.generateToken();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/getLogicFromType/{type}")
	public @ResponseBody String getLogicFromType(@PathVariable("type") String type) {
		return digitalTwinDeviceService.getLogicFromType(type);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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
			
			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId!=null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.DIGITALTWINDEVICE.toString());
				httpSession.setAttribute("resourceIdentificationAdded", digitalTwinDevice.getIdentification());
				httpSession.removeAttribute(APP_ID);
				return REDIRECT_PROJECT_SHOW + projectId.toString();
			}

		} catch (final DigitalTwinServiceException e) {
			log.error("Cannot create digital twin device because of:" + e.getMessage());
			utils.addRedirectException(e, redirect);
			
			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId!=null) {
				httpSession.removeAttribute(APP_ID);
				return REDIRECT_PROJECT_SHOW + projectId.toString();
			}
			
			return REDIRECT_DIGITAL_TWIN_DEV_CREATE;
		}
		return REDIRECT_DIGITAL_TWIN_DEV_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		if (!digitalTwinDeviceService.hasUserEditAccess(id, utils.getUserId()))
			return ERROR_403;
		digitalTwinDeviceService.getDigitalTwinToUpdate(model, id);
		model.addAttribute(ResourcesInUseService.RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
		resourcesInUseService.put(id, utils.getUserId());

		model.addAttribute("typesDigitalTwin", digitalTwinDeviceService.getAllDigitalTwinTypeNames());
		return "digitaltwindevices/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_DIGITAL_TWIN_DEV_LIST;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
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

	@GetMapping(value = "/createMicroservice/{identification}/{sensehat}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String createMicroservice(@PathVariable("identification") String identification,
			@PathVariable("sensehat") Boolean sensehat,
			@RequestParam(value = "gitlabUrl", required = false) String gitlabUrl,
			@RequestParam(value = "gitlabToken", required = false) String gitlabToken, RedirectAttributes ra) {
		try {
			digitalTwinDeviceHelper.createMicroservice(identification, sensehat, gitlabUrl, gitlabToken);
		} catch (final Exception e) {
			utils.addRedirectException(e, ra);
		}
		return "redirect:/microservices/list";

	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free dashboard resource ", id);
	}

}

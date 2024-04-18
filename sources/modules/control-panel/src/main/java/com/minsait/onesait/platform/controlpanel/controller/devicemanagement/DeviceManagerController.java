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
package com.minsait.onesait.platform.controlpanel.controller.devicemanagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.device.ClientPlatformInstanceService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/devices/management")
@Slf4j
public class DeviceManagerController {

	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ClientPlatformInstanceService deviceService;
	@Autowired
	private UserService userService;
	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private IntegrationResourcesService intregationResourcesService;

	@Autowired
	private GraphDeviceUtil graphDeviceUtil;
	
	@Autowired 
	private HttpSession httpSession;

	private static final String LOG_PREFIX = "LOG_";
	private final ObjectMapper mapper = new ObjectMapper();
	private static final String APP_ID = "appId";


	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String identification,
			@RequestParam(required = false) String[] ontologies) throws JsonProcessingException {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		if (!utils.isAdministrator()) {
			final List<ClientPlatformInstance> devices = new ArrayList<>();
			for (final ClientPlatform client : clientPlatformService
					.getclientPlatformsByUser(userService.getUser(utils.getUserId()))) {
				devices.addAll(deviceService.getByClientPlatformId(client));
			}
			model.addAttribute("devices", devices);
			model.addAttribute("devicesJson", mapper.writeValueAsString(devices));
		} else {
			final List<ClientPlatformInstance> devices = deviceService.getAll();
			model.addAttribute("devices", devices);
			model.addAttribute("devicesJson", mapper.writeValueAsString(devices));
		}

		return "devices/management/list";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PatchMapping
	public String update(Model model, @RequestParam String id, @RequestParam String tags) {

		deviceService.patchClientPlatformInstance(id, tags);
		return "redirect:/devices/management/show/" + id;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("/show")
	public String show(Model model, RedirectAttributes redirect) {
		return "devices/management/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("/show/{id}")
	public String info(Model model, RedirectAttributes redirect, @PathVariable String id)
			throws IOException, DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		final ClientPlatformInstance device = deviceService.getById(id);
		if (null == device) {
			return "redirect:/devices/management/list";
		}
		model.addAttribute("device", device);
		Optional<IoTSession> sessionKey = deviceService.getSessionKeys(device).stream().findFirst();
		if (sessionKey.isPresent()) {
			model.addAttribute("sessionkey", sessionKey.get().getSessionKey());
		} else {
			model.addAttribute("sessionkey", "");
		}
		final String ontology = LOG_PREFIX + device.getClientPlatform().getIdentification().replaceAll(" ", "");
		final String query = "select * from " + ontology + " as c where c.DeviceLog.device = \""
				+ device.getIdentification() + "\" ORDER BY c.contextData.timestampMillis Desc limit 50";
		final String result = queryToolService.querySQLAsJson(utils.getUserId(), ontology, query, 0);
		model.addAttribute("commands", deviceService.getClientPlatformInstanceCommands(device));
		model.addAttribute("query", query.replace(" limit 50", ""));
		model.addAttribute("logs", deviceService.getLogInstances(result));
		final String iotbrokerurl = intregationResourcesService.getUrl(
				com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module.IOTBROKER,
				ServiceUrl.BASE);
		model.addAttribute("iotbrokerUrl",
				iotbrokerurl.contains(IntegrationResourcesServiceImpl.LOCALHOST) ? iotbrokerurl
						: intregationResourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE).concat("iot-broker"));
		return "devices/management/info";
	}

	@GetMapping("/getgraph")
	public @ResponseBody String getGraph(Model model) {

		final List<GraphDeviceDTO> arrayLinks = new LinkedList<>();

		arrayLinks.addAll(graphDeviceUtil.constructGraphWithClientPlatformsForUser());

		return arrayLinks.toString();
	}

}

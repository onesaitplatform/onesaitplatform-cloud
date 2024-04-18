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
package com.minsait.onesait.platform.controlpanel.controller.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.business.services.prometheus.PrometheusService;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource.Resources;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.CategorizationUserRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.services.main.MainService;
import com.minsait.onesait.platform.config.services.menu.MenuService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.simulation.DeviceSimulationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MainPageController {

	private static final String MESSAGE = "message";
	private static final String APP_ID = "appId";
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private MenuService menuService;
	@Autowired
	private UserService userService;
	@Autowired
	private IntegrationResourcesService integrationResourcesService;
	@Autowired
	private CategorizationUserRepository categorizationUserRepository;

	// TEMPORAL
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private DeviceSimulationService deviceSimulationService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private MainService mainService;
	@Autowired
	private PrometheusService prometheusService;
	@Autowired 
	private HttpSession httpSession;


	@GetMapping("/main")
	public String main(Model model, HttpServletRequest request) {
		final User user = userService.getUser(utils.getUserId());
	
		if(model.asMap().containsKey(MESSAGE)) {
			model.addAttribute(MESSAGE, model.asMap().get(MESSAGE));
		}
		model.addAttribute("urlsMap", getUrlsMap());
		// Load menu by role in session
		final String jsonMenu = menuService.loadMenuByRole(userService.getUser(utils.getUserId()));
		// Remove PrettyPrinted
		final String menu = utils.validateAndReturnJson(jsonMenu);
		utils.setSessionAttribute(request, "menu", menu);
		if (request.getSession().getAttribute("apis") == null) {
			utils.setSessionAttribute(request, "apis", integrationResourcesService.getSwaggerUrls());
		}

		final Boolean prometheusEnabled = prometheusEnabled();

		if (utils.isAdministrator()) {
			final List<CategorizationUser> activeCategorizations = categorizationUserRepository.findByUserAndActive(user);
			model.addAttribute("hasCategorizationTreeActive", !activeCategorizations.isEmpty());
		
			if (prometheusEnabled) {
				model.addAttribute("kpis", mainService.createKPIsNew());
			} else {
				model.addAttribute("kpis", mainService.createKPIs());
			}
			model.addAttribute("groupModules", mainService.getGroupModules());
			model.addAttribute("groupServices", mainService.getGroupServices());
			model.addAttribute("prometheusEnabled", prometheusEnabled);
			return "main";
		} else if (utils.isDeveloper()) {
			// FLOW
			model.addAttribute("hasOntology", ontologyService.getOntologiesByUserId(utils.getUserId()).isEmpty() ? false : true);
			model.addAttribute("hasDevice",	clientPlatformRepository.findByUser(user).isEmpty() ? false : true);
			model.addAttribute("hasDashboard", dashboardRepository.findByUser(user).isEmpty() ? false : true);
			model.addAttribute("hasSimulation",	deviceSimulationService.getSimulationsForUser(user.getUserId()).isEmpty() ? false : true);
			model.addAttribute("hasApi", apiRepository.findByUser(user).isEmpty() ? false : true);

			return "main";
		} else if (userService.isUserUser(user)) {
			return "redirect:/dashboards/viewerlist";
		} else if (utils.getRole().equals(Role.Type.ROLE_PLATFORM_ADMIN.name())) {
			return "redirect:/multitenancy/verticals";
		} else if (utils.getRole().equals(Role.Type.ROLE_DATAVIEWER.name())) {
			return "redirect:/dashboards/viewerlist";
		}

		// FLOW
		model.addAttribute("hasOntology", ontologyService.getOntologiesByUserId(utils.getUserId()).isEmpty() ? false : true);
		model.addAttribute("hasDevice",	clientPlatformRepository.findByUser(userService.getUser(utils.getUserId())).isEmpty() ? false : true);
		model.addAttribute("hasDashboard", dashboardRepository.findByUser(userService.getUser(utils.getUserId())).isEmpty() ? false : true);
		model.addAttribute("hasSimulation", deviceSimulationService.getSimulationsForUser(utils.getUserId()).isEmpty() ? false : true);
		model.addAttribute("hasApi", apiRepository.findByUser(userService.getUser(utils.getUserId())).isEmpty() ? false : true);

		return "main";
	}

	private Boolean prometheusEnabled() {
		try {
			prometheusService.getMemStats("onesait-platform");
			prometheusService.getCpuStats("onesait-platform");
			return true;
		} catch (final RuntimeException e) {
			log.debug("Error getting prometheus metrics: " + e);
			return false;
		}
	}

	@GetMapping(value = "/main/memstats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public @ResponseBody ResponseEntity<String> getMemStats() {
		try {
			final String memStats = prometheusService.getMemStats("onesait-platform");
			return new ResponseEntity<>(memStats, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/main/cpustats", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public @ResponseBody ResponseEntity<String> getCpuStats(Model model) {
		try {
			final String cpuStats = prometheusService.getCpuStats("onesait-platform");
			return new ResponseEntity<>(cpuStats, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	private Map<String, String> getUrlsMap() {
		final Map<String, String> urls = new HashMap<>();
		urls.put(Resources.API.name(), "apimanager");
		urls.put(Resources.CLIENTPLATFORM.name(), "devices");
		urls.put(Resources.BINARYFILE.name(), "files");
		urls.put(Resources.DASHBOARD.name(), "dashboards");
		urls.put(Resources.GADGET.name(), "gadgets");
		urls.put(Resources.DIGITALTWINDEVICE.name(), "digitaltwindevices");
		urls.put(Resources.FLOWDOMAIN.name(), "flows");
		urls.put(Resources.NOTEBOOK.name(), "notebooks");
		urls.put(Resources.ONTOLOGY.name(), "ontologies");
		urls.put(Resources.DATAFLOW.name(), "dataflow");
		urls.put(Resources.GADGETDATASOURCE.name(), "datasources");
		urls.put(Resources.ONTOLOGYVIRTUALDATASOURCE.name(), "virtualdatasources");
		urls.put(Resources.CONFIGURATION.name(), "configurations");
		urls.put(Resources.GADGETTEMPLATE.name(), "gadgettemplates");
		urls.put(Resources.REPORT.name(), "reports");
		return urls;
	}
}
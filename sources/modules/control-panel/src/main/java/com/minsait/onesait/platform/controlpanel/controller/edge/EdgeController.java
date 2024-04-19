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
package com.minsait.onesait.platform.controlpanel.controller.edge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Controller
@RequestMapping("/edge")
@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR,ROLE_EDGE_DEVELOPER,ROLE_EDGE_USER')")
public class EdgeController {

	private static final String EDGE_SHOW = "edge/show";
	private static final String BASE_PATH = "basepath";
	private static final String URL = "url";

	@Autowired
	IntegrationResourcesService integrationResourcesService;

	public String getBaseUrl() {
		return integrationResourcesService.getUrl(Module.EDGE, ServiceUrl.BASE);
	}

	@GetMapping(value = "/landscape/devices", produces = "text/html")
	public String showDevices(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/landscape/edge?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@GetMapping(value = "/landscape/field", produces = "text/html")
	public String showField(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/landscape/field?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@GetMapping(value = "/landscape/cloud2cloud", produces = "text/html")
	public String showCloud(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/landscape/cloud2cloud?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR,ROLE_EDGE_DEVELOPER')")
	@GetMapping(value = "/projects", produces = "text/html")
	public String showProjet(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/projects?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR,ROLE_EDGE_DEVELOPER')")
	@GetMapping(value = "/registry", produces = "text/html")
	public String showRepository(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/registry?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR')")
	@GetMapping(value = "/admin/user", produces = "text/html")
	public String showUser(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/user-roles?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR')")
	@GetMapping(value = "/admin/config", produces = "text/html")
	public String showConfig(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/configuration?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR')")
	@GetMapping(value = "/cibersecurity/keys", produces = "text/html")
	public String showKeys(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/cybersecurity/keys?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR')")
	@GetMapping(value = "/cibersecurity/secrets", produces = "text/html")
	public String showSecrets(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/cybersecurity/secrets?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR')")
	@GetMapping(value = "/admin/dashboards", produces = "text/html")
	public String showDashboard(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/dashboards?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_EDGE_ADMINISTRATOR,ROLE_EDGE_DEVELOPER')")
	@GetMapping(value = "/connectors", produces = "text/html")
	public String showConnectors(Model model) {
		String baseUrl = getBaseUrl();
		model.addAttribute(URL, baseUrl + "/#/connectors?token=");
		model.addAttribute(BASE_PATH, baseUrl);
		return EDGE_SHOW;
	}

}

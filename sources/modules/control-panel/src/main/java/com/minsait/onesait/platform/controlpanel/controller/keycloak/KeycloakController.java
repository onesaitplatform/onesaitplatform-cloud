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
package com.minsait.onesait.platform.controlpanel.controller.keycloak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@RequestMapping("/keycloak")
@Controller
public class KeycloakController {

	@Autowired
	private IntegrationResourcesService resourcesService;

	public String getAdminUrl() {
		return resourcesService.getUrl(Module.KEYCLOAK_MANAGER, ServiceUrl.ADMIN);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/show", produces = "text/html")
	public String show(Model model) {

		model.addAttribute("adminUrl", getAdminUrl().endsWith("/") ? getAdminUrl()
				: getAdminUrl() + "/" + Tenant2SchemaMapper
				.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()) + "/console");

		return "keycloak/show";
	}

}

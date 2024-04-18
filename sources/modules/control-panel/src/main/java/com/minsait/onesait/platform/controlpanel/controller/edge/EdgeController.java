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
package com.minsait.onesait.platform.controlpanel.controller.edge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
@RequestMapping("/edge")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER')")
public class EdgeController {

	private final static String EDGE_SHOW = "edge/show";

	@Value("${edge.base.url: https://watergylab.homedns.org}")
	private String baseUrl;

	@Autowired
	private AppWebUtils utils;

	@GetMapping(value = "/devices", produces = "text/html")
	public String showDevices(Model model) {

		String url = baseUrl + "/ui/#/devices";

		model.addAttribute("url", url);
		model.addAttribute("role", utils.getRole());
		model.addAttribute("basepath", baseUrl);
		return EDGE_SHOW;

	}

	@GetMapping(value = "/bastions", produces = "text/html")
	public String showBastions(Model model) {

		String url = baseUrl + "/ui/#/bastions";

		model.addAttribute("url", url);
		model.addAttribute("role", utils.getRole());
		return EDGE_SHOW;

	}

	@GetMapping(value = "/organizations", produces = "text/html")
	public String showOrganizations(Model model) {

		String url = baseUrl + "/ui/#/organizations";

		model.addAttribute("url", url);
		model.addAttribute("role", utils.getRole());
		return EDGE_SHOW;

	}

}

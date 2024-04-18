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
package com.minsait.onesait.platform.controlpanel.controller.multitenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.config.model.MasterConfiguration;
import com.minsait.onesait.platform.multitenant.config.services.MultitenantConfigurationService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("multitenancy/configuration")
@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
@Slf4j
public class MultitenantConfigurationController {

	@Autowired
	private MultitenantConfigurationService multitenantConfigurationService;
	@Autowired
	private AppWebUtils utils;

	@GetMapping
	public String list(Model model) {
		model.addAttribute("configurations", multitenantConfigurationService.getMasterConfigurations());
		return "multitenancy/configuration/list";
	}

	@GetMapping("update/{id}")
	public String updatePage(Model model, @PathVariable("id") String id) {
		model.addAttribute("configuration", multitenantConfigurationService.getConfiguration(id));
		model.addAttribute("configurationTypes", MasterConfiguration.Type.values());
		return "multitenancy/configuration/create";
	}

	@GetMapping("create")
	public String createPage(Model model) {
		model.addAttribute("configuration", new MasterConfiguration());
		model.addAttribute("configurationTypes", MasterConfiguration.Type.values());
		return "multitenancy/configuration/create";
	}

	@PostMapping("create")
	public String create(@ModelAttribute MasterConfiguration configuration, RedirectAttributes ra) {
		try {
			multitenantConfigurationService.createConfiguration(configuration);
		} catch (final Exception e) {
			log.error("Error while creating Master Configuration");
			utils.addRedirectException(e, ra);
		}
		return "redirect:/multitenancy/configuration";
	}

	@PutMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id, @ModelAttribute MasterConfiguration configuration,
			RedirectAttributes ra) {
		if (multitenantConfigurationService.getConfiguration(id) != null) {
			try {
				multitenantConfigurationService.updateConfiguration(configuration, id);
			} catch (final Exception e) {
				log.error("Error while updating Master Configuration");
				utils.addRedirectException(e, ra);
			}
		}
		return "redirect:/multitenancy/configuration";
	}

	@DeleteMapping("{id}")
	public String delete(@PathVariable("id") String id, RedirectAttributes ra) {
		try {
			multitenantConfigurationService.deleteConfiguration(id);
		} catch (final Exception e) {
			log.error("Error while deleting Master Configuration");
			utils.addRedirectException(e, ra);
		}
		return "redirect:/multitenancy/configuration";
	}
}

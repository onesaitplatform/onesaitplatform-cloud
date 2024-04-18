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
package com.minsait.onesait.platform.controlpanel.controller.configuration;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Configuration;

import com.minsait.onesait.platform.config.model.ProjectResourceAccessList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.exceptions.ConfigServiceException;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/configurations")
@Slf4j
public class ConfigurationController {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ProjectResourceAccessRepository projectResourceAccessRepository;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired 
	private HttpSession httpSession;

	@Value("${dynamic-load-balancer.enable}")
	private Boolean nginxServiceEnabled;

	private static final String CONFIGURATION_STR = "configuration";
	private static final String CONF_CREATE = "configurations/create";
	private static final String REDIRECT_CONF_LIST = "redirect:/configurations/list";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";
	private static final String APP_USER_ACCESS = "app_user_access";

	@GetMapping("/list")
	public String list(Model model) {
		
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		final List<Configuration> configurations = configurationService
				.getAllConfigurations(userService.getUser(utils.getUserId()));
		model.addAttribute("configurations", configurations);
		model.addAttribute("nginxServiceEnabled", nginxServiceEnabled);
		return "configurations/list";

	}

	@GetMapping("/create")
	public String createForm(Model model) {
		populateFormData(model);
		final Configuration configuration = new Configuration();
		// Logged user is going to be the creator of the new config
		configuration.setUser(userService.getUser(utils.getUserId()));

		model.addAttribute(CONFIGURATION_STR, configuration);
		
		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId!=null) {
			model.addAttribute(APP_ID, projectId.toString());
		}
		
		return CONF_CREATE;

	}

	@PostMapping("/create")
	public String create(@Valid Configuration configuration, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("configuration.validation.error", redirectAttributes);
			log.debug("Missing fields");
			return "redirect:/configurations/create";
		}
		try {
			configurationService.createConfiguration(configuration);
		}catch (ConfigServiceException e){
			utils.addRedirectMessage(e.getMessage(), redirectAttributes);
			return "redirect:/configurations/create";
		}
		
		
		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId!=null) {
			httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.CONFIGURATION.toString());
			httpSession.setAttribute("resourceIdentificationAdded", configuration.getIdentification());
			httpSession.removeAttribute(APP_ID);
			return REDIRECT_PROJECT_SHOW + projectId.toString();
		}
		
		return REDIRECT_CONF_LIST;

	}

	@GetMapping("/update/{id}")
	public String updateForm(@PathVariable String id, Model model) {

		populateFormData(model);
		Configuration configuration = configurationService.getConfiguration(id);

		if (configuration == null) {
			configuration = new Configuration();
			configuration.setUser(userService.getUser(utils.getUserId()));
		}
		
		ResourceAccessType resourceAccess = resourceService.getResourceAccess(utils.getUserId(),configuration.getId());
		
		if (utils.isAdministrator() || configuration.getUser().getUserId().equals(utils.getUserId())
				|| (resourceAccess!= null && resourceAccess.equals(ResourceAccessType.MANAGE))) {
			model.addAttribute(APP_USER_ACCESS, resourceAccess);
			model.addAttribute(CONFIGURATION_STR, configuration);
			return CONF_CREATE;
		} else {
			return "error/403";
		}

	}

	@PutMapping("/update/{id}")
	public String update(@PathVariable String id, Model model, @ModelAttribute Configuration configuration,
        RedirectAttributes redirectAttributes) {

		if (configuration != null) {

			try {
				if (utils.isAdministrator() || configuration.getUser().getUserId().equals(utils.getUserId())
						|| resourceService.hasAccess(utils.getUserId(), configuration.getId(),
								ResourceAccessType.MANAGE)) {
					configurationService.updateConfiguration(configuration);
				} else {
					return "error/403";
				}
			} catch (final Exception e) {
				log.debug(e.getMessage());
	            utils.addRedirectException(e, redirectAttributes);
				return "redirect:/configurations/update/" + id;
			}
		} else {
			return "redirect:/update/" + id;
		}

		model.addAttribute(CONFIGURATION_STR, configuration);
		return REDIRECT_CONF_LIST;

	}

	private void populateFormData(Model model) {
		model.addAttribute("configurationTypes",
				configurationService.getAllConfigurationTypes(userService.getUser(utils.getUserId())));
		// model.addAttribute("environments",
		// this.configurationService.getEnvironmentValues());
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(@PathVariable("id") String id, Model model) {
		Configuration configuration = null;
		if (id != null) {
			configuration = configurationService.getConfiguration(id);
		}
		if (configuration == null)
			return "error/404";

		ResourceAccessType resourceAccess = resourceService.getResourceAccess(utils.getUserId(),configuration.getId());
		
		if (utils.isAdministrator() || configuration.getUser().getUserId().equals(utils.getUserId())
				|| (resourceAccess!= null)) {
			model.addAttribute(APP_USER_ACCESS, resourceAccess);
			model.addAttribute(CONFIGURATION_STR, configuration);
			return "configurations/show";
		} else {
			return "error/403";
		}
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
		
		Configuration configuration = null;
		if (id != null) {
			configuration = configurationService.getConfiguration(id);
		}
		ProjectResourceAccessList getResource = null;
		getResource = projectResourceAccessRepository.getResource_id(id);
		
		if (getResource != null) {
			utils.addRedirectMessage("This setting is shared within an app, please revoke app access before deleting", ra);
			return REDIRECT_CONF_LIST;
		}
	
		if (configuration == null)
			return "error/404";
		if (utils.isAdministrator() || configuration.getUser().getUserId().equals(utils.getUserId())
				|| resourceService.hasAccess(utils.getUserId(), configuration.getId(), ResourceAccessType.MANAGE)) {
			configurationService.deleteConfiguration(id);
			return REDIRECT_CONF_LIST;
		} else {
			return "error/403";
		}
	}

	@GetMapping("/reload")
	public String reloadConfigurations(Model model) {
		resourcesService.reloadConfigurations();
		return REDIRECT_CONF_LIST;
	}

}

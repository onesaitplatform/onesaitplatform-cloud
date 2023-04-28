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
package com.minsait.onesait.platform.controlpanel.controller.dashboardconf;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.services.dashboardconf.DashboardConfService;
import com.minsait.onesait.platform.config.services.exceptions.DashboardConfServiceException;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dashboardconf")
@Controller
@Slf4j
public class DashboardConfController {

	private static final String DASHBOARD_CONF = "dashboardconf";
	private static final String MESSAGE = "message";

	@Autowired
	private DashboardConfService dashboardConfService;

	@Autowired()
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private AppWebUtils utils;
	
	@Autowired 
	private HttpSession httpSession;

	private static final String REDIRECT_DASHBOARD_CONF_LIST = "redirect:/dashboardconf/list";
	private static final String APP_ID = "appId";

	@RequestMapping(method = RequestMethod.POST, value = "getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.dashboardConfService.getAllIdentifications();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request) {

		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		final List<DashboardConf> dc = this.dashboardConfService.findAllDashboardConf();

		uiModel.addAttribute(DASHBOARD_CONF, dc);

		return "dashboardconf/list";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createDashboardConf(Model model) {
		model.addAttribute(DASHBOARD_CONF, new DashboardConf());
		return "dashboardconf/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/create", produces = "text/html")
	public String saveDashboardConf(@Valid DashboardConf dashboardConf, BindingResult bindingResult, Model model,
			HttpServletRequest httpServletRequest, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some DashboardConf properties missing");
			dashboardConf.setId(null);
			model.addAttribute(DASHBOARD_CONF, dashboardConf);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error", ""));
			return "dashboardconf/create";
		}
		// Error identification exist
		List<DashboardConf> ldc = this.dashboardConfService
				.getDashboardConfByIdentification(dashboardConf.getIdentification());
		if (ldc.size() > 0) {
			dashboardConf.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error.identifier", ""));
			model.addAttribute(DASHBOARD_CONF, dashboardConf);
			return "dashboardconf/create";
		}
		try {
			this.dashboardConfService.saveDashboardConf(dashboardConf);
			return REDIRECT_DASHBOARD_CONF_LIST;

		} catch (final DashboardConfServiceException e) {
			utils.addRedirectException(e, redirect);
			dashboardConf.setId(null);
			model.addAttribute(MESSAGE, utils.getMessage("dashboardConf.validation.error", ""));
			model.addAttribute(DASHBOARD_CONF, dashboardConf);
			return "dashboardconf/create";
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String updateDashboardConf(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_CONF, this.dashboardConfService.getDashboardConfById(id));
		model.addAttribute(ResourcesInUseService.RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
		resourcesInUseService.put(id, utils.getUserId());
		return "dashboardconf/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/view/{id}", produces = "text/html")
	public String showDashboardConf(Model model, @PathVariable("id") String id) {
		model.addAttribute(DASHBOARD_CONF, this.dashboardConfService.getDashboardConfById(id));
		return "dashboardconf/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id) {
		this.dashboardConfService.deleteDashboardConf(id);
		return REDIRECT_DASHBOARD_CONF_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDashboardConf(Model model, @PathVariable("id") String id, @Valid DashboardConf dashboardConf,
			BindingResult bindingResult, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some DashboardConf properties missing");
			utils.addRedirectMessage("dashboardConf.validation.error", redirect);
			return "redirect:/dashboardconf/update/" + id;
		}
		this.dashboardConfService.saveDashboardConf(dashboardConf);
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_DASHBOARD_CONF_LIST;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	/*
	 * @GetMapping(value = "getDashboardConfByIdentification/{identification}")
	 * public @ResponseBody DashboardConf getDashboardConfByIdentification(
	 * 
	 * @PathVariable("identification") String identification) { return
	 * this.dashboardConfService.get(identification, utils.getUserId()); }
	 */

}
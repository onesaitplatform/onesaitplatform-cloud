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
package com.minsait.onesait.platform.controlpanel.controller.virtual.datasources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.minsait.onesait.platform.business.services.virtual.datasources.VirtualDatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.exceptions.VirtualDatasourceServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/virtualdatasources")
@Slf4j
public class VirtualDatasourceController {

	@Autowired
	private UserService userService;

	@Autowired
	private VirtualDatasourceService virtualDatasourceService;

	@Autowired
	private AppWebUtils utils;

	private static final String DATASOURCE_STR = "datasource";
	private static final String VIRTUAL_DATASOURCE_CREATE = "virtualdatasources/create";
	private static final String REDIRECT_VIRT_DS_LIST = "redirect:/virtualdatasources/list";
	private static final String REDIRECT_VIRT_DS_CREATE = "redirect:/virtualdatasources/create";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false, name = "identification") String identification) {

		model.addAttribute("datasources", virtualDatasourceService.getAllByDatasourceNameAndUser(identification, utils.getUserId()));
		return "virtualdatasources/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create", produces = "text/html")
	public String create(Model model) {
		model.addAttribute("fieldDisabled", "disabled");
		model.addAttribute(DATASOURCE_STR, new OntologyVirtualDatasource());
		model.addAttribute("rdbs", 
				Arrays.stream(VirtualDatasourceType.values()).
				filter(o -> !o.equals(VirtualDatasourceType.KUDU)).
				collect(Collectors.toList()));
		return VIRTUAL_DATASOURCE_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final OntologyVirtualDatasource datasource = virtualDatasourceService.getDatasourceByIdAndUserIdOrIsPublic(id, utils.getUserId());
			if (datasource != null) {
				model.addAttribute(DATASOURCE_STR, datasource);
				return "virtualdatasources/show";

			} else {
				utils.addRedirectMessage("datasource.notfound.error", redirect);
				return REDIRECT_VIRT_DS_LIST;
			}
		} catch (final OntologyServiceException e) {
			return REDIRECT_VIRT_DS_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		try {
			final OntologyVirtualDatasource datasource = virtualDatasourceService.getDatasourceByIdAndUserId(id, utils.getUserId());
			if (datasource != null) {
				model.addAttribute("fieldDisabled", "disabled");
				model.addAttribute(DATASOURCE_STR, datasource);
				model.addAttribute("rdbs", 
						Arrays.stream(VirtualDatasourceType.values()).
						filter(o -> !o.equals(VirtualDatasourceType.KUDU)).
						collect(Collectors.toList()));
				return VIRTUAL_DATASOURCE_CREATE;
			} else {
				return VIRTUAL_DATASOURCE_CREATE;
			}
		} catch (final RuntimeException e) {
			return VIRTUAL_DATASOURCE_CREATE;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/create")
	@Transactional
	public String createDatasource(Model model, @Valid OntologyVirtualDatasource datasource,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		if (bindingResult.hasErrors()) {
			log.debug("Some datasource properties missing");
			utils.addRedirectMessage("datasource.validation.error", redirect);
			return REDIRECT_VIRT_DS_CREATE;
		}
		try {

			final User user = userService.getUser(utils.getUserId());
			datasource.setUserId(user);
			virtualDatasourceService.createDatasource(datasource);

		} catch (final OntologyServiceException | GenericOPException e) {
			log.error("Cannot create virtual datasource because of:" + e.getMessage(), e);
			utils.addRedirectException(e, redirect);
			redirect.addFlashAttribute(DATASOURCE_STR, datasource);
			return REDIRECT_VIRT_DS_CREATE;
		}
		return REDIRECT_VIRT_DS_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDatasource(Model model, @PathVariable("id") String id,
			@Valid OntologyVirtualDatasource datasource, BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some virtual datasource properties missing");
			utils.addRedirectMessage("datasource.validation.error", redirect);
			return "redirect:/virtualdatasources/update/" + id;
		}

		try {
			final OntologyVirtualDatasource datasourceOld = virtualDatasourceService.getDatasourceByIdAndUserId(id, utils.getUserId());	
			datasource.setUserId(datasourceOld.getUserId());
			this.virtualDatasourceService.updateOntology(datasource);
		} catch (VirtualDatasourceServiceException e) {
			log.error("Cannot update datasource", e);
			utils.addRedirectMessage("datasource.update.error", redirect);
			return REDIRECT_VIRT_DS_CREATE;
		}
		return "redirect:/virtualdatasources/show/" + id;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final OntologyVirtualDatasource datasource = virtualDatasourceService.getDatasourceByIdAndUserId(id, utils.getUserId());
		if (datasource != null) {
			try {
				virtualDatasourceService.deleteDatasource(datasource);

			} catch (final Exception e) {
				utils.addRedirectMessage("datasource.delete.error", redirect);
				log.error("Error deleting virtual datasource. ", e);
				return REDIRECT_VIRT_DS_LIST;
			}
			return REDIRECT_VIRT_DS_LIST;
		} else {
			return REDIRECT_VIRT_DS_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		List<String> datasources = virtualDatasourceService.getAllIdentifications();
		Collections.sort(datasources);
		return datasources;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/checkConnection")
	public @ResponseBody ResponseEntity<?> getTables(@RequestParam String datasource, @RequestParam String user,
			@RequestParam String credentials, @RequestParam String sgdb, @RequestParam String url,
			@RequestParam String queryLimit) {
		Boolean valid;
		try {
			valid = this.virtualDatasourceService.checkConnection(datasource, user, credentials, sgdb, url, queryLimit);
			return new ResponseEntity<>(valid, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = "/checkConnectionExtern")
	public @ResponseBody ResponseEntity<?> checkConnectionExtern(@RequestParam String datasource) {
		Boolean valid;
		try {
			valid = this.virtualDatasourceService.checkConnectionExtern(datasource);
			return new ResponseEntity<>(valid, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/public")
	public String changePublic(@RequestParam("datasource") String datasource) {
		virtualDatasourceService.changePublic(datasource);
		return REDIRECT_VIRT_DS_LIST;
	}

}

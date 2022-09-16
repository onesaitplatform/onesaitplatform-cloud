/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

@Controller
@RequestMapping("multitenancy")
public class MultitenancyController {

	private static final String VERTICAL = "vertical";
	private static final String VERTICALS = "verticals";
	private static final String PASSWORD_PATTERN = "password-pattern";
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private AppWebUtils utils;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@GetMapping("verticals")
	public String list(Model model) {
		model.addAttribute(VERTICALS, verticals());
		return "multitenancy/verticals/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN,ROLE_ADMINISTRATOR')")
	@GetMapping("tenants")
	public String tenants(Model model) {
		final Optional<Vertical> vertical = multitenancyService
				.getVertical(MultitenancyContextHolder.getVerticalSchema());
		model.addAttribute("tenants", tenants(vertical.get().getName()));
		model.addAttribute(VERTICAL, vertical.get());
		return "multitenancy/tenants/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@GetMapping("verticals/data")
	@ResponseBody
	public List<VerticalDTO> verticals() {

		return multitenancyService.getAllVerticals().stream()
				.map(v -> VerticalDTO.builder().name(v.getName()).configDBSchema(v.getSchema()).build())
				.collect(Collectors.toList());
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN,ROLE_ADMINISTRATOR')")
	@GetMapping("vertical/{name}/tenants/data")
	@ResponseBody
	public List<TenantDTO> tenants(@PathVariable("name") String vertical) {
		final Optional<Vertical> v = multitenancyService.getVertical(vertical);
		if (v.isPresent()) {
			return v.get().getTenants().stream()
					.map(t -> TenantDTO.builder().name(t.getName())
							.users(multitenancyService.countTenantUsers(t.getName()))
							.vertical(t.getVerticals().stream().map(Vertical::getName).collect(Collectors.toList()))
							.build())
					.collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN,ROLE_ADMINISTRATOR')")
	@GetMapping("tenants/{name}/users/data")
	@ResponseBody
	public List<UserDTO> users(@PathVariable("name") String tenant) {
		return multitenancyService.getUsers(tenant).stream()
				.map(mu -> UserDTO.builder().userId(mu.getUserId()).fullName(mu.getFullName()).build())
				.collect(Collectors.toList());
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN,ROLE_ADMINISTRATOR')")
	@GetMapping("tenants/{name}/show")
	public String showTenant(@PathVariable("name") String tenant, Model model) {
		final Optional<Tenant> t = multitenancyService.getTenant(tenant);
		if (t.isPresent()) {
			model.addAttribute("users", users(tenant));
			model.addAttribute("tenant", t.get());
			return "multitenancy/tenants/create";
		}
		return "error/404";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@GetMapping("verticals/create")
	public String verticalCreate(Model model) {
		model.addAttribute(VERTICAL, new Vertical());
		return "multitenancy/verticals/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN,ROLE_ADMINISTRATOR')")
	@GetMapping("tenants/create")
	public String tenantCreate(Model model) {
		model.addAttribute("tenant", new Tenant());
		model.addAttribute("user", new User());
		model.addAttribute("passwordPattern", getPasswordPattern());
		if (utils.isPlatformAdmin()) {
			model.addAttribute("verticals", multitenancyService.getAllVerticals());
		}
		return "multitenancy/tenants/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@PostMapping("verticals/create")
	public String createVertical(Model model, @ModelAttribute Vertical vertical) {
		multitenancyService.createVertical(vertical);
		return "redirect:/multitenancy/verticals";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN,ROLE_ADMINISTRATOR')")
	@PostMapping("tenants/create")
	public String createTenant(Model model, @ModelAttribute Tenant tenant, @ModelAttribute User user,
			RedirectAttributes ra, @RequestParam(value = "vertical", required = false) String vertical) {
		try {
			multitenancyService.createTenant(multitenancyService.getVertical(vertical).orElse(null), tenant, user);
		} catch (final Exception e) {
			utils.addRedirectException(e, ra);

		}
		return "redirect:/multitenancy/tenants";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@GetMapping("verticals/update/{name}")
	public String verticalUpdate(Model model, @PathVariable("name") String name) {
		final Optional<Vertical> vertical = multitenancyService.getVertical(name);
		if (!vertical.isPresent()) {
			return "error/404";
		} else {
			model.addAttribute("tenants", tenants(vertical.get().getName()));
			model.addAttribute(VERTICAL, vertical.get());
			model.addAttribute("verticalsAvailable", multitenancyService.getAllTenants().stream()
					.map(t -> TenantDTO.builder().name(t.getName()).build()).filter(t -> !vertical.get().getTenants()
							.stream().map(Tenant::getName).collect(Collectors.toList()).contains(t.getName()))
					.collect(Collectors.toList()));
		}
		return "multitenancy/verticals/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@PutMapping("verticals/update/{name}")
	public String updateVertical(Model model, @ModelAttribute Vertical vertical, @PathVariable("name") String name) {

		return "redirect:/multitenancy/verticals";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_PLATFORM_ADMIN')")
	@PostMapping("verticals/{name}/tenants/add")
	public ResponseEntity<String> addTenantToVertical(@PathVariable("name") String vertical,
			@RequestParam("tenant") String tenant) {
		multitenancyService.addTenant(vertical, tenant);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private String getPasswordPattern() {
		return ((String) resourcesService.getGlobalConfiguration().getEnv().getControlpanel().get(PASSWORD_PATTERN));
	}
}

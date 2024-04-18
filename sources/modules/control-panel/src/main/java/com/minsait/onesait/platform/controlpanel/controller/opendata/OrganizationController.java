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
package com.minsait.onesait.platform.controlpanel.controller.opendata;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.opendata.OpenDataPermissions;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.business.services.opendata.user.OpenDataUserService;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataAuthorizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataMember;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataRole;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataUser;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/opendata/organizations")
@Controller
@Slf4j
public class OrganizationController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private OpenDataPermissions openDataPermissions;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private OpenDataUserService openDataUserService;
	@Autowired 
	private HttpSession httpSession;
	
	private static final String APP_ID = "appId";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/list", produces = "text/html")
	public String listOrganizations(Model model, RedirectAttributes redirect, 
	    @RequestParam(required = false, name = "name") String name) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		final String userToken = utils.getCurrentUserOauthToken();
		try {
			List<OpenDataOrganization> organizationsFromUser = organizationService
					.getOrganizationsFromUser(userToken);
			if(name != null) {
			    organizationsFromUser = organizationsFromUser.stream().filter(o -> o.getDisplay_name().contains(name)).collect(Collectors.toList());
			}
			
			final List<OpenDataOrganizationDTO> organizationsDTO = organizationService
					.getDTOFromOrganizationList(organizationsFromUser);
			model.addAttribute("organizations", organizationsDTO);
			return "opendata/organizations/list";
		} catch (final ResourceAccessException e) {
			log.error("Error listing organizations: " + e.getMessage());
			utils.addRedirectMessage("organizations.error.accessing", redirect);
			return "redirect:/main";
		}
	}

	@GetMapping(value = "/create")
	public String createOrganization(Model model) {
		if (!utils.isAdministrator()) {
			return "error/403";
		}
		model.addAttribute("organization", new OpenDataOrganizationDTO());
		return "opendata/organizations/create";
	}

	@PostMapping(value = { "/create" }, produces = "text/html")
	public String createOrganization(Model model, @RequestParam(value = "image", required = false) MultipartFile image,
			@ModelAttribute OpenDataOrganizationDTO organizationDTO, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator()) {
			return "error/403";
		}

		try {
			organizationDTO.setName(organizationService.getOrganizationId(organizationDTO.getTitle()));
			if (organizationService.existsOrganization(organizationDTO, userToken)) {
				log.error("This Organization already exists");
				utils.addRedirectMessage("organizations.error.exist", redirect);
				return "redirect:/opendata/organizations/create";
			}
			organizationService.createOrganization(organizationDTO, image, userToken);
			log.debug("Organization has been created succesfully");
			return "redirect:/opendata/organizations/list";

		} catch (final HttpClientErrorException e) {
			log.error("Cannot create organization in Open Data Portal: " + e.getResponseBodyAsString());
			utils.addRedirectMessage(" Cannot create organization in Open Data Portal: " + e.getResponseBodyAsString(),
					redirect);
			return "redirect:/opendata/organizations/create";
		} catch (final Exception e) {
			log.error("Cannot create organization: " + e.getMessage());
			utils.addRedirectMessage("organizations.error.created", redirect);
			return "redirect:/opendata/organizations/create";
		}
	}

	@GetMapping(value = "/update/{id}")
	public String updateOrganization(@PathVariable String id, Model model, HttpServletRequest request) {

		final String userToken = utils.getCurrentUserOauthToken();
		OpenDataOrganization organization = null;
		if (id != null) {
			organization = organizationService.getOrganizationById(userToken, id);
		}
		if (organization == null) {
			return "error/404";
		}
		if (!utils.isAdministrator()
				&& !openDataPermissions.hasPermissionsToManipulateOrganization(utils.getUserId(), organization)) {
			return "error/403";
		}

		final OpenDataOrganizationDTO organizationDTO = organizationService.getDTOFromOrganization(organization);
		final List<OpenDataUser> users = openDataUserService.getAllUsers();
		final List<OpenDataRole> roles = openDataUserService.getAllRoles();
		final List<OpenDataAuthorizationDTO> authorizations = organizationService.getDTOAuthorizations(id, userToken);

		model.addAttribute("roles", roles);
		model.addAttribute("authorizations", authorizations);
		model.addAttribute("users", users);
		model.addAttribute("organization", organizationDTO);
		return "opendata/organizations/create";
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	public String updateOrganization(@PathVariable String id, Model model,
			@ModelAttribute OpenDataOrganizationDTO organizationDTO,
			@RequestParam(value = "image", required = false) MultipartFile image, RedirectAttributes redirect,
			HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (organizationDTO != null) {
			try {
				final OpenDataOrganization organization = organizationService.getOrganizationById(userToken, id);
				if (organization != null) {
					organizationDTO.setName(organization.getName());
					if (!utils.isAdministrator() && !openDataPermissions
							.hasPermissionsToManipulateOrganization(utils.getUserId(), organization)) {
						return "error/403";
					}
					organizationService.updateOrganization(organizationDTO, image, userToken);
				}
			} catch (final HttpClientErrorException e) {
				log.error("Cannot update organization in Open Data Portal: " + e.getResponseBodyAsString());
				utils.addRedirectMessage(
						" Cannot update organization in Open Data Portal: " + e.getResponseBodyAsString(), redirect);
				return "redirect:/opendata/organizations/list";
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Cannot update organization {}", e.getMessage());
				}
				utils.addRedirectMessage("organizations.error.updated", redirect);
				return "redirect:/opendata/organizations/list";
			}
		} else {
			return "redirect:/opendata/organizations/update/" + id;
		}
		log.debug("Organization has been updated succesfully");
		return "redirect:/opendata/organizations/show/" + organizationDTO.getId();
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String showOrganization(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final String userToken = utils.getCurrentUserOauthToken();
			final String userId = utils.getUserId();

			OpenDataOrganization organization = null;
			if (id != null) {
				organization = organizationService.getOrganizationById(userToken, id);
			}
			if (organization == null) {
				return "error/404";
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToShowOrganization(organization, userId)) {
				return "error/403";
			}

			final OpenDataOrganizationDTO organizationDTO = organizationService.getDTOFromOrganization(organization);
			final List<OpenDataOrganization> orgsFromUser = organizationService.getOrganizationsFromUser(userToken);
			List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken);
			datasets = datasets.stream().filter(elem -> elem.getOwner_org().equals(id)).collect(Collectors.toList());
			final List<OpenDataPackageDTO> datasetsDTO = datasetService.getDTOFromDatasetList(datasets, orgsFromUser);
			final List<OpenDataAuthorizationDTO> authorizations = organizationService.getDTOAuthorizations(id,
					userToken);

			model.addAttribute("authorizations", authorizations);
			model.addAttribute("datasetsList", datasetsDTO);
			model.addAttribute("organization", organizationDTO);
			model.addAttribute("modifyPermissions",
					openDataPermissions.hasPermissionsToManipulateOrganization(userId, organization));
			return "opendata/organizations/show";
		} catch (final Exception e) {
			log.error("Error in Organizations controller: " + e.getMessage());
			return "error/500";
		}
	}

	@DeleteMapping("/delete/{id}")
	public String deleteOrganization(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final String userToken = utils.getCurrentUserOauthToken();
		final OpenDataOrganization organization = organizationService.getOrganizationById(userToken, id);
		if (!utils.isAdministrator()
				&& !openDataPermissions.hasPermissionsToManipulateOrganization(utils.getUserId(), organization)) {
			return "error/403";
		}
		if (organization != null) {
			try {
				organizationService.deleteOrganization(userToken, id);
			} catch (final HttpClientErrorException e) {
				log.error("Could not delete the Organization in Open Data Portal: " + e.getResponseBodyAsString());
				if (e.getStatusCode() == HttpStatus.CONFLICT) {
					utils.addRedirectMessage("organizations.error.delete.datasets", redirect);
				} else {
					utils.addRedirectMessage(
							" Could not delete the Organization in Open Data Portal: " + e.getResponseBodyAsString(),
							redirect);
				}
			} catch (final Exception e) {
				log.error("Could not delete the Organization");
				utils.addRedirectMessage("organizations.error.delete", redirect);
			}
			log.debug("The Organization has been deleted correctly");
		} else {
			log.error("The Organization does not exist");
		}
		return "redirect:/opendata/organizations/list";
	}

	@PostMapping(value = { "/authorization",
			"/authorization/update" }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<OpenDataAuthorizationDTO> createAuthorization(@RequestParam String role,
			@RequestParam String orgId, @RequestParam String user) {

		final String userToken = utils.getCurrentUserOauthToken();
		final OpenDataOrganization organization = organizationService.getOrganizationById(userToken, orgId);
		if ((!utils.isAdministrator()
				&& !openDataPermissions.hasPermissionsToManipulateOrganization(utils.getUserId(), organization))
				|| !openDataPermissions.isUserValidForRole(user, role)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			if (organization != null) {
				final OpenDataMember member = organizationService.manipulateOrgMembers(orgId, user, role, userToken);
				organizationService.updateOrganizationResourcesPermissions(orgId, userToken);
				if (member == null) {
					return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
				final OpenDataAuthorizationDTO authorizationDTO = new OpenDataAuthorizationDTO();
				authorizationDTO.setId(user + "/" + role);
				authorizationDTO.setRole(role);
				authorizationDTO.setUserId(user);

				return new ResponseEntity<>(authorizationDTO, HttpStatus.CREATED);
			} else {
				log.error("The Organization does not exist");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String orgId, @RequestParam String user) {

		final String userToken = utils.getCurrentUserOauthToken();
		final OpenDataOrganization organization = organizationService.getOrganizationById(userToken, orgId);
		if (!utils.isAdministrator()
				&& !openDataPermissions.hasPermissionsToManipulateOrganization(utils.getUserId(), organization)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			if (organization != null) {
				organizationService.manipulateOrgMembers(orgId, user, null, userToken);
				return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
			} else {
				log.error("The Organization does not exist");
				return new ResponseEntity<>("The Organization does not exist", HttpStatus.BAD_REQUEST);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

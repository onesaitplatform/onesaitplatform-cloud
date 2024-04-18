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
package com.minsait.onesait.platform.controlpanel.controller.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppRoleRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.app.dto.AppAssociatedCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.UserAppCreateDTO;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.project.ProjectDTO;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.helper.app.AppHelper;
import com.minsait.onesait.platform.controlpanel.services.keycloak.AdviceNotification.Type;
import com.minsait.onesait.platform.controlpanel.services.keycloak.KeycloakNotificator;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.security.ldap.ri.service.LdapUserService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/apps")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class AppController {

	@Autowired
	private AppService appService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private AppRoleRepository appRoleRepository;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private AppHelper appHelper;
	@Autowired(required = false)
	private KeycloakNotificator keycloakNotificator;

	private static final String NO_APP_CREATION = "Cannot create app";
	private static final String REDIRECT_APPS_CREATE = "redirect:/apps/create";
	private static final String REDIRECT_APPS_LIST = "redirect:/apps/list";
	private static final String REDIRECT_APPS_UPDATE = "redirect:/apps/update/";

	private static final String URL_APP_LIST = "/controlpanel/apps/list";
	private static final String APP_ID = "appId";
	
	@Autowired(required = false)
	private LdapUserService ldapUserService;
	@Value("${onesaitplatform.authentication.provider}")
	private String provider;
	@Value("${ldap.base}")
	private String ldapBaseDn;
	private static final String LDAP = "ldap";
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired 
	private HttpSession httpSession;

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String identification) {
		
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		final List<App> apps = appService.getAppsByUser(utils.getUserId(), identification);

		appHelper.populateAppList(model, apps);

		return "apps/list";

	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		appHelper.populateAppCreate(model);
		return "apps/create";
	}

	@PostMapping(value = { "/create" })
	public String createApp(Model model, @Valid AppCreateDTO app, BindingResult bindingResult,
			RedirectAttributes redirect) {

		try {

			appService.createApp(appHelper.dto2app(app));
			if (keycloakNotificator != null) {
				keycloakNotificator.notifyRealmToKeycloak(app.getIdentification(), Type.CREATE);
			}

		} catch (final AppServiceException | IOException e) {
			log.debug(NO_APP_CREATION);
			utils.addRedirectException(e, redirect);
			return REDIRECT_APPS_CREATE;
		}
		return REDIRECT_APPS_LIST;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	@Transactional
	public String update(Model model, @PathVariable("id") String id) {
		final AppList app = appService.getAppListById(id);

		if (app != null) {

			final User sessionUser = userService.getUser(utils.getUserId());
			if (null != app.getUser() && app.getUser().getUserId().equals(sessionUser.getUserId())
					|| sessionUser.isAdmin()) {


				appHelper.populateAppUpdate(model, app, sessionUser, ldapBaseDn, ldapActive());

				model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
						resourcesInUseService.isInUse(id, utils.getUserId()));
				resourcesInUseService.put(id, utils.getUserId());

				return "apps/create";

			} else {
				return REDIRECT_APPS_LIST;
			}
		} else {
			return REDIRECT_APPS_LIST;
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateApp(Model model, @PathVariable("id") String id, @Valid AppCreateDTO appDTO,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some app properties missing");
			utils.addRedirectMessage("app.validation.error", redirect);
			return REDIRECT_APPS_UPDATE + id;
		}

		try {

			final AppList app = appService.getAppListById(id);
			if (app != null) {
				final User sessionUser = userService.getUser(utils.getUserId());
				if (null != app.getUser() && app.getUser().getUserId().equals(sessionUser.getUserId())
						|| sessionUser.isAdmin()) {

					appDTO.setAppId(id);
					appService.updateApp(appDTO);
					if (keycloakNotificator != null) {
						keycloakNotificator.notifyRealmToKeycloak(appDTO.getIdentification(), Type.UPDATE);
					}
				} else {
					resourcesInUseService.removeByUser(id, utils.getUserId());
					return REDIRECT_APPS_LIST;
				}
			} else {
				resourcesInUseService.removeByUser(id, utils.getUserId());
				return REDIRECT_APPS_LIST;
			}

		} catch (final AppServiceException e) {
			log.debug("Cannot update app");
			utils.addRedirectMessage(e.getMessage(), redirect);
			return REDIRECT_APPS_UPDATE + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_APPS_LIST;
	}

	@GetMapping("/show/{id}")
	@Transactional
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		final AppList app = appService.getAppListById(id);

		if (app != null) {

			final User sessionUser = userService.getUser(utils.getUserId());
			if (null != app.getUser() && app.getUser().getUserId().equals(sessionUser.getUserId())
					|| sessionUser.isAdmin()) {
				appHelper.populateAppShow(model, app);

				return "apps/show";
			} else {
				return REDIRECT_APPS_LIST;
			}
		} else {
			return REDIRECT_APPS_LIST;
		}

	}

	@DeleteMapping("/{id}")
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final App app = appService.getById(id);
			if (app != null) {
				final User sessionUser = userService.getUser(utils.getUserId());
				if ((null != app.getUser() && app.getUser().getUserId().equals(sessionUser.getUserId())
						|| sessionUser.isAdmin()) && app.getProject() == null) {

					appService.deleteApp(app);
					if (keycloakNotificator != null) {
						keycloakNotificator.notifyRealmToKeycloak(app.getIdentification(), Type.DELETE);
					}
				} else {
					return URL_APP_LIST;
				}
			} else {
				return URL_APP_LIST;
			}

		} catch (final Exception e) {
			utils.addRedirectMessage("app.delete.error", redirect);
			return URL_APP_LIST;
		}

		return URL_APP_LIST;
	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UserAppCreateDTO> createAuthorization(@RequestParam String roleId, @RequestParam String appId,
			@RequestParam String userId) {
		try {

			final String appUserId = appService.createUserAccess(appId, userId, roleId);
			final UserAppCreateDTO appUserDTO = new UserAppCreateDTO();
			appUserDTO.setId(appUserId);
			appUserDTO.setRoleName(appRoleRepository.findById(roleId).orElse(null).getName());
			appUserDTO.setUser(userId);
			appUserDTO.setRoleId(roleId);

			return new ResponseEntity<>(appUserDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/ldap", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UserAppCreateDTO> createAuthorizationLdap(@RequestParam String roleId,
			@RequestParam String appId, @RequestParam String userId, @RequestParam String dn)
					throws GenericOPException {
		try {
			if (userService.getUser(userId) == null) {
				ldapUserService.createUser(userId, dn);
			}
			final String appUserId = appService.createUserAccess(appId, userId, roleId);
			final UserAppCreateDTO appUserDTO = new UserAppCreateDTO();
			appUserDTO.setId(appUserId);
			appUserDTO.setRoleName(appRoleRepository.findById(roleId).orElse(null).getName());
			appUserDTO.setUser(userId);
			appUserDTO.setRoleId(roleId);

			return new ResponseEntity<>(appUserDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		try {

			appService.deleteUserAccess(id);
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/association", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AppAssociatedCreateDTO> createAssociation(@RequestParam String fatherRoleId,
			@RequestParam String childRoleId) {
		try {

			final Map<String, String> result = appService.createAssociation(fatherRoleId, childRoleId);
			final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
			appAssociatedDTO.setId(result.get("fatherRoleName") + ':' + result.get("childRoleName"));
			appAssociatedDTO.setFatherAppId(result.get("fatherAppId"));
			appAssociatedDTO.setFatherRoleName(result.get("fatherRoleName"));
			appAssociatedDTO.setChildAppId(result.get("childAppId"));
			appAssociatedDTO.setChildRoleName(result.get("childRoleName"));

			return new ResponseEntity<>(appAssociatedDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/association/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAssociation(@RequestParam String fatherRoleName,
			@RequestParam String childRoleName, String fatherAppId, String childAppId) {

		try {
			appService.deleteAssociation(fatherRoleName, childRoleName, fatherAppId, childAppId);
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/getRoles", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Map<String, String>> getRolesByApp(@RequestParam String appId) {
		final App app = appService.getById(appId);
		final Map<String, String> roles = new HashMap<>();
		for (final AppRole role : app.getAppRoles()) {
			roles.put(role.getId(), role.getName());
		}
		return new ResponseEntity<>(roles, HttpStatus.CREATED);

	}

	@GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<String>> getUsers(@RequestParam("dn") String dn) {
		final List<User> users = ldapUserService.getAllUsers(dn);
		return new ResponseEntity<>(users.stream().map(User::getUserId).collect(Collectors.toList()), HttpStatus.OK);

	}

	@GetMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<String>> getGroups(@RequestParam("dn") String dn) {
		final List<String> groups = ldapUserService.getAllGroups(dn);
		return new ResponseEntity<>(groups, HttpStatus.OK);

	}

	@GetMapping(value = "/groups/{group}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<String>> getUsersInGroup(@RequestParam("dn") String dn,
			@PathVariable("group") String group) {
		final List<User> users = ldapUserService.getAllUsersFromGroup(dn, group);
		return new ResponseEntity<>(users.stream().map(User::getUserId).collect(Collectors.toList()), HttpStatus.OK);

	}

	@PostMapping("/project")
	public String createProject(Model model, @Valid ProjectDTO project, @RequestParam("appId") String appId,
			@RequestParam(value = "existingProject", required = false) String existingProject,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors() && !StringUtils.hasText(existingProject)) {

			return REDIRECT_APPS_UPDATE + appId;
		} else {
			final App realm = appService.getById(appId);
			if (StringUtils.hasText(project.getIdentification()) && StringUtils.hasText(project.getDescription())) {
				project.setUser(userService.getUser(utils.getUserId()));
				final Project p = projectService.createProject(project);
				p.getProjectResourceAccesses().clear();
				p.setApp(realm);

				project.setUser(userService.getUser(utils.getUserId()));
				realm.setProject(p);

			} else {
				final Project projectDB = projectService.getById(existingProject);
				realm.setProject(projectDB);
				projectDB.setApp(realm);
				projectService.updateProject(projectDB);
			}
			appService.updateApp(realm);
			if (keycloakNotificator != null) {
				keycloakNotificator.notifyRealmToKeycloak(realm.getIdentification(), Type.UPDATE);
			}
			return REDIRECT_APPS_UPDATE + appId;
		}

	}

	private boolean ldapActive() {
		return LDAP.equals(provider);
	}

}

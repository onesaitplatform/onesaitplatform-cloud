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
package com.minsait.onesait.platform.controlpanel.helper.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleListOauth;
import com.minsait.onesait.platform.config.model.AppUserListOauth;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.app.dto.AppAssociatedCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppDTO;
import com.minsait.onesait.platform.config.services.app.dto.RoleAppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.UserAppCreateDTO;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppHelper {

        @Value("${onesaitplatform.controlpanel.realms.max.users.app.assign.table:0}")
	private static final int MAX_USERS_IN_APP_TABLE = 50;

        @Value("${onesaitplatform.controlpanel.realms.max.users.combo:0}")
	private static final int MAX_USERS_COMBO_BOX = 100;

	@Autowired
	ApiRepository apiRepository;

	@Autowired
	private AppService appService;

	@Autowired
	UserService userService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	AppWebUtils utils;

	public void populateAppList(Model model, List<App> apps) {
		final List<AppDTO> appsDTO = new ArrayList<>();

		if (apps != null && !apps.isEmpty()) {
			for (final App app : apps) {
				final AppDTO appDTO = new AppDTO();
				appDTO.setId(app.getId());
				appDTO.setDateCreated(app.getCreatedAt());
				appDTO.setDateUpdated(app.getUpdatedAt());
				appDTO.setDescription(app.getDescription());
				appDTO.setIdentification(app.getIdentification());
				appDTO.setUser(app.getUser().getUserId());
				if (app.getAppRoles() != null && !app.getAppRoles().isEmpty()) {
					final List<String> list = new ArrayList<>();
					for (final AppRole appRole : app.getAppRoles()) {
						list.add(appRole.getName());
					}
					appDTO.setRoles(StringUtils.arrayToDelimitedString(list.toArray(), ", "));
				}
				appsDTO.add(appDTO);
			}
		}

		model.addAttribute("apps", appsDTO);

	}

	private AppRole dto2appRole(RoleAppCreateDTO role, App app) {
		final AppRole appRole = new AppRole();
		appRole.setApp(app);
		appRole.setDescription(role.getDescription());
		appRole.setName(role.getName());

		return appRole;
	}

	public App dto2app(AppCreateDTO app) throws IOException {
		final App napp = new App();
		napp.setIdentification(app.getIdentification());
		napp.setUser(userService.getUser(utils.getUserId()));
		napp.setSecret(app.getSecret());
		napp.setDescription(app.getDescription());
		napp.setTokenValiditySeconds(app.getTokenValiditySeconds());

		final ObjectMapper mapper = new ObjectMapper();
		final List<RoleAppCreateDTO> roles = new ArrayList<>(
				mapper.readValue(app.getRoles(), new TypeReference<List<RoleAppCreateDTO>>() {
				}));
		roles.stream().map(x -> dto2appRole(x, napp)).forEach(x -> napp.getAppRoles().add(x));
		return napp;
	}

	public void populateAppCreate(Model model) {
		model.addAttribute("app", new AppCreateDTO());
		model.addAttribute("users", new ArrayList<>());
		model.addAttribute("authorizations", new ArrayList<>());
	}

	public void populateAppShow(Model model, AppList app) {

		final AppCreateDTO appDTO = new AppCreateDTO();
		appDTO.setId(app.getId());
		appDTO.setIdentification(app.getIdentification());
		appDTO.setSecret(app.getSecret());
		appDTO.setDescription(app.getDescription());
		appDTO.setTokenValiditySeconds(app.getTokenValiditySeconds());

		final List<AppAssociatedCreateDTO> associations = new ArrayList<>();
		final List<UserAppCreateDTO> usersList = new ArrayList<>();
		final List<AppRoleListOauth> roles = appService.getAppRolesListOauth(app.getIdentification());
		copyRoleList(appDTO, app, roles, usersList);
		// OK

		if (!CollectionUtils.isEmpty(app.getChildApps())) {
			for (final AppRoleListOauth role : roles) {
				if (!CollectionUtils.isEmpty(role.getChildRoles())) {
					for (final AppRoleListOauth childRole : role.getChildRoles()) {
						final AppAssociatedCreateDTO associatedAppDTO = new AppAssociatedCreateDTO();
						associatedAppDTO.setId(role.getName() + ':' + childRole.getName());
						associatedAppDTO.setFatherAppId(app.getIdentification());
						associatedAppDTO.setFatherRoleName(role.getName());
						associatedAppDTO.setChildAppId(childRole.getApp().getIdentification());
						associatedAppDTO.setChildRoleName(childRole.getName());
						associations.add(associatedAppDTO);
					}
				}
			}
		}

		mapRolesAndUsersToJson(app, roles, appDTO);

		model.addAttribute("app", appDTO);
		model.addAttribute("roles", app.getAppRoles());
		model.addAttribute("authorizations", usersList);
		model.addAttribute("associations", associations);

	}

	public List<UserAppCreateDTO> getAuthorizations(String appIdentification, String filter) {
		final List<AppUserListOauth> users;
		if (!StringUtils.hasText(filter)) {
			users = appService.getAppUsersByApp(appIdentification);
		} else {
			users = appService.getAppUsersByAppAndUserIdLike(appIdentification, "%" + filter + "%");
		}
		return users.stream().map(u -> {
			final UserAppCreateDTO userAppDTO = new UserAppCreateDTO();
			userAppDTO.setId(String.valueOf(u.getId()));
			userAppDTO.setRoleName(u.getRole().getName());
			userAppDTO.setUser(u.getUser().getUserId());
			userAppDTO.setUserName(u.getUser().getFullName());
			return userAppDTO;
		}).collect(Collectors.toList());
	}

	private void copyRoleList(AppCreateDTO appDTO, AppList app, List<AppRoleListOauth> roles,
			List<UserAppCreateDTO> usersList) {
		final List<String> rolesList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(roles)) {
			for (final AppRoleListOauth appRole : roles) {
				rolesList.add(appRole.getName());
			}
			appDTO.setRoles(StringUtils.arrayToDelimitedString(rolesList.toArray(), ", "));
		}
		final long usersInApp = appService.countUsersInApp(app.getIdentification());
		if ((usersInApp > 0 && usersInApp < MAX_USERS_IN_APP_TABLE) || MAX_USERS_IN_APP_TABLE==0) {
			final List<AppUserListOauth> users = appService.getAppUsersByApp(app.getIdentification());
			for (final AppUserListOauth appUser : users) {
				final UserAppCreateDTO userAppDTO = new UserAppCreateDTO();
				userAppDTO.setId(String.valueOf(appUser.getId()));
				userAppDTO.setRoleName(appUser.getRole().getName());
				userAppDTO.setUser(appUser.getUser().getUserId());
				userAppDTO.setUserName(appUser.getUser().getFullName());
				usersList.add(userAppDTO);
			}
			appDTO.setUsers(StringUtils.arrayToDelimitedString(usersList.toArray(), ", "));

		}
	}

	private void mapRolesAndUsersToJson(AppList app, List<AppRoleListOauth> roles, AppCreateDTO appDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		final ArrayNode arrayNodeUser = mapper.createArrayNode();

		for (final AppRoleListOauth appRole : roles) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("name", appRole.getName());
			on.put("description", appRole.getDescription());
			arrayNode.add(on);
		}
		final long usersInApp = appService.countUsersInApp(app.getIdentification());
		if (usersInApp > 0 && usersInApp < MAX_USERS_IN_APP_TABLE) {
			final List<AppUserListOauth> users = appService.getAppUsersByApp(app.getIdentification());
			if (!CollectionUtils.isEmpty(users)) {
				for (final AppUserListOauth appUser : users) {
					final ObjectNode onUser = mapper.createObjectNode();
					onUser.put("user", appUser.getUser().getUserId());
					onUser.put("roleName", appUser.getRole().getName());
					arrayNodeUser.add(onUser);
				}
			}
		}

		try {
			appDTO.setRoles(mapper.writer().writeValueAsString(arrayNode));
			appDTO.setUsers(mapper.writer().writeValueAsString(arrayNodeUser));
		} catch (final JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

	public void populateAppUpdate(Model model, AppList app, User sessionUser, String ldapBaseDn, boolean ldapActive) {
		final AppCreateDTO appDTO = new AppCreateDTO();
		appDTO.setId(app.getId());
		appDTO.setIdentification(app.getIdentification());
		appDTO.setSecret(app.getSecret());
		appDTO.setDescription(app.getDescription());
		appDTO.setTokenValiditySeconds(app.getTokenValiditySeconds());

		final List<AppAssociatedCreateDTO> appsAssociatedList = new ArrayList<>();
		final List<UserAppCreateDTO> usersList = new ArrayList<>();
		final List<AppRoleListOauth> roles = appService.getAppRolesListOauth(app.getIdentification());
		copyRoleList(appDTO, app, roles, usersList);

		// Si la app es padre:
		if (app.getChildApps() != null && !app.getChildApps().isEmpty()) {
			asociateChildRoles(appsAssociatedList, app, roles);
		}

		// Si la app es hija:
		asociateFatherRoles(appsAssociatedList, app, roles);

		mapRolesAndUsersToJson(app, roles, appDTO);
		List<User> users;
		if (userService.countUsers() < MAX_USERS_COMBO_BOX || MAX_USERS_COMBO_BOX==0) {
			users = userService.getAllUsers();
		} else {
			users = new ArrayList<>();
		}
		List<AppList> appsToChoose = new ArrayList<>();

		// Si nuestra app es hija de otra no podremos asociarle una app
		for (final AppList appToChoose : appService.getAllAppsList()) {
			if (appToChoose.getChildApps() != null && appToChoose.getChildApps().contains(app)) {
				appsToChoose.clear();
				break;
			} else {
				// Quitamos del 'select' de apps hijas: la propia app y las apps que ya tienen
				// apps hijas
				appsToChoose = appService.getAppsByUserList(sessionUser.getUserId(), null).stream()
						.filter(appToSelect -> (!appToSelect.getIdentification().equals(app.getIdentification())
								&& (appToSelect.getChildApps() == null || appToSelect.getChildApps().isEmpty())))
						.collect(Collectors.toList());
			}
		}
		if (app.getProject() != null) {
			model.addAttribute("project", app.getProject());
		} else {
			model.addAttribute("project", new Project());
		}

		model.addAttribute("app", appDTO);
		model.addAttribute("roles", roles);
		model.addAttribute("users", this.obfuscateUsers(users));
		model.addAttribute("appsChild", appsToChoose);
		model.addAttribute("authorizations", usersList);
		model.addAttribute("associations", appsAssociatedList);
		model.addAttribute("ldapEnabled", ldapActive);
		model.addAttribute("baseDn", ldapBaseDn);
		model.addAttribute("projectTypes", Project.ProjectType.values());
		model.addAttribute("authorizationsCount", appService.countUsersInApp(app.getIdentification()));
		model.addAttribute("projects",
				projectService.getAllProjects().stream()
						.filter(p -> p.getApp() == null && (CollectionUtils.isEmpty(p.getUsers())
								|| (p.getUsers().contains(p.getUser()) && p.getUsers().size() == 1)))
						.collect(Collectors.toList()));

	}

	private List<User> obfuscateUsers(List<User> users) {
		List<User> obfuscatedUsers = new ArrayList<User>();
		users.forEach(user -> {
			User obfuscatedUser = new User();
			obfuscatedUser.setUserId(user.getUserId());
			obfuscatedUser.setFullName(user.getFullName());
			obfuscatedUser.setProjects(user.getProjects());
			obfuscatedUsers.add(obfuscatedUser);
		});

		return obfuscatedUsers;
	}

	private void asociateFatherRoles(List<AppAssociatedCreateDTO> appsAssociatedList, AppList app,
			List<AppRoleListOauth> roles) {
		for (final AppRoleListOauth appRole : roles) {
			for (final AppRoleListOauth role : appService.getAllRolesList()) {
				if (role.getChildRoles() != null && role.getChildRoles().contains(appRole)) {
					final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
					appAssociatedDTO.setId(role.getName() + ':' + appRole.getName());
					appAssociatedDTO.setFatherAppId(role.getApp().getIdentification());
					appAssociatedDTO.setChildAppId(app.getIdentification());
					appAssociatedDTO.setFatherRoleName(role.getName());
					appAssociatedDTO.setChildRoleName(appRole.getName());
					appsAssociatedList.add(appAssociatedDTO);
				}
			}
		}
	}

	private void asociateChildRoles(List<AppAssociatedCreateDTO> appsAssociatedList, AppList app,
			List<AppRoleListOauth> roles) {
		for (final AppRoleListOauth fatherAppRole : roles) {
			if (fatherAppRole.getChildRoles() != null && !fatherAppRole.getChildRoles().isEmpty()) {
				for (final AppRoleListOauth childAppRole : fatherAppRole.getChildRoles()) {
					final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
					appAssociatedDTO.setId(fatherAppRole.getName() + ':' + childAppRole.getName());
					appAssociatedDTO.setFatherAppId(app.getIdentification());
					appAssociatedDTO.setChildAppId(childAppRole.getApp().getIdentification());
					appAssociatedDTO.setFatherRoleName(fatherAppRole.getName());
					appAssociatedDTO.setChildRoleName(childAppRole.getName());
					appsAssociatedList.add(appAssociatedDTO);
				}
			}
		}
	}
}

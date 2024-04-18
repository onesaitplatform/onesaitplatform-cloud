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
package com.minsait.onesait.platform.controlpanel.helper.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
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
				appDTO.setAppId(app.getAppId());
				appDTO.setDateCreated(app.getCreatedAt());
				appDTO.setDescription(app.getDescription());
				appDTO.setName(app.getName());

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
		napp.setAppId(app.getAppId());
		napp.setName(app.getName());
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

	public void populateAppShow(Model model, App app) {

		final AppCreateDTO appDTO = new AppCreateDTO();
		appDTO.setAppId(app.getAppId());
		appDTO.setName(app.getName());
		appDTO.setSecret(app.getSecret());
		appDTO.setDescription(app.getDescription());
		appDTO.setTokenValiditySeconds(app.getTokenValiditySeconds());

		
		final List<AppAssociatedCreateDTO> associations = new ArrayList<>();
		final List<UserAppCreateDTO> usersList = new ArrayList<>();
		
		if (app.getAppRoles() != null && !app.getAppRoles().isEmpty()) {
			copyRoleList(appDTO, app, usersList);
		}

		if (app.getChildApps() != null && !app.getChildApps().isEmpty()) {
			for (final AppRole role : app.getAppRoles()) {
				if (role.getChildRoles() != null && !role.getChildRoles().isEmpty()) {
					for (final AppRole childRole : role.getChildRoles()) {
						final AppAssociatedCreateDTO associatedAppDTO = new AppAssociatedCreateDTO();
						associatedAppDTO.setId(role.getName() + ':' + childRole.getName());
						associatedAppDTO.setFatherAppId(app.getAppId());
						associatedAppDTO.setFatherRoleName(role.getName());
						associatedAppDTO.setChildAppId(childRole.getApp().getAppId());
						associatedAppDTO.setChildRoleName(childRole.getName());
						associations.add(associatedAppDTO);
					}
				}
			}
		}

		mapRolesAndUsersToJson(app, appDTO);

		model.addAttribute("app", appDTO);
		model.addAttribute("roles", app.getAppRoles());
		model.addAttribute("authorizations", usersList);
		model.addAttribute("associations", associations);
		
	}
	
	private void copyRoleList(AppCreateDTO appDTO, App app, List<UserAppCreateDTO> usersList) {
		final List<String> rolesList = new ArrayList<>();
		for (final AppRole appRole : app.getAppRoles()) {
			rolesList.add(appRole.getName());
			if (appRole.getAppUsers() != null && !appRole.getAppUsers().isEmpty()) {
				for (final AppUser appUser : appRole.getAppUsers()) {
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
		appDTO.setRoles(StringUtils.arrayToDelimitedString(rolesList.toArray(), ", "));
	}

	private void mapRolesAndUsersToJson(App app, AppCreateDTO appDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		final ArrayNode arrayNodeUser = mapper.createArrayNode();

		for (final AppRole appRole : app.getAppRoles()) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("name", appRole.getName());
			on.put("description", appRole.getDescription());
			arrayNode.add(on);
			if (appRole.getAppUsers() != null && !appRole.getAppUsers().isEmpty())
				for (final AppUser appUser : appRole.getAppUsers()) {
					final ObjectNode onUser = mapper.createObjectNode();
					onUser.put("user", appUser.getUser().getUserId());
					onUser.put("roleName", appUser.getRole().getName());
					arrayNodeUser.add(onUser);
				}
		}

		try {
			appDTO.setRoles(mapper.writer().writeValueAsString(arrayNode));
			appDTO.setUsers(mapper.writer().writeValueAsString(arrayNodeUser));
		} catch (final JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

	public void populateAppUpdate(Model model, App app, User sessionUser, String ldapBaseDn, boolean ldapActive) {
		final AppCreateDTO appDTO = new AppCreateDTO();
		appDTO.setAppId(app.getAppId());
		appDTO.setName(app.getName());
		appDTO.setSecret(app.getSecret());
		appDTO.setDescription(app.getDescription());
		appDTO.setTokenValiditySeconds(app.getTokenValiditySeconds());

		final List<AppAssociatedCreateDTO> appsAssociatedList = new ArrayList<>();
		final List<UserAppCreateDTO> usersList = new ArrayList<>();
		
		if (app.getAppRoles() != null && !app.getAppRoles().isEmpty()) {
			copyRoleList(appDTO, app, usersList);
		}

		// Si la app es padre:
		if (app.getChildApps() != null && !app.getChildApps().isEmpty()) {
			asociateChildRoles(appsAssociatedList, app);
		}

		// Si la app es hija:
		asociateFatherRoles(appsAssociatedList, app);

		mapRolesAndUsersToJson(app, appDTO);
		final List<User> users = userService.getAllUsers();
		List<App> appsToChoose = new ArrayList<>();

		// Si nuestra app es hija de otra no podremos asociarle una app
		for (final App appToChoose : appService.getAllApps()) {
			if (appToChoose.getChildApps() != null && appToChoose.getChildApps().contains(app)) {
				appsToChoose.clear();
				break;
			} else {
				// Quitamos del 'select' de apps hijas: la propia app y las apps que ya tienen
				// apps hijas
				appsToChoose = appService.getAppsByUser(sessionUser.getUserId(), null).stream()
						.filter(appToSelect -> ((!appToSelect.getAppId().equals(app.getAppId()))
								&& (appToSelect.getChildApps() == null
										|| appToSelect.getChildApps().isEmpty())))
						.collect(Collectors.toList());
			}
		}
		if (app.getProject() != null) {
			model.addAttribute("project", app.getProject());
		} else {
			model.addAttribute("project", new Project());
			model.addAttribute("app", appDTO);
			model.addAttribute("roles", app.getAppRoles());
			model.addAttribute("users", users);
			model.addAttribute("appsChild", appsToChoose);
			model.addAttribute("authorizations", usersList);
			model.addAttribute("associations", appsAssociatedList);
			model.addAttribute("ldapEnabled", ldapActive);
			model.addAttribute("baseDn", ldapBaseDn);
			model.addAttribute("projectTypes", Project.ProjectType.values());
			model.addAttribute("projects",
				projectService.getAllProjects().stream()
						.filter(p -> p.getApp() == null && CollectionUtils.isEmpty(p.getUsers()))
						.collect(Collectors.toList()));
		}
	}

	private void asociateFatherRoles(List<AppAssociatedCreateDTO> appsAssociatedList, App app) {
		for (final AppRole appRole : app.getAppRoles()) {
			for (final AppRole role : appService.getAllRoles()) {
				if (role.getChildRoles() != null && role.getChildRoles().contains(appRole)) {
					final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
					appAssociatedDTO.setId(role.getName() + ':' + appRole.getName());
					appAssociatedDTO.setFatherAppId(role.getApp().getAppId());
					appAssociatedDTO.setChildAppId(app.getAppId());
					appAssociatedDTO.setFatherRoleName(role.getName());
					appAssociatedDTO.setChildRoleName(appRole.getName());
					appsAssociatedList.add(appAssociatedDTO);
				}
			}
		}
	}

	private void asociateChildRoles(List<AppAssociatedCreateDTO> appsAssociatedList, App app) {
		for (final AppRole fatherAppRole : app.getAppRoles()) {
			if (fatherAppRole.getChildRoles() != null && !fatherAppRole.getChildRoles().isEmpty()) {
				for (final AppRole childAppRole : fatherAppRole.getChildRoles()) {
					final AppAssociatedCreateDTO appAssociatedDTO = new AppAssociatedCreateDTO();
					appAssociatedDTO.setId(fatherAppRole.getName() + ':' + childAppRole.getName());
					appAssociatedDTO.setFatherAppId(app.getAppId());
					appAssociatedDTO.setChildAppId(childAppRole.getApp().getAppId());
					appAssociatedDTO.setFatherRoleName(fatherAppRole.getName());
					appAssociatedDTO.setChildRoleName(childAppRole.getName());
					appsAssociatedList.add(appAssociatedDTO);
				}
			}
		}
	}
}

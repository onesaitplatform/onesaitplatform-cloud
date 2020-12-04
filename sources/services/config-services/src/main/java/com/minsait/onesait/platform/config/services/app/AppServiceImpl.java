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
package com.minsait.onesait.platform.config.services.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppChild;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleChild;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserList;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.AppRoleRepository;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.services.app.dto.AppAssociatedCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.Realm;
import com.minsait.onesait.platform.config.services.app.dto.UserAppCreateDTO;
import com.minsait.onesait.platform.config.services.entity.cast.EntitiesCast;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppServiceImpl implements AppService {

	@Autowired
	private AppRepository appRepository;
	@Autowired
	private AppRoleRepository appRoleRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AppUserRepository appUserRepository;
	@Autowired
	private ProjectService projectService;

	private static final String EXCEPTION_REACHED = "Exception reached ";

	@Override
	public List<App> getAllApps() {
		final List<App> appCastList = new ArrayList<>();
		for (final AppList appList : appRepository.findAllList()) {
			appCastList.add(EntitiesCast.castAppList(appList, false));
		}
		return appCastList;
	}

	@Override
	public List<AppRole> getAllRoles() {
		final List<AppList> allApps = appRepository.findAllList();
		final List<AppRole> allRoles = new ArrayList<>();
		for (final AppList app : allApps) {
			for (final AppRoleList appRole : app.getAppRoles()) {
				allRoles.add(EntitiesCast.castAppRoleList(appRole));
			}
		}
		return allRoles;
	}

	@Transactional
	@Override
	public List<App> getAppsByUser(String sessionUserId, String identification) {
		final List<App> apps = new LinkedList<>();
		List<AppList> appsList;
		final User sessionUser = userService.getUser(sessionUserId);

		identification = identification == null ? "" : identification;

		if (userService.isUserAdministrator(sessionUser)) {
			appsList = appRepository.findByIdentificationLike(identification);
		} else {
			appsList = appRepository.findByUserANDIdentification(sessionUser, identification);
		}
		for (final AppList al : appsList) {
			apps.add(EntitiesCast.castAppList(al, false));
		}
		return apps;

	}

	@Override
	public AppRole getByRoleNameAndApp(String roleName, App app) {
		for (final AppRole appRole : app.getAppRoles()) {
			if (appRole.getName().equals(roleName)) {
				return appRole;
			}
		}
		return null;
	}

	@Override
	public void createApp(App app) {
		if (appRepository.findByIdentificationEquals(app.getIdentification()) != null) {
			throw new AppServiceException("App with identification: " + app.getIdentification() + " exists");
		}
		appRepository.save(app);

	}

	@Override
	@Transactional
	public App getById(String id) {
		return appRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void updateApp(AppCreateDTO appDTO) {
		final App app = appRepository.findById(appDTO.getId()).orElse(new App());
		app.setSecret(appDTO.getSecret());
		app.setTokenValiditySeconds(appDTO.getTokenValiditySeconds());
		app.setIdentification(appDTO.getIdentification());
		app.setDescription(appDTO.getDescription());

		app.getChildApps().clear();

		updateAppRoles(app, appDTO);

		appRepository.save(app);

	}

	private void updateAppRoles(App app, AppCreateDTO appDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final HashSet<AppRole> roles = new HashSet<>(
					mapper.readValue(appDTO.getRoles(), new TypeReference<List<AppRole>>() {
					}));
			app.getAppRoles().removeIf(
					r -> !roles.stream().map(AppRole::getName).collect(Collectors.toList()).contains(r.getName()));
			app.getAppRoles().addAll(roles.stream().filter(r -> !app.getAppRoles().stream().map(AppRole::getName)
					.collect(Collectors.toList()).contains(r.getName())).collect(Collectors.toSet()));
			for (final AppRole role : app.getAppRoles()) {
				if (role.getApp() == null) {
					role.setApp(app);
					role.setAppUsers(new HashSet<>());
					appRoleRepository.save(role);
				}
			}
			final Set<UserAppCreateDTO> users = new HashSet<>(
					mapper.readValue(appDTO.getUsers(), new TypeReference<List<UserAppCreateDTO>>() {
					}));
			final List<AppAssociatedCreateDTO> associations = new ArrayList<>(
					mapper.readValue(appDTO.getAssociations(), new TypeReference<List<AppAssociatedCreateDTO>>() {
					}));

			if (!users.isEmpty()) {
				for (final UserAppCreateDTO user : users) {
					if (user.getUser() != null && user.getRoleName() != null && !user.getUser().equals("")
							&& !user.getRoleName().equals("")) {
						final User usuario = new User();
						usuario.setUserId(user.getUser());
						final AppRole rol = findRole(app, user.getRoleName());
						if (!rol.getAppUsers().stream().map(au -> au.getUser().getUserId()).collect(Collectors.toList())
								.contains(usuario.getUserId())) {
							final AppUser appUser = new AppUser();
							appUser.setUser(usuario);
							appUser.setRole(rol);
							if (rol != null && rol.getAppUsers() != null) {
								rol.getAppUsers().add(appUser);
							}
						}
					}
				}
			}

			updateAppAssociations(associations, app);

		} catch (final Exception e) {
			log.error(EXCEPTION_REACHED + e.getMessage(), e);
		}
	}

	private void updateAppAssociations(List<AppAssociatedCreateDTO> associations, App app) {
		if (!associations.isEmpty()) {
			for (final AppAssociatedCreateDTO association : associations) {
				if (association.getFatherAppId().equals(app.getIdentification())) {
					updateFatherAssociation(association, app);
				} else if (association.getChildAppId().equals(app.getIdentification())) {
					updateChildAssociation(association, app);
				}
			}
		}
	}

	private void updateFatherAssociation(AppAssociatedCreateDTO association, App app) {
		AppRole fatherRole = null;
		final Optional<AppRole> fatherRoleOptional = app.getAppRoles().stream()
				.filter(rolPadre -> rolPadre.getName().equals(association.getFatherRoleName())).findFirst();

		if (fatherRoleOptional.isPresent()) {
			fatherRole = fatherRoleOptional.get();
		}

		final App childApp = appRepository.findByIdentification(association.getChildAppId());

		final AppChild castedChild = EntitiesCast.castAppChild(childApp);
		AppRoleChild childRole = null;

		final Optional<AppRole> childRoleOptional = childApp.getAppRoles().stream()
				.filter(rolHijo -> rolHijo.getName().equals(association.getChildRoleName())).findFirst();

		if (childRoleOptional.isPresent()) {
			childRole = EntitiesCast.castAppRoleChild(childRoleOptional.get());
		}

		if (fatherRole != null) {
			if (fatherRole.getChildRoles() == null) {
				fatherRole.setChildRoles(new HashSet<AppRoleChild>());
			}
			fatherRole.getChildRoles().add(childRole);
		}

		if (app.getChildApps() == null) {
			app.setChildApps(new HashSet<AppChild>());
		}
		app.getChildApps().add(castedChild);
		appRepository.save(app);
	}

	private void updateChildAssociation(AppAssociatedCreateDTO association, App app) {
		final App fatherApp = appRepository.findByIdentification(association.getFatherAppId());

		AppRole fatherRole = null;

		final Optional<AppRole> fatherRoleOptional = fatherApp.getAppRoles().stream()
				.filter(rolPadre -> rolPadre.getName().equals(association.getFatherRoleName())).findFirst();

		if (fatherRoleOptional.isPresent()) {
			fatherRole = fatherRoleOptional.get();
		}

		AppRoleChild childRole = null;
		final Optional<AppRole> childRoleOptional = app.getAppRoles().stream()
				.filter(rolHijo -> rolHijo.getName().equals(association.getChildRoleName())).findFirst();

		if (childRoleOptional.isPresent()) {
			childRole = EntitiesCast.castAppRoleChild(childRoleOptional.get());
		}

		if (fatherRole != null) {
			if (fatherRole.getChildRoles() == null) {
				fatherRole.setChildRoles(new HashSet<AppRoleChild>());
			}
			fatherRole.getChildRoles().add(childRole);
		}

		if (fatherApp.getChildApps() == null) {
			fatherApp.setChildApps(new HashSet<AppChild>());
		}
		final AppChild castedChild = EntitiesCast.castAppChild(app);
		fatherApp.getChildApps().add(castedChild);

		appRepository.save(fatherApp);

	}

	private AppRole findRole(App app, String roleName) {
		final Optional<AppRole> opt = app.getAppRoles().stream().filter(x -> x.getName().equals(roleName)).findFirst();
		if (opt.isPresent()) {
			return opt.get();
		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public void deleteApp(App app) {

		if (app.getChildApps() != null && !app.getChildApps().isEmpty()) {
			deleteFatherApp(app);
		} else {
			for (final App application : getAllApps()) {
				if (application.getChildApps() != null && application.getChildApps().contains(app)) {
					deleteChildApp(application, app);
				}
			}
		}
		appRepository.delete(app);
	}

	private void deleteChildApp(App application, App app) {
		application.getChildApps().remove(app);
		for (final AppRole appRole : application.getAppRoles()) {
			if (appRole.getChildRoles() != null) {
				for (final AppRoleChild childRole : appRole.getChildRoles()) {
					if (childRole.getApp().equals(app)) {
						appRole.getChildRoles().remove(childRole);
					}
				}
			}
		}
		appRepository.save(application);
	}

	private void deleteFatherApp(App app) {
		for (final AppRole appRole : app.getAppRoles()) {
			appRole.getChildRoles().clear();
		}
		app.getChildApps().clear();
	}

	@Override
	public String createUserAccess(String appId, String userId, String roleId) {

		final Optional<App> opt = appRepository.findById(appId);
		final Optional<AppRole> optRole = appRoleRepository.findById(roleId);
		if (opt.isPresent() && optRole.isPresent()) {
			final App app = opt.get();
			final User sessionUser = userService.getUser(userId);

			final AppRole appRole = optRole.get();

			AppUser appUser = new AppUser();
			appUser.setRole(appRole);
			appUser.setUser(sessionUser);
			appUser = appUserRepository.save(appUser);
			return appUser.getId();

		} else {
			throw new AppServiceException("Problem creating the authorization");
		}

	}

	@Override
	public void deleteUserAccess(String appUserId) {
		try {
			appUserRepository.deleteByQuery(appUserId);

		} catch (final Exception e) {
			log.error("Error deleting user from Realm", e);
		}
	}

	@Override
	public Set<AppUser> findUsersByRole(AppRole role) {
		final Optional<AppRole> appRole = appRoleRepository.findById(role.getId());
		if (appRole.isPresent()) {
			return appRole.get().getAppUsers();
		} else {
			return new HashSet<>();
		}
	}

	@Override
	@Transactional
	public Map<String, String> createAssociation(String fatherRoleId, String childRoleId) {
		final Map<String, String> result = new HashMap<>();

		final Optional<AppRole> fatherRole = appRoleRepository.findById(fatherRoleId);
		final Optional<AppRole> childRole = appRoleRepository.findById(childRoleId);
		if (fatherRole.isPresent() && childRole.isPresent()) {
			final App fatherApp = fatherRole.get().getApp();
			final App childApp = childRole.get().getApp();

			final AppChild castedChild = EntitiesCast.castAppChild(childApp);
			fatherApp.getChildApps().add(castedChild);
			fatherRole.get().getChildRoles().add(EntitiesCast.castAppRoleChild(childRole.get()));

			result.put("fatherAppId", fatherApp.getIdentification());
			result.put("fatherRoleName", fatherRole.get().getName());
			result.put("childAppId", childApp.getIdentification());
			result.put("childRoleName", childRole.get().getName());

			appRepository.save(fatherApp);
			return result;
		} else {
			throw new AppServiceException("Wrong roles, either father role or child role does not exist");
		}
	}

	@Override
	@Transactional
	public void createAssociation(String fatherRoleName, String childRoleName, String fatherAppId, String childAppId) {
		final App fatherApp = getAppByIdentification(fatherAppId);
		final AppRole fatherRole = getByRoleNameAndApp(fatherRoleName, fatherApp);
		final App childApp = getAppByIdentification(childAppId);
		final AppRole childRole = getByRoleNameAndApp(childRoleName, childApp);

		final AppChild castedChild = EntitiesCast.castAppChild(childApp);
		fatherApp.getChildApps().add(castedChild);
		fatherRole.getChildRoles().add(EntitiesCast.castAppRoleChild(childRole));

		appRepository.save(fatherApp);

	}

	@Override
	@Transactional
	public void deleteAssociation(String fatherRoleName, String childRoleName, String fatherAppId, String childAppId) {

		final App fatherApp = getAppByIdentification(fatherAppId);
		final AppRole fatherRole = getByRoleNameAndApp(fatherRoleName, fatherApp);
		final App childApp = getAppByIdentification(childAppId);
		final AppRole childRole = getByRoleNameAndApp(childRoleName, childApp);

		if (fatherRole.getChildRoles().contains(childRole)) {
			fatherRole.getChildRoles().remove(childRole);
			if (fatherApp.getProject() != null) {
				final Project project = fatherApp.getProject();
				project.getProjectResourceAccesses()
						.removeIf(pra -> pra.getAppRole() != null && pra.getAppRole().equals(childRole));
				projectService.updateProject(project);
			}
		}
		appRoleRepository.save(fatherRole);

		// Borramos la asociacion de apps solamente si no quedan roles asociados entre
		// ellas
		final List<AppRole> coincidentes = new ArrayList<>();
		for (final AppRole rolPadre : fatherApp.getAppRoles()) {
			if (rolPadre.getChildRoles() != null && !rolPadre.getChildRoles().isEmpty()) {
				coincidentes.add(rolPadre);
			}
		}

		if (coincidentes.isEmpty() && fatherApp.getChildApps().contains(childApp)) {
			fatherApp.getChildApps().remove(childApp);
		}

		appRepository.save(fatherApp);
	}

	@Override
	public void updateApp(App app) {
		appRepository.save(app);

	}

	@Override
	@Transactional
	public void deleteRole(AppRole role) {

		getAllRoles().forEach(r -> {
			if (r.getChildRoles().contains(role)) {
				r.getChildRoles().remove(role);
				appRoleRepository.save(r);
				if (role.getApp().getChildApps().contains(r.getApp())) {
					r.getApp().getChildApps().remove(role.getApp());
					appRepository.save(r.getApp());

				}
			}
		});
		role.getApp().getAppRoles().remove(role);
		role.setApp(null);
		appRoleRepository.delete(role);
	}

	@Override
	public AppRole findRole(String roleId) {
		return appRoleRepository.findById(roleId).orElse(null);
	}

	@Override
	public App getAppByIdentification(String identification) {
		return appRepository.findByIdentification(identification);
	}

	@Override
	@Transactional
	public Realm getRealmByAppIdentification(String appId) {
		final Optional<App> opt = appRepository.findById(appId);
		if (opt.isPresent()) {
			final App app = opt.get();
			final List<AppRole> allRoles = getAllRoles();
			return new Realm(appUserRepository, app, allRoles);
		} else {
			throw new AppServiceException("No realm found");
		}
	}

	@Override
	public boolean isUserInApp(String userId, String realmId) {
		final List<AppUserList> listUser = appUserRepository.findAppUserListByUserAndIdentification(userId, realmId);
		return !(listUser == null || listUser.isEmpty());
	}
}

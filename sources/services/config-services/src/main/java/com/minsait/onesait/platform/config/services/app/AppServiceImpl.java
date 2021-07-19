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
import com.minsait.onesait.platform.config.model.AppListOauth;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleChild;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppRoleListOauth;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserListOauth;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppListOauthRepository;
import com.minsait.onesait.platform.config.repository.AppListRepository;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.AppRoleListOauthRepository;
import com.minsait.onesait.platform.config.repository.AppRoleRepository;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.services.app.dto.AppAssociatedCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.AppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.Realm;
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
	private AppListRepository appListRepository;
	@Autowired
	private AppRoleRepository appRoleRepository;
	@Autowired
	private AppRoleListOauthRepository appRoleListOauthRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AppUserRepository appUserRepository;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private AppListOauthRepository appListOauthRepository;

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
	public AppRoleList getByRoleNameAndAppList(String roleName, AppList app) {
		return appRoleRepository.findAppRoleListByAppIdentificationAndRoleName(app.getIdentification(), roleName)
				.stream().findFirst().orElse(null);
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
	public void updateApp(AppCreateDTO appDTO) throws AppServiceException {
		final AppListOauth app = appListOauthRepository.findById(appDTO.getAppId()).orElse(null);
		if (app != null) {
			app.setSecret(appDTO.getSecret());
			app.setTokenValiditySeconds(appDTO.getTokenValiditySeconds());
			app.setIdentification(appDTO.getIdentification());
			app.setDescription(appDTO.getDescription());


			//TO-DO review this logic
			app.getChildApps().clear();
			updateAppRoles(app, appDTO);
			appListOauthRepository.save(app);
		}
	}
	private void updateAppRoles(AppListOauth app, AppCreateDTO appDTO) throws AppServiceException {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final HashSet<AppRoleListOauth> roles = new HashSet<>(
					mapper.readValue(appDTO.getRoles(), new TypeReference<List<AppRoleListOauth>>() {
					}));
			final List<String> dtoRoles = roles.stream().map(AppRoleListOauth::getName).collect(Collectors.toList());

			for (final AppRoleListOauth r : app.getAppRoles()) {
				if (!dtoRoles.contains(r.getName())) {
					// mirar si el rol eliminado tiene una asociación en otro realm
					AppRoleListOauth deletedRole = new AppRoleListOauth();
					final Optional<AppRoleListOauth> dRole = appRoleListOauthRepository.findById(r.getId());
					if (dRole.isPresent()) {
						deletedRole = dRole.get();
					}
					final List<AppRole> roleList = getAllRoles();
					final List<String> roleIdsList = roleList.stream().map(AppRole::getId).collect(Collectors.toList());
					for (final String id : roleIdsList) {
						AppRoleListOauth fatherRole = new AppRoleListOauth();
						final Optional<AppRoleListOauth> fRole = appRoleListOauthRepository.findById(id);
						if (fRole.isPresent()) {
							fatherRole = fRole.get();
						}
						final Set<AppRoleListOauth> childRoles = fatherRole.getChildRoles();
						for (final AppRoleListOauth child : childRoles) {
							if (child.getId().equals(deletedRole.getId())) {
								throw new AppServiceException(
										"The deleted role is a child of the " + fatherRole.getApp().getIdentification()
										+ " realm. Remove the association before removing the role.");
							}
						}
					}
				}
			}

			app.getAppRoles().removeIf(
					r -> !roles.stream().map(AppRoleListOauth::getName).collect(Collectors.toList()).contains(r.getName()));
			app.getAppRoles().addAll(roles.stream().filter(r -> !app.getAppRoles().stream().map(AppRoleListOauth::getName)
					.collect(Collectors.toList()).contains(r.getName())).collect(Collectors.toSet()));
			for (final AppRoleListOauth role : app.getAppRoles()) {
				if (role.getApp() == null) {
					role.setApp(app);
					role.setAppUsers(new HashSet<>());
					appRoleListOauthRepository.save(role);
				}
			}

			final List<AppAssociatedCreateDTO> associations = new ArrayList<>(
					mapper.readValue(appDTO.getAssociations(), new TypeReference<List<AppAssociatedCreateDTO>>() {
					}));

			updateAppAssociations(associations, app);
		}catch (final Exception e) {
			// TODO: handle exception
		}
	}

	private void updateAppRoles(App app, AppCreateDTO appDTO) throws AppServiceException {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final HashSet<AppRole> roles = new HashSet<>(
					mapper.readValue(appDTO.getRoles(), new TypeReference<List<AppRole>>() {
					}));
			final List<String> dtoRoles = roles.stream().map(AppRole::getName).collect(Collectors.toList());

			for (final AppRole r : app.getAppRoles()) {
				if (!dtoRoles.contains(r.getName())) {
					// mirar si el rol eliminado tiene una asociación en otro realm
					AppRole deletedRole = new AppRole();
					final Optional<AppRole> dRole = appRoleRepository.findById(r.getId());
					if (dRole.isPresent()) {
						deletedRole = dRole.get();
					}
					final List<AppRole> roleList = getAllRoles();
					final List<String> roleIdsList = roleList.stream().map(AppRole::getId).collect(Collectors.toList());
					for (final String id : roleIdsList) {
						AppRole fatherRole = new AppRole();
						final Optional<AppRole> fRole = appRoleRepository.findById(id);
						if (fRole.isPresent()) {
							fatherRole = fRole.get();
						}
						final Set<AppRoleChild> childRoles = fatherRole.getChildRoles();
						for (final AppRoleChild child : childRoles) {
							if (child.getId().equals(deletedRole.getId())) {
								throw new AppServiceException(
										"The deleted role is a child of the " + fatherRole.getApp().getIdentification()
										+ " realm. Remove the association before removing the role.");
							}
						}
					}
				}
			}

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

			final List<AppAssociatedCreateDTO> associations = new ArrayList<>(
					mapper.readValue(appDTO.getAssociations(), new TypeReference<List<AppAssociatedCreateDTO>>() {
					}));

			updateAppAssociations(associations, app);

		} catch (final Exception e) {
			log.error(EXCEPTION_REACHED + e.getMessage(), e);
			throw new AppServiceException(e.getMessage());
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
	private void updateAppAssociations(List<AppAssociatedCreateDTO> associations, AppListOauth app) {
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

	private void updateFatherAssociation(AppAssociatedCreateDTO association, AppListOauth app) {
		AppRoleListOauth fatherRole = null;
		final Optional<AppRoleListOauth> fatherRoleOptional = app.getAppRoles().stream()
				.filter(rolPadre -> rolPadre.getName().equals(association.getFatherRoleName())).findFirst();

		if (fatherRoleOptional.isPresent()) {
			fatherRole = fatherRoleOptional.get();
		}

		final AppListOauth childApp = appListOauthRepository.findByIdentification(association.getChildAppId());
		AppRoleListOauth childRole = null;

		final Optional<AppRoleListOauth> childRoleOptional = childApp.getAppRoles().stream()
				.filter(rolHijo -> rolHijo.getName().equals(association.getChildRoleName())).findFirst();

		if (childRoleOptional.isPresent()) {
			childRole = childRoleOptional.get();
		}

		if (fatherRole != null) {
			if (fatherRole.getChildRoles() == null) {
				fatherRole.setChildRoles(new HashSet<AppRoleListOauth>());
			}
			fatherRole.getChildRoles().add(childRole);
		}

		if (app.getChildApps() == null) {
			app.setChildApps(new HashSet<AppListOauth>());
		}
		app.getChildApps().add(childApp);
		appListOauthRepository.save(app);
	}

	private void updateChildAssociation(AppAssociatedCreateDTO association, AppListOauth app) {
		final AppListOauth fatherApp = appListOauthRepository.findByIdentification(association.getFatherAppId());

		AppRoleListOauth fatherRole = null;

		final Optional<AppRoleListOauth> fatherRoleOptional = fatherApp.getAppRoles().stream()
				.filter(rolPadre -> rolPadre.getName().equals(association.getFatherRoleName())).findFirst();

		if (fatherRoleOptional.isPresent()) {
			fatherRole = fatherRoleOptional.get();
		}

		AppRoleListOauth childRole = null;
		final Optional<AppRoleListOauth> childRoleOptional = app.getAppRoles().stream()
				.filter(rolHijo -> rolHijo.getName().equals(association.getChildRoleName())).findFirst();

		if (childRoleOptional.isPresent()) {
			childRole = childRoleOptional.get();
		}

		if (fatherRole != null) {
			if (fatherRole.getChildRoles() == null) {
				fatherRole.setChildRoles(new HashSet<AppRoleListOauth>());
			}
			fatherRole.getChildRoles().add(childRole);
		}

		if (fatherApp.getChildApps() == null) {
			fatherApp.setChildApps(new HashSet<AppListOauth>());
		}

		fatherApp.getChildApps().add(app);

		appListOauthRepository.save(fatherApp);

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

		final Optional<AppRoleListOauth> appRoleDb = appRoleListOauthRepository.findById(roleId);
		if (appRoleDb.isPresent()) {
			final User sessionUser = new User();
			sessionUser.setUserId(userId);
			final AppRole role = new AppRole();
			role.setId(appRoleDb.get().getId());
			AppUser appUser = new AppUser();
			appUser.setRole(role);
			appUser.setUser(sessionUser);
			appUser = appUserRepository.save(appUser);
			return appUser.getId();

		} else {
			throw new AppServiceException("Problem creating the authorization");
		}

	}

	@Override
	@Deprecated
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
		final List<AppUserListOauth> listUser = appUserRepository.findAppUserListByUser(userId);
		return listUser.stream().anyMatch(au -> au.getRole().getApp().getIdentification().equals(realmId));
	}

	@Override
	public AppList getAppListByIdentification(String identification) {
		return appRepository.findAppListByIdentification(identification);
	}

	@Override
	public void removeAuthorizations(String userId, String appIdentification) {
		final List<AppUserListOauth> appUsers = appUserRepository.findAppUserByUserIdAndApp(userId,  appIdentification);
		appUsers.forEach(u -> appUserRepository.deleteAppUserById(u.getId()));
	}

	@Override
	public void updateApp(AppList appDTO) {
		appListRepository.save(appDTO);

	}

	@Override
	public AppList getAppListById(String id) {
		return appListRepository.findById(id).orElse(null);
	}

	@Override
	public List<AppUserListOauth> getAppUsersByUserIdAndApp(String userId, String appIdentification) {
		return appUserRepository.findAppUserListByUserAndIdentification(userId, appIdentification);
	}

	@Override
	public List<AppUserListOauth> getAppUsersByApp(String appIdentification) {
		return appUserRepository.findAppUserListByAppIdentification(appIdentification);
	}

	@Override
	public List<AppUserListOauth> getAppUsersByAppAndUserIdLike(String appIdentification, String userIdLike){
		return appUserRepository.findAppUserListByAppIdentificationAndUserIdLike(appIdentification, userIdLike);
	}

	@Override
	public AppRoleListOauth getByRoleNameAndAppListOauth(String roleName, AppList app) {
		final List<AppRoleListOauth> roles = appRoleRepository.findAppRoleListOauthByAppIdentificationAndRoleName(app.getIdentification(), roleName);
		return roles.stream().findFirst().orElse(null);
	}

	@Override
	public void deleteUserAccess(String userId, String roleName, String appIdentification) {
		final List<AppUserListOauth> appUsers = appUserRepository.findAppUserByUserIdAndRoleAndApp(userId, roleName, appIdentification);
		appUsers.forEach(u -> appUserRepository.deleteAppUserById(u.getId()));
	}

	@Override
	public List<AppRoleListOauth> getAppRolesListOauth(String appIdentification) {
		return appRoleRepository.findAppRoleListOauthByAppIdentification(appIdentification);
	}

	@Override
	public long countUsersInApp(String appIdentification) {
		return appUserRepository.countAppUserListByAppIdentification(appIdentification);
	}

	@Override
	public List<AppRoleListOauth> getAllRolesList() {
		return appRoleRepository.findAllRolesList();
	}

	@Override
	public List<AppList> getAllAppsList() {
		return appRepository.findAllList();
	}

	@Override
	public List<AppList> getAppsByUserList(String sessionUserId, String identification) {
		List<AppList> appsList;
		final User sessionUser = userService.getUser(sessionUserId);

		identification = identification == null ? "" : identification;

		if (userService.isUserAdministrator(sessionUser)) {
			appsList = appRepository.findByIdentificationLike(identification);
		} else {
			appsList = appRepository.findByUserANDIdentification(sessionUser, identification);
		}
		return appsList;
	}

}

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
package com.minsait.onesait.platform.config.services.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.entity.cast.EntitiesCast;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.exceptions.ProjectServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;

import avro.shaded.com.google.common.collect.Lists;

@Service
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProjectResourceAccessRepository projectResourceAccessRepository;
	@Autowired
	private AppService appService;
	@Autowired
	private UserService userService;
	@Autowired
	private WebProjectService webProjectService;
	@Autowired(required = false)
	private MetricsManager metricsManager;

	@Override
	public Project createProject(ProjectDTO project) {

		if (!projectRepository.findByIdentification(project.getIdentification()).isEmpty()) {
			throw new ProjectServiceException(
					"Project with identification: " + project.getIdentification() + " exists");
		}

		Project p = new Project();
		p.setIdentification(project.getIdentification());
		p.setDescription(project.getDescription());
		p.setType(project.getType());
		p.setUser(project.getUser());
		p = projectRepository.save(p);

		if (null != metricsManager) {
			metricsManager.logControlPanelProjectsCreation(project.getUser().getUserId(), "OK");
		}
		return p;
	}

	@Override
	@Transactional
	public void deleteProject(String projectId) {
		projectRepository.findById(projectId).ifPresent(project -> {
			final App app = project.getApp();
			if (app != null) {
				project.setApp(null);
				app.setProject(null);
				appService.updateApp(app);
			}
			if (!project.getUsers().isEmpty()) {
				removeUsersFromProject(project);
				project.getUsers().clear();
			}
			projectRepository.delete(project);
		});

	}

	@Override
	public List<Project> getAllProjects() {
		final List<Project> lproject = new LinkedList<>();
		for (final ProjectList plist : projectRepository.findAllForList()) {
			lproject.add(EntitiesCast.castProjectList(plist, true));
		}
		return lproject;
	}

	@Override
	public Project getById(String id) {
		return projectRepository.findById(id).orElse(null);
	}

	@Override
	public ProjectList getByIdForList(String id) {
		final List<ProjectList> projects = projectRepository.findByIdForList(id);
		if (!projects.isEmpty()) {
			return projects.get(0);
		} else {
			return null;
		}
	}

	@Override
	public Project getByName(String identification) {
		final List<Project> projects = projectRepository.findByIdentification(identification);
		if (!projects.isEmpty()) {
			return projects.get(0);
		} else {
			return null;
		}
	}

	@Override
	public ProjectList getByNameForList(String identification) {
		final List<ProjectList> projects = projectRepository.findByIdentificationForList(identification);
		if (!projects.isEmpty()) {
			return projects.get(0);
		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public void updateProject(Project project) {
		projectRepository.save(project);
	}

	@Override
	public void updateWithParameters(ProjectDTO project) {
		projectRepository.findById(project.getId()).ifPresent(projectDb -> {
			projectDb.setDescription(project.getDescription());
			projectDb.setType(project.getType());
			projectRepository.save(projectDb);
		});

	}

	@Override
	@Transactional
	public List<ProjectUserDTO> getProjectMembers(String projectId) {
		final Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null) {
			final List<?> users = getProjectUsers(project);
			return getMembersDTO(users);
		} else {
			return new ArrayList<>();
		}
	}

	private List<?> getProjectUsers(Project project) {
		if (project.getApp() != null) {
			final List<AppUser> users = new ArrayList<>();
			final App app = project.getApp();
			if (!CollectionUtils.isEmpty(app.getChildApps())) {
				app.getChildApps().forEach(a -> a.getAppRoles().forEach(r -> r.getAppUsers().forEach(au -> {
					if (!users.contains(au)) {
						users.add(au);
					}
				})

				));
			}
			app.getAppRoles().forEach(r -> r.getAppUsers().forEach(au -> {
				if (!users.contains(au)) {
					users.add(au);
				}
			}));
			return users;
		} else {
			return Lists.newArrayList(project.getUsers());
		}
	}

	private List<ProjectUserDTO> getMembersDTO(List<?> members) {
		return members.stream().map(o -> {
			if (o instanceof AppUser) {
				return ProjectUserDTO.builder().userId(((AppUser) o).getUser().getUserId())
						.roleName(((AppUser) o).getRole().getName()).fullName(((AppUser) o).getUser().getFullName())
						.realm(((AppUser) o).getRole().getApp().getIdentification()).build();
			} else if (o instanceof User) {
				return ProjectUserDTO.builder().userId(((User) o).getUserId()).roleName(((User) o).getRole().getId())
						.fullName(((User) o).getFullName()).build();
			} else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public List<App> getAvailableRealmsForUser(String userId) {
		return appService.getAppsByUser(userId, null).stream().filter(a -> a.getProject() == null)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public List<Project> getProjectsForUser(String userId) {

		final List<ProjectList> projectsList = projectRepository.findAllForList();
		final List<Project> projects = new LinkedList<>();

		final User user = userService.getUserNoCache(userId);
		if (user.isAdmin()) {
			for (final ProjectList pl : projectsList) {
				projects.add(EntitiesCast.castProjectList(pl, true));
			}
			return projects;
		} else {
			for (final ProjectList pl : projectsList) {
				projects.add(EntitiesCast.castProjectList(pl, false));
			}
		}
		final List<Project> filteredProjects = projects.stream()
				.filter(p -> p.getUser().equals(user) || p.getUsers().contains(user)).collect(Collectors.toList());
		projects.forEach(p -> addAppRelatedProjects(p, filteredProjects, user));
		return filteredProjects;
	}

	private void addAppRelatedProjects(Project p, List<Project> filteredProjects, User user) {
		if (p.getApp() != null) {
			final App app = p.getApp();
			if (!CollectionUtils.isEmpty(app.getChildApps())) {
				app.getChildApps().forEach(a -> a.getAppRoles().forEach(r -> {
					final List<User> users = r.getAppUsers().stream().map(AppUser::getUser)
							.collect(Collectors.toList());
					if (users.contains(user) && !filteredProjects.contains(p)) {
						filteredProjects.add(p);
					}

				}));
			}
			app.getAppRoles().forEach(r -> {
				final List<User> users = r.getAppUsers().stream().map(AppUser::getUser).collect(Collectors.toList());
				if (users.contains(user) && !filteredProjects.contains(p)) {
					filteredProjects.add(p);
				}
			});
		}
	}

	@Override
	@Transactional
	public void addUserToProject(String userId, String projectId) {
		final User user = userService.getUserNoCache(userId);
		final Project project = projectRepository.findById(projectId).orElse(null);
		if (user != null && project != null && project.getApp() == null && !project.getUsers().contains(user)) {
			project.getUsers().add(user);
			user.getProjects().add(project);
			projectRepository.save(project);
			userService.saveExistingUser(user);
			userService.evictFromCache(user);
		}
	}

	@Override
	@Transactional
	public void removeUserFromProject(String userId, String projectId) {
		final User user = userService.getUserNoCache(userId);
		final Project project = projectRepository.findById(projectId).orElse(null);
		if (user != null && project != null && project.getApp() == null) {
			project.getUsers().remove(user);
			user.getProjects().removeIf(p -> p.getId().equals(project.getId()));
			project.getProjectResourceAccesses().removeIf(pra -> pra.getUser().equals(user));
			projectRepository.save(project);
			userService.saveExistingUser(user);
			userService.evictFromCache(user);
		}
	}

	private void removeUsersFromProject(Project project) {
		if (project != null && project.getApp() == null && !project.getUsers().isEmpty()) {
			project.getUsers().stream().forEach(u -> {
				u.getProjects().remove(project);
				userService.saveExistingUser(u);
			});
		}
	}

	@Override
	@Transactional
	public void setRealm(String realmId, String projectId) {
		final App app = appService.getAppByIdentification(realmId);
		if (app.getProject() != null) {
			throw new AppServiceException("Realm is assigned to project " + app.getProject().getIdentification());
		}
		if (app != null) {
			projectRepository.findById(projectId).ifPresent(project -> {
				if (!project.getUsers().isEmpty()) {
					removeUsersFromProject(project);
					project.getUsers().clear();
				}
				project.setApp(app);
				app.setProject(project);
				projectRepository.save(project);
				appService.updateApp(app);
			});

		}

	}

	@Override
	@Transactional
	public void unsetRealm(String realmId, String projectId) {
		final App app = appService.getAppByIdentification(realmId);
		if (app != null) {
			projectRepository.findById(projectId).ifPresent(project -> {
				project.setApp(null);
				app.setProject(null);
				project.getProjectResourceAccesses().clear();
				projectRepository.save(project);
				appService.updateApp(app);
			});

		}

	}

	@Override
	public void addWebProject(String webProjectId, String projectId, String userId) {
		final Project project = projectRepository.findById(projectId).orElse(null);
		final WebProjectDTO webProject = webProjectService.getWebProjectById(webProjectId, userId);
		if (webProject != null && project != null) {
			project.setWebProject(WebProjectDTO.convert(webProject, userService.getUser(userId)));
			projectRepository.save(project);
		}

	}

	@Override
	public void removeWebProject(String projectId) {
		final Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null) {
			project.setWebProject(null);
			projectRepository.save(project);
		}

	}

	@Override
	@Transactional
	public boolean isUserInProject(String userId, String projectId) {
		final User user = userService.getUserNoCache(userId);
		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			return false;
		}
		final Project project = opt.get();
		if (project.getUser().equals(user)) {

			return true;
		} else if (project.getApp() != null) {
			return project.getApp().getAppRoles().stream()
					.map(ar -> ar.getAppUsers().stream().map(au -> au.getUser().equals(user))
							.filter(Boolean::booleanValue).findFirst().orElse(false))
					.filter(Boolean::booleanValue).findFirst().orElse(false);
		} else {
			return project.getUsers().contains(user);
		}
	}

	@Transactional
	@Override
	public Set<User> getUsersInProject(String projectId) {
		final Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null) {

			return project.getUsers().stream().collect(Collectors.toSet());
		} else {
			return new HashSet<>();
		}
	}

	@Override
	public Map<AppRole, Set<AppUser>> getAllRoleUsersInProject(Set<AppRole> projectRoles) {
		final Map<AppRole, Set<AppUser>> projectUserRoles = new HashMap<>();
		for (final AppRole rol : projectRoles) {
			projectUserRoles.put(rol, rol.getAppUsers());
		}
		return projectUserRoles;
	}

	@Override
	public Set<ProjectResourceAccess> getResourcesAccessesForUser(String projectId, String userId) {
		final User user = userService.getUserNoCache(userId);
		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			throw new ProjectServiceException("Project not found");
		}
		final Project project = opt.get();
		if (project.getApp() != null) {
			final Set<AppRole> roles = project
					.getApp().getAppRoles().stream().filter(ar -> null != ar.getAppUsers().stream()
							.map(AppUser::getUser).filter(u -> u.equals(user)).findFirst().orElse(null))
					.collect(Collectors.toSet());
			return project.getProjectResourceAccesses().stream().filter(pra -> roles.contains(pra.getAppRole()))
					.collect(Collectors.toSet());
		} else {
			return project.getProjectResourceAccesses().stream().filter(pra -> pra.getUser().equals(user))
					.collect(Collectors.toSet());
		}
	}

	@Override
	public Set<ProjectResourceAccess> getResourcesAccessesForAppRole(String projectId, String name) {
		final AppRole appRole = appService.findRole(name);
		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			throw new ProjectServiceException("Project not found");
		}
		final Project project = opt.get();
		if (project.getApp() != null) {
			return project.getProjectResourceAccesses().stream()
					.filter(pra -> pra.getAppRole().getName().equals(appRole.getName())).collect(Collectors.toSet());
		} else {
			return new HashSet<>();
		}
	}

	@Override
	public Set<ProjectResourceAccess> getAllResourcesAccesses(String projectId) {
		final Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null) {
			return project.getProjectResourceAccesses().stream().collect(Collectors.toSet());

		} else

		{
			return new HashSet<>();
		}
	}

	@Override
	public Set<OPResource> getResourcesForProjectAndUser(String projectId, String userId) {
		final User user = userService.getUserNoCache(userId);
		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			throw new ProjectServiceException("Project not found");
		}
		final Project project = opt.get();
		final String role_user = user.getRole().getId();
		if (user.equals(project.getUser()) || role_user.equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return project.getProjectResourceAccesses().stream().map(ProjectResourceAccess::getResource)
					.collect(Collectors.toSet());
		} else {
			return getResourcesAccessesForUser(projectId, userId).stream().map(ProjectResourceAccess::getResource)
					.collect(Collectors.toSet());
		}
	}

	@Override
	@Transactional
	public Set<OPResource> getResourcesForUser(String userId) {
		final Set<OPResource> resources = new HashSet<>();
		final List<ProjectResourceAccess> accesses = projectResourceAccessRepository.findByUserIdAccess(userId);
		accesses.stream().forEach(a -> resources.add(a.getResource()));
		return resources;

	}

	@Override
	@Transactional
	public <T> Set<T> getResourcesForUserOfType(String userId, Class<T> clazz) {
		return getResourcesForUser(userId).stream().filter(clazz::isInstance).map(clazz::cast)
				.collect(Collectors.toSet());
	}

	@Override
	@Transactional
	public Set<AppRole> getProjectRoles(String projectId) {
		final Set<AppRole> roles = new HashSet<>();
		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			throw new ProjectServiceException("Project not found");
		}
		final Project project = opt.get();
		roles.addAll(project.getApp().getAppRoles());
		project.getApp().getAppRoles().forEach(a -> roles.addAll(EntitiesCast.castAppRoles(a.getChildRoles())));
		return roles;
	}

	@Override
	public boolean isUserAuthorized(String projectId, String userId) {
		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			throw new ProjectServiceException("Project not found");
		}
		final Project project = opt.get();

		final User user = userService.getUserNoCache(userId);
		return project.getUser().equals(user) || user.isAdmin();
	}

	@Override
	@Transactional
	public void deleteResourceFromProjects(String resourceId) {
		final List<Project> projects = getProjectsWithResource(resourceId);
		projects.forEach(
				p -> p.getProjectResourceAccesses().removeIf(pra -> pra.getResource().getId().equals(resourceId)));
		projectRepository.saveAll(projects);

	}

	@Override
	public List<Project> getProjectsWithResource(String resourceId) {
		return projectResourceAccessRepository.findProjectsWithResourceId(resourceId);
	}

	@Transactional
	@Override
	public boolean isUserInProjectWithoutOwner(String userId, String projectId) {
		final User user = userService.getUserNoCache(userId);

		final Optional<Project> opt = projectRepository.findById(projectId);
		if (!opt.isPresent()) {
			throw new ProjectServiceException("Project not found");
		}
		final Project project = opt.get();
		if (project.getApp() != null) {

			return project.getApp().getAppRoles().stream()
					.map(ar -> ar.getAppUsers().stream().map(au -> au.getUser().equals(user))
							.filter(Boolean::booleanValue).findFirst().orElse(false))
					.filter(Boolean::booleanValue).findFirst().orElse(false);
		} else {
			return project.getUsers().contains(user);
		}
	}

}

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
package com.minsait.onesait.platform.controlpanel.rest.management.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectDTO;
import com.minsait.onesait.platform.config.services.project.ProjectResourceRestRealmDTO;
import com.minsait.onesait.platform.config.services.project.ProjectResourceRestUserDTO;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "Projects management", tags = { "Projects management service" })
@RestController
@RequestMapping("api/projects")
public class ProjectManagementController {

	@Autowired
	private ProjectService projectService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private AppService appService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;

	private static final String ERROR_USER_NOT_FOUND = "Invalid input data: User not found";
	private static final String ERROR_REALM_NOT_FOUND = "Invalid input data: Realm not found";
	private static final String ERROR_USER_IN_PROJECT = "Invalid input data: User is already in the project";
	private static final String ERROR_USER_NOT_IN_PROJECT = "Invalid input data: User is not in the project";
	private static final String ERROR_REALM_IN_PROJECT = "Invalid input data: There is realm already asigned to project";
	private static final String ERROR_REALM_NOT_IN_PROJECT = "Invalid input data: There is no realm asigned to project";
	private static final String ERROR_USERACCESS_ROL = "Not possible to give access to administrator: role not allowed";
	private static final String ERROR_RESOURCES_IN_PROJECT = "There are resources still attached to users in the project";

	@ApiOperation(value = "List projects")
	@GetMapping(value = "/")
	public ResponseEntity<?> listProjects(@RequestHeader("Authorization") String authorization) {
		try {

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			JSONArray responseInfo = new JSONArray();

			List<Project> projects = projectService.getProjectsForUser(userId);

			Iterator<Project> i1 = projects.iterator();
			while (i1.hasNext()) {
				Project currentProject = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("name", currentProject.getIdentification());
				jsonAccess.put("description", currentProject.getDescription());
				jsonAccess.put("type", currentProject.getType());
				if (currentProject.getApp() != null)
					jsonAccess.put("realm", currentProject.getApp().getIdentification());
				else
					jsonAccess.put("realm", "");
				responseInfo.put(jsonAccess);
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Get project by identification")
	@GetMapping(value = "/{project}")
	public ResponseEntity<?> getByIdentification(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {
		try {

			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			JSONArray responseInfo = new JSONArray();

			if (!projectService.isUserAuthorized(project.getId(), userId))
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);

			Project currentProject = projectService.getByName(projectId);
			Set<ProjectResourceAccess> userAccesses = projectService.getAllResourcesAccesses(project.getId());

			JSONArray resources = resourceAccessestoJson(userAccesses, project);
			JSONArray users = projectUserstoJson(project);

			JSONObject jsonAccess = new JSONObject();
			jsonAccess.put("name", currentProject.getIdentification());
			jsonAccess.put("description", currentProject.getDescription());
			jsonAccess.put("type", currentProject.getType());
			if (currentProject.getApp() != null)
				jsonAccess.put("realm", currentProject.getApp().getIdentification());
			else
				jsonAccess.put("realm", "");
			jsonAccess.put("resources", resources);
			jsonAccess.put("users", users);

			responseInfo.put(jsonAccess);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Deprecated
	@ApiOperation(value = "Create project")
	@PostMapping(value = "/create")
	public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRestDTO projectDTO,
			@RequestHeader("Authorization") String authorization) {

		try {

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (userService.isUserAdministrator(user)) {
				return new ResponseEntity<>(String.format("User with role ADMINISTRATOR cannot create projects"),
						HttpStatus.UNAUTHORIZED);
			}

			final Project existingProject = projectService.getByName(projectDTO.getIdentification());

			if (existingProject != null) {
				return new ResponseEntity<>(
						String.format("Project with id %s already exists", projectDTO.getIdentification()),
						HttpStatus.BAD_REQUEST);
			}

			if (projectDTO.getIdentification().equals("") || projectDTO.getDescription().equals("")) {
				return new ResponseEntity<>(String.format("Missing input data"), HttpStatus.BAD_REQUEST);
			}

			ProjectDTO projDTO = new ProjectDTO();
			projDTO.setDescription(projectDTO.getDescription());
			projDTO.setIdentification(projectDTO.getIdentification());
			projDTO.setType(projectDTO.getType());
			projDTO.setUser(user);
			projectService.createProject(projDTO);

			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Create project")
	@PostMapping(value = "/")
	public ResponseEntity<?> newProject(@Valid @RequestBody ProjectRestDTO projectDTO,
			@RequestHeader("Authorization") String authorization) {

		try {

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (userService.isUserAdministrator(user)) {
				return new ResponseEntity<>(String.format("User with role ADMINISTRATOR cannot create projects"),
						HttpStatus.UNAUTHORIZED);
			}

			final Project existingProject = projectService.getByName(projectDTO.getIdentification());

			if (existingProject != null) {
				return new ResponseEntity<>(
						String.format("Project with id %s already exists", projectDTO.getIdentification()),
						HttpStatus.BAD_REQUEST);
			}

			if (projectDTO.getIdentification().equals("") || projectDTO.getDescription().equals("")) {
				return new ResponseEntity<>(String.format("Missing input data"), HttpStatus.BAD_REQUEST);
			}

			ProjectDTO projDTO = new ProjectDTO();
			projDTO.setDescription(projectDTO.getDescription());
			projDTO.setIdentification(projectDTO.getIdentification());
			projDTO.setType(projectDTO.getType());
			projDTO.setUser(user);
			projectService.createProject(projDTO);

			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Delete project")
	@DeleteMapping(value = "/{project}")
	public ResponseEntity<?> deleteProject(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			projectService.deleteProject(project.getId());

			return new ResponseEntity<>("OK", HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Get all resources from a project")
	@GetMapping(value = "/{project}/getAllResources")
	public ResponseEntity<?> getAllResources(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			Set<ProjectResourceAccess> userAccesses = projectService.getAllResourcesAccesses(project.getId());

			JSONArray responseInfo = resourceAccessestoJson(userAccesses, project);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Get all resources from a project")
	@GetMapping(value = "/{project}/resources")
	public ResponseEntity<?> allResources(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			Set<ProjectResourceAccess> userAccesses = projectService.getAllResourcesAccesses(project.getId());

			JSONArray responseInfo = resourceAccessestoJson(userAccesses, project);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Get all users from a project")
	@GetMapping(value = "/{project}/getAllUsers")
	public ResponseEntity<?> getAllUsers(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			JSONArray responseInfo = projectUserstoJson(project);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Get all users from a project")
	@GetMapping(value = "/{project}/users")
	public ResponseEntity<?> allUsers(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			JSONArray responseInfo = projectUserstoJson(project);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Get resources from project and user")
	@GetMapping(value = "/{project}/getResources/{user}")
	public ResponseEntity<?> getResourcesForProjectAndUser(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@ApiParam(value = "User Id", required = true) @PathVariable("user") String userId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String loggedUserId = utils.getUserId();
			final User loggedUser = userService.getUser(loggedUserId);
			if (loggedUser == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", loggedUserId),
						HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!(loggedUserId.equals(userId) || projectService.isUserAuthorized(project.getId(), loggedUserId))) {
				return new ResponseEntity<>(String.format("User is not authorized in project", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (userService.isUserAdministrator(user)) {
				return new ResponseEntity<>(
						String.format("User with role administrator cannot have resources in project", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (!projectService.isUserInProject(userId, project.getId())) {
				return new ResponseEntity<>(String.format("User with id %s is not in project", userId),
						HttpStatus.UNAUTHORIZED);
			}

			JSONArray responseInfo = new JSONArray();

			Set<ProjectResourceAccess> userAccesses = projectService.getResourcesAccessesForUser(project.getId(),
					userId);

			Iterator<ProjectResourceAccess> i1 = userAccesses.iterator();
			while (i1.hasNext()) {
				ProjectResourceAccess currentUserAccess = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("resource", currentUserAccess.getResource().getIdentification());
				jsonAccess.put("resourceType",
						currentUserAccess.getResource().getClass().getSimpleName().toUpperCase());
				if (project.getApp() == null)
					jsonAccess.put("userId", currentUserAccess.getUser().getUserId());
				else {
					jsonAccess.put("realm", currentUserAccess.getAppRole().getApp().getIdentification());
					jsonAccess.put("role", currentUserAccess.getAppRole().getName());
				}
				jsonAccess.put("accessType", currentUserAccess.getAccess().name());
				responseInfo.put(jsonAccess);
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Get resources from project and user")
	@GetMapping(value = "/{project}/resources/{user}")
	public ResponseEntity<?> resourcesFromProjectAndUser(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@ApiParam(value = "User Id", required = true) @PathVariable("user") String userId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String loggedUserId = utils.getUserId();
			final User loggedUser = userService.getUser(loggedUserId);
			if (loggedUser == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", loggedUserId),
						HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!(loggedUserId.equals(userId) || projectService.isUserAuthorized(project.getId(), loggedUserId))) {
				return new ResponseEntity<>(String.format("User is not authorized in project", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (userService.isUserAdministrator(user)) {
				return new ResponseEntity<>(
						String.format("User with role administrator cannot have resources in project", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (!projectService.isUserInProject(userId, project.getId())) {
				return new ResponseEntity<>(String.format("User with id %s is not in project", userId),
						HttpStatus.UNAUTHORIZED);
			}

			JSONArray responseInfo = new JSONArray();

			Set<ProjectResourceAccess> userAccesses = projectService.getResourcesAccessesForUser(project.getId(),
					userId);

			Iterator<ProjectResourceAccess> i1 = userAccesses.iterator();
			while (i1.hasNext()) {
				ProjectResourceAccess currentUserAccess = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("resource", currentUserAccess.getResource().getIdentification());
				jsonAccess.put("resourceType",
						currentUserAccess.getResource().getClass().getSimpleName().toUpperCase());
				if (project.getApp() == null)
					jsonAccess.put("userId", currentUserAccess.getUser().getUserId());
				else {
					jsonAccess.put("realm", currentUserAccess.getAppRole().getApp().getIdentification());
					jsonAccess.put("role", currentUserAccess.getAppRole().getName());
				}
				jsonAccess.put("accessType", currentUserAccess.getAccess().name());
				responseInfo.put(jsonAccess);
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Get resources from project and appRole")
	@GetMapping(value = "/{project}/getResourcesRole/{appRole}")
	public ResponseEntity<?> getResourcesForProjectAndAppRole(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@ApiParam(value = "AppRole Id", required = true) @PathVariable("appRole") String appRoleName,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User loggedUser = userService.getUser(userId);

			if (loggedUser == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (project.getApp() == null) {
				return new ResponseEntity<>(String.format("Project has no realm associated"), HttpStatus.BAD_REQUEST);
			}

			final App app = project.getApp();
			final AppRole appRole = appService.getByRoleNameAndApp(appRoleName, app);
			if (appRole == null) {
				return new ResponseEntity<>(String.format("Role not found with id %s", appRoleName),
						HttpStatus.BAD_REQUEST);
			}

			JSONArray responseInfo = new JSONArray();

			Set<ProjectResourceAccess> appRoleAccesses = projectService.getResourcesAccessesForAppRole(project.getId(),
					appRole.getId());

			Iterator<ProjectResourceAccess> i1 = appRoleAccesses.iterator();
			while (i1.hasNext()) {
				ProjectResourceAccess currentAppRoleAccess = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("resource", currentAppRoleAccess.getResource().getIdentification());
				jsonAccess.put("resourceType",
						currentAppRoleAccess.getResource().getClass().getSimpleName().toUpperCase());
				jsonAccess.put("realm", currentAppRoleAccess.getAppRole().getApp().getIdentification());
				jsonAccess.put("role", currentAppRoleAccess.getAppRole().getName());
				jsonAccess.put("accessType", currentAppRoleAccess.getAccess().name());
				responseInfo.put(jsonAccess);
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Get resources from project and appRole")
	@GetMapping(value = "/{project}/resources/role/{appRole}")
	public ResponseEntity<?> resourcesFromProjectAndAppRole(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@ApiParam(value = "AppRole Id", required = true) @PathVariable("appRole") String appRoleName,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User loggedUser = userService.getUser(userId);

			if (loggedUser == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (project.getApp() == null) {
				return new ResponseEntity<>(String.format("Project has no realm associated"), HttpStatus.BAD_REQUEST);
			}

			final App app = project.getApp();
			final AppRole appRole = appService.getByRoleNameAndApp(appRoleName, app);
			if (appRole == null) {
				return new ResponseEntity<>(String.format("Role not found with id %s", appRoleName),
						HttpStatus.BAD_REQUEST);
			}

			JSONArray responseInfo = new JSONArray();

			Set<ProjectResourceAccess> appRoleAccesses = projectService.getResourcesAccessesForAppRole(project.getId(),
					appRole.getId());

			Iterator<ProjectResourceAccess> i1 = appRoleAccesses.iterator();
			while (i1.hasNext()) {
				ProjectResourceAccess currentAppRoleAccess = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("resource", currentAppRoleAccess.getResource().getIdentification());
				jsonAccess.put("resourceType",
						currentAppRoleAccess.getResource().getClass().getSimpleName().toUpperCase());
				jsonAccess.put("realm", currentAppRoleAccess.getAppRole().getApp().getIdentification());
				jsonAccess.put("role", currentAppRoleAccess.getAppRole().getName());
				jsonAccess.put("accessType", currentAppRoleAccess.getAccess().name());
				responseInfo.put(jsonAccess);
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Associate resources to a project by user")
	@PostMapping(value = "/{project}/associateResourcesByUser")
	public ResponseEntity<?> associateResourceByUser(
			@Valid @RequestBody List<ProjectResourceRestUserDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)
					&& !projectService.isUserInProject(userId, project.getId())) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> userIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			projectResources.sort(Comparator.comparing(ProjectResourceRestUserDTO::getResourceType).reversed());
			for (ProjectResourceRestUserDTO projectResource : projectResources) {
				userIds.add(projectResource.getUserId());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> created = resourceService.createAuthorizations(projectId, userIds, resources, versions,
					resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = userIds.iterator();
			Iterator<String> i4 = resourceAccessTypes.iterator();
			Iterator<String> i5 = created.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " (" + i4.next() + ")",
						i5.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Associate resources to a project by user")
	@PostMapping(value = "/{project}/resources/user")
	public ResponseEntity<?> addResourceByUser(@Valid @RequestBody List<ProjectResourceRestUserDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)
					&& !projectService.isUserInProject(userId, project.getId())) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> userIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			projectResources.sort(Comparator.comparing(ProjectResourceRestUserDTO::getResourceType).reversed());
			for (ProjectResourceRestUserDTO projectResource : projectResources) {
				userIds.add(projectResource.getUserId());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> created = resourceService.createAuthorizations(projectId, userIds, resources, versions,
					resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = userIds.iterator();
			Iterator<String> i4 = resourceAccessTypes.iterator();
			Iterator<String> i5 = created.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " (" + i4.next() + ")",
						i5.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Deprecated
	@ApiOperation(value = "Disassociate resources to a project by user")
	@DeleteMapping(value = "/{project}/disassociateResourcesByUser")
	public ResponseEntity<?> disassociateResourceByUser(
			@Valid @RequestBody List<ProjectResourceRestUserDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> userIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			for (ProjectResourceRestUserDTO projectResource : projectResources) {
				userIds.add(projectResource.getUserId());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> deleted = resourceService.deleteAuthorizations(projectId, userIds, resources, versions,
					resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = userIds.iterator();
			Iterator<String> i4 = resourceAccessTypes.iterator();
			Iterator<String> i5 = deleted.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " (" + i4.next() + ")",
						i5.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Disassociate resources to a project by user")
	@DeleteMapping(value = "/{project}/resources/user")
	public ResponseEntity<?> suppressResourceByUser(
			@Valid @RequestBody List<ProjectResourceRestUserDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> userIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			for (ProjectResourceRestUserDTO projectResource : projectResources) {
				userIds.add(projectResource.getUserId());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> deleted = resourceService.deleteAuthorizations(projectId, userIds, resources, versions,
					resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = userIds.iterator();
			Iterator<String> i4 = resourceAccessTypes.iterator();
			Iterator<String> i5 = deleted.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " (" + i4.next() + ")",
						i5.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Associate resources to a project by realm")
	@PostMapping(value = "/{project}/associateResourcesByRealm")
	public ResponseEntity<?> associateResourceByRealm(
			@Valid @RequestBody List<ProjectResourceRestRealmDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)
					&& !projectService.isUserInProject(userId, project.getId())) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() == null) {
				return new ResponseEntity<>(String.format("There is no realm associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> realmIds = new ArrayList<>();
			List<String> roleIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			projectResources.sort(Comparator.comparing(ProjectResourceRestRealmDTO::getResourceType).reversed());
			for (ProjectResourceRestRealmDTO projectResource : projectResources) {
				realmIds.add(projectResource.getRealm());
				roleIds.add(projectResource.getRole());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> created = resourceService.createRealmAuthorizations(projectId, realmIds, roleIds, resources,
					versions, resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = realmIds.iterator();
			Iterator<String> i4 = roleIds.iterator();
			Iterator<String> i5 = resourceAccessTypes.iterator();
			Iterator<String> i6 = created.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " - ROLE " + i4.next()
						+ " (" + i5.next() + ")", i6.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Associate resources to a project by realm")
	@PostMapping(value = "/{project}/resources/realm")
	public ResponseEntity<?> addResourceByRealm(@Valid @RequestBody List<ProjectResourceRestRealmDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)
					&& !projectService.isUserInProject(userId, project.getId())) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() == null) {
				return new ResponseEntity<>(String.format("There is no realm associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> realmIds = new ArrayList<>();
			List<String> roleIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			projectResources.sort(Comparator.comparing(ProjectResourceRestRealmDTO::getResourceType).reversed());
			for (ProjectResourceRestRealmDTO projectResource : projectResources) {
				realmIds.add(projectResource.getRealm());
				roleIds.add(projectResource.getRole());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> created = resourceService.createRealmAuthorizations(projectId, realmIds, roleIds, resources,
					versions, resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = realmIds.iterator();
			Iterator<String> i4 = roleIds.iterator();
			Iterator<String> i5 = resourceAccessTypes.iterator();
			Iterator<String> i6 = created.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " - ROLE " + i4.next()
						+ " (" + i5.next() + ")", i6.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Deprecated
	@ApiOperation(value = "Disassociate resource to a project by realm")
	@DeleteMapping(value = "/{project}/disassociateResourcesByRealm")
	public ResponseEntity<?> disassociateResourceByRealm(
			@Valid @RequestBody List<ProjectResourceRestRealmDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() == null) {
				return new ResponseEntity<>(String.format("There is no realm associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> realmIds = new ArrayList<>();
			List<String> roleIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			for (ProjectResourceRestRealmDTO projectResource : projectResources) {
				realmIds.add(projectResource.getRealm());
				roleIds.add(projectResource.getRole());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> deleted = resourceService.deleteRealmAuthorizations(projectId, realmIds, roleIds, resources,
					versions, resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = realmIds.iterator();
			Iterator<String> i4 = roleIds.iterator();
			Iterator<String> i5 = resourceAccessTypes.iterator();
			Iterator<String> i6 = deleted.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " - ROLE " + i4.next()
						+ " (" + i5.next() + ")", i6.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Disassociate resource to a project by realm")
	@DeleteMapping(value = "/{project}/resources/realm")
	public ResponseEntity<?> suppressResourceByRealm(
			@Valid @RequestBody List<ProjectResourceRestRealmDTO> projectResources,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() == null) {
				return new ResponseEntity<>(String.format("There is no realm associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			JSONObject responseInfo = new JSONObject();
			List<String> realmIds = new ArrayList<>();
			List<String> roleIds = new ArrayList<>();
			List<String> resources = new ArrayList<>();
			List<String> versions = new ArrayList<>();
			List<String> resourceTypes = new ArrayList<>();
			List<String> resourceAccessTypes = new ArrayList<>();
			Integer version;
			for (ProjectResourceRestRealmDTO projectResource : projectResources) {
				realmIds.add(projectResource.getRealm());
				roleIds.add(projectResource.getRole());
				resources.add(projectResource.getResource());
				resourceTypes.add(projectResource.getResourceType().toString());
				version = projectResource.getVersion();
				if (version == null)
					versions.add(null);
				else
					versions.add(version.toString());
				resourceAccessTypes.add(projectResource.getAccessType().toString());
			}

			List<String> deleted = resourceService.deleteRealmAuthorizations(projectId, realmIds, roleIds, resources,
					versions, resourceTypes, resourceAccessTypes, userId);

			Iterator<String> i1 = resources.iterator();
			Iterator<String> i2 = resourceTypes.iterator();
			Iterator<String> i3 = realmIds.iterator();
			Iterator<String> i4 = roleIds.iterator();
			Iterator<String> i5 = resourceAccessTypes.iterator();
			Iterator<String> i6 = deleted.iterator();
			while (i1.hasNext() && i2.hasNext() && i3.hasNext()) {
				responseInfo.put(i1.next() + " (" + i2.next() + ")" + " for " + i3.next() + " - ROLE " + i4.next()
						+ " (" + i5.next() + ")", i6.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@Deprecated
	@ApiOperation(value = "Associate user to a project")
	@PostMapping(value = "/{project}/associateUser")
	public ResponseEntity<?> associateUserToProject(@Valid @RequestBody List<String> users,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			List<String> created = new ArrayList<>();

			for (String userName : users) {
				final User userTo = userService.getUser(userName);
				if (userTo == null) {
					created.add(ERROR_USER_NOT_FOUND);
				} else if (userTo.getRole().getId().toString().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
					created.add(ERROR_USERACCESS_ROL);
				} else if (projectService.isUserInProjectWithoutOwner(userName, project.getId())) {
					created.add(ERROR_USER_IN_PROJECT);
				} else {
					projectService.addUserToProject(userName, project.getId());
					created.add("OK");
				}

			}

			JSONObject responseInfo = new JSONObject();
			Iterator<String> i1 = created.iterator();
			Iterator<String> i2 = users.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i2.next(), i1.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Associate user to a project")
	@PostMapping(value = "/{project}/user")
	public ResponseEntity<?> addUserToProject(@Valid @RequestBody List<String> users,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			List<String> created = new ArrayList<>();

			for (String userName : users) {
				final User userTo = userService.getUser(userName);
				if (userTo == null) {
					created.add(ERROR_USER_NOT_FOUND);
				} else if (userTo.getRole().getId().toString().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
					created.add(ERROR_USERACCESS_ROL);
				} else if (projectService.isUserInProjectWithoutOwner(userName, project.getId())) {
					created.add(ERROR_USER_IN_PROJECT);
				} else {
					projectService.addUserToProject(userName, project.getId());
					created.add("OK");
				}

			}

			JSONObject responseInfo = new JSONObject();
			Iterator<String> i1 = created.iterator();
			Iterator<String> i2 = users.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i2.next(), i1.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Deprecated
	@ApiOperation(value = "Disassociate user to a project")
	@DeleteMapping(value = "/{project}/disassociateUser")
	public ResponseEntity<?> disassociateUserToProject(@Valid @RequestBody List<String> users,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			List<String> deleted = new ArrayList<>();

			for (String userName : users) {
				final User userTo = userService.getUser(userName);
				if (userTo == null) {
					deleted.add(ERROR_USER_NOT_FOUND);
				} else if (!projectService.isUserInProjectWithoutOwner(userName, project.getId())) {
					deleted.add(ERROR_USER_NOT_IN_PROJECT);
				} else {
					projectService.removeUserFromProject(userName, project.getId());
					deleted.add("OK");
				}

			}

			JSONObject responseInfo = new JSONObject();
			Iterator<String> i1 = deleted.iterator();
			Iterator<String> i2 = users.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i2.next(), i1.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Disassociate user to a project")
	@DeleteMapping(value = "/{project}/user")
	public ResponseEntity<?> suppressUserToProject(@Valid @RequestBody List<String> users,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			if (project.getApp() != null) {
				return new ResponseEntity<>(String.format("There is a realm already associated with this project"),
						HttpStatus.BAD_REQUEST);
			}

			List<String> deleted = new ArrayList<>();

			for (String userName : users) {
				final User userTo = userService.getUser(userName);
				if (userTo == null) {
					deleted.add(ERROR_USER_NOT_FOUND);
				} else if (!projectService.isUserInProjectWithoutOwner(userName, project.getId())) {
					deleted.add(ERROR_USER_NOT_IN_PROJECT);
				} else {
					projectService.removeUserFromProject(userName, project.getId());
					deleted.add("OK");
				}

			}

			JSONObject responseInfo = new JSONObject();
			Iterator<String> i1 = deleted.iterator();
			Iterator<String> i2 = users.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i2.next(), i1.next());
			}

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (

		Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Deprecated
	@ApiOperation(value = "Associate realm to a project")
	@PostMapping(value = "/{project}/associateRealm/{realm}")
	public ResponseEntity<?> associateRealmToProject(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@ApiParam(value = "Realm Name", required = true) @PathVariable("realm") String realmId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			String created = "";

			final App realm = appService.getByIdentification(realmId);
			if (realm == null) {
				created = ERROR_REALM_NOT_FOUND;
			} else if (project.getApp() != null) {
				created = ERROR_REALM_IN_PROJECT;
			} else if (!project.getProjectResourceAccesses().isEmpty()) {
				created = ERROR_RESOURCES_IN_PROJECT;
			} else {
				projectService.setRealm(realmId, project.getId());
				created = "OK";
			}

			JSONObject responseInfo = new JSONObject();
			responseInfo.put(realmId, created);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Associate realm to a project")
	@PostMapping(value = "/{project}/realm/{realm}")
	public ResponseEntity<?> addRealmToProject(
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@ApiParam(value = "Realm Name", required = true) @PathVariable("realm") String realmId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			String created = "";

			final App realm = appService.getAppByIdentification(realmId);
			if (realm == null) {
				created = ERROR_REALM_NOT_FOUND;
			} else if (project.getApp() != null) {
				created = ERROR_REALM_IN_PROJECT;
			} else if (!project.getProjectResourceAccesses().isEmpty()) {
				created = ERROR_RESOURCES_IN_PROJECT;
			} else {
				projectService.setRealm(realmId, project.getId());
				created = "OK";
			}

			JSONObject responseInfo = new JSONObject();
			responseInfo.put(realmId, created);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Deprecated
	@ApiOperation(value = "Disassociate realm to a project")
	@DeleteMapping(value = "/{project}/disassociateRealm/{realm}")
	public ResponseEntity<?> disassociateRealmToProject(
			@ApiParam(value = "Realm", required = true) @PathVariable("realm") String realmId,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			String deleted = "";

			final App realm = appService.getAppByIdentification(realmId);
			if (realm == null) {
				deleted = ERROR_REALM_NOT_FOUND;
			} else if (project.getApp() == null) {
				deleted = ERROR_REALM_NOT_IN_PROJECT;
			} else {
				projectService.unsetRealm(realmId, project.getId());
				deleted = "OK";
			}

			JSONObject responseInfo = new JSONObject();
			responseInfo.put(realmId, deleted);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Disassociate realm to a project")
	@DeleteMapping(value = "/{project}/realm/{realm}")
	public ResponseEntity<?> suppressRealmToProject(
			@ApiParam(value = "Realm", required = true) @PathVariable("realm") String realmId,
			@ApiParam(value = "Project Name", required = true) @PathVariable("project") String projectId,
			@RequestHeader("Authorization") String authorization) {

		try {
			final Project project = projectService.getByName(projectId);

			if (project == null) {
				return new ResponseEntity<>(String.format("Project not found with id %s", projectId),
						HttpStatus.BAD_REQUEST);
			}

			final String userId = utils.getUserId();
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format("User not found with id %s", userId), HttpStatus.BAD_REQUEST);
			}

			if (!projectService.isUserAuthorized(project.getId(), userId)) {
				return new ResponseEntity<>(String.format("User with id %s is not authorized", userId),
						HttpStatus.UNAUTHORIZED);
			}

			String deleted = "";

			final App realm = appService.getAppByIdentification(realmId);
			if (realm == null) {
				deleted = ERROR_REALM_NOT_FOUND;
			} else if (project.getApp() == null) {
				deleted = ERROR_REALM_NOT_IN_PROJECT;
			} else {
				projectService.unsetRealm(realmId, project.getId());
				deleted = "OK";
			}

			JSONObject responseInfo = new JSONObject();
			responseInfo.put(realmId, deleted);

			return new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);

		} catch (Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	JSONArray resourceAccessestoJson(Set<ProjectResourceAccess> userAccesses, Project project) {
		JSONArray responseInfo = new JSONArray();
		Iterator<ProjectResourceAccess> i1 = userAccesses.iterator();
		while (i1.hasNext()) {
			ProjectResourceAccess currentUserAccess = i1.next();
			JSONObject jsonAccess = new JSONObject();
			jsonAccess.put("resource", currentUserAccess.getResource().getIdentification());
			jsonAccess.put("resourceType", currentUserAccess.getResource().getClass().getSimpleName().toUpperCase());
			if (project.getApp() == null)
				jsonAccess.put("userId", currentUserAccess.getUser().getUserId());
			else {
				jsonAccess.put("realm", currentUserAccess.getAppRole().getApp().getIdentification());
				jsonAccess.put("role", currentUserAccess.getAppRole().getName());
			}
			jsonAccess.put("accessType", currentUserAccess.getAccess().name());
			responseInfo.put(jsonAccess);
		}
		return responseInfo;
	}

	JSONArray projectUserstoJson(Project project) {
		JSONArray responseInfo = new JSONArray();
		if (project.getApp() == null) {
			Set<User> projectUsers = projectService.getUsersInProject(project.getId());

			Iterator<User> i1 = projectUsers.iterator();
			while (i1.hasNext()) {
				User currentUser = i1.next();
				JSONObject jsonAccess = new JSONObject();
				jsonAccess.put("userId", currentUser.getUserId());
				responseInfo.put(jsonAccess);
			}
		} else {
			Set<AppRole> roles = projectService.getProjectRoles(project.getId());
			if (!roles.isEmpty()) {
				Map<AppRole, Set<AppUser>> projectUserRoles = projectService.getAllRoleUsersInProject(roles);

				for (Map.Entry<AppRole, Set<AppUser>> entry : projectUserRoles.entrySet()) {
					Iterator<AppUser> i2 = entry.getValue().iterator();
					while (i2.hasNext()) {
						AppUser currentUser = i2.next();
						JSONObject jsonAccess = new JSONObject();
						jsonAccess.put("roleId", entry.getKey().getName());
						jsonAccess.put("userId", currentUser.getUser().getUserId());
						responseInfo.put(jsonAccess);
					}
				}
			}
		}
		return responseInfo;
	}

}
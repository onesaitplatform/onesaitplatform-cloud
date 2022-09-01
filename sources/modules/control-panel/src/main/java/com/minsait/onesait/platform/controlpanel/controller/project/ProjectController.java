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
package com.minsait.onesait.platform.controlpanel.controller.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.base.OPResource.Resources;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.exceptions.ProjectServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectDTO;
import com.minsait.onesait.platform.config.services.project.ProjectResourceAccessDTO;
import com.minsait.onesait.platform.config.services.project.ProjectResourceDTO;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.project.ProjectUserDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/projects")
@Slf4j
public class ProjectController {

	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private WebProjectService webprojectService;
	@Autowired
	private AppService appService;
	@Autowired
	private GadgetService gadgetService;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private GadgetDatasourceService datasourceService;
	@Autowired
	private OntologyService ontologyService;
	/*
	 * @Autowired private ResourcesInUseService resourcesInUseService;
	 */

	private static final String ALL_USERS = "ALL";
	private static final String PROJECT_OBJ_STR = "projectObj";
	private static final String ERROR_403 = "error/403";
	private static final String REDIRECT_PROJ_LIST = "redirect:/projects/list";
	private static final String PROJ_FRAG_USERTAB = "project/fragments/users-tab";
	private static final String PROJ_FRAG_RESTAB = "project/fragments/resources-tab";
	private static final String ACCESSES = "accesses";
	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18000/web/}")
	private String rootWWW;

	@GetMapping("list")

	public String list(Model model) {
		model.addAttribute("projects", projectService.getProjectsForUser(utils.getUserId()));
		return "project/list";
	}

	@GetMapping("create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_DEVELOPER')")
	public String create(Model model) {
		model.addAttribute(PROJECT_OBJ_STR, new Project());
		model.addAttribute("projectTypes", Project.ProjectType.values());
		return "project/create";
	}

	@GetMapping("show/{id}")
	public String show(Model model, @PathVariable("id") String projectId) {
		if (!projectService.isUserInProject(utils.getUserId(), projectId) && !utils.isAdministrator()) {
			return ERROR_403;
		}
		final String creator = projectService.getById(projectId).getUser().getUserId().toString();
		final boolean isCreator = creator.equals(utils.getUserId());
		model.addAttribute("urlsMap", getUrlsMap());
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(projectId));
		model.addAttribute("userRole", utils.getRole());
		model.addAttribute("rootWWW", rootWWW);
		if (utils.getRole().equals("ROLE_ADMINISTRATOR") || isCreator) {
			final Set<ProjectResourceAccess> pr = projectService.getById(projectId).getProjectResourceAccesses();
			final Collection<List<ProjectResourceAccess>> prfil = pr.stream()
					.collect(Collectors.groupingBy(ProjectResourceAccess::getResource)).values();
			final List<ProjectResourceAccess> pra = new ArrayList<>();
			for (final List<ProjectResourceAccess> elem : prfil) {
				pra.add(elem.get(0));
			}
			model.addAttribute("objResources", pra);
		} else {
			model.addAttribute("objResources",
					projectService.getResourcesAccessesForUser(projectId, utils.getUserId()));
		}
		return "project/show";
	}

	@PostMapping("create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_DEVELOPER')")
	public String createProject(Model model, @Valid ProjectDTO project, RedirectAttributes redirect) {
		project.setUser(userService.getUser(utils.getUserId()));
		try {
			projectService.createProject(project);
			Project projectDb = projectService.getByName(project.getIdentification());
			projectService.addUserToProject(projectDb.getUser().getUserId(), projectDb.getId());
			populateUsertabData(model, projectDb.getId());
		} catch (final ProjectServiceException e) {
			log.debug("Cannot create project");
			utils.addRedirectException(e, redirect);
			return "redirect:/projects/create";
		}
		return REDIRECT_PROJ_LIST;
	}

	@GetMapping("update/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String update(Model model, @PathVariable("id") String id) {
		if (!projectService.isUserAuthorized(id, utils.getUserId())) {
			return ERROR_403;
		}
		populateUsertabData(model, id);
		model.addAttribute("projectTypes", Project.ProjectType.values());
		model.addAttribute("resourceTypes", Resources.values());
		/*
		 * model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
		 * resourcesInUseService.isInUse(id, utils.getUserId()));
		 * resourcesInUseService.put(id, utils.getUserId());
		 */

		return "project/create";
	}

	@GetMapping("share/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String share(Model model, @PathVariable("id") String id) {
		if (!projectService.isUserInProject(utils.getUserId(), id)) {
			return ERROR_403;
		}
		populateUsertabData(model, id);
		model.addAttribute("resourceTypes", Resources.values());
		return "project/share";
	}

	@PutMapping("update/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String updateProject(Model model, @Valid ProjectDTO project, @PathVariable("id") String id) {
		if (!projectService.isUserAuthorized(id, utils.getUserId())) {
			return ERROR_403;
		}
		/* resourcesInUseService.removeByUser(id, utils.getUserId()); */
		projectService.updateWithParameters(project);
		return REDIRECT_PROJ_LIST;
	}

	@PostMapping("setrealm")
	public String setRealm(Model model, @RequestParam("realm") String realmId,
			@RequestParam("project") String projectId, RedirectAttributes ra) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		try {
			projectService.setRealm(realmId, projectId);
		} catch (final AppServiceException e) {
			log.error("Can not set realm {} to project {}", realmId, projectId, e);
			utils.addRedirectException(e, ra);
		}
		populateUsertabData(model, projectId);
		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("unsetrealm")
	public String unsetRealm(Model model, @RequestParam("realm") String realmId,
			@RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		projectService.unsetRealm(realmId, projectId);
		populateUsertabData(model, projectId);

		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("adduser")
	public String addUser(Model model, @RequestParam("project") String projectId, @RequestParam("user") String userId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		projectService.addUserToProject(userId, projectId);
		populateUsertabData(model, projectId);
		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("removeuser")
	public String removeUser(Model model, @RequestParam("project") String projectId,
			@RequestParam("user") String userId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		projectService.removeUserFromProject(userId, projectId);
		populateUsertabData(model, projectId);
		return PROJ_FRAG_USERTAB;
	}

	@PostMapping("addwebproject")
	public String addWebProject(Model model, @RequestParam("webProject") String webProjectId,
			@RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		projectService.addWebProject(webProjectId, projectId, utils.getUserId());
		populateWebProjectTabData(model, projectId);
		return "project/fragments/webprojects-tab";
	}

	@PostMapping("removewebproject")
	public String removeWebProject(Model model, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		projectService.removeWebProject(projectId);
		populateWebProjectTabData(model, projectId);
		return "project/fragments/webprojects-tab";
	}

	@GetMapping("resources")
	public String getResources(Model model, @RequestParam("identification") String identification,
			@RequestParam("type") Resources resource, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())
				&& !projectService.isUserInProject(utils.getUserId(), projectId)) {
			return ERROR_403;
		}
		model.addAttribute("resourcesMatch", getAllResourcesDTO2(identification, resource));
		populateResourcesModal(model, projectId, resource);
		return "project/fragments/resources-modal";
	}

	@GetMapping("associated")
	public String getAssociated(Model model, @RequestParam("resourceId") String resourceId,
			@RequestParam("type") String resource, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())
				&& !projectService.isUserInProject(utils.getUserId(), projectId)) {
			return ERROR_403;
		}

		JSONArray elementsJson = new JSONArray();
		switch (resource) {
		case "GADGETDATASOURCE":
			elementsJson = new JSONArray(datasourceService.getElementsAssociated(resourceId));
			break;
		case "GADGET":
			elementsJson = new JSONArray(gadgetService.getElementsAssociated(resourceId));
			break;
		case "DASHBOARD":
			elementsJson = new JSONArray(dashboardService.getElementsAssociated(resourceId));
			break;
		case "ONTOLOGY":
			elementsJson = new JSONArray(ontologyService.getElementsAssociated(resourceId));
			break;
		default:
			break;
		}

		final List<ProjectResourceDTO> elements = new ArrayList<>();

		for (int i = 0; i < elementsJson.length(); i++) {
			final ProjectResourceDTO element = new ProjectResourceDTO();
			final JSONObject elementJson = elementsJson.getJSONObject(i);
			element.setId(elementJson.get("id").toString());
			element.setIdentification(elementJson.get("identification").toString());
			element.setType(elementJson.get("type").toString());
			elements.add(element);
		}

		model.addAttribute(ACCESSES, ResourceAccessType.values());
		model.addAttribute("elements", elements);
		return "project/fragments/elements-associated-modal";
	}

	@GetMapping("authorizations")
	public String authorizationsTab(Model model, @RequestParam("project") String projectId) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())
				&& !projectService.isUserInProject(utils.getUserId(), projectId)) {
			return ERROR_403;
		}
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(projectId));
		return PROJ_FRAG_RESTAB;
	}

	@PostMapping("authorizations")
	@Transactional
	public String insertAuthorization(Model model, @RequestBody @Valid ProjectResourceAccessDTO authorization) {
		if (!projectService.isUserAuthorized(authorization.getProject(), utils.getUserId())
				&& !resourceService.isUserAuthorized(utils.getUserId(), authorization.getResource())) {
			return ERROR_403;
		}
		log.debug("New request for access for user {} with permission {} to resource {} in project {}",
				authorization.getAuthorizing(), authorization.getAccess().name(), authorization.getResource(),
				authorization.getProject());
		final Project project = projectService.getById(authorization.getProject());
		final Set<ProjectResourceAccess> accesses = new HashSet<>();
		if (project.getApp() != null) {
			if (authorization.getAuthorizing().equals(ALL_USERS)) {
				projectService.getProjectRoles(authorization.getProject())
						.forEach(ar -> accesses.add(new ProjectResourceAccess(null, authorization.getAccess(),
								resourceService.getResourceById(authorization.getResource()), project,
								appService.findRole(ar.getId()))));
				resourceService.insertAuthorizations(accesses);
			} else {
				resourceService.createUpdateAuthorization(new ProjectResourceAccess(null, authorization.getAccess(),
						resourceService.getResourceById(authorization.getResource()), project,
						appService.findRole(authorization.getAuthorizing())));
			}
		} else {
			if (authorization.getAuthorizing().equals(ALL_USERS)) {
				project.getUsers().forEach(u -> accesses.add(new ProjectResourceAccess(u, authorization.getAccess(),
						resourceService.getResourceById(authorization.getResource()), project, null)));
				if (authorization.getResourceType().equalsIgnoreCase(Resources.DATAFLOW.toString())
						|| authorization.getResourceType().equalsIgnoreCase(Resources.NOTEBOOK.toString())) {
					resourceService.insertAuthorizations(accesses.stream()
							.filter(a -> userService.isUserAnalytics(a.getUser())).collect(Collectors.toSet()));
				} else {
					resourceService.insertAuthorizations(accesses);
				}
			} else {
				resourceService.createUpdateAuthorization(new ProjectResourceAccess(
						userService.getUser(authorization.getAuthorizing()), authorization.getAccess(),
						resourceService.getResourceById(authorization.getResource()), project, null));
			}

		}
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(authorization.getProject()));
		return PROJ_FRAG_RESTAB;
	}

	@PostMapping("authorizationsAssociated")
	@Transactional
	public String insertAuthorizationAssociated(Model model,
			@RequestBody @Valid List<ProjectResourceAccessDTO> authorizations) {
		for (final ProjectResourceAccessDTO authorization : authorizations) {
			if (!projectService.isUserAuthorized(authorization.getProject(), utils.getUserId())
					&& !resourceService.isUserAuthorized(utils.getUserId(), authorization.getResource())) {
				return ERROR_403;
			}
			final Project project = projectService.getById(authorization.getProject());
			final Set<ProjectResourceAccess> accesses = new HashSet<>();
			if (project.getApp() != null) {
				if (authorization.getAuthorizing().equals(ALL_USERS)) {
					projectService.getProjectRoles(authorization.getProject())
							.forEach(ar -> accesses.add(new ProjectResourceAccess(null, authorization.getAccess(),
									resourceService.getResourceById(authorization.getResource()), project,
									appService.findRole(ar.getId()))));
					resourceService.insertAuthorizations(accesses);

				} else {
					resourceService.createUpdateAuthorization(new ProjectResourceAccess(null, authorization.getAccess(),
							resourceService.getResourceById(authorization.getResource()), project,
							appService.findRole(authorization.getAuthorizing())));
				}
			} else {
				if (authorization.getAuthorizing().equals(ALL_USERS)) {
					project.getUsers().forEach(u -> accesses.add(new ProjectResourceAccess(u, authorization.getAccess(),
							resourceService.getResourceById(authorization.getResource()), project, null)));
					resourceService.insertAuthorizations(accesses);
				} else {
					resourceService.createUpdateAuthorization(new ProjectResourceAccess(
							userService.getUser(authorization.getAuthorizing()), authorization.getAccess(),
							resourceService.getResourceById(authorization.getResource()), project, null));
				}
			}
			model.addAttribute(PROJECT_OBJ_STR, projectService.getById(authorization.getProject()));
		}
		return PROJ_FRAG_RESTAB;
	}

	@DeleteMapping("authorizations")
	public String deleteAuthorization(Model model, @RequestParam("id") String id,
			@RequestParam("project") String projectId) {

		try {
			resourceService.removeAuthorization(id, projectId, utils.getUserId());
		} catch (final Exception e) {
			return ERROR_403;
		}
		model.addAttribute(PROJECT_OBJ_STR, projectService.getById(projectId));
		return PROJ_FRAG_RESTAB;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("{id}")
	public String delete(Model model, @PathVariable("id") String projectId, RedirectAttributes ra) {
		if (!projectService.isUserAuthorized(projectId, utils.getUserId())) {
			return ERROR_403;
		}
		try {
			projectService.deleteProject(projectId);
		} catch (final Exception e) {
			utils.addRedirectException(e, ra);
		}
		return list(model);
	}

	private void populateUsertabData(Model model, String projectId) {
		final Project project = projectService.getById(projectId);
		final List<ProjectUserDTO> members = projectService.getProjectMembers(project.getId());
		model.addAttribute("members", members);
		model.addAttribute("realms", projectService.getAvailableRealmsForUser(utils.getUserId()));
		model.addAttribute(ACCESSES, ResourceAccessType.values());
		model.addAttribute("users",
				userService.getAllActiveUsers().stream().filter(u -> !u.isAdmin()).collect(Collectors.toList()));
		model.addAttribute("webprojects",
				webprojectService.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), "", ""));
		model.addAttribute(PROJECT_OBJ_STR, project);
	}

	private void populateResourcesModal(Model model, String projectId, Resources type) {
		final Project project = projectService.getById(projectId);
		List<ProjectUserDTO> members = projectService.getProjectMembers(project.getId());
		if (type.name().equals(Resources.DATAFLOW.name()) || type.name().equals(Resources.NOTEBOOK.name())) {
			members = members.stream().filter(m -> userService.isUserAnalytics(userService.getUser(m.getUserId())))
					.collect(Collectors.toList());
		}
		model.addAttribute(ACCESSES, ResourceAccessType.values());
		model.addAttribute(PROJECT_OBJ_STR, project);
		model.addAttribute("members", members);
		if (project.getApp() != null) {
			model.addAttribute("roles", projectService.getProjectRoles(projectId));
		}
	}

	private void populateWebProjectTabData(Model model, String projectId) {
		final Project project = projectService.getById(projectId);
		model.addAttribute("webprojects",
				webprojectService.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), "", ""));
		model.addAttribute(PROJECT_OBJ_STR, project);

	}

	private List<ProjectResourceDTO> getAllResourcesDTO(String identification, Resources type) {
		final Collection<OPResource> resources = resourceService.getResources(utils.getUserId(), identification);
		String type_resource;

		if (type.name().equals(OPResource.Resources.API.toString())) {
			type_resource = type.name();
			return resources.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase(type_resource))
					.map(r -> ProjectResourceDTO.builder().id(r.getId())
							.identification(r.getIdentification() + " - V" + ((Api) r).getNumversion())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		} else {
			if (type.name().equals("DATAFLOW")) {
				type_resource = "PIPELINE";
			} else {
				type_resource = type.name();
			}
			return resources.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase(type_resource))
					.map(r -> ProjectResourceDTO.builder().id(r.getId()).identification(r.getIdentification())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		}
	}

	private List<ProjectResourceDTO> getAllResourcesDTO2(String identification, Resources type) {

		String type_resource;
		if (type.name().equals("DATAFLOW")) {
			type_resource = "PIPELINE";
		} else {
			type_resource = type.name();
		}
		final Collection<OPResource> resources2 = resourceService.getResourcesByType(utils.getUserId(), type_resource);

		if (type_resource.equals("API")) {
			return resources2.stream()
					.filter(r -> r.getIdentification().toLowerCase().contains(identification.toLowerCase()))
					.map(r -> ProjectResourceDTO.builder().id(r.getId())
							.identification(r.getIdentification() + " - V" + ((Api) r).getNumversion())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		} else if (type_resource.equals("CONFIGURATION")) {
			return resources2.stream()
					.filter(r -> (r.getIdentification().toLowerCase().contains(identification.toLowerCase())
							&& ((Configuration) r).getType().equals(Configuration.Type.EXTERNAL_CONFIG)))
					.map(r -> ProjectResourceDTO.builder().id(r.getId()).identification(r.getIdentification())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		} else {
			return resources2.stream()
					.filter(r -> r.getIdentification().toLowerCase().contains(identification.toLowerCase()))
					.map(r -> ProjectResourceDTO.builder().id(r.getId()).identification(r.getIdentification())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		}
	}

	private Map<String, String> getUrlsMap() {
		final Map<String, String> urls = new HashMap<>();
		urls.put(Resources.API.name(), "apimanager");
		urls.put(Resources.CLIENTPLATFORM.name(), "devices");
		urls.put(Resources.DASHBOARD.name(), "dashboards");
		urls.put(Resources.GADGET.name(), "gadgets");
		urls.put(Resources.DIGITALTWINDEVICE.name(), "digitaltwindevices");
		urls.put(Resources.FLOWDOMAIN.name(), "flows");
		urls.put(Resources.NOTEBOOK.name(), "notebooks");
		urls.put(Resources.ONTOLOGY.name(), "ontologies");
		urls.put(Resources.DATAFLOW.name(), "dataflow");
		urls.put(Resources.GADGETDATASOURCE.name(), "datasources");
		urls.put(Resources.ONTOLOGYVIRTUALDATASOURCE.name(), "virtualdatasources");
		urls.put(Resources.CONFIGURATION.name(), "configurations");
		urls.put(Resources.GADGETTEMPLATE.name(), "gadgettemplates");
		return urls;
	}
}

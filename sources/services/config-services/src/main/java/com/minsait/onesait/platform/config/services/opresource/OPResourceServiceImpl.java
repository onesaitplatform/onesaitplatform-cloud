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
package com.minsait.onesait.platform.config.services.opresource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.dto.ProjectUserAccess;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessVersioning;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.base.OPResource.Resources;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.AppRoleRepository;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.entity.cast.EntitiesCast;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OPResourceServiceImpl implements OPResourceService {

	@Autowired
	private OPResourceRepository resourceRepository;
	@Autowired
	private ProjectResourceAccessRepository resourceAccessRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	@Lazy
	private ProjectService projectService;
	@Autowired
	private AppUserRepository appUserRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	@Lazy
	private AppService appService;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private AppRoleRepository appRoleRepository;
	@Autowired
	private GadgetService gadgetService;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private GadgetDatasourceService datasourceService;

	private static final String ERROR = "ERROR: ";
	private static final String ERROR_MISSING_DATA = "Missing data in message";
	private static final String ERROR_USERACCESS_PROPR = "Not possible to manage access to user creator of project";
	private static final String ERROR_USERACCESS_ROL = "Not possible to manage access to administrator: role not allowed";
	private static final String ERROR_NOT_VERSION_API = "API version not provided";
	private static final String ERROR_USER_NOT_IN_PROJECT = "User is not in project";
	private static final String ERROR_ACCESS_EXISTS = "Invalid operation, user access already exists";
	private static final String ERROR_ACCESS_NOT_EXISTS = "Invalid operation, user access does not exist";
	private static final String ERROR_RESOURCE_NOT_FOUND = "Invalid input data: Resource not found";
	private static final String ERROR_USER_NOT_FOUND = "Invalid input data: User not found";
	private static final String ERROR_LOGGED_USER_NO_AUTH = "Logged user not authorized to manage access to the resource";
	private static final String ERROR_NO_ROLES = "Invalid input data: No roles associated";
	private static final String ERROR_REALM_NOT_FOUND = "Invalid input data: Realm not found";
	private static final String ERROR_REALM_NOT_IN_PROJECT = "Invalid input data: Realm not in project";
	private static final String ERROR_NOT_RESOURCE_TYPE = "Invalid operation: User is not authorized to use ";

	@Override
	public Collection<OPResource> getResources(String userId, String identification) {
		if (identification == null) {
			identification = "";
		}
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			return resourceRepository.findByIdentificationContainingIgnoreCase(identification);
		} else {
			final List<OPResource> resources = resourceRepository
					.findByIdentificationContainingIgnoreCase(identification);
			final Set<OPResource> resourcesFiltered = resources.stream().filter(r -> r.getUser().equals(user))
					.collect(Collectors.toSet());
			resourcesFiltered.addAll(resources.stream().filter(r -> r instanceof Ontology).filter(o -> ((Ontology) o)
					.getOntologyUserAccesses().stream()
					.map(a -> (a.getUser().equals(user)
							&& a.getOntologyUserAccessType().getName().equals(OntologyUserAccessType.Type.ALL.name())))
					.findAny().orElse(false)).collect(Collectors.toSet()));
			return resourcesFiltered;
		}

	}

	@Override
	public Collection<OPResource> getResourcesByType(String userId, String type) {
		final User user = userService.getUser(userId);
		List<OPResource> res;
		if (userService.isUserAdministrator(user)) {
			res = resourceRepository.findAll();
		} else {
			res = resourceRepository.findByUser(userService.getUser(userId));
		}

		return res.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase(type))
				.collect(Collectors.toList());

	}

	@Override
	public Collection<Versionable<?>> getResourcesVersionablesByType(User user, Class<?> type) {
		List<OPResource> res;
		if (userService.isUserAdministrator(user)) {
			res = resourceRepository.findAll();
		} else {
			res = resourceRepository.findByUser(user);
		}

		return res.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase(type.getSimpleName()))
				.map(opr -> (Versionable<?>) opr).collect(Collectors.toList());

	}

	@Override
	public Collection<OPResource> getAllResourcesVersionablesVOs() {
		return resourceRepository.findAll().stream().filter(o -> o instanceof Versionable<?>)
				.collect(Collectors.toList());
	}

	@Override
	public void createUpdateAuthorization(ProjectResourceAccess pRA) {
		ProjectResourceAccess pRADB;
		log.debug("createUpdateAuthorization() arguments are: {}", pRA.toString());
		if (pRA.getAppRole() != null) {
			pRADB = pRA.getProject().getProjectResourceAccesses().stream()
					.filter(a -> a.getResource().equals(pRA.getResource()) && a.getAppRole().equals(pRA.getAppRole())
							&& a.getProject().equals(pRA.getProject()))
					.findFirst().orElse(null);
		} else {
			pRADB = pRA.getProject().getProjectResourceAccesses().stream()
					.filter(a -> a.getResource().equals(pRA.getResource()) && a.getUser().equals(pRA.getUser())
							&& a.getProject().equals(pRA.getProject()))
					.findFirst().orElse(null);
		}

		if (pRADB != null) {
			log.debug("Project already had authorization, will remove it: {}", pRADB.toString());
			pRA.getProject().getProjectResourceAccesses().remove(pRADB);
		}

		log.debug("createUpdateAuthorization() persisting new entity: {}", pRA.toString());
		pRA.getProject().getProjectResourceAccesses().add(pRA);
		log.debug("Project {} now has the following accesess:", pRA.getProject().getIdentification());
		pRA.getProject().getProjectResourceAccesses().forEach(ra -> log.debug("{}", ra.toString()));
		projectService.updateProject(pRA.getProject());

	}

	@Override
	public OPResource getResourceById(String id) {
		return resourceRepository.findById(id).orElse(null);
	}

	@Override
	public OPResource getResourceByIdentificationAndType(String identification, Resources type) {
		List<OPResource> resList = resourceRepository.findByIdentification(identification);
		if (resList.isEmpty()) {
			return null;
		} else {
			if (type.toString().equalsIgnoreCase("DATAFLOW")) {
				resList = resList.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase("PIPELINE"))
						.collect(Collectors.toList());
			}

			else {
				resList = resList.stream().filter(r -> r.getClass().getSimpleName().equalsIgnoreCase(type.toString()))
						.collect(Collectors.toList());
			}

			if (resList.size() == 1) {
				return resList.get(0);
			} else {
				return null;
			}
		}
	}

	@Override
	public OPResource getResourceByIdentification(String identification) {
		final List<OPResource> resList = resourceRepository.findByIdentification(identification);
		if (resList.isEmpty()) {
			return null;
		} else {
			if (resList.size() == 1) {
				return resList.get(0);
			} else {
				return null;
			}
		}
	}

	@Override
	@Transactional
	public void removeAuthorization(String id, String projectId, String userId) throws GenericOPException {
		resourceAccessRepository.findById(id).ifPresent(pra -> {
			if (!projectService.isUserAuthorized(projectId, userId)
					&& !isUserAuthorized(userId, pra.getResource().getId())) {
				return;
			}
			resourceAccessRepository.deleteById(id);
		});

	}

	@Override
	public boolean hasAccess(String userId, String resourceId, ResourceAccessType access) {
		boolean hasAccess = false;
		switch (access) {
		case MANAGE:
			hasAccess = !resourceAccessRepository.findByUserIdAndResourceIdAndAcessManage(userId, resourceId).isEmpty();
			break;

		case VIEW:
		default:
			hasAccess = !resourceAccessRepository.findByUserIdAndResourceIdAndAccessView(userId, resourceId).isEmpty();
		}
		return hasAccess;

	}

	@Override
	public ResourceAccessType getResourceAccess(String userId, String resourceId) {
		final List<ProjectResourceAccessList> accesses = resourceAccessRepository.findByUserIdAndResourceId(userId,
				resourceId);
		return accesses.stream().map(ProjectResourceAccessList::getAccess).findAny().orElse(null);

	}

	@Override
	public Map<String, ResourceAccessType> getResourcesAccessMapByUserAndResourceIdList(User user,
			List<String> resourceIdList) {
		final Map<String, ResourceAccessType> midrat = new HashMap<>();
		final List<ProjectUserAccess> accesses = resourceAccessRepository.findUserAccessByUserAndResourceIds(user,
				resourceIdList);
		for (final ProjectUserAccess pra : accesses) {
			midrat.put(pra.getEntity_id(), pra.getAccessType());
		}
		return midrat;
	}

	@Override
	public void insertAuthorizations(Set<ProjectResourceAccess> accesses) {
		log.debug("insertAuthorizations()");
		accesses.forEach(ra -> log.debug("{}", ra.toString()));
		final Project project = accesses.iterator().next().getProject();
		final Set<ProjectResourceAccess> repeated = accesses.stream()
				.filter(pra -> project.getProjectResourceAccesses().contains(pra)).collect(Collectors.toSet());
		repeated.forEach(pra -> pra.getProject().getProjectResourceAccesses().remove(pra));
		project.getProjectResourceAccesses().addAll(accesses);
		log.debug("Persisting project {} ,now has the following accesess:", project.getIdentification());
		project.getProjectResourceAccesses().forEach(ra -> log.debug("{}", ra.toString()));
		projectService.updateProject(project);

	}

	@Override
	public List<String> createAuthorizations(String projectName, List<String> userIds, List<String> resources,
			List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes, String currentUser) {
		final List<String> created = new ArrayList<>();

		if (!(userIds.size() == resources.size() && resourceTypes.size() == resourceAccessTypes.size())) {
			return created;
		}

		for (int i = 0; i < userIds.size(); i++) {
			try {
				final String userId = userIds.get(i);
				final String resource = resources.get(i);
				final String resourceType = resourceTypes.get(i);
				final String resourceAccessType = resourceAccessTypes.get(i);
				final String version = versions.get(i);
				createResourceAccess(projectName, userId, resource, version, resourceType, resourceAccessType,
						currentUser);
				created.add("OK");
			} catch (final DataIntegrityViolationException e) {
				created.add(ERROR_ACCESS_EXISTS);
			} catch (final OPResourceServiceException e) {
				created.add(e.getMessage());
			} catch (final NullPointerException e) {
				created.add(ERROR_MISSING_DATA);
			} catch (final Exception e) {
				created.add(e.getMessage());
			}
		}

		return created;
	}

	@Override
	public List<String> createRealmAuthorizations(String projectName, List<String> realmIds, List<String> roleIds,
			List<String> resources, List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes,
			String currentUser) {
		final List<String> created = new ArrayList<>();

		if (!(realmIds.size() == resources.size() && resourceTypes.size() == roleIds.size())) {
			return created;
		}

		for (int i = 0; i < realmIds.size(); i++) {
			try {
				final String realmId = realmIds.get(i);
				final String roleId = roleIds.get(i);
				final String resource = resources.get(i);
				final String resourceType = resourceTypes.get(i);
				final String resourceAccessType = resourceAccessTypes.get(i);
				final String version = versions.get(i);
				createResourceAccessRealm(projectName, realmId, roleId, resource, version, resourceType,
						resourceAccessType, currentUser);
				created.add("OK");
			} catch (final DataIntegrityViolationException e) {
				created.add(ERROR_ACCESS_EXISTS);
			} catch (final OPResourceServiceException e) {
				created.add(e.getMessage());
			} catch (final NullPointerException e) {
				created.add(ERROR_ACCESS_NOT_EXISTS);
			} catch (final Exception e) {
				created.add(e.getMessage());
			}
		}

		return created;
	}

	@Override
	public List<String> deleteAuthorizations(String projectName, List<String> userIds, List<String> resources,
			List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes, String currentUser) {
		final List<String> deleted = new ArrayList<>();

		if (!(userIds.size() == resources.size() && resourceTypes.size() == resourceAccessTypes.size())) {
			return deleted;
		}

		for (int i = 0; i < userIds.size(); i++) {
			try {
				final String userId = userIds.get(i);
				final String resource = resources.get(i);
				final String resourceType = resourceTypes.get(i);
				final String resourceAccessType = resourceAccessTypes.get(i);
				final String version = versions.get(i);
				deleteResourceAccess(projectName, userId, resource, version, resourceType, resourceAccessType,
						currentUser);
				deleted.add("OK");
			} catch (final DataIntegrityViolationException e) {
				deleted.add(ERROR_ACCESS_EXISTS);
			} catch (final OPResourceServiceException e) {
				deleted.add(e.getMessage());
			} catch (final NullPointerException e) {
				deleted.add(ERROR_ACCESS_NOT_EXISTS);
			} catch (final Exception e) {
				deleted.add(e.getMessage());
			}
		}

		return deleted;
	}

	@Override
	public List<String> deleteRealmAuthorizations(String projectName, List<String> realmIds, List<String> roleIds,
			List<String> resources, List<String> versions, List<String> resourceTypes, List<String> resourceAccessTypes,
			String userId) {
		final List<String> deleted = new ArrayList<>();

		if (!(realmIds.size() == resources.size() && resourceTypes.size() == roleIds.size())) {
			return deleted;
		}

		for (int i = 0; i < realmIds.size(); i++) {
			try {
				final String realmId = realmIds.get(i);
				final String roleId = roleIds.get(i);
				final String resource = resources.get(i);
				final String resourceType = resourceTypes.get(i);
				final String resourceAccessType = resourceAccessTypes.get(i);
				final String version = versions.get(i);
				deleteResourceAccessRealm(projectName, realmId, roleId, resource, version, resourceType,
						resourceAccessType, userId);
				deleted.add("OK");
			} catch (final DataIntegrityViolationException e) {
				deleted.add(ERROR_ACCESS_EXISTS);
			} catch (final OPResourceServiceException e) {
				deleted.add(e.getMessage());
			} catch (final NullPointerException e) {
				deleted.add(ERROR_ACCESS_NOT_EXISTS);
			} catch (final Exception e) {
				deleted.add(e.getMessage());
			}
		}

		return deleted;
	}

	@Override
	public boolean isResourceSharedInAnyProject(OPResource resource) {
		return resourceAccessRepository.countByResource(resource) > 0;
	}

	@Override
	public List<ProjectResourceAccess> getProjectsByResource(OPResource resource) {
		return resourceAccessRepository.findByResource(resource);
	}

	@Override
	public Collection<OPResource> getResourcesForUserAndType(User user, String type) {
		final List<AppUser> appUsers = appUserRepository.findByUser(user);
		final Set<OPResource> resources = new HashSet<>();
		if (!CollectionUtils.isEmpty(appUsers)) {
			appUsers.forEach(au -> resources.addAll(resourceAccessRepository.findByAppRole(au.getRole()).stream()
					.map(ProjectResourceAccess::getResource).filter(r -> r.getClass().getSimpleName().equals(type))
					.collect(Collectors.toSet())));
		}
		resources.addAll(resourceAccessRepository.findByUser(user).stream().map(ProjectResourceAccess::getResource)
				.filter(r -> r.getClass().getSimpleName().equals(type)).collect(Collectors.toSet()));
		return resources;
	}

	private Collection<OPResource> getResourcesForUserAndType(User user, String type, ResourceAccessType accessType) {
		final Set<OPResource> resources = new HashSet<>();
		resources.addAll(resourceAccessRepository.findByUserIdVersioning(user.getUserId()).stream()
				.filter(pra -> accessType.equals(pra.getAccess())).map(ProjectResourceAccessVersioning::getResource)
				.filter(r -> r.getClass().getSimpleName().equals(type)).collect(Collectors.toSet()));
		return resources;
	}

	@Override
	@Transactional
	public Collection<Versionable<?>> getResourcesVersionablesForUserAndType(User user, Class<?> type) {
		final long start = System.currentTimeMillis();
		final List<OPResource> res = resourceRepository.findByUser(user).stream().filter(r -> r.getClass().equals(type))
				.collect(Collectors.toList());
		log.debug("First collect took time: {} ms", System.currentTimeMillis() - start);
		final Collection<OPResource> projectResources = getResourcesForUserAndType(user, type.getSimpleName(),
				ResourceAccessType.MANAGE);
		log.debug("After second collect took total time: {} ms", System.currentTimeMillis() - start);
		return Stream.of(res, projectResources).flatMap(Collection::stream).map(opr -> (Versionable<?>) opr)
				.collect(Collectors.toList());

	}

	@Override
	public boolean isUserAuthorized(String userId, String resourceId) {
		final User user = userService.getUser(userId);

		final OPResource resource = resourceRepository.findById(resourceId).orElse(null);
		return userService.isUserAdministrator(user) || resource != null && resource.getUser().equals(user)
				|| hasAccess(userId, resourceId, ResourceAccessType.MANAGE);

	}

	void createResourceAccess(String projectName, String userId, String resourceId, String version, String resourceType,
			String resourceAccessType, String currentUserId) {
		ProjectResourceAccess projectResourceAccess = null;

		if (!projectName.equals("") && !userId.equals("") && !resourceId.equals("") && !resourceType.equals("")) {

			final User user = userRepository.findByUserId(userId);
			final ProjectList project = projectService.getByNameForList(projectName);

			if (resourceType.equals(Resources.API.toString()) && version == null) {
				log.error(ERROR_NOT_VERSION_API);
				throw new OPResourceServiceException(ERROR_NOT_VERSION_API);
			}

			final OPResource resource;
			if (Resources.valueOf(resourceType) != Resources.API) {
				resource = getResourceByIdentificationAndType(resourceId, Resources.valueOf(resourceType));
			} else {
				resource = apiRepository.findByIdentificationAndNumversion(resourceId, Integer.parseInt(version));
			}

			if (resource == null) {
				log.error(ERROR_RESOURCE_NOT_FOUND);
				throw new OPResourceServiceException(ERROR_RESOURCE_NOT_FOUND);
			}

			if (!isUserAuthorized(currentUserId, resource.getId())) {
				log.error(ERROR_LOGGED_USER_NO_AUTH);
				throw new OPResourceServiceException(ERROR_LOGGED_USER_NO_AUTH);
			}

			final ProjectResourceAccessList pRA = checkResourceAccessByUser(project, user, resource, version,
					resourceType, resourceAccessType, currentUserId);

			if (pRA == null) {
				projectResourceAccess = new ProjectResourceAccess();
				projectResourceAccess.setProject(EntitiesCast.castProjectList(project, false));
				projectResourceAccess.setUser(user);
				projectResourceAccess.setResource(resource);
				projectResourceAccess.setAccess(ResourceAccessType.valueOf(resourceAccessType));
				resourceAccessRepository.save(projectResourceAccess);
			} else if (pRA.getAccess().name().equals(resourceAccessType)) {
				log.error(ERROR_ACCESS_EXISTS);
				throw new OPResourceServiceException(ERROR_ACCESS_EXISTS);
			} else {
				pRA.setAccess(ResourceAccessType.valueOf(resourceAccessType));
				resourceAccessRepository.save(EntitiesCast.castProjectResourceAccessList(pRA));
			}

			JSONArray elementsJson = new JSONArray();
			switch (resourceType) {
			case "GADGETDATASOURCE":
				elementsJson = new JSONArray(datasourceService.getElementsAssociated(resource.getId()));
				break;
			case "GADGET":
				elementsJson = new JSONArray(gadgetService.getElementsAssociated(resource.getId()));
				break;
			case "DASHBOARD":
				elementsJson = new JSONArray(dashboardService.getElementsAssociated(resource.getId()));
				break;
			default:
				break;
			}

			for (int i = 0; i < elementsJson.length(); i++) {
				final JSONObject elementJson = elementsJson.getJSONObject(i);

				final ProjectResourceAccess projectRA = new ProjectResourceAccess();
				projectRA.setProject(EntitiesCast.castProjectList(project, false));
				projectRA.setUser(user);
				final OPResource res = getResourceById(elementJson.get("id").toString());
				projectRA.setResource(res);
				projectRA.setAccess(ResourceAccessType.VIEW);
				final ProjectResourceAccessList prevPRA = resourceAccessRepository
						.findByResourceListAndProjectAndUserId(res.getId(), project.getId(), user.getUserId());
				if (prevPRA == null) {
					resourceAccessRepository.save(projectRA);
				}

			}
		}

		else {
			log.error(ERROR_MISSING_DATA);
			throw new OPResourceServiceException(ERROR_MISSING_DATA);
		}
	}

	void deleteResourceAccess(String projectName, String userId, String resourceId, String version, String resourceType,
			String resourceAccessType, String currentUserId) throws GenericOPException {

		if (!projectName.equals("") && !userId.equals("") && !resourceId.equals("") && !resourceType.equals("")) {

			final User user = userRepository.findByUserId(userId);
			final ProjectList project = projectService.getByNameForList(projectName);

			if (resourceType.equals(Resources.API.toString()) && version == null) {
				log.error(ERROR_NOT_VERSION_API);
				throw new OPResourceServiceException(ERROR_NOT_VERSION_API);
			}

			final OPResource resource;
			if (Resources.valueOf(resourceType) != Resources.API) {
				resource = getResourceByIdentificationAndType(resourceId, Resources.valueOf(resourceType));
			} else {
				resource = apiRepository.findByIdentificationAndNumversion(resourceId, Integer.parseInt(version));
			}

			if (resource == null) {
				log.error(ERROR_RESOURCE_NOT_FOUND);
				throw new OPResourceServiceException(ERROR_RESOURCE_NOT_FOUND);
			}

			final ProjectResourceAccessList pRA = checkResourceAccessByUser(project, user, resource, version,
					resourceType, resourceAccessType, currentUserId);

			if (pRA == null) {
				log.error(ERROR_ACCESS_NOT_EXISTS);
				throw new OPResourceServiceException(ERROR_ACCESS_NOT_EXISTS);
			}
			deleteUserAccess(pRA.getId());
		} else {
			log.error(ERROR_MISSING_DATA);
			throw new OPResourceServiceException(ERROR_MISSING_DATA);
		}
	}

	private ProjectResourceAccessList checkResourceAccessByUser(ProjectList project, User user, OPResource resource,
			String version, String resourceType, String resourceAccessType, String currentUserId) {

		if (user == null) {
			log.error(ERROR_USER_NOT_FOUND);
			throw new OPResourceServiceException(ERROR_USER_NOT_FOUND);
		}
		if (userService.isUserAdministrator(user)) {
			log.error(ERROR_USERACCESS_ROL);
			throw new OPResourceServiceException(ERROR_USERACCESS_ROL);
		}
		if (!projectService.isUserInProjectWithoutOwner(user.getUserId(), project.getId())) {
			log.error(ERROR_USER_NOT_IN_PROJECT);
			throw new OPResourceServiceException(ERROR_USER_NOT_IN_PROJECT);
		}

		if (!userService.isUserAnalytics(user) && (resourceType.equals(Resources.DATAFLOW.toString())
				|| resourceType.equals(Resources.NOTEBOOK.toString()))) {
			log.error(ERROR_NOT_RESOURCE_TYPE + resourceType);
			throw new OPResourceServiceException(ERROR_NOT_RESOURCE_TYPE + resourceType);
		}
		if (userService.isUserUser(user) && resourceAccessType.equals(ResourceAccessType.MANAGE.toString())
				|| userService.isUserUser(user) && !(resourceType.equals(Resources.ONTOLOGY.toString())
						|| resourceType.equals(Resources.DASHBOARD.toString()))) {
			log.error(ERROR_NOT_RESOURCE_TYPE + resourceType);
			throw new OPResourceServiceException(
					ERROR_NOT_RESOURCE_TYPE + resourceType + " (" + resourceAccessType + ")");
		}
		return resourceAccessRepository.findByResourceListAndProjectAndUserId(resource.getId(), project.getId(),
				user.getUserId());
	}

	private void createResourceAccessRealm(String projectName, String realmId, String roleId, String resourceId,
			String version, String resourceType, String resourceAccessType, String currentUserId) {
		ProjectResourceAccess projectResourceAccess = null;

		if (!projectName.equals("") && !realmId.equals("") && !resourceId.equals("") && !resourceType.equals("")
				&& !roleId.equals("")) {
			final Project project = projectService.getByName(projectName);
			final App realm = appService.getAppByIdentification(realmId);

			if (realm == null) {
				log.error(ERROR_REALM_NOT_FOUND);
				throw new OPResourceServiceException(ERROR_REALM_NOT_FOUND);
			}
			if (realm.getAppRoles().isEmpty()) {
				log.error(ERROR_NO_ROLES);
				throw new OPResourceServiceException(ERROR_NO_ROLES);
			}

			if (!project.getApp().equals(realm)) {
				log.error(ERROR_REALM_NOT_IN_PROJECT);
				throw new OPResourceServiceException(ERROR_REALM_NOT_IN_PROJECT);
			}

			if (resourceType.equals(Resources.API.toString()) && version == null) {
				log.error(ERROR_NOT_VERSION_API);
				throw new OPResourceServiceException(ERROR_NOT_VERSION_API);
			}

			final OPResource resource;
			if (Resources.valueOf(resourceType) != Resources.API) {
				resource = getResourceByIdentificationAndType(resourceId, Resources.valueOf(resourceType));
			} else {
				resource = apiRepository.findByIdentificationAndNumversion(resourceId, Integer.parseInt(version));
			}

			final AppRole rol = appRoleRepository.findAll().stream()
					.filter(r -> r.getApp().getIdentification().equals(realmId)).filter(r -> r.getName().equals(roleId))
					.collect(Collectors.toList()).get(0);

			if (resource == null) {
				log.error(ERROR_RESOURCE_NOT_FOUND);
				throw new OPResourceServiceException(ERROR_RESOURCE_NOT_FOUND);
			}

			if (!isUserAuthorized(currentUserId, resource.getId())) {
				log.error(ERROR_LOGGED_USER_NO_AUTH);
				throw new OPResourceServiceException(ERROR_LOGGED_USER_NO_AUTH);
			}

			final ProjectResourceAccess pRA = checkResourceAccessByRealm(project, rol, resource, version, resourceType,
					resourceAccessType, currentUserId);

			if (pRA == null) {
				projectResourceAccess = new ProjectResourceAccess();
				projectResourceAccess.setProject(project);
				projectResourceAccess.setAppRole(rol);
				projectResourceAccess.setResource(resource);
				projectResourceAccess.setAccess(ResourceAccessType.valueOf(resourceAccessType));
				resourceAccessRepository.save(projectResourceAccess);
			} else if (pRA.getAccess().name().equals(resourceAccessType)) {
				log.error(ERROR_ACCESS_EXISTS);
				throw new OPResourceServiceException(ERROR_ACCESS_EXISTS);
			} else {
				pRA.setAccess(ResourceAccessType.valueOf(resourceAccessType));
				resourceAccessRepository.save(pRA);
			}

			JSONArray elementsJson = new JSONArray();
			switch (resourceType) {
			case "GADGETDATASOURCE":
				elementsJson = new JSONArray(datasourceService.getElementsAssociated(resource.getId()));
				break;
			case "GADGET":
				elementsJson = new JSONArray(gadgetService.getElementsAssociated(resource.getId()));
				break;
			case "DASHBOARD":
				elementsJson = new JSONArray(dashboardService.getElementsAssociated(resource.getId()));
				break;
			default:
				break;
			}

			for (int i = 0; i < elementsJson.length(); i++) {
				final JSONObject elementJson = elementsJson.getJSONObject(i);

				final ProjectResourceAccess projectRA = new ProjectResourceAccess();
				projectRA.setProject(project);
				projectRA.setAppRole(rol);
				final OPResource res = getResourceById(elementJson.get("id").toString());
				projectRA.setResource(res);
				projectRA.setAccess(ResourceAccessType.VIEW);
				final ProjectResourceAccess prevPRA = resourceAccessRepository.findByResourceAndProjectAndAppRole(res,
						project, rol);
				if (prevPRA == null) {
					resourceAccessRepository.save(projectRA);
				}

			}

		} else {
			log.error(ERROR_MISSING_DATA);
			throw new OPResourceServiceException(ERROR_MISSING_DATA);
		}

	}

	private void deleteResourceAccessRealm(String projectName, String realmId, String roleId, String resourceId,
			String version, String resourceType, String resourceAccessType, String currentUserId)
			throws GenericOPException {

		if (!projectName.equals("") && !realmId.equals("") && !resourceId.equals("") && !resourceType.equals("")
				&& !roleId.equals("")) {
			final Project project = projectService.getByName(projectName);
			final App realm = appService.getAppByIdentification(realmId);

			if (realm == null) {
				log.error(ERROR_REALM_NOT_FOUND);
				throw new OPResourceServiceException(ERROR_REALM_NOT_FOUND);
			}

			if (realm.getAppRoles().isEmpty()) {
				log.error(ERROR_NO_ROLES);
				throw new OPResourceServiceException(ERROR_NO_ROLES);
			}

			if (!project.getApp().equals(realm)) {
				log.error(ERROR_REALM_NOT_IN_PROJECT);
				throw new OPResourceServiceException(ERROR_REALM_NOT_IN_PROJECT);
			}

			if (resourceType.equals(Resources.API.toString()) && version == null) {
				log.error(ERROR_NOT_VERSION_API);
				throw new OPResourceServiceException(ERROR_NOT_VERSION_API);
			}

			final OPResource resource;
			if (Resources.valueOf(resourceType) != Resources.API) {
				resource = getResourceByIdentificationAndType(resourceId, Resources.valueOf(resourceType));
			} else {
				resource = apiRepository.findByIdentificationAndNumversion(resourceId, Integer.parseInt(version));
			}

			if (resource == null) {
				log.error(ERROR_RESOURCE_NOT_FOUND);
				throw new OPResourceServiceException(ERROR_RESOURCE_NOT_FOUND);
			}

			final AppRole rol = appRoleRepository.findAll().stream()
					.filter(r -> r.getApp().getIdentification().equals(realmId)).filter(r -> r.getName().equals(roleId))
					.collect(Collectors.toList()).get(0);

			final ProjectResourceAccess pRA = checkResourceAccessByRealm(project, rol, resource, version, resourceType,
					resourceAccessType, currentUserId);

			if (pRA == null) {
				log.error(ERROR_ACCESS_NOT_EXISTS);
				throw new OPResourceServiceException(ERROR_ACCESS_NOT_EXISTS);
			}

			project.getProjectResourceAccesses().removeIf(p -> p.equals(pRA));
			projectService.updateProject(project);
		} else {
			log.error(ERROR_MISSING_DATA);
			throw new OPResourceServiceException(ERROR_MISSING_DATA);
		}

	}

	private ProjectResourceAccess checkResourceAccessByRealm(Project project, AppRole rol, OPResource resource,
			String version, String resourceType, String resourceAccessType, String currentUserId) {

		if (rol == null) {
			log.error(ERROR_NO_ROLES);
			throw new OPResourceServiceException(ERROR_NO_ROLES);
		}

		final Set<AppUser> usersRol = rol.getAppUsers();
		final List<String> usuarios = new ArrayList<>();
		final List<String> rolesRol = new ArrayList<>();
		for (final AppUser appUser : usersRol) {
			usuarios.add(appUser.getUser().getUserId());
			rolesRol.add(appUser.getUser().getRole().getId());
		}

		if (rolesRol.contains(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			log.error(ERROR_USERACCESS_ROL);
			throw new OPResourceServiceException(ERROR_USERACCESS_ROL);
		}

		return resourceAccessRepository.findByResourceAndProjectAndAppRole(resource, project, rol);
	}

	@Override
	public void deleteUserAccess(ProjectResourceAccess projectUserAcc, Project project) {
		project.getProjectResourceAccesses().remove(projectUserAcc);
		projectRepository.save(project);
	}

	@Override
	public void deleteUserAccess(String projectResourceAccessId) {
		resourceAccessRepository.deleteById(projectResourceAccessId);
	}

}

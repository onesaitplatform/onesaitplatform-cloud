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
package com.minsait.onesait.platform.config.services.opresource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class OPResourceServiceImpl implements OPResourceService {

	@Autowired
	private OPResourceRepository resourceRepository;
	@Autowired
	private ProjectResourceAccessRepository resourceAccessRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private AppUserRepository appUserRepository;

	@Override
	public Collection<OPResource> getResources(String userId, String identification) {
		if (identification == null)
			identification = "";
		final User user = userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			return resourceRepository.findByIdentificationContainingIgnoreCase(identification);
		else {
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
	public void createUpdateAuthorization(ProjectResourceAccess pRA) {
		ProjectResourceAccess pRADB;

		if (pRA.getAppRole() != null)
			pRADB = pRA.getProject().getProjectResourceAccesses().stream()
					.filter(a -> a.getResource().equals(pRA.getResource()) && a.getAppRole().equals(pRA.getAppRole())
							&& a.getProject().equals(pRA.getProject()))
					.findFirst().orElse(null);

		else
			pRADB = pRA.getProject().getProjectResourceAccesses().stream()
					.filter(a -> a.getResource().equals(pRA.getResource()) && a.getUser().equals(pRA.getUser())
							&& a.getProject().equals(pRA.getProject()))
					.findFirst().orElse(null);

		if (pRADB != null) {
			pRA.getProject().getProjectResourceAccesses().remove(pRADB);
		}

		pRA.getProject().getProjectResourceAccesses().add(pRA);
		projectService.updateProject(pRA.getProject());

	}

	@Override
	public OPResource getResourceById(String id) {
		return resourceRepository.findOne(id);
	}

	@Override
	@Transactional
	public void removeAuthorization(String id, String projectId, String userId) throws GenericOPException {
		final Project project = projectService.getById(projectId);
		final ProjectResourceAccess pra = resourceAccessRepository.findOne(id);
		if (!projectService.isUserAuthorized(projectId, userId) && !isUserAuthorized(userId, pra.getResource().getId()))
			throw new GenericOPException("Unauthorized");
		project.getProjectResourceAccesses().removeIf(p -> p.equals(pra));
		projectService.updateProject(project);

	}

	@Override
	public boolean hasAccess(String userId, String resourceId, ResourceAccessType access) {
		final User user = userService.getUser(userId);
		final OPResource resource = resourceRepository.findOne(resourceId);
		final List<ProjectResourceAccess> accesses = resourceAccessRepository.findByResource(resource);
		return accesses.stream().map(pra -> userIsAllowed(pra, user, access)).filter(Boolean::booleanValue).findFirst()
				.orElse(false);
	}

	private boolean userIsAllowed(ProjectResourceAccess pra, User user, ResourceAccessType access) {
		if (pra.getAppRole() != null) {
			final User userInApp = pra.getAppRole().getAppUsers().stream().map(AppUser::getUser)
					.filter(u -> u.equals(user)).findFirst().orElse(null);
			if (userInApp != null) {
				switch (access) {
				case MANAGE:
					return access.equals(pra.getAccess());
				case VIEW:
				default:
					return true;
				}

			}
		} else {
			if (pra.getUser().equals(user)) {
				switch (access) {
				case MANAGE:
					return pra.getAccess().equals(access);
				case VIEW:
				default:
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ResourceAccessType getResourceAccess(String userId, String resourceId) {
		final User user = userService.getUser(userId);
		final OPResource resource = resourceRepository.findOne(resourceId);
		final List<ProjectResourceAccess> accesses = resourceAccessRepository.findByResource(resource);
		return accesses.stream().map(pra -> {
			if (pra.getAppRole() != null) {
				final User userInApp = pra.getAppRole().getAppUsers().stream().map(AppUser::getUser)
						.filter(u -> u.equals(user)).findFirst().orElse(null);
				if (userInApp != null) {
					return pra.getAccess();
				}
			} else {
				if (pra.getUser().equals(user))
					return pra.getAccess();
			}
			return null;

		}).filter(Objects::nonNull).findFirst().orElse(null);

	}

	@Override
	public void insertAuthorizations(Set<ProjectResourceAccess> accesses) {
		final Project project = accesses.iterator().next().getProject();
		final Set<ProjectResourceAccess> repeated = accesses.stream()
				.filter(pra -> project.getProjectResourceAccesses().contains(pra)).collect(Collectors.toSet());
		repeated.forEach(pra -> pra.getProject().getProjectResourceAccesses().remove(pra));
		project.getProjectResourceAccesses().addAll(accesses);
		projectService.updateProject(project);

	}

	@Override
	public boolean isResourceSharedInAnyProject(OPResource resource) {
		return (resourceAccessRepository.countByResource(resource) > 0);
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

	@Override
	public boolean isUserAuthorized(String userId, String resourceId) {
		final User user = userService.getUser(userId);

		final OPResource resource = resourceRepository.findOne(resourceId);
		return (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
				|| (resource != null && resource.getUser().equals(user))
				|| hasAccess(userId, resourceId, ResourceAccessType.MANAGE));

	}

}

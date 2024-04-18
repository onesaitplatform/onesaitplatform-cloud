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
package com.minsait.onesait.platform.business.services.opendata;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResourceDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataUser;
import com.minsait.onesait.platform.config.services.user.UserService;

@Component
public class OpenDataPermissions {

	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private UserService userService;
	
	public boolean hasPermissionsToCreateDataset(String userToken, Object dataset) {
		final List<OpenDataOrganization> orgsFromUser = organizationService.getOrganizationsFromUser(userToken);
		if (orgsFromUser.isEmpty()) {
			return false;
		} else {
			if (dataset == null) {
				// GET datasets/create
				return orgsFromUser.stream()
						.anyMatch(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"));
			} else {
				// POST datasets/create
				final String orgId = getOrganizationId(dataset);
				return orgsFromUser.stream().anyMatch(elem -> elem.getId().equals(orgId)
						&& (elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor")));
			}
		}
	}

	public boolean hasPermissionsToManipulateDataset(String userToken, Object dataset) {
		final List<OpenDataOrganization> orgsFromUser = organizationService.getOrganizationsFromUser(userToken);
		if (orgsFromUser.isEmpty()) {
			return false;
		} else {
			if (dataset == null || getId(dataset) == null) {
				// Intenta editar/borrar un dataset privado de fuera de su organizacion
				return false;
			} else {
				final String orgId = getOrganizationId(dataset);
				return orgsFromUser.stream().anyMatch(elem -> elem.getId().equals(orgId)
						&& (elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor")));
			}
		}
	}

	public boolean hasPermissionsToShowDataset(Object dataset) {
		return dataset != null && getId(dataset) != null;
	}

	public boolean hasPermissionsToCreateResource(String userToken, Object resource) {
		final List<OpenDataOrganization> orgsFromUser = organizationService.getOrganizationsFromUser(userToken);
		if (orgsFromUser.isEmpty()) {
			return false;
		} else {
			if (resource == null) {
				// GET resources/create
				return orgsFromUser.stream()
						.anyMatch(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"));
			} else {
				// POST resources/create
				final String datasetId = getDatasetId(resource);
				final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
				if (dataset == null || getId(dataset) == null) {
					return false;
				} else {
					final String orgId = getOrganizationId(dataset);
					return orgsFromUser.stream().anyMatch(elem -> elem.getId().equals(orgId)
							&& (elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor")));
				}
			}
		}
	}

	public boolean hasPermissionsToManipulateResource(String userToken, Object resource) {
		final List<OpenDataOrganization> orgsFromUser = organizationService.getOrganizationsFromUser(userToken);
		if (orgsFromUser.isEmpty()) {
			return false;
		} else {
			if (resource == null || getId(resource) == null) {
				// Intenta editar/borrar un recurso privado de fuera de su organizacion
				return false;
			} else {
				// Buscamos el dataset al que pertenece
				final String datasetId = getDatasetId(resource);
				final OpenDataPackage dataset = datasetService.getDatasetById(userToken, datasetId);
				if (dataset == null || getId(dataset) == null) {
					return false;
				} else {
					final String orgId = getOrganizationId(dataset);
					return orgsFromUser.stream().anyMatch(elem -> elem.getId().equals(orgId)
							&& (elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor")));
				}
			}
		}
	}

	public boolean hasPermissionsToShowResource(Object resource) {
		return resource != null && getId(resource) != null;
	}

	public boolean hasPermissionsToManipulateOrganization(String userId, OpenDataOrganization organization) {
		final List<OpenDataUser> users = organization.getUsers();
		final Optional<OpenDataUser> foundUser = users.stream().filter(elem -> elem.getName().equals(userId))
				.findFirst();
		if (foundUser.isPresent()) {
			final String role = foundUser.get().getCapacity();
			return role.equals("admin");
		} else {
			return false;
		}
	}

	public boolean hasPermissionsToShowOrganization(OpenDataOrganization organization, String userId) {
		final List<OpenDataUser> users = organization.getUsers();
		final Optional<OpenDataUser> foundUser = users.stream().filter(elem -> elem.getName().equals(userId))
				.findFirst();
		return foundUser.isPresent();
	}

	private String getOrganizationId(Object dataset) {
		if (dataset instanceof OpenDataPackageDTO) {
			return ((OpenDataPackageDTO) dataset).getOrganization();
		} else {
			return ((OpenDataPackage) dataset).getOwner_org();
		}
	}

	private String getId(Object obj) {
		if (obj instanceof OpenDataPackageDTO) {
			return ((OpenDataPackageDTO) obj).getId();
		} else if (obj instanceof OpenDataPackage) {
			return ((OpenDataPackage) obj).getId();
		} else if (obj instanceof OpenDataResourceDTO) {
			return ((OpenDataResourceDTO) obj).getId();
		} else {
			return ((OpenDataResource) obj).getId();
		}
	}

	private String getDatasetId(Object resource) {
		if (resource instanceof OpenDataResourceDTO) {
			return ((OpenDataResourceDTO) resource).getDataset();
		} else {
			return ((OpenDataResource) resource).getPackage_id();
		}
	}

	public boolean isUserValidForRole(String userId, String role) {
		final User user = userService.getUser(userId);
		if (!userService.isUserUser(user))
			return true;
		return (userService.isUserUser(user) && role.equals("member"));
	}
}

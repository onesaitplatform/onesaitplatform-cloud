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
package com.minsait.onesait.platform.business.services.opendata.organization;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.business.services.opendata.OpenDataApi;
import com.minsait.onesait.platform.business.services.opendata.user.OpenDataUserService;
import com.minsait.onesait.platform.business.services.resources.ResourceService;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataAuthorizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataMember;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataUser;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.MemberCreateResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.MemberListResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.OrganizationListForUserResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.OrganizationShowResponse;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

	private static final String DELETED = "deleted";

	@Autowired
	private OpenDataApi api;
	@Autowired
	private UserService userService;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private OpenDataUserService openDataUserService;

	@Override
	public List<OpenDataOrganization> getOrganizationsFromUser(String userToken) {
		final OrganizationListForUserResponse responseOrgsFromUser = (OrganizationListForUserResponse) api
				.getOperation("organization_list_for_user", userToken, OrganizationListForUserResponse.class);

		if (responseOrgsFromUser.getSuccess()) {
			return responseOrgsFromUser.getResult();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<OpenDataOrganizationDTO> getDTOFromOrganizationList(List<OpenDataOrganization> organizationsFromUser) {
		final List<OpenDataOrganizationDTO> dtos = new ArrayList<>();
		for (final OpenDataOrganization org : organizationsFromUser) {
			final OpenDataOrganizationDTO obj = new OpenDataOrganizationDTO();
			obj.setName(org.getTitle());
			obj.setId(org.getId());
			obj.setRole(org.getCapacity());
			obj.setDescription(org.getDescription());

			Date created = null;
			try {
				created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(org.getCreated());
			} catch (final ParseException e) {
				log.error("Cannot parse date: %s", org.getCreated());
			}
			obj.setCreatedAt(created);
			obj.setUpdatedAt(null);
			dtos.add(obj);
		}
		return dtos;
	}

	@Override
	public boolean existsOrganization(OpenDataOrganizationDTO organizationDTO, String userToken) {
		final String orgName = organizationDTO.getName();
		try {
			final OrganizationShowResponse responseOrganization = (OrganizationShowResponse) api
					.getOperation("organization_show?id=" + orgName, userToken, OrganizationShowResponse.class);
			if (responseOrganization.getSuccess() && responseOrganization.getResult().getState().equals(DELETED)) {
				throw new OpenDataServiceException("Organization exists in deleted state");
			}
			return responseOrganization.getSuccess();
		} catch (final HttpClientErrorException e) {
			return e.getStatusCode() != HttpStatus.NOT_FOUND;
		}
	}

	@Override
	public OpenDataOrganization createOrganization(OpenDataOrganizationDTO organizationDTO, MultipartFile image,
			String userToken) {
		final OpenDataOrganization organization = new OpenDataOrganization();
		organization.setTitle(organizationDTO.getTitle());
		organization.setName(organizationDTO.getName());

		if (organizationDTO.getDescription() != null && !organizationDTO.getDescription().equals("")) {
			organization.setDescription(organizationDTO.getDescription());
		}

		OrganizationShowResponse responseOrganization = null;
		if (image != null && !image.getOriginalFilename().isEmpty()) {
			organization.setImage_upload(image);
			responseOrganization = (OrganizationShowResponse) api
					.postOperationWithFile("organization_create", userToken, organization).getBody();
		} else {
			responseOrganization = (OrganizationShowResponse) api
					.postOperation("organization_create", userToken, organization, OrganizationShowResponse.class)
					.getBody();
		}

		if (responseOrganization != null && responseOrganization.getSuccess()) {
			return responseOrganization.getResult();
		} else {
			return null;
		}
	}

	@Override
	public List<User> getUsersFromOrganization(String userToken, String orgId) {
		final List<User> result = new ArrayList<>();
		final MemberListResponse memberList = (MemberListResponse) api.getOperation("member_list?id=" + orgId,
				userToken, MemberListResponse.class);
		final List<OpenDataUser> userList = openDataUserService.getAllUsers();

		for (final List<String> member : memberList.getResult()) {
			if (member.get(1).equals("user")) {
				final String userId = member.get(0);
				final Optional<OpenDataUser> foundUser = userList.stream().filter(elem -> elem.getId().equals(userId))
						.findFirst();
				if (foundUser.isPresent()) {
					final String platformUserName = foundUser.get().getName();
					final User platformUser = userService.getUser(platformUserName);
					if (platformUser != null) {
						result.add(platformUser);
					}
				}
			}
		}

		return result;
	}

	@Override
	public OpenDataOrganization getOrganizationById(String userToken, String id) {
		try {
			final OrganizationShowResponse responseOrganization = (OrganizationShowResponse) api
					.getOperation("organization_show?id=" + id, userToken, OrganizationShowResponse.class);
			if (responseOrganization.getSuccess()) {
				if (responseOrganization.getResult().getState().equals(DELETED)) {
					throw new OpenDataServiceException("Organization exists in deleted state");
				}
				return responseOrganization.getResult();
			} else {
				return null;
			}
		} catch (final HttpClientErrorException e) {
			log.error("Error getting organization " + e.getMessage());
			return null;
		}
	}

	@Override
	public OpenDataOrganizationDTO getDTOFromOrganization(OpenDataOrganization organization) {
		final OpenDataOrganizationDTO organizationDTO = new OpenDataOrganizationDTO();
		organizationDTO.setId(organization.getId());
		organizationDTO.setTitle(organization.getTitle());
		organizationDTO.setName(organization.getName());
		organizationDTO.setDescription(organization.getDescription());
		organizationDTO.setImage_display_url(
				organization.getImage_display_url().isEmpty() ? null : organization.getImage_display_url());
		try {
			organizationDTO
					.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(organization.getCreated()));
		} catch (final ParseException e) {
			log.error("Cannot parse date: %s", organization.getCreated());
		}
		return organizationDTO;
	}

	@Override
	public OpenDataOrganization updateOrganization(OpenDataOrganizationDTO organizationDTO, MultipartFile image,
			String userToken) {
		final OpenDataOrganization organization = new OpenDataOrganization();
		organization.setId(organizationDTO.getId());
		organization.setTitle(organizationDTO.getTitle());
		organization.setName(organizationDTO.getName());

		if (organizationDTO.getDescription() != null) {
			organization.setDescription(organizationDTO.getDescription());
		} else {
			organization.setDescription("");
		}

		OrganizationShowResponse responseOrganization = null;
		if (image != null && !image.getOriginalFilename().isEmpty()) {
			organization.setImage_upload(image);
			responseOrganization = (OrganizationShowResponse) api
					.postOperationWithFile("organization_patch", userToken, organization).getBody();
		} else {
			responseOrganization = (OrganizationShowResponse) api
					.postOperation("organization_patch", userToken, organization, OrganizationShowResponse.class)
					.getBody();
		}

		if (responseOrganization != null && responseOrganization.getSuccess()) {
			return responseOrganization.getResult();
		} else {
			return null;
		}
	}

	@Override
	public void deleteOrganization(String userToken, String id) {
		final OpenDataOrganization organization = new OpenDataOrganization();
		organization.setId(id);
		api.postOperation("organization_delete", userToken, organization, OrganizationShowResponse.class);
	}

	@Override
	public List<OpenDataAuthorizationDTO> getDTOAuthorizations(String orgId, String userToken) {
		final List<OpenDataAuthorizationDTO> result = new ArrayList<>();
		final MemberListResponse memberList = (MemberListResponse) api.getOperation("member_list?id=" + orgId,
				userToken, MemberListResponse.class);
		final List<OpenDataUser> userList = openDataUserService.getAllUsers();

		for (final List<String> member : memberList.getResult()) {
			if (member.get(1).equals("user")) {
				final String userId = member.get(0);
				final String role = member.get(2).toLowerCase();
				final Optional<OpenDataUser> foundUser = userList.stream().filter(elem -> elem.getId().equals(userId))
						.findFirst();
				if (foundUser.isPresent()) {
					final OpenDataAuthorizationDTO authDTO = new OpenDataAuthorizationDTO();
					final String platformUserId = foundUser.get().getName();
					authDTO.setId(platformUserId + "/" + role);
					authDTO.setUserName(foundUser.get().getDisplay_name());
					authDTO.setRole(role);
					authDTO.setUserId(platformUserId);
					result.add(authDTO);
				}
			}
		}

		return result;
	}

	@Override
	public void updateOrganizationResourcesPermissions(String orgId, String userToken) {
		final List<OpenDataResource> resources = resourceService.getResourcesFromOrganization(orgId, userToken);
		for (final OpenDataResource resource : resources) {
			final String resourceUrl = resource.getUrl();
			final Dashboard dashboard = resourceService.checkDashboardResource(resourceUrl);
			final Api api = resourceService.checkApiResource(resourceUrl);
			if (dashboard != null) {
				resourceService.updateDashboardPermissions(dashboard, resource.getPackage_id(), userToken);
			} else if (api != null) {
				resourceService.updateApiPermissions(api, resource.getPackage_id(), userToken);
			}
		}
	}

	@Override
	public OpenDataMember manipulateOrgMembers(String orgId, String userId, String role, String userToken) {
		String endpoint = "";
		final List<OpenDataUser> userList = openDataUserService.getAllUsers();
		final Optional<OpenDataUser> foundUser = userList.stream().filter(elem -> elem.getName().equals(userId))
				.findFirst();
		if (foundUser.isPresent()) {
			final OpenDataMember member = new OpenDataMember();
			member.setId(orgId);
			member.setUsername(userId);
			if (role != null && !role.equals("")) {
				member.setRole(role);
				endpoint = "organization_member_create";
			} else {
				endpoint = "organization_member_delete";
			}
			api.postOperation(endpoint, userToken, member, MemberCreateResponse.class);
			return member;
		} else {
			return null;
		}
	}

	@Override
	public String getOrganizationId(String identification) {
		String str = identification.replaceAll("\\s+", "-").toLowerCase();
		str = Normalizer.normalize(str, Normalizer.Form.NFKD);
		return str.replaceAll("[^a-z,^A-Z,^0-9,^-]", "");
	}

}

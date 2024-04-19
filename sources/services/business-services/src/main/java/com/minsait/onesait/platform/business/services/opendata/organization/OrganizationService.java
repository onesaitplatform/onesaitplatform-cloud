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
package com.minsait.onesait.platform.business.services.opendata.organization;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataAuthorizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataMember;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganizationDTO;

public interface OrganizationService {

	public List<OpenDataOrganization> getOrganizationsFromUser(String userToken);

	public List<OpenDataOrganizationDTO> getDTOFromOrganizationList(List<OpenDataOrganization> organizationsFromUser);

	public boolean existsOrganization(OpenDataOrganizationDTO organizationDTO, String userToken);

	public OpenDataOrganization createOrganization(OpenDataOrganizationDTO organizationDTO, MultipartFile image, String userToken);

	public List<User> getUsersFromOrganization(String userToken, String orgId);

	public OpenDataOrganization getOrganizationById(String userToken, String id);

	public OpenDataOrganizationDTO getDTOFromOrganization(OpenDataOrganization organization);

	public OpenDataOrganization updateOrganization(OpenDataOrganizationDTO organizationDTO, MultipartFile image, String userToken);

	public void deleteOrganization(String userToken, String id);

	public List<OpenDataAuthorizationDTO> getDTOAuthorizations(String orgId, String userToken);

	public void updateOrganizationResourcesPermissions(String orgId, String userToken);

	public OpenDataMember manipulateOrgMembers(String orgId, String userId, String role, String userToken);

	public String getOrganizationId(String identification);

}

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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.business.services.opendata.OpenDataPermissions;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataAuthorizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataMember;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganizationDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataOrganizationAuthorizationDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataOrganizationAuthorizationResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataOrganizationResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.opendata.model.OpenDataRole;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Organizations Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"), @ApiResponse(responseCode = "401", description = "Unathorized"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/opendata/organizations")
@Slf4j
public class OpenDataOrganizationsManagementController {

	private static final String MSG_ERROR_JSON_RESPONSE = "{\"error\":\"%s\"}";
	private static final String MSG_OK_JSON_RESPONSE = "{\"ok\":\"%s\"}";
	private static final String MSG_USER_UNAUTHORIZED = "User is unauthorized";
	private static final String MSG_ORGANIZATION_NOT_EXIST = "Organization does not exist";
	private static final String MSG_ORGANIZATION_EXISTS = "Organization already exists";
	private static final String MSG_ORGANIZATION_DELETED = "Organization has been deleted succesfully";
	private static final String MSG_USER_NOT_EXIST = "User does not exist in Open Data Portal";
	private static final String MSG_ORGANIZATION_AUTHORIZATION_DELETED = "Organization authorization has been deleted succesfully";
	private static final String MSG_FILE_MAX_SIZE_ALLOWED = "File size is larger than max allowed";
	private static final String MSG_FILE_EXTENSION_NOT_ALLOWED = "File extension forbidden";
	private static final String MSG_ORGANIZATION_AUTHORIZATION_NOT_EXIST = "Organization authorization does not exist";

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private OpenDataPermissions openDataPermissions;
	@Autowired
	private UserService userService;

	@Operation(summary = "Get all organizations")
	@GetMapping("")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataOrganizationResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> getAll() {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();

			final List<OpenDataOrganization> organizationsFromUser = organizationService
					.getOrganizationsFromUser(userToken);
			if (organizationsFromUser == null || organizationsFromUser.isEmpty()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}

			final List<OpenDataOrganizationResponseDTO> organizations = new ArrayList<>();

			organizationsFromUser.forEach(o -> organizations.add(new OpenDataOrganizationResponseDTO(o)));
			return new ResponseEntity<>(organizations, HttpStatus.OK);
		} catch (final Exception e) {
			log.error(String.format("Error getting organization list: %s ", e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get organization by id")
	@GetMapping("/{id}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataOrganizationResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> get(
			@Parameter(description= "Organization id", required = true) @PathVariable("id") String organizationId) {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToShowOrganization(organization, userId)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}
			final OpenDataOrganizationResponseDTO organizationDTO = new OpenDataOrganizationResponseDTO(organization,
					userId);
			return new ResponseEntity<>(organizationDTO, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Error getting organization %s: %s", organizationId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Create new organization")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataOrganizationResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> create(
			@RequestParam(required = true, value = "Organization identification") String identification,
			@RequestParam(required = false, value = "Organization description") String description,
			@RequestPart(value = "file", required = false) MultipartFile file) {

		try {
			if (file != null && utils.isFileExtensionForbidden(file)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_FILE_MAX_SIZE_ALLOWED),
						HttpStatus.BAD_REQUEST);
			}
			if (file != null && file.getSize() > utils.getMaxFileSizeAllowed().longValue()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_FILE_EXTENSION_NOT_ALLOWED),
						HttpStatus.BAD_REQUEST);
			}
			if (!utils.isAdministrator()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final String organizationId = organizationService.getOrganizationId(identification);
			final OpenDataOrganizationDTO organizationDTO = new OpenDataOrganizationDTO(organizationId, identification,
					description);
			if (organizationService.existsOrganization(organizationDTO, userToken)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_EXISTS),
						HttpStatus.BAD_REQUEST);
			}

			final OpenDataOrganization newOrganization = organizationService.createOrganization(organizationDTO, file,
					userToken);
			final OpenDataOrganizationResponseDTO organizationResponseDTO = new OpenDataOrganizationResponseDTO(
					newOrganization, userId);

			return new ResponseEntity<>(organizationResponseDTO, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot create organization %s: %s", identification, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Deprecated
	@Operation(summary = "Update an existing organization")
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataOrganizationResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> update(
			@RequestParam(required = true, value = "Organization id") String organizationId,
			@RequestParam(required = true, value = "Organization identification") String identification,
			@RequestParam(required = false, value = "Organization description") String description,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		try {
			if (file != null && utils.isFileExtensionForbidden(file)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_FILE_MAX_SIZE_ALLOWED),
						HttpStatus.BAD_REQUEST);
			}
			if (file != null && file.getSize() > utils.getMaxFileSizeAllowed().longValue()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_FILE_EXTENSION_NOT_ALLOWED),
						HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateOrganization(userId, organization)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}

			final OpenDataOrganizationDTO organizationDTO = createOrganizationUpdateObject(organization, description);
			final OpenDataOrganization newOrganization = organizationService.updateOrganization(organizationDTO, file,
					userToken);
			final OpenDataOrganizationResponseDTO organizationResponseDTO = new OpenDataOrganizationResponseDTO(
					newOrganization, userId);

			return new ResponseEntity<>(organizationResponseDTO, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot update organization %s: %s", identification, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update an existing organization")
	@PostMapping(value="update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataOrganizationResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> updateOrganization(
			@RequestParam(required = true, value = "Organization id") String organizationId,
			@RequestParam(required = true, value = "Organization identification") String identification,
			@RequestParam(required = false, value = "Organization description") String description,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		try {
			if (file != null && utils.isFileExtensionForbidden(file)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_FILE_MAX_SIZE_ALLOWED),
						HttpStatus.BAD_REQUEST);
			}
			if (file != null && file.getSize() > utils.getMaxFileSizeAllowed().longValue()) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_FILE_EXTENSION_NOT_ALLOWED),
						HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateOrganization(userId, organization)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}

			final OpenDataOrganizationDTO organizationDTO = createOrganizationUpdateObject(organization, description);
			final OpenDataOrganization newOrganization = organizationService.updateOrganization(organizationDTO, file,
					userToken);
			final OpenDataOrganizationResponseDTO organizationResponseDTO = new OpenDataOrganizationResponseDTO(
					newOrganization, userId);

			return new ResponseEntity<>(organizationResponseDTO, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot update organization %s: %s", identification, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete organization by id")
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> delete(
			@Parameter(description= "Organization id", required = true) @PathVariable("id") String organizationId) {
		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateOrganization(userId, organization)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}

			organizationService.deleteOrganization(userToken, organizationId);
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_ORGANIZATION_DELETED), HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot deleted organization %s: %s", organizationId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get users access authorizations for an organization by organization id")
	@GetMapping("/{id}/authorizations")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=OpenDataOrganizationAuthorizationResponseDTO.class)), responseCode = "200", description = "Ok"))
	public ResponseEntity<Object> getOrganizationAuthorizations(
			@Parameter(description= "Organization id", required = true) @PathVariable("id") String organizationId) {

		try {
			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateOrganization(userId, organization)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}
			final List<OpenDataAuthorizationDTO> authorizationList = organizationService
					.getDTOAuthorizations(organizationId, userToken);
			final List<OpenDataOrganizationAuthorizationResponseDTO> authorizations = new ArrayList<>();

			authorizationList.forEach(o -> authorizations.add(new OpenDataOrganizationAuthorizationResponseDTO(o)));
			return new ResponseEntity<>(authorizations, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot get organization authorizations %s: %s", organizationId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Set user access authorizations for an organization by organization id")
	@PostMapping("/{id}/authorizations")
	public ResponseEntity<Object> setOrganizationAuthorizations(
			@Parameter(description= "Organization id", required = true) @PathVariable("id") String organizationId,
			@Valid @RequestBody OpenDataOrganizationAuthorizationDTO organizationAuth) {

		try {
			if (organizationAuth.getUserId() == null || organizationAuth.getUserRole() == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE,
						"Missing required fields. Required = [userId, userRole]"), HttpStatus.BAD_REQUEST);
			}

			final User user = userService.getUser(organizationAuth.getUserId());
			if (user == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_NOT_EXIST),
						HttpStatus.BAD_REQUEST);
			}

			if (!openDataPermissions.isUserValidForRole(organizationAuth.getUserId(),
					organizationAuth.getUserRole().toString().toLowerCase())) {
				return new ResponseEntity<>(
						String.format(MSG_ERROR_JSON_RESPONSE, "User can only be a member of the organization"),
						HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final String userId = utils.getUserId();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateOrganization(userId, organization)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}

			final OpenDataMember member = organizationService.manipulateOrgMembers(organizationId,
					organizationAuth.getUserId(), organizationAuth.getUserRole().toString().toLowerCase(), userToken);
			if (member == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_NOT_EXIST),
						HttpStatus.BAD_REQUEST);
			}
			organizationService.updateOrganizationResourcesPermissions(organizationId, userToken);
			final OpenDataOrganizationAuthorizationResponseDTO newOrganizationAuth = new OpenDataOrganizationAuthorizationResponseDTO(
					user.getUserId(), user.getFullName(), OpenDataRole.valueOf(member.getRole().toUpperCase()));
			return new ResponseEntity<>(newOrganizationAuth, HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format(
					String.format("Cannot set organization authorizations %s: %s", organizationId, e.getMessage())));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete user access authorization for an organization by organization id and user id")
	@DeleteMapping("/{id}/authorizations/{userId}")
	public ResponseEntity<String> deleteOrganizationAuthorizations(
			@Parameter(description= "Organization id", required = true) @PathVariable("id") String organizationId,
			@Parameter(description= "User id", required = true) @PathVariable("userId") String userId) {
		try {
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_NOT_EXIST),
						HttpStatus.BAD_REQUEST);
			}

			final String userToken = utils.getUserOauthTokenByCurrentHttpRequest();
			final OpenDataOrganization organization = organizationService.getOrganizationById(userToken,
					organizationId);
			if (organization == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_NOT_EXIST),
						HttpStatus.NOT_FOUND);
			}
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateOrganization(utils.getUserId(), organization)) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_UNAUTHORIZED),
						HttpStatus.FORBIDDEN);
			}
			if (organization.getUsers().stream().filter(u -> userId.equals(u.getName())).findAny()
					.orElse(null) == null) {
				return new ResponseEntity<>(
						String.format(MSG_ERROR_JSON_RESPONSE, MSG_ORGANIZATION_AUTHORIZATION_NOT_EXIST),
						HttpStatus.BAD_REQUEST);
			}

			final OpenDataMember member = organizationService.manipulateOrgMembers(organizationId, userId, null,
					userToken);
			if (member == null) {
				return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_USER_NOT_EXIST),
						HttpStatus.BAD_REQUEST);
			}

			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_ORGANIZATION_AUTHORIZATION_DELETED),
					HttpStatus.OK);
		} catch (final OpenDataServiceException e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(String.format("Cannot set organization authorizations %s: %s", organizationId, e.getMessage()));
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private OpenDataOrganizationDTO createOrganizationUpdateObject(OpenDataOrganization oldOrganization,
			String description) {
		final OpenDataOrganizationDTO newOrganization = new OpenDataOrganizationDTO(oldOrganization.getId(),
				oldOrganization.getName(), oldOrganization.getTitle(), description);
		if (description == null) {
			newOrganization.setDescription(oldOrganization.getDescription());
		}
		return newOrganization;
	}
}

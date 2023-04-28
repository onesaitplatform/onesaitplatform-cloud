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
package com.minsait.onesait.platform.controlpanel.rest.management.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.business.services.api.APIBusinessService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Api.ClientJS;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageResponse;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeApi;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeException;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiDTOConverter;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiResponseErrorDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiRestDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiSimplifiedResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.UserApiSimplifiedInputDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.UserApiSimplifiedResponseDTO;
import com.minsait.onesait.platform.controlpanel.services.gravitee.GraviteeService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Tag(name = "APIs management")
@RestController
@RequestMapping("api/apis")
public class APIManagementController {

	@Autowired
	ClientPlatformService clientPlatformService;
	@Autowired
	ApiManagerService apiManagerService;
	@Autowired
	UserService userService;
	@Autowired
	UserTokenService userTokenService;
	@Autowired
	OntologyService ontologyService;
	@Autowired
	IntegrationResourcesService resourcesService;
	@Autowired
	AppWebUtils utils;
	@Autowired
	JWTService jwtService;
	@Autowired
	ApiDTOConverter apiDTOConverter;

	@Autowired(required = false)
	private GraviteeService graviteeService;

	private static final String ERROR_API_NOT_FOUND = "Api not found";
	private static final String ERROR_USER_NOT_ALLOWED = "User is not authorized";
	private static final String ERROR_USER_ACCESS_NOT_FOUND = "User access not found";
	private static final String ERROR_MISSING_ONTOLOGY = "Missing Ontology";
	private static final String ERROR_MISSING_API_IDENTIFICATION = "Missing Api identification";
	private static final String ERROR_API_IDENTIFICATION_FORMAT = "Identification Error: Use alphanumeric characters and '-', '_'";
	private static final String ERROR_MISSING_OPERATIONS = "Missing operations";
	private static final String ERROR_API_INVALID_STATE = "Api state not valid";
	private static final String EMPTY_RESPONSE_APIS = "{\"apis\" : \"\"}";

	@Operation(summary= "Get users access to api by identification or id")
	@GetMapping(value = "/{apiId}/authorizations")
	public ResponseEntity<?> getAuthorizations(
			@Parameter(description= "Api identification or id") @PathVariable(value = "apiId") String apiId,
			@Parameter(description= "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {

		ResponseEntity<?> response;
		try {
			final User loggedUser = userService.getUser(utils.getUserId());
			final List<UserApi> usersapi = apiManagerService.getAuthorizations(apiId, apiVersion, loggedUser);
			final List<UserApiSimplifiedResponseDTO> usersapiDto = new ArrayList<>();
			for (final UserApi ua : usersapi) {
				usersapiDto.add(new UserApiSimplifiedResponseDTO(ua));
			}
			response = new ResponseEntity<>(usersapiDto, HttpStatus.OK);
		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			response = new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Operation(summary= "Create users access to api by identification or id")
	@PostMapping(value = "/{apiId}/authorizations")
	public ResponseEntity<?> createAuthorizations(
			@Parameter(description= "Api identification or id") @PathVariable(value = "apiId") String apiId,
			@Parameter(description= "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@Valid @RequestBody List<UserApiSimplifiedInputDTO> userApiAccesses) {

		ResponseEntity<?> response;
		try {
			final User loggedUser = userService.getUser(utils.getUserId());
			final List<String> usersId = userApiAccesses.stream().map(UserApiSimplifiedInputDTO::getUserId)
					.collect(Collectors.toList());

			final List<String> created = apiManagerService.updateAuthorizations(apiId, apiVersion, usersId, loggedUser);

			final JSONObject responseInfo = new JSONObject();
			final Iterator<String> i1 = usersId.iterator();
			final Iterator<String> i2 = created.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i1.next(), i2.next());
			}
			response = new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);
		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			response = new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Operation(summary= "Remove users access to api by identification or id")
	@DeleteMapping(value = "/{apiId}/authorizations")
	public ResponseEntity<?> removeAuthorizations(
			@Parameter(description= "Api identification or id") @PathVariable(value = "apiId") String apiId,
			@Parameter(description= "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@Valid @RequestBody List<UserApiSimplifiedInputDTO> userApiAccesses) {

		ResponseEntity<?> response;
		try {
			final User loggedUser = userService.getUser(utils.getUserId());
			final List<String> usersId = userApiAccesses.stream().map(UserApiSimplifiedInputDTO::getUserId)
					.collect(Collectors.toList());

			final List<String> removed = apiManagerService.removeAuthorizations(apiId, apiVersion, usersId, loggedUser);

			final JSONObject responseInfo = new JSONObject();
			final Iterator<String> i1 = usersId.iterator();
			final Iterator<String> i2 = removed.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i1.next(), i2.next());
			}
			response = new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);
		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			response = new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Operation(summary= "Authorize user for api by identification or id")
	@PostMapping(value = "/authorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> authorize(
			@Parameter(description= "Api identification or id", required = true) @PathVariable("apiId") String apiId,
			@Parameter(description= "User", required = true) @PathVariable(name = "userId") String userId,
			@Parameter(description= "Version required if use identification (if not present it applies to all versions)", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@RequestHeader("Authorization") String authorization) {

		ResponseEntity<?> response;
		final List<UserApiSimplifiedResponseDTO> usersapiDto = new ArrayList<>();

		final String loggedUserId = utils.getUserId();
		final User loggedUser = userService.getUser(loggedUserId);

		try {
			final com.minsait.onesait.platform.config.model.Api api = apiManagerService
					.getApiByIdentificationVersionOrId(apiId, apiVersion);

			if (api != null) {
				if (!apiManagerService.hasUserEditAccess(api, loggedUser)) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
							ERROR_USER_NOT_ALLOWED);
				}
				final UserApi userapi = apiManagerService.updateAuthorization(api.getId(), userId);
				usersapiDto.add(new UserApiSimplifiedResponseDTO(userapi));
			} else {
				final List<com.minsait.onesait.platform.config.model.Api> apiAllVersions = apiManagerService
						.getApisOfOwnerAndIdentification(loggedUser, apiId);
				if (apiAllVersions.isEmpty()) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.NOT_FOUND,
							ERROR_API_NOT_FOUND);
				}
				// update api+version when possible, else: skip
				final List<UserApi> userapis = apiManagerService.updateAuthorizationAllVersions(apiId, userId,
						loggedUser);
				for (final UserApi ua : userapis) {
					usersapiDto.add(new UserApiSimplifiedResponseDTO(ua));
				}
			}

			if (usersapiDto.isEmpty()) {
				response = new ResponseEntity<>(usersapiDto, HttpStatus.NO_CONTENT);
			} else {
				response = new ResponseEntity<>(usersapiDto, HttpStatus.CREATED);
			}

		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final NullPointerException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO();
			errorDTO.setError(ApiManagerServiceException.Error.NOT_FOUND.name());
			errorDTO.setMsg(ERROR_API_NOT_FOUND);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return response;

	}

	@Operation(summary= "Deauthorize user for api by identification or id")
	@PostMapping(value = "/deauthorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> deauthorize(
			@Parameter(description= "Api identification or id", required = true) @PathVariable("apiId") String apiId,
			@Parameter(description= "User", required = true) @PathVariable(name = "userId") String userId,
			@Parameter(description= "Version required if use identification (if not present it applies to all versions)", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@RequestHeader("Authorization") String authorization) {

		ResponseEntity<?> response;

		final String loggedUserId = utils.getUserId();
		final User loggedUser = userService.getUser(loggedUserId);

		try {
			final com.minsait.onesait.platform.config.model.Api api = apiManagerService
					.getApiByIdentificationVersionOrId(apiId, apiVersion);

			if (api != null) {
				if (!apiManagerService.hasUserEditAccess(api, loggedUser)) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
							ERROR_USER_NOT_ALLOWED);
				}
				final UserApi userapi = apiManagerService.getUserApiByIdAndUser(api.getId(), userId);
				if (userapi == null) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_ACCESS_NOT_FOUND,
							ERROR_USER_ACCESS_NOT_FOUND);
				}
				apiManagerService.removeAuthorizationById(userapi.getId());

			} else {
				apiManagerService.removeAuthorizationAllVersions(apiId, userId, loggedUser);
			}
			response = new ResponseEntity<>("{\"status\": \"ok\"}", HttpStatus.OK);

		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final NullPointerException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO();
			errorDTO.setError(ApiManagerServiceException.Error.NOT_FOUND.name());
			errorDTO.setMsg(ERROR_API_NOT_FOUND);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@Operation(summary= "Get user token for api")
	@GetMapping(value = "/api/token")
	public ResponseEntity<String> getApiToken() {
		final List<UserToken> tokens = userService.getUserToken(userService.getUser(utils.getUserId()));
		if (!tokens.isEmpty()) {
			tokens.sort(Comparator.comparing(UserToken::getCreatedAt).reversed());
			return new ResponseEntity<>("{\"userToken\" : \"" + tokens.get(0).getToken() + "\"}", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("{\"userToken\" : \"\"}", HttpStatus.OK);
		}
	}

	@Operation(summary= "Get username for api by user token")
	@GetMapping(value = "/api/username/{token}")
	public ResponseEntity<String> getApiUsernameByToken(
			@Parameter(description= "Token Id ", required = true) @PathVariable("token") String token) {
		String username = null;
		try {
			username = userService.getUserByToken(token).getUserId();
		} catch (final NullPointerException e) {
			return new ResponseEntity<>("The token \"" + token + "\" does not belong to any user.",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{\"username\" : \"" + username + "\"}", HttpStatus.OK);
	}

	@Operation(summary= "Get user tokens for api")
	@GetMapping(value = "/api/tokens")
	public ResponseEntity<?> getApiTokens() {

		final List<UserToken> tokens = userService.getUserToken(userService.getUser(utils.getUserId()));
		if (!tokens.isEmpty()) {
			tokens.sort(Comparator.comparing(UserToken::getCreatedAt).reversed());
			final List<String> tokenList = new ArrayList<>();
			for (final UserToken token : tokens) {
				tokenList.add(token.getToken());
			}
			final JSONObject response = new JSONObject();
			response.put("userTokens", tokenList);
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("{\"userToken\" : \"\"}", HttpStatus.OK);
		}

	}

	@Operation(summary= "Generate new user token for api")
	@PostMapping(value = "/api/token")
	public ResponseEntity<String> generateApiToken() {
		try {
			apiManagerService.generateToken(utils.getUserId());
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return getApiToken();

	}

	@Operation(summary= "Delete user token for api")
	@DeleteMapping(value = "/api/token/{token}")
	public ResponseEntity<String> deleteApiToken(
			@Parameter(description= "Token Id ", required = true) @PathVariable("token") String token) {
		final UserToken tokenOj = userTokenService.getTokenByUserAndToken(userService.getUser(utils.getUserId()),
				token);

		if (tokenOj == null) {
			return new ResponseEntity<>("Token with id " + token + " does not exist", HttpStatus.BAD_REQUEST);
		}
		try {
			apiManagerService.removeToken(utils.getUserId(), "{\"token\":\"" + token + "\"}");
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@Operation(summary= "Get list of user apis and public apis")
	@GetMapping
	public ResponseEntity<?> getApiList() {
		final User user = userService.getUser(utils.getUserId());
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService.loadAPISByFilter("", "", "",
				utils.getUserId());
		if (!apis.isEmpty()) {
			final ArrayList<ApiRestDTO> apisdto = new ArrayList<>();
			for (final com.minsait.onesait.platform.config.model.Api api : apis) {
				final ApiRestDTO apidto = apiDTOWithOperationsAndAuthorizations(api, user, true);
				apisdto.add(apidto);
			}
			return new ResponseEntity<>(apisdto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(EMPTY_RESPONSE_APIS, HttpStatus.NO_CONTENT);
		}

	}

	@Operation(summary= "Get api by identification or id")
	@GetMapping(value = "/{id}")
	public ResponseEntity<?> getApiByIdentification(
			@Parameter(description= "Api identification or id", required = true) @PathVariable("id") String apiId,
			@Parameter(description= "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {
		final com.minsait.onesait.platform.config.model.Api api = apiManagerService
				.getApiByIdentificationVersionOrId(apiId, apiVersion);
		if (api != null) {
			if (apiManagerService.hasUserAccess(api.getId(), utils.getUserId())) {
				final User user = userService.getUser(utils.getUserId());
				final ApiRestDTO apidto = apiDTOWithOperationsAndAuthorizations(api, user, true);
				return new ResponseEntity<>(apidto, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(EMPTY_RESPONSE_APIS, HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary= "Delete api by identification or id")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteApi(
			@Parameter(description= "Api identification or id", required = true) @PathVariable("id") String apiId,
			@Parameter(description= "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {
		try {
			final com.minsait.onesait.platform.config.model.Api api = apiManagerService
					.getApiByIdentificationVersionOrId(apiId, apiVersion);
			if (api == null) {
				return new ResponseEntity<>("Api \"" + apiId + "\" does not exist", HttpStatus.NOT_FOUND);
			}
			final User user = userService.getUser(utils.getUserId());
			if (!apiManagerService.hasUserEditAccess(api.getId(), user.getUserId())) {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
			apiManagerService.removeAPI(api.getId());
		} catch (final Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Api deleted successfully", HttpStatus.OK);
	}

	@Operation(summary= "Create new API")
	@PostMapping
	public ResponseEntity<?> createApi(
			@Parameter(description= "APIBody", required = true) @Valid @RequestBody ApiRestDTO apiBody) {
		com.minsait.onesait.platform.config.model.Api createdApi = null;
		List<com.minsait.onesait.platform.config.model.Api> existingApisWithIdentificationAndUser;

		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
					ERROR_USER_NOT_ALLOWED);
		}
		ApiStates state = ApiStates.CREATED;
		try {
			if (StringUtils.hasText(apiBody.getStatus())) {
				state = ApiStates.valueOf(apiBody.getStatus());
			}
			if (!state.equals(ApiStates.CREATED) && !state.equals(ApiStates.DEVELOPMENT)) {
				state = ApiStates.CREATED;
			}
		} catch (final Exception e) {
			log.debug("ApiState not valid, falling back to CREATED");
		}
		try {
			// build api from body
			if (!apiBody.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.API_IDENTIFICATION_FORMAT_ERROR,
						ERROR_API_IDENTIFICATION_FORMAT);
			}

			final com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody, user, state);

			List<com.minsait.onesait.platform.config.model.ApiOperation> operations;
			if (apiBody.getOperations() == null) {
				operations = null;
			} else {
				operations = apiDTOConverter.toAPIOperations(apiBody.getOperations(),
						new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), api);
			}
			final List<UserApi> auths = apiDTOConverter.toUserApi(apiBody.getAuthentications(),
					new ArrayList<UserApi>(), api);

			// search if exists by identification + version
			existingApisWithIdentificationAndUser = apiManagerService.getApisOfOwnerAndIdentification(user,
					api.getIdentification());

			if (existingApisWithIdentificationAndUser.isEmpty()) {
				api.setUser(user);
				createdApi = apiManagerService.createApiRest(api, operations, auths);
			} else {
				createdApi = apiManagerService.versionateApiRest(api, operations, auths, user);
			}
			if (graviteeService != null && apiBody.getPublishInGravitee() != null && apiBody.getPublishInGravitee()) {
				publish2Gravitee(createdApi.getId());
			}

		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			return new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(new ApiSimplifiedResponseDTO(createdApi), HttpStatus.OK);
	}

	@Operation(summary= "Update API")
	@PutMapping
	public ResponseEntity<?> updateApi(
			@Parameter(description= "APIBody", required = true) @Valid @RequestBody ApiRestDTO apiBody) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}

		final com.minsait.onesait.platform.config.model.Api apimemory = apiManagerService
				.getApiByIdentificationVersionOrId(apiBody.getIdentification(), String.valueOf(apiBody.getVersion()));

		if (apimemory == null) {
			return new ResponseEntity<>(
					"Api \"" + apiBody.getIdentification() + " - v" + apiBody.getVersion() + "\" does not exist",
					HttpStatus.NOT_FOUND);
		}

		if (!apiManagerService.isApiStateValidForEdit(apimemory)) {
			return new ResponseEntity<>(ERROR_API_INVALID_STATE, HttpStatus.FORBIDDEN);
		}

		try {

			final com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody, user,
					ApiStates.valueOf(apiBody.getStatus()));

			if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
				return new ResponseEntity<>(ERROR_MISSING_ONTOLOGY, HttpStatus.BAD_REQUEST);
			}
			if (api.getIdentification() == null || api.getIdentification().equals("")) {
				return new ResponseEntity<>(ERROR_MISSING_API_IDENTIFICATION, HttpStatus.BAD_REQUEST);
			}

			if (apiBody.getOperations() == null) {
				return new ResponseEntity<>(ERROR_MISSING_OPERATIONS, HttpStatus.BAD_REQUEST);
			}
			final List<com.minsait.onesait.platform.config.model.ApiOperation> operations = apiDTOConverter
					.toAPIOperations(apiBody.getOperations(),
							new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), apimemory);

			final List<UserApi> auths = apiDTOConverter.toUserApi(apiBody.getAuthentications(),
					new ArrayList<UserApi>(), apimemory);

			final String apiId = apiManagerService.updateApiRest(api, apimemory, operations, auths, false);
			api.setId(apiId); // to print in result update (retrocomp)

			if (graviteeService != null && apiBody.getPublishInGravitee() != null && apiBody.getPublishInGravitee()) {
				publish2Gravitee(apiId);
			}

			return new ResponseEntity<>(new ApiSimplifiedResponseDTO(api), HttpStatus.OK);

		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary= "Change api state by identification or id")
	@PostMapping(value = "/changestate/{id}/{state}")
	public ResponseEntity<?> changeStateByIdentification(
			@Parameter(description= "Api identification or id", required = true) @PathVariable("id") String apiId,
			@Parameter(description= "Api state", required = true) @PathVariable("state") String state,
			@Parameter(description= "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {
		final com.minsait.onesait.platform.config.model.Api api = apiManagerService
				.getApiByIdentificationVersionOrId(apiId, apiVersion);
		if (api != null) {
			if (apiManagerService.hasUserAccess(api.getId(), utils.getUserId())) {
				final User user = userService.getUser(utils.getUserId());
				if (!apiManagerService.validateState(api.getState(), state)) {
					return new ResponseEntity<>("\"Forbidden change of state\"", HttpStatus.FORBIDDEN);
				}
				apiManagerService.updateState(api.getId(), state.toUpperCase());
				api.setState(ApiStates.valueOf(state.toUpperCase()));
				final ApiRestDTO apidto = apiDTOWithOperationsAndAuthorizations(api, user, true);
				return new ResponseEntity<>(apidto, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
		}

	}

	private HttpHeaders exportHeaders(String apiNameFile) {
		final HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Disposition", "attachment; filename=\"" + apiNameFile + ".json\"");
		return headers;
	}

	private ApiRestDTO apiDTOWithOperationsAndAuthorizations(com.minsait.onesait.platform.config.model.Api api,
			User user, boolean allowEditUsers) {
		final List<com.minsait.onesait.platform.config.model.ApiOperation> apiops = apiManagerService
				.getOperations(api);

		List<UserApi> usersapi = new ArrayList<>();
		if (apiManagerService.isUserOwnerOrAdmin(user, api)
				|| allowEditUsers && apiManagerService.hasUserEditAccess(api, user)) {
			usersapi = apiManagerService.getUserApiByApiId(api.getId());
		}

		if (api.getGraviteeId() == null) {
			return new ApiRestDTO(api, apiops, usersapi, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		} else {
			return new ApiRestDTO(api, apiops, usersapi, resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY));
		}
	}

	@Operation(summary= "Export api by identification or id")
	@GetMapping(value = "export/{id}")
	public ResponseEntity<?> exportApiByIdentification(
			@Parameter(description= "Api identification or id", required = true) @PathVariable("id") String apiId,
			@Parameter(description= "Version required if use identification (if not present it applies to all versions)", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {

		final User user = userService.getUser(utils.getUserId());
		final com.minsait.onesait.platform.config.model.Api api = apiManagerService
				.getApiByIdentificationVersionOrId(apiId, apiVersion);
		if (api != null) {
			if (apiManagerService.hasUserAccess(api.getId(), utils.getUserId())) {
				final ApiRestDTO apidto = apiDTOWithOperationsAndAuthorizations(api, user, false);
				final HttpHeaders headers = exportHeaders(api.getIdentification() + "_V" + api.getNumversion());
				return new ResponseEntity<>(apidto, headers, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
		} else {
			if (!apiVersion.equals("")) {
				return new ResponseEntity<>(ERROR_API_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final List<com.minsait.onesait.platform.config.model.Api> apiAllVersions = apiManagerService
					.getApisOfOwnerAndIdentification(user, apiId);

			if (apiAllVersions.isEmpty()) {
				return new ResponseEntity<>(EMPTY_RESPONSE_APIS, HttpStatus.NOT_FOUND);
			}

			final List<ApiRestDTO> allVersionsDTO = new ArrayList<>();
			for (final com.minsait.onesait.platform.config.model.Api apiVers : apiAllVersions) {
				if (apiManagerService.hasUserAccess(apiVers, user)) {
					final ApiRestDTO apidtoVersion = apiDTOWithOperationsAndAuthorizations(apiVers, user, false);
					allVersionsDTO.add(apidtoVersion);
				}

			}
			final HttpHeaders headers = exportHeaders(allVersionsDTO.get(0).getIdentification());
			return new ResponseEntity<>(allVersionsDTO, headers, HttpStatus.OK);
		}

	}

	@Operation(summary= "Export all apis")
	@GetMapping(value = "export/")
	public ResponseEntity<?> exportAllApis() {
		final List<ApiRestDTO> apisDTO = new ArrayList<>();
		final User user = userService.getUser(utils.getUserId());
		List<com.minsait.onesait.platform.config.model.Api> apis;
		if (userService.isUserAdministrator(user)) {
			apis = apiManagerService.getAllApis(user);
		} else {
			apis = apiManagerService.getApisOfOwner(user);
		}

		if (!apis.isEmpty()) {
			for (final com.minsait.onesait.platform.config.model.Api api : apis) {
				final ApiRestDTO apidto = apiDTOWithOperationsAndAuthorizations(api, user, false);
				apisDTO.add(apidto);
			}

			final HttpHeaders headers = exportHeaders(user.getUserId() + "_apis");
			return new ResponseEntity<>(apisDTO, headers, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(EMPTY_RESPONSE_APIS, HttpStatus.NOT_FOUND);
		}

	}

	private User getImportingUser(ApiRestDTO apiDTO, User loggedUser) {
		User importingUser = userService.getUser(apiDTO.getUserId());
		if (!(userService.isUserAdministrator(loggedUser) && importingUser != null)) {
			importingUser = loggedUser;
		}
		return importingUser;
	}

	@Operation(summary= "Import API")
	@PostMapping(value = "import")
	public ResponseEntity<?> importApi(
			@Parameter(description= "Overwrite api if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@Parameter(description= "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@Parameter(description= "APIBody", required = true) @Valid @RequestBody ApiRestDTO apiBody) {

		// ------ Importing rules --------
		// version ---------------------OK (import any version allowed to not change
		// endpoints in migration)

		final User user = userService.getUser(utils.getUserId());
		com.minsait.onesait.platform.config.model.Api createdApi = null;

		try {
			final com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody,
					getImportingUser(apiBody, user), ApiStates.valueOf(ApiStates.class, apiBody.getStatus()));

			List<com.minsait.onesait.platform.config.model.ApiOperation> operations;
			if (apiBody.getOperations() == null) {
				operations = null;
			} else {
				operations = apiDTOConverter.toAPIOperations(apiBody.getOperations(),
						new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), api);
			}
			List<UserApi> auths = new ArrayList<>();
			if (importAuthorizations) {
				auths = apiDTOConverter.toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(), api);
			}
			createdApi = apiManagerService.importApiRest(api, operations, auths, overwrite, user.getUserId());

		} catch (final ApiManagerServiceException e) {
			final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			return new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new ApiSimplifiedResponseDTO(createdApi), HttpStatus.OK);
	}

	@Operation(summary= "Import several APIs")
	@PostMapping(value = "import/apis/")
	public ResponseEntity<?> importSeveralApis(
			@Parameter(description= "Overwrite api if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@Parameter(description= "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@Parameter(description= "APIBodies", required = true) @Valid @RequestBody List<ApiRestDTO> apiBodies) {

		// ------ Importing rules --------
		// version ---------------------OK (import any version allowed to not change
		// endpoints in migration)

		final User user = userService.getUser(utils.getUserId());

		final List<ApiSimplifiedResponseDTO> importedAPIsResponse = new ArrayList<>();
		for (final ApiRestDTO apiBody : apiBodies) {
			final com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody,
					getImportingUser(apiBody, user), ApiStates.valueOf(ApiStates.class, apiBody.getStatus()));

			try {
				com.minsait.onesait.platform.config.model.Api createdApi = null;
				List<com.minsait.onesait.platform.config.model.ApiOperation> operations;
				if (apiBody.getOperations() == null) {
					operations = null;
				} else {
					operations = apiDTOConverter.toAPIOperations(apiBody.getOperations(),
							new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), api);
				}
				List<UserApi> auths = new ArrayList<>();
				if (importAuthorizations) {
					auths = apiDTOConverter.toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(), api);
				}

				createdApi = apiManagerService.importApiRest(api, operations, auths, overwrite, user.getUserId());
				importedAPIsResponse.add(new ApiSimplifiedResponseDTO(createdApi));
			} catch (final ApiManagerServiceException e) {
				final ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
				importedAPIsResponse.add(new ApiSimplifiedResponseDTO(api.getId(), api.getIdentification(),
						api.getNumversion(), errorDTO.getMsg()));
			} catch (final Exception e) {
				importedAPIsResponse.add(new ApiSimplifiedResponseDTO(api.getId(), api.getIdentification(),
						api.getNumversion(), "Error importing API"));
			}
		}

		return new ResponseEntity<>(importedAPIsResponse, HttpStatus.OK);
	}

	@Autowired
	private APIBusinessService apiBusinessService;

	@Operation(summary= "Client")
	@PostMapping("client-js")
	public ResponseEntity<ByteArrayResource> generateClientJS(
			@Parameter(description= "Target JS framework") @RequestParam("framework") ClientJS framework,
			@Parameter(description= "List of API ids") @RequestBody List<String> ids) throws IOException {
		try {
			final File file = apiBusinessService.generateJSClient(framework, ids, utils.getUserId());
			final Path path = Paths.get(file.getAbsolutePath());
			final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
			FileUtils.cleanDirectory(new File(file.getParent()));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(file.getName()))
					.header(HttpHeaders.SET_COOKIE, "fileDownload=true")
					.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate")
					.contentLength(resource.contentLength())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (final Exception e) {
			log.error("Error while generating JS client", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping(value = "/gravitee/update/swagger", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> updateGraviteeSwagger(@RequestParam(name="apiId") String apiId, @RequestParam(required=false, name="content") String content) {
		try {
			if (!apiManagerService.hasUserEditAccess(apiId, utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			updateGraviteeSwaggerDoc(apiId, content);

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException | GenericOPException  e) {
			log.error("Error updating Gravitee Swagger documentation : {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	private ApiPageResponse updateGraviteeSwaggerDoc(String apiId, String content) throws GenericOPException {
		final com.minsait.onesait.platform.config.model.Api apiDb = apiManagerService.getById(apiId);
		if (graviteeService != null && apiManagerService.isGraviteeApi(apiId)) {
			return graviteeService.processUpdateAPIDocs(apiDb, !StringUtils.hasText(content) ? apiDb.getSwaggerJson() : content);
		}
		return new ApiPageResponse();
	}


	private void publish2Gravitee(String apiId) throws GenericOPException {
		final com.minsait.onesait.platform.config.model.Api apiDb = apiManagerService.getById(apiId);
		try {
			final GraviteeApi graviteeApi = graviteeService.processApi(apiDb);
			apiDb.setGraviteeId(graviteeApi.getApiId());
			apiManagerService.updateApi(apiDb);
		} catch (final GraviteeException e) {
			log.error("Could not publish API to Gravitee {}", e.getMessage());
		}
	}

}

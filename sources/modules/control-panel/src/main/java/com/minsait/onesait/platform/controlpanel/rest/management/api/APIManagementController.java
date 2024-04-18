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
package com.minsait.onesait.platform.controlpanel.rest.management.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
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
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiDTOConverter;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiResponseErrorDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiRestDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.ApiSimplifiedResponseDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.UserApiSimplifiedInputDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.api.model.UserApiSimplifiedResponseDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "APIs management", tags = { "APIs management service" })
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

	private static final String ERROR_API_NOT_FOUND = "Api not found";
	private static final String ERROR_USER_NOT_ALLOWED = "User is not authorized";
	private static final String ERROR_USER_ACCESS_NOT_FOUND = "User access not found";
	private static final String ERROR_MISSING_ONTOLOGY = "Missing Ontology";
	private static final String ERROR_MISSING_API_IDENTIFICATION = "Missing Api identification";
	private static final String ERROR_MISSING_OPERATIONS = "Missing operations";
	private static final String ERROR_API_INVALID_STATE = "Api state not valid";
	private static final String EMPTY_RESPONSE_APIS = "{\"apis\" : \"\"}";

	@ApiOperation(value = "Get users access to api by identification or id")
	@GetMapping(value = "/{apiId}/authorizations")
	public ResponseEntity<?> getAuthorizations(
			@ApiParam(value = "Api identification or id") @PathVariable(value = "apiId") String apiId,
			@ApiParam(value = "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {

		ResponseEntity<?> response;
		try {
			final User loggedUser = userService.getUser(utils.getUserId());
			List<UserApi> usersapi = apiManagerService.getAuthorizations(apiId, apiVersion, loggedUser);
			List<UserApiSimplifiedResponseDTO> usersapiDto = new ArrayList<>();
			for (UserApi ua : usersapi) {
				usersapiDto.add(new UserApiSimplifiedResponseDTO(ua));
			}
			response = new ResponseEntity<>(usersapiDto, HttpStatus.OK);
		} catch (ApiManagerServiceException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (Exception e) {
			response = new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@ApiOperation(value = "Create users access to api by identification or id")
	@PostMapping(value = "/{apiId}/authorizations")
	public ResponseEntity<?> createAuthorizations(
			@ApiParam(value = "Api identification or id") @PathVariable(value = "apiId") String apiId,
			@ApiParam(value = "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@Valid @RequestBody List<UserApiSimplifiedInputDTO> userApiAccesses) {

		ResponseEntity<?> response;
		try {
			final User loggedUser = userService.getUser(utils.getUserId());
			List<String> usersId = userApiAccesses.stream().map(UserApiSimplifiedInputDTO::getUserId)
					.collect(Collectors.toList());

			List<String> created = apiManagerService.updateAuthorizations(apiId, apiVersion, usersId, loggedUser);

			JSONObject responseInfo = new JSONObject();
			Iterator<String> i1 = usersId.iterator();
			Iterator<String> i2 = created.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i1.next(), i2.next());
			}
			response = new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);
		} catch (ApiManagerServiceException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (Exception e) {
			response = new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@ApiOperation(value = "Remove users access to api by identification or id")
	@DeleteMapping(value = "/{apiId}/authorizations")
	public ResponseEntity<?> removeAuthorizations(
			@ApiParam(value = "Api identification or id") @PathVariable(value = "apiId") String apiId,
			@ApiParam(value = "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@Valid @RequestBody List<UserApiSimplifiedInputDTO> userApiAccesses) {

		ResponseEntity<?> response;
		try {
			final User loggedUser = userService.getUser(utils.getUserId());
			List<String> usersId = userApiAccesses.stream().map(UserApiSimplifiedInputDTO::getUserId)
					.collect(Collectors.toList());

			List<String> removed = apiManagerService.removeAuthorizations(apiId, apiVersion, usersId, loggedUser);

			JSONObject responseInfo = new JSONObject();
			Iterator<String> i1 = usersId.iterator();
			Iterator<String> i2 = removed.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				responseInfo.put(i1.next(), i2.next());
			}
			response = new ResponseEntity<>(responseInfo.toString(), HttpStatus.OK);
		} catch (ApiManagerServiceException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (Exception e) {
			response = new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@ApiOperation(value = "Authorize user for api by identification or id")
	@PostMapping(value = "/authorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> authorize(
			@ApiParam(value = "Api identification or id", required = true) @PathVariable("apiId") String apiId,
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@ApiParam(value = "Version required if use identification (if not present it applies to all versions)", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@RequestHeader("Authorization") String authorization) {

		ResponseEntity<?> response;
		List<UserApiSimplifiedResponseDTO> usersapiDto = new ArrayList<>();

		final String loggedUserId = utils.getUserId();
		final User loggedUser = userService.getUser(loggedUserId);

		try {
			com.minsait.onesait.platform.config.model.Api api = apiManagerService
					.getApiByIdentificationVersionOrId(apiId, apiVersion);

			if (api != null) {
				if (!apiManagerService.hasUserEditAccess(api, loggedUser)) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
							ERROR_USER_NOT_ALLOWED);
				}
				UserApi userapi = apiManagerService.updateAuthorization(api.getId(), userId);
				usersapiDto.add(new UserApiSimplifiedResponseDTO(userapi));
			} else {
				List<com.minsait.onesait.platform.config.model.Api> apiAllVersions = apiManagerService
						.getApisOfOwnerAndIdentification(loggedUser, apiId);
				if (apiAllVersions.isEmpty()) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.NOT_FOUND,
							ERROR_API_NOT_FOUND);
				}
				// update api+version when possible, else: skip
				List<UserApi> userapis = apiManagerService.updateAuthorizationAllVersions(apiId, userId, loggedUser);
				for (UserApi ua : userapis) {
					usersapiDto.add(new UserApiSimplifiedResponseDTO(ua));
				}
			}

			if (usersapiDto.isEmpty()) {
				response = new ResponseEntity<>(usersapiDto, HttpStatus.NO_CONTENT);
			} else {
				response = new ResponseEntity<>(usersapiDto, HttpStatus.CREATED);
			}

		} catch (ApiManagerServiceException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (NullPointerException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO();
			errorDTO.setError(ApiManagerServiceException.Error.NOT_FOUND.name());
			errorDTO.setMsg(ERROR_API_NOT_FOUND);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (Exception e) {
			response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return response;

	}

	@ApiOperation(value = "Deauthorize user for api by identification or id")
	@PostMapping(value = "/deauthorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> deauthorize(
			@ApiParam(value = "Api identification or id", required = true) @PathVariable("apiId") String apiId,
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@ApiParam(value = "Version required if use identification (if not present it applies to all versions)", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion,
			@RequestHeader("Authorization") String authorization) {

		ResponseEntity<?> response;

		final String loggedUserId = utils.getUserId();
		final User loggedUser = userService.getUser(loggedUserId);

		try {
			com.minsait.onesait.platform.config.model.Api api = apiManagerService
					.getApiByIdentificationVersionOrId(apiId, apiVersion);

			if (api != null) {
				if (!apiManagerService.hasUserEditAccess(api, loggedUser)) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
							ERROR_USER_NOT_ALLOWED);
				}
				UserApi userapi = apiManagerService.getUserApiByIdAndUser(api.getId(), userId);
				if (userapi == null) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_ACCESS_NOT_FOUND,
							ERROR_USER_ACCESS_NOT_FOUND);
				}
				apiManagerService.removeAuthorizationById(userapi.getId());

			} else {
				apiManagerService.removeAuthorizationAllVersions(apiId, userId, loggedUser);
			}
			response = new ResponseEntity<>("{\"status\": \"ok\"}", HttpStatus.OK);

		} catch (ApiManagerServiceException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (NullPointerException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO();
			errorDTO.setError(ApiManagerServiceException.Error.NOT_FOUND.name());
			errorDTO.setMsg(ERROR_API_NOT_FOUND);
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (Exception e) {
			response = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return response;
	}

	@ApiOperation(value = "Get user token for api")
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

	@ApiOperation(value = "Get user tokens for api")
	@GetMapping(value = "/api/tokens")
	public ResponseEntity<?> getApiTokens() {

		final List<UserToken> tokens = userService.getUserToken(userService.getUser(utils.getUserId()));
		if (!tokens.isEmpty()) {
			tokens.sort(Comparator.comparing(UserToken::getCreatedAt).reversed());
			List<String> tokenList = new ArrayList<>();
			for (UserToken token : tokens) {
				tokenList.add(token.getToken());
			}
			JSONObject response = new JSONObject();
			response.put("userTokens", tokenList);
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("{\"userToken\" : \"\"}", HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Generate new user token for api")
	@PostMapping(value = "/api/token")
	public ResponseEntity<String> generateApiToken() {
		try {
			apiManagerService.generateToken(utils.getUserId());
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return getApiToken();

	}

	@ApiOperation(value = "Delete user token for api")
	@DeleteMapping(value = "/api/token/{token}")
	public ResponseEntity<String> deleteApiToken(
			@ApiParam(value = "Token Id ", required = true) @PathVariable("token") String token) {
		final UserToken tokenOj = userTokenService.getTokenByUserAndToken(userService.getUser(utils.getUserId()),
				token);

		if (tokenOj == null)
			return new ResponseEntity<>("Token with id " + token + " does not exist", HttpStatus.BAD_REQUEST);
		try {
			apiManagerService.removeToken(utils.getUserId(), "{\"token\":\"" + token + "\"}");
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@ApiOperation(value = "Get list of user apis and public apis")
	@GetMapping
	public ResponseEntity<?> getApiList() {
		final User user = userService.getUser(utils.getUserId());
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService.loadAPISByFilter("", "", "",
				utils.getUserId());
		if (!apis.isEmpty()) {
			final ArrayList<ApiRestDTO> apisdto = new ArrayList<>();
			for (com.minsait.onesait.platform.config.model.Api api : apis) {
				ApiRestDTO apidto = apiDTOWithOperationsAndAuthorizations(api, user, true);
				apisdto.add(apidto);
			}
			return new ResponseEntity<>(apisdto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(EMPTY_RESPONSE_APIS, HttpStatus.NO_CONTENT);
		}

	}

	@ApiOperation(value = "Get api by identification or id")
	@GetMapping(value = "/{id}")
	public ResponseEntity<?> getApiByIdentification(
			@ApiParam(value = "Api identification or id", required = true) @PathVariable("id") String apiId,
			@ApiParam(value = "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {
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

	@ApiOperation(value = "Delete api by identification or id")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteApi(
			@ApiParam(value = "Api identification or id", required = true) @PathVariable("id") String apiId,
			@ApiParam(value = "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {
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

	@ApiOperation(value = "Create new API")
	@PostMapping
	public ResponseEntity<?> createApi(
			@ApiParam(value = "APIBody", required = true) @Valid @RequestBody ApiRestDTO apiBody) {
		com.minsait.onesait.platform.config.model.Api createdApi = null;
		List<com.minsait.onesait.platform.config.model.Api> existingApisWithIdentificationAndUser;

		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
					ERROR_USER_NOT_ALLOWED);
		}
		try {
			// build api from body
			com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody, user, ApiStates.CREATED);
			List<com.minsait.onesait.platform.config.model.ApiOperation> operations;
			if (apiBody.getOperations() == null) {
				operations = null;
			} else {
				operations = apiDTOConverter.toAPIOperations(apiBody.getOperations(),
						new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), api);
			}
			List<UserApi> auths = apiDTOConverter.toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(),
					api);

			// search if exists by identification + version
			existingApisWithIdentificationAndUser = apiManagerService.getApisOfOwnerAndIdentification(user,
					api.getIdentification());

			if (existingApisWithIdentificationAndUser.isEmpty()) {
				api.setUser(user);
				createdApi = apiManagerService.createApiRest(api, operations, auths);
			} else {
				createdApi = apiManagerService.versionateApiRest(api, operations, auths, user);
			}

		} catch (final ApiManagerServiceException e) {
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			return new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(new ApiSimplifiedResponseDTO(createdApi), HttpStatus.OK);
	}

	@ApiOperation(value = "Update API")
	@PutMapping
	public ResponseEntity<?> updateApi(
			@ApiParam(value = "APIBody", required = true) @Valid @RequestBody ApiRestDTO apiBody) {
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}

		com.minsait.onesait.platform.config.model.Api apimemory = apiManagerService
				.getApiByIdentificationVersionOrId(apiBody.getIdentification(), String.valueOf(apiBody.getVersion()));

		if (apimemory == null)
			return new ResponseEntity<>(
					"Api \"" + apiBody.getIdentification() + " - v" + apiBody.getVersion() + "\" does not exist",
					HttpStatus.NOT_FOUND);

		if (!apiManagerService.isApiStateValidForEdit(apimemory)) {
			return new ResponseEntity<>(ERROR_API_INVALID_STATE, HttpStatus.FORBIDDEN);
		}

		try {

			com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody, user, ApiStates.CREATED);

			if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY))
				return new ResponseEntity<>(ERROR_MISSING_ONTOLOGY, HttpStatus.BAD_REQUEST);
			if (api.getIdentification() == null || api.getIdentification().equals(""))
				return new ResponseEntity<>(ERROR_MISSING_API_IDENTIFICATION, HttpStatus.BAD_REQUEST);

			if (apiBody.getOperations() == null)
				return new ResponseEntity<>(ERROR_MISSING_OPERATIONS, HttpStatus.BAD_REQUEST);
			List<com.minsait.onesait.platform.config.model.ApiOperation> operations = apiDTOConverter.toAPIOperations(
					apiBody.getOperations(), new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(),
					apimemory);

			List<UserApi> auths = apiDTOConverter.toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(),
					apimemory);

			String apiId = apiManagerService.updateApiRest(api, apimemory, operations, auths, false);
			api.setId(apiId); // to print in result update (retrocomp)

			return new ResponseEntity<>(new ApiSimplifiedResponseDTO(api), HttpStatus.OK);

		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Change api state by identification or id")
	@PostMapping(value = "/changestate/{id}/{state}")
	public ResponseEntity<?> changeStateByIdentification(
			@ApiParam(value = "Api identification or id", required = true) @PathVariable("id") String apiId,
			@ApiParam(value = "Api state", required = true) @PathVariable("state") String state,
			@ApiParam(value = "Version required if use identification", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {
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
				|| (allowEditUsers && apiManagerService.hasUserEditAccess(api, user))) {
			usersapi = apiManagerService.getUserApiByApiId(api.getId());
		}

		if (api.getGraviteeId()==null) {
			return new ApiRestDTO(api, apiops, usersapi, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		} else {
			return new ApiRestDTO(api, apiops, usersapi, resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY));
		}
	}

	@ApiOperation(value = "Export api by identification or id")
	@GetMapping(value = "export/{id}")
	public ResponseEntity<?> exportApiByIdentification(
			@ApiParam(value = "Api identification or id", required = true) @PathVariable("id") String apiId,
			@ApiParam(value = "Version required if use identification (if not present it applies to all versions)", required = false) @RequestParam(value = "version", required = false, defaultValue = "") String apiVersion) {

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

			List<com.minsait.onesait.platform.config.model.Api> apiAllVersions = apiManagerService
					.getApisOfOwnerAndIdentification(user, apiId);

			if (apiAllVersions.isEmpty()) {
				return new ResponseEntity<>(EMPTY_RESPONSE_APIS, HttpStatus.NOT_FOUND);
			}

			List<ApiRestDTO> allVersionsDTO = new ArrayList<>();
			for (com.minsait.onesait.platform.config.model.Api apiVers : apiAllVersions) {
				if (apiManagerService.hasUserAccess(apiVers, user)) {
					final ApiRestDTO apidtoVersion = apiDTOWithOperationsAndAuthorizations(apiVers, user, false);
					allVersionsDTO.add(apidtoVersion);
				}

			}
			final HttpHeaders headers = exportHeaders(allVersionsDTO.get(0).getIdentification());
			return new ResponseEntity<>(allVersionsDTO, headers, HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Export all apis")
	@GetMapping(value = "export/")
	public ResponseEntity<?> exportAllApis() {
		List<ApiRestDTO> apisDTO = new ArrayList<>();
		final User user = userService.getUser(utils.getUserId());
		List<com.minsait.onesait.platform.config.model.Api> apis;
		if (userService.isUserAdministrator(user)) {
			apis = apiManagerService.getAllApis(user);
		} else {
			apis = apiManagerService.getApisOfOwner(user);
		}

		if (!apis.isEmpty()) {
			for (com.minsait.onesait.platform.config.model.Api api : apis) {
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

	@ApiOperation(value = "Import API")
	@PostMapping(value = "import")
	public ResponseEntity<?> importApi(
			@ApiParam(value = "Overwrite api if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@ApiParam(value = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@ApiParam(value = "APIBody", required = true) @Valid @RequestBody ApiRestDTO apiBody) {

		// ------ Importing rules --------
		// version ---------------------OK (import any version allowed to not change
		// endpoints in migration)

		final User user = userService.getUser(utils.getUserId());
		com.minsait.onesait.platform.config.model.Api createdApi = null;

		try {
			com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody,
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
			ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
			return new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new ApiSimplifiedResponseDTO(createdApi), HttpStatus.OK);
	}

	@ApiOperation(value = "Import several APIs")
	@PostMapping(value = "import/apis/")
	public ResponseEntity<?> importSeveralApis(
			@ApiParam(value = "Overwrite api if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@ApiParam(value = "Import authorizations if exist") @RequestParam(required = false, defaultValue = "false") boolean importAuthorizations,
			@ApiParam(value = "APIBodies", required = true) @Valid @RequestBody List<ApiRestDTO> apiBodies) {

		// ------ Importing rules --------
		// version ---------------------OK (import any version allowed to not change
		// endpoints in migration)

		final User user = userService.getUser(utils.getUserId());

		List<ApiSimplifiedResponseDTO> importedAPIsResponse = new ArrayList<>();
		for (ApiRestDTO apiBody : apiBodies) {
			com.minsait.onesait.platform.config.model.Api api = apiDTOConverter.toAPI(apiBody,
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
				ApiResponseErrorDTO errorDTO = new ApiResponseErrorDTO(e);
				importedAPIsResponse.add(new ApiSimplifiedResponseDTO(api.getId(), api.getIdentification(),
						api.getNumversion(), errorDTO.getMsg()));
			} catch (final Exception e) {
				importedAPIsResponse.add(new ApiSimplifiedResponseDTO(api.getId(), api.getIdentification(),
						api.getNumversion(), "Error importing API"));
			}
		}

		return new ResponseEntity<>(importedAPIsResponse, HttpStatus.OK);
	}

}

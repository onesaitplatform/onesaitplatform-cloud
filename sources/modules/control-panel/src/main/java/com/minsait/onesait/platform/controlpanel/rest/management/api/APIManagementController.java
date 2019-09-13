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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiHeader;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiDTO;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiHeaderDTO;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiOperationDTO;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiQueryParameterDTO;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.UserApiDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
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
	UserTokenRepository userTokenRepository;
	@Autowired
	UserApiRepository userApiRepository;
	@Autowired
	ApiOperationRepository apiOperationRepository;
	@Autowired
	OntologyRepository ontologyRepository;
	@Autowired
	IntegrationResourcesService resourcesService;
	@Autowired
	AppWebUtils utils;
	@Autowired
	JWTService jwtService;
	
	static final String MISSING_ONTOLOGY = "Missing Ontology";
	static final String MISSING_API_IDENTIFICATION = "Missing Api identification";
	static final String MISSING_OPERATIONS = "Missing operations";
	static final String NOT_AUTHORIZED = "\" is not authorized\"";
	static final String API = "\"api\": \"";
	static final String APIS = "{\"apis\" : \"\"}";
	
	@ApiOperation(value = "Authorize user for api")
	@PostMapping(value = "/authorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> authorize(
			@ApiParam(value = "Api Id  ", required = true) @PathVariable("apiId") String apiId,
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@RequestHeader("Authorization") String authorization) {

		final String loggedUser = jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService
				.loadAPISByFilter(apiId, "", loggedUser, loggedUser).stream()
				.filter(a -> a.getUser().getUserId().equals(loggedUser) && a.getIdentification().equals(apiId))
				.collect(Collectors.toList());

		UserApi userApi = null;
		if (!apis.isEmpty()) {
			for (final com.minsait.onesait.platform.config.model.Api api : apis) {
				userApi = apiManagerService.updateAuthorization(api.getId(), userId);
			}
			if (userApi != null) {
				final UserApiDTO userApiDTO = new UserApiDTO(userApi);
				return new ResponseEntity<>(userApiDTO, HttpStatus.CREATED);
			}
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

	}

	@ApiOperation(value = "Deauthorize user for api")
	@PostMapping(value = "/deauthorize/api/{apiId}/user/{userId}")
	public ResponseEntity<?> deauthorize(
			@ApiParam(value = "Api Id ", required = true) @PathVariable("apiId") String apiId,
			@ApiParam(value = "User", required = true) @PathVariable(name = "userId") String userId,
			@RequestHeader("Authorization") String authorization) {
		final String loggedUser = jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService
				.loadAPISByFilter(apiId, "", loggedUser, loggedUser).stream()
				.filter(a -> a.getUser().getUserId().equals(loggedUser) && a.getIdentification().equals(apiId))
				.collect(Collectors.toList());
		if (!apis.isEmpty()) {
			final List<UserApi> userApi = userApiRepository.findByUser(userService.getUser(userId)).stream()
					.filter(ua -> ua.getApi().getIdentification().equals(apiId)).collect(Collectors.toList());
			if (userApi.get(0) != null) {
				userApiRepository.delete(userApi.get(0));
				return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
			}
			return new ResponseEntity<>("No authorization for " + userId + " in " + apiId, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		}

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
			List<String> tokenList = new ArrayList<> ();
			for (UserToken token : tokens){
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
		final UserToken tokenOj = userTokenRepository.findByUserAndToken(userService.getUser(utils.getUserId()),
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
		final List<com.minsait.onesait.platform.config.model.Api> apis = apiManagerService.loadAPISByFilter("", "", "",
				utils.getUserId());
		if (!apis.isEmpty()) {
			final ArrayList<ApiDTO> apisdto = new ArrayList<>();
			for (com.minsait.onesait.platform.config.model.Api api : apis) {
				List<com.minsait.onesait.platform.config.model.ApiOperation> apiops = apiOperationRepository
						.findByApiIdOrderByOperationDesc(api.getId());
				List<UserApi> usersapi = userApiRepository.findByApiId(api.getId());
				ApiDTO apidto = new ApiDTO(api, apiops, usersapi);
				apisdto.add(apidto);
			}
			return new ResponseEntity<>(apisdto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(APIS, HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Get api by Id")
	@GetMapping(value = "/{id}")
	public ResponseEntity<?> getApiByIdentification(
			@ApiParam(value = "api id", required = true) @PathVariable("id") String id) {
		final com.minsait.onesait.platform.config.model.Api api = apiManagerService.getById(id);
		if (api != null) {
			if (apiManagerService.hasUserAccess(api.getId(), utils.getUserId())) {
				final List<com.minsait.onesait.platform.config.model.ApiOperation> apiops = apiOperationRepository
						.findByApiIdOrderByOperationDesc(api.getId());
				final List<UserApi> usersapi = userApiRepository.findByApiId(api.getId());
				final ApiDTO apidto = new ApiDTO(api, apiops, usersapi);
				return new ResponseEntity<>(apidto, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("\" is not authorized \"", HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(APIS, HttpStatus.NOT_FOUND);
		}

	}

	@ApiOperation(value = "Delete api by identification")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteApi(@ApiParam(value = "Api id", required = true) @PathVariable("id") String apiId) {
		try {
			final com.minsait.onesait.platform.config.model.Api api = apiManagerService.getById(apiId);
			if (api == null) {
				return new ResponseEntity<>("Api \"" + apiId + "\" does not exist", HttpStatus.NOT_FOUND);
			}
			final User user = userService.getUser(utils.getUserId());
			if (!apiManagerService.hasUserEditAccess(api.getId(), user.getUserId())) {
				return new ResponseEntity<>(NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
			}
			apiManagerService.removeAPI(api.getId());
		} catch (final Exception exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Api deleted successfully", HttpStatus.OK);
	}

	@ApiOperation(value = "Create new API")
	@PostMapping
	public ResponseEntity<?> createApi(@ApiParam(value = "APIBody", required = true) @Valid @RequestBody ApiDTO apiBody,
			Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		com.minsait.onesait.platform.config.model.Api api = toAPI(apiBody, user,
				new com.minsait.onesait.platform.config.model.Api());

		if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY))
			return new ResponseEntity<>(MISSING_ONTOLOGY, HttpStatus.BAD_REQUEST);
		if (api.getIdentification() == null || api.getIdentification().equals(""))
			return new ResponseEntity<>(MISSING_API_IDENTIFICATION, HttpStatus.BAD_REQUEST);

		if (apiBody.getOperations() == null)
			return new ResponseEntity<>(MISSING_OPERATIONS, HttpStatus.BAD_REQUEST);
		List<com.minsait.onesait.platform.config.model.ApiOperation> operations = toAPIOperations(
				apiBody.getOperations(), new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), api);

		List<UserApi> auths = toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(), api);

		try {
			apiManagerService.createApiRest(api, operations, auths);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(API + api.getIdentification() + "\"", HttpStatus.OK);
	}

	@ApiOperation(value = "Update API")
	@PutMapping
	public ResponseEntity<?> updateApi(@ApiParam(value = "APIBody", required = true) @Valid @RequestBody ApiDTO apiBody,
			Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		List<com.minsait.onesait.platform.config.model.Api> apimemory = apiManagerService
				.loadAPISByFilter(apiBody.getIdentification(), "", "", utils.getUserId());

		if (apimemory.isEmpty())
			return new ResponseEntity<>("Api \"" + apiBody.getIdentification() + "\" does not exist",
					HttpStatus.NOT_FOUND);

		com.minsait.onesait.platform.config.model.Api api = toAPI(apiBody, user,
				new com.minsait.onesait.platform.config.model.Api());

		if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY))
			return new ResponseEntity<>(MISSING_ONTOLOGY, HttpStatus.BAD_REQUEST);
		if (api.getIdentification() == null || api.getIdentification().equals(""))
			return new ResponseEntity<>(MISSING_API_IDENTIFICATION, HttpStatus.BAD_REQUEST);

		if (apiBody.getOperations() == null)
			return new ResponseEntity<>(MISSING_OPERATIONS, HttpStatus.BAD_REQUEST);
		List<com.minsait.onesait.platform.config.model.ApiOperation> operations = toAPIOperations(
				apiBody.getOperations(), new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(),
				apimemory.get(0));

		List<UserApi> auths = toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(), apimemory.get(0));

		try {
			apiManagerService.updateApiRest(api, apimemory.get(0), operations, auths);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(API + api.getIdentification() + "\"", HttpStatus.OK);
	}

	private com.minsait.onesait.platform.config.model.Api toAPI(ApiDTO apiDTO, User user,
			com.minsait.onesait.platform.config.model.Api api) {

		api.setUser(user);
		api.setIdentification(apiDTO.getIdentification());
		api.setDescription(apiDTO.getDescription());
		api.setMetaInf(apiDTO.getMetainf());
		api.setSsl_certificate(false);
		api.setEndpoint(resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE) + "server/api/v");
		api.setPublic(apiDTO.getIsPublic());
		api.setCategory(ApiCategories.valueOf(apiDTO.getCategory()));
		api.setState(ApiStates.CREATED);
		api.setImageType(apiDTO.getImageType());
		api.setApilimit(apiDTO.getApiLimit());
		if (apiDTO.getType().equals("EXTERNAL_FROM_JSON")) {
			api.setApiType(ApiType.EXTERNAL_FROM_JSON);
			api.setSwaggerJson(apiDTO.getSwaggerJson());
		} else {
			api.setApiType(ApiType.INTERNAL_ONTOLOGY);
			api.setOntology(ontologyRepository.findById(apiDTO.getOntologyId()));
		}
		return api;
	}

	private List<com.minsait.onesait.platform.config.model.ApiOperation> toAPIOperations(
			List<ApiOperationDTO> apiopsDTO, List<com.minsait.onesait.platform.config.model.ApiOperation> apiops,
			com.minsait.onesait.platform.config.model.Api api) {

		for (ApiOperationDTO apiopdto : apiopsDTO) {
			com.minsait.onesait.platform.config.model.ApiOperation apiop = new com.minsait.onesait.platform.config.model.ApiOperation();
			apiop.setApi(api);
			apiop.setPath(apiopdto.getPath());
			apiop.setDescription(apiopdto.getDescription());
			apiop.setEndpoint(apiopdto.getEndpoint());
			apiop.setIdentification(apiopdto.getIdentification());
			apiop.setOperation(apiopdto.getOperation());
			apiop.setPostProcess(apiopdto.getPostProcess());
			HashSet<ApiHeader> apiheader = new HashSet<>();
			for (ApiHeaderDTO apiheaderdto : apiopdto.getHeaders()) {
				ApiHeader apih = new ApiHeader();
				apih.setName(apiheaderdto.getName());
				apih.setApiOperation(apiop);
				apih.setHeader_type(apiheaderdto.getType());
				apih.setHeader_description(apiheaderdto.getDescription());
				apih.setHeader_value(apiheaderdto.getValue());
				apih.setHeader_condition(apiheaderdto.getCondition());
				apiheader.add(apih);
			}
			apiop.setApiheaders(apiheader);
			HashSet<ApiQueryParameter> apiqueryparam = new HashSet<>();
			for (ApiQueryParameterDTO apiquerydto : apiopdto.getQueryParams()) {
				ApiQueryParameter apiqp = new ApiQueryParameter();
				apiqp.setName(apiquerydto.getName());
				apiqp.setDataType(apiquerydto.getDataType());
				apiqp.setDescription(apiquerydto.getDescription());
				apiqp.setValue(apiquerydto.getValue());
				apiqp.setCondition(apiquerydto.getCondition());
				apiqp.setHeaderType(apiquerydto.getHeaderType());
				apiqp.setApiOperation(apiop);
				apiqueryparam.add(apiqp);
			}
			apiop.setApiqueryparameters(apiqueryparam);
			apiops.add(apiop);
		}
		return apiops;
	}

	List<UserApi> toUserApi(List<UserApiDTO> userapisdto, List<UserApi> auths,
			com.minsait.onesait.platform.config.model.Api api) {
		if (userapisdto != null) {
			for (UserApiDTO userapiDTO : userapisdto) {
				UserApi userapi = new UserApi();
				userapi.setApi(api);
				userapi.setUser(userService.getUser(userapiDTO.getUserId()));
				auths.add(userapi);
			}
		}
		return auths;

	}

	@ApiOperation(value = "Change api state by Id")
	@PostMapping(value = "/changestate/{id}/{state}")
	public ResponseEntity<?> changeStateByIdentification(
			@ApiParam(value = "api id", required = true) @PathVariable("id") String id,
			@ApiParam(value = "api state", required = true) @PathVariable("state") String state) {
		final com.minsait.onesait.platform.config.model.Api api = apiManagerService.getById(id);
		if (api != null) {
			if (apiManagerService.hasUserAccess(api.getId(), utils.getUserId())) {
				if (!apiManagerService.validateState(api.getState(), state)) {
					return new ResponseEntity<>("\"Forbidden change of state\"", HttpStatus.FORBIDDEN);
				}
				apiManagerService.updateState(api.getId(), state.toUpperCase());
				api.setState(ApiStates.valueOf(state.toUpperCase()));
				final List<com.minsait.onesait.platform.config.model.ApiOperation> apiops = apiOperationRepository
						.findByApiIdOrderByOperationDesc(api.getId());
				final List<UserApi> usersapi = userApiRepository.findByApiId(api.getId());
				final ApiDTO apidto = new ApiDTO(api, apiops, usersapi);
				return new ResponseEntity<>(apidto, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(NOT_AUTHORIZED, HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
		}

	}

	@ApiOperation(value = "Export api by Id")
	@GetMapping(value = "export/{id}")
	public ResponseEntity<?> exportApiByIdentification(
			@ApiParam(value = "api id", required = true) @PathVariable("id") String id) {
		final com.minsait.onesait.platform.config.model.Api api = apiManagerService.getById(id);
		if (api != null) {
			if (apiManagerService.hasUserAccess(api.getId(), utils.getUserId())) {
				final List<com.minsait.onesait.platform.config.model.ApiOperation> apiops = apiOperationRepository
						.findByApiIdOrderByOperationDesc(api.getId());
				final List<UserApi> usersapi = userApiRepository.findByApiId(api.getId());
				final ApiDTO apidto = new ApiDTO(api, apiops, usersapi);
				apidto.setOntologyId(ontologyRepository.findById(apidto.getOntologyId()).getIdentification());
				return new ResponseEntity<>(apidto, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(APIS, HttpStatus.NOT_FOUND);
		}

	}

	@ApiOperation(value = "Import API")
	@PostMapping(value = "import")
	public ResponseEntity<?> importApi(@ApiParam(value = "APIBody", required = true) @Valid @RequestBody ApiDTO apiBody,
			Errors errors) {
		if (errors.hasErrors())
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		final User user = userService.getUser(utils.getUserId());

		if (!userService.isUserAdministrator(user) && !userService.isUserDeveloper(user)) {
			return new ResponseEntity<>(NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

		com.minsait.onesait.platform.config.model.Api api = toAPI(apiBody, user,
				new com.minsait.onesait.platform.config.model.Api());

		api.setOntology(ontologyRepository.findByIdentification(apiBody.getOntologyId()));

		if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY))
			return new ResponseEntity<>(MISSING_ONTOLOGY, HttpStatus.BAD_REQUEST);
		if (api.getIdentification() == null || api.getIdentification().equals(""))
			return new ResponseEntity<>(MISSING_API_IDENTIFICATION, HttpStatus.BAD_REQUEST);

		if (apiBody.getOperations() == null)
			return new ResponseEntity<>(MISSING_OPERATIONS, HttpStatus.BAD_REQUEST);
		List<com.minsait.onesait.platform.config.model.ApiOperation> operations = toAPIOperations(
				apiBody.getOperations(), new ArrayList<com.minsait.onesait.platform.config.model.ApiOperation>(), api);

		List<UserApi> auths = toUserApi(apiBody.getAuthentications(), new ArrayList<UserApi>(), api);

		try {
			apiManagerService.createApiRest(api, operations, auths);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(API + api.getIdentification() + "\"", HttpStatus.OK);
	}

}

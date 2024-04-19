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
package com.minsait.onesait.platform.config.services.apimanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiQueryParameterRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.apimanager.authentication.AuthenticationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.OperationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.QueryStringJson;
import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiManagerServiceImpl implements ApiManagerService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private UserApiRepository userApiRepository;
	@Autowired
	private ApiOperationRepository apiOperationRepository;
	@Autowired
	private ApiQueryParameterRepository apiQueryParameterRepository;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private UserService userService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired(required = false)
	private MetricsManager metricsManager;

	private static final String EXCEPTION_REACHED = "Exception reached ";
	private static final String AMPERSAND = "&amp;";
	private static final String PARSING_ERROR = "Error parsing operations: {} ";
	private static final String ERROR_API_NOT_FOUND = "Api not found";
	private static final String ERROR_API_INVALID_STATE = "Api state not valid";
	private static final String ERROR_USER_NOT_FOUND = "User not found";
	private static final String ERROR_USER_ACCESS_NOT_FOUND = "User access not found";
	private static final String ERROR_USER_NOT_ALLOWED = "User is not authorized";
	private static final String ERROR_USER_ACCESS_EXISTS = "User access exists";
	private static final String ERROR_USER_IS_OWNER = "User is owner of the api";
	private static final String ERROR_USER_IS_ADMIN = "User has role administrator";
	private static final String ERROR_MISSING_ONTOLOGY = "Missing Ontology";
	private static final String ERROR_MISSING_API_IDENTIFICATION = "Missing Api identification";
	private static final String ERROR_MISSING_OPERATIONS = "Missing operations";
	private static final String OK = "OK";
	private static final String KO = "KO";
	private static String NOT_POSSIBLE_NOTIFY_ROUTER = "Not possible to send creation info to router: {}";

	@Override
	public List<Api> loadAPISByFilter(String apiId, String state, String userId, String loggeduser) {
		List<Api> apis = null;
		// Gets context User
		final User user = userService.getUser(loggeduser);

		// Clean the filter
		if (apiId == null || "".equals(apiId)) {
			apiId = "";
		}

		if (userId == null || "".equals(userId)) {
			userId = "";
		}

		if (state == null || "".equals(state)) {
			apis = apiRepository.findApisByIdentificationOrUserForAdminOrOwnerOrPublicOrPermission(user.getUserId(),
					user.getRole().getId(), apiId, userId);
		} else {
			apis = apiRepository.findApisByIdentificationOrStateOrUserForAdminOrOwnerOrPublicOrPermission(
					user.getUserId(), user.getRole().getId(), apiId, Api.ApiStates.valueOf(state), userId);
		}

		return apis;
	}

	@Override
	public Integer calculateNumVersion(String identification, ApiType apiType) {
		List<Api> apis = null;
		Integer version = 0;
		apis = apiRepository.findByIdentificationAndApiType(identification, apiType);
		for (final Api api : apis) {
			if (api.getNumversion() > version) {
				version = api.getNumversion();
			}
		}
		return version + 1;
	}

	@Override
	public Integer calculateNumVersion(String numversionData) {
		Integer version = 1;
		String identification;
		ApiType apiType;
		Map<String, String> obj;
		try {
			obj = new ObjectMapper().readValue(numversionData, new TypeReference<Map<String, String>>() {
			});
			identification = obj.get("identification");
			apiType = ApiType.valueOf(obj.get("apiType"));

			if (StringUtils.isEmpty(apiType)) {
				apiType = null;
			}

			version = calculateNumVersion(identification, apiType);

		} catch (final IOException e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
		}
		return version;
	}

	@Override
	public List<String> getIdentificationsByUserOrPermission(String userId) {
		User user = userService.getUser(userId);
		List<Api> apis = new ArrayList<>();
		if (user.isAdmin()) {
			apis = apiRepository.findAll();
		} else {
			apis = apiRepository.findApisByUserOrPermission(userId);
		}

		List<String> apisIdentification = new ArrayList<>();
		for (Api api : apis) {
			apisIdentification.add(api.getIdentification() + " - V" + api.getNumversion());
		}
		return apisIdentification;
	}

	@Override
	public String createApi(Api api, String operationsObject, String authenticationObject) {

		try {
			final String numversionData = "{\"identification\":\"" + api.getIdentification() + "\",\"apiType\":\""
					+ api.getApiType() + "\"}";
			api.setNumversion(calculateNumVersion(numversionData));

			final ObjectMapper objectMapper = new ObjectMapper();

			List<OperationJson> operationsJson = null;

			if (operationsObject != null && !operationsObject.equals("")) {
				try {
					operationsJson = objectMapper.readValue(operationsObject, new TypeReference<List<OperationJson>>() {
					});
				} catch (final IOException e) {
					log.error(EXCEPTION_REACHED + e.getMessage(), e);
				}

			}

			final AuthenticationJson authenticationJson = null;

			apiRepository.save(api);

			if (operationsJson != null) {
				createOperations(api, operationsJson);
			}

			metricsManagerLogControlPanelApiCreation(api.getUser().getUserId(), OK);

		} catch (final Exception e) {
			metricsManagerLogControlPanelApiCreation(api.getUser().getUserId(), KO);
			log.error("Error creating API", e);
			throw e;
		}
		return api.getId();
	}

	private void createOperations(Api api, List<OperationJson> operationsJson) {
		for (final OperationJson operationJson : operationsJson) {
			final ApiOperation operation = new ApiOperation();
			operation.setApi(api);
			operation.setIdentification(operationJson.getIdentification());
			operation.setDescription(operationJson.getDescription());
			operation.setOperation(ApiOperation.Type.valueOf(operationJson.getOperation()));
			if (operationJson.getBasepath() != null && !operationJson.getBasepath().equals("")) {
				final String basepath = operationJson.getBasepath().replace(AMPERSAND, "&");
				operation.setBasePath(basepath);
			}
			if (operationJson.getEndpoint() != null && !operationJson.getEndpoint().equals("")) {
				final String endpoint = operationJson.getEndpoint().replace(AMPERSAND, "&");
				operation.setEndpoint(endpoint);
			}
			final String path = operationJson.getPath().replace(AMPERSAND, "&");
			operation.setPath(path);

			operation.setPostProcess(operationJson.getPostprocess());

			apiOperationRepository.save(operation);

			if (operationJson.getQuerystrings() != null && !operationJson.getQuerystrings().isEmpty()) {
				createQueryStrings(operation, operationJson.getQuerystrings());
			}
		}
	}

	private void createQueryStrings(ApiOperation operation, List<QueryStringJson> querystrings) {
		for (final QueryStringJson queryStringJson : querystrings) {
			final ApiQueryParameter apiQueryParameter = new ApiQueryParameter();
			apiQueryParameter.setApiOperation(operation);
			apiQueryParameter.setName(queryStringJson.getName());
			apiQueryParameter.setDescription(queryStringJson.getDescription());
			apiQueryParameter.setDataType(ApiQueryParameter.DataType.valueOf(queryStringJson.getDataType()));
			apiQueryParameter.setHeaderType(ApiQueryParameter.HeaderType.valueOf(queryStringJson.getHeaderType()));
			apiQueryParameter.setValue(queryStringJson.getValue());
			apiQueryParameter.setCondition(queryStringJson.getCondition());

			apiQueryParameterRepository.save(apiQueryParameter);
		}
	}

	@Override
	public void updateApi(Api api, String deprecateApis, String operationsObject, String authenticationObject) {

		if (deprecateApis != null && !deprecateApis.equals("")) {
			deprecateApis(api.getIdentification());
		}

		final Optional<Api> opt = apiRepository.findById(api.getId());
		if (opt.isPresent()) {
			final Api apimemory = opt.get();
			final byte[] imagenOriginal = apimemory.getImage();

			apimemory.setIdentification(api.getIdentification());
			apimemory.setPublic(api.isPublic());
			apimemory.setSsl_certificate(api.isSsl_certificate());
			apimemory.setDescription(api.getDescription());
			apimemory.setCategory(api.getCategory());
			apimemory.setEndpointExt(api.getEndpointExt());
			apimemory.setMetaInf(api.getMetaInf());

			if (api.getApicachetimeout() != null) {
				apimemory.setApicachetimeout(api.getApicachetimeout());
			} else {
				apimemory.setApicachetimeout(null);
			}

			Integer limit = api.getApilimit();

			if (limit != null && limit <= 0) {
				limit = 1;
			}
			apimemory.setApilimit(limit);

			apimemory.setState(api.getState());

			if (api.getImage() != null && api.getImage().length > 0) {
				apimemory.setImage(api.getImage());
			} else {
				apimemory.setImage(imagenOriginal);
			}

			if (apimemory.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
				apimemory.setSwaggerJson(api.getSwaggerJson());
			}

			apiRepository.save(apimemory);

			final ObjectMapper objectMapper = new ObjectMapper();

			List<OperationJson> operationsJson = null;

			try {
				if (apimemory.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
					operationsJson = objectMapper.readValue(reformat(operationsObject),
							new TypeReference<List<OperationJson>>() {
							});
					updateOperations(apimemory, operationsJson);
				}
			} catch (final IOException e) {
				log.error(PARSING_ERROR, e);
			}
		}

	}

	private String reformat(String operationsObject) {
		if (operationsObject.indexOf(',') == 0) {
			operationsObject = operationsObject.substring(1);
		}
		return operationsObject;
	}

	private void updateOperations(Api api, List<OperationJson> operationsJson) {
		final List<ApiOperation> apiOperations = apiOperationRepository.findAllByApi(api);
		for (final ApiOperation apiOperation : apiOperations) {
			for (final ApiQueryParameter apiQueryParameter : apiOperation.getApiqueryparameters()) {
				apiQueryParameterRepository.delete(apiQueryParameter);
			}
			apiOperationRepository.delete(apiOperation);
		}
		createOperations(api, operationsJson);

	}

	@Override
	public void removeAPI(String id) {
		final Optional<Api> opt = apiRepository.findById(id);
		if (opt.isPresent()) {
			final Api apiremove = opt.get();
			if (resourceService.isResourceSharedInAnyProject(apiremove)) {
				throw new OPResourceServiceException(
						"This Api is shared within a Project, revoke access from project prior to deleting");
			}
			final List<ApiOperation> apiOperationList = apiOperationRepository.findByApiIdOrderByOperationDesc(id);

			apiOperationRepository.deleteAll(apiOperationList);
			log.debug("API's operation deleted");

			final List<UserApi> userApiList = userApiRepository.findByApiId(id);

			userApiRepository.deleteAll(userApiList);
			log.debug("API's authorizations deleted");

			apiRepository.delete(apiremove);
			log.debug("API deleted");
		}
	}

	private void deprecateApis(String apiId) {
		List<Api> apis = null;
		try {
			apis = apiRepository.findByIdentification(apiId);
			for (final Api api : apis) {
				if (api.getState().equals(Api.ApiStates.PUBLISHED)) {
					api.setState(Api.ApiStates.DEPRECATED);
					apiRepository.save(api);
				}
			}
		} catch (final Exception e) {
			log.error("Error deprecating APIS");
		}
	}

	@Override
	public List<UserApi> getAuthorizations(String apiId, String apiVersion, User user) {
		final Api api = getApiByIdentificationVersionOrId(apiId, apiVersion);

		if (api == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.NOT_FOUND, ERROR_API_NOT_FOUND);
		}

		if (!hasUserEditAccess(api, user)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
					ERROR_USER_NOT_ALLOWED);
		}
		return userApiRepository.findByApiId(api.getId());
	}

	@Override
	public List<String> updateAuthorizations(String apiId, String version, List<String> usersId, User user) {
		final List<String> updatedAuth = new ArrayList<>();
		final Api api = getApiByIdentificationVersionOrId(apiId, version);

		if (api == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.NOT_FOUND, ERROR_API_NOT_FOUND);
		}

		if (!isApiStateValidForEditAuth(api)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_STATE,
					ERROR_API_INVALID_STATE);
		}

		if (!hasUserEditAccess(api, user)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
					ERROR_USER_NOT_ALLOWED);
		}

		for (final String userId : usersId) {
			try {
				updateAuthorization(api.getId(), userId);
				updatedAuth.add(OK);
			} catch (final ApiManagerServiceException e) {
				updatedAuth.add(e.getError().name());
			} catch (final DataIntegrityViolationException e) {
				updatedAuth.add(ERROR_USER_ACCESS_EXISTS);
			} catch (final NullPointerException e) {
				updatedAuth.add(ERROR_USER_ACCESS_NOT_FOUND);
			} catch (final Exception e) {
				updatedAuth.add(e.getMessage());
			}
		}
		return updatedAuth;
	}

	@Override
	@Modifying
	public UserApi updateAuthorization(String apiId, String userId) {
		UserApi userApi = userApiRepository.findByApiIdAndUser(apiId, userId);

		if (userApi == null) {
			final Optional<Api> opt = apiRepository.findById(apiId);
			if (opt.isPresent()) {
				final Api api = opt.get();
				final User user = userRepository.findByUserId(userId);

				if (user == null) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_NOT_FOUND,
							ERROR_USER_NOT_FOUND);
				}

				if (user.equals(api.getUser())) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_IS_OWNER,
							ERROR_USER_IS_OWNER);
				}

				if (!isApiStateValidForEditAuth(api)) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_STATE,
							ERROR_API_INVALID_STATE);
				}

				if (userService.isUserAdministrator(user)) {
					throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_IS_ADMIN,
							ERROR_USER_IS_ADMIN);
				}

				userApi = new UserApi();
				userApi.setApi(api);
				userApi.setUser(user);

				api.getUserApiAccesses().removeIf(ua -> ua.getUser().equals(user) && ua.getApi().equals(api));
				api.getUserApiAccesses().add(userApi);
				return apiRepository.save(api).getUserApiAccesses().stream()
						.filter(ua -> ua.getUser().equals(user) && ua.getApi().equals(api)).findFirst().orElse(userApi);

			}
		}
		return userApi;
	}

	@Override
	public List<UserApi> updateAuthorizationAllVersions(String identification, String userId, User user) {
		// for api+version: if possible: update auth, else: skip update auth
		final List<UserApi> authorizations = new ArrayList<>();

		final User userToAuthorize = userService.getUser(userId);
		if (userToAuthorize == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_NOT_FOUND, ERROR_USER_NOT_FOUND);
		}

		final List<Api> apis = apiRepository.findByIdentificationAndUser(identification, user);
		if (!apis.isEmpty()) {
			for (final Api api : apis) {
				if (hasUserEditAccess(api, user)) {
					try {
						final UserApi userApi = updateAuthorization(api.getId(), userToAuthorize.getUserId());
						if (userApi != null) {
							authorizations.add(userApi);
						}
					} catch (final ApiManagerServiceException e) {
						log.warn("Not possible create authorization for user {} on api {} - V{}: {}", userId,
								api.getIdentification(), api.getNumversion(), e.getMessage());
					}
				}
			}
		}
		return authorizations;
	}

	@Override
	public List<String> removeAuthorizations(String apiId, String version, List<String> usersId, User user) {
		final List<String> removedAuth = new ArrayList<>();
		final Api api = getApiByIdentificationVersionOrId(apiId, version);

		if (api == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.NOT_FOUND, ERROR_API_NOT_FOUND);
		}

		if (!isApiStateValidForEditAuth(api)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_STATE,
					ERROR_API_INVALID_STATE);
		}

		if (!hasUserEditAccess(api, user)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
					ERROR_USER_NOT_ALLOWED);
		}

		for (final String userId : usersId) {
			try {
				final UserApi userapi = userApiRepository.findByApiIdAndUser(api.getId(), userId);
				removeAuthorizationById(userapi.getId());
				removedAuth.add(OK);
			} catch (final DataIntegrityViolationException e) {
				removedAuth.add(ERROR_USER_ACCESS_EXISTS);
			} catch (final NullPointerException e) {
				removedAuth.add(ERROR_USER_ACCESS_NOT_FOUND);
			} catch (final Exception e) {
				removedAuth.add(e.getMessage());
			}
		}

		return removedAuth;
	}

	@Override
	@Modifying
	public void removeAuthorizationById(String id) {
		final Optional<UserApi> opt = userApiRepository.findById(id);
		opt.ifPresent(ua -> {
			final UserApi userApi = ua;
			if (!isApiStateValidForEditAuth(userApi.getApi())) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_STATE,
						ERROR_API_INVALID_STATE);
			}
			userApi.getApi().getUserApiAccesses().remove(userApi);
			apiRepository.save(userApi.getApi());
		});

	}

	@Override
	public void removeAuthorizationAllVersions(String identification, String userId, User user) {
		// remove auths where possible, else: skip
		final User userToDeauthorize = userService.getUser(userId);
		if (userToDeauthorize == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_NOT_FOUND, ERROR_USER_NOT_FOUND);
		}

		final List<Api> apis = apiRepository.findByIdentificationAndUser(identification, user);
		if (apis.isEmpty()) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.USER_ACCESS_NOT_FOUND,
					ERROR_USER_ACCESS_NOT_FOUND);
		}

		for (final Api api : apis) {
			if (hasUserEditAccess(api, user)) {
				final UserApi userApi = userApiRepository.findByApiIdAndUser(api.getId(), userId);
				if (userApi != null) {
					try {
						removeAuthorizationById(userApi.getId());
					} catch (final ApiManagerServiceException e) {
						log.warn("Not possible remove authorization for user {} on api {} - V{}: {}", userId,
								api.getIdentification(), api.getNumversion(), e.getMessage());
					}
				}

			}
		}
	}

	@Override
	public byte[] getImgBytes(String id) {
		final Optional<Api> api = apiRepository.findById(id);
		if (api.isPresent()) {
			return api.get().getImage();
		} else {
			throw new ApiManagerServiceException("Api not found");
		}
	}

	@Override
	public void updateState(String id, String state) {
		final Optional<Api> api = apiRepository.findById(id);
		api.ifPresent(a -> {
			a.setState(Api.ApiStates.valueOf(state));
			apiRepository.save(a);
		});

	}

	@Override
	public void generateToken(String userId) throws GenericOPException {
		final User user = userService.getUser(userId);

		userTokenService.generateToken(user);

	}

	@Override
	public void removeToken(String userId, String tokenJson) throws IOException {
		String token;
		Map<String, String> obj = null;

		obj = new ObjectMapper().readValue(tokenJson, new TypeReference<Map<String, String>>() {
		});

		token = obj.get("token");

		final User user = userService.getUser(userId);

		userTokenService.removeToken(user, token);
	}

	@Override
	public void removeAuthorizationByApiAndUser(String apiId, String userId) {
		final UserApi userApi = userApiRepository.findByApiIdAndUser(apiId, userId);
		if (userApi != null) {
			removeAuthorizationById(userApi.getId());
		}

	}

	@Override
	public boolean hasUserEditAccess(Api api, User user) {
		return isUserOwnerOrAdmin(user, api)
				|| resourceService.hasAccess(user.getUserId(), api.getId(), ResourceAccessType.MANAGE);
	}

	@Override
	public boolean hasUserEditAccess(String apiId, String userId) {
		final User user = userService.getUser(userId);
		final Optional<Api> api = apiRepository.findById(apiId);
		if (api.isPresent()) {
			return hasUserEditAccess(api.get(), user);
		} else {
			return false;
		}
	}

	@Override
	public boolean hasUserAccess(Api api, User user) {
		return isUserOwnerOrAdmin(user, api)
				|| (api.isPublic() || userApiRepository.findByApiIdAndUser(api.getId(), user.getUserId()) != null)
						&& isApiStateValidForUserAccess(api)
				|| resourceService.hasAccess(user.getUserId(), api.getId(), ResourceAccessType.VIEW)
						&& isApiStateValidForUserAccess(api);
	}

	@Override
	public boolean hasUserAccess(String apiId, String userId) {
		final User user = userService.getUser(userId);
		final Optional<Api> api = apiRepository.findById(apiId);
		if (api.isPresent()) {
			return hasUserAccess(api.get(), user);
		} else {
			return false;
		}
	}

	@Override
	public boolean isApiStateValidForUserAccess(String apiId) {
		final Optional<Api> api = apiRepository.findById(apiId);
		if (api.isPresent()) {
			return isApiStateValidForUserAccess(api.get());
		} else {
			return false;

		}

	}

	@Override
	public boolean isApiStateValidForUserAccess(Api api) {
		return !api.getState().name().equals(Api.ApiStates.CREATED.name())
				&& !api.getState().name().equals(Api.ApiStates.DELETED.name());
	}

	@Override
	public boolean isApiStateValidForEdit(String apiId) {
		final Optional<Api> api = apiRepository.findById(apiId);
		if (api.isPresent()) {
			return isApiStateValidForEdit(api.get());
		} else {
			return false;
		}
	}

	@Override
	public boolean isApiStateValidForEditAuth(String apiId) {
		final Optional<Api> api = apiRepository.findById(apiId);
		if (api.isPresent()) {
			return isApiStateValidForEditAuth(api.get());
		} else {
			return false;
		}
	}

	@Override
	public boolean isApiStateValidForEdit(Api api) {
		return api.getState().name().equals(Api.ApiStates.CREATED.name())
				|| api.getState().name().equals(Api.ApiStates.DEVELOPMENT.name());
	}

	@Override
	public boolean isApiStateValidForEditAuth(Api api) {
		return api.getState().name().equals(Api.ApiStates.CREATED.name())
				|| api.getState().name().equals(Api.ApiStates.DEVELOPMENT.name())
				|| api.getState().name().equals(Api.ApiStates.PUBLISHED.name());
	}

	@Override
	public boolean isUserOwnerOrAdmin(User user, Api api) {
		return user.equals(api.getUser()) || userService.isUserAdministrator(user);
	}

	@Override
	public void updateApiPostProcess(String apiId, String postProcessFx) {
		final Optional<Api> api = apiRepository.findById(apiId);
		api.ifPresent(a -> {
			if (a.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
				// Only one operation should exist for this type of apis
				ApiOperation operation = apiOperationRepository.findAllByApi(a).stream().findFirst().orElse(null);
				if (operation != null) {
					operation.setPostProcess(postProcessFx);
				} else {
					operation = new ApiOperation();
					operation.setIdentification(a.getIdentification());
					operation.setApi(a);
					operation.setPostProcess(postProcessFx);
					operation.setOperation(Type.GET);
					operation.setDescription("Post process operation");

				}
				apiOperationRepository.save(operation);

			}
		});

	}

	@Override
	public boolean postProcess(Api api) {

		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final ApiOperation operation = apiOperationRepository.findAllByApi(api).stream().findFirst().orElse(null);
			if (operation != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getPostProccess(Api api) {
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			final ApiOperation operation = apiOperationRepository.findAllByApi(api).stream().findFirst().orElse(null);
			if (operation != null) {
				return operation.getPostProcess();
			}
		}
		return "";
	}

	@Override
	public List<ApiOperation> getOperations(Api api) {
		return apiOperationRepository.findByApiOrderByOperationDesc(api);

	}

	@Override
	public List<ApiOperation> getOperationsByMethod(Api api, Type method) {
		return apiOperationRepository.findByApiAndOperation(api, method);
	}

	@Override
	public Api getById(String id) {
		return apiRepository.findById(id).orElse(null);
	}

	private Api getApiByIdentificationAndVersion(String apiId, String version) {
		Api api = null;
		int versionInt = 0;
		try {
			if (version.toLowerCase().startsWith("v")) {
				version = version.substring(1);
			}
			versionInt = Integer.valueOf(version);
			api = apiRepository.findByIdentificationAndNumversion(apiId, versionInt);
		} catch (final NumberFormatException e) {
			log.info("Not possible to convert version str {} to int", version);
		}
		return api;
	}

	@Override
	public Api getApiByIdentificationVersionOrId(String apiId, String version) {
		Api api = null;
		// get by identification, version
		if (version != null && !version.equals("")) {
			api = getApiByIdentificationAndVersion(apiId, version);
		} else {
			// get by id (retrocomp)
			api = getById(apiId);
		}
		return api;
	}

	@Override
	public List<Api> getAllApis(User user) {
		List<Api> apis = new ArrayList<>();
		if (userService.isUserAdministrator(user)) {
			apis = apiRepository.findAllOrderByDate();
		}
		return apis;
	}

	@Override
	public List<Api> getApisOfOwner(User user) {
		return apiRepository.findByUserOrderByDate(user.getUserId());
	}

	@Override
	public List<Api> getApisOfOwnerAndIdentification(User user, String identification) {
		return apiRepository.findByIdentificationAndUser(identification, user);
	}

	public List<ApiOperation> getApiOperationByApiAndIdentification(Api api, String identification) {
		return apiOperationRepository.findByApiAndIdentification(api, identification);
	}

	@Override
	public void updateApi(Api api) {
		apiRepository.save(api);
	}

	@Override
	public UserApi getUserApiAccessById(String id) {
		return userApiRepository.findById(id).orElse(null);
	}

	@Override
	public UserApi getUserApiByIdAndUser(String apiId, String userId) {
		return userApiRepository.findByApiIdAndUser(apiId, userId);
	}

	@Override
	public List<UserApi> getUserApiByApiId(String apiId) {
		return userApiRepository.findByApiId(apiId);
	}

	@Override
	public Api createApiRest(Api api, List<ApiOperation> operations, List<UserApi> authentications) {
		return createApiRest(api, operations, authentications, 0);
	}

	@Override
	@Transactional
	public Api createApiRest(Api api, List<ApiOperation> operations, List<UserApi> authentications,
			int forcedNumVersion) {
		try {
			Api createdApi = null;

			if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_ONTOLOGY,
						ERROR_MISSING_ONTOLOGY);
			}
			if (api.getIdentification() == null || api.getIdentification().equals("")) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_API_IDENTIFICATION,
						ERROR_MISSING_API_IDENTIFICATION);
			}
			if (operations == null) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_OPERATIONS,
						ERROR_MISSING_OPERATIONS);
			}

			if (forcedNumVersion <= 0) {
				api.setNumversion(calculateNumVersion(api.getIdentification(), api.getApiType()));
			} else {
				api.setNumversion(forcedNumVersion);
			}

			createdApi = apiRepository.save(api);

			if (!authentications.isEmpty()) {
				userApiRepository.saveAll(authentications);
			}

			if (!operations.isEmpty()) {
				for (final ApiOperation operation : operations) {
					saveApiOperation(operation);
				}
			}

			metricsManagerLogControlPanelApiCreation(api, OK);

			return createdApi;

		} catch (final RuntimeException e) {
			metricsManagerLogControlPanelApiCreation(api, KO);
			log.error("Error creating API", e);
			throw e;
		}
	}

	@Override
	public Api importApiRest(Api api, List<ApiOperation> operations, List<UserApi> authentications, boolean overwrite,
			String userId) {
		Api importedApi = null;
		final com.minsait.onesait.platform.config.model.Api existingApi = getApiByIdentificationVersionOrId(
				api.getIdentification(), String.valueOf(api.getNumversion()));
		final User user = userService.getUser(userId);

		if (existingApi != null) {
			if (!overwrite) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.EXISTING_API,
						"Existing api found");
			}

			if (!isUserOwnerOrAdmin(user, existingApi)) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.PERMISSION_DENIED,
						ERROR_USER_NOT_ALLOWED);
			}
			final List<com.minsait.onesait.platform.config.model.ApiOperation> existingApiops = apiOperationRepository
					.findByApiIdOrderByOperationDesc(existingApi.getId());
			final List<UserApi> existingUsersapi = userApiRepository.findByApiId(api.getId());

			try {
				importedApi = api;
				importedApi.setId(updateApiRest(api, existingApi, operations, authentications, true));
			} catch (final Exception e) {
				createApiRest(existingApi, existingApiops, existingUsersapi);
			}

		} else {
			importedApi = createApiRest(api, operations, authentications, api.getNumversion());
		}
		return importedApi;
	}

	public Api getLastVersionOfApiByApiAndUser(Api api, User user) {
		Api lastApi = null;
		final List<Api> existingApisWithIdentificationAndUser = getApisOfOwnerAndIdentification(user,
				api.getIdentification());
		if (!existingApisWithIdentificationAndUser.isEmpty()) {

			lastApi = existingApisWithIdentificationAndUser.get(0);
			for (final Api existingApi : existingApisWithIdentificationAndUser) {
				if (existingApi.getNumversion() > lastApi.getNumversion()) {
					lastApi = existingApi;
				}
			}
		}

		return lastApi;
	}

	@Override
	public Api versionateApiRest(Api api, List<ApiOperation> operations, List<UserApi> authentications, User user) {
		// create api with owner previous api owner, state DEVELOPMENT if previous api
		// is not editable
		try {
			Api updatedApi = null;

			if (api.getIdentification() == null || api.getIdentification().equals("")) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_API_IDENTIFICATION,
						ERROR_MISSING_API_IDENTIFICATION);
			}

			if (operations == null) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_OPERATIONS,
						ERROR_MISSING_OPERATIONS);
			}

			final Api lastApi = getLastVersionOfApiByApiAndUser(api, user);

			if (lastApi == null) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.NOT_FOUND, ERROR_API_NOT_FOUND);
			}

			if (!isApiStateValidForEdit(lastApi)) {
				throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_STATE,
						ERROR_API_INVALID_STATE);
			}

			if (api.getOntology() == null && api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)) {
				api.setOntology(lastApi.getOntology());
			}

			api.setNumversion(lastApi.getNumversion() + 1);
			api.setUser(lastApi.getUser());
			api.setState(ApiStates.DEVELOPMENT);

			updatedApi = apiRepository.save(api);

			if (!authentications.isEmpty()) {
				userApiRepository.saveAll(authentications);
			}

			if (!operations.isEmpty()) {
				for (final ApiOperation operation : operations) {
					saveApiOperation(operation);
				}
			}

			metricsManagerLogControlPanelApiCreation(api, OK);

			return updatedApi;

		} catch (final RuntimeException e) {
			metricsManagerLogControlPanelApiCreation(api, KO);
			log.error("Error versionating API", e);
			throw e;
		}
	}

	@Override
	public String updateApiRest(Api apinew, Api apimemory, List<ApiOperation> operations, List<UserApi> authentications,
			boolean isImportingApi) {

		// ------- Edition policy --------
		// identification..............KO
		// version.....................KO
		// category......................OK
		// state.......................KO (OK if importing)
		// type........................KO
		// ontology....................KO
		// requests per second............OK
		// public........................OK
		// endpoint base...............KO
		// endpoint swagger............KO
		// description...................OK
		// meta-inf......................OK
		// publish to gravitee...........OK
		// image.........................OK
		// operations....................OK
		// authorizations................OK
		// swagger json .................OK

		apimemory.setPublic(apinew.isPublic());
		apimemory.setSsl_certificate(apinew.isSsl_certificate());
		apimemory.setDescription(apinew.getDescription());
		apimemory.setCategory(apinew.getCategory());
		apimemory.setMetaInf(apinew.getMetaInf());

		Integer limit = apinew.getApilimit();

		if (limit != null && limit <= 0) {
			limit = 1;
		}
		apimemory.setApilimit(limit);

		if (validateState(apimemory.getState(), apinew.getState().toString()) || isImportingApi) {
			apimemory.setState(apinew.getState());
		}

		if (apimemory.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			apimemory.setSwaggerJson(apinew.getSwaggerJson());
		}

		if (apinew.getImage() != null && apinew.getImage().length > 0) {
			apimemory.setImage(apinew.getImage());
		}

		apiRepository.save(apimemory);

		updateAuthorizationsRest(apimemory, authentications);
		updateApiOperations(apimemory, operations);

		return apimemory.getId();
	}

	public void updateApiOperations(Api api, List<ApiOperation> operations) {
		if (!operations.isEmpty()) {
			final List<ApiOperation> apiOperations = apiOperationRepository.findAllByApi(api);
			for (final ApiOperation apiOperation : apiOperations) {

				for (final ApiQueryParameter apiQueryParameter : apiOperation.getApiqueryparameters()) {
					apiQueryParameterRepository.delete(apiQueryParameter);
				}
				apiOperationRepository.delete(apiOperation);
			}
			for (final ApiOperation operation : operations) {
				operation.setApi(api);
				saveApiOperation(operation);
			}
		}
	}

	public void saveApiOperation(ApiOperation operation) {
		apiOperationRepository.save(operation);

		if (!operation.getApiqueryparameters().isEmpty()) {
			apiQueryParameterRepository.saveAll(operation.getApiqueryparameters());
		}
	}

	private void updateAuthorizationsRest(Api apimemory, List<UserApi> authentications) {
		final List<UserApi> userapis = userApiRepository.findByApiId(apimemory.getId());
		for (final UserApi userapi : userapis) {
			removeAuthorizationById(userapi.getId());
		}
		for (final UserApi userapinew : authentications) {
			userApiRepository.save(userapinew);
		}
	}

	@Override
	public boolean validateState(ApiStates oldState, String newState) {
		switch (newState.toUpperCase()) {
		case "CREATED": {
			if (!oldState.equals(ApiStates.CREATED)) {
				return false;
			}
			break;
		}
		case "DEVELOPMENT": {
			if (!oldState.equals(ApiStates.CREATED) && !oldState.equals(ApiStates.DEVELOPMENT)) {
				return false;
			}
			break;
		}
		case "PUBLISHED": {
			if (!oldState.equals(ApiStates.CREATED) && !oldState.equals(ApiStates.DEVELOPMENT)
					&& !oldState.equals(ApiStates.PUBLISHED)) {
				return false;
			}
			break;
		}
		case "DEPRECATED": {
			if (!oldState.equals(ApiStates.PUBLISHED) && !oldState.equals(ApiStates.DEPRECATED)) {
				return false;
			}
			break;
		}
		case "DELETED": {
			if (!oldState.equals(ApiStates.DEPRECATED) && !oldState.equals(ApiStates.DELETED)) {
				return false;
			}
			break;
		}
		default: {
			return false;
		}
		}
		return true;
	}

	private void metricsManagerLogControlPanelApiCreation(String userId, String result) {
		try {
			if (null != metricsManager) {
				metricsManager.logControlPanelApiCreation(userId, result);
			}
		} catch (final NullPointerException e) {
			log.error(NOT_POSSIBLE_NOTIFY_ROUTER, e.getMessage());
		}
	}

	private void metricsManagerLogControlPanelApiCreation(Api api, String result) {
		try {
			metricsManager.logControlPanelApiCreation(api.getUser().getUserId(), result);
		} catch (final NullPointerException e) {
			log.error(NOT_POSSIBLE_NOTIFY_ROUTER, e.getMessage());

		}
	}

	@Override
	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description) {
		User user = userService.getUser(userId);
		if (user.isAdmin()) {
			return apiRepository.findAllDto(identification, description);
		} else {
			return apiRepository.findDtoByUserAndPermissions(user, identification, description);
		}
	}

	@Override
	@Transactional
	public String cloneApi(String id, String identification, String userId) {

		final Api api = getById(id);
		final User user = userService.getUser(userId);

		try {

			final Api newApi = copyApi(api, identification, user);
			final String numversionData = "{\"identification\":\"" + newApi.getIdentification() + "\",\"apiType\":\""
					+ newApi.getApiType() + "\"}";
			newApi.setNumversion(calculateNumVersion(numversionData));

			apiRepository.save(newApi);

			List<ApiOperation> operationList = copyOperationList(getOperations(api), newApi);
			if (!operationList.isEmpty()) {
				for (ApiOperation operation : operationList) {
					saveApiOperation(operation);
				}
			}

			metricsManagerLogControlPanelApiCreation(user.getUserId(), OK);

			return newApi.getId();

		} catch (final Exception e) {
			metricsManagerLogControlPanelApiCreation(user.getUserId(), KO);
			log.error("Error clonning API", e);
			throw e;
		}

	}

	private Api copyApi(Api api, String identification, User user) {

		final Api newApi = new Api();

		newApi.setIdentification(identification);
		newApi.setApiType(api.getApiType());
		newApi.setPublic(api.isPublic());
		newApi.setDescription(api.getDescription());
		newApi.setCategory(api.getCategory());
		newApi.setOntology(api.getOntology());
		newApi.setEndpointExt(api.getEndpointExt());
		newApi.setMetaInf(api.getMetaInf());
		newApi.setImageType(api.getImageType());
		newApi.setState(Api.ApiStates.CREATED);
		newApi.setSsl_certificate(api.isSsl_certificate());
		newApi.setUser(user);
		newApi.setApicachetimeout(api.getApicachetimeout());
		newApi.setApilimit(api.getApilimit());
		newApi.setSwaggerJson(api.getSwaggerJson());
		newApi.setImage(api.getImage());
		newApi.setApiType(api.getApiType());

		return newApi;
	}

	private List<ApiOperation> copyOperationList(List<ApiOperation> operationList, Api api) {
		List<ApiOperation> newOperationList = new ArrayList<>();
		for (ApiOperation operation : operationList) {
			ApiOperation newOperation = new ApiOperation();
			newOperation.setApi(api);
			newOperation.setIdentification(operation.getIdentification());
			newOperation.setDescription(operation.getDescription());
			newOperation.setOperation(operation.getOperation());
			newOperation.setEndpoint(operation.getEndpoint());
			newOperation.setBasePath(operation.getBasePath());
			newOperation.setPath(operation.getPath());
			newOperation.setPostProcess(operation.getPostProcess());

			Set<ApiQueryParameter> apiQueryParameterList = operation.getApiqueryparameters();
			if (apiQueryParameterList != null && !apiQueryParameterList.isEmpty()) {
				Set<ApiQueryParameter> newApiQueryParameterList = new HashSet<>();
				for (ApiQueryParameter apiQueryParameter : apiQueryParameterList) {
					ApiQueryParameter newApiQueryParameter = new ApiQueryParameter();
					newApiQueryParameter.setApiOperation(newOperation);
					newApiQueryParameter.setName(apiQueryParameter.getName());
					newApiQueryParameter.setDataType(apiQueryParameter.getDataType());
					newApiQueryParameter.setDescription(apiQueryParameter.getDescription());
					newApiQueryParameter.setValue(apiQueryParameter.getValue());
					newApiQueryParameter.setCondition(apiQueryParameter.getCondition());
					newApiQueryParameter.setHeaderType(apiQueryParameter.getHeaderType());
					newApiQueryParameterList.add(newApiQueryParameter);
				}
				newOperation.setApiqueryparameters(newApiQueryParameterList);
			}
			newOperationList.add(newOperation);
		}
		return newOperationList;
	}

	@Override
	public Boolean isGraviteeApi(String apiId) {
		final Api api = getById(apiId);
		return !(api.getGraviteeId() == null || api.getGraviteeId().isEmpty());
	}
}

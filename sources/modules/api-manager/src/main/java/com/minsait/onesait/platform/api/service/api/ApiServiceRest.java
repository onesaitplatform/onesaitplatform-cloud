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
package com.minsait.onesait.platform.api.service.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.rest.api.fiql.OperationFIQL;
import com.minsait.onesait.platform.api.rest.api.fiql.QueryParameterFIQL;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiQueryParameterRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiQueryParameterDTO;
import com.minsait.onesait.platform.config.services.apimanager.dto.OperacionDTO;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

@Service
public class ApiServiceRest {

	@Autowired
	private ApiFIQL apiFIQL;

	@Autowired
	private ApiRepository apiRepository;

	@Autowired
	private ApiOperationRepository apiOperationRepository;

	@Autowired
	private ApiQueryParameterRepository apiQueryParameterRepository;

	@Autowired
	private UserApiRepository userApiRepository;

	@Autowired
	private ApiSecurityService apiSecurityService;

	@Autowired
	private UserTokenRepository userTokenRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String NOT_ALLOWED_OPERATIONS = "com.indra.sofia2.web.api.services.NoPermisosOperacion";

	private static final String NO_API = "com.indra.sofia2.web.api.services.NoApi";

	private static final String NON_EXISTENT_SUSCRIPTION = "com.indra.sofia2.web.api.services.SuscripcionNoExiste";

	private static final String NON_EXISTENT_API = "com.indra.sofia2.web.api.services.ApiNoExiste";

	private static final String NON_PUBLISHED_API = "com.indra.sofia2.web.api.services.ApiNoPublicada";

	private static final String WRONGVERSIONMIN = "com.indra.sofia2.web.api.services.wrongversionMin";

	private static final String REQUIRED_ID_API = "com.indra.sofia2.web.api.services.IdentificacionApiRequerido";

	private static final String REQUIRED_ID_SCRIPT = "com.indra.sofia2.web.api.services.IdentificacionScriptRequerido";

	private static final String REQUIRED_USER_TOKEN = "com.indra.sofia2.web.api.services.TokenUsuarioApiRequerido";

	private static final String NOT_ALLOWED_USER_OPERATION = "com.indra.sofia2.web.api.services.NoPermisosOperacionUsuario";

	public List<Api> findApisByUser(String userId, String token) {

		return apiRepository.findByUser(apiSecurityService.getUser(userId)).stream()
				.filter(a -> apiSecurityService.authorized(a, token)).collect(Collectors.toList());
	}

	public Api getApi(String identificationApi) {
		Api api = null;
		final List<Api> apis = apiRepository.findByIdentification(identificationApi);
		for (final Api apiAux : apis) {
			if (apiAux.getState().name().equalsIgnoreCase(Api.ApiStates.PUBLISHED.name())) {
				api = apiAux;
			}
		}
		if (api == null) {
			throw new IllegalArgumentException(NON_PUBLISHED_API);
		}
		return api;
	}

	public Api getApiByIdentificationAndVersion(String identification, String numVersion) {
		final Api api = apiRepository.findByIdentificationAndNumversion(identification, new Integer(numVersion));

		if (null == api) {
			throw new IllegalArgumentException(NON_EXISTENT_API);
		}
		return api;
	}

	public Api getApiMaxVersion(String identificationApi) {
		Api api = null;
		final List<Api> apis = apiRepository.findByIdentification(identificationApi);
		for (final Api apiAux : apis) {
			if (api == null || api.getNumversion() < apiAux.getNumversion()) {
				api = apiAux;
			}
		}
		if (api == null) {
			throw new IllegalArgumentException(NON_EXISTENT_API);
		}
		return api;
	}

	public Api findApi(String identification, String token) {
		final Api api = getApiMaxVersion(identification);
		if (api != null) {
			if (apiSecurityService.authorized(api, token)) {
				return api;
			}
		} else {
			throw new IllegalArgumentException(NON_EXISTENT_API);
		}
		return null;
	}

	public List<Api> findApis(String identification, String token) {
		final User user = apiSecurityService.getUserByApiToken(token);
		if (apiSecurityService.isAdmin(user)) {
			return apiRepository.findByIdentification(identification);
		} else {
			return apiRepository.findByIdentificationAndUser(identification, user);
		}
	}

	public Api changeState(String indentifier, ApiStates api, String token) {

		final User user = apiSecurityService.getUserByApiToken(token);
		if (apiSecurityService.isAdmin(user)) {
			final List<Api> apis = apiRepository.findByIdentification(indentifier);
			if (apis != null) {
				final Api theApi = apis.get(0);
				theApi.setState(api);
				apiRepository.saveAndFlush(theApi);
				return theApi;
			} else {
				return null;
			}
		}
		return null;
	}

	public void createApi(ApiDTO apiDTO, String token) {
		final User user = apiSecurityService.getUserByApiToken(token);
		final Api api = apiFIQL.copyProperties(apiDTO, user);

		Integer numVersion = 0;
		final List<Api> apis = apiRepository.findByIdentification(api.getIdentification());
		for (final Api apiBD : apis) {
			if (numVersion < apiBD.getNumversion()) {
				numVersion = apiBD.getNumversion();
			}
		}
		if (numVersion >= api.getNumversion()) {
			throw new IllegalArgumentException(WRONGVERSIONMIN);
		}

		api.setUser(user);
		api.setState(Api.ApiStates.CREATED);
		apiRepository.saveAndFlush(api);
		createOperations(apiDTO.getOperations(), api);
	}

	public void updateApi(ApiDTO apiDTO, String token) {
		try {
			final User user = apiSecurityService.getUserByApiToken(token);
			final Api api = apiFIQL.copyProperties(apiDTO, user);

			Api apiUpdate = apiRepository.findByIdentificationAndNumversion(api.getIdentification(),
					api.getNumversion());
			if (apiSecurityService.authorized(api, token)) {
				apiUpdate = apiFIQL.copyProperties(apiUpdate, api);
				apiRepository.saveAndFlush(apiUpdate);
				updateOperaciones(apiDTO.getOperations(), apiUpdate);

			} else {
				throw new AuthorizationServiceException(NOT_ALLOWED_OPERATIONS);
			}
		} catch (final Exception e) {
			throw new AuthorizationServiceException(NO_API);
		}
	}

	public void removeApi(ApiDTO apiDTO, String token) {
		try {
			final User user = apiSecurityService.getUserByApiToken(token);
			final Api api = apiFIQL.copyProperties(apiDTO, user);
			final Api apiDelete = apiRepository.findByIdentificationAndNumversion(api.getIdentification(),
					api.getNumversion());
			if (apiSecurityService.authorized(apiDelete, token)) {
				removeOperations(apiDelete);
				apiRepository.delete(apiDelete);
			} else {
				throw new AuthorizationServiceException(NOT_ALLOWED_OPERATIONS);
			}
		} catch (final Exception e) {
			throw new AuthorizationServiceException(NO_API);
		}
	}

	public void removeApiByIdentificationNumversion(String identification, String numversion, String token) {
		Integer version = null;
		try {
			version = Integer.parseInt(numversion);
		} catch (final Exception e) {
			throw new AuthorizationServiceException(WRONGVERSIONMIN);
		}
		try {
			final Api apiDelete = apiRepository.findByIdentificationAndNumversion(identification, version);
			if (apiSecurityService.authorized(apiDelete, token)) {
				removeOperations(apiDelete);
				apiRepository.delete(apiDelete);
			} else {
				throw new AuthorizationServiceException(NOT_ALLOWED_OPERATIONS);
			}
		} catch (final Exception e) {
			throw new AuthorizationServiceException(NO_API);
		}
	}

	private void createOperations(ArrayList<OperacionDTO> operaciones, Api api) {
		for (final OperacionDTO operacionDTO : operaciones) {
			final ApiOperation operacion = OperationFIQL.copyProperties(operacionDTO);
			operacion.setIdentification(operacionDTO.getIdentification());
			operacion.setApi(api);
			apiOperationRepository.saveAndFlush(operacion);
			if (operacionDTO.getQueryParams() != null)
				createQueryParams(operacion, operacionDTO.getQueryParams());
		}
	}

	private void updateOperaciones(ArrayList<OperacionDTO> operacionesDTO, Api api) {
		removeOperations(api);
		createOperations(operacionesDTO, api);
	}

	private void removeOperations(Api api) {
		final List<ApiOperation> operaciones = apiOperationRepository.findByApiOrderByOperationDesc(api);
		for (final ApiOperation operacion : operaciones) {
			apiOperationRepository.delete(operacion);
		}
	}

	private void createQueryParams(ApiOperation operacion, List<ApiQueryParameterDTO> list) {
		for (final ApiQueryParameterDTO queryParamDTO : list) {
			final ApiQueryParameter apiQueryParam = QueryParameterFIQL.copyProperties(queryParamDTO);
			apiQueryParam.setApiOperation(operacion);

			apiQueryParameterRepository.saveAndFlush(apiQueryParam);

		}
	}

	public UserApi findApiSuscriptions(String identificationApi, String tokenUsuario) {
		if (identificationApi == null) {
			throw new IllegalArgumentException(REQUIRED_ID_API);
		}
		if (tokenUsuario == null) {
			throw new IllegalArgumentException(REQUIRED_USER_TOKEN);
		}

		final Api api = findApi(identificationApi, tokenUsuario);
		UserApi suscription = null;

		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		suscription = userApiRepository.findByApiIdAndUser(api.getId(), user.getUserId());

		return suscription;
	}

	public UserApi findApiSuscriptions(Api api, User user) {
		UserApi suscription = null;
		suscription = userApiRepository.findByApiIdAndUser(api.getId(), user.getUserId());
		return suscription;
	}

	public List<UserApi> findApiSuscripcionesUser(String identificationUsuario) {
		List<UserApi> suscriptions = null;

		if (identificationUsuario == null) {
			throw new IllegalArgumentException(REQUIRED_ID_API);
		}

		final User suscriber = apiSecurityService.getUser(identificationUsuario);
		suscriptions = userApiRepository.findByUser(suscriber);
		return suscriptions;
	}

	private boolean authorizedOrSuscriptor(Api api, String tokenUsuario, String suscriptor) {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		return (apiSecurityService.isAdmin(user) || user.getUserId().equals(api.getUser().getUserId())
				|| user.getUserId().equals(suscriptor));
	}

	public void createSuscripcion(UserApi suscription, String tokenUsuario) {
		if (authorizedOrSuscriptor(suscription.getApi(), tokenUsuario, suscription.getUser().getUserId())) {
			try {
				final UserApi apiUpdate = findApiSuscriptions(suscription.getApi(), suscription.getUser());
				if (apiUpdate == null) {
					userApiRepository.save(suscription);
				}
			} catch (final Exception e) {
				throw new IllegalArgumentException(NON_EXISTENT_SUSCRIPTION);
			}
		} else {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.NoAutorizado");
		}
	}

	public void updateSuscripcion(UserApi suscription, String tokenUsuario) {
		if (authorizedOrSuscriptor(suscription.getApi(), tokenUsuario, suscription.getUser().getUserId())) {
			try {
				final UserApi apiUpdate = findApiSuscriptions(suscription.getApi(), suscription.getUser());
				if (apiUpdate != null) {
					apiUpdate.setCreatedAt(suscription.getCreatedAt());
					apiUpdate.setUpdatedAt(suscription.getUpdatedAt());
					userApiRepository.save(apiUpdate);
				}

			} catch (final Exception e) {
				throw new IllegalArgumentException(NON_EXISTENT_SUSCRIPTION);
			}
		}
	}

	public void removeSuscripcionByUserAndAPI(UserApi suscription, String tokenUsuario) {
		if (authorizedOrSuscriptor(suscription.getApi(), tokenUsuario, suscription.getUser().getUserId())) {
			try {
				final UserApi apiUpdate = findApiSuscriptions(suscription.getApi(), suscription.getUser());
				if (apiUpdate != null) {
					userApiRepository.delete(apiUpdate);
				}

			} catch (final Exception e) {
				throw new IllegalArgumentException(NON_EXISTENT_SUSCRIPTION);
			}
		}
	}

	public UserToken findTokenUserByIdentification(String identification, String tokenUsuario) {

		UserToken token = null;
		if (identification == null) {
			throw new IllegalArgumentException(REQUIRED_ID_SCRIPT);
		}

		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(identification)) {
			final User userToTokenize = apiSecurityService.getUser(identification);
			token = apiSecurityService.getUserToken(userToTokenize, identification);
		} else {
			throw new AuthorizationServiceException(NOT_ALLOWED_USER_OPERATION);
		}
		return token;
	}

	public UserToken addTokenUsuario(String identification, String tokenUsuario) {
		UserToken token = null;
		if (identification == null) {
			throw new IllegalArgumentException(REQUIRED_ID_SCRIPT);
		}

		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(identification)) {

			final User userToTokenize = apiSecurityService.getUser(identification);

			token = apiSecurityService.getUserToken(userToTokenize, tokenUsuario);
			if (token == null)
				token = initToken(userToTokenize);
			else {
				token = initToken(userToTokenize);
			}

		} else {
			throw new AuthorizationServiceException(NOT_ALLOWED_USER_OPERATION);
		}
		return token;
	}

	public UserToken generateTokenUsuario(String identification, String tokenUsuario) {

		UserToken token = null;
		if (identification == null) {
			throw new IllegalArgumentException(REQUIRED_ID_SCRIPT);
		}

		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);

		if (apiSecurityService.isAdmin(user) || user.getUserId().equals(identification)) {

			final User userToTokenize = apiSecurityService.getUser(identification);

			token = initToken(userToTokenize);

		} else {
			throw new AuthorizationServiceException(NOT_ALLOWED_USER_OPERATION);
		}
		return token;
	}

	public String generateTokenUsuario() {
		String candidateToken = "";
		candidateToken = UUID.randomUUID().toString();
		return candidateToken;
	}

	public UserToken initToken(User user) {

		final UserToken userToken = new UserToken();

		userToken.setToken(generateTokenUsuario());
		userToken.setUser(user);
		userToken.setCreatedAt(Calendar.getInstance().getTime());

		userTokenRepository.save(userToken);
		return userToken;

	}

}
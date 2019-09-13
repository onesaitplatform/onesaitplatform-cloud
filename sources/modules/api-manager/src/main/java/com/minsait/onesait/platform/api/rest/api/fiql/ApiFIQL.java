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
package com.minsait.onesait.platform.api.rest.api.fiql;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.rest.api.dto.ApiDTO;
import com.minsait.onesait.platform.api.rest.api.dto.AutenticacionAtribDTO;
import com.minsait.onesait.platform.api.rest.api.dto.OperacionDTO;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationRepository;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class ApiFIQL {

	@Autowired
	private UserService userService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserApiRepository userApiRepository;

	@Autowired
	private ApiOperationRepository operationRepository;

	@Autowired
	private ApiAuthenticationRepository authenticationRepository;

	public static final String API_PUBLICA = "PUBLIC";
	public static final String API_PRIVADA = "PRIVATE";

	private final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	public List<ApiDTO> toApiDTO(List<Api> apis) {
		final List<ApiDTO> apisDTO = new ArrayList<>();
		for (final Api api : apis) {
			apisDTO.add(toApiDTO(api));
		}
		return apisDTO;
	}

	public ApiDTO toApiDTO(Api api) {
		final ApiDTO apiDTO = new ApiDTO();

		apiDTO.setIdentification(api.getIdentification());
		apiDTO.setVersion(api.getNumversion());
		apiDTO.setIsPublic(api.isPublic());
		apiDTO.setType(api.getApiType().name());

		apiDTO.setCategory(api.getCategory().toString());
		if (api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
			apiDTO.setExternalApi(true);
		} else {
			apiDTO.setExternalApi(false);
		}
		if (api.getOntology() != null) {
			apiDTO.setOntologyId(api.getOntology().getId());
		}
		apiDTO.setEndpoint(api.getEndpoint());
		apiDTO.setEndpointExt(api.getEndpointExt());
		apiDTO.setDescription(api.getDescription());
		apiDTO.setMetainf(api.getMetaInf());
		apiDTO.setImageType(api.getImageType());
		apiDTO.setStatus(api.getState());
		apiDTO.setApiLimit(api.getApilimit());
		apiDTO.setCreationDate(df.format(api.getCreatedAt()));
		if (api.getUser() != null) {
			apiDTO.setUserId(api.getUser().getUserId());
		}

		// Se copian las Operaciones
		final ArrayList<OperacionDTO> operacionesDTO = new ArrayList<>();
		final List<ApiOperation> operaciones = operationRepository.findByApiOrderByOperationDesc(api);
		for (final ApiOperation operacion : operaciones) {
			final OperacionDTO operacionDTO = OperationFIQL.toOperacionDTO(operacion);
			operacionesDTO.add(operacionDTO);
		}

		apiDTO.setOperations(operacionesDTO);

		// Se copia el Objeto Autenticacion
		final List<UserApi> autenticaciones = userApiRepository.findByApiId(api.getId());
		if (autenticaciones != null && !autenticaciones.isEmpty()) {

			// Se copian los atributos
			final ArrayList<AutenticacionAtribDTO> atributosDTO = new ArrayList<>();
			for (final UserApi autenticacion : autenticaciones) {
				final AutenticacionAtribDTO atribDTO = new AutenticacionAtribDTO();
				atribDTO.setApi(autenticacion.getApi().getIdentification());
				atribDTO.setUser(autenticacion.getUser().getUserId());
				atributosDTO.add(atribDTO);
			}

			apiDTO.setAuthentication(atributosDTO);
		}

		return apiDTO;
	}

	public Api copyProperties(ApiDTO apiDTO, User user) {
		final Api api = new Api();

		api.setIdentification(apiDTO.getIdentification());
		api.setNumversion(apiDTO.getVersion());

		api.setPublic(apiDTO.getIsPublic());
		api.setApilimit(apiDTO.getApiLimit());

		api.setCategory(Api.ApiCategories.valueOf(apiDTO.getCategory()));

		if (apiDTO.getOntologyId() != null && !apiDTO.getOntologyId().equals("")) {
			final Ontology ont = ontologyService.getOntologyById(apiDTO.getOntologyId(), user.getUserId());

			if (ont == null) {
				throw new IllegalArgumentException("com.indra.sofia2.web.api.services.wrongOntology");
			}

			api.setOntology(ont);
		}

		api.setApiType(ApiType.valueOf(apiDTO.getType()));
		api.setEndpoint(apiDTO.getEndpoint());
		api.setEndpointExt(apiDTO.getEndpointExt());
		api.setDescription(apiDTO.getDescription());
		api.setMetaInf(apiDTO.getMetainf());
		api.setState(apiDTO.getStatus());

		try {
			if (apiDTO.getCreationDate() != null && !apiDTO.getCreationDate().equals("")) {
				api.setCreatedAt(df.parse(apiDTO.getCreationDate()));
			}
		} catch (final ParseException ex) {
			throw new IllegalArgumentException("com.indra.sofia2.web.api.services.WrongDateFormat");
		}

		if (apiDTO.getUserId() != null) {
			final User userApiDTO = userService.getUser(apiDTO.getUserId());
			if (userApiDTO != null) {
				api.setUser(userApiDTO);
			}
		}

		return api;
	}

	public Api copyProperties(Api apiUpdate, Api api) {
		apiUpdate.setIdentification(api.getIdentification());
		apiUpdate.setNumversion(api.getNumversion());
		apiUpdate.setPublic(api.isPublic());
		apiUpdate.setCategory(api.getCategory());

		apiUpdate.setOntology(api.getOntology());

		apiUpdate.setEndpoint(api.getEndpoint());
		apiUpdate.setEndpointExt(api.getEndpointExt());
		apiUpdate.setDescription(api.getDescription());
		apiUpdate.setMetaInf(api.getMetaInf());
		apiUpdate.setState(api.getState());
		apiUpdate.setCreatedAt(api.getCreatedAt());

		return apiUpdate;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ApiOperationRepository getOperationRepository() {
		return operationRepository;
	}

	public void setOperationRepository(ApiOperationRepository operationRepository) {
		this.operationRepository = operationRepository;
	}

	public OntologyService getOntologyService() {
		return ontologyService;
	}

	public void setOntologyService(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	public ApiAuthenticationRepository getAuthenticationRepository() {
		return authenticationRepository;
	}

	public void setAuthenticationRepository(ApiAuthenticationRepository authenticationRepository) {
		this.authenticationRepository = authenticationRepository;
	}

}

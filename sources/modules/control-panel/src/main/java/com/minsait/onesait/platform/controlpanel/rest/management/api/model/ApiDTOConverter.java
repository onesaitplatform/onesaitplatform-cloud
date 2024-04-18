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
package com.minsait.onesait.platform.controlpanel.rest.management.api.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.ApiQueryParameterDTO;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Component
public class ApiDTOConverter {

	private static final String INVALID_API_TYPE_MSG = "Api type is invalid";
	private static final String ONTOLOGY_NOT_FOUND_MSG = "Ontology not found";
	private static final String DIGITALFLOW_NOT_FOUND_MSG = "Digital Flow not found";
	private static final String DUPLICATED_OPS_MSG = "Found duplicated operation identifications";

	private String noderedProxyUrl;

	@Autowired
	IntegrationResourcesService resourcesService;

	@Autowired
	OntologyService ontologyService;

	@Autowired
	FlowDomainService flowDomainService;

	@Autowired
	UserService userService;
	
	@PostConstruct
	public void init() {
		noderedProxyUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.ADVICE);		
	}

	public Api toAPI(ApiRestDTO apiRestDTO, User user, ApiStates apiState) {
		com.minsait.onesait.platform.config.model.Api api = new com.minsait.onesait.platform.config.model.Api();

		ApiType apiType = null;
		try {
			apiType = ApiType.valueOf(apiRestDTO.getType().toUpperCase());
		} catch (final IllegalArgumentException e) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_TYPE,
					INVALID_API_TYPE_MSG);
		}

		if (apiType == null || !((apiType.equals(ApiType.INTERNAL_ONTOLOGY))
				|| (apiType.equals(ApiType.EXTERNAL_FROM_JSON)) || (apiType.equals(ApiType.NODE_RED)))) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.INVALID_API_TYPE,
					INVALID_API_TYPE_MSG);
		}

		api.setUser(user);
		setCommonAttributes(apiRestDTO, api);
		setApiCategory(apiRestDTO, api);
		setApiState(api, apiState);

		if (apiType.equals(ApiType.INTERNAL_ONTOLOGY)) {
			toOntologyRestAPI(apiRestDTO, api, user);
		} else if (apiType.equals(ApiType.EXTERNAL_FROM_JSON)) {
			toExternalFromJsonAPI(apiRestDTO, api);
		} else if (apiType.equals(ApiType.NODE_RED)) {
			toNodeRedAPI(apiRestDTO, api);
		}

		return api;
	}

	private void setCommonAttributes(ApiRestDTO apiRestDTO, Api api) {
		api.setIdentification(apiRestDTO.getIdentification());
		api.setNumversion(apiRestDTO.getVersion());
		api.setDescription(apiRestDTO.getDescription());
		api.setMetaInf(apiRestDTO.getMetainf());
		api.setSsl_certificate(false);
		api.setPublic(apiRestDTO.getIsPublic());
		api.setImageType(apiRestDTO.getImageType());
		api.setApilimit(apiRestDTO.getApiLimit());
	}

	private void setApiCategory(ApiRestDTO apiRestDTO, Api api) {
		ApiCategories apiCategory;
		try {
			apiCategory = ApiCategories.valueOf(apiRestDTO.getCategory());
		} catch (final IllegalArgumentException e) {
			apiCategory = ApiCategories.ALL;
		}
		api.setCategory(apiCategory);
	}

	private void setApiState(Api api, ApiStates apiState) {
		if (apiState == null) {
			api.setState(ApiStates.CREATED);
		} else {
			api.setState(apiState);
		}
	}

	private void toOntologyRestAPI(ApiRestDTO apiRestDTO, Api api, User user) {
		api.setApiType(ApiType.INTERNAL_ONTOLOGY);
		api.setOntology(ontologyService.getOntologyById(apiRestDTO.getOntologyId(), user.getUserId()));
		if (api.getOntology() == null) {
			api.setOntology(ontologyService.getOntologyByIdentification(apiRestDTO.getOntologyId()));
		}
		if (api.getOntology() == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_ONTOLOGY,
					ONTOLOGY_NOT_FOUND_MSG);
		}
	}

	private void toExternalFromJsonAPI(ApiRestDTO apiRestDTO, Api api) {
		api.setApiType(ApiType.EXTERNAL_FROM_JSON);
		api.setSwaggerJson(apiRestDTO.getSwaggerJson());
	}

	private void toNodeRedAPI(ApiRestDTO apiRestDTO, Api api) {
		api.setApiType(ApiType.NODE_RED);

		if (apiRestDTO.getEndpointExt() == null) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_DIGITAL_FLOW,
					DIGITALFLOW_NOT_FOUND_MSG);
		}

		String[] endpointParts = apiRestDTO.getEndpointExt().split("/");
		if (endpointParts.length == 0) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_DIGITAL_FLOW,
					DIGITALFLOW_NOT_FOUND_MSG);
		}

		String domainIdentification = endpointParts[endpointParts.length - 1];
		if (!flowDomainService.domainExists(domainIdentification)) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.MISSING_DIGITAL_FLOW,
					DIGITALFLOW_NOT_FOUND_MSG);
		}
		api.setEndpointExt(noderedProxyUrl + domainIdentification);
	}

	public List<UserApi> toUserApi(List<UserApiSimplifiedResponseDTO> userapisdto, List<UserApi> auths, Api api) {
		if (userapisdto != null) {
			for (UserApiSimplifiedResponseDTO userapiDTO : userapisdto) {
				UserApi userapi = new UserApi();
				userapi.setApi(api);
				userapi.setUser(userService.getUser(userapiDTO.getUserId()));
				if (!auths.contains(userapi)) {
					auths.add(userapi);
				}
			}
		}
		return auths;

	}

	public List<String> duplicatedApiOpsIdentifications(List<ApiRestOperationDTO> apiopsDTO) {
		List<String> duplicates = new ArrayList<>();
		List<String> apiOpsIdentifications = new ArrayList<>();

		for (ApiRestOperationDTO aoDTO : apiopsDTO) {
			if (apiOpsIdentifications.contains(aoDTO.getIdentification())) {
				if (!duplicates.contains(aoDTO.getIdentification())) {
					duplicates.add(aoDTO.getIdentification());
				}
			} else {
				apiOpsIdentifications.add(aoDTO.getIdentification());
			}
		}
		return duplicates;
	}

	public List<ApiOperation> toAPIOperations(List<ApiRestOperationDTO> apiopsDTO, List<ApiOperation> apiops, Api api) {

		if (!duplicatedApiOpsIdentifications(apiopsDTO).isEmpty()) {
			throw new ApiManagerServiceException(ApiManagerServiceException.Error.DUPLICATED_OPERATIONS,
					DUPLICATED_OPS_MSG);
		}

		for (ApiRestOperationDTO apiopdto : apiopsDTO) {
			ApiOperation apiop = new ApiOperation();
			apiop.setApi(api);
			apiop.setPath(apiopdto.getPath());
			apiop.setDescription(apiopdto.getDescription());
			apiop.setEndpoint(apiopdto.getEndpoint());
			apiop.setIdentification(apiopdto.getIdentification());
			apiop.setOperation(apiopdto.getOperation());
			apiop.setPostProcess(apiopdto.getPostProcess());
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

}

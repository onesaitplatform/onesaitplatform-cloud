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
package com.minsait.onesait.platform.controlpanel.helper.apimanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiAuthentication;
import com.minsait.onesait.platform.config.model.ApiAuthenticationAttribute;
import com.minsait.onesait.platform.config.model.ApiAuthenticationParameter;
import com.minsait.onesait.platform.config.model.ApiHeader;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.repository.ApiAuthenticationRepository;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.apimanager.authentication.AuthenticationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.HeaderJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.OperationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.QueryStringJson;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.apimanager.UserApiDTO;
import com.minsait.onesait.platform.controlpanel.multipart.ApiMultipart;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Component
public class ApiManagerHelper {

	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserApiRepository userApiRepository;
	@Autowired
	private ApiOperationRepository apiOperationRepository;
	@Autowired
	private ApiAuthenticationRepository apiAuthenticationRepository;
	@Autowired
	private UserTokenRepository userTokenRepository;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private AppWebUtils utils;

	private static final String API_SERVICES_STR = "apiServices";
	private static final String API_SWAGGER_UI_STR = "apiSwaggerUI";
	private static final String USERS_STR = "users";
	private static final String ENDPOINT_BASE_STR = "endpointBase";
	private static final String OPERATIONS_STR = "operations";
	private static final String CLIENTS_STR = "clients";

	// To populate the List Api Form
	public void populateApiManagerListForm(Model uiModel) {
		final List<User> users = userRepository.findAll();

		final User user = userService.getUser(utils.getUserId());
		uiModel.addAttribute(API_SERVICES_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON));
		uiModel.addAttribute(API_SWAGGER_UI_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERUI));
		uiModel.addAttribute(USERS_STR, users);
		uiModel.addAttribute("states", Api.ApiStates.values());
		uiModel.addAttribute("auths", userApiRepository.findByUser(user));
	}

	// To populate the Create Api Form
	public void populateApiManagerCreateForm(Model uiModel) {
		Set<Ontology> ontologies;

		ontologies = new LinkedHashSet<>(ontologyService.getAllOntologies(utils.getUserId()));
		ontologies.addAll(projectService.getResourcesForUserOfType(utils.getUserId(), Ontology.class));

		uiModel.addAttribute(ENDPOINT_BASE_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		uiModel.addAttribute(API_SERVICES_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON));
		uiModel.addAttribute(API_SWAGGER_UI_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERUI));

		uiModel.addAttribute("categories", Api.ApiCategories.values());
		uiModel.addAttribute(OPERATIONS_STR, new ArrayList<String>());
		uiModel.addAttribute("ontologies", ontologies);
		uiModel.addAttribute("api", new Api());
	}

	// To populate de Api Create Form
	public void populateApiManagerUpdateForm(Model uiModel, String apiId) {

		// POPULATE API TAB
		populateApiManagerCreateForm(uiModel);

		final Api api = apiRepository.findById(apiId);

		final List<ApiAuthentication> apiAuthenticacion = apiAuthenticationRepository.findAllByApi(api);
		final AuthenticationJson authenticacion = populateAuthenticationObject(apiAuthenticacion);
		final List<ApiOperation> apiOperations = apiOperationRepository.findAllByApi(api);
		final List<OperationJson> operations = populateOperationsObject(apiOperations);

		uiModel.addAttribute(ENDPOINT_BASE_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		uiModel.addAttribute(API_SERVICES_STR, resourcesService.getUrl(
				com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module.APIMANAGER,
				ServiceUrl.SWAGGERJSON));
		uiModel.addAttribute(API_SWAGGER_UI_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERUI));
		uiModel.addAttribute("authenticacion", authenticacion);
		uiModel.addAttribute(OPERATIONS_STR, operations);
		uiModel.addAttribute("api", api);

		// POPULATE AUTH TAB

		uiModel.addAttribute(CLIENTS_STR, toUserApiDTO(userApiRepository.findByApiId(apiId)));
		uiModel.addAttribute(USERS_STR, userRepository.findUserByIdentificationAndNoRol(utils.getUserId(),
				Role.Type.ROLE_ADMINISTRATOR.toString()));
		if (apiManagerService.postProcess(api))
			uiModel.addAttribute("postProcessFx", apiManagerService.getPostProccess(api));
	}

	private List<UserApiDTO> toUserApiDTO(List<UserApi> findByApiId) {
		final List<UserApiDTO> userApiDTOList = new ArrayList<>();
		for (final UserApi userApi : findByApiId) {
			final UserApiDTO userApiDTO = new UserApiDTO(userApi);
			userApiDTOList.add(userApiDTO);
		}
		return userApiDTOList;
	}

	public void populateApiManagerShowForm(Model uiModel, String apiId) {

		// POPULATE API TAB
		final Api api = apiRepository.findById(apiId);

		final List<ApiAuthentication> apiAuthenticacion = apiAuthenticationRepository.findAllByApi(api);
		final AuthenticationJson authenticacion = populateAuthenticationObject(apiAuthenticacion);
		final List<ApiOperation> apiOperations = apiOperationRepository.findAllByApi(api);
		final List<OperationJson> operations = populateOperationsObject(apiOperations);

		uiModel.addAttribute(ENDPOINT_BASE_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		uiModel.addAttribute(API_SERVICES_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON));
		uiModel.addAttribute(API_SWAGGER_UI_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERUI));
		uiModel.addAttribute("authenticacion", authenticacion);
		uiModel.addAttribute(OPERATIONS_STR, operations);
		uiModel.addAttribute("api", api);
		if (apiManagerService.postProcess(api))
			uiModel.addAttribute("postProcessFx", apiManagerService.getPostProccess(api));

		// POPULATE AUTH TAB
		uiModel.addAttribute(CLIENTS_STR, userApiRepository.findByApiId(apiId));
	}

	private AuthenticationJson populateAuthenticationObject(List<ApiAuthentication> apiAuthentications) {
		if (apiAuthentications != null && !apiAuthentications.isEmpty()) {
			final ApiAuthentication apiAuthentication = apiAuthentications.get(0);
			final AuthenticationJson authenticacion = new AuthenticationJson();
			authenticacion.setType(apiAuthentication.getType());
			authenticacion.setDescription(apiAuthentication.getDescription());

			final List<List<Map<String, String>>> paramList = new ArrayList<>();

			for (final ApiAuthenticationParameter apiparam : apiAuthentication.getApiAuthenticationParameters()) {
				final List<Map<String, String>> params = new ArrayList<>();

				for (final ApiAuthenticationAttribute apiAttrib : apiparam.getApiautenticacionattribs()) {
					final Map<String, String> attrib = new HashMap<>();
					attrib.put("key", apiAttrib.getName());
					attrib.put("value", apiAttrib.getValue());

					params.add(attrib);
				}
				paramList.add(params);
			}
			authenticacion.setParams(paramList);

			return authenticacion;
		}
		return null;
	}

	private static List<OperationJson> populateOperationsObject(List<ApiOperation> apiOperations) {
		final List<OperationJson> operations = new ArrayList<>();

		for (final ApiOperation operation : apiOperations) {
			final OperationJson operationJson = new OperationJson();
			operationJson.setIdentification(operation.getIdentification());
			operationJson.setDescription(operation.getDescription());
			operationJson.setBasepath(operation.getBasePath());
			operationJson.setOperation(operation.getOperation().toString());
			operationJson.setPath(operation.getPath());
			operationJson.setEndpoint(operation.getEndpoint());
			operationJson.setPostprocess(operation.getPostProcess());

			final List<HeaderJson> headers = new ArrayList<>();

			for (final ApiHeader header : operation.getApiheaders()) {
				final HeaderJson headerJson = new HeaderJson();
				headerJson.setName(header.getName());
				headerJson.setDescription(header.getHeader_description());
				headerJson.setType(header.getHeader_type());
				headerJson.setValue(header.getHeader_value());
				headerJson.setCondition(header.getHeader_condition());

				headers.add(headerJson);
			}

			operationJson.setHeaders(headers);

			final List<QueryStringJson> queryStrings = new ArrayList<>();

			for (final ApiQueryParameter apiQueryParameter : operation.getApiqueryparameters()) {
				final QueryStringJson queryStringJson = new QueryStringJson();
				queryStringJson.setName(apiQueryParameter.getName());
				queryStringJson.setDescription(apiQueryParameter.getDescription());
				queryStringJson.setDataType(apiQueryParameter.getDataType().toString());
				queryStringJson.setHeaderType(apiQueryParameter.getHeaderType().toString());
				queryStringJson.setValue(apiQueryParameter.getValue());
				queryStringJson.setCondition(apiQueryParameter.getCondition());

				queryStrings.add(queryStringJson);
			}

			operationJson.setQuerystrings(queryStrings);

			operations.add(operationJson);
		}
		return operations;

	}

	public Api apiMultipartMap(ApiMultipart apiMultipart) {
		final Api api = new Api();

		api.setId(apiMultipart.getId());

		api.setIdentification(apiMultipart.getIdentification());
		api.setApiType(apiMultipart.getApiType());

		api.setPublic(apiMultipart.isPublic());
		api.setDescription(apiMultipart.getDescription());
		api.setCategory(Api.ApiCategories.valueOf(apiMultipart.getCategory()));
		api.setOntology(apiMultipart.getOntology());
		api.setEndpoint(apiMultipart.getEndpoint());
		api.setEndpointExt(apiMultipart.getEndpointExt());
		api.setMetaInf(apiMultipart.getMetaInf());
		api.setImageType(apiMultipart.getImageType());
		if (apiMultipart.getState() == null) {
			api.setState(Api.ApiStates.CREATED);
		} else {
			api.setState(Api.ApiStates.valueOf(apiMultipart.getState()));
		}

		api.setSsl_certificate(apiMultipart.isSslCertificate());

		api.setUser(userService.getUser(utils.getUserId()));

		if (apiMultipart.getCachetimeout() != null) {

			if (apiMultipart.getCachetimeout() > 1000 || apiMultipart.getCachetimeout() < 10) {
				// throw new Exception("Cache Limits exceded");
			} else {
				api.setCachetimeout(apiMultipart.getCachetimeout());
			}
		}

		if (apiMultipart.getApilimit() != null) {
			if (apiMultipart.getApilimit() <= 0) {
				api.setApilimit(1);
			} else {
				api.setApilimit(apiMultipart.getApilimit());
			}
		}

		api.setSwaggerJson(apiMultipart.getSwaggerJson());

		api.setCreatedAt(apiMultipart.getCreatedAt());

		// if (apiMultipart.getImage()!=null && apiMultipart.getImage().getSize()>0 &&
		// !"image/png".equalsIgnoreCase(apiMultipart.getImage().getContentType()) &&
		// !"image/jpeg".equalsIgnoreCase(apiMultipart.getImage().getContentType())
		// && !"image/jpg".equalsIgnoreCase(apiMultipart.getImage().getContentType()) &&
		// !"application/octet-stream".equalsIgnoreCase(apiMultipart.getImage().getContentType())){
		// return null;
		// }
		//

		try {
			api.setImage(apiMultipart.getImage().getBytes());
		} catch (final Exception e) {
			// throw new Exception("ERROR IMAGEN");
		}

		api.setApiType(apiMultipart.getApiType());

		return api;
	}

	public void populateAutorizationForm(Model model) {
		model.addAttribute("userapi", new UserApi());
		model.addAttribute(USERS_STR, userRepository.findUserByIdentificationAndNoRol(utils.getUserId(),
				Role.Type.ROLE_ADMINISTRATOR.toString()));

		if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			model.addAttribute("apis", apiRepository.findApisNotPublicAndPublishedOrDevelopment());

			final List<UserApi> clients = userApiRepository.findAll();
			model.addAttribute(CLIENTS_STR, clients);
		} else if (utils.getRole().equals(Role.Type.ROLE_DEVELOPER.toString())) {

			model.addAttribute("apis",
					apiRepository.findApisByUserNotPublicAndPublishedOrDevelopment(utils.getUserId()));

			final List<UserApi> clients = userApiRepository.findByOwner(utils.getUserId());
			model.addAttribute(CLIENTS_STR, clients);
		}
	}

	public void populateUserTokenForm(Model model) {
		final User user = userService.getUser(utils.getUserId());
		model.addAttribute("tokens", userTokenRepository.findByUser(user));
	}

	public void populateApiManagerInvokeForm(Model model, String apiId) {
		final Api api = apiRepository.findById(apiId);

		model.addAttribute("api", api);
		model.addAttribute(API_SWAGGER_UI_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERUI));
		model.addAttribute(ENDPOINT_BASE_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE));
		model.addAttribute(API_SERVICES_STR, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON));
	}

}

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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.HeaderType;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.exceptions.FlowDomainServiceException;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiInvocationParams;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationParamDTO;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.OpenAPI3Utils;
import com.minsait.onesait.platform.flowengine.exception.FlowengineApiNotFoundException;
import com.minsait.onesait.platform.flowengine.exception.InvalidInvocationParamTypeException;
import com.minsait.onesait.platform.flowengine.exception.NoValueForParamIvocationException;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowEngineApiService {

	@Autowired
	private FlowDomainService domainService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private OpenAPI3Utils openApiUtils;
	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;
	@Autowired
	private ApiInvokerUtils apiInvokerUtils;
	@Autowired
	private ProjectService projectService;

	private static final String ERROR_DOMAIN = "{'error':'Domain ";

	public ResponseEntity<String> invokeRestApiOperation(FlowEngineInvokeRestApiOperationRequest invokeRequest) {

		final long start = System.currentTimeMillis();
		RestApiInvocationParams restInvocationParams;
		ResponseEntity<String> result = null;
		// Search api
		log.debug("get domain by identification");
		final FlowDomain domain = domainService.getFlowDomainByIdentification(invokeRequest.getDomainName());
		User platformUser = null;
		if (domain != null) {
			platformUser = domain.getUser();
		} else {
			log.error("Domain {} not found for API execution.", invokeRequest.getDomainName());
			return new ResponseEntity<>(ERROR_DOMAIN + invokeRequest.getDomainName()
					+ " not found for API invocation named '" + invokeRequest.getApiName() + "'.'}",
					HttpStatus.BAD_REQUEST);
		}
		log.debug("find Api from swagger json");
		final Optional<Api> selectedApi = findApiFromSwaggerJson(invokeRequest, platformUser);
		// Search operation
		if (selectedApi.isPresent()) {
			try {
				if (selectedApi.get().getApiType() == ApiType.INTERNAL_ONTOLOGY
						|| selectedApi.get().getApiType() == ApiType.NODE_RED) {
					log.debug("get invocation parameters");
					restInvocationParams = getInvocaionParametersForInternalOrFlowEngineApi(invokeRequest,
							selectedApi.get());
				} else {
					// API Swagger External
					log.debug("get invocation parameters");
					restInvocationParams = getInvocationParamsForSwaggerOperation(selectedApi.get(), invokeRequest);
				}
			} catch (NoValueForParamIvocationException | InvalidInvocationParamTypeException
					| FlowengineApiNotFoundException e) {
				return new ResponseEntity<>("{'error':'" + e.getMessage() + ".' }", HttpStatus.BAD_REQUEST);
			}

			// Execute call
			log.debug("add default headers");
			addDefaultHeaders(restInvocationParams, platformUser);
			
			log.debug("call api operation");
			result = apiInvokerUtils.callApiOperation(restInvocationParams);

		} else {
			log.error("API named [v{}] - {} was not found.", invokeRequest.getApiVersion(), invokeRequest.getApiName());
			return new ResponseEntity<>("{'error':'API named '" + invokeRequest.getApiName() + "' was not found.'}",
					HttpStatus.BAD_REQUEST);
		}
		final long executionTime = System.currentTimeMillis() - start;
		log.debug("invokeRestApiOperation for API {}, executed in {} ms",
				invokeRequest.getApiName() + '-' + invokeRequest.getApiVersion(), executionTime);
		return result;
	}

	private RestApiInvocationParams getInvocaionParametersForInternalOrFlowEngineApi(
			FlowEngineInvokeRestApiOperationRequest invokeRequest, Api selectedApi) {
		final Optional<ApiOperation> operation = findOperationFromSwaggerJson(invokeRequest, selectedApi);

		if (operation.isPresent()) {
			// Extract param values
			return getInvocationParamsForOperation(operation.get(), invokeRequest);
		} else {
			final String msg = "[" + invokeRequest.getOperationMethod() + "] API operation named "
					+ invokeRequest.getOperationName() + " for API [" + invokeRequest.getApiVersion() + "] - "
					+ invokeRequest.getApiName() + " was not found";
			log.error(msg);
			throw new FlowengineApiNotFoundException(msg);
		}
	}

	private Optional<Api> findApiFromSwaggerJson(FlowEngineInvokeRestApiOperationRequest invokeRequest,
			User platformUser) {
		final Set<Api> projectApis = projectService.getResourcesForUserOfType(platformUser.getUserId(), Api.class);
		final List<Api> userApis = apiManagerService.loadAPISByFilter(invokeRequest.getApiName(), null, null,
				platformUser.getUserId());
		final Collection<Api> apis = Stream.of(projectApis, userApis).flatMap(Collection::stream)
				.collect(Collectors.toMap(Api::getId, e -> e, (e1, e2) -> e1)).values();
		return apis.stream().filter(a -> a.getIdentification().equals(invokeRequest.getApiName())
				&& a.getNumversion().equals(invokeRequest.getApiVersion())).findFirst();
	}

	private Optional<ApiOperation> findOperationFromSwaggerJson(FlowEngineInvokeRestApiOperationRequest invokeRequest,
			Api selectedApi) {
		final List<ApiOperation> operations = apiManagerService.getOperationsByMethod(selectedApi,
				Type.valueOf(invokeRequest.getOperationMethod()));
		return operations.stream().filter(o -> o.getIdentification().equals(invokeRequest.getOperationName()))
				.findFirst();
	}

	private void addDefaultHeaders(RestApiInvocationParams restInvocationParams, User platformUser) {
		restInvocationParams.getHeaders().add("X-OP-APIKey", userTokenService.getToken(platformUser).getToken());
		restInvocationParams.getHeaders().add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
		if (!restInvocationParams.isMultipart()) {
			restInvocationParams.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		} else {
			restInvocationParams.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA.toString());
		}
	}

	private RestApiInvocationParams getInvocationParamsForSwaggerOperation(Api selectedApi,
			FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		final RestApiInvocationParams resultInvocationParams = new RestApiInvocationParams();
		final Swagger swagger = new SwaggerParser().parse(selectedApi.getSwaggerJson());
		if (swagger == null) {
			return openApiUtils.getInvocationParamsForSwaggerOperation(selectedApi, invokeRequest);
		}
		final Map<String, Path> paths = swagger.getPaths();
		for (final Entry<String, Path> pathEntry : paths.entrySet()) {
			final Path path = pathEntry.getValue();
			final String operationPath = pathEntry.getKey();
			for (final Entry<HttpMethod, Operation> operationEntity : path.getOperationMap().entrySet()) {
				final Operation operation = operationEntity.getValue();
				if (operation.getOperationId().equals(invokeRequest.getOperationName())
						&& operationEntity.getKey().toString().equals(invokeRequest.getOperationMethod())) {
					resultInvocationParams.setMethod(Type.valueOf(operationEntity.getKey().toString()));
					resultInvocationParams.setUrl(resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.API)
							.concat("/v").concat(String.valueOf(selectedApi.getNumversion())).concat("/")
							.concat(selectedApi.getIdentification()).concat(operationPath));
					// Parameters
					apiInvokerUtils.fillSwaggerInvocationParams(operation, invokeRequest, resultInvocationParams);
					break;
				}
			}
		}
		return resultInvocationParams;
	}

	private RestApiInvocationParams getInvocationParamsForOperation(ApiOperation operation,
			FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		final Set<ApiQueryParameter> params = operation.getApiqueryparameters();
		final RestApiInvocationParams resultInvocationParams = new RestApiInvocationParams();
		for (final ApiQueryParameter param : params) {
			String value = "";
			try {
				value = apiInvokerUtils.getValueForParam(param.getName(), invokeRequest.getOperationInputParams());
			} catch (final FlowDomainServiceException e) {

				final String msg = "No value was found for parameter " + param.getName() + " in operation ["
						+ invokeRequest.getOperationMethod() + "] - " + invokeRequest.getOperationName() + " from API ["
						+ invokeRequest.getApiVersion() + "] - " + operation.getApi().getIdentification() + ".";
				log.error(msg);
				throw new NoValueForParamIvocationException(msg);
			}
			if (param.getHeaderType() == HeaderType.QUERY) {
				resultInvocationParams.getQueryParams().put(param.getName(), value);
			} else if (param.getHeaderType() == HeaderType.PATH) {
				resultInvocationParams.getPathParams().put(param.getName(), value);
			} else if (param.getHeaderType() == HeaderType.BODY) {
				resultInvocationParams.setBody(value);
			} else {
				final String msg = "Unspected param type " + param.getHeaderType().toString() + " for param: "
						+ param.getName() + ".";
				log.error(msg);
				throw new InvalidInvocationParamTypeException(msg);
			}
		}
		resultInvocationParams.setUrl(resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.API).concat("/v")
				.concat(String.valueOf(operation.getApi().getNumversion())).concat("/")
				.concat(operation.getApi().getIdentification()).concat(operation.getPath()));
		resultInvocationParams.setMethod(operation.getOperation());
		return resultInvocationParams;
	}

	public List<String> getApiRestCategories(String authentication) {
		final List<String> response = new ArrayList<>();
		for (final ApiCategories category : Api.ApiCategories.values()) {
			response.add(category.name());
		}
		return response;
	}

	public List<RestApiDTO> getApiRestByUser(String authentication) {
		final List<RestApiDTO> apiNames = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		final Set<Api> projectApis = projectService.getResourcesForUserOfType(sofia2User.getUserId(), Api.class);
		final List<Api> userApis = apiManagerService.loadAPISByFilter("", null, null, sofia2User.getUserId());
		final Collection<Api> apis = Stream.of(projectApis, userApis).flatMap(Collection::stream)
				.collect(Collectors.toMap(Api::getId, e -> e, (e1, e2) -> e1)).values();

		for (final Api api : apis) {
			final RestApiDTO apiDTO = new RestApiDTO();
			apiDTO.setName(api.getIdentification());
			apiDTO.setVersion(api.getNumversion());
			apiNames.add(apiDTO);
		}
		return apiNames;
	}

	public List<RestApiOperationDTO> getApiRestOperationsByUser(String apiName, Integer version,
			String authentication) {
		List<RestApiOperationDTO> operationNames = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		final Set<Api> projectApis = projectService.getResourcesForUserOfType(sofia2User.getUserId(), Api.class);
		final List<Api> userApis = apiManagerService.loadAPISByFilter("", null, null, sofia2User.getUserId());
		final Collection<Api> apis = Stream.of(projectApis, userApis).flatMap(Collection::stream)
				.collect(Collectors.toMap(Api::getId, e -> e, (e1, e2) -> e1)).values();
		final Optional<Api> selectedApi = apis.stream()
				.filter(a -> a.getIdentification().equals(apiName) && a.getNumversion().equals(version)).findFirst();
		if (selectedApi.isPresent()) {
			final List<ApiOperation> operations = apiManagerService.getOperations(selectedApi.get());
			if ((operations == null || operations.isEmpty()) && !selectedApi.get().getSwaggerJson().isEmpty()) {
				// Get all operations from SwaggerJSON
				operationNames = getOperationsFromSwaggerJson(selectedApi.get().getSwaggerJson());
			} else {
				for (final ApiOperation op : operations) {
					final RestApiOperationDTO opDTO = new RestApiOperationDTO();

					opDTO.setName(op.getIdentification());
					opDTO.setMethod(op.getOperation().name());
					// ADD Input parameter names

					final List<RestApiOperationParamDTO> parameters = new ArrayList<>();
					for (final ApiQueryParameter param : op.getApiqueryparameters()) {
						final RestApiOperationParamDTO paramDTO = new RestApiOperationParamDTO();
						paramDTO.setName(param.getName());
						paramDTO.setType(param.getHeaderType().name());
						//if not Swagger, params are required always
						paramDTO.setRequired(true);

						parameters.add(paramDTO);
					}
					opDTO.setParams(parameters);
					// ADD StatusCodes
					opDTO.setReturnMessagesresponseCodes(apiInvokerUtils.getDefaultStatusCodes());
					operationNames.add(opDTO);
				}
			}
		}
		return operationNames;
	}

	private List<RestApiOperationDTO> getOperationsFromSwaggerJson(String swaggerJson) {
		// Get all operations from SwaggerJSON
		final List<RestApiOperationDTO> operationNames = new ArrayList<>();
		final SwaggerParser swaggerParser = new SwaggerParser();
		final Swagger swagger = swaggerParser.parse(swaggerJson);
		if (swagger == null) {
			return getOperationsFromOpenAPI(swaggerJson);
		}
		final Map<String, Path> paths = swagger.getPaths();
		for (final Entry<String, Path> pathEntry : paths.entrySet()) {
			final Path path = pathEntry.getValue();
			for (final Entry<HttpMethod, Operation> operationEntity : path.getOperationMap().entrySet()) {
				final Operation operation = operationEntity.getValue();

				final RestApiOperationDTO opDTO = new RestApiOperationDTO();
				opDTO.setName(operation.getOperationId());
				opDTO.setMethod(operationEntity.getKey().name());
				// Parameters and headers
				final List<RestApiOperationParamDTO> parameters = new ArrayList<>();
				for (final Parameter param : operation.getParameters()) {

					final RestApiOperationParamDTO paramDTO = new RestApiOperationParamDTO();
					paramDTO.setName(param.getName());
					paramDTO.setType(param.getIn().toUpperCase());
					paramDTO.setRequired(param.getRequired());
					parameters.add(paramDTO);
				}

				opDTO.setParams(parameters);
				// StatusCodes
				final Map<String, String> statusCodes = new HashMap<>();
				for (final Entry<String, Response> responsesEntry : operation.getResponses().entrySet()) {
					statusCodes.put(responsesEntry.getKey(), responsesEntry.getValue().getDescription());
				}
				if (operation.getResponses().isEmpty()) {
					opDTO.setReturnMessagesresponseCodes(apiInvokerUtils.getDefaultStatusCodes());
				} else {
					// always add "other" statusCode
					statusCodes.put("???", "Other status code");
					opDTO.setReturnMessagesresponseCodes(statusCodes);
				}
				operationNames.add(opDTO);
			}
		}
		return operationNames;
	}

	private List<RestApiOperationDTO> getOperationsFromOpenAPI(String openApi) {
		return openApiUtils.getOperationsFromOpenAPI(openApi, null, null);
	}

}

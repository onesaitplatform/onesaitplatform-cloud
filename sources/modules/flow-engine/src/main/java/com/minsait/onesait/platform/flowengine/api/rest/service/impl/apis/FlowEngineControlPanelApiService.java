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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MultivaluedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiInvocationParams;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationParamDTO;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowEngineControlPanelApiService {

	private String swaggerUrl;

	@Value("${onesaitplatform.platform.base.url:http://controlpanelservice:18000/controlpanel}")
	private String controlpanelUrl;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
	private Swagger swagger;
	private static final String ERROR_DOMAIN = "{'error':'Domain ";

	@Value("${onesaitplatform.controlpanel.avoidsslverification:false}")
	private boolean avoidSSLVerification;

	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private ApiInvokerUtils apiInvokerUtils;
	@Autowired
	private FlowDomainService domainService;

	@PostConstruct
	private void init() {
		swagger = null;
		// controlpanelUrl = resourcesService.getUrl(Module.CONTROLPANEL,
		// ServiceUrl.BASE);
		swaggerUrl = controlpanelUrl + "/v2/api-docs?group=All groups";
		swagger = getControlpanelApiSwaggerJson();
	}

	private Swagger getControlpanelApiSwaggerJson() {
		String json = null;
		ResponseEntity<String> result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		HttpEntity<?> entity = new HttpEntity<>(new MultivaluedHashMap<>());
		try {
			result = restTemplate.exchange(swaggerUrl, org.springframework.http.HttpMethod.GET, entity, String.class);
			json = result.getBody();

			final SwaggerParser swaggerParser = new SwaggerParser();
			return swaggerParser.parse(json);
		} catch (Exception e) {
			// TODO
			log.error("Error getting ControlPanel API Swagger Json.Cause={}, message = {}.", e.getCause(),
					e.getMessage(), e);
			return null;
		}
	}

	public List<String> getControlPanelApis(String authentication) {
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		List<String> result = new ArrayList<>();
		if (swagger == null) {
			swagger = getControlpanelApiSwaggerJson();
		}

		List<Tag> tags = swagger.getTags();
		for (final Tag tag : tags) {
			result.add(tag.getName());
		}
		return result;
	}

	public List<RestApiOperationDTO> getApiRestOperations(String authentication, String apiTag) {

		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		final List<RestApiOperationDTO> operationNames = new ArrayList<>();

		if (swagger == null) {
			swagger = getControlpanelApiSwaggerJson();
		}

		final Map<String, Path> paths = swagger.getPaths();
		for (final Entry<String, Path> pathEntry : paths.entrySet()) {
			final Path path = pathEntry.getValue();
			for (final Entry<HttpMethod, Operation> operationEntity : path.getOperationMap().entrySet()) {
				final Operation operation = operationEntity.getValue();
				if (operation.getTags().contains(apiTag)) {

					final RestApiOperationDTO opDTO = new RestApiOperationDTO();
					opDTO.setName(operation.getOperationId());
					opDTO.setMethod(operationEntity.getKey().name());
					// Parameters and headers
					final List<RestApiOperationParamDTO> parameters = new ArrayList<>();
					for (final Parameter param : operation.getParameters()) {
						// Oauth API does not have Authentication header
						// Swagegr puts it by default and we have to ignore it
						if (!apiTag.equalsIgnoreCase("Login Oauth service")
								|| !param.getName().equalsIgnoreCase("Authorization")) {
							final RestApiOperationParamDTO paramDTO = new RestApiOperationParamDTO();
							paramDTO.setName(param.getName());
							paramDTO.setType(param.getIn().toUpperCase());
							parameters.add(paramDTO);
						}
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
		}
		return operationNames;
	}

	public ResponseEntity<String> invokeManagementRestApiOperation(
			FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		// TODO LOGIC
		ResponseEntity<String> result = null;
		// Get user from domain
		final FlowDomain domain = domainService.getFlowDomainByIdentification(invokeRequest.getDomainName());
		if (domain == null) {
			log.error("Domain {} not found for DataFlow Status Check execution.", invokeRequest.getDomainName());
			return new ResponseEntity<>(ERROR_DOMAIN + invokeRequest.getDomainName()
					+ " not found for Management API Operation execution: '[" + invokeRequest.getOperationMethod()
					+ "] " + invokeRequest.getApiName() + " - " + invokeRequest.getOperationName() + "'.'}",
					HttpStatus.BAD_REQUEST);
		}
		// Get TOKEN

		// Search for operacion and tag in swagger

		if (swagger == null) {
			swagger = getControlpanelApiSwaggerJson();
		}
		RestApiInvocationParams invocationParams = new RestApiInvocationParams();

		final Map<String, Path> paths = swagger.getPaths();
		for (final Entry<String, Path> pathEntry : paths.entrySet()) {
			final Path path = pathEntry.getValue();
			for (final Entry<HttpMethod, Operation> operationEntity : path.getOperationMap().entrySet()) {
				final Operation operation = operationEntity.getValue();
				if (operation.getTags().contains(invokeRequest.getApiName())
						&& operation.getOperationId().equals(invokeRequest.getOperationName())
						&& operationEntity.getKey().name().equalsIgnoreCase(invokeRequest.getOperationMethod())) {
					// Operation found, get URL and params to chech
					invocationParams.setUrl(controlpanelUrl + pathEntry.getKey());
					invocationParams.setMethod(Type.valueOf(operationEntity.getKey().toString()));
					// Check parameters and headers
					apiInvokerUtils.fillSwaggerInvocationParams(operation, invokeRequest, invocationParams);
					break;
				}
			}
		}
		// execute call
		result = apiInvokerUtils.callApiOperation(invocationParams);
		// response
		return result;
	}

}

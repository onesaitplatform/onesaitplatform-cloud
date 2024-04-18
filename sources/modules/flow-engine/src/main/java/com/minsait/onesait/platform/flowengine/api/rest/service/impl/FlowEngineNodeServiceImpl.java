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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.HeaderType;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.MessageType;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.apimanager.operation.OperationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.QueryStringJson;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.digitaltwin.type.DigitalTwinTypeService;
import com.minsait.onesait.platform.config.services.exceptions.FlowDomainServiceException;
import com.minsait.onesait.platform.config.services.flow.FlowService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.flownode.FlowNodeService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DeployRequestRecord;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinDeviceDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinTypeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineDeployerApis;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NodeREDAPIInvokerInputFile;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiInvocationParams;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationParamDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.UserDomainValidationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineNodeService;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.audit.aop.FlowEngineAuditable;
import com.minsait.onesait.platform.flowengine.exception.FlowEngineDeployException;
import com.minsait.onesait.platform.flowengine.exception.FlowengineApiNotFoundException;
import com.minsait.onesait.platform.flowengine.exception.InvalidInvocationParamTypeException;
import com.minsait.onesait.platform.flowengine.exception.NoValueForParamIvocationException;
import com.minsait.onesait.platform.flowengine.exception.NodeRedAdminServiceException;
import com.minsait.onesait.platform.flowengine.exception.NotAllowedException;
import com.minsait.onesait.platform.flowengine.exception.ResourceNotFoundException;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowEngineNodeServiceImpl implements FlowEngineNodeService {

	private static final String ADMINISTRATOR_STR = "ROLE_ADMINISTRATOR";
	private static final String REQUIRED = "REQUIRED";
	private static final String CAUSE = "Cause";
	private static final String MESSAGE = ", Message = ";
	private static final String PIPELINE_NOT_EXISTS = "Specified Pipeline '{}' does not exist.";
	private static final String PIPELINE_NOT_EXITS_MSG = "{'error':'Specified Pipeline does not exist.'}";
	private static final String ERROR_DOMAIN = "{'error':'Domain ";
	@Autowired(required = false)
	private RouterService routerService;

	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private FlowService flowService;

	@Autowired
	private FlowNodeService nodeService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ClientPlatformService clientPlatformService;

	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;

	@Autowired
	private DigitalTwinTypeService digitalTwinTypeService;
	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectResourceAccessRepository projectResourceAccessRepository;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private MailService mailService;
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private UserService userService;
	@Autowired
	private MultitenancyService masterUserService;
	@Autowired
	private OpenAPI3Utils openApiUtils;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	void setUTF8Encoding() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	@Override
	@Transactional
	public ResponseEntity<String> deploymentNotification(String json) {
		final ObjectMapper mapper = new ObjectMapper();
		FlowEngineDeployerApis deployedAPisInfo;
		List<DeployRequestRecord> deployRecords = new ArrayList<>();
		try {
			deployRecords = mapper.readValue(json, new TypeReference<List<DeployRequestRecord>>() {
			});
			deployedAPisInfo = getDeployedApisInfo(deployRecords);
			// before delete fetch project resources related to API
			final Map<String, List<ProjectResourceAccess>> oldAccesses = fetchProjectAndAccessesToAPIs(
					deployedAPisInfo.getDeployedApis());
			processDeploymentRecords(deployRecords, deployedAPisInfo);
			// Create APIs
			final Map<String, String> newAPIsDeployed = createApis(deployedAPisInfo.getDeployedApis());
			// Set new accesses
			updateAccesses(oldAccesses, newAPIsDeployed);
		} catch (final IOException | FlowEngineDeployException e) {
			log.error("Unable to save deployment info from NodeRed into CDB. Cause = {}, message = {}", e.getCause(),
					e.getMessage());
			return new ResponseEntity<>(
					"{\"error\":\"Unable to save deployment info from NodeRed into CDB.\",\"message\":\""
							+ e.getMessage() + "\"}",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	private void updateAccesses(Map<String, List<ProjectResourceAccess>> oldAccesses,
			Map<String, String> newAPIsDeployed) {
		newAPIsDeployed.entrySet().forEach(e -> {
			final String mapKey = e.getKey();
			final String newId = e.getValue();
			if (oldAccesses.get(mapKey) != null) {
				final Api api = apiManagerService.getById(newId);
				oldAccesses.get(mapKey).forEach(pra -> {
					pra.setId(null);
					pra.setResource(api);
				});
				final Set<Project> projectsInvolved = oldAccesses.get(mapKey).stream()
						.map(ProjectResourceAccess::getProject).collect(Collectors.toSet());
				projectsInvolved.forEach(p -> {
					p.getProjectResourceAccesses().addAll(oldAccesses.get(mapKey).stream()
							.filter(pra -> pra.getProject().getId().equals(p.getId())).collect(Collectors.toSet()));
					projectService.updateProject(p);
				});
			}
		});

	}

	private Map<String, List<ProjectResourceAccess>> fetchProjectAndAccessesToAPIs(
			Map<Api, List<OperationJson>> deployedAPisInfo) {
		final Map<String, List<ProjectResourceAccess>> accesses = new HashMap<>();
		deployedAPisInfo.entrySet().forEach(e -> {
			final Api api = apiManagerService.getApiByIdentificationVersionOrId(e.getKey().getIdentification(),
					String.valueOf(e.getKey().getNumversion()));
			if (api != null) {
				final List<ProjectResourceAccess> relations = projectResourceAccessRepository.findByResource(api);
				if (!CollectionUtils.isEmpty(relations)) {
					accesses.put(api.getIdentification() + api.getNumversion(), relations);
					final Set<Project> projectsAffected = relations.stream().map(ProjectResourceAccess::getProject)
							.collect(Collectors.toSet());
					final Iterator<Project> it = projectsAffected.iterator();
					while (it.hasNext()) {
						final Project p = it.next();
						final Set<ProjectResourceAccess> filtered = p.getProjectResourceAccesses().stream()
								.filter(pra -> !pra.getResource().getId().equals(api.getId()))
								.collect(Collectors.toSet());
						p.getProjectResourceAccesses().clear();
						p.getProjectResourceAccesses().addAll(filtered);
						projectService.updateProject(p);
					}
				}
			}

		});
		return accesses;
	}

	private FlowEngineDeployerApis getDeployedApisInfo(List<DeployRequestRecord> deployRecords) {

		final FlowEngineDeployerApis deployedApisInfo = new FlowEngineDeployerApis();
		deployedApisInfo.setApiOperations(new HashMap<>());
		deployedApisInfo.setDeployedApis(new HashMap<>());
		final String proxyUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.ADVICE);
		final Map<String, ApiStates> apisPrevStates = new HashMap<>();
		final Map<String, Integer> apiVersionDup = new HashMap<>();

		for (final DeployRequestRecord record : deployRecords) {
			if (record.getDomain() != null) {
				log.info("Deployment info from domain = {}", record.getDomain());
				// SET TENANT HERE FOR DEPLOYMENTS
				deployedApisInfo.setDomain(masterUserService.getAllVerticals().stream().map(v -> {
					MultitenancyContextHolder.setVerticalSchema(v.getSchema());
					return domainService.getFlowDomainByIdentification(record.getDomain());
				}).filter(Objects::nonNull).findAny().orElse(null));
			} else if (record.getType().equals(FlowNode.Type.API_REST.getName())) {
				// save previous state

				Integer version;
				if (!apiVersionDup.containsKey(record.getName())) {
					version = getApiVersion(record.getName(), deployedApisInfo.getDomain().getUser().getUserId(),
							apisPrevStates);
				} else {
					version = apiVersionDup.get(record.getName()) + 1;

				}
				apiVersionDup.put(record.getName(), version);
				final Api apiRest = new Api();

				// Fill the API
				apiRest.setUser(deployedApisInfo.getDomain().getUser());
				apiRest.setIdentification(record.getName());

				apiRest.setEndpointExt(proxyUrl + deployedApisInfo.getDomain().getIdentification());
				apiRest.setDescription(record.getDescription());
				apiRest.setCategory(ApiCategories.valueOf(record.getCategory()));
				apiRest.setApiType(ApiType.NODE_RED);
				apiRest.setState(apisPrevStates.getOrDefault(record.getName(), ApiStates.CREATED));
				apiRest.setPublic(record.getIsPublic());
				apiRest.setSsl_certificate(false);
				apiRest.setNumversion(version);
				// Classify API in a MAP by noderedId
				deployedApisInfo.getDeployedApis().put(apiRest, new ArrayList<OperationJson>());
				for (final List<String> wires : record.getWires()) {
					for (final String wiredId : wires) {
						deployedApisInfo.getApiOperations().put(wiredId,
								deployedApisInfo.getDeployedApis().get(apiRest));
					}
				}

			}
		}

		return deployedApisInfo;
	}

	private Integer getApiVersion(String apiName, String userId, Map<String, ApiStates> apisPrevStates) {
		Integer version = apiManagerService.calculateNumVersion(
				"{\"identification\":\"" + apiName + "\",\"apiType\":\"" + ApiType.NODE_RED.toString() + "\"}");
		final List<Api> apis = apiManagerService.loadAPISByFilter(apiName, null, null, userId);
		for (final Api api : apis) {
			if (api.getUser().getUserId().equals(userId)) {
				apisPrevStates.put(api.getIdentification(), api.getState());
				version = api.getNumversion();
			}
		}
		return version;

	}

	private void processDeploymentRecords(List<DeployRequestRecord> deployRecords,
			FlowEngineDeployerApis deployedAPisInfo) throws IOException {
		for (final DeployRequestRecord record : deployRecords) {
			if (record != null) {
				if (record.getDomain() != null && deployedAPisInfo.getDomain() != null) {
					domainService.deleteFlowDomainFlows(record.getDomain(), deployedAPisInfo.getDomain().getUser());
				} else {
					log.debug("Deployment record = {}", record.toString());
					processSingleDeployRecor(record, deployedAPisInfo);
				}
			}
		}
	}

	private void processSingleDeployRecor(DeployRequestRecord record, FlowEngineDeployerApis deployedAPisInfo)
			throws IOException {
		if (record.getType() != null) {
			if (record.getType().equals("tab")) {
				// it is a FLOW
				createFlowEntityFromNode(record, deployedAPisInfo.getDomain());
			} else {
				// It is a node
				if (record.getType().equals(FlowNode.Type.HTTP_NOTIFIER.getName())) {
					createHttpNotifierFromNode(record, deployedAPisInfo.getDomain());
				} else if (record.getType().equals(FlowNode.Type.API_REST.getName())) {
					createApiFromNode(record);
				} else if (record.getType().equals(FlowNode.Type.API_REST_OPERATION.getName())) {
					createApiOperationFromNode(record, deployedAPisInfo);
				}
			}
		} else {
			log.warn("Undefined type for NodeRed element. Record will be skipped : {}", record.toString());
		}
	}

	private void createApiOperationFromNode(DeployRequestRecord record, FlowEngineDeployerApis deployedAPisInfo)
			throws IOException {
		final OperationJson operation = new OperationJson();
		final ObjectMapper mapper = new ObjectMapper();
		operation.setIdentification(record.getName());
		operation.setDescription(record.getDescription());
		operation.setOperation(record.getMethod().toUpperCase());
		// process path params
		String url = record.getUrl();
		if (!url.endsWith("/")) {
			url = record.getUrl() + "/";
		}
		final String replacePattern = "(:)(\\w+)/";
		url = url.replaceAll(replacePattern, "{$2}/");
		// replace nodered syntax to brakets
		final String path = url.substring(1);
		operation.setPath(path.substring(path.indexOf('/')));
		operation.setEndpoint(url);
		// Check for path/query params
		final Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
		final Matcher matcher = pattern.matcher(url);
		// Path params
		final List<QueryStringJson> querystrings = new ArrayList<>();
		while (matcher.find()) {
			final QueryStringJson param = new QueryStringJson();
			param.setDataType(ApiQueryParameter.DataType.STRING.toString());
			param.setCondition(REQUIRED);
			param.setName(matcher.group(1));
			param.setHeaderType(ApiQueryParameter.HeaderType.PATH.toString());
			param.setDescription("");
			querystrings.add(param);
		}
		// Post body as param if needed
		if (operation.getOperation().equals("POST") || operation.getOperation().equals("PUT")) {
			final QueryStringJson param = new QueryStringJson();
			param.setDataType(ApiQueryParameter.DataType.STRING.toString());
			param.setCondition(null);
			param.setName("body");
			param.setHeaderType(ApiQueryParameter.HeaderType.BODY.toString());
			param.setDescription("");
			param.setValue("");
			querystrings.add(param);
		}
		// Query params
		Map<String, String> map;

		// convert JSON string to Map
		String queryParams = record.getQueryParams();
		if (queryParams == null || queryParams.isEmpty()) {
			queryParams = "{}";
		}
		map = mapper.readValue(queryParams, new TypeReference<Map<String, String>>() {
		});
		for (final Entry<String, String> entry : map.entrySet()) {
			final QueryStringJson param = new QueryStringJson();
			param.setDataType(entry.getValue().toUpperCase());
			param.setCondition(REQUIRED);
			param.setName(entry.getKey());
			param.setHeaderType(ApiQueryParameter.HeaderType.QUERY.toString());
			param.setDescription("");
			querystrings.add(param);
		}
		operation.setQuerystrings(querystrings);
		deployedAPisInfo.getApiOperations().get(record.getId()).add(operation);
	}

	private void createApiFromNode(DeployRequestRecord record) {
		final FlowNode node = new FlowNode();
		final Flow flow = flowService.getFlowByNodeRedFlowId(record.getZ());
		node.setIdentification(record.getName());
		node.setNodeRedNodeId(record.getId());
		node.setFlow(flow);
		node.setFlowNodeType(FlowNode.Type.API_REST);
		node.setMessageType(null);
		node.setOntology(null);
		node.setPartialUrl(record.getUrl() != null ? record.getUrl() : "");
		try {
			nodeService.createFlowNode(node);
		} catch (final Exception e) {
			final String msg = "API " + record.getName() + " cound not be created. Cause: " + e.getCause() + ", Error: "
					+ e.getMessage() + ".";
			log.error(msg);
			throw new FlowEngineDeployException(msg);
		}
	}

	private void createFlowEntityFromNode(DeployRequestRecord record, FlowDomain domain) {
		final Flow newFlow = new Flow();
		newFlow.setIdentification(record.getLabel());
		newFlow.setNodeRedFlowId(record.getId());
		newFlow.setActive(true);
		newFlow.setFlowDomain(domain);
		flowService.createFlow(newFlow);
	}

	private void createHttpNotifierFromNode(DeployRequestRecord record, FlowDomain domain) {
		final FlowNode node = new FlowNode();
		final Flow flow = flowService.getFlowByNodeRedFlowId(record.getZ());
		node.setIdentification(record.getName());
		node.setNodeRedNodeId(record.getId());
		node.setFlow(flow);
		node.setFlowNodeType(FlowNode.Type.HTTP_NOTIFIER);
		node.setMessageType(MessageType.valueOf(record.getMeassageType()));
		node.setOntology(
				ontologyService.getOntologyByIdentification(record.getOntology(), domain.getUser().getUserId()));
		node.setPartialUrl(record.getUrl());
		node.setDiscardAfterElapsedTime(record.getDiscardNotifAfterElapsedTime());
		node.setRetryOnFailure(record.getRetryAfterError());
		node.setMaxRetryElapsedTime(record.getNotificationRetryTimeout());
		try {
			nodeService.createFlowNode(node);
		} catch (final Exception e) {
			final String msg = "Notification node '" + node.getIdentification()
					+ "' has an invalid Ontology selected: '" + node.getOntology() + "'.";
			log.error(msg);
			throw new FlowEngineDeployException(msg);
		}
	}

	private Map<String, String> createApis(Map<Api, List<OperationJson>> deployedApis) throws JsonProcessingException {
		final Map<String, String> newAPIsIds = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper();
		for (final Map.Entry<Api, List<OperationJson>> entry : deployedApis.entrySet()) {
			final Api api = entry.getKey();
			final String newId = apiManagerService.createApi(api, mapper.writeValueAsString(entry.getValue()), "");
			newAPIsIds.put(api.getIdentification() + api.getNumversion(), newId);

		}
		return newAPIsIds;

	}

	@Override
	public List<String> getApiRestCategories(String authentication) {
		final List<String> response = new ArrayList<>();
		for (final ApiCategories category : Api.ApiCategories.values()) {
			response.add(category.name());
		}
		return response;
	}

	@Override
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

	@Override
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
						parameters.add(paramDTO);
					}
					opDTO.setParams(parameters);
					// ADD StatusCodes
					opDTO.setReturnMessagesresponseCodes(getDefaultStatusCodes());
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
					parameters.add(paramDTO);
				}

				opDTO.setParams(parameters);
				// StatusCodes
				final Map<String, String> statusCodes = new HashMap<>();
				for (final Entry<String, Response> responsesEntry : operation.getResponses().entrySet()) {
					statusCodes.put(responsesEntry.getKey(), responsesEntry.getValue().getDescription());
				}
				if (operation.getResponses().isEmpty()) {
					opDTO.setReturnMessagesresponseCodes(getDefaultStatusCodes());
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

	private Map<String, String> getDefaultStatusCodes() {
		final Map<String, String> statusCodes = new HashMap<>();
		statusCodes.put("200", "OK");
		statusCodes.put("204", "No Content");
		statusCodes.put("400", "Bad Request");
		statusCodes.put("401", "Unauthorized");
		statusCodes.put("501", "Internal Server Error");
		statusCodes.put("???", "Other status code");
		return statusCodes;
	}

	@Override
	public Set<String> getOntologyByUser(String authentication) {

		final Set<String> response = new TreeSet<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User user = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		if (userService.isUserAdministrator(user)) {

			response.addAll(ontologyService.getAllOntologies(user.getUserId()).stream().map(Ontology::getIdentification)
					.collect(Collectors.toSet()));
		} else {
			response.addAll(ontologyService.getOntologiesByUserId(user.getUserId()).stream()
					.map(Ontology::getIdentification).collect(Collectors.toSet()));
			response.addAll(resourceService.getResourcesForUserAndType(user, Ontology.class.getSimpleName()).stream()
					.map(OPResource::getIdentification).collect(Collectors.toSet()));

		}

		return response;
	}

	@Override
	public List<String> getClientPlatformByUser(String authentication) {

		final List<String> response = new ArrayList<>();

		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User user = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		List<ClientPlatform> clientPlatforms = null;
		if (userService.isUserAdministrator(user)) {
			clientPlatforms = clientPlatformService.getAllClientPlatforms();
		} else {
			clientPlatforms = clientPlatformService.getclientPlatformsByUser(user);
		}
		for (final ClientPlatform clientPlatform : clientPlatforms) {
			response.add(clientPlatform.getIdentification());
		}
		Collections.sort(response);
		return response;
	}

	@Override
	public String validateUserDomain(UserDomainValidationRequest request) {

		String response = null;
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService
				.decodeAuth(request.getAuthentication());
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		if (request.getDomainId() == null) {
			throw new IllegalArgumentException("DomainId must be specified.");
		}

		final FlowDomain domain = domainService.getFlowDomainByIdentification(request.getDomainId());

		if (domain == null) {
			throw new ResourceNotFoundException(
					"Domain with identification " + request.getDomainId() + " could not be found.");
		}

		if (sofia2User.getRole().getName().equals(ADMINISTRATOR_STR)) {
			response = "OK"; // Has permission over all domains
		} else {
			if (!domain.getUser().getUserId().equals(sofia2User.getUserId())) {
				throw new NotAllowedException("User " + decodedAuth.getUserId()
						+ " has no permissions over specified domain " + request.getDomainId());
			}
			response = "OK";
		}
		return response;
	}

	@Override
	@FlowEngineAuditable
	public String submitQuery(String ontology, String queryType, String query, String domainName)
			throws NotFoundException, JsonProcessingException {

		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainName);
		if (domain == null || domain.getUser() == null) {
			log.error("Domain {} does not exist.", domainName);
			throw new NodeRedAdminServiceException("Domain " + domainName + " does not exist.");
		}
		final User platformUser = domain.getUser();
		OperationType operationType = OperationType.QUERY;
		QueryType type;

		if ("sql".equalsIgnoreCase(queryType)) {
			type = QueryType.SQL;
			Ontology dbOntology;
			try {
				dbOntology = ontologyService.getOntologyByIdentification(ontology, platformUser.getUserId());
			} catch (final Exception e) {

				log.error("Error checking access to ontology. Ontology={}, User = {}. Cause = {}, Message = {}.",
						ontology, platformUser.getUserId(), e.getCause(), e.getMessage());
				throw new NodeRedAdminServiceException("Error checking access to ontology. Ontology=" + ontology
						+ ", User = " + platformUser.getUserId() + ". " + CAUSE + " = " + e.getCause() + MESSAGE
						+ e.getMessage() + ".");
			}
			if (query.trim().toUpperCase().startsWith("INSERT ")) {
				throw new IllegalArgumentException("Invalid QUERY. INSERT not allowed, please use Insert node.");
			}

			// if ontologyType is external API, then check OperationType
			if (dbOntology.getRtdbDatasource() == RtdbDatasource.API_REST) {
				if (query.trim().toUpperCase().startsWith("DELETE ")) {
					operationType = OperationType.DELETE;
				} else if (query.trim().toUpperCase().startsWith("UPDATE ")) {
					operationType = OperationType.UPDATE;
				}
			}

		} else if ("native".equalsIgnoreCase(queryType)) {
			type = QueryType.NATIVE;
			if (query.trim().startsWith("db." + ontology + ".remove")) {
				operationType = OperationType.DELETE;
			} else if (query.trim().startsWith("db." + ontology + ".update")) {
				operationType = OperationType.UPDATE;
			}
		} else {
			log.error("Invalid value {} for queryType. Possible values are: SQL, NATIVE.", queryType);
			throw new IllegalArgumentException(
					"Invalid value " + queryType + " for queryType. Possible values are: SQL, NATIVE.");
		}

		final OperationModel operationModel = OperationModel
				.builder(ontology, operationType, platformUser.getUserId(), OperationModel.Source.FLOWENGINE)
				.body(query).queryType(type).build();

		return sendNotificationModelToExecuteQuery(operationModel);
	}

	private String sendNotificationModelToExecuteQuery(OperationModel operationModel) {

		OperationResultModel result = null;
		try {
			final NotificationModel notificationModel = new NotificationModel();
			notificationModel.setOperationModel(operationModel);
			result = routerService.query(notificationModel);
		} catch (final Exception e) {

			log.error("Error executing query. Ontology={}, QueryType ={}, Query = {}. Cause = {}, Message = {}.",
					operationModel.getOntologyName(), operationModel.getQueryType(), operationModel.getBody(),
					e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Error executing query. Ontology=" + operationModel.getOntologyName()
					+ ", QueryType =" + operationModel.getQueryType() + ", Query = " + operationModel.getBody()
					+ ". Cause = " + e.getCause() + MESSAGE + e.getMessage() + ".");
		}
		if (!result.isStatus()) {
			throw new NodeRedAdminServiceException("Error executing query. Ontology=" + operationModel.getOntologyName()
					+ ", QueryType =" + operationModel.getQueryType() + ", Query = " + operationModel.getBody() + ". "
					+ CAUSE + " = " + result.getMessage() + ".");
		}

		return result.getResult();
	}

	@Override
	@FlowEngineAuditable
	public String submitInsert(String ontology, String data, String domainName)
			throws JsonProcessingException, NotFoundException {

		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainName);
		if (domain != null && domain.getUser() != null) {
			// check access to ontology
			try {
				ontologyService.getOntologyByIdentification(ontology, domain.getUser().getUserId());
			} catch (final Exception e) {
				log.error("Error checking access to ontology. Ontology={}, User = {}. Cause = {}, Message = {}.",
						ontology, domain.getUser().getUserId(), e.getCause(), e.getMessage());
				throw new NodeRedAdminServiceException("Error checking access to ontology. Ontology=" + ontology
						+ ", User = " + domain.getUser().getUserId() + ". Domain = " + domainName + ". " + CAUSE + " = "
						+ e.getCause() + MESSAGE + e.getMessage() + ".");
			}
		} else {
			log.error("Domain {} does not exist.", domainName);
			throw new NodeRedAdminServiceException("Domain " + domainName + " does not exist.");

		}

		final OperationModel operationModel = OperationModel
				.builder(ontology, OperationType.INSERT, domain.getUser().getUserId(), OperationModel.Source.FLOWENGINE)
				.body(data).build();

		OperationResultModel result = null;

		try {
			final NotificationModel notificationModel = new NotificationModel();
			notificationModel.setOperationModel(operationModel);
			result = routerService.insert(notificationModel);
		} catch (final Exception e) {
			log.error("Error inserting data. Ontology={}, Data = {}. Cause = {}, Message = {}.", ontology, data,
					e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Error inserting data. Ontology=" + ontology + ", Data = " + data
					+ ". " + CAUSE + " = " + e.getCause() + MESSAGE + e.getMessage() + ".");
		}
		if (!result.isStatus()) {
			throw new NodeRedAdminServiceException("Error inserting data. Ontology=" + ontology + ", Data = " + data
					+ ". " + CAUSE + " = " + result.getMessage() + ".");
		}

		String output = result.getResult();

		final JSONObject obj = new JSONObject(output);
		if (obj.has(InsertResult.DATA_PROPERTY)) {
			output = obj.get(InsertResult.DATA_PROPERTY).toString();
		}

		return output;
	}

	@Override
	public List<DigitalTwinTypeDTO> getDigitalTwinTypes(String authentication) {

		final List<DigitalTwinTypeDTO> response = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());

		final List<DigitalTwinType> digitalTwinTypes = digitalTwinTypeService.getAll();

		for (final DigitalTwinType digitalTwinType : digitalTwinTypes) {
			final DigitalTwinTypeDTO type = new DigitalTwinTypeDTO();
			type.setName(digitalTwinType.getIdentification());
			type.setJson(digitalTwinType.getJson());
			final List<DigitalTwinDevice> devices = digitalTwinDeviceService
					.getAllDigitalTwinDevicesByTypeId(digitalTwinType.getIdentification());
			final List<DigitalTwinDeviceDTO> devicesDTO = new ArrayList<>();
			if (devices != null) {
				for (final DigitalTwinDevice device : devices) {
					final DigitalTwinDeviceDTO deviceDTO = new DigitalTwinDeviceDTO();
					deviceDTO.setDevice(device.getIdentification());
					deviceDTO.setDigitalKey(device.getDigitalKey());
					devicesDTO.add(deviceDTO);
				}
			}
			type.setDevices(devicesDTO);
			response.add(type);
		}
		return response;

	}

	@Override
	public ResponseEntity<String> invokeRestApiOperation(FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		final long start = System.currentTimeMillis();
		RestApiInvocationParams restInvocationParams;
		ResponseEntity<String> result = null;
		// Search api
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

		final Optional<Api> selectedApi = findApiFromSwaggerJson(invokeRequest, platformUser);
		// Search operation
		if (selectedApi.isPresent()) {
			try {
				if (selectedApi.get().getApiType() == ApiType.INTERNAL_ONTOLOGY
						|| selectedApi.get().getApiType() == ApiType.NODE_RED) {
					restInvocationParams = getInvocaionParametersForInternalOrFlowEngineApi(invokeRequest,
							selectedApi.get());
				} else {
					// API Swagger External
					restInvocationParams = getInvocationParamsForSwaggerOperation(selectedApi.get(), invokeRequest);
				}
			} catch (NoValueForParamIvocationException | InvalidInvocationParamTypeException
					| FlowengineApiNotFoundException e) {
				return new ResponseEntity<>("{'error':'" + e.getMessage() + ".' }", HttpStatus.BAD_REQUEST);
			}

			// Execute call

			addDefaultHeaders(restInvocationParams, platformUser);

			result = callApiOperation(restInvocationParams);

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
					fillSwaggerInvocationParams(operation, selectedApi, invokeRequest, resultInvocationParams);
					break;
				}
			}
		}
		return resultInvocationParams;
	}

	private void fillSwaggerInvocationParams(Operation operation, Api selectedApi,
			FlowEngineInvokeRestApiOperationRequest invokeRequest, RestApiInvocationParams resultInvocationParams) {
		for (final Parameter param : operation.getParameters()) {
			// QUERY, PATH, BODY (formData ignore) or HEADER
			String value = "";
			try {
				value = getValueForParam(param.getName(), invokeRequest.getOperationInputParams());
			} catch (final FlowDomainServiceException e) {

				final String msg = "No value was found for parameter " + param.getName() + " in operation ["
						+ invokeRequest.getOperationMethod() + "] - " + invokeRequest.getOperationName() + " from API ["
						+ invokeRequest.getApiVersion() + "] - " + selectedApi.getIdentification() + ".";
				log.error(msg);
				throw new NoValueForParamIvocationException(msg);
			}

			switch (param.getIn().toUpperCase()) {
			case "QUERY":
				resultInvocationParams.getQueryParams().put(param.getName(), value);
				break;
			case "PATH":
				resultInvocationParams.getPathParams().put(param.getName(), value);
				break;
			case "BODY":
				resultInvocationParams.setBody(value);
				break;
			case "HEADER":
				resultInvocationParams.getHeaders().add(param.getName(), value);
				break;
			case "FORMDATA":
				resultInvocationParams.setMultipart(true);
				final FormParameter formParam = (FormParameter) param;
				if (formParam.getType().equalsIgnoreCase("file")) {

					try {
						// transform JSON (NodeJS) buffer to Bytes array
						final ObjectMapper mapper = new ObjectMapper();
						final NodeREDAPIInvokerInputFile nodeFile = mapper.readValue(value,
								NodeREDAPIInvokerInputFile.class);
						final File file = new File("/tmp/" + nodeFile.getFileName());
						Files.write(file.toPath(), nodeFile.getFile().getData());
						resultInvocationParams.getMultipartData().add(param.getName(), new FileSystemResource(file));
					} catch (final IOException e1) {
						log.error("Could not create temp file for multipart request");
					}

				} else {
					resultInvocationParams.getMultipartData().add(param.getName(), value);
				}
				break;
			default:
				break;
			}
		}

	}

	private RestApiInvocationParams getInvocationParamsForOperation(ApiOperation operation,
			FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		final Set<ApiQueryParameter> params = operation.getApiqueryparameters();
		final RestApiInvocationParams resultInvocationParams = new RestApiInvocationParams();
		for (final ApiQueryParameter param : params) {
			String value = "";
			try {
				value = getValueForParam(param.getName(), invokeRequest.getOperationInputParams());
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

	private String getValueForParam(String paramName, List<Map<String, String>> paramValues) {
		String value = "";
		boolean found = false;
		for (final Map<String, String> parameterValues : paramValues) {
			if (parameterValues.get("name") != null && parameterValues.get("name").equals(paramName)) {
				value = parameterValues.get("value");
				found = true;
				break;
			}
		}
		if (!found) {
			log.error("No value was defined for param {}.", paramName);
			throw new FlowDomainServiceException("No value was defined for param '" + paramName + "'.");
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<String> callApiOperation(RestApiInvocationParams invocationParams) {
		ResponseEntity<String> result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		HttpEntity<?> entity = new HttpEntity<>(invocationParams.getBody(), invocationParams.getHeaders());
		if (invocationParams.isMultipart()) {
			entity = new HttpEntity<>(invocationParams.getMultipartData(), invocationParams.getHeaders());
		}
		// Add query params
		String url = addExtraQueryParameters(invocationParams.getUrl(), invocationParams.getQueryParams());
		// Add path params
		for (final Entry<String, String> entry : invocationParams.getPathParams().entrySet()) {
			url = url.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue());
		}
		try {
			switch (invocationParams.getMethod()) {
			case GET:
				result = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
				break;
			case POST:
				result = restTemplate.postForEntity(url, entity, String.class);
				break;
			case PUT:
				result = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, entity, String.class);
				break;
			case DELETE:
				result = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, entity, String.class);
				break;
			default:
				break;

			}
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		} finally {
			if (invocationParams.isMultipart()) {
				((MultiValueMap<String, Object>) entity.getBody()).entrySet().forEach(e -> {
					if (e.getValue() != null) {
						final List<Object> params = e.getValue();
						if (!params.isEmpty() && params.get(0) instanceof FileSystemResource) {
							((FileSystemResource) e.getValue().get(0)).getFile().delete();
						}
					}

				});
			}
		}
		return result;
	}

	private String addExtraQueryParameters(String url, Map<String, String> queryParams) {
		final StringBuilder sb = new StringBuilder(url);

		if (queryParams.size() > 0) {
			sb.append("?");
			queryParams.entrySet().forEach(e -> {
				final String param = e.getKey() + "=" + String.join("", e.getValue());
				sb.append(param).append("&");
			});
		}
		String urlWithQueryParams = sb.toString();
		if (urlWithQueryParams.endsWith("&")) {
			urlWithQueryParams = sb.deleteCharAt(sb.length() - 1).toString();
		}
		return urlWithQueryParams;
	}

	@Override
	public void sendMail(MailRestDTO mail) {
		try {
			mailService.sendHtmlMailWithFile(mail.getTo(), mail.getSubject(), mail.getBody(), mail.getFilename(),
					mail.getFiledata(), mail.isHtmlenable());
		} catch (final MessagingException e) {

			log.error("Mail sending error", e.getMessage());
		}
	}

	@Override
	public void sendSimpleMail(MailRestDTO mail) {
		mailService.sendMail(mail.getTo(), mail.getSubject(), mail.getBody());
	}

	@Override
	public List<NotebookDTO> getNotebooksByUser(String authentication) {

		final List<NotebookDTO> notebooks = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		final List<Notebook> notebooksList = notebookService.getNotebooks(decodedAuth.getUserId());

		for (final Notebook notebook : notebooksList) {
			final NotebookDTO notebookDTO = new NotebookDTO();
			notebookDTO.setId(notebook.getIdzep());
			notebookDTO.setName(notebook.getIdentification());
			notebooks.add(notebookDTO);
		}
		return notebooks;
	}

	@Override
	public String getNotebookJSONDataByUser(String notebookId, String authentication) {
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		return notebookService.exportNotebook(notebookId, decodedAuth.getUserId()).toString();
	}

	@Override
	public ResponseEntity<String> invokeNotebook(NotebookInvokeDTO notebookInvocationData) {

		final FlowDomain domain = domainService.getFlowDomainByIdentification(notebookInvocationData.getDomainName());
		if (domain == null) {
			log.error("Domain {} not found for Notebook execution.", notebookInvocationData.getDomainName());
			return new ResponseEntity<>(ERROR_DOMAIN + notebookInvocationData.getDomainName()
					+ " not found for Notebook invocation: '" + notebookInvocationData.getNotebookId() + "'.'}",
					HttpStatus.BAD_REQUEST);
		}

		final Notebook notebook = notebookService.getNotebookByZepId(notebookInvocationData.getNotebookId(),
				domain.getUser().getUserId());
		final String cloneName = notebook.getIdentification() + "-clone-" + UUID.randomUUID();
		// clone notebook
		final String cloneNotebookId = notebookService.cloneNotebookOnlyZeppelin(cloneName,
				notebookInvocationData.getNotebookId(), domain.getUser().getUserId());
		// run notebook according to data in DTO
		notebookInvocationData.setNotebookId(cloneNotebookId);
		try {
			final ResponseEntity<String> response = runNotebookInvocation(notebookInvocationData);
			// delete clone
			notebookService.removeNotebookOnlyZeppelin(cloneNotebookId, domain.getUser().getUserId());
			return response;
		} catch (URISyntaxException | IOException e) {
			final String msg = "{'error':'" + e.getMessage() + "'}";
			return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private ResponseEntity<String> runNotebookInvocation(NotebookInvokeDTO notebookInvocationData)
			throws URISyntaxException, IOException {
		final List<String> allParagraphsResult = new ArrayList<>();
		if (Boolean.TRUE.equals(notebookInvocationData.getExecuteNotebook())) {
			final ResponseEntity<String> executionResult = notebookService
					.runAllParagraphs(notebookInvocationData.getNotebookId());
			if (executionResult.getStatusCode() != HttpStatus.OK) {

				return executionResult;
			} else {
				// get each requested Paragraph output
				for (final Map<String, String> paragraph : notebookInvocationData.getOutputParagraphs()) {
					final String paragraphId = paragraph.get("paragraph");
					final ResponseEntity<String> paragraphResult = notebookService
							.getParagraphResult(notebookInvocationData.getNotebookId(), paragraphId);
					if (paragraphResult.getStatusCode() == HttpStatus.OK) {
						allParagraphsResult.add(paragraphResult.getBody());
					} else {
						return paragraphResult;
					}
				}
				return new ResponseEntity<>(new Gson().toJson(allParagraphsResult), HttpStatus.OK);
			}
		} else {
			return notebookService.runParagraph(notebookInvocationData.getNotebookId(),
					notebookInvocationData.getParagraphId(), notebookInvocationData.getExecutionParams());
		}
	}

	@Override
	public List<String> getPipelinesByUser(String authentication) {
		final List<String> pipelinesByUser = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUser(decodedAuth.getUserId());
		final List<Pipeline> pipelines = dataflowService.getPipelines(sofia2User.getUserId());
		for (final Pipeline pipeline : pipelines) {
			pipelinesByUser.add(pipeline.getIdentification());
		}
		return pipelinesByUser;
	}

	@Override
	public ResponseEntity<String> getPipelineStatus(String domainName, String pipelineIdentification) {
		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainName);
		if (domain == null) {
			log.error("Domain {} not found for DataFlow Status Check execution.", domainName);
			return new ResponseEntity<>(ERROR_DOMAIN + domainName + " not found for DataFlow Status Check execution: '"
					+ pipelineIdentification + "'.'}", HttpStatus.BAD_REQUEST);
		}

		final Pipeline pipeline = dataflowService.getPipelineByIdentification(pipelineIdentification);
		if (pipeline == null) {
			// Pipeline does not exist
			log.error(PIPELINE_NOT_EXISTS, pipelineIdentification);
			return new ResponseEntity<>(PIPELINE_NOT_EXITS_MSG, HttpStatus.NOT_FOUND);
		}

		if (!dataflowService.hasUserViewPermission(pipeline, domain.getUser().getUserId())) {
			// User has no permissions over the requested pipeline
			log.error("User has no VIEW permissions over the specified Pipeline '{}'.", pipelineIdentification);
			return new ResponseEntity<>(
					"{'error':'Forbidden. User has no VIEW permissions over the requested resource.'}",
					HttpStatus.FORBIDDEN);
		}

		return dataflowService.statusPipeline(domain.getUser().getUserId(), pipelineIdentification);
	}

	@Override
	public ResponseEntity<String> stopDataflow(String domainName, String pipelineIdentification) {
		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainName);
		if (domain == null) {
			log.error("Domain {} not found for DataFlow Stop execution.", domainName);
			return new ResponseEntity<>(ERROR_DOMAIN + domainName + " not found for DataFlow Stop execution: '"
					+ pipelineIdentification + "'.'}", HttpStatus.BAD_REQUEST);
		}

		final Pipeline pipeline = dataflowService.getPipelineByIdentification(pipelineIdentification);
		if (pipeline == null) {
			// Pipeline does not exist
			log.error(PIPELINE_NOT_EXISTS, pipelineIdentification);
			return new ResponseEntity<>(PIPELINE_NOT_EXITS_MSG, HttpStatus.NOT_FOUND);
		}

		if (!dataflowService.hasUserEditPermission(pipeline, domain.getUser().getUserId())) {
			// User has no permissions over the requested pipeline
			log.error("User has no EDIT permissions over the specified Pipeline '{}'.", pipelineIdentification);
			return new ResponseEntity<>(
					"{'error':'Forbidden. User has no EDIT permissions over the requested resource.'}",
					HttpStatus.FORBIDDEN);
		}
		return dataflowService.stopPipeline(domain.getUser().getUserId(), pipelineIdentification);
	}

	@Override
	public ResponseEntity<String> startDataflow(String domainName, String pipelineIdentification, String parameters,
			boolean resetOrigin) {
		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainName);

		if (domain == null) {
			log.error("Domain {} not found for DataFlow Start execution.", domainName);
			return new ResponseEntity<>(ERROR_DOMAIN + domainName + " not found for DataFlow Start execution: '"
					+ pipelineIdentification + "'.'}", HttpStatus.BAD_REQUEST);
		}

		final Pipeline pipeline = dataflowService.getPipelineByIdentification(pipelineIdentification);
		if (pipeline == null) {
			// Pipeline does not exist
			log.error(PIPELINE_NOT_EXISTS, pipelineIdentification);
			return new ResponseEntity<>(PIPELINE_NOT_EXITS_MSG, HttpStatus.NOT_FOUND);
		}

		if (!dataflowService.hasUserEditPermission(pipeline, domain.getUser().getUserId())) {
			// User has no permissions over the requested pipeline
			log.error("User has no EDIT permissions over the specified Pipeline '{}'.", pipelineIdentification);
			return new ResponseEntity<>(
					"{'error':'Forbidden. User has no EDIT permissions over the requested resource.'}",
					HttpStatus.FORBIDDEN);
		}

		if (resetOrigin) {
			dataflowService.resetOffsetPipeline(domain.getUser().getUserId(), pipelineIdentification);
		}
		return dataflowService.startPipeline(domain.getUser().getUserId(), pipelineIdentification, parameters);
	}

}

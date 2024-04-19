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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl.deployment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.MessageType;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.apimanager.operation.OperationJson;
import com.minsait.onesait.platform.config.services.apimanager.operation.QueryStringJson;
import com.minsait.onesait.platform.config.services.flow.FlowService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.flownode.FlowNodeService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DeployRequestRecord;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineDeployerApis;
import com.minsait.onesait.platform.flowengine.exception.FlowEngineDeployException;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowEngineDeploymentProcessorService {

	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private MultitenancyService masterUserService;
	@Autowired
	private FlowDomainService domainService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private FlowService flowService;
	@Autowired
	private FlowNodeService nodeService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectResourceAccessRepository projectResourceAccessRepository;

	private static final String REQUIRED = "REQUIRED";

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

}

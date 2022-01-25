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
package com.minsait.onesait.platform.flowengine.api.rest.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.audit.bean.OPAuthAuditEvent;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.commons.audit.producer.EventProducer;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.digitaltwin.type.DigitalTwinTypeService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DecodedAuthentication;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinDeviceDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DigitalTwinTypeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.RestApiOperationDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.UserDomainValidationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineNodeService;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.apis.FlowEngineApiService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.apis.FlowEngineControlPanelApiService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.dataflow.FlowEngineDataflowService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.deployment.FlowEngineDeploymentProcessorService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.notebook.FlowEngineNotebookService;
import com.minsait.onesait.platform.flowengine.audit.aop.FlowEngineAuditable;
import com.minsait.onesait.platform.flowengine.exception.NodeRedAdminServiceException;
import com.minsait.onesait.platform.flowengine.exception.NotAllowedException;
import com.minsait.onesait.platform.flowengine.exception.ResourceNotFoundException;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowEngineNodeServiceImpl implements FlowEngineNodeService {

	private static final String ADMINISTRATOR_STR = "ROLE_ADMINISTRATOR";
	private static final String CAUSE = "Cause";
	private static final String MESSAGE = ", Message = ";
	@Autowired(required = false)
	private RouterService routerService;

	@Autowired
	private FlowDomainService domainService;
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
	private MailService mailService;
	@Autowired
	private UserService userService;
	@Autowired
	private FlowEngineDataflowService flowengineDataflowService;
	@Autowired
	private FlowEngineNotebookService flowengineNoebookService;
	@Autowired
	private FlowEngineApiService flowengineApiService;
	@Autowired
	private FlowEngineDeploymentProcessorService flowengineDeploymentProcessorService;
	@Autowired
	private FlowEngineControlPanelApiService flowengineControlPanelApiService;
	@Autowired
	private EventRouter eventRouter;

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	void setUTF8Encoding() {
		restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	}

	@Override
	public ResponseEntity<String> deploymentNotification(String json) {
		return flowengineDeploymentProcessorService.deploymentNotification(json);

	}

	@Override
	public List<String> getApiRestCategories(String authentication) {
		return flowengineApiService.getApiRestCategories(authentication);
	}

	@Override
	public List<RestApiDTO> getApiRestByUser(String authentication) {
		return flowengineApiService.getApiRestByUser(authentication);
	}

	@Override
	public List<RestApiOperationDTO> getApiRestOperationsByUser(String apiName, Integer version,
			String authentication) {
		return flowengineApiService.getApiRestOperationsByUser(apiName, version, authentication);
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
		return flowengineApiService.invokeRestApiOperation(invokeRequest);
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
		return flowengineNoebookService.getNotebooksByUser(authentication);
	}

	@Override
	public String getNotebookJSONDataByUser(String notebookId, String authentication) {
		return flowengineNoebookService.getNotebookJSONDataByUser(notebookId, authentication);
	}

	@Override
	public ResponseEntity<String> invokeNotebook(NotebookInvokeDTO notebookInvocationData) {
		return flowengineNoebookService.invokeNotebook(notebookInvocationData);
	}

	@Override
	public List<String> getPipelinesByUser(String authentication) {
		return flowengineDataflowService.getPipelinesByUser(authentication);
	}

	@Override
	public ResponseEntity<String> getPipelineStatus(String domainName, String pipelineIdentification) {
		return flowengineDataflowService.getPipelineStatus(domainName, pipelineIdentification);
	}

	@Override
	public ResponseEntity<String> stopDataflow(String domainName, String pipelineIdentification) {
		return flowengineDataflowService.stopDataflow(domainName, pipelineIdentification);
	}

	@Override
	public ResponseEntity<String> startDataflow(String domainName, String pipelineIdentification, String parameters,
			boolean resetOrigin) {
		return flowengineDataflowService.startDataflow(domainName, pipelineIdentification, parameters, resetOrigin);
	}

	@Override
	public List<String> getControlpanelApis(String authentication) {
		return flowengineControlPanelApiService.getControlPanelApis(authentication);
	}

	@Override
	public List<RestApiOperationDTO> getControlpanelApiOperations(String apiName, String authentication) {
		return flowengineControlPanelApiService.getApiRestOperations(authentication, apiName);
	}

	@Override
	public ResponseEntity<String> invokeManagementRestApiOperation(
			FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		return flowengineControlPanelApiService.invokeManagementRestApiOperation(invokeRequest);
	}

	@Override
	public void submitAudit(String data, String domainName) throws JsonProcessingException, NotFoundException {
		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainName);

		if (domain != null) {

			OPAuthAuditEvent event = new OPAuthAuditEvent();

			Date now = new Date();

			event.setMessage(data);
			event.setModule(Module.FLOWENGINE);
			event.setType(EventType.DATA);
			event.setOperationType(OPAuditEvent.OperationType.LOG.name());
			event.setUser(domain.getUser().getUserId());
			event.setTimeStamp(now.getTime());
			eventRouter.notify(event.toJson());
		} else {
			log.error("Domain not found: {}", domainName);
			throw new NotFoundException("Domain not found " + domainName);
		}

	}

}

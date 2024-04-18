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
package com.minsait.onesait.platform.flowengine.api.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.DataflowDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineAuditRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInsertRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineQueryRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.UserDomainValidationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineNodeService;

import javassist.NotFoundException;

@RestController
@RequestMapping(value = "/node/services")
public class FlowEngineNodeServicesController {

	@Autowired
	private FlowEngineNodeService flowEngineNodeService;
	ObjectMapper mapper = new ObjectMapper();

	@PostMapping(value = "/deployment", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> deploymentNotification(@RequestBody String json) {
		return flowEngineNodeService.deploymentNotification(json);

	}

	@GetMapping(value = "/api/rest/categories", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getApiRestCategories(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getApiRestCategories(authentication));
		return callbackName + "(" + response + ")";
	}

	@GetMapping(value = "/user/api/rest", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getApiRestByUser(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getApiRestByUser(authentication));
		return callbackName + "(" + response + ")";
	}

	@GetMapping(value = "/user/api/rest/operations", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getApiRestOperationsByUser(@RequestParam("apiName") String apiName,
			@RequestParam("version") Integer version, @RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper
				.writeValueAsString(flowEngineNodeService.getApiRestOperationsByUser(apiName, version, authentication));
		return callbackName + "(" + response + ")";
	}

	@GetMapping(value = "/user/ontologies", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getOntologiesByUser(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getOntologyByUser(authentication));
		return callbackName + "(" + response + ")";
	}

	@GetMapping(value = "/user/client_platforms", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getClientPlatformsByUser(@RequestParam String authentication)
			throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getClientPlatformByUser(authentication));
		return "kpUser(" + response + ")";
	}

	@PostMapping(value = "/user/validate", produces = { "application/javascript", "application/json" })

	public @ResponseBody String getClientPlatformsByUser(@RequestBody UserDomainValidationRequest request) {
		return flowEngineNodeService.validateUserDomain(request);
	}

	@GetMapping(value = "/user/all_data", produces = { "application/javascript" })
	public @ResponseBody String getOntologiesAndClientPlatformsByUser(@RequestParam String authentication)
			throws JsonProcessingException {
		String ontologies = mapper.writeValueAsString(flowEngineNodeService.getOntologyByUser(authentication));
		String clientPlatforms = mapper
				.writeValueAsString(flowEngineNodeService.getClientPlatformByUser(authentication));
		StringBuilder response = new StringBuilder();
		response.append("dataAllUser([").append(ontologies.substring(1, ontologies.length() - 1)).append(",\"##$$##\",")
				.append(clientPlatforms.substring(1, clientPlatforms.length() - 1)).append("])");

		return response.toString();
	}

	@PostMapping(value = "/user/query", produces = { "application/javascript", "application/json" })
	public @ResponseBody String submitQuery(@RequestBody FlowEngineQueryRequest queryRequest)
			throws JsonProcessingException, NotFoundException {
		return flowEngineNodeService.submitQuery(queryRequest);
	}

	@PostMapping(value = "/user/insert", produces = { "application/javascript", "application/json" })
	public @ResponseBody String submitInsert(@RequestBody FlowEngineInsertRequest insertRequest)
			throws JsonProcessingException, NotFoundException {
		return flowEngineNodeService.submitInsert(insertRequest);
	}
	
	@PostMapping(value = "/user/audit", produces = { "application/javascript", "application/json" })
	public @ResponseBody String submitAudit(@RequestBody FlowEngineAuditRequest auditRequest)
			throws JsonProcessingException, NotFoundException {
            flowEngineNodeService.submitAudit( auditRequest.getData(),auditRequest.getDomainName());
            return "{\"result\":\"OK\"}";
	}

	@GetMapping(value = "/user/digital_twin_ypes", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getdigitalTwinTypes(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getDigitalTwinTypes(authentication));
		return callbackName + "(" + response + ")";
	}

	@PostMapping(value = "/user/invoke_rest_api_operation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> invokeRestApiOperation(
			@RequestBody FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		return flowEngineNodeService.invokeRestApiOperation(invokeRequest);
	}

	@PostMapping(value = "/sendMail", produces = { "application/javascript", "application/json" })
	public @ResponseBody String sendMail(@RequestBody MailRestDTO mailData) {
		flowEngineNodeService.sendMail(mailData);
		return null;
	}

	@GetMapping(value = "/user/notebooks", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getNotebooksByUser(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getNotebooksByUser(authentication));
		return callbackName + "(" + response + ")";
	}

	@GetMapping(value = "/user/notebooks/paragraphs", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getNotebookParagraphByUser(@RequestParam String authentication,
			@RequestParam String notebookZeId, @RequestParam("callback") String callbackName)
			throws JsonProcessingException {
		String response = mapper
				.writeValueAsString(flowEngineNodeService.getNotebookJSONDataByUser(notebookZeId, authentication));
		return callbackName + "(" + response + ")";
	}

	@PostMapping(value = "/user/notebooks/run", produces = { "application/javascript", "application/json" })
	public @ResponseBody ResponseEntity<String> runNotebook(@RequestBody NotebookInvokeDTO notebookInvocationData) {
		return flowEngineNodeService.invokeNotebook(notebookInvocationData);
	}

	@GetMapping(value = "/user/dataflows", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getDataflowsByUser(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getPipelinesByUser(authentication));
		return callbackName + "(" + response + ")";
	}

	@PostMapping(value = "/user/dataflow/status", produces = { "application/javascript", "application/json" })
	public @ResponseBody ResponseEntity<String> getDataflowStatus(@RequestBody DataflowDTO dataflowData) {
		return flowEngineNodeService.getPipelineStatus(dataflowData);
	}

	@PostMapping(value = "/user/dataflow/start", produces = { "application/javascript", "application/json" })
	public @ResponseBody ResponseEntity<String> startDataflow(@RequestBody DataflowDTO dataflowData) {
		return flowEngineNodeService.startDataflow(dataflowData);
	}

	@PostMapping(value = "/user/dataflow/stop", produces = { "application/javascript", "application/json" })
	public @ResponseBody ResponseEntity<String> stopDataflow(@RequestBody DataflowDTO dataflowData) {
		return flowEngineNodeService.stopDataflow(dataflowData);
	}

	@GetMapping(value = "/user/management/api/rest", produces = { "application/javascript", "application/json" })
	public @ResponseBody String getControlpanelApis(@RequestParam String authentication,
			@RequestParam("callback") String callbackName) throws JsonProcessingException {
		String response = mapper.writeValueAsString(flowEngineNodeService.getControlpanelApis(authentication));
		return callbackName + "(" + response + ")";
	}

	@GetMapping(value = "/user/management/api/rest/operations", produces = { "application/javascript",
			"application/json" })
	public @ResponseBody String getControlpanelApiOperations(@RequestParam("apiName") String apiName,
			@RequestParam String authentication, @RequestParam("callback") String callbackName)
			throws JsonProcessingException {
		String response = mapper
				.writeValueAsString(flowEngineNodeService.getControlpanelApiOperations(apiName, authentication));
		return callbackName + "(" + response + ")";
	}
	
	@PostMapping(value = "/user/management/invoke_rest_api_operation", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> invokeManagementRestApiOperation(
			@RequestBody FlowEngineInvokeRestApiOperationRequest invokeRequest) {
		return flowEngineNodeService.invokeManagementRestApiOperation(invokeRequest);
	}
}

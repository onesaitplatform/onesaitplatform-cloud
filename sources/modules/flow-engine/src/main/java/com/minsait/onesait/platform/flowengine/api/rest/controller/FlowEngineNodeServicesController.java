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
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInsertRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineQueryRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
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
		return flowEngineNodeService.submitQuery(queryRequest.getOntology(), queryRequest.getQueryType(),
				queryRequest.getQuery(), queryRequest.getAuthentication());
	}

	@PostMapping(value = "/user/insert", produces = { "application/javascript", "application/json" })
	public @ResponseBody String submitInsert(@RequestBody FlowEngineInsertRequest insertRequest)
			throws JsonProcessingException, NotFoundException {
		return flowEngineNodeService.submitInsert(insertRequest.getOntology(), insertRequest.getData(),
				insertRequest.getAuthentication());
	}

	@GetMapping(value = "/user/digital_twin_ypes", produces = {
			"application/javascript", "application/json" })
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

	@PostMapping(value = "/sendMail", produces = { "application/javascript",
			"application/json" })
	public @ResponseBody String sendMail(@RequestBody MailRestDTO mailData) {
		flowEngineNodeService.sendMail(mailData);
		return null;
	}

	@PostMapping(value = "/sendSimpleMail", produces = { "application/javascript",
			"application/json" })
	public @ResponseBody String sendsimpleMail(@RequestBody MailRestDTO mailData) {
		flowEngineNodeService.sendSimpleMail(mailData);
		return null;
	}

}

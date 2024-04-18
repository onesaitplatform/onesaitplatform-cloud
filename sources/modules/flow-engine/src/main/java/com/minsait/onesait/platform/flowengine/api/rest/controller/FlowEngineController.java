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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineNodeService;
import com.minsait.onesait.platform.flowengine.nodered.communication.NodeRedAdminClient;

@RestController
@RequestMapping(value = "/admin")
public class FlowEngineController {

	@Autowired
	private NodeRedAdminClient nodeRedClientAdmin;
	@Autowired
	private FlowEngineNodeService flowEngineNodeService;

	@PostMapping(value = "/stop", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String stopFlowEngine() {
		return nodeRedClientAdmin.stopFlowEngine();
	}

	@PutMapping(value = "/domain/stop/{domainId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody void stopFlowEngineDomain(@PathVariable(value = "domainId") String domainId) {
		nodeRedClientAdmin.stopFlowEngineDomain(domainId);
	}

	@PostMapping(value = "/domain/start", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String startFlowEngineDomain(@RequestBody FlowEngineDomain domain) {
		return nodeRedClientAdmin.startFlowEngineDomain(domain);
	}

	@PostMapping(value = "/domain", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String createFlowengineDomain(@RequestBody FlowEngineDomain domain) {
		return nodeRedClientAdmin.createFlowengineDomain(domain);
	}

	@DeleteMapping(value = "/domain/{domainId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody void deleteFlowEngineDomain(@PathVariable(value = "domainId") String domainId) {
		nodeRedClientAdmin.deleteFlowEngineDomain(domainId);
	}

	@GetMapping(value = "/domain/{domainId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody FlowEngineDomain getFlowEngineDomain(@PathVariable(value = "domainId") String domainId) {
		return nodeRedClientAdmin.getFlowEngineDomain(domainId);
	}

	// Generic Flow Engine Requests
	@GetMapping(value = "/domain/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<FlowEngineDomainStatus> getAllFlowEnginesDomains() {
		return nodeRedClientAdmin.getAllFlowEnginesDomains();
	}

	@GetMapping(value = "/domain/status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<FlowEngineDomainStatus> getFlowEngineDomainStatus(@RequestParam List<String> domainList) {
		return nodeRedClientAdmin.getFlowEngineDomainStatus(domainList);
	}

	// Synchronization of the active/inactive domains with CDB
	@PutMapping(value = "/sync/reset", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody void resetSynchronizedWithBDC() {
		nodeRedClientAdmin.resetSynchronizedWithBDC();
	}

	@PostMapping(value = "/sync", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String synchronizeMF(@RequestBody List<FlowEngineDomainStatus> domainList) {
		return nodeRedClientAdmin.synchronizeMF(domainList);
	}
	
	@PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> externalDeployment(@RequestBody String json) {
		return flowEngineNodeService.deploymentNotification(json);
	}
	
	@GetMapping(value = "/exportDomainFromFS/{domain}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String exportDomainFromFS(@PathVariable(value = "domain") String domain) {
		return nodeRedClientAdmin.exportDomainFromFS(domain);
	}
}

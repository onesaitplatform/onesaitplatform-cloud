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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@RequestMapping(value = "/stop", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String stopFlowEngine() {
		return nodeRedClientAdmin.stopFlowEngine();
	}

	@RequestMapping(value = "/domain/stop/{domainId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody void stopFlowEngineDomain(@PathVariable(value = "domainId") String domainId) {
		nodeRedClientAdmin.stopFlowEngineDomain(domainId);
	}

	@RequestMapping(value = "/domain/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String startFlowEngineDomain(@RequestBody FlowEngineDomain domain) {
		return nodeRedClientAdmin.startFlowEngineDomain(domain);
	}

	@RequestMapping(value = "/domain", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String createFlowengineDomain(@RequestBody FlowEngineDomain domain) {
		return nodeRedClientAdmin.createFlowengineDomain(domain);
	}

	@RequestMapping(value = "/domain/{domainId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody void deleteFlowEngineDomain(@PathVariable(value = "domainId") String domainId) {
		nodeRedClientAdmin.deleteFlowEngineDomain(domainId);
	}

	@RequestMapping(value = "/domain/{domainId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody FlowEngineDomain getFlowEngineDomain(@PathVariable(value = "domainId") String domainId) {
		return nodeRedClientAdmin.getFlowEngineDomain(domainId);
	}

	// Generic Flow Engine Requests
	@RequestMapping(value = "/domain/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<FlowEngineDomainStatus> getAllFlowEnginesDomains() {
		return nodeRedClientAdmin.getAllFlowEnginesDomains();
	}

	@RequestMapping(value = "/domain/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody List<FlowEngineDomainStatus> getFlowEngineDomainStatus(@RequestParam List<String> domainList) {
		return nodeRedClientAdmin.getFlowEngineDomainStatus(domainList);
	}

	// Synchronization of the active/inactive domains with CDB
	@RequestMapping(value = "/sync/reset", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody void resetSynchronizedWithBDC() {
		nodeRedClientAdmin.resetSynchronizedWithBDC();
	}

	@RequestMapping(value = "/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody String synchronizeMF(@RequestBody List<FlowEngineDomainStatus> domainList) {
		return nodeRedClientAdmin.synchronizeMF(domainList);
	}
	
	@PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> externalDeployment(@RequestBody String json) {
		return flowEngineNodeService.deploymentNotification(json);
	}
}

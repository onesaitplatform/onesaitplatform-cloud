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
package com.minsait.onesait.platform.controlpanel.rest.management.bpm;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.exceptions.BPMServiceException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "BPM Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/bpm")
@Slf4j
public class BPMRestController {

	@Autowired
	private BpmService bpmService;
	@Autowired
	private AppWebUtils utils;

	@Operation(summary = "Get all process definition")
	@GetMapping("/process-definition")
	public ResponseEntity<?> getAllProcessDefinition() {
		log.debug("Get all process definitions of bpms");
		try {
			Object values = bpmService.getAllProcessDef(utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(values);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Operation(summary = "Get process definition by id")
	@GetMapping("/process-definition/{id}/")
	public ResponseEntity<?> getProcessDef(
			@Parameter(description = "Id of the process definition", required = true) @PathVariable("id") String id,
			HttpServletResponse response) {
		try {
			Object bpm = bpmService.getAllProcessDefById(id, utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(bpm);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	@Operation(summary = "Get process definition xml by id")
	@GetMapping("/process-definition/{id}/xml")
	public ResponseEntity<String> getXmlProcessDef(
			@Parameter(description = "Id of the process definition", required = true) @PathVariable("id") String id,
			HttpServletResponse response) {
		try {
			String bpm = bpmService.getProcessDefXmlById(id, utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(bpm);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	
	@Operation(summary = "Clone process definition")
	@PostMapping("/clone-process-definition/")
	public ResponseEntity<?> cloneProcessDef(@RequestBody BpmRequestDTO request) {
		try {
			Object bpm = bpmService.cloneProcessDef(request, utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(bpm);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	@Operation(summary = "Get all deployments")
	@GetMapping("/deployment")
	public ResponseEntity<?> getAllDeployments() {
		log.debug("Get all process instances of bpms");
		try {
			Object values = bpmService.getAllDeployments(utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(values);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Operation(summary = "Get process instance by id")
	@GetMapping("/deployment/{id}/")
	public ResponseEntity<?> getDeployment(
			@Parameter(description = "Id of the process instance", required = true) @PathVariable("id") String id,
			HttpServletResponse response) {
		try {
			Object bpm = bpmService.getDeploymentById(id, utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(bpm);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	@Operation(summary = "Get all process instances")
	@GetMapping("/process-instance")
	public ResponseEntity<?> getAllProcessInstances() {
		log.debug("Get all process instances of bpms");
		try {
			Object values = bpmService.getAllProcessInstances(utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(values);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@Operation(summary = "Get deployment by id")
	@GetMapping("/process-instance/{id}/")
	public ResponseEntity<?> getProcessInstance(
			@Parameter(description = "Id of the deployment", required = true) @PathVariable("id") String id,
			HttpServletResponse response) {
		try {
			Object bpm = bpmService.getProcessInstanceById(id, utils.getUserOauthTokenByCurrentHttpRequest());
			return ResponseEntity.ok().body(bpm);
		} catch (BPMServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

}

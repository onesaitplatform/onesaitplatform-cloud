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
package com.minsait.onesait.platform.controlpanel.rest.management.kubernetes;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.controlpanel.service.kubernetes.KubernetesManagerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/kubernetes")
@Tag(name = "Kubernetes")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
public class KubernetesRestController {

	@Autowired
	KubernetesManagerService kubernetesClient;

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "")
	@Operation(summary = "Get deployments for a specified namespace")
	public ResponseEntity<?> getDeploymentsInNamespace() {
		try {
			return new ResponseEntity<>(kubernetesClient.getModulesByNamespace(),
					HttpStatus.OK);
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}

	}
	
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "getNodeMetrics")
	@Operation(summary = "Get deployments for a specified namespace")
	public ResponseEntity<?> getNodeMetrics() {
		try {
			return new ResponseEntity<>(kubernetesClient.getNodeMetrics(),
					HttpStatus.OK);
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}

	}
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "pause/{deployment}")
	@Operation(summary = "Pause a Deployment")
	public ResponseEntity<?> pause(@PathVariable("deployment") String deployment) {
		try {
			if (kubernetesClient.pauseDeployment(deployment)) {
				return new ResponseEntity<>("Deployment pause", HttpStatus.OK);

			} else {
				return new ResponseEntity<>("Deployment not found or couldn't be paused", HttpStatus.NOT_FOUND);

			}
			
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}


	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "resume/{deployment}")
	@Operation(summary = "Resume a Deployment")
	public ResponseEntity<?> resume(@PathVariable("deployment") String deployment) {
		try {
			if (kubernetesClient.resumeDeployment(deployment)) {
				return new ResponseEntity<>("Deployment resumed", HttpStatus.OK);

			} else {
				return new ResponseEntity<>("Deployment not found or wasn't paused", HttpStatus.NOT_FOUND);

			}
			
		}  catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}


	}
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "restart/{deployment}")
	@Operation(summary = "Restart a Deployment")
	public ResponseEntity<?> restart(@PathVariable("deployment") String deployment) {
		try {
			
			if (kubernetesClient.restartDeployment(deployment)) {
				return new ResponseEntity<>("Deployment restarted", HttpStatus.OK);

			} else {
				return new ResponseEntity<>("Deployment not found", HttpStatus.NOT_FOUND);

			}
			
		}  catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}


	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "scale/{deployment}/{scale}")
	@Operation(summary = "Scale a Deployment")
	public ResponseEntity<?> scale(@PathVariable("deployment") String deployment, @PathVariable("scale") int scale) {
		try {
			if (kubernetesClient.scaleDeployment(deployment, scale)) {
				return new ResponseEntity<>("Deployment scaled", HttpStatus.OK);

			} else {
				return new ResponseEntity<>("Deployment not found", HttpStatus.NOT_FOUND);

			}
		}  catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}


	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "updateImage/{deployment}/{image}")
	@Operation(summary = "Update an Deployment image")
	public ResponseEntity<?> updateImage(@PathVariable("deployment") String deployment, @PathVariable("image") String image) {
		try {
			if (kubernetesClient.updateDeploymentImage(deployment, image)) {
				return new ResponseEntity<>("Image updated", HttpStatus.OK);

			} else {
				return new ResponseEntity<>("Deployment not found", HttpStatus.NOT_FOUND);

			}			
		}  catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>("Error retrieving information", HttpStatus.NOT_FOUND);
		}


	}

//	@GetMapping(value = "nodes/{projectId}/{environmentId}/{namespace}")
//	@ApiOperation(value = "Get all the nodes in an Environment")
//	public ResponseEntity<?> getNodes(@PathVariable("projectId") String projectId,
//			@PathVariable("environmentId") String environmentId, @PathVariable("namespace") String namespace) {
//		try {
//			return new ResponseEntity<>(kubernetesClient.getNodeList(projectId, environmentId, namespace),
//					HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<>("Error finding the namespace", HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//
//	}

}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.microservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.services.exceptions.MicroserviceException;
import com.minsait.onesait.platform.config.services.microservice.MicroserviceService;
import com.minsait.onesait.platform.config.services.microservice.dto.DeployParameters;
import com.minsait.onesait.platform.config.services.microservice.dto.JenkinsParameter;
import com.minsait.onesait.platform.config.services.microservice.dto.MicroserviceDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.microservice.MicroserviceBusinessService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/microservices")
@Tag(name = "Microservices")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"),
	@ApiResponse(responseCode = "403", description = "Forbidden") })
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class MicroserviceRestController {

	@Autowired
	private MicroserviceBusinessService microserviceBusinessService;
	@Autowired
	private MicroserviceService microserviceService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;

	@Operation(summary = "Create a Microservice")
	@PostMapping
	public ResponseEntity<String> create(
			@Parameter(description = "Microservice configuration data") @RequestBody(required = true) @Valid MicroserviceEntity microservice) {
		if (!microservice.getMicroservice().getName().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		microservice.getMicroservice().setOwner(utils.getUserId());
		try {
			microserviceBusinessService.createMicroservice(microservice.getMicroservice(), microservice.getConfig(),
					null);
		} catch (final MicroserviceException e) {
			log.error("Could not create microservice {}", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error("Could not create microservice {}", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Operation(summary = "Update a Microservice")
	@PutMapping
	public ResponseEntity<String> update(
			@Parameter(description = "Microservice configuration data") @RequestBody(required = true) @Valid MicroserviceDTO microservice) {
		final Microservice serviceDb = microserviceService.getByIdentification(microservice.getName());
		if (serviceDb == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(serviceDb, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		microservice.setId(serviceDb.getId());
		microserviceService.update(microservice);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Build microservice")
	@PostMapping("{name}/build")
	public ResponseEntity<String> build(
			@Parameter(description = "Jenkins build parameters") @RequestBody(required = true) List<JenkinsParameter> parameters,
			@Parameter(description = "Microservice name") @PathVariable("name") String name) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		microserviceBusinessService.buildJenkins(ms, parameters);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Jenkins parameters for microservice build")
	@GetMapping("{name}/build/parameters")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=JenkinsParameter[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<List<JenkinsParameter>> parameters(
			@Parameter(description = "Microservice name") @PathVariable("name") String name) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(microserviceBusinessService.getJenkinsJobParameters(ms), HttpStatus.OK);
	}

	@Operation(summary = "Deploy microservice")
	@PostMapping("{name}/deploy")
	public ResponseEntity<String> deploy(
			@Parameter(description = "Microservice name") @PathVariable("name") String name,
			@Parameter(description = "CaaS environment") @RequestParam("environment") String environment,
			@Parameter(description = "CaaS Host/Worker", required = true) @RequestParam("worker") String worker,
			@Parameter(description = "Deployed onesait platform URL", required = false) @RequestParam(value = "onesaitServerUrl", defaultValue = "development.onesaitplatform.com") String onesaitServerUrl,
			@Parameter(description = "Docker image url", required = false) @RequestParam("dockerImageUrl") String dockerImageUrl) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (!StringUtils.hasText(dockerImageUrl)) {
			dockerImageUrl = ms.getDockerImage();
		}
		final String url = microserviceBusinessService.deployMicroservice(ms, environment, worker, onesaitServerUrl,
				dockerImageUrl);
		return new ResponseEntity<>(url, HttpStatus.OK);
	}

	@Operation(summary = "Upgrade microservice")
	@PostMapping("{name}/upgrade")
	public ResponseEntity<String> upgrade(
			@Parameter(description = "Microservice name") @PathVariable("name") String name,
			@Parameter(description = "Environment variables for upgrade", required = false) @RequestBody Map<String, String> env,
			@Parameter(description = "Docker image url", required = false) @RequestParam("dockerImageUrl") String dockerImageUrl) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (!StringUtils.hasText(dockerImageUrl)) {
			dockerImageUrl = ms.getDockerImage();
		}
		if (env == null) {
			env = new HashMap<>();
		}
		if (!StringUtils.hasText(dockerImageUrl)) {
			dockerImageUrl = ms.getDockerImage();
		}
		microserviceBusinessService.upgradeMicroservice(ms, dockerImageUrl, env);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Environments available in related CaaS")
	@GetMapping("{name}/deploy/environments")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = DeployParameters[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<DeployParameters> environemnts(
			@Parameter(description = "Microservice name") @PathVariable("name") String name) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		final DeployParameters parameters = microserviceBusinessService.getEnvironments(ms);
		return new ResponseEntity<>(parameters, HttpStatus.OK);
	}

	@Operation(summary = "Hosts/Workers available in requested related environment")
	@GetMapping("{name}/deploy/hosts")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = DeployParameters[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<DeployParameters> hosts(
			@Parameter(required = true) @RequestParam("environment") String environment,
			@Parameter(description = "Microservice name") @PathVariable("name") String name) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		final DeployParameters parameters = microserviceBusinessService.getHosts(ms, environment);
		return new ResponseEntity<>(parameters, HttpStatus.OK);
	}

	@Operation(summary = "Hosts/Workers available in requested related environment")
	@DeleteMapping("{name}")
	@ApiResponses(@ApiResponse(content = @Content(schema = @Schema(implementation = String.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<DeployParameters> delete(
			@Parameter(description = "Microservice name") @PathVariable("name") String name) {
		final Microservice ms = microserviceService.getByIdentification(name);
		if (ms == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(ms, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		microserviceBusinessService.deleteMicroservice(ms);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}

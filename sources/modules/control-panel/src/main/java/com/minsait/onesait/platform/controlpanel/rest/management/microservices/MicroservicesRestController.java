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
package com.minsait.onesait.platform.controlpanel.rest.management.microservices;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.CaaS;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.microservice.MicroserviceService;
import com.minsait.onesait.platform.controlpanel.rest.deployment.CaasPlatform;
import com.minsait.onesait.platform.controlpanel.rest.management.microservices.model.GitlabProject;
import com.minsait.onesait.platform.controlpanel.rest.management.microservices.model.MicroserviceDeployment;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsException;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsService;
import com.minsait.onesait.platform.controlpanel.services.jenkins.model.JenkinsBuild;
import com.minsait.onesait.platform.controlpanel.services.jenkins.model.JenkinsPipeline;
import com.minsait.onesait.platform.controlpanel.services.project.MSAServiceDispatcher;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Microservices")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@Slf4j
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@RequestMapping("api/v1/microservices")
public class MicroservicesRestController {

	@Autowired
	private JenkinsService jenkinsService;

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private MSAServiceDispatcher msaServiceDispatcher;
	@Autowired
	private MicroserviceService service;

	@Autowired
	private AppWebUtils utils;

	private static final String COULDNT_CREATE_PIPELINE = "Could not create Pipeline ";

	@Operation(summary = "Creates Microservice")
	@PostMapping
	public ResponseEntity<?> create(
			@Parameter(description = "Microservice Name", required = true) String microserviceName,
			@Parameter(description = "Template Type", required = true) TemplateType template) {

		if (!microserviceName.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		final Microservice microservice = new Microservice();
		microservice.setIdentification(microserviceName);
		microservice.setTemplateType(template.toString());
		microservice.setCaas(CaaS.RANCHER);

		// TO-DO Decide how to implement via REST
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Create Jenkins Pipeline")
	@PostMapping(value = "/jenkins", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "The URL of the Jenkins Pipeline")
	public ResponseEntity<?> createJenkinsPipeline(@ApiParam("Pipeline info") @Valid JenkinsPipeline pipeline,
			@ApiParam("XML Pipeline File") MultipartFile file, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}
		if (!file.getContentType().equalsIgnoreCase("text/xml")) {
			return new ResponseEntity<>("Invalid Config XML file, must be text/xml", HttpStatus.BAD_REQUEST);
		}

		try (InputStream is = file.getInputStream()) {
			jenkinsService.createJob(pipeline.getJenkinsUrl(), pipeline.getUsername(), pipeline.getToken(),
					pipeline.getJobName(), null, IOUtils.toString(is));
			final JobInfo job = jenkinsService.getJobInfo(pipeline.getJenkinsUrl(), pipeline.getUsername(),
					pipeline.getToken(), pipeline.getJobName(), null);
			return new ResponseEntity<>(job.url(), HttpStatus.OK);
		} catch (final JenkinsException e) {
			log.error("Could not create pipeline {}", e.getMessage());
			return new ResponseEntity<>(COULDNT_CREATE_PIPELINE + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (final IOException e1) {
			return new ResponseEntity<>("Invalid Config XML file content", HttpStatus.BAD_REQUEST);
		}

	}

	@Operation(summary = "Gets Jenkins Parameters")
	@PostMapping("/jenkins/parameters")
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "The URL of the Jenkins Pipeline")
	public ResponseEntity<?> getJenkinsParameters(@ApiParam("Pipeline info") @Valid JenkinsPipeline pipeline,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		try {
			final Map<String, String> parameters = jenkinsService.getParametersFromJob(pipeline.getJenkinsUrl(),
					pipeline.getUsername(), pipeline.getToken(), pipeline.getJobName());
			return new ResponseEntity<>(parameters, HttpStatus.OK);
		} catch (final JenkinsException e) {
			log.error("Could not create pipeline {}", e.getMessage());
			return new ResponseEntity<>(COULDNT_CREATE_PIPELINE + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Build Jenkins Pipeline")
	@PostMapping("/jenkins/build")
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "The URL of the Jenkins Pipeline")
	public ResponseEntity<?> buildJenkinsPipeline(
			@Parameter(description = "Pipeline Build Info", required = true) @Valid @RequestBody JenkinsBuild buildInfo) {

		try {
			final JobInfo job = jenkinsService.getJobInfo(buildInfo.getPipeline().getJenkinsUrl(),
					buildInfo.getPipeline().getUsername(), buildInfo.getPipeline().getToken(),
					buildInfo.getPipeline().getJobName(), null);
			if (job != null) {

				final Map<String, List<String>> params = buildInfo.getParameters().entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey(), e -> Arrays.asList(e.getValue())));
				jenkinsService.buildWithParameters(buildInfo.getPipeline().getJenkinsUrl(),
						buildInfo.getPipeline().getUsername(), buildInfo.getPipeline().getToken(),
						buildInfo.getPipeline().getJobName(), null, params);
				return new ResponseEntity<>("Pipeline is being executed, more info at " + job.url(), HttpStatus.OK);
			}
			return new ResponseEntity<>("Could not create Pipeline, job==null ", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (final JenkinsException e) {
			log.error("Could not build pipeline {}", e.getMessage());
			return new ResponseEntity<>(COULDNT_CREATE_PIPELINE + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Create Gitlab Project")
	@PostMapping("/gitlab")
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "The URL of the Gitlab project")
	@Deprecated
	public ResponseEntity<?> createGitlabProject(
			@ApiParam("Gitlab Parameters") @Valid @RequestBody GitlabProject gitlabProject, Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Operation(summary = "Deploys an existing Microservice")
	@PostMapping("/deploy")
	public ResponseEntity<?> deploy(@ApiParam("Deploy Configuration") @RequestBody @Valid MicroserviceDeployment deploy,
			Errors errors) throws GenericOPException {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		String url = deploy.getDeploymentUrl();

		if (deploy.getCaasPlatform().equals(CaasPlatform.RANCHER)) {

			if (StringUtils.isEmpty(deploy.getDeploymentUrl())) {
				RancherConfiguration config;

				try {
					config = configurationService.getRancherConfiguration("", "default");
					if (config == null) {
						throw new GenericOPException();
					}
				} catch (final RuntimeException e) {
					log.error("There's no configuration for Rancher");
					return new ResponseEntity<>(
							"Deployment URL is empty and there's no Rancher configuration available",
							HttpStatus.BAD_REQUEST);
				}
				try {
					msaServiceDispatcher.dispatch(CaaS.RANCHER).deployMicroservice(config, deploy.getEnvironment(),
							deploy.getMicroserviceName(), deploy.getDockerImageURL(), deploy.getOnesaitServerName(),
							deploy.getContextPath(), deploy.getPort());
					url = config.getUrl();
				} catch (final Exception e) {
					log.error("Could not deploy microservice", e);
					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {
				try {
					msaServiceDispatcher.dispatch(CaaS.RANCHER).deployMicroservice(
							RancherConfiguration.builder().url(deploy.getDeploymentUrl())
									.accessKey(deploy.getAccessKey()).secretKey(deploy.getSecretKey()).build(),
							deploy.getEnvironment(), deploy.getMicroserviceName(), deploy.getDockerImageURL(),
							deploy.getOnesaitServerName(), deploy.getContextPath(), deploy.getPort());
				} catch (final Exception e) {
					return new ResponseEntity<>("Could not deploy on Rancher environment " + deploy.getEnvironment()
							+ " reason: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

		} else {
			return new ResponseEntity<>("Only Rancher is supported ", HttpStatus.NOT_IMPLEMENTED);
		}

		return new ResponseEntity<>("Environment successfully deployed in " + url, HttpStatus.OK);
	}

	@Operation(summary = "Creates a Microservice from a Spring Boot template. "
			+ "This operation executes the full flow: publish project to Gitlab repo,"
			+ " create Jenkins Pipeline, build pipeline with parameters (if provided), and finally deploy it to Rancher."
			+ "Only use this operation for testing purposes.")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("/full/pipeline")
	public ResponseEntity<?> executeFullFlow() {
		final String url = "";

		return new ResponseEntity<>("Environment successfully deployed in " + url, HttpStatus.OK);

	}

	@Operation(summary = "Restart microservice")
	@PostMapping("/{identification}/restart")
	public ResponseEntity<?> restart(@PathVariable("identification") String identification) {
		final Microservice m = service.getByIdentification(identification);
		if (m == null) {
			return ResponseEntity.notFound().build();
		}
		if (!utils.isAdministrator() && !utils.getUserId().equals(m.getUser().getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		msaServiceDispatcher.dispatch(m.getCaas()).stopService(m.getCaaSConfiguration(), m.getStackOrNamespace(),
				m.getStackOrNamespace(), m.getIdentification());
		msaServiceDispatcher.dispatch(m.getCaas()).startService(m.getCaaSConfiguration(), m.getStackOrNamespace(),
				m.getStackOrNamespace(), m.getIdentification());
		return ResponseEntity.ok().build();

	}

	@Operation(summary = "Restart microservice")
	@PostMapping("/{identification}/start")
	public ResponseEntity<?> start(@PathVariable("identification") String identification) {
		final Microservice m = service.getByIdentification(identification);
		if (m == null) {
			return ResponseEntity.notFound().build();
		}
		if (!utils.isAdministrator() && !utils.getUserId().equals(m.getUser().getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		msaServiceDispatcher.dispatch(m.getCaas()).startService(m.getCaaSConfiguration(), m.getStackOrNamespace(),
				m.getStackOrNamespace(), m.getIdentification());
		return ResponseEntity.ok().build();

	}

	@Operation(summary = "Restart microservice")
	@PostMapping("/{identification}/stop")
	public ResponseEntity<?> stop(@PathVariable("identification") String identification) {
		final Microservice m = service.getByIdentification(identification);
		if (m == null) {
			return ResponseEntity.notFound().build();
		}
		if (!utils.isAdministrator() && !utils.getUserId().equals(m.getUser().getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		msaServiceDispatcher.dispatch(m.getCaas()).stopService(m.getCaaSConfiguration(), m.getStackOrNamespace(),
				m.getStackOrNamespace(), m.getIdentification());
		return ResponseEntity.ok().build();

	}

}

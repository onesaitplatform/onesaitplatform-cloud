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
package com.minsait.onesait.platform.controlpanel.controller.microservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Microservice.CaaS;
import com.minsait.onesait.platform.config.model.Microservice.TemplateType;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.microservice.MicroserviceService;
import com.minsait.onesait.platform.config.services.microservice.dto.DeployParameters;
import com.minsait.onesait.platform.config.services.microservice.dto.GitTemplateMicroservice;
import com.minsait.onesait.platform.config.services.microservice.dto.JenkinsParameter;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.microservice.dto.MicroserviceDTO;
import com.minsait.onesait.platform.config.services.microservice.dto.ZipMicroservice;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.gateway.CloudGatewayService;
import com.minsait.onesait.platform.controlpanel.services.microservice.MicroserviceBusinessService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("microservices")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class MicroserviceController {

	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";
	@Autowired
	private MicroserviceService microserviceService;
	@Autowired
	private MicroserviceBusinessService microserviceBusinessService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ProjectService projectService;

	@Autowired
	private CloudGatewayService cloudGatewayService;
	@Autowired
	private NotebookService notebookService;

	@Autowired
	private HttpSession httpSession;

	private static final String DEFAULT_SOURCES_PATH = "sources/";
	private static final String DEFAULT_DOCKER_PATH = "sources/docker/";
	private static final String APP_ID = "appId";

	@GetMapping("list")
	public String list(Model model) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		if (utils.isAdministrator()) {
			model.addAttribute("microservices", microserviceService.getAllMicroservices());
		} else {
			model.addAttribute("microservices",
					microserviceService.getMicroservices(userService.getUser(utils.getUserId())));
		}

		return "microservice/list";
	}

	@GetMapping("jenkins/parameters/{id}")
	public @ResponseBody List<JenkinsParameter> parameters(@PathVariable("id") String id) {
		final Microservice microservice = microserviceService.getById(id);
		if (microservice != null
				&& microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return microserviceBusinessService.getJenkinsJobParameters(microservice);
		}
		return new ArrayList<>();
	}

	@GetMapping("data")
	public @ResponseBody List<MicroserviceDTO> listData(Model model) {
		List<Microservice> microservices = null;
		if (utils.isAdministrator()) {
			microservices = microserviceService.getAllMicroservices();
		} else {
			microservices = microserviceService.getMicroservices(userService.getUser(utils.getUserId()));
		}

		return microservices.stream()
				.map(m -> MicroserviceDTO.builder().caasUrl(getCaas(m)).caas(m.getCaas().name())
						.gitlab(m.getGitlabRepository()).id(m.getId()).name(m.getIdentification())
						.isDeployed(getDeployStatus(m)).deploymentUrl(cloudGatewayService.getDeployedMicroserviceURL(m))
						.jenkins(m.getJobUrl()).owner(m.getUser().getUserId()).contextPath(m.getContextPath())
						.template(m.getTemplateType())
						.lastBuild(m.getJenkinsQueueId() == null ? null : String.valueOf(m.getJenkinsQueueId()))
						.build())
				.collect(Collectors.toList());
	}

	@GetMapping("create")
	public String create(Model model) {
		model.addAttribute("microservice", MicroserviceDTO.builder()
				.config(MSConfig.builder().sources(DEFAULT_SOURCES_PATH).docker(DEFAULT_DOCKER_PATH).build())
				.port(30000)
				.zipInfo(ZipMicroservice.builder().sources(DEFAULT_SOURCES_PATH).docker(DEFAULT_DOCKER_PATH).build())
				.gitTemplate(GitTemplateMicroservice.builder().sources(DEFAULT_SOURCES_PATH).docker(DEFAULT_DOCKER_PATH)
						.build())
				.build());
		model.addAttribute("caas", CaaS.values());
		model.addAttribute("templates", TemplateType.values());
		model.addAttribute("defaultGitlab", configurationService.getDefautlGitlabConfiguration() != null);
		model.addAttribute("defaultCaaS", configurationService.getDefaultRancherConfiguration() != null);
		model.addAttribute("defaultJenkins", configurationService.getDefaultJenkinsConfiguration() != null);
		final List<OntologyDTO> ontologies = ontologyService
				.getAllOntologiesForListWithProjectsAccess(utils.getUserId());
		model.addAttribute("ontologies", ontologies);
		final List<Notebook> notebooks = notebookService.getNotebooks(utils.getUserId());
		model.addAttribute("notebooks", notebooks);
		return "microservice/create";
	}

	@GetMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id) {
		final Microservice ms = microserviceService.getById(id);
		if (ms == null) {
			return ERROR_404;
		}
		model.addAttribute("microservice", MicroserviceDTO.builder().name(ms.getIdentification())
				.rancherConfiguration(ms.getRancherConfiguration()).contextPath(ms.getContextPath()).port(ms.getPort())
				.caas(ms.getCaas().name()).openshiftConfiguration(ms.getOpenshiftConfiguration())
				.gitlabConfiguration(ms.getGitlabConfiguration()).template(ms.getTemplateType())
				.jenkinsConfiguration(ms.getJenkinsConfiguration()).id(ms.getId()).build());
		return "microservice/create";

	}

	@PostMapping("update/{id}")
	public String update(Model model, MicroserviceDTO microservice, @PathVariable("id") String id) {
		final Microservice serviceDb = microserviceService.getById(id);
		if (serviceDb == null) {
			return ERROR_404;
		}
		if (!microserviceService.hasUserPermission(serviceDb, userService.getUser(utils.getUserId()))) {
			return ERROR_403;
		}
		microservice.setId(serviceDb.getId());
		microserviceService.update(microservice);
		return "redirect:/microservices/list";
	}

	@PostMapping("create")
	public String createPost(Model model, @Valid MicroserviceDTO microservice, RedirectAttributes ra) {
		try {
			microservice.setOwner(utils.getUserId());
			if (microservice.getZipInfo().getFile() != null
					&& microservice.getTemplate().equals(Microservice.TemplateType.IMPORT_FROM_ZIP)) {
				microservice.getConfig().setSources(microservice.getZipInfo().getSources());
				microservice.getConfig().setDocker(microservice.getZipInfo().getDocker());
				microserviceBusinessService.createMicroserviceZipImport(microservice, microservice.getConfig(),
						microservice.getZipInfo().getFile());
			} else if (microservice.getTemplate().equals(Microservice.TemplateType.IMPORT_FROM_GIT)) {
				microservice.getConfig().setSources(microservice.getGitTemplate().getSources());
				microservice.getConfig().setDocker(microservice.getGitTemplate().getDocker());
				microserviceBusinessService.createMicroservice(microservice, microservice.getConfig(), null);
			}else if(microservice.getTemplate().equals(Microservice.TemplateType.MLFLOW_MODEL)) {
				microservice.getConfig().setCreateGitlab(false);
				microserviceBusinessService.createMicroservice(microservice, microservice.getConfig(), null);
			} else {
				microserviceBusinessService.createMicroservice(microservice, microservice.getConfig(), null);
			}

		} catch (final Exception e) {
			log.error("Could not create Microservice", e);
			utils.addRedirectException(e, ra);
		}
		return "redirect:/microservices/list";
	}

	@PostMapping(value = "jenkins/build/{id}", consumes = "application/json")
	public ResponseEntity<String> buildWithParameters(@PathVariable("id") String id,
			@RequestBody List<JenkinsParameter> parameters) {

		final Microservice microservice = microserviceService.getById(id);
		if (microservice == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		try {
			final int i = microserviceBusinessService.buildJenkins(microservice, parameters);
			return new ResponseEntity<>(String.valueOf(i), HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Could not complete jenkins build", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("jenkins/completed/{id}")
	public ResponseEntity<String> buildWithParameters(@PathVariable("id") String id) {
		final Microservice microservice = microserviceService.getById(id);
		if (microservice == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			if (microserviceBusinessService.hasPipelineFinished(microservice)) {
				return new ResponseEntity<>("y", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("n", HttpStatus.OK);
			}
		} catch (final Exception e) {
			log.error("Could not complete jenkins build", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("deploy/{id}/parameters")
	public String deployParameters(Model model, @PathVariable("id") String id,
			@RequestParam(value = "upgrade", required = false, defaultValue = "false") Boolean upgrade,
			@RequestParam(value = "hosts", required = false, defaultValue = "false") Boolean hosts,
			@RequestParam(value = "environment", required = false, defaultValue = "") String environment) {
		final Microservice microservice = microserviceService.getById(id);
		if (microservice == null) {
			return ERROR_404;
		}
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return ERROR_403;
		}
		if (microservice.getCaas().equals(CaaS.RANCHER)) {

			if (upgrade) {
				model.addAttribute("currentImageUrl", microservice.getDockerImage());
				model.addAttribute("microserviceId", microservice.getId());
				model.addAttribute("env", microserviceBusinessService.getEnvMap(microservice));
				return "microservice/fragments/upgrade-modal";
			} else if (!hosts) {
				final DeployParameters parameters = microserviceBusinessService.getEnvironments(microservice);
				model.addAttribute("deploymentParameters", parameters);
				return "microservice/fragments/deployment-modal";
			} else {
				final DeployParameters parameters = microserviceBusinessService.getHosts(microservice, environment);
				model.addAttribute("deploymentParameters", parameters);
				return "microservice/fragments/deployment-modal";
			}
		} else {
			if (upgrade) {
				model.addAttribute("currentImageUrl", microservice.getDockerImage());
				model.addAttribute("microserviceId", microservice.getId());
				model.addAttribute("env", microserviceBusinessService.getEnvMap(microservice));
				model.addAttribute("caas", microservice.getCaas().name());
				return "microservice/fragments/upgrade-openshift-modal";
			} else if (!hosts) {
				final DeployParameters parameters = microserviceBusinessService.getEnvironments(microservice);
				model.addAttribute("deploymentParameters", parameters);
				model.addAttribute("caas", microservice.getCaas().name());
				return "microservice/fragments/deployment-openshift-modal";
			} else {
				final DeployParameters parameters = microserviceBusinessService.getHosts(microservice, environment);
				model.addAttribute("deploymentParameters", parameters);
				model.addAttribute("caas", microservice.getCaas().name());
				return "microservice/fragments/deployment-openshift-modal";
			}
		}
		// throw new MicroserviceException("Unsupported CaaS");
	}

	@PostMapping(value = "deploy/{id}")
	public ResponseEntity<String> deploy(@PathVariable("id") String id, @RequestParam("environment") String environment,
			@RequestParam("worker") String worker, @RequestParam("onesaitServerUrl") String onesaitServerUrl,
			@RequestParam("dockerImageUrl") String dockerImageUrl,
			@RequestParam(value = "stack", required = false) String stack) {
		final Microservice microservice = microserviceService.getById(id);
		if (microservice == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		final String url;
		if (microservice.getCaas().equals(CaaS.RANCHER)) {
			if (!StringUtils.hasText(stack)) {
				url = microserviceBusinessService.deployMicroservice(microservice, environment, worker,
						onesaitServerUrl, dockerImageUrl);
			} else {
				url = microserviceBusinessService.deployMicroservice(microservice, environment, worker,
						onesaitServerUrl, dockerImageUrl, stack);
			}
			return new ResponseEntity<>(url, HttpStatus.OK);
		} else /* if (microservice.getCaas().equals(CaaS.OPENSHIFT)) */ {
			url = microserviceBusinessService.deployMicroservice(microservice, environment, worker, onesaitServerUrl,
					dockerImageUrl);
			return new ResponseEntity<>(url, HttpStatus.OK);
		}
		// return new ResponseEntity<>("Not supported", HttpStatus.BAD_REQUEST);

	}

	@PostMapping(value = "upgrade/{id}")
	public ResponseEntity<String> upgrade(@PathVariable("id") String id,
			@RequestParam("dockerImageUrl") String dockerImageUrl, @RequestParam String env) throws IOException {
		final Microservice microservice = microserviceService.getById(id);
		if (microservice == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (microservice.getCaas().equals(CaaS.RANCHER)) {
			Map<String, String> mapEnv = new HashMap<>();
			if (!!StringUtils.hasText(env)) {
				final ObjectMapper mapper = new ObjectMapper();
				mapEnv = mapper.readValue(env, new TypeReference<Map<String, String>>() {
				});
			}
			final String url = microserviceBusinessService.upgradeMicroservice(microservice, dockerImageUrl, mapEnv);

			return new ResponseEntity<>(url, HttpStatus.OK);
		} else /* if (microservice.getCaas().equals(CaaS.OPENSHIFT) */ {
			// TO-DO
			Map<String, String> mapEnv = new HashMap<>();
			if (!!StringUtils.hasText(env)) {
				final ObjectMapper mapper = new ObjectMapper();
				mapEnv = mapper.readValue(env, new TypeReference<Map<String, String>>() {
				});
			}
			final String url = microserviceBusinessService.upgradeMicroservice(microservice, dockerImageUrl, mapEnv);
			return new ResponseEntity<>(url, HttpStatus.OK);
		}
		// return new ResponseEntity<>("Not supported", HttpStatus.BAD_REQUEST);

	}

	@PostMapping("stop/{id}")
	public ResponseEntity<String> stop(@PathVariable("id") String id) {
		final Microservice microservice = microserviceService.getById(id);
		if (microservice == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			microserviceBusinessService.stopMicroservice(microservice);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<String> delete(@PathVariable("id") String id) {
		final Microservice microservice = microserviceService.getById(id);
		if (!microserviceService.hasUserPermission(microservice, userService.getUser(utils.getUserId()))) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (microservice == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		microserviceBusinessService.deleteMicroservice(microservice);
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	private String getCaas(Microservice m) {
		if (m.getCaas().equals(CaaS.RANCHER)) {
			return m.getRancherConfiguration().getUrl();
		} else {
			return m.getOpenshiftConfiguration().getUrl();
		}
	}

	private boolean getDeployStatus(Microservice m) {
		if (m.getCaas().equals(CaaS.RANCHER)) {
			return m.getRancherStack() != null;
		} else {
			return m.getOpenshiftNamespace() != null;
		}
	}

}

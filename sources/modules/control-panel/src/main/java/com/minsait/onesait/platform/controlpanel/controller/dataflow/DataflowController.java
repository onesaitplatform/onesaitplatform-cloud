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
package com.minsait.onesait.platform.controlpanel.controller.dataflow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.DataflowInstance;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessRepository;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.dataflow.beans.InstanceBuilder;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dataflow")
@Controller
@Slf4j
public class DataflowController {

	private static final String DATAFLOW_VERSION_STR = "dataflowVersion";

	@Autowired
	private DataflowService dataflowService;

	@Autowired
	private PipelineRepository pipelineRepository;

	@Autowired
	private PipelineUserAccessRepository pipelineUserAccessRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@Autowired
	private HttpSession httpSession;

	@Autowired
	ServletContext context;

	private static final String APP_ID = "appId";

	/* TEMPLATES VIEWS */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/list", "/list/{redirect}" }, produces = "text/html")
	public String list(Model uiModel, @PathVariable("redirect") Optional<Boolean> redirect) {
		final String instanceIdentification = dataflowService.getDataflowInstanceForUserId(utils.getUserId())
				.getIdentification();
		uiModel.addAttribute("lpl", dataflowService.getPipelinesWithStatus(utils.getUserId()));
		uiModel.addAttribute("user", utils.getUserId());
		uiModel.addAttribute("userRole", utils.getRole());
		uiModel.addAttribute(DATAFLOW_VERSION_STR, dataflowService.getVersion());
		uiModel.addAttribute("instance", instanceIdentification);

		if (!redirect.isPresent()) {
			// CLEANING APP_ID FROM SESSION
			httpSession.removeAttribute(APP_ID);
		} else {
			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				uiModel.addAttribute(APP_ID, projectId.toString());
				httpSession.removeAttribute(APP_ID);
			}
		}

		return "dataflow/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = { "/{instance}/app", "/{instance}/app/collector/jvmMetrics", "/{instance}/app/collector/logs",
			"/{instance}/app/collector/configuration", "/{instance}/app/collector/packageManager" })
	public String getToolsWithInstance(@PathVariable("instance") String id, Model uiModel) {
		uiModel.addAttribute(DATAFLOW_VERSION_STR, dataflowService.getVersion());
		return "dataflow/index";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/app/collector/pipeline/{id}", "/app/collector/logs/{name}/{id}" })
	public String getPipeline(@PathVariable("id") String id, Model uiModel) {
		final Pipeline dataFlow = pipelineRepository.findByIdstreamsets(id);
		if (dataFlow != null) {
			if (dataflowService.hasUserViewPermission(dataFlow, utils.getUserId())) {
				uiModel.addAttribute(ResourcesInUseService.RESOURCEINUSE,
						resourcesInUseService.isInUse(id, utils.getUserId()));
				resourcesInUseService.put(id, utils.getUserId());
				uiModel.addAttribute(DATAFLOW_VERSION_STR, dataflowService.getVersion());
				return "dataflow/index";
			} else {
				return "redirect:/403";
			}
		} else {
			return "redirect:/404";
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/{instance}/app/collector/pipeline/{id}" })
	public String showDataFlowForManager(@PathVariable("id") String id) {
		final Pipeline dataFlow = pipelineRepository.findByIdstreamsets(id);
		if (dataFlow != null) {
			final String idStreamsets = dataFlow.getIdstreamsets();
			return "redirect:/dataflow/app/collector/pipeline/" + idStreamsets;
		} else {
			return "redirect:/404";
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/show/{id}" })
	public String showDataFlow(@PathVariable("id") String id) {
		final Pipeline dataFlow = pipelineRepository.findById(id).orElse(null);
		if (dataFlow != null) {
			final String idStreamsets = dataFlow.getIdstreamsets();
			return "redirect:/dataflow/app/collector/pipeline/" + idStreamsets;
		} else {
			return "redirect:/404";
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/share/{id}", produces = "text/html")
	public String share(Model model, @PathVariable("id") String id) {
		final String userId = utils.getUserId();
		final Pipeline pipeline = dataflowService.getPipelineById(id);

		// Only owners and administrators
		if (utils.isAdministrator() || pipeline.getUser().getUserId().equals(userId)) {
			final List<PipelineUserAccess> pipelineUserAccesses = pipelineUserAccessRepository.findByPipeline(pipeline);
			final List<User> usersAlreadyGivenAccess = pipelineUserAccesses.stream().map(PipelineUserAccess::getUser)
					.collect(Collectors.toList());

			// Get all users - not the same user from pipeline, active and analytics
			final List<User> users = userService.getAllActiveUsers().stream()
					.filter(user -> !user.getUserId().equals(pipeline.getUser().getUserId()))
					.filter(user -> userService.isUserAdministrator(user) || userService.isUserAnalytics(user))
					.filter(user -> !usersAlreadyGivenAccess.contains(user)).collect(Collectors.toList());

			model.addAttribute("users", users);
			model.addAttribute("int", pipelineUserAccesses);
			model.addAttribute("pipelineid", pipeline.getId());

			return "dataflow/share";
		} else {
			return "error/403";
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/instances", produces = "text/html")
	public String getDataflowInstances(Model uiModel) {
		final List<DataflowInstance> instances = dataflowService.getAllDataflowInstances();
		uiModel.addAttribute("instances", instances);
		return "dataflow/instances";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("/instances/instance")
	public String newDataflowInstance(Model model) {
		final DataflowInstance instance = new DataflowInstance();
		final List<User> users = dataflowService.getFreeAnalyticsUsers();

		model.addAttribute("instance", instance);
		model.addAttribute("users", users);
		return "dataflow/instance";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("/instances/instance/{id}")
	public String getDataflowInstance(Model model, @PathVariable("id") String id) {
		try {
			final DataflowInstance instance = dataflowService.getDataflowInstanceById(id);
			if (instance == null) {
				return "error/404";
			} else {
				final List<User> users = dataflowService.getFreeAnalyticsUsers();
				if (instance.getUser() != null) {
					users.add(instance.getUser());
				}

				model.addAttribute("users", users);
				model.addAttribute("instance", instance);
				model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
						resourcesInUseService.isInUse(id, utils.getUserId()));
				resourcesInUseService.put(id, utils.getUserId());

				return "dataflow/instance";
			}
		} catch (final Exception e) {
			return "error/403";
		}
	}

	/* PIPELINES OPS */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PutMapping(value = "/pipeline")
	@ResponseBody
	public ResponseEntity<String> createPipeline(@RequestBody Pipeline pipeline, BindingResult bindingResult) {
		final Pipeline newPipeline = dataflowService.createPipeline(pipeline, utils.getUserId());
		return new ResponseEntity<>(newPipeline.getIdstreamsets(), HttpStatus.CREATED);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping(value = "/pipeline/clone")
	@ResponseBody
	public ResponseEntity<String> clonePipeline(@RequestParam String identificationFrom,
			@RequestParam String identificationTo) {
		final ResponseEntity<String> response = dataflowService.clonePipeline(utils.getUserId(), identificationFrom,
				identificationTo);
		final JSONObject createResponseObj = new JSONObject(response.getBody());
		final String streamsetsId = createResponseObj.getJSONObject("pipelineConfig").getString("pipelineId");
		return new ResponseEntity<>(streamsetsId, HttpStatus.CREATED);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping(value = "/pipeline/rename", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<String> renamePipeline(@RequestParam String id, @RequestParam String newIdentification) {
		final Pipeline pipeline = dataflowService.renamePipeline(id, utils.getUserId(), newIdentification);
		return new ResponseEntity<>(pipeline.getIdentification(), HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@DeleteMapping(value = "/pipeline/{id}", produces = "text/html")
	public ResponseEntity removePipeline(@PathVariable("id") String id) {
		dataflowService.deleteHardPipeline(id, utils.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@DeleteMapping(value = "/pipeline/hardDelete/{id}", produces = "text/html")
	public ResponseEntity removeHardPipeline(@PathVariable("id") String id) {
		dataflowService.deletePipeline(id, utils.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/* AUTHORIZATION OPS */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<PipelineUserAccess> createAuthorization(@RequestParam String accesstype,
			@RequestParam String dataflow, @RequestParam String user) {
		final PipelineUserAccess userAccess = dataflowService.createUserAccess(dataflow, utils.getUserId(), accesstype,
				user);
		return new ResponseEntity<>(userAccess, HttpStatus.CREATED);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@DeleteMapping(value = "/auth/{id}")
	@ResponseBody
	public ResponseEntity deleteAuthorization(@PathVariable("id") String id) {
		dataflowService.deleteUserAccess(id, utils.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping("/public")
	@ResponseBody
	public ResponseEntity changePublic(@RequestParam("id") String dataflowId) {
		dataflowService.changePublic(dataflowId, utils.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/* INSTANCES OPS */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("/instances/instance")
	@ResponseBody
	public ResponseEntity<DataflowInstance> createDataflowInstance(@Valid @RequestBody InstanceBuilder instanceBuilder,
			BindingResult bindingResult) {
		final DataflowInstance instance = dataflowService.createDataflowInstance(instanceBuilder);
		return new ResponseEntity<>(instance, HttpStatus.CREATED);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping("/instances/instance/{id}")
	@ResponseBody
	public ResponseEntity<DataflowInstance> updateDataflowInstance(@PathVariable("id") String id,
			@Valid @RequestBody InstanceBuilder instanceBuilder, BindingResult bindingResult) {
		final DataflowInstance updatedInstance = dataflowService.updateDataflowInstance(id, instanceBuilder);
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return new ResponseEntity<>(updatedInstance, HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/instances/instance/{id}")
	@ResponseBody
	public ResponseEntity<String> deleteDataflowInstance(@PathVariable("id") String instanceId,
			@RequestParam("action") String action) {
		dataflowService.deleteDataflowInstance(instanceId, action, utils.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("/instances/instance/{id}/restart")
	@ResponseBody
	public ResponseEntity<String> restartInstance(@PathVariable("id") String instanceId) {
		dataflowService.restartDataflowInstance(instanceId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/* STREAMSETS REST OPS */

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@RequestMapping(value = { "/app/rest/**" }, method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,
			RequestMethod.PUT }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> appRest(HttpServletRequest request, @RequestBody(required = false) String body) {
		ResponseEntity<String> dataflowResponse = dataflowService.sendHttp(request,
				HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
		return ResponseEntity.status(dataflowResponse.getStatusCode()).body(dataflowResponse.getBody());
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value = { "/{instance}/app/rest/**" }, method = { RequestMethod.GET, RequestMethod.POST,
			RequestMethod.DELETE, RequestMethod.PUT }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> appRestWithInstance(@PathVariable("instance") String instance,
			HttpServletRequest request, @RequestBody(required = false) String body) {
		ResponseEntity<String> dataflowResponse = dataflowService.sendHttpWithInstance(request,
				HttpMethod.valueOf(request.getMethod()), body, instance);
		return ResponseEntity.status(dataflowResponse.getStatusCode()).body(dataflowResponse.getBody());
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/app/rest/v1/definitions/stages/{lib}/{id}/icon" })
	@ResponseBody
	public ResponseEntity<byte[]> getStageIcon(@PathVariable("lib") String lib, @PathVariable("id") String id,
			HttpServletRequest request) {
		return ResponseEntity.ok().body(dataflowService.getyHttpBinary(lib, id, request, "", utils.getUserId()));
	}

	// To allow uploads of stages extra libraries
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping(value = { "/app/rest/v1/stageLibraries/extras/{lib}/upload" })
	@ResponseBody
	public ResponseEntity<String> uploadBinaryWithInstance(@PathVariable("lib") String lib, HttpServletRequest request,
			@RequestParam("file") @NotNull MultipartFile file) {
		return dataflowService.sendHttpFile(request, file, utils.getUserId());
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = { "/{instance}/app/rest/v1/stageLibraries/extras/{lib}/upload" })
	@ResponseBody
	public ResponseEntity<String> uploadBinaryWithInstance(@PathVariable("instance") String instance,
			@PathVariable("lib") String lib, HttpServletRequest request,
			@RequestParam("file") @NotNull MultipartFile file) {
		return dataflowService.sendHttpFileWithInstance(request, file, instance);
	}

	/* EXCEPTION HANDLERS */

	@ExceptionHandler(ResourceAccessException.class)
	@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
	@ResponseBody
	public String handleOPException(final ResourceAccessException exception) {
		return "Could not access the resource. Response: " + exception.getMessage();
	}

	@ExceptionHandler({ IllegalArgumentException.class, RestClientException.class, DataAccessException.class,
			BadRequestException.class })
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleOPException(final RuntimeException exception) {
		return exception.getMessage();
	}

	@ExceptionHandler(ClientErrorException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleOPException(final ClientErrorException exception) {
		return "Status: " + exception.getResponse().getStatus() + " Response: " + exception.getMessage();
	}

	@ExceptionHandler(NotAuthorizedException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public String handleOPException(final NotAuthorizedException exception) {
		return exception.getMessage();
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ResponseBody
	public String handleOPException(final NotFoundException exception) {
		return exception.getMessage();
	}

	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {
		final Pipeline pipeline = new Pipeline();
		String instanceIdentification = "";
		try {
			instanceIdentification = dataflowService.getDataflowInstanceForUserId(utils.getUserId())
					.getIdentification();

		} catch (final Exception e) {
		}
		model.addAttribute("instance", instanceIdentification);
		model.addAttribute("pipeline", pipeline);
		model.addAttribute(DATAFLOW_VERSION_STR, dataflowService.getVersion());
		return "dataflow/create";

	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

}
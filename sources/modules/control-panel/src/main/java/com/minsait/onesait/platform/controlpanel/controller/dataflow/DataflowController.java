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
package com.minsait.onesait.platform.controlpanel.controller.dataflow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.PipelineUserAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessTypeRepository;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowServiceImpl;
import com.minsait.onesait.platform.config.services.user.UserService;
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
	private PipelineUserAccessTypeRepository pipelineUserAccessTypeRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	ServletContext context;

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PutMapping(value = "/app/rest/v1/pipeline/{name}")
	@ResponseBody
	public ResponseEntity<String> createPipeline(@PathVariable("name") String name,
			@RequestParam("autoGeneratePipelineId") boolean autoGeneratePipelineId,
			@RequestParam("description") String description,
			@RequestParam("pipelineType") DataflowServiceImpl.PipelineTypes type) {
		try {
			final String idstreamsets = dataflowService.createPipeline(name, type, description, utils.getUserId())
					.getIdstreamsets();
			return new ResponseEntity<>(idstreamsets, HttpStatus.CREATED);
		} catch (final Exception e) {
			log.error("Cannot create pipeline: " + e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Transactional
	// @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@DeleteMapping(value = "/{id}", produces = "text/html")
	public String removePipeline(@PathVariable("id") String id, Model uiModel) {
		dataflowService.removePipeline(id, utils.getUserId());
		uiModel.asMap().clear();
		return "redirect:/dataflow/list";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel) {
		uiModel.addAttribute("lpl", dataflowService.getPipelines(utils.getUserId()));
		uiModel.addAttribute("user", utils.getUserId());
		uiModel.addAttribute("userRole", utils.getRole());
		uiModel.addAttribute(DATAFLOW_VERSION_STR, this.dataflowService.getVersion());
		return "dataflow/list";
	}

	@RequestMapping(value = { "/app/rest/pipeline/{id}/**" }, method = { RequestMethod.GET, RequestMethod.POST,
			RequestMethod.PUT, RequestMethod.DELETE }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> pipelineRestUserJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body, @PathVariable("id") String id)
			throws URISyntaxException, IOException {
		if (utils.isAdministrator() || dataflowService.hasUserViewPermission(id, utils.getUserId())) {
			return dataflowService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
		} else {
			return null;
		}

	}

	@RequestMapping(value = "/share/{id}", produces = "text/html")
	public String share(Model model, @PathVariable("id") String id) {
		final String user = utils.getUserId();
		final Pipeline pipeline = pipelineRepository.findByIdstreamsets(id);
		if (pipeline.getUser().toString().equals(user) || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			final List<User> users = userService.getAllActiveUsers();

			model.addAttribute("users", users);
			model.addAttribute("int", pipelineUserAccessRepository.findByPipeline(pipeline));
			model.addAttribute("pipelineid", pipeline.getIdstreamsets());

			return "dataflow/share";
		} else {
			return "error/403";
		}
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<PipelineUserAccess> createAuthorization(@RequestParam String accesstype,
			@RequestParam String dataflow, @RequestParam String user) {

		try {
			dataflowService.createUserAccess(dataflow, user, accesstype);
			final User userObject = userService.getUser(user);
			final Pipeline pipelineObject = pipelineRepository.findByIdstreamsets(dataflow);
			final PipelineUserAccessType pipelineUserAccessTypeObject = pipelineUserAccessTypeRepository
					.findById(accesstype);
			final PipelineUserAccess pipelineUserAccess = pipelineUserAccessRepository
					.findByPipelineAndUserAndAccess(pipelineObject, userObject, pipelineUserAccessTypeObject);
			return new ResponseEntity<>(pipelineUserAccess, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/auth/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		try {
			dataflowService.deleteUserAccess(id);
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = { "/app/rest/pipelines/**" }, method = { RequestMethod.GET, RequestMethod.POST,
			RequestMethod.PUT, RequestMethod.DELETE }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> pipelineRestAdminJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body) throws URISyntaxException, IOException {
		if (utils.isAdministrator()) {
			return dataflowService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
		} else {
			return null;
		}

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = { "/app/rest/v1/stageLibraries/extras/{lib}/upload" }, consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<String> adminUploadExternalLibrary(Model uiModel, HttpServletRequest request,
			@RequestPart("file") @Valid @NotNull MultipartFile file) throws URISyntaxException, IOException {
		return dataflowService.sendHttp(request, HttpMethod.POST, file, utils.getUserId());
	}

	// @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value = { "/app/rest/**" }, method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,
			RequestMethod.PUT }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> adminAppRestPutJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body) throws URISyntaxException, IOException {

		return dataflowService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body, utils.getUserId());
	}

	@GetMapping(value = { "/app/rest/v1/definitions/stages/{lib}/{id}/icon" })
	@ResponseBody
	public ResponseEntity<byte[]> analyAppRestBinary(Model uiModel, HttpServletRequest request)
			throws URISyntaxException, IOException {
		return dataflowService.sendHttpBinary(request, HttpMethod.GET, "", utils.getUserId());
	}

	// @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@RequestMapping(value = { "/app/collector/pipeline/{id}", "/app/collector/logs/{name}/{id}" })
	public String indexAppViewPipeline(@PathVariable("id") String id, Model uiModel, HttpServletRequest request) {
		if (utils.isAdministrator() || dataflowService.hasUserViewPermission(id, utils.getUserId())) {
			uiModel.addAttribute(DATAFLOW_VERSION_STR, this.dataflowService.getVersion());
			return "dataflow/index";
		} else {
			return "redirect:/403";
		}
	}

	// @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@RequestMapping(value = { "/app", "/app/collector/jvmMetrics", "/app/collector/logs",
			"/app/collector/configuration", "/app/collector/packageManager" })
	public String indexAppRedirectNoPath(Model uiModel, HttpServletRequest request) {
		if (utils.isAdministrator()) {
			uiModel.addAttribute(DATAFLOW_VERSION_STR, this.dataflowService.getVersion());
			return "dataflow/index";
		} else {
			return "redirect:/403";
		}
	}

	@PostMapping("/public")
	@ResponseBody
	public String changePublic(@RequestParam("id") String dataflowId) {
		if (dataflowService.hasUserEditPermission(dataflowId, utils.getUserId())) {
			dataflowService.changePublic(pipelineRepository.findByIdstreamsets(dataflowId));
			return "ok";
		} else {
			return "ko";
		}
	}
	
	@GetMapping(value = "/show/{id}")
	public String showDataFlow(Model Model, @PathVariable("id") String id) {
		Pipeline dataFlow = pipelineRepository.findById(id);
		if (dataFlow == null) {
			return "error/403";
		}
		String idStreamsets = dataFlow.getIdstreamsets();
		return "redirect:/dataflow/app/collector/pipeline/"+idStreamsets;
	}

}
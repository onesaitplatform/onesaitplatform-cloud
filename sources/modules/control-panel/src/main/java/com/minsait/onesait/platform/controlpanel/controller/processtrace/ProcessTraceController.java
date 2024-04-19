/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.processtrace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.business.services.audit.AuditService;
import com.minsait.onesait.platform.config.model.ProcessOperation;
import com.minsait.onesait.platform.config.model.ProcessTrace;
import com.minsait.onesait.platform.config.services.exceptions.ProcessTraceServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.processtrace.ProcessTraceService;
import com.minsait.onesait.platform.config.services.processtrace.dto.OperationStatus;
import com.minsait.onesait.platform.config.services.processtrace.dto.ProcessOperationDTO;
import com.minsait.onesait.platform.config.services.processtrace.dto.ProcessTraceCreateDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.helper.processtrace.ProcessTraceHelper;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.quartz.services.process.ProcessExecutorService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/process")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class ProcessTraceController {

	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";

	@Autowired
	private ProcessTraceService service;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private ProcessTraceHelper helper;
	@Autowired
	@Qualifier("processExecutionMap")
	private Map<String, LinkedHashSet<OperationStatus>> processExecutionMap;
	@Autowired
	private AuditService auditService;
	@Autowired
	private ProcessExecutorService processExecutorService;

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String identification,
			@RequestParam(required = false) String description) {
		model.addAttribute("processes",
				service.getProcessTraceByUser(userService.getUser(utils.getUserId()), identification, description));
		return "process/list";
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("process", new ProcessTraceCreateDTO());
		model.addAttribute("ontologies", ontologyService.getAllOntologiesForListWithProjectsAccess(utils.getUserId()));
		return "process/create";
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			ProcessTrace process = service.getById(id);

			if (process == null) {
				log.error("Process with id {} not found.", id);
				return ERROR_404;
			}
			if (!userService.getUser(utils.getUserId()).isAdmin()
					&& !process.getUser().getUserId().equals(utils.getUserId())) {
				log.error("The user {} has not permission to update the process {}", utils.getUserId(), id);
				return ERROR_403;
			}

			List<ProcessOperationDTO> operations = helper.convertToProcessOperation(process.getOperations());
			operations.sort((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()));

			model.addAttribute("ontologies",
					ontologyService.getAllOntologiesForListWithProjectsAccess(utils.getUserId()));
			model.addAttribute("process", helper.convertToProcessTraceDTO(process));
			model.addAttribute("operations", operations);
			return "process/create";
		} catch (final ProcessTraceServiceException | IOException e) {
			log.error("Error getting Process {}", id, e);
			utils.addRedirectException(e, redirect);
			return "redirect:/process/list";
		}
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		ProcessTrace process = service.getById(id);
		if (process == null) {
			log.error("Process with id {} not found.", id);
			return ERROR_404;
		}
		if (!userService.getUser(utils.getUserId()).isAdmin()
				&& !process.getUser().getUserId().equals(utils.getUserId())) {
			log.error("The user {} has not permission to show the process {}", utils.getUserId(), id);
			return ERROR_403;
		}

		try {
			String queryResult = auditService.getCustomQueryData("select * from Audit_" + process.getUser().getUserId()
					+ " where module='" + Module.CONTROLPANEL.name() + "' and operationType='"
					+ OperationType.PROCESS_EXECUTION.name() + "' and id='" + process.getId() + "' and version="
					+ process.getVersion() + " order by timeStamp desc", process.getUser().getUserId());

			log.debug("queryResult: {}", queryResult);
			model.addAttribute("historics", helper.populateExecutionHistorics(queryResult, process));
			model.addAttribute("process", helper.convertToProcessTraceDTO(process));

			List<ProcessOperation> ops = process.getOperations().stream().collect(Collectors.toList());
			if (process.getIsOrdered()) {
				ops.sort((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()));
				model.addAttribute("operations", helper.convertToProcessOperation(new TreeSet<ProcessOperation>(ops)));
			} else {
				model.addAttribute("operations", helper.convertToProcessOperation(process.getOperations()));
			}

		} catch (Exception e) {
			log.error("Error getting the Process {}", id, e);
			utils.addRedirectException(e, redirect);
			return "redirect:/process/list";
		}

		return "process/show";
	}

	@GetMapping("/getgraph")
	public @ResponseBody String getGraph(Model model,
			@RequestParam(value = "identification", required = true) String identification,
			HttpServletRequest request) {
		final Set<GraphProcessDTO> arrayLinks = new HashSet<>();
		ProcessTrace process = service.getByIdentification(identification);
		List<ProcessOperation> sortOperations = new ArrayList<ProcessOperation>(process.getOperations());
		if (process.getIsOrdered()) {

			sortOperations.sort((o1, o2) -> o1.getPosition().compareTo(o2.getPosition()));

			ProcessOperation previusOp = new ProcessOperation();
			for (ProcessOperation op : sortOperations) {
				if (op.getPosition() == 1) {
					arrayLinks
							.add(GraphProcessDTO.constructSingleNode(op.getId(), op.getOntologyId().getIdentification(),
									op.getType().name(), op.getType().name(), helper.buildProperties(op)));
				} else {
					arrayLinks.add(new GraphProcessDTO(previusOp.getId(), op.getId(),
							previusOp.getOntologyId().getIdentification(), op.getOntologyId().getIdentification(),
							previusOp.getType().name(), op.getType().name(), op.getType().name(),
							helper.buildProperties(op)));
				}
				previusOp = op;
			}

		} else {
			for (ProcessOperation op : sortOperations) {
				arrayLinks.add(GraphProcessDTO.constructSingleNode(op.getId(), op.getOntologyId().getIdentification(),
						op.getType().name(), op.getType().name(), helper.buildProperties(op)));

			}
		}

		return arrayLinks.toString();
	}

	@PostMapping(value = { "/create" })
	@Transactional
	public String create(Model model, @Valid ProcessTraceCreateDTO dto, RedirectAttributes redirect,
			HttpServletRequest httpServletRequest) {
		try {
			ProcessTrace process = service.getByIdentification(dto.getIdentification());
			if (process != null) {
				log.error("Process already exists with the identification {}", dto.getIdentification());
				utils.addRedirectMessage("Process already exists with this identification", redirect);
				return "redirect:/proces/create";
			}
			final String[] operations = httpServletRequest.getParameterValues("operations");
			ProcessTrace p = helper.convertToProcessTrace(dto, operations, new ProcessTrace());
			p.setVersion(1);
			p = service.createProcessTrace(p);

			if (dto.getIsActive()) {
				processExecutorService.scheduleProcess(p);
				processExecutionMap.put(p.getId(), new LinkedHashSet<OperationStatus>());
			}
		} catch (final ProcessTraceServiceException | IOException e) {
			log.error("Error creating ProcessTrace {}", dto.getIdentification(), e);
			utils.addRedirectException(e, redirect);
			return "redirect:/process/create";
		}
		return "redirect:/process/list";
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	@Transactional
	public String update(Model model, @PathVariable("id") String id, @Valid ProcessTraceCreateDTO dto,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest httpServletRequest) {

		if (bindingResult.hasErrors()) {
			log.debug("Some process properties missing");
			utils.addRedirectMessage("Error updating process", redirect);
			return "redirect:/process/update/" + id;
		}

		ProcessTrace process = service.getById(id);

		if (process == null) {
			log.error("Process with id {} not found.", id);
			return ERROR_404;
		}
		if (!userService.getUser(utils.getUserId()).isAdmin()
				&& !process.getUser().getUserId().equals(utils.getUserId())) {
			log.error("The user {} has not permission to update the process {}", utils.getUserId(), id);
			return ERROR_403;
		}

		try {
			final String[] operations = httpServletRequest.getParameterValues("operations");
			service.updateProcessTrace(helper.convertToProcessTrace(dto, operations, process));

			processExecutorService.unscheduleProcess(process);

			if (dto.getIsActive()) {
				processExecutorService.scheduleProcess(process);
				processExecutionMap.put(process.getId(), new LinkedHashSet<OperationStatus>());
			}
		} catch (ProcessTraceServiceException | IOException e) {
			log.error("Cannot update Process");
			utils.addRedirectMessage("Cannot update Process", redirect);
			return "redirect:/process/update/" + id;
		} catch (Exception e) {
			log.error("Error deprecating data for processId: {}", process.getId(), e);
			utils.addRedirectMessage("Cannot deprecated process audit data", redirect);
			return "redirect:/process/update/" + id;
		}
		return "redirect:/process/list";

	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		ProcessTrace process = service.getById(id);
		if (process != null) {
			try {
				if (!userService.getUser(utils.getUserId()).isAdmin()
						&& !process.getUser().getUserId().equals(utils.getUserId())) {
					log.error("The user {} has not permission to delete the process {}", utils.getUserId(), id);
					return ERROR_403;
				}
				processExecutorService.unscheduleProcess(process);
				service.deleteProcessTrace(process);
				processExecutionMap.remove(process.getId());
			} catch (final Exception e) {
				log.error("Error deleting process. ", e);
				return "redirect:/process/list";
			}
			return "redirect:/process/list";
		} else {
			log.error("Process with id {} not found.", id);
			return ERROR_404;
		}
	}

	@PostMapping(value = "/getOntologyFields")
	public ResponseEntity<Map<String, String>> getOntologyFields(@RequestParam String ontologyIdentification)
			throws IOException {

		try {
			return new ResponseEntity<>(ontologyService.getOntologyFields(ontologyIdentification, utils.getUserId()),
					HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

}

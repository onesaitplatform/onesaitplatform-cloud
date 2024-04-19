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
package com.minsait.onesait.platform.controlpanel.controller.ksql.relation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.ksql.KsqlExecutionException;
import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.exceptions.KsqlRelationServiceException;
import com.minsait.onesait.platform.config.services.ksql.flow.KsqlFlowService;
import com.minsait.onesait.platform.config.services.ksql.relation.KsqlRelationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ksql/relation")
@Slf4j
public class KsqlRelationController {

	@Autowired
	private KsqlRelationService ksqlRelationService;
	@Autowired
	private KsqlFlowService ksqlFlowService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private KsqlRelationUtil ksqlRelationUtil;
	@Autowired
	private OntologyService ontologyService;

	private static final String MSG_STR = "{\"msg\":\"";
	private static final String KSQL_RELATION_CREATION_ERROR = "ksql.relation.creation.error.server";
	private static final String KSQL_SYNTAX_ERROR = "KSQL Syntax error. Please check KSQL Statemet.";
	private static final String NEW_LINE = ". \\n";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.ksqlRelationService.getAllIdentifications();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = {
			"/create" }, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<String> create(@RequestBody KsqlResourceDTO ksqlResourceDTO) {

		// Check user permissions over FlowId
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(ksqlResourceDTO.getFlowId());
		if (ksqlFlow == null) {
			String error = utils.getMessage("ksql.relation.creation.error.flow.not.found", KSQL_SYNTAX_ERROR);
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.NOT_FOUND);
		}
		if (!userService.getUser(utils.getUserId()).isAdmin()
				&& !ksqlFlow.getUser().getUserId().equals(utils.getUserId())) {
			// No permissions for this operation
			String error = utils.getMessage("ksql.relation.creation.error.no.permissions",
					"Unathorized to do this operation.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.UNAUTHORIZED);
		}
		// Create Resource from DTO
		KsqlResource ksqlResource = null;
		try {
			ksqlResource = ksqlRelationUtil.convertFromDTO(ksqlResourceDTO, utils.getUserId());
		} catch (Exception e) {
			String error = utils.getMessage(KSQL_RELATION_CREATION_ERROR, KSQL_SYNTAX_ERROR);
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		}
		// Persist in CDB
		try {
			ksqlRelationService.createKsqlRelation(ksqlFlow, ksqlResource);
		} catch (KsqlRelationServiceException e) {
			// Duplicated Identification for resource
			String error = utils.getMessage("ksql.relation.creation.error.dup.relation",
					"Duplicated Resource in this KSQL Flow.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		} catch (KsqlExecutionException e) {
			String error = utils.getMessage(KSQL_RELATION_CREATION_ERROR,
					"KSQL Syntax error. Please check KSQL Statemet.  ");
			String exceptionMsg = e.getMessage() + '\n';
			error += NEW_LINE + e.getMessage().substring(0, exceptionMsg.indexOf('\n'));
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			String error = utils.getMessage(KSQL_RELATION_CREATION_ERROR, KSQL_SYNTAX_ERROR);
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		}
		// Prepare response
		String success = utils.getMessage("ksql.relation.creation.ok", "KSQL Resource successfully created.");
		return new ResponseEntity<>(MSG_STR + success + "\"}", HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public @ResponseBody ResponseEntity<String> delete(Model model, @PathVariable("id") String id,
			RedirectAttributes redirect) {
		KsqlRelation relation = this.ksqlRelationService.getKsqlRelationWithId(id);
		if (relation == null) {
			String error = utils.getMessage("ksql.relation.delete.error.not.found", "KSQL Resource not found.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.NOT_FOUND);
		}

		if (userService.getUser(utils.getUserId()).isAdmin()
				|| relation.getKsqlFlow().getUser().getUserId().equals(utils.getUserId())) {

			try {
				this.ksqlRelationService.deleteKsqlRelation(relation);
			} catch (KsqlExecutionException e) {
				String error = utils.getMessage("ksql.relation.deletion.error.server", KSQL_SYNTAX_ERROR);
				error += NEW_LINE + e.getMessage().replaceAll("\n", "\\n");
				return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
			}
		} else {
			log.debug("Admin cannot delete other users");
			String error = utils.getMessage("ksql.relation.deletion.error.no.permissions",
					"UNAUTHORIZED. Not enough permissions for this operation.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.UNAUTHORIZED);
		}
		String success = utils.getMessage("ksql.relation.deletion.ok", "KSQL Resource sucecssfully deleted.");
		return new ResponseEntity<>(MSG_STR + success + "\"}", HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String getRelations(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "flowId") String flowId) {
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(flowId);
		model.addAttribute("ksqlFlow", ksqlFlow);
		// Relations Info
		List<KsqlRelation> ksqlRelations = ksqlRelationService
				.getKsqlRelationsWithFlowId(userService.getUser(utils.getUserId()), flowId);
		model.addAttribute("ksqlRelations", ksqlRelations);
		// Available Ontologies Info
		List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
		model.addAttribute("ontologies", ontologies);

		return "redirect:/ksql/flow/update/" + flowId + "#ksqlRelations";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}")
	public @ResponseBody ResponseEntity<String> updateFlow(Model model, @PathVariable("id") String id,
			@RequestBody KsqlResourceDTO ksqlResourceDTO, BindingResult bindingResult, RedirectAttributes redirect) {

		// Check user permissions over FlowId
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(ksqlResourceDTO.getFlowId());
		if (ksqlFlow == null) {
			String error = utils.getMessage("ksql.relation.creation.error.flow.not.found", "KSQL Flow not found.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.NOT_FOUND);
		}
		if (!userService.getUser(utils.getUserId()).isAdmin()
				&& !ksqlFlow.getUser().getUserId().equals(utils.getUserId())) {
			// No permissions for this operation
			String error = utils.getMessage("ksql.relation.creation.error.no.permissions",
					"Unauthorized. You have no permissions for this operation.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.UNAUTHORIZED);
		}

		KsqlResource ksqlResource = ksqlRelationUtil.getOriginalFromDTO(ksqlResourceDTO);

		if (ksqlResource == null) {
			String error = utils.getMessage("ksql.relation.delete.error.not.found", "KSQL Resource not found.");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.NOT_FOUND);
		}
		try {
			ksqlRelationService.updateKsqlRelation(ksqlFlow, ksqlResource, ksqlResourceDTO.getStatement(),
					ksqlResource.getDescription());
		} catch (KsqlExecutionException e) {
			String error = utils.getMessage("ksql.relation.update.error.server", KSQL_SYNTAX_ERROR);
			error += NEW_LINE + e.getMessage().replaceAll("\n", "\\n");
			return new ResponseEntity<>(MSG_STR + error + "\"}", HttpStatus.BAD_REQUEST);
		}

		String success = utils.getMessage("ksql.relation.update.ok", "KSQL Resource successfully updated.");
		return new ResponseEntity<>(MSG_STR + success + "\"}", HttpStatus.OK);
	}
}

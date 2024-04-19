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
package com.minsait.onesait.platform.controlpanel.controller.ksql.flow;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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

import com.minsait.onesait.platform.commons.ksql.KsqlExecutionException;
import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.exceptions.KsqlFlowServiceException;
import com.minsait.onesait.platform.config.services.ksql.flow.KsqlFlowService;
import com.minsait.onesait.platform.config.services.ksql.relation.KsqlRelationService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.ksql.flow.dto.KsqlFlowDTO;
import com.minsait.onesait.platform.controlpanel.controller.ksql.flow.dto.KsqlRelationFIQL;
import com.minsait.onesait.platform.controlpanel.controller.ksql.flow.dto.OntologyJsonSchemaDto;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ksql/flow")
@Slf4j
public class KsqlFlowController {

	@Autowired
	private KsqlFlowService ksqlFlowService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private KsqlRelationService ksqlRelationService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private KsqlRelationFIQL ksqlRelationFIQL;

	private static final String KSQL_FLOW_VALIDATION_ERROR = "ksql.flow.validation.error";
	private static final String REDIRECT_KSQL_FLOW_LIST = "redirect:/ksql/flow/list";
	private static final String KSQL_FLOW = "ksqlFlow";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		List<KsqlFlow> ksqlFlows = this.ksqlFlowService.getKsqlFlowsWithDescriptionAndIdentification(
				userService.getUser(utils.getUserId()), identification, description);
		model.addAttribute("ksqlFlows", ksqlFlows);
		return "ksql/flow/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.ksqlFlowService.getAllIdentifications();
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {
		KsqlFlow ksqlFlow = new KsqlFlow();
		ksqlFlow.setJsonFlow("");
		ksqlFlow.setUser(userService.getUser(utils.getUserId()));
		model.addAttribute(KSQL_FLOW, ksqlFlow);
		return "ksql/flow/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping(value = { "/create" })
	public String create(Model model, @Valid KsqlFlowDTO ksqlFlowDTO, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some KSQL Flow properties missing");
			utils.addRedirectMessage(KSQL_FLOW_VALIDATION_ERROR, redirect);
			return "redirect:/ksql/flow/create";
		}
		try {
			KsqlFlow ksqlFlow = new KsqlFlow();
			ksqlFlow.setDescription(ksqlFlowDTO.getDescription());
			ksqlFlow.setJsonFlow(ksqlFlowDTO.getJsonFlow());
			ksqlFlow.setIdentification(ksqlFlowDTO.getIdentification());
			User user = userService.getUser(utils.getUserId());
			ksqlFlow.setUser(user);
			ksqlFlowService.createKsqlFlow(ksqlFlow);

		} catch (KsqlFlowServiceException e) {
			log.error("Cannot create KSQL Flow because of:" + e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/ksql/flow/create";
		}
		return REDIRECT_KSQL_FLOW_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/check/{ksqlflowIdentification}")
	public @ResponseBody boolean checkAvailableDomainIdentifier(
			@PathVariable(value = "ksqlflowIdentification") String identification) {

		return ksqlFlowService.identificationIsAvailable(userService.getUser(utils.getUserId()), identification);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{ksqlFlowId}", produces = "text/html")
	public String updateForm(Model model, @PathVariable(value = "ksqlFlowId") String id,
			@RequestParam(required = false, name = "relationIdentification") String relationIdentification,
			@RequestParam(required = false, name = "relationDescription") String relationDescription,
			RedirectAttributes redirect) {
		// Flow info
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(id);

		if (ksqlFlow.getUser().getUserId().equals(utils.getUserId())
				|| userService.getUser(utils.getUserId()).isAdmin()) {
			model.addAttribute(KSQL_FLOW, ksqlFlow);
			// Relations Info
			List<KsqlRelation> ksqlRelations = ksqlRelationService
					.getKsqlRelationsWithFlowIdDescriptionAndIdentification(userService.getUser(utils.getUserId()), id,
							relationIdentification, relationDescription);
			model.addAttribute("ksqlRelations", ksqlRelationFIQL.toKsqlRelationDTO(ksqlRelations));
			// Available Ontologies Info
			List<Ontology> ontologies = ontologyService.getOntologiesByUserId(ksqlFlow.getUser().getUserId());
			List<OntologyJsonSchemaDto> ontologiesDto = new ArrayList<>();
			for (Ontology ontol : ontologies) {
				OntologyJsonSchemaDto dto = new OntologyJsonSchemaDto();
				dto.setIdentification(ontol.getIdentification());
				dto.setJsonSchema(ontol.getJsonSchema());
				dto.setAllowsCreateTopic(ontol.isAllowsCreateTopic());
				ontologiesDto.add(dto);
			}
			model.addAttribute("ontologies", ontologiesDto);
			return "ksql/flow/create";
		} else {
			log.debug("User has not access");
			utils.addRedirectMessage(KSQL_FLOW_VALIDATION_ERROR, redirect);
			return REDIRECT_KSQL_FLOW_LIST;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		KsqlFlow flow = this.ksqlFlowService.getKsqlFlowWithId(id);
		if (flow.getUser().getUserId().equals(utils.getUserId())) {
			// Avoid Administrator deleting other users KsqlFlows
			try {
				this.ksqlFlowService.deleteKsqlFlow(id);
			} catch (KsqlExecutionException e) {
				log.error(e.getMessage());
			}
		} else {
			log.debug("Admin cannot delete other users");
			utils.addRedirectMessage(KSQL_FLOW_VALIDATION_ERROR, redirect);
			return REDIRECT_KSQL_FLOW_LIST;
		}
		return REDIRECT_KSQL_FLOW_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}")
	public String updateFlow(Model model, @PathVariable("id") String id, @Valid KsqlFlowDTO ksqlFlow,
			BindingResult bindingResult, RedirectAttributes redirect) {
		// Get Flow from CDB
		KsqlFlow cdbFlow = ksqlFlowService.getKsqlFlowWithId(ksqlFlow.getId());
		if (cdbFlow != null) {
			// Change Description
			cdbFlow.setDescription(ksqlFlow.getDescription());
			ksqlFlowService.updateKsqlFlow(id, cdbFlow, utils.getUserId());
		} else {
			log.debug("Unable to update flow. Identification = {}", ksqlFlow.getIdentification());
			utils.addRedirectMessage(KSQL_FLOW_VALIDATION_ERROR, redirect);
			return REDIRECT_KSQL_FLOW_LIST;
		}
		return REDIRECT_KSQL_FLOW_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/show/{ksqlFlowId}", produces = "text/html")
	public String show(Model model, @PathVariable(value = "ksqlFlowId") String id,
			@RequestParam(required = false, name = "relationIdentification") String relationIdentification,
			@RequestParam(required = false, name = "relationDescription") String relationDescription,
			RedirectAttributes redirect) {
		// Flow info
		KsqlFlow ksqlFlow = ksqlFlowService.getKsqlFlowWithId(id);
		if (ksqlFlow.getUser().getUserId().equals(utils.getUserId())
				|| userService.getUser(utils.getUserId()).isAdmin()) {
			model.addAttribute(KSQL_FLOW, ksqlFlow);
			// Relations Info
			List<KsqlRelation> ksqlRelations = ksqlRelationService
					.getKsqlRelationsWithFlowIdDescriptionAndIdentification(userService.getUser(utils.getUserId()), id,
							relationIdentification, relationDescription);
			model.addAttribute("ksqlRelations", ksqlRelationFIQL.toKsqlRelationDTO(ksqlRelations));
			// Available Ontologies Info
			List<Ontology> ontologies = ontologyService.getOntologiesByUserId(utils.getUserId());
			List<OntologyJsonSchemaDto> ontologiesDto = new ArrayList<>();
			for (Ontology ontol : ontologies) {
				OntologyJsonSchemaDto dto = new OntologyJsonSchemaDto();
				dto.setIdentification(ontol.getIdentification());
				dto.setJsonSchema(ontol.getJsonSchema());
				dto.setAllowsCreateTopic(ontol.isAllowsCreateTopic());
				ontologiesDto.add(dto);
			}
			model.addAttribute("ontologies", ontologiesDto);
			return "ksql/flow/show";
		} else {
			log.debug("User has not access");
			utils.addRedirectMessage(KSQL_FLOW_VALIDATION_ERROR, redirect);
			return REDIRECT_KSQL_FLOW_LIST;
		}
	}

}

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
package com.minsait.onesait.platform.controlpanel.rest.management.rule;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.hazelcast.topic.ITopic;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.services.drools.DroolsRuleService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.controlpanel.controller.rules.RuleDTO;
import com.minsait.onesait.platform.controlpanel.services.rules.BusinessRuleService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.annotations.ApiOperation;


import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/rules")
@Tag(name = "Rules")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@Slf4j
public class DomainRuleRestController {

	@Autowired
	@Qualifier("topicChangedDomains")
	private ITopic<String> topicDomains;

	@Autowired
	private DroolsRuleService droolsRuleService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private BusinessRuleService businessRuleService;

	@GetMapping
	@Operation(summary = "List rules")
	public ResponseEntity<List<RuleDTO>> listRules() {
		final List<DroolsRule> rules = droolsRuleService.getAllRules(utils.getUserId());
		return ResponseEntity.ok().body(rules.stream().map(RuleDTO::convert).collect(Collectors.toList()));
	}

	@GetMapping("/rule/{identification}")
	@Operation(summary = "Find rule")
	public ResponseEntity<RuleDTO> rule(@PathVariable("identification") String identification) {
		if (!droolsRuleService.hasUserEditPermission(identification, utils.getUserId()))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		final DroolsRule rule = droolsRuleService.getRule(identification);
		if (rule == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok().body(RuleDTO.convert(rule));
	}

	@PutMapping("/rule/{identification}")
	@Operation(summary = "Edit Rule")
	public ResponseEntity<String> editRule(@PathVariable("identification") String identification,
			@RequestBody RuleDTO rule) {
		if (!droolsRuleService.hasUserEditPermission(identification, utils.getUserId()))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		try {
			// escape UTF-8
			rule.setDrl(URLDecoder.decode(rule.getDrl(), "UTF-8"));
			businessRuleService.update(rule, utils.getUserId(), identification);
		} catch (final GenericOPException | UnsupportedEncodingException e) {
			log.error("Error in update Rule REST service ", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/rule")
	@Operation(summary = "Create rule")
	public ResponseEntity<String> createRule(@RequestBody RuleDTO rule) {
		try {
			if (!rule.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			    return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'", HttpStatus.BAD_REQUEST);
			}
			final DroolsRuleDomain domain = droolsRuleService.getUserDomain(utils.getUserId());
			if (domain == null)
				businessRuleService.createDomain(utils.getUserId());
			// escape UTF-8
			rule.setDrl(URLDecoder.decode(rule.getDrl(), "UTF-8"));
			businessRuleService.save(rule, utils.getUserId());
		} catch (final GenericOPException | UnsupportedEncodingException e) {
			log.error("Error in create Rule REST service ", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping("domains/self/start")
	@Operation(summary = "Starts the domain of the current user")
	public ResponseEntity<String> startDomain() {

		businessRuleService.changeDomainState(utils.getUserId(), true);
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@PostMapping("domains/self/stop")
	@Operation(summary = "Stops the domain of the current user")
	public ResponseEntity<String> stopDomain() {
		businessRuleService.changeDomainState(utils.getUserId(), false);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("domains/stop")
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@Operation(summary = "Stops the domain of the current user")
	public ResponseEntity<String> stopDomains() {
		businessRuleService.changeDomainStates(false);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("domains/start")
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@Operation(summary = "Stops the domain of the current user")
	public ResponseEntity<String> startDomains() {
		businessRuleService.changeDomainStates(true);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PatchMapping("rule/{identification}/drl")
	@Operation(summary = "Changes the DRL of a rule")
	public ResponseEntity<String> updateRuleDRL(@PathVariable("identification") String identification,
			@RequestBody String newDRL) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.updateDRL(identification, newDRL);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not update DRL " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PutMapping("rule/{identification}/active/{active}")
	@Operation(summary = "Makes a rule either active or inactive")
	public ResponseEntity<String> updateRule(@PathVariable("identification") String identification,
			@PathVariable("active") Boolean active) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.updateActive(identification, active);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not update Rule", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@DeleteMapping("rule/{identification}")
	@Operation(summary = "Deletes a rule")
	public ResponseEntity<String> deleteRule(@PathVariable("identification") String identification) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.delete(identification);
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not delete Rule", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("rule/{identification}/test")
	@Operation(summary = "Test a rule by providing a valid JSON input")
	public ResponseEntity<String> test(@PathVariable("identification") String identification,
			@RequestBody String input) {
		try {
			final String response = businessRuleService.test(identification, input);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}

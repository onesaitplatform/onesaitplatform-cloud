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
package com.minsait.onesait.platform.controlpanel.controller.simulation;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.SimulationServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.DataSchemaValidationException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.simulation.DeviceSimulationService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.quartz.services.simulation.SimulationService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("devicesimulation")
@Slf4j
public class DeviceSimulatorController {

	@Autowired
	private DeviceSimulationService deviceSimulationService;
	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private SimulationService simulationService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired 
	private HttpSession httpSession;
	
	private static final String SIMULATORS_STR = "simulators";
	private static final String ERROR_403 = "error/403";
	private static final String APP_ID = "appId";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("list")
	public String list(Model model) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		model.addAttribute(SIMULATORS_STR, data());
		return "simulator/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("data")
	public @ResponseBody List<DeviceSimulationDTO> data() {
		List<ClientPlatformInstanceSimulation> simulations = null;
		if (utils.isAdministrator()) {
			simulations = deviceSimulationService.getAllSimulations();
		} else {
			simulations = deviceSimulationService.getSimulationsForUser(utils.getUserId());
		}

		return simulations.stream()
				.map(s -> DeviceSimulationDTO.builder().active(s.isActive())
						.device(s.getClientPlatform().getIdentification()).ontology(s.getOntology().getIdentification())
						.name(s.getIdentification()).id(s.getId()).build())
				.collect(Collectors.toList());

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("create")
	public String createForm(Model model) {
		final List<String> clients = deviceSimulationService.getClientsForUser(utils.getUserId()).stream()
				.filter(c -> !clientPlatformService.getOntologiesByClientPlatform(c).isEmpty())
				.collect(Collectors.toList());
		final List<String> simulators = deviceSimulationService.getSimulatorTypes();
		model.addAttribute("platformClients", clients);
		model.addAttribute(SIMULATORS_STR, simulators);
		model.addAttribute("simulation", new ClientPlatformInstanceSimulation());
		return "simulator/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("update/{id}")
	public String updateForm(Model model, @PathVariable("id") String id) {

		final ClientPlatformInstanceSimulation simulation = deviceSimulationService.getSimulationById(id);

		if (!utils.isAdministrator() && !simulation.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		final List<String> clients = deviceSimulationService.getClientsForUser(utils.getUserId());
		final List<String> simulators = deviceSimulationService.getSimulatorTypes();

		model.addAttribute("platformClient", simulation.getClientPlatform());
		model.addAttribute("ontology", simulation.getOntology());
		model.addAttribute("token", simulation.getToken());
		model.addAttribute("platformClients", clients);
		model.addAttribute(SIMULATORS_STR, simulators);
		model.addAttribute("simulation", simulation);
		model.addAttribute("ontologies", deviceSimulationService
				.getClientOntologiesIdentification(simulation.getClientPlatform().getIdentification()));
		model.addAttribute("tokens", deviceSimulationService
				.getClientTokensIdentification(simulation.getClientPlatform().getIdentification()));
		model.addAttribute(ResourcesInUseService.RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
		resourcesInUseService.put(id, utils.getUserId());

		return "simulator/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping("create")
	public String create(Model model, RedirectAttributes redirect, @RequestParam String identification,
			@RequestParam String jsonMap, @RequestParam String ontology, @RequestParam String clientPlatform,
			@RequestParam String token, @RequestParam int interval, @RequestParam String jsonInstances,
			@RequestParam String instancesMode) throws IOException {

		try {
			simulationService.createSimulation(identification, interval, utils.getUserId(),
					simulationService.getDeviceSimulationJson(identification, clientPlatform, token, ontology, jsonMap,
							jsonInstances, instancesMode));
		} catch (final SimulationServiceException e) {
			log.debug("Cannot create simulation");
			utils.addRedirectException(e, redirect);
			return "redirect:/devicesimulation/create";
		}

		return "redirect:/devicesimulation/list";
	}

	@PostMapping("ontologiesandtokens")
	public String getOntologiesAndTokens(Model model, @RequestParam String clientPlatformId) {

		final ClientPlatform clientPlatform = clientPlatformService.getByIdentification(clientPlatformId);

		if (!clientPlatformService.hasUserManageAccess(clientPlatform.getId(), utils.getUserId())) {
			return ERROR_403;
		}

		model.addAttribute("ontologies", deviceSimulationService.getClientOntologiesIdentification(clientPlatformId));
		model.addAttribute("tokens", deviceSimulationService.getClientTokensIdentification(clientPlatformId));

		return "simulator/create :: ontologiesAndTokens";
	}

	@PostMapping("ontologyfields")
	public String getOntologyfields(Model model, @RequestParam String ontologyIdentification) throws IOException {

		model.addAttribute("fields", ontologyService.getOntologyFields(ontologyIdentification, utils.getUserId()));
		model.addAttribute(SIMULATORS_STR, deviceSimulationService.getSimulatorTypes());
		return "simulator/create :: ontologyFields";
	}

	@PostMapping("startstop")
	public String startStop(Model model, @RequestParam String id) {
		final ClientPlatformInstanceSimulation simulation = deviceSimulationService.getSimulationById(id);

		if (!utils.isAdministrator() && !simulation.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		List<ClientPlatformInstanceSimulation> simulations = null;
		if (simulation != null) {
			if (simulation.isActive()) {
				simulationService.unscheduleSimulation(simulation);
			} else {
				simulationService.scheduleSimulation(simulation);
			}
		}
		if (utils.isAdministrator()) {
			simulations = deviceSimulationService.getAllSimulations();
		} else {
			simulations = deviceSimulationService.getSimulationsForUser(utils.getUserId());
		}
		model.addAttribute("simulations", simulations);
		return "simulator/list :: simulations";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PutMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id, @RequestParam String identification,
			@RequestParam String jsonMap, @RequestParam String ontology, @RequestParam String clientPlatform,
			@RequestParam String token, @RequestParam int interval, @RequestParam String jsonInstances,
			@RequestParam String instancesMode, RedirectAttributes redirect) throws IOException {

		final ClientPlatformInstanceSimulation simulation = deviceSimulationService.getSimulationById(id);

		if (!utils.isAdministrator() && !simulation.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		if (simulation != null) {
			if (!simulation.isActive()) {
				simulationService.updateSimulation(identification, interval,

						simulationService.getDeviceSimulationJson(identification, clientPlatform, token, ontology,
								jsonMap, jsonInstances, instancesMode),

						simulation);
				resourcesInUseService.removeByUser(id, utils.getUserId());
				return "redirect:/devicesimulation/list";
			} else {
				utils.addRedirectMessage("simulation.update.isactive", redirect);
				return "redirect:/devicesimulation/update/" + id;
			}

		} else {
			utils.addRedirectMessage("simulation.update.error", redirect);
			return "redirect:/devicesimulation/update/" + id;
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@DeleteMapping("{id}")
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		final ClientPlatformInstanceSimulation simulation = deviceSimulationService.getSimulationById(id);

		if (!utils.isAdministrator() && !simulation.getUser().getUserId().equals(utils.getUserId())) {
			return ERROR_403;
		}

		if (simulation != null) {
			try {
				entityDeletionService.deleteDeviceSimulation(simulation);
			} catch (final Exception e) {
				utils.addRedirectException(e, redirect);
				return "error";
			}
			return "ok";
		} else {
			utils.addRedirectMessage("simulation.exists.false", redirect);
			return "error";
		}
	}

	@PostMapping("checkjson")
	public @ResponseBody String checkJson(Model model, @RequestParam("json") String json,
			@RequestParam("ontology") String ontology) {
		final ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			node = mapper.readTree(json);
			if (node.isArray()) {
				node.forEach(n -> {
					ontologyDataService.checkOntologySchemaCompliance(n,
							ontologyService.getOntologyByIdentification(ontology, utils.getUserId()));
				});
			} else {

				ontologyDataService.checkOntologySchemaCompliance(node,
						ontologyService.getOntologyByIdentification(ontology, utils.getUserId()));
			}
		} catch (final IOException e) {
			return "Invalid json";
		} catch (final DataSchemaValidationException e) {
			return e.getMessage();
		}
		return "ok";

	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free dashboard resource ", id);
	}

}

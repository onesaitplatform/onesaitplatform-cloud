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
package com.minsait.onesait.platform.controlpanel.controller.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.commons.kafka.KafkaExectionException;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessTypeRepository;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.client.dto.DeviceDTO;
import com.minsait.onesait.platform.config.services.client.dto.GenerateTokensResponse;
import com.minsait.onesait.platform.config.services.client.dto.TokenActivationRequest;
import com.minsait.onesait.platform.config.services.client.dto.TokenActivationResponse;
import com.minsait.onesait.platform.config.services.client.dto.TokenSelectedRequest;
import com.minsait.onesait.platform.config.services.client.dto.TokensRequest;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.device.dto.TokenDTO;
import com.minsait.onesait.platform.config.services.exceptions.ClientPlatformServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;
import com.minsait.onesait.platform.quartz.services.simulation.SimulationService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/devices")
@Slf4j
public class ClientPlatformController {

	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyUserAccessTypeRepository ontologyUserAccessTypeRepository;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private SimulationService simulationService;

	private static final String LOG_ONTOLOGY_PREFIX = "LOG_";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String ACCESS_LEVEL_STR = "accessLevel";
	private static final String DEVICE_STR = "device";
	private static final String REDIRECT_DEV_CREATE = "redirect:/devices/create";
	private static final String REDIRECT_DEV_LIST = "redirect:/devices/list";
	private static final String REDIRECT_UPDATE = "redirect:/devices/update/";
	private static final String ERROR_403 = "/error/403";
	private static final String TOKEN_STR = "token";
	private static final String ACTIVE_STR = "active";

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String identification,
			@RequestParam(required = false) String[] ontologies) {

		if (identification != null && identification.equals("")) {
			identification = null;
		}

		if (ontologies != null && ontologies.length == 0) {
			ontologies = null;
		}
		populateClientList(model,
				clientPlatformService.getAllClientPlatformByCriteria(utils.getUserId(), identification, ontologies));

		return "devices/list";

	}

	private void populateClientList(Model model, List<ClientPlatform> clients) {

		final List<DeviceDTO> devicesDTO = new ArrayList<DeviceDTO>();

		if (clients != null && !clients.isEmpty()) {
			for (final ClientPlatform client : clients) {
				final DeviceDTO deviceDTO = new DeviceDTO();
				deviceDTO.setUser(client.getUser().getUserId());
				deviceDTO.setDateCreated(client.getCreatedAt());
				deviceDTO.setDateUpdated(client.getUpdatedAt());
				deviceDTO.setDescription(client.getDescription());
				deviceDTO.setId(client.getId());
				deviceDTO.setIdentification(client.getIdentification());

				if (client.getClientPlatformOntologies() != null && !client.getClientPlatformOntologies().isEmpty()) {
					final List<String> list = new ArrayList<>();

					for (final ClientPlatformOntology cpo : client.getClientPlatformOntologies()) {
						list.add(cpo.getOntology().getIdentification());
					}
					deviceDTO.setOntologies(StringUtils.arrayToDelimitedString(list.toArray(), ", "));
				}
				devicesDTO.add(deviceDTO);
			}
		}

		model.addAttribute("devices", devicesDTO);
		model.addAttribute(ONTOLOGIES_STR,
				ontologyService.getOntologiesWithDescriptionAndIdentification(utils.getUserId(), null, null));
		model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		try {
			final ClientPlatform device = clientPlatformService.getById(id);
			if (!clientPlatformService.hasUserManageAccess(id, utils.getUserId())) {
				return new ResponseEntity<>(utils.getMessage("device.delete.error.forbidden", "forbidden"),
						HttpStatus.FORBIDDEN);
			}
			if (resourceService.isResourceSharedInAnyProject(device)) {
				return new ResponseEntity<>(
						"This digital client is shared within a Project, revoke access from project prior to deleting",
						HttpStatus.PRECONDITION_FAILED);
			}
			removeTable(device.getIdentification());
			entityDeletionService.deleteClient(id);
		} catch (final Exception e) {
			return new ResponseEntity<>(utils.getMessage("device.delete.error", "error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("/controlpanel/devices/list", HttpStatus.OK);
	}

	private void removeTable(String identification) {
		try {
			manageDBPersistenceServiceFacade.removeTable4Ontology(LOG_ONTOLOGY_PREFIX + identification);
		} catch (final Exception e) {
			log.debug("Sth went wrong while removing table 4 ontology", e);
		}
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/create")
	public String create(Model model) {
		final DeviceCreateDTO deviceDTO = new DeviceCreateDTO();

		createInitalTokenToJson(deviceDTO);
		model.addAttribute(DEVICE_STR, deviceDTO);
		final List<OntologyDTO> ontologies = ontologyService
				.getAllOntologiesForListWithProjectsAccess(utils.getUserId());
		model.addAttribute(ONTOLOGIES_STR, ontologies);

		return "devices/create";
	}

	@PostMapping(value = "/getAccess")
	public ResponseEntity<List<String>> getAccess(@RequestParam("ontology") String ontology) {

		final String userStr = utils.getUserId();
		final Ontology ontologySelected = ontologyService.getOntologyByIdentification(ontology, userStr);

		if (utils.getRole().equals("ROLE_ADMINISTRATOR") || userStr.equals(ontologySelected.getUser().getUserId())) {

			return new ResponseEntity<>(clientPlatformService.getClientPlatformOntologyAccessLevel().stream()
					.map(Enum::name).collect(Collectors.toList()), HttpStatus.OK);

		} else if (ontologyUserAccessRepository.findByOntologyAndUser(ontologySelected,
				userService.getUser(userStr)) != null) {

			final List<String> accessList = new ArrayList<>();
			final List<OntologyUserAccessType> access = new ArrayList<>();
			access.add(ontologyUserAccessRepository
					.findByOntologyAndUser(ontologySelected, userService.getUser(userStr)).getOntologyUserAccessType());

			for (final OntologyUserAccessType accessName : access) {
				accessList.add(accessName.getName());
			}

			return new ResponseEntity<>(accessList, HttpStatus.OK);
		}

		final List<String> accessList = new ArrayList<>();
		for (final OntologyUserAccessType accessName : ontologyUserAccessTypeRepository.findByName("QUERY")) {
			accessList.add(accessName.getName());
		}
		return new ResponseEntity<>(accessList, HttpStatus.OK);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = { "/create" })
	public String createDevice(Model model, @Valid DeviceCreateDTO device, BindingResult bindingResult,
			RedirectAttributes redirect) {

		try {
			final String userId = utils.getUserId();
			final ClientPlatform ndevice = clientPlatformService.createClientPlatform(device, userId, false);
			final Ontology onto = clientPlatformService.createDeviceLogOntology(ndevice);
			ontologyBusinessService.createOntology(onto, userId, null);
			clientPlatformService.createOntologyRelation(onto, ndevice);

		} catch (final ClientPlatformServiceException | JSONException e) {
			log.error("Cannot create clientPlatform", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final OntologyBusinessServiceException e) {
			log.error("Error creating ontology", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final Exception e) {
			log.error("Error creating device", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT_DEV_CREATE;
		}
		return REDIRECT_DEV_LIST;
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		final ClientPlatform device = clientPlatformService.getById(id);

		if (device != null) {
			if (!clientPlatformService.hasUserManageAccess(id, utils.getUserId())) {
				return ERROR_403;
			}
			final DeviceCreateDTO deviceDTO = new DeviceCreateDTO();
			deviceDTO.setId(device.getId());
			deviceDTO.setDescription(device.getDescription());
			deviceDTO.setIdentification(device.getIdentification());
			deviceDTO.setMetadata(device.getMetadata());
			deviceDTO.setUserId(device.getUser().getUserId());
			mapOntologiesToJson(model, device, deviceDTO);
			mapTokensToJson(device, deviceDTO);
			model.addAttribute(DEVICE_STR, deviceDTO);
			model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());

			model.addAttribute(ResourcesInUseService.RESOURCEINUSE,
					resourcesInUseService.isInUse(id, utils.getUserId()));
			resourcesInUseService.put(id, utils.getUserId());
			if (utils.isAdministrator()) {
				model.addAttribute("tenants", multitenancyService.getTenantsForCurrentVertical());
			} else {
				model.addAttribute("tenants", Collections.singletonList(
						multitenancyService.getTenant(MultitenancyContextHolder.getTenantName()).orElse(new Tenant())));
			}

			return "devices/create";
		} else {
			return REDIRECT_DEV_LIST;
		}
	}

	private void mapOntologiesToJson(Model model, ClientPlatform device, DeviceCreateDTO deviceDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		final List<OntologyDTO> ontologies = ontologyService
				.getAllOntologiesForListWithProjectsAccess(utils.getUserId());
		for (final ClientPlatformOntology cpo : device.getClientPlatformOntologies()) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("id", cpo.getOntology().getIdentification());
			on.put("access", cpo.getAccess().name());

			for (final Iterator<OntologyDTO> iterator = ontologies.iterator(); iterator.hasNext();) {
				final OntologyDTO ontology = iterator.next();
				if (ontology.getIdentification().equals(cpo.getOntology().getIdentification())) {
					iterator.remove();
					break;
				}
			}
			arrayNode.add(on);
		}

		try {
			deviceDTO.setClientPlatformOntologies(mapper.writer().writeValueAsString(arrayNode));
			model.addAttribute(ONTOLOGIES_STR, ontologies);
		} catch (final JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

	private void mapTokensToJson(ClientPlatform device, DeviceCreateDTO deviceDTO) {

		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		for (final Token token : device.getTokens()) {
			final ObjectNode on = mapper.createObjectNode();
			on.put("id", token.getId());
			on.put(TOKEN_STR, token.getTokenName());
			on.put(ACTIVE_STR, token.isActive());
			on.put("tenant", multitenancyService.getMasterDeviceToken(token.getTokenName()).getTenant());
			arrayNode.add(on);
		}

		try {
			deviceDTO.setTokens(mapper.writer().writeValueAsString(arrayNode));
		} catch (final JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

	private void createInitalTokenToJson(DeviceCreateDTO deviceDTO) {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();

		final ObjectNode on = mapper.createObjectNode();
		on.put("id", "");
		on.put(TOKEN_STR, UUID.randomUUID().toString().replaceAll("-", ""));
		on.put(ACTIVE_STR, true);
		arrayNode.add(on);

		try {
			deviceDTO.setTokens(mapper.writer().writeValueAsString(arrayNode));
		} catch (final JsonProcessingException e) {
			log.error(e.getMessage());
		}
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDevice(Model model, @PathVariable("id") String id, @Valid DeviceCreateDTO uDevice,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some device properties missing");
			utils.addRedirectMessage("device.validation.error", redirect);
			return REDIRECT_UPDATE + id;
		}

		if (!clientPlatformService.hasUserManageAccess(id, utils.getUserId())) {
			return ERROR_403;
		}
		try {
			clientPlatformService.updateDevice(uDevice, utils.getUserId());
		} catch (final ClientPlatformServiceException | JSONException e) {
			log.debug("Cannot update device");
			utils.addRedirectMessage("device.update.error", redirect);
			return REDIRECT_DEV_CREATE;
		} catch (final KafkaExectionException e) {
			log.debug("Cannot update Kafka topics ACL.");
			utils.addRedirectMessage("device.update.error", redirect);
			return REDIRECT_UPDATE + id;
		}
		resourcesInUseService.removeByUser(id, utils.getUserId());
		return REDIRECT_DEV_LIST;
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final ClientPlatform device = clientPlatformService.getById(id);

		if (device != null) {
			if (!clientPlatformService.hasUserViewAccess(id, utils.getUserId())) {
				return ERROR_403;
			}
			final DeviceCreateDTO deviceDTO = new DeviceCreateDTO();
			deviceDTO.setId(device.getId());
			deviceDTO.setDescription(device.getDescription());
			deviceDTO.setIdentification(device.getIdentification());
			deviceDTO.setMetadata(device.getMetadata());
			deviceDTO.setUserId(device.getUser().getUserId());
			mapOntologiesToJson(model, device, deviceDTO);
			mapTokensToJson(device, deviceDTO);
			model.addAttribute(DEVICE_STR, deviceDTO);
			model.addAttribute(ACCESS_LEVEL_STR, clientPlatformService.getClientPlatformOntologyAccessLevel());
			return "devices/show";
		} else {
			return REDIRECT_DEV_LIST;
		}

	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/desactivateToken")
	public @ResponseBody TokenActivationResponse desactivateToken(@RequestBody TokenActivationRequest request) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setRequestedActive(request.isActive());
		response.setToken(request.getToken());
		try {
			final Token token = tokenService.getTokenByID(request.getToken());
			tokenService.deactivateToken(token, request.isActive());
			response.setOk(true);
		} catch (final Exception e) {
			response.setOk(false);
		}
		return response;
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/deleteToken")
	public @ResponseBody TokenActivationResponse deleteToken(@RequestBody TokenSelectedRequest request) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setToken(request.getToken());
		try {
			final Token token = tokenService.getTokenByID(request.getToken());
			if (!clientPlatformService.hasUserManageAccess(token.getClientPlatform().getId(), utils.getUserId())) {
				response.setOk(false);
			} else {
				final List<ClientPlatformInstanceSimulation> simulations = tokenService.getSimulations(token);
				simulations.forEach(simulationService::unscheduleSimulation);
				entityDeletionService.deleteToken(token);
				response.setOk(true);
			}
		} catch (final Exception e) {
			response.setOk(false);
		}
		return response;
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/generateToken")
	public @ResponseBody GenerateTokensResponse generateTokens(@RequestBody TokensRequest request,
			@RequestParam(value = "tenant", required = false) String tenant) {
		if (!StringUtils.isEmpty(tenant)) {
			multitenancyService.getTenant(tenant).ifPresent(t -> MultitenancyContextHolder.setTenantName(t.getName()));
		}

		final GenerateTokensResponse response = new GenerateTokensResponse();
		final boolean check = request == null || request.getDeviceIdentification() == null
				|| request.getDeviceIdentification().equals("");
		response.setOk(check);
		if (!check && tokenService.generateTokenForClient(
				clientPlatformService.getByIdentification(request.getDeviceIdentification())) != null) {
			response.setOk(true);
		}
		return response;
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@PostMapping(value = "/loadDeviceTokens")
	public ResponseEntity<List<TokenDTO>> loadDeviceTokens(@RequestBody TokensRequest request) {

		final ClientPlatform clientPlatform = clientPlatformService
				.getByIdentification(request.getDeviceIdentification());
		if (!clientPlatformService.hasUserManageAccess(clientPlatform.getId(), utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			final List<Token> tokens = tokenService.getTokens(clientPlatform);
			if (tokens != null && !tokens.isEmpty()) {
				return ResponseEntity.ok().body(tokens.stream()
						.map(t -> TokenDTO.builder().id(t.getId()).token(t.getTokenName()).active(t.isActive())
								.tenant(multitenancyService.getMasterDeviceToken(t.getTokenName()).getTenant()).build())
						.collect(Collectors.toList()));

			}
			return ResponseEntity.ok().body(new ArrayList<>());
		}

	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free dashboard resource ", id);
	}

}

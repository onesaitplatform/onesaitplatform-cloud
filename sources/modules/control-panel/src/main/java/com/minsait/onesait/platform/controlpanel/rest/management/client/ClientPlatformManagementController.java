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
package com.minsait.onesait.platform.controlpanel.rest.management.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.client.dto.DeviceCreateDTO;
import com.minsait.onesait.platform.config.services.client.dto.GenerateTokensResponse;
import com.minsait.onesait.platform.config.services.client.dto.TokenActivationResponse;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.device.dto.ClientPlatformDTO;
import com.minsait.onesait.platform.config.services.device.dto.TokenDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.client.ClientPlatformController;
import com.minsait.onesait.platform.controlpanel.rest.management.client.model.ClientPlatformCreate;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("api/clientplatform")
@CrossOrigin(origins = "*")
@Tag(name = "Client Platform Management")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
public class ClientPlatformManagementController {

	private static final String NOT_VALID_STR = "NOT_VALID";

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	
	
	private static final String LOG_ONTOLOGY_PREFIX = "LOG_";
	
	

	@Operation(summary="Get all clientplatforms")
	@GetMapping
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ClientPlatformDTO[].class))))
	public ResponseEntity<Object> getAllClientplatforms() {

		List<ClientPlatform> list;

		if (utils.isAdministrator()) {
			list = clientPlatformService.getAllClientPlatforms();
		} else {
			final User user = userService.getUserByIdentification(utils.getUserId());
			list = clientPlatformService.getclientPlatformsByUser(user);
		}

		final List<ClientPlatformDTO> returnlist = new ArrayList<>();
		for (final ClientPlatform clientPlatform : list) {
			returnlist.add(clientPlatformService.parseClientPlatform(clientPlatform));
		}

		return ResponseEntity.ok(returnlist);
	}

	@Operation(summary="Get clientplatform by id")
	@GetMapping("/{identification}")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=ClientPlatformDTO.class))),
		@ApiResponse(responseCode = "404", description = "Not found") })
	public ResponseEntity<Object> getClientplatformByID(
			@Parameter(description= "identification  ", required = true) @PathVariable("identification") String identification) {
		final ClientPlatform clientPlatform = clientPlatformService.getByIdentification(identification);
		if (clientPlatform == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		final User user = userService.getUser(utils.getUserId());

		if (clientPlatform.getUser().getUserId().equals(utils.getUserId()) || userService.isUserAdministrator(user)) {
			return ResponseEntity.ok(clientPlatformService.parseClientPlatform(clientPlatform));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}

	@Operation(summary="Create new clientplatform")
	@PostMapping
	@ApiResponses(@ApiResponse(responseCode = "201", description = "Default token", content=@Content(schema=@Schema(implementation=String.class))))
	public ResponseEntity<?> createNewClientplatform(@Valid @RequestBody ClientPlatformCreate clientPlatformCreate,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		if (!clientPlatformCreate.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		// Get Ontologies
		final User user = userService.getUserByIdentification(utils.getUserId());
		final Map<Ontology, AccessType> ontologies = new HashMap<>();
		for (final Entry<String, AccessType> entry : clientPlatformCreate.getOntologies().entrySet()) {
			final Ontology ontology = ontologyService.getOntologyByIdentification(entry.getKey(), utils.getUserId());
			if (ontology == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Coulnd't found ontology");
			}

			boolean hasPermissions = false;

			switch (entry.getValue()) {
			case ALL:
				hasPermissions = ontologyService.hasUserPermisionForChangeOntology(user, ontology);
				break;
			case INSERT:
				hasPermissions = ontologyService.hasUserPermissionForInsert(user, ontology);
				break;
			case QUERY:
				hasPermissions = ontologyService.hasUserPermissionForQuery(user, ontology);
				break;
			default:
				break;
			}
			if (!hasPermissions) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
			}
			ontologies.put(ontology, entry.getValue());
		}

		// Create base Client Platform
		final ClientPlatform clientPlatform = new ClientPlatform();
		clientPlatform.setDescription(clientPlatformCreate.getDescription());
		clientPlatform.setMetadata(clientPlatformCreate.getMetadata());
		clientPlatform.setIdentification(clientPlatformCreate.getIdentification());
		clientPlatform.setUser(user);

		final Token token = clientPlatformService.createClientTokenWithAccessType(ontologies, clientPlatform);

		// Create Log Ontology
		try {
			final Ontology ontoLog = clientPlatformService.createDeviceLogOntology(clientPlatform);
			ontologyBusinessService.createOntology(ontoLog, utils.getUserId(), null);
			clientPlatformService.createOntologyRelation(ontoLog, clientPlatform);
		} catch (OntologyBusinessServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Coulnd't create log ontology for digital client");
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(token.getTokenName());
	}

	@Operation(summary="Update clientplatform")
	@PutMapping
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=String.class))))
	public ResponseEntity<?> updateClientplatform(@Valid @RequestBody ClientPlatformCreate clientPlatform,
			Errors errors) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			if (errors.hasErrors()) {
				return ErrorValidationResponse.generateValidationErrorResponse(errors);
			}

			final ClientPlatform ndevice = clientPlatformService
					.getByIdentification(clientPlatform.getIdentification());
			if (ndevice == null) {
				return new ResponseEntity<>("DigitalClient not found", HttpStatus.NOT_FOUND);
			}

			if (!clientPlatformService.hasUserManageAccess(ndevice.getId(), utils.getUserId())) {
				return new ResponseEntity<>("User has not permission to update the DigitalClient",
						HttpStatus.FORBIDDEN);
			}

			final User user = userService.getUserByIdentification(utils.getUserId());
			final ArrayNode array = mapper.createArrayNode();
			for (final Entry<String, AccessType> entry : clientPlatform.getOntologies().entrySet()) {
				final Ontology ontology = ontologyService.getOntologyByIdentification(entry.getKey(),
						utils.getUserId());
				if (ontology == null) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Coulnd't found ontology");
				}

				boolean hasPermissions = false;

				switch (entry.getValue()) {
				case ALL:
					hasPermissions = ontologyService.hasUserPermisionForChangeOntology(user, ontology);
					break;
				case INSERT:
					hasPermissions = ontologyService.hasUserPermissionForInsert(user, ontology);
					break;
				case QUERY:
					hasPermissions = ontologyService.hasUserPermissionForQuery(user, ontology);
					break;
				default:
					break;
				}
				if (!hasPermissions) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
				}
				final ObjectNode node = mapper.createObjectNode();
				node.put("id", entry.getKey());
				node.put("access", entry.getValue().name());
				array.add(node);
			}

			final List<Token> tokens = tokenService.getTokens(ndevice);

			final DeviceCreateDTO device = new DeviceCreateDTO();
			device.setIdentification(clientPlatform.getIdentification());
			device.setDescription(clientPlatform.getDescription());
			device.setId(ndevice.getId());
			device.setMetadata(clientPlatform.getMetadata());
			device.setTokens(tokens.toString());

			device.setClientPlatformOntologies(mapper.writeValueAsString(array));

			device.setUserId(ndevice.getUser().getUserId());

			clientPlatformService.updateDevice(device, utils.getUserId());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final JsonProcessingException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Coulnd't update Digital Client");
		}
	}

	@Operation(summary = "validate Clientplatform id with token")
	@GetMapping(value = "/validate/clientplatform/{identification}/token/{token}")
	public ResponseEntity<?> validate(
			@Parameter(description= "identification  ", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "Token", required = true) @PathVariable(name = "token") String token) {

		try {
			final ClientPlatform cp = clientPlatformService.getByIdentification(identification);

			final String clientPlatformId = cp.getId();

			final List<Token> tokens = clientPlatformService.getTokensByClientPlatformId(clientPlatformId);

			if (tokens == null || tokens.isEmpty()) {
				return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
			}

			final Token result = tokens.stream().filter(x -> token.equals(x.getTokenName()) && x.isActive()).findAny()
					.orElse(null);

			if (result == null) {
				return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
			} else {
				return new ResponseEntity<>("VALID", HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(NOT_VALID_STR, HttpStatus.OK);
		}

	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "List all device tokens")
	@GetMapping(value = "/{identification}/token")
	public ResponseEntity<List<TokenDTO>> loadClientplatformTokens(
			@PathVariable("identification") String identification) {
		final ClientPlatform clientPlatform = clientPlatformService.getByIdentification(identification);
		if (clientPlatform == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
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
			return ResponseEntity.ok().build();
		}
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Delete token in the digital client by token")
	@DeleteMapping(value = "/token/{token}")
	public ResponseEntity<TokenActivationResponse> deleteToken(@PathVariable("token") String tokenName) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setToken(tokenName);
		try {
			final Token token = tokenService.getTokenByToken(tokenName);
			if (token == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			if (!clientPlatformService.hasUserManageAccess(token.getClientPlatform().getId(), utils.getUserId())) {
				response.setOk(false);
			} else {
				entityDeletionService.deleteToken(token);
				response.setOk(true);
			}
		} catch (final Exception e) {
			response.setOk(false);
		}
		return ResponseEntity.ok().body(response);
	}
	
	
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Delete ClientPlatform in the digital client by id")
	@DeleteMapping(value = "/deleteClientPlatform/{id}")
	public ResponseEntity<?> deleteClientPlatform(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		
		final ClientPlatform ClientPlatformId = clientPlatformService.getIdByIdentification(id);
		try {
			final ClientPlatform device = clientPlatformService.getById(ClientPlatformId.getId());
			if (!clientPlatformService.hasUserManageAccess(ClientPlatformId.getId(), utils.getUserId())) {
				return new ResponseEntity<>(utils.getMessage("device.delete.error.forbidden", "forbidden"),
						HttpStatus.FORBIDDEN);
			}
			if (resourceService.isResourceSharedInAnyProject(device)) {
				return new ResponseEntity<>(
						"This digital client is shared within a Project, revoke access from project prior to deleting",
						HttpStatus.PRECONDITION_FAILED);
			}
			removeTable(device.getIdentification());
			entityDeletionService.deleteClient(ClientPlatformId.getId());
		} catch (final Exception e) {
			return new ResponseEntity<>(utils.getMessage("device.delete.error", "error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("Digital client " + ClientPlatformId.getIdentification() + " removed successfully", HttpStatus.OK);
	}
	
	private void removeTable(String identification) {
		try {
			manageDBPersistenceServiceFacade.removeTable4Ontology(LOG_ONTOLOGY_PREFIX + identification);
		} catch (final Exception e) {
			log.debug("Sth went wrong while removing table 4 ontology", e);
		}
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Enable or disable clientPlatform token by token")
	@PostMapping(value = "/token/{token}/active/{active}")
	public ResponseEntity<TokenActivationResponse> desactivateToken(@PathVariable("token") String tokenName,
			@PathVariable("active") Boolean active) {
		final TokenActivationResponse response = new TokenActivationResponse();
		response.setRequestedActive(active);
		response.setToken(tokenName);
		try {
			final Token token = tokenService.getTokenByToken(tokenName);
			if (token == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			tokenService.deactivateToken(token, active);
			response.setOk(true);
		} catch (final Exception e) {
			response.setOk(false);
		}
		return ResponseEntity.ok().body(response);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Create new token in the digital client by identification")
	@PostMapping(value = "/{identification}/token")
	public ResponseEntity<?> generateTokens(@PathVariable("identification") String identification,
			@RequestParam(value = "tenant", required = false) String tenant) {
		if (!StringUtils.isEmpty(tenant)) {
			multitenancyService.getTenant(tenant).ifPresent(t -> MultitenancyContextHolder.setTenantName(t.getName()));
		}
		final GenerateTokensResponse response = new GenerateTokensResponse();
		final boolean check = identification == null || identification.equals("");
		response.setOk(check);
		if (!check && tokenService
				.generateTokenForClient(clientPlatformService.getByIdentification(identification)) != null) {
			response.setOk(true);
			Token token = tokenService.generateTokenForClient(clientPlatformService.getByIdentification(identification));
			return ResponseEntity.ok().body("Token: " + token.getTokenName());
		} else {
			return ResponseEntity.ok().body(response);
		}
		
		
	}

}

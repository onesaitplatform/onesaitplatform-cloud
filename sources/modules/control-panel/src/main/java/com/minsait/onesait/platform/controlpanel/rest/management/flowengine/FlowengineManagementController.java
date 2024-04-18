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
package com.minsait.onesait.platform.controlpanel.rest.management.flowengine;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowDomain.State;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologySimplified;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.flow.engine.FlowEngineService;
import com.minsait.onesait.platform.libraries.flow.engine.FlowEngineServiceFactory;
import com.minsait.onesait.platform.libraries.flow.engine.exception.FlowEngineServiceException;
import com.minsait.onesait.platform.libraries.nodered.auth.NoderedAuthenticationService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "FlowEngine  Management", tags = { "FlowEngine management service" })
@RestController
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@RequestMapping("api/flowengine")
@Slf4j
public class FlowengineManagementController {
	@Value("${onesaitplatform.router.avoidsslverification:false}")
	private boolean avoidSSLVerification;
	@Value("${onesaitplatform.flowengine.startupdomain.wait.seconds:1}")
	private int secondsToWaitDomainStartup;
	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	private String proxyUrl;
	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;
	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private FlowDomainService flowDomainService;

	private FlowEngineService flowEngineService;

	@Autowired
	private NoderedAuthenticationService noderedAuthService;

	private static final String FLOWS_PATH = "/flows";
	private static final String FLOW_PATH = "/flow";

	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer ";
	private static final String UNAUTHORIZED = "Unauthorized access.";

	private static final String UNAUTHORIZED_LOG = "Unable to access the domain {}. Unauthorized access to NodeRED API.";
	private static final String DOMAIN_NOT_FOUND = "No domain was found for user {}";

	@PostConstruct
	public void init() {
		proxyUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.ADVICE);
		if (avoidSSLVerification) {
			httpRequestFactory = SSLUtil.getHttpRequestFactoryAvoidingSSLVerification();
		} else {
			httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		}

		httpRequestFactory.setConnectTimeout(restRequestTimeout);
		proxyUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.PROXYURL);
		String baseUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.BASE); // <host>/flowengine/admin
		flowEngineService = FlowEngineServiceFactory.getFlowEngineService(baseUrl, restRequestTimeout,
				avoidSSLVerification);
	}

	@ApiOperation(value = "Export Flow Domain by identification (Administrator only)")
	@GetMapping("/export/domain/{identification}")
	@ApiResponses(@ApiResponse(response = OntologySimplified.class, code = 200, message = "OK"))
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> exportFlowDomainByIdentification(
			@ApiParam(value = "Flow Domain identification", required = true) @PathVariable("identification") String flowDomainIdentification) {

		FlowDomain domain = flowDomainService.getFlowDomainByIdentification(flowDomainIdentification);
		if (domain != null) {
			return exportDomain(domain);
		} else {
			log.error("Domain {} was not found.", flowDomainIdentification);
			return new ResponseEntity<>("Domain not found.", HttpStatus.NOT_FOUND);
		}

	}

	@ApiOperation(value = "Exports a flow (NodeRED tab) from the desired FlowDomain (Administrator only)")
	@GetMapping("/export/{domainIdentification}/flow/{noderedFlowId}")
	@ApiResponses(@ApiResponse(response = OntologySimplified.class, code = 200, message = "OK"))
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> getFlowByUserAndId(
			@ApiParam(value = "Flow Domain identification", required = true) @PathVariable("domainIdentification") String flowDomainIdentification,
			@ApiParam(value = "NodeRED Flow internal Id", required = true) @PathVariable("noderedFlowId") String noderedFlowId) {
		FlowDomain domain = flowDomainService.getFlowDomainByIdentification(flowDomainIdentification);
		if (domain != null) {
			RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
			String url = proxyUrl + domain.getIdentification() + FLOW_PATH + "/" + noderedFlowId;
			HttpHeaders headers = new HttpHeaders();
			headers.set(AUTHORIZATION, BEARER
					+ noderedAuthService.getNoderedAuthAccessToken(utils.getUserId(), domain.getIdentification()));
			try {

				HttpEntity<String> entity = new HttpEntity<>(null, headers);
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
				return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
			} catch (HttpClientErrorException e) {
				if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
					return new ResponseEntity<>("No Flow was found for that domian.", HttpStatus.NOT_FOUND);
				}
			} catch (Exception e) {
				log.error(UNAUTHORIZED_LOG, flowDomainIdentification);
				return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			}

		}
		log.error("Domain {} was not found.", flowDomainIdentification);
		return new ResponseEntity<>("No domain was found.", HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Export Flow Domain based on the user.")
	@GetMapping("/export/domain")
	@ApiResponses(@ApiResponse(response = OntologySimplified.class, code = 200, message = "OK"))
	public ResponseEntity<String> exportFlowDomainByUser() {
		List<FlowDomain> domainList = flowDomainService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
		Optional<FlowDomain> domain = domainList.stream().filter(d -> d.getUser().getUserId().equals(utils.getUserId()))
				.findFirst();
		if (domain.isPresent()) {
			return exportDomain(domain.get());
		} else {
			log.error(DOMAIN_NOT_FOUND + "{}", userService.getUser(utils.getUserId()));
			return new ResponseEntity<>(DOMAIN_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
	}

	private ResponseEntity<String> exportDomain(FlowDomain domain) {

		if (domain.getState().equals("STOP")) {
			try {
				log.debug("Domain is not running. Searching for export json on FS ...");
				return new ResponseEntity<>(flowEngineService.exportDomainFromFS(domain.getIdentification()),
						HttpStatus.OK);
			} catch (Exception e) {
				log.error("Unable to get export json from FS for domain {}.", domain.getIdentification());
				return new ResponseEntity<>("Unable to get export json from FS for domain.",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		String url = proxyUrl + domain.getIdentification() + FLOWS_PATH;
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION,
				BEARER + noderedAuthService.getNoderedAuthAccessToken(utils.getUserId(), domain.getIdentification()));
		try {

			HttpEntity<String> entity = new HttpEntity<>(null, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			log.error(UNAUTHORIZED_LOG, domain.getIdentification());
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}
	}

	@ApiOperation(value = "Exports a flow (NodeRED tab) from your own domain by NodeRED ID")
	@GetMapping("/export/flow/{noderedId}")
	@ApiResponses(@ApiResponse(response = OntologySimplified.class, code = 200, message = "OK"))
	public ResponseEntity<String> getFlowByUserAndId(
			@ApiParam(value = "NodeRED Flow internal Id", required = true) @PathVariable("noderedId") String noderedId) {
		List<FlowDomain> domainList = flowDomainService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
		Optional<FlowDomain> domain = domainList.stream().filter(d -> d.getUser().getUserId().equals(utils.getUserId()))
				.findFirst();
		if (domain.isPresent()) {
			RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
			String url = proxyUrl + domain.get().getIdentification() + FLOW_PATH + "/" + noderedId;
			HttpHeaders headers = new HttpHeaders();
			headers.set(AUTHORIZATION, BEARER + noderedAuthService.getNoderedAuthAccessToken(utils.getUserId(),
					domain.get().getIdentification()));
			try {

				HttpEntity<String> entity = new HttpEntity<>(null, headers);
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
				return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
			} catch (HttpClientErrorException e) {
				if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
					return new ResponseEntity<>("No Flow was found for that domian.", HttpStatus.NOT_FOUND);
				}
			} catch (Exception e) {
				log.error(UNAUTHORIZED_LOG, domain.get().getIdentification());
				return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
			}

		}
		log.error(DOMAIN_NOT_FOUND + " {}", userService.getUser(utils.getUserId()));
		return new ResponseEntity<>(DOMAIN_NOT_FOUND, HttpStatus.NOT_FOUND);

	}

	@ApiOperation(value = "Import Flow Domain based on the user.")
	@PostMapping("/import/{domainName}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<String> importFlowDomainByUser(
			@ApiParam(value = "data", required = true) @Valid @RequestBody String json,
			@ApiParam(value = "Flow Domain identification", required = true) @PathVariable("domainName") String domainName,
			@ApiParam(value = "Overwrite existing domain for user", required = true) @RequestParam("overwriteDomain") boolean overwriteDomain) {
		return importDomainToUser(utils.getUserId(), json, domainName, overwriteDomain);
	}

	@ApiOperation(value = "Import Flow Domain to the desired user (administrator only).")
	@PostMapping("/import/domain/{domainName}/user/{user}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> importFlowDomainToUserAdmin(
			@ApiParam(value = "data", required = true) @Valid @RequestBody String json,
			@ApiParam(value = "Flow Domain identification", required = true) @PathVariable("domainName") String domainName,
			@ApiParam(value = "Platform User", required = true) @PathVariable("user") String user,
			@ApiParam(value = "Overwrite existing domain for user", required = true) @RequestParam("overwriteDomain") boolean overwriteDomain) {

		return importDomainToUser(user, json, domainName, overwriteDomain);
	}

	@ApiOperation(value = "Import Flow to the domain based on the user.")
	@PostMapping("/import/flow/domain/{domainName}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<String> importFlowToDomainByUser(
			@ApiParam(value = "data", required = true) @Valid @RequestBody String json,
			@ApiParam(value = "Flow Domain identification", required = true) @PathVariable("domainName") String domainName) {

		return importDomainFlowToUser(utils.getUserId(), json, domainName);
	}

	@ApiOperation(value = "Import Flow to the demoain of the desired user (administrator only).")
	@PostMapping("/import/flow/domain/{domainName}/user{user}")
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> importFlowToDomainAndUser(
			@ApiParam(value = "data", required = true) @Valid @RequestBody String json,
			@ApiParam(value = "Flow Domain identification", required = true) @PathVariable("domainName") String domainName,
			@ApiParam(value = "Platform User", required = true) @PathVariable("user") String user) {

		return importDomainFlowToUser(user, json, domainName);
	}

	private ResponseEntity<String> importDomainToUser(String userId, String data, String domainName,
			boolean overwriteDomain) {
		User user = userService.getUser(userId);
		if (user == null) {
			return new ResponseEntity<>("User " + userId + " does not exist.", HttpStatus.NOT_FOUND);
		}
		List<FlowDomain> domainList = flowDomainService.getFlowDomainByUser(user);
		Optional<FlowDomain> domain = domainList.stream().filter(d -> d.getUser().getUserId().equals(userId))
				.findFirst();
		if (domain.isPresent()) {
			if (!overwriteDomain) {
				log.error("User {} already has a defined domain ({}), different than the one imported: {}.",
						userService.getUser(userId), domain.get().getIdentification(), domainName);
				return new ResponseEntity<>("User already has a defined domain.", HttpStatus.NOT_FOUND);
			} else {
				// if overwrite is selected, then remove existing domain
				log.info("Domain {} will be overwritten by {} for user {}.", domain.get().getIdentification(),
						domainName, userId);
				flowDomainService.deleteFlowdomain(domainName);
				// Create and start domain
				FlowDomain newDomain = flowDomainService.createFlowDomain(domainName,
						userService.getUser(userId));
				startDomain(newDomain);
			}

		} else {
			// Create and start domain
			log.info("Domain {} does not exist for user {}. It will be creaded.", domainName, userId);
			FlowDomain newDomain = flowDomainService.createFlowDomain(domainName,
					userService.getUser(userId));
			startDomain(newDomain);
		}

		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		String url = proxyUrl + domainName + FLOWS_PATH;
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION,
				BEARER + noderedAuthService.getNoderedAuthAccessToken(userId, domainName));
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			String parsedJson = changeDomainIDs(data, userService.getUser(userId));
			HttpEntity<String> entity = new HttpEntity<>(parsedJson, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
		} catch (JSONException e) {
			log.error("Imported json has invalid structure for domain {}.", domainName);
			return new ResponseEntity<>("Imported json has invalid structure", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error(UNAUTHORIZED_LOG, domainName);
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}
	}

	private ResponseEntity<String> importDomainFlowToUser(String userId, String data, String domainName) {
		User user = userService.getUser(userId);
		if (user == null) {
			return new ResponseEntity<>("User " + userId + " does not exist.", HttpStatus.NOT_FOUND);
		}
		List<FlowDomain> domainList = flowDomainService.getFlowDomainByUser(user);
		Optional<FlowDomain> domain = domainList.stream().filter(d -> d.getUser().getUserId().equals(userId))
				.findFirst();
		if (domain.isPresent() && domain.get().getIdentification().equals(domainName)) {
			if (!domain.get().getIdentification().equals(domainName)) {
				log.error("User {} already has a defined domain ({}), different than the one imported: {}.",
						userService.getUser(utils.getUserId()), domain.get().getIdentification(), domainName);
				return new ResponseEntity<>("No domain was found for user.", HttpStatus.NOT_FOUND);
			}
			// Start domain if is STOPPED
			startDomain(domain.get());
		} else {
			log.error("Domain {} not found for user {}.", domainName, userService.getUser(utils.getUserId()));
			return new ResponseEntity<>("No domain was found for user.", HttpStatus.NOT_FOUND);
		}

		// import the FLow in NodeRED
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		String url = proxyUrl + domainName + FLOW_PATH;
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION,
				BEARER + noderedAuthService.getNoderedAuthAccessToken(utils.getUserId(), domainName));
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			String parsedJson = changeFlowIDs(data, userService.getUser(utils.getUserId()));
			HttpEntity<String> entity = new HttpEntity<>(parsedJson, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				// Get exported data for domain and send it to the FlowEngine to
				// persist in configDB (force deploy)
				ResponseEntity<String> exportResponse = exportDomain(domain.get());
				if (response.getStatusCode() != HttpStatus.OK) {
					log.error("Error retrieving domain {} deployment info.", domainName);
					return new ResponseEntity<>("Error retrieving domain deployment info.",
							exportResponse.getStatusCode());
				}
				// Force deploy
				flowEngineService.deployFlowengineDomain(domainName, exportResponse.getBody());

				return new ResponseEntity<>("Flow successfully imported.", HttpStatus.OK);

			}
			log.error("Error importing domain {} info.", domainName);
			return new ResponseEntity<>("Error importing domain info.", response.getStatusCode());

		} catch (FlowEngineServiceException e) {
			log.error("Error deploying after importing flow for domain {}", domainName);
			return new ResponseEntity<>("Error deploying after importing flow for domain",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (JSONException e) {
			log.error("Imported json has invalid structure for domain {}.", domainName);
			return new ResponseEntity<>("Imported json has invalid structure", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error(UNAUTHORIZED_LOG, domainName);
			return new ResponseEntity<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
		}

	}

	private void startDomain(FlowDomain domain) {
		if (domain.getState().equals("STOP")) {
			final FlowEngineDomain engineDom = FlowEngineDomain.builder().domain(domain.getIdentification())
					.port(domain.getPort()).home(domain.getHome()).servicePort(domain.getServicePort()).build();
			flowEngineService.startFlowEngineDomain(engineDom);
			domain.setState(State.START.name());
			flowDomainService.updateDomain(domain);
			try {
				TimeUnit.SECONDS.sleep(secondsToWaitDomainStartup);
			} catch (InterruptedException e) {
				log.warn(
						"Error waiting for domain {} to start. Execution will continue but it might fail due to the domain not being started. Should the process fail, please re-execute it.");
				Thread.currentThread().interrupt();
			}
		}
	}

	private String generateNodeAuthToken(User user) {
		final String password = user.getPassword();
		final String auth = user.getUserId() + ":" + password;
		return Base64.getEncoder().encodeToString(auth.getBytes());
	}

	private String changeDomainIDs(String jsonText, User user) {
		String parsedJson = jsonText;
		Map<String, String> idMap = new HashMap<>();
		String authToken = generateNodeAuthToken(user);
		// Get all ids and generate UUID for each one

		JSONArray json = new JSONArray(jsonText);

		for (int i = 0; i < json.length(); i++) {
			JSONObject o = json.getJSONObject(i);
			String uuid = UUID.randomUUID().toString().replace("-", "");
			// for ID replace
			idMap.put(o.getString("id"), uuid);
			if (o.getString("type").equals("onesaitplatform api rest operation")) {
				// replace url beginning with uuid
				String url = o.getString("url");
				String oldIdPath = url.substring(1, url.indexOf('/', 2));
				// replace string between the two first '/' chars
				o.put("url", url.replace(oldIdPath, uuid));
			}
			// Replace Authentication
			if (o.has("authentication")) {
				o.put("authentication", authToken);
			}
			parsedJson = json.toString();
		}

		for (Entry<String, String> entry : idMap.entrySet()) {
			// Replace ids and references
			parsedJson = parsedJson.replace(entry.getKey(), entry.getValue());
		}
		return parsedJson;
	}

	private String changeFlowIDs(String jsonText, User user) {

		JSONObject jsonFlow = null;

		// Extract nodes
		jsonFlow = new JSONObject(jsonText);
		// replace IDs in nodes
		String replacedNodes = changeDomainIDs(jsonFlow.getJSONArray("nodes").toString(), user);
		// persist changes
		jsonFlow.put("nodes", new JSONArray(replacedNodes));
		// change flow id in json (id and Z dependencies)
		return jsonFlow.toString().replace(jsonFlow.getString("id"), UUID.randomUUID().toString().replace("-", ""));
	}
}

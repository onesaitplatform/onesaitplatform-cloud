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
package com.minsait.onesait.platform.controlpanel.controller.bpm;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.components.AuthorizationLevel;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRoleListOauth;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.bpm.BPMTenantService;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("bpm")
@Slf4j
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
public class BPMController {

	@Autowired
	private UserService userService;
	@Autowired
	private BPMTenantService tenantService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private HttpSession httpSession;
	@Autowired
	private AppService appService;
	@Autowired
	private ConfigurationService configurationService;

	private RestTemplate restTemplate;

	private static final String APP_ID = "appId";
	private static final ObjectMapper MAPPER = new ObjectMapper();
	public static final String ROOT_NODE_REALMS = "realms";

	@PostConstruct
	void initRestTemplate() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	}

	@GetMapping("list")
	public String list(Model model) {

		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		model.addAttribute("realms", appService.getAllAppsList().stream().map(AppList::getIdentification).toList());
		model.addAttribute("tenants", tenants());
		model.addAttribute("camundaEndpoint", resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.BASE));
		model.addAttribute("users", userService.getAllUsers());
		return "bpm/list";
	}

	@GetMapping("authorizations/{tenantId}")
	@Deprecated
	public ResponseEntity<List<BPMAuthorization>> authorizations(@PathVariable("tenantId") String tenantId) {
		if (!tenantService.hasUserPermissions(tenantId, utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		final List<BPMAuthorization> auths = tenantService.getTenantAuthorizations(tenantId).stream()
				.map(a -> BPMAuthorization.builder().tenantId(a.getBpmTenant().getIdentification())
						.userId(a.getAuthorizedUser().getUserId()).build())
				.collect(Collectors.toList());
		return new ResponseEntity<>(auths, HttpStatus.OK);
	}

	@DeleteMapping("authorizations/{tenantId}/{userId}")
	@Deprecated
	public ResponseEntity<List<BPMAuthorization>> deleteAuth(@PathVariable("tenantId") String tenantId,
			@PathVariable("userId") String userId) {
		if (!tenantService.hasUserPermissions(tenantId, utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		tenantService.removeAuthorization(tenantId, userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("authorizations")
	@Deprecated
	public ResponseEntity<List<BPMAuthorization>> createAuth(@RequestBody BPMAuthorization authorization) {
		if (!tenantService.hasUserPermissions(authorization.getTenantId(), utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		tenantService.createTenantAuthorizationWhitId(authorization.getTenantId(), authorization.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("tenants")
	@Deprecated
	public @ResponseBody List<BPMTenant> tenants() {
		return tenantService
				.list(userService.getUser(utils.getUserId())).stream().map(t -> BPMTenant.builder().id(t.getId())
						.name(t.getIdentification()).owner(t.getUser().getUserId()).build())
				.collect(Collectors.toList());

	}

	@PostMapping("upload")
	public ResponseEntity<String> upload(@RequestParam("data") List<MultipartFile> data,
			@RequestParam(required = false, value = "useTenat", defaultValue = "true") boolean useTenant,
			@RequestParam(value = "name") String name) {

		File file = null;

		try {
			final MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
			int i = 0;
			for (final MultipartFile mf : data) {
				file = new File("/tmp/" + mf.getOriginalFilename());
				Files.write(file.toPath(), mf.getBytes());
				formData.add("data" + i, new FileSystemResource(file));
				i++;
			}
			formData.add("deployment-name", name);
			formData.add("deployment-source", "onesait platform");
			if (useTenant) {
				formData.add("tenant-id",
						com.minsait.onesait.platform.config.model.BPMTenant.TENANT_PREFIX + utils.getUserId());
			}
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + utils.getCurrentUserOauthToken());
			restTemplate.exchange(resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.DEPLOYMENT), HttpMethod.POST,
					new HttpEntity<>(formData, headers), JsonNode.class);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error while uploading bpmn file: {}", e.getResponseBodyAsString(), e);
			return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (final Exception e) {
			log.error("Error while uploading bpmn file", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			for (final MultipartFile mf : data) {
				file = new File("/tmp/" + mf.getOriginalFilename());
				if (file != null && file.exists()) {
					file.delete();
				}
			}

		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("sync-users")
	public String syncUsers(RedirectAttributes ra) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + utils.getCurrentUserOauthToken());
		try {
			restTemplate.exchange(
					resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.BASE) + "/management/sync-users",
					HttpMethod.POST, new HttpEntity<>(headers), String.class);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error while synchronizing users: {}", e.getResponseBodyAsString(), e);
			utils.addRedirectMessage(e.getResponseBodyAsString(), ra);
		} catch (final Exception e) {
			log.error("Error while uploading bpmn file", e);
			utils.addRedirectException(e, ra);
		}
		return "redirect:/bpm/list";
	}

	@GetMapping("realms/{realm}/map-roles")
	public String getMapRoles(@PathVariable("realm") String realm, Model model) {
		final JsonNode config = getConfigMap();
		if (config != null) {
			try {
				if (!config.path(ROOT_NODE_REALMS).isMissingNode()
						&& !config.path(ROOT_NODE_REALMS).path(realm).isMissingNode()) {
					model.addAttribute("rolesMap", config.get(ROOT_NODE_REALMS).get(realm));
				}
			} catch (final Exception e) {
				log.error("could not load camunda-realm mapping configuration", e);
			}
		}
		model.addAttribute("realm", realm);
		model.addAttribute("roles",
				appService.getAppRolesListOauth(realm).stream().map(AppRoleListOauth::getName).toList());
		model.addAttribute("groupLevels", List.of(AuthorizationLevel.values()));
		return "bpm/fragments/role-map";
	}

	@PostMapping("realms/{realm}/map-roles")
	public ResponseEntity<String> postMapRoles(@PathVariable("realm") String realm, @RequestBody JsonNode mapInfo) {
		JsonNode config = getConfigMap();
		if (config == null) {
			config = createBasicConfig();
		}
		try {
			if (!config.path(ROOT_NODE_REALMS).isMissingNode()) {
				((ObjectNode) config.get(ROOT_NODE_REALMS)).set(realm, mapInfo);
				Configuration c = getDBConfig();
				if (c != null) {
					c.setYmlConfig(MAPPER.writeValueAsString(config));
					configurationService.updateConfiguration(c);
				} else {
					c = new Configuration();
					c.setUser(userService.getUser(utils.getUserId()));
					c.setDescription(Type.BPM_ROLE_MAPPING.name());
					c.setEnvironment("DEFAULT");
					c.setIdentification(Type.BPM_ROLE_MAPPING.name().toLowerCase());
					c.setYmlConfig(MAPPER.writeValueAsString(config));
					c.setType(Type.BPM_ROLE_MAPPING);
					configurationService.createConfiguration(c);
				}
			} else {
				log.error("Malformerd BPM_MAPPING configuration");
			}
		} catch (final Exception e) {
			log.error("could not create camunda-realm mapping configuration", e);
		}

		return ResponseEntity.ok().build();
	}

	public JsonNode createBasicConfig() {
		final ObjectNode node = MAPPER.createObjectNode();
		node.set(ROOT_NODE_REALMS, MAPPER.createObjectNode());
		return node;
	}

	private JsonNode getConfigMap() {
		final Configuration config = getDBConfig();
		if (config != null) {
			try {
				return MAPPER.readValue(config.getYmlConfig(), JsonNode.class);
			} catch (final JsonProcessingException e) {
				log.error("Malformed BPM_ROLE_MAPPING configuration");
			}
		}
		return null;
	}

	private Configuration getDBConfig() {
		final List<Configuration> configs = configurationService.getConfigurations(Type.BPM_ROLE_MAPPING);
		if (!CollectionUtils.isEmpty(configs)) {
			if (configs.size() > 1) {
				log.error("More than one configuration found for BPM_ROLE_MAPPING, please keep just one configuration");
			} else {
				return configs.get(0);
			}
		} else {
			log.info("No configurations found of type BPM_ROLE_MAPPING");
		}
		return null;
	}

}

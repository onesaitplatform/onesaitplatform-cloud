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

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.services.bpm.BPMTenantService;
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
	
	private RestTemplate restTemplate;
	
	private static final String APP_ID = "appId";

	@PostConstruct
	void initRestTemplate() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
	}

	@GetMapping("list")
	public String list(Model model) {
		
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		model.addAttribute("tenants", tenants());
		model.addAttribute("camundaEndpoint", resourcesService.getUrl(Module.BPM_ENGINE, ServiceUrl.BASE));
		model.addAttribute("users", userService.getAllUsers());
		return "bpm/list";
	}

	@GetMapping("authorizations/{tenantId}")
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
	public ResponseEntity<List<BPMAuthorization>> deleteAuth(@PathVariable("tenantId") String tenantId,
			@PathVariable("userId") String userId) {
		if (!tenantService.hasUserPermissions(tenantId, utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		tenantService.removeAuthorization(tenantId, userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping("authorizations")
	public ResponseEntity<List<BPMAuthorization>> createAuth(@RequestBody BPMAuthorization authorization) {
		if (!tenantService.hasUserPermissions(authorization.getTenantId(), utils.getUserId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		tenantService.createTenantAuthorizationWhitId(authorization.getTenantId(), authorization.getUserId());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("tenants")
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

}

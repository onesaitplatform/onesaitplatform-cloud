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
package com.minsait.onesait.platform.controlpanel.controller.modelsmanager;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.services.modelsmanager.ModelsManagerService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/modelsmanager")
@Controller
@Slf4j
public class ModelsManagerController {

	@Autowired
	private ModelsManagerService modelsManagerService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	ServletContext context;
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/show", produces = "text/html")
	public String show(Model uiModel) {
		uiModel.addAttribute("user", utils.getUserId());
		uiModel.addAttribute("userRole", utils.getRole());
		
		return "modelsmanager/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/ajax-api/**" })
	@ResponseBody
	public ResponseEntity<String> restAjaxApiInterGet(Model uiModel, HttpServletRequest request)
			throws URISyntaxException, IOException {
		return modelsManagerService.sendHttp(request, HttpMethod.GET, "");
	}
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping(value = { "/ajax-api/**" })
	@ResponseBody
	public ResponseEntity<String> restAjaxApiInterPost(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body) throws URISyntaxException, IOException {
		return modelsManagerService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@DeleteMapping(value = { "/ajax-api/**" })
	@ResponseBody
	public ResponseEntity<String> restAjaxApiInterDelete(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body) throws URISyntaxException, IOException {
		return modelsManagerService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}
	
	//@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/api/**" })
	@ResponseBody
	public ResponseEntity<String> restApiInterGet(Model uiModel, HttpServletRequest request)
			throws URISyntaxException, IOException {
		return modelsManagerService.sendHttp(request, HttpMethod.GET, "");
	}
	
	//@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@PostMapping(value = { "/api/**" })
	@ResponseBody
	public ResponseEntity<String> restApiInterPost(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body) throws URISyntaxException, IOException {
		return modelsManagerService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}
	
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/get-artifact/**" })
	@ResponseBody
	public ResponseEntity<String> restGetArtInterGet(Model uiModel, HttpServletRequest request)
			throws URISyntaxException, IOException {
		return modelsManagerService.sendHttp(request, HttpMethod.GET, "");
	}
		
}

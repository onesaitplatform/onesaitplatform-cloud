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
package com.minsait.onesait.platform.controlpanel.controller.logcentralizer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/logcentralizer")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class LogCentralizerController {
	

	private static final String BEARER = "Bearer";
	private static final String REDIRECT_FLOWS_LIST = "redirect:/";
	
	private String baseUrl;

	@Autowired
	private AppWebUtils utils;
	
	@Autowired
	private IntegrationResourcesService resourcesService;
	
	@PostConstruct
	public void init() {
		baseUrl = resourcesService.getUrl(Module.LOGCENTRALIZER, ServiceUrl.BASE);
	}

	@GetMapping(value = "/show", produces = "text/html")
	public String showNodeRedPanelForm(Model model, RedirectAttributes ra) {
		
		
		// GET Oauth access token
		String accessToken = utils.getCurrentUserOauthToken();
		if (accessToken.toLowerCase().startsWith(BEARER)) {
			accessToken = accessToken.substring(BEARER.length());
		}
		// SET Query param
		try {
			String logCentralizerUrl = baseUrl + "/?accesstoken=" + accessToken;

			model.addAttribute("logCentralizerUrl", logCentralizerUrl);
			return "logcentralizer/show";
		} catch (final Exception e) {
			log.error("Error while constructing Graylog url. Cause: {}, Message: {}",e.getCause(),e.getMessage());
			utils.addRedirectException(e, ra);
			return REDIRECT_FLOWS_LIST;
		}

	}
	
}

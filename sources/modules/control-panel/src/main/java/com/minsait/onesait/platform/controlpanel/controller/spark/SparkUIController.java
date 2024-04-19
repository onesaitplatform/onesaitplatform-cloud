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
package com.minsait.onesait.platform.controlpanel.controller.spark;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/sparkui")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
@Slf4j
public class SparkUIController {

	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;

	@Value("${onesaitplatform.controlpanel.avoidsslverification:false}")
	private boolean avoidSSLVerification;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	@Autowired
	private AppWebUtils utils;

	private String SPARK_MASTER_UI = "https://midominio.com/spark/master";

	@PostConstruct
	public void init() {
		final Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());
		try{
			SPARK_MASTER_UI = urls.getSpark().getMasterUIBase();
		}catch (final Exception e) {
			log.warn("No Spark URL configured");
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/show", produces = "text/html")
	public String showNodeRedPanelForm(Model model, RedirectAttributes ra) {
		final String tokenOauth = utils.getCurrentUserOauthToken();
		model.addAttribute("masterUrl", SPARK_MASTER_UI + "?token=" + tokenOauth);
		return "sparkui/show";

	}
}

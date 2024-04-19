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
package com.minsait.onesait.platform.controlpanel.controller.promotiontool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.services.microservice.dto.JenkinsParameter;
import com.minsait.onesait.platform.config.services.promotiontool.PromotionToolParamsDTO;
import com.minsait.onesait.platform.config.services.promotiontool.PromotionToolService;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/promotiontool")
@Slf4j
public class PromotionToolController {

	@Autowired
	private JenkinsService jenkinsService;

	@Autowired
	private PromotionToolService promotionToolService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ConfigurationRepository configurationRepository;

	public static final String PROMOTION_TOOL_PARAMS = "promotiontoolparams";
	public static final String QUERY_NATIVE = "NATIVE";
	private static final String STATUS_STR = "status";
	private static final String CAUSE_STR = "cause";
	private static final String REDIRECT_STR = "redirect";
	private static final String ERROR_STR = "error";

	private static final String EXPORT_ON_ORIGIN = "EXPORT_ON_ORIGIN";
	private static final String IMPORT_ON_TARGET = "IMPORT_ON_TARGET";
	private static final String CONFIGDB_ORIGIN_HOST = "CONFIGDB_ORIGIN_HOST";
	private static final String REALTIMEDB_ORIGIN_HOST = "REALTIMEDB_ORIGIN_HOST";
	private static final String FLOWENGINE_ORIGIN_HOST = "FLOWENGINE_ORIGIN_HOST";
	private static final String NOTEBOOK_ORIGIN_HOST = "NOTEBOOK_ORIGIN_HOST";
	private static final String CONFIGDB_TARGET_HOST = "CONFIGDB_TARGET_HOST";
	private static final String REALTIMEDB_TARGET_HOST = "REALTIMEDB_TARGET_HOST";
	private static final String FLOWENGINE_TARGET_HOST = "FLOWENGINE_TARGET_HOST";
	private static final String NOTEBOOK_TARGET_HOST = "NOTEBOOK_TARGET_HOST";

	private static final String JENKINS_URL = "http://promotiontoolservice:8080/exporter";
	private static final String JENKINS_JOBNAME = "promotion-between-environments";
	private static final String JENKINS_CONFIGJOBNAME = "global-configuration";

	private static final String BACK_UP_PATH = "/tmp";
	private static final String REMOTE_USER_HOME = "/home/onesaitplatform";
	private static final String ORIGIN_DATA_PATH = "/datadrive";
	private static final String TARGET_DATA_PATH = "/datadrive";
	private static final String CONFIG_DB_USER = "root";
	private static final String CONFIG_DB_PASS = "changeIt!";
	private static final String REALTIME_DB_USER = "platformadmin";
	private static final String REALTIME_DB_PASS = "0pen-platf0rm-2018!";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping("execute")
	public String execute(Model model) {
		List<String> tenants = promotionToolService.getTenants();
		model.addAttribute(PROMOTION_TOOL_PARAMS, new PromotionToolParamsDTO());
		model.addAttribute("originTenants", tenants);
		return "promotiontool/execute";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("execute")
	public ResponseEntity<Map<String, String>> execute(Model model, @Valid PromotionToolParamsDTO promotiontoolparams,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) throws Exception {
		final Map<String, String> response = new HashMap<>();

		boolean result = configureJenkins(promotiontoolparams);
		if (!result) {
			response.put(STATUS_STR, "error");
			response.put(CAUSE_STR, "Something failed while configuring the promotion");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Boolean exportOrigin = promotiontoolparams.getExportOnOrigin();
		Boolean importTarget = promotiontoolparams.getImportOnTarget();

		List<JenkinsParameter> parameters = new ArrayList<>();

		parameters.add(new JenkinsParameter(EXPORT_ON_ORIGIN, exportOrigin.toString()));
		parameters.add(new JenkinsParameter(IMPORT_ON_TARGET, importTarget.toString()));

		if (exportOrigin) {
			parameters.add(new JenkinsParameter(CONFIGDB_ORIGIN_HOST, promotiontoolparams.getConfigdbOriginHost()));
			parameters.add(new JenkinsParameter(REALTIMEDB_ORIGIN_HOST, promotiontoolparams.getRtdbOriginHost()));
			parameters.add(new JenkinsParameter(FLOWENGINE_ORIGIN_HOST, promotiontoolparams.getFlowEngineOriginHost()));
			parameters.add(new JenkinsParameter(NOTEBOOK_ORIGIN_HOST, promotiontoolparams.getNotebooksOriginHost()));
		} else {
			parameters.add(new JenkinsParameter(CONFIGDB_ORIGIN_HOST, promotiontoolparams.getConfigdbTargetHost()));
			parameters.add(new JenkinsParameter(REALTIMEDB_ORIGIN_HOST, promotiontoolparams.getConfigdbTargetHost()));
			parameters.add(new JenkinsParameter(FLOWENGINE_ORIGIN_HOST, promotiontoolparams.getConfigdbTargetHost()));
			parameters.add(new JenkinsParameter(NOTEBOOK_ORIGIN_HOST, promotiontoolparams.getConfigdbTargetHost()));
		}

		if (importTarget) {
			parameters.add(new JenkinsParameter(CONFIGDB_TARGET_HOST, promotiontoolparams.getConfigdbTargetHost()));
			parameters.add(new JenkinsParameter(REALTIMEDB_TARGET_HOST, promotiontoolparams.getRtdbTargetHost()));
			parameters.add(new JenkinsParameter(FLOWENGINE_TARGET_HOST, promotiontoolparams.getFlowEngineTargetHost()));
			parameters.add(new JenkinsParameter(NOTEBOOK_TARGET_HOST, promotiontoolparams.getNotebooksTargetHost()));
		} else {
			parameters.add(new JenkinsParameter(CONFIGDB_TARGET_HOST, promotiontoolparams.getConfigdbOriginHost()));
			parameters.add(new JenkinsParameter(REALTIMEDB_TARGET_HOST, promotiontoolparams.getConfigdbOriginHost()));
			parameters.add(new JenkinsParameter(FLOWENGINE_TARGET_HOST, promotiontoolparams.getConfigdbOriginHost()));
			parameters.add(new JenkinsParameter(NOTEBOOK_TARGET_HOST, promotiontoolparams.getConfigdbOriginHost()));

		}

		final Map<String, List<String>> paramMap = parameters.stream()
				.collect(Collectors.toMap(p -> p.getName(), p -> Arrays.asList(p.getValue())));

		try {
			final int resultID = jenkinsService.buildWithParametersNoAuth(JENKINS_URL, JENKINS_JOBNAME, null, paramMap);

			response.put(STATUS_STR, "ok");
			response.put(CAUSE_STR, "Queue ID:" + resultID);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put(STATUS_STR, "error");
			response.put(CAUSE_STR, "Something failed in the promotion execution");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("checkStatus")
	public ResponseEntity<Map<String, String>> checkStatus(Model model, @RequestParam int queueId,
			RedirectAttributes redirect, HttpServletRequest request) throws Exception {
		final Map<String, String> response = new HashMap<>();
		Thread.sleep(10000);
		String result = jenkinsService.buildInfo(JENKINS_URL, JENKINS_JOBNAME, null, queueId).result();

		if (result == null) {
			response.put(STATUS_STR, "NO");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (result.equals("SUCCESS")) {
			response.put(STATUS_STR, "SUCCESS");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (result.equals("FAILURE")) {
			response.put(STATUS_STR, "FAILURE");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.put(STATUS_STR, result);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

	}

	public boolean configureJenkins(PromotionToolParamsDTO promotiontoolparams) {
		String configDBs = promotionToolService.getConfigDBs(promotiontoolparams.getTenants());
		String RTDBs = promotionToolService.getRealTimeDBs(promotiontoolparams.getTenants());

		List<JenkinsParameter> parameters = new ArrayList<>();

		parameters.add(new JenkinsParameter("BACK_UP_PATH", BACK_UP_PATH));
		parameters.add(new JenkinsParameter("REMOTE_USER_HOME", REMOTE_USER_HOME));
		parameters.add(new JenkinsParameter("ORIGIN_DATA_PATH", ORIGIN_DATA_PATH));
		parameters.add(new JenkinsParameter("TARGET_DATA_PATH", TARGET_DATA_PATH));
		parameters.add(new JenkinsParameter("CONFIG_DB_USER", CONFIG_DB_USER));
		parameters.add(new JenkinsParameter("CONFIG_DB_PASS", CONFIG_DB_PASS));
		parameters.add(new JenkinsParameter("REALTIME_DB_USER", REALTIME_DB_USER));
		parameters.add(new JenkinsParameter("REALTIME_DB_PASS", REALTIME_DB_PASS));
		parameters.add(new JenkinsParameter("CONFIG_DB_SCHEMAS", configDBs));
		parameters.add(new JenkinsParameter("REALTIME_DB_SCHEMAS", RTDBs));

		final Map<String, List<String>> paramMap = parameters.stream()
				.collect(Collectors.toMap(p -> p.getName(), p -> Arrays.asList(p.getValue())));

		try {
			final int queueId = jenkinsService.buildWithParametersNoAuth(JENKINS_URL, JENKINS_CONFIGJOBNAME, null,
					paramMap);
			BuildInfo pipelineInfo = jenkinsService.buildInfo(JENKINS_URL, JENKINS_CONFIGJOBNAME, null, queueId);
			while (pipelineInfo == null || pipelineInfo.result() == null) {
				Thread.sleep(1000);
				pipelineInfo = jenkinsService.buildInfo(JENKINS_URL, JENKINS_CONFIGJOBNAME, null, queueId);
			}
			if (jenkinsService.buildInfo(JENKINS_URL, JENKINS_CONFIGJOBNAME, null, queueId).result().equals("SUCCESS"))
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}

	}

}

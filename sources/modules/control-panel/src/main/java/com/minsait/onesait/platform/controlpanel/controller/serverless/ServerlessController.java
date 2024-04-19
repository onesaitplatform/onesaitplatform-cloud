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
package com.minsait.onesait.platform.controlpanel.controller.serverless;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.controlpanel.service.serverless.ServerlessService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Controller
@RequestMapping("serverless")
public class ServerlessController {

	@Value("${onesaitplatform.gitlab.manager.server}")
	private String gitlabManagerServer;
	
	@Autowired
	UserService userservice;
	
	@Autowired
	UserTokenService usertokenservice;
	
	@Autowired
	private AppWebUtils utils;
	
	@Autowired
	private ServerlessService serverlessService;

	@GetMapping("applications")
	public String applications(Model model) {
		try {
			model.addAttribute("applications", serverlessService.getApplications());
		} catch (final Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("applications", new ArrayList<>());
		}
		return "serverless/applications";
	}

	@GetMapping("applications/{name}")
	public String getShowApplication(Model model, @PathVariable("name") String appName) {
		try {

			model.addAttribute("app", serverlessService.getApplication(appName));
		} catch (final Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("app", new ApplicationInfo());
		}
		return "serverless/application-show";
	}

	@GetMapping("applications/create")
	public String getCreateApplication(Model model) {
		model.addAttribute("app", new ApplicationCreate());
		return "serverless/application-create";
	}

	@PostMapping("applications")
	public String createApplication(@ModelAttribute ApplicationCreate app, Model model,
			@RequestParam(name = "createGit", defaultValue = "true", required = false) Boolean createGit) {
		try {
			app.setCreateGit(createGit);
			serverlessService.createApplication(app);
		} catch (final Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("app", new ApplicationInfo());
			return "serverless/application-show";
		}
		return "redirect:/serverless/applications/" + app.getName();
	}

	@GetMapping("applications/{name}/update")
	public String getUpdateApplication(Model model, @PathVariable("name") String appName) {
		try {
			// TO-DO environment
			model.addAttribute("app", serverlessService.getApplication(appName));
		} catch (final Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("app", new ApplicationInfo());
		}
		return "serverless/application-create";
	}

	@PostMapping("applications/{name}")
	public String updateApplication(@ModelAttribute ApplicationUpdate app, @PathVariable("name") String appName,
			Model model) {
		try {
			serverlessService.updateApplication(app);
		} catch (final Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("app", new ApplicationInfo());
			return "serverless/application-show";
		}
		return "redirect:/serverless/applications/" + appName;
	}

	@GetMapping("applications/{name}/functions")
	public String functions(Model model, @PathVariable("name") String appName) {
		model.addAttribute("token", utils.getCurrentUserOauthToken());
		model.addAttribute("baseUrl", gitlabManagerServer);
		model.addAttribute("app", serverlessService.getApplication(appName));
		model.addAttribute("function", new FunctionCreate());
		return "serverless/functions";
	}

	@PostMapping("applications/{name}/functions")
	public String createFunction(@ModelAttribute FunctionCreate function, @PathVariable("name") String appName) {
		serverlessService.createFunction(appName, function.getName(), function);
		return "redirect:/serverless/applications/" + appName;
	}

	@GetMapping("applications/{appName}/functions/{fnName}/update")
	public String getUpdateFunction(Model model, @PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		try {
			model.addAttribute("token", utils.getCurrentUserOauthToken());
			model.addAttribute("baseUrl", gitlabManagerServer);
			model.addAttribute("function", serverlessService.getFunction(appName, fnName));
			model.addAttribute("app", serverlessService.getApplication(appName));
		} catch (final Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("function", new FunctionInfo());
		}
		return "serverless/functions";
	}

	@PostMapping("applications/{appName}/functions/{fnName}/update")
	public String updateFunction(@ModelAttribute FunctionUpdate function, @PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		serverlessService.updateFunction(appName, fnName, function);
		return "redirect:/serverless/applications/" + appName;
	}

	@PostMapping("applications/{appName}/functions/{fnName}/deploy")
	public ResponseEntity<Object> deployFunction(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		serverlessService.deployFunction(appName, fnName);
		return ResponseEntity.ok().build();
	}

	@PostMapping("applications/{appName}/functions/{fnName}/version")
	public ResponseEntity<Object> updateVersion(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName, @RequestBody String version) {
		serverlessService.updateFunction(appName, fnName, version);
		return ResponseEntity.ok().build();
	}

	@GetMapping("applications/{appName}/functions/{fnName}/environment")
	public ResponseEntity<Object> getEnvironment(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		final ObjectNode config = serverlessService.getEnvironment(appName, fnName);
		return ResponseEntity.ok(config);
	}

	@PostMapping("applications/{appName}/functions/{fnName}/environment")
	public ResponseEntity<Object> updateEnvironment(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName, @RequestBody ObjectNode config) {
		serverlessService.updateFunctionEnvironment(appName, fnName, config);
		return ResponseEntity.ok().build();
	}

	@GetMapping("applications/{appName}/functions/{fnName}")
	public String showFunction(@PathVariable("appName") String appName, @PathVariable("fnName") String fnName) {
		return "serverless/functions";
	}

	@DeleteMapping("applications/{appName}/functions/{fnName}")
	public ResponseEntity<Object> deleteFunction(@PathVariable("appName") String appName,
			@PathVariable("fnName") String fnName) {
		serverlessService.deleteFunction(appName, fnName);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("applications/{appName}")
	public ResponseEntity<Object> deleteApplication(@PathVariable("appName") String appName) {
		serverlessService.deleteApplication(appName);
		return ResponseEntity.ok().build();
	}

}

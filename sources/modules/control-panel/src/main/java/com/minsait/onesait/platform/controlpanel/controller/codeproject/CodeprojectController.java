/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.codeproject;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.CodeProject;
import com.minsait.onesait.platform.config.services.codeproject.CodeProjectService;
import com.minsait.onesait.platform.config.services.codeproject.dto.CodeProjectDTO;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.microservice.dto.MSConfig;
import com.minsait.onesait.platform.config.services.microservice.dto.MicroserviceDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionInfo;
import com.minsait.onesait.platform.controlpanel.services.codeproject.CodeprojectBusinessService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("codeproject")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class CodeprojectController {

	private static final String ERROR_404 = "error/404";
	@Autowired
	private CodeProjectService codeProjectService;
	@Autowired
	private CodeprojectBusinessService codeProjectBusinessService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private HttpSession httpSession;
	private static final String APP_ID = "appId";
	
	@Value("${onesaitplatform.gitlab.manager.server}")
	private String gitlabManagerServer;

	@GetMapping("list")
	public String list(Model model, @RequestParam(required = false, name = "find_codeproject") String desc) {
		httpSession.removeAttribute(APP_ID);
		if (utils.isAdministrator()) {
			if (desc == null || desc.equals("")) {
				model.addAttribute("codeproject", codeProjectService.getAllCodeProjects());				
			}else {
				model.addAttribute("codeproject", codeProjectService.getCodeProjects(desc));
				model.addAttribute("find_codeproject",desc); 
			}
		} else {
			model.addAttribute("codeproject",
					codeProjectService.getCodeProjects(userService.getUser(utils.getUserId())));
		}
		return "codeproject/list";
	}

	@GetMapping("data")
	public @ResponseBody List<CodeProjectDTO> listData(Model model, @RequestParam(required = false, name = "find_codeproject") String findCodeProject) {
		List<CodeProject> codeProject = null;
		if (utils.isAdministrator()) {
			if (findCodeProject == null || findCodeProject.equals("")) {
				codeProject = codeProjectService.getAllCodeProjects();
			}else {
				codeProjectService.getCodeProjects(findCodeProject);
				model.addAttribute("find_codeproject",findCodeProject);
			}
		} else {
			if (findCodeProject == null || findCodeProject.equals("")) {
				codeProject = codeProjectService.getCodeProjects(userService.getUser(utils.getUserId()));
			}else {
				codeProjectService.getCodeProjects(findCodeProject);
				model.addAttribute("find_codeproject",findCodeProject);
			}
		}
		return codeProject.stream()
				.map(c -> CodeProjectDTO.builder().id(c.getId()).name(c.getIdentification())
						.owner(c.getUser().getUserId())
						.privateToken(c.getGitlabConfiguration().getPrivateToken())
						.username(c.getGitlabConfiguration().getUser())
						.repo(c.getGitlabConfiguration().getSite())
						.active(c.isActive()).build())
				.collect(Collectors.toList());
	}

	@GetMapping("create")
	public String create(Model model) {
		model.addAttribute("codeproject", CodeProjectDTO.builder().build());
		model.addAttribute("defaultGitlab", configurationService.getDefautlGitlabConfiguration() != null);
		return "codeproject/create";
	}
	
	@PostMapping("create")
	public String createPost(Model model, @Valid CodeProjectDTO codeProject, RedirectAttributes ra) {
		try {
			codeProjectBusinessService.createCodeproject(codeProject);
		} catch (final Exception e) {
			log.error("Could not create codeProject", e);
			utils.addRedirectException(e, ra);
		}
		return "redirect:/codeproject/list";
	}

	@GetMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id) {

		final CodeProject codeProject = codeProjectService.getById(id);
		if (codeProject == null) {
			return ERROR_404;
		}
		model.addAttribute("codeproject",
				CodeProjectDTO.builder().name(codeProject.getName()).id(codeProject.getId())
						.owner(codeProject.getUser().getUserId())
						.username(codeProject.getGitlabConfiguration().getUser())
						.privateToken(codeProject.getGitlabConfiguration().getPrivateToken())
						.repo(codeProject.getGitlabConfiguration().getSite()).build());
		return "codeproject/create";
	}
	
	@GetMapping("sourcecode/{id}")
	public String sourcecode(Model model, @PathVariable("id") String id) {

		final CodeProject codeProject = codeProjectService.getById(id);
		if (codeProject == null) {
			return ERROR_404;
		}
		
		model.addAttribute("token", utils.getCurrentUserOauthToken());
		model.addAttribute("baseUrl", gitlabManagerServer);
		
		model.addAttribute("codeproject",
				CodeProjectDTO.builder().name(codeProject.getName())
						.id(codeProject.getId())						
						.owner(codeProject.getUser().getUserId())
						.username(codeProject.getGitlabConfiguration().getUser())
						.privateToken(codeProject.getGitlabConfiguration().getPrivateToken())
						.repo(codeProject.getGitlabConfiguration().getSite()).build());
						
		return "codeproject/sourcecode";
	}
	
	@PostMapping("update/{id}")
	public String update(Model model, CodeProjectDTO codeProject, @PathVariable("id") String id) {
		codeProjectService.update(codeProject);
		return "redirect:/codeproject/list";
	}

	@DeleteMapping("{id}")
	public ResponseEntity<String> delete(@PathVariable("id") String id) {
		final CodeProject codeProject = codeProjectService.getById(id);
		codeProjectBusinessService.deleteCodeproject(codeProject);
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}
}

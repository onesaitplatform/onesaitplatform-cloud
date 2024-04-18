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
package com.minsait.onesait.platform.controlpanel.controller.nginxmanager;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.config.services.nginxmanage.NginxDto;
import com.minsait.onesait.platform.config.services.nginxmanage.NginxManageService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/nginxmanager")
@Slf4j
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
public class NginxManagerController {

	@Autowired
	private NginxManageService nginxmanagerService;
	
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String NGINXMANAGER_STR = "nginxmanager";
	private static final String REDIRECT_NGINX_SHOW = "redirect:/nginxmanager/show";

	@GetMapping("/show")
	public String show(Model model) {
	
		String nginxConf=nginxmanagerService.getNginx();
		NginxDto dto=new NginxDto();
		dto.setValue(nginxConf);
		model.addAttribute("nginx", dto);
		model.addAttribute("user",utils.getUserId());

		return "nginxmanager/show";

	}

	@GetMapping("/undo")
	public String undo(Model model) {

		String nginxConf=nginxmanagerService.undoNginx();
		NginxDto dto=new NginxDto();
		dto.setValue(nginxConf);
		model.addAttribute("nginx", dto);
		model.addAttribute("user",utils.getUserId());

		return "nginxmanager/show";

	}
	
	@GetMapping("/reset")
	public String reset(Model model) {

		String nginxConf=nginxmanagerService.resetNginx();
		NginxDto dto=new NginxDto();
		dto.setValue(nginxConf);
		model.addAttribute("nginx", dto);
		model.addAttribute("user",utils.getUserId());

		return "nginxmanager/show";

	}
	
	@PostMapping("/set")
	public String set(@Valid NginxDto nginx, Model model) {

		String nginxConf=nginxmanagerService.setNginx(nginx.getValue());
		NginxDto dto=new NginxDto();
		dto.setValue(nginxConf);
		model.addAttribute("nginx", dto);
		model.addAttribute("user",utils.getUserId());

		return "nginxmanager/show";

	}
	
	@PostMapping("/test")
	public String test(@Valid NginxDto nginx, Model model) {

		String nginxConf=nginxmanagerService.testNginx(nginx.getValue());
		NginxDto dto=new NginxDto();
		dto.setValue(nginxConf);
		model.addAttribute("nginx", dto);
		model.addAttribute("user",utils.getUserId());

		return "nginxmanager/show";

	}
	
	@GetMapping("/test")
	public String test(@PathVariable String nginx, Model model) {

		String nginxConf=nginxmanagerService.testNginx(nginx);
		NginxDto dto=new NginxDto();
		dto.setValue(nginxConf);
		model.addAttribute("nginx", dto);
		model.addAttribute("user",utils.getUserId());

		return "nginxmanager/show";

	}

}

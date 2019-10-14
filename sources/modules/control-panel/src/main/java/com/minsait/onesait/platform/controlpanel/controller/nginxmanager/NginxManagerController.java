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
package com.minsait.onesait.platform.controlpanel.controller.nginxmanager;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.nginxmanage.NginxManageService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import com.minsait.onesait.platform.config.services.nginxmanage.NginxDto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/nginxmanager")
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
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

		//final String nginx = NginxManagerService.getNginx();
//		final String nginx = "user www-data;\r\n" + 
//				"worker_processes 4;\r\n" + 
//				"pid /run/nginx.pid;\r\n" + 
//				"\r\n" + 
//				"events {\r\n" + 
//				"	worker_connections 4000;\r\n" + 
//				"	use epoll;\r\n" + 
//				"	multi_accept on;\r\n" + 
//				"}\r\n" + 
//				"\r\n" + 
//				"http {\r\n" + 
//				"	##\r\n" + 
//				"	# Basic Settings\r\n" + 
//				"	##\r\n" + 
//				"\r\n" + 
//				"	sendfile on;\r\n" + 
//				"	tcp_nopush on;\r\n" + 
//				"	tcp_nodelay on;\r\n" + 
//				"	keepalive_timeout 65;\r\n" + 
//				"	types_hash_max_size 2048;\r\n" + 
//				"	\r\n" + 
//				"	# disable any limits to avoid HTTP 413 for large image uploads\r\n" + 
//				"	client_max_body_size 0;\r\n" + 
//				"	\r\n" + 
//				"    # required to avoid HTTP 411: see Issue #1486 (https://github.com/docker/docker/issues/1486)\r\n" + 
//				"    chunked_transfer_encoding on;	\r\n" + 
//				"	\r\n" + 
//				"	server_tokens off;\r\n" + 
//				"    proxy_pass_header Server;\r\n" + 
//				"	\r\n" + 
//				"	include /etc/nginx/mime.types;\r\n" + 
//				"	default_type application/octet-stream;\r\n" + 
//				"\r\n" + 
//				"	##\r\n" + 
//				"	# Logging Settings\r\n" + 
//				"	##\r\n" + 
//				"\r\n" + 
//				"	access_log /var/log/nginx/access.log;\r\n" + 
//				"	error_log /var/log/nginx/error.log;\r\n" + 
//				"\r\n" + 
//				"	##\r\n" + 
//				"	# Gzip Settings\r\n" + 
//				"	##\r\n" + 
//				"\r\n" + 
//				"	gzip on;\r\n" + 
//				"	gzip_disable \"msie6\";\r\n" + 
//				"\r\n" + 
//				"	##\r\n" + 
//				"	# Virtual Host Configs\r\n" + 
//				"	##\r\n" + 
//				"	\r\n" + 
//				"	# Importante para nombres de dominio muy largos\r\n" + 
//				"	server_names_hash_bucket_size 128;\r\n" + 
//				"\r\n" + 
//				"	include /etc/nginx/conf.d/*.conf;\r\n" + 
//				"	include /etc/nginx/sites-enabled/*;\r\n" + 
//				"\r\n" + 
//				"	server {\r\n" + 
//				"			\r\n" + 
//				"    		listen 90;\r\n" + 
//				"    		server_name ${SERVER_NAME};\r\n" + 
//				"						\r\n" + 
//				"			location /controlpanel {\r\n" + 
//				"				proxy_pass http://controlpanelservice:18000/controlpanel;\r\n" + 
//				"				proxy_read_timeout 360s;\r\n" + 
//				"			\r\n" + 
//				"			    proxy_http_version 1.1;\r\n" + 
//				"				proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\r\n" + 
//				"            	proxy_set_header Host $http_host;\r\n" + 
//				"				proxy_set_header X-Forwarded-Proto http;\r\n" + 
//				"            	#proxy_redirect off;\r\n" + 
//				"				proxy_redirect ~^http://controlpanelservice:18000(.+)$ https://$host$1;\r\n" + 
//				"				add_header 'Access-Control-Allow-Origin' '*' always;\r\n" + 
//				"                add_header 'Access-Control-Allow-Credentials' 'true';\r\n" + 
//				"                add_header 'Access-Control-Allow-Methods' 'GET,POST';\r\n" + 
//				"			}\r\n" + 
//				"			\r\n" + 
//				"	}\r\n" + 
//				"\r\n" + 
//				"\r\n" + 
//				"}\r\n" + 
//				"";
		
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

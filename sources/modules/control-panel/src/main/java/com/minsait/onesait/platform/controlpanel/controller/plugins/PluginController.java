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
package com.minsait.onesait.platform.controlpanel.controller.plugins;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.plugins.PluginsManager;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.plugin.Module;
import com.minsait.onesait.platform.plugin.PlatformPlugin;

@Controller
@RequestMapping("/plugins")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_PLATFORM_ADMIN')")
public class PluginController {

	@Autowired
	private AppWebUtils utils;

	@GetMapping("/list")
	public String list(Model model) {
		final List<PlatformPlugin> plugins = PluginsManager.plugins();
		model.addAttribute("plugins", plugins);
		return "plugins/list";
	}

	@GetMapping("/upload")
	public String uploadGet(Model model) {
		if (model.asMap().get("message") != null) {
			model.addAttribute("message", model.asMap().get("message"));
		}
		model.addAttribute("moduleTypes", List.of(Module.values()));
		model.addAttribute("plugin", new PlatformPlugin());
		return "plugins/create";
	}

	@PostMapping("/upload")
	public String upload(@ModelAttribute PlatformPlugin plugin, @RequestParam("file") MultipartFile jarFile,
			RedirectAttributes ra) {
		plugin.setPublisher(utils.getUserId());
		try {
			PluginsManager.uploadPlugin(plugin, jarFile);
		} catch (final Exception e) {
			ra.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/plugins/list";
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> deletePlugin(@PathVariable("id") String id) {
		PluginsManager.deletePlugin(id);
		return ResponseEntity.ok().build();
	}

}

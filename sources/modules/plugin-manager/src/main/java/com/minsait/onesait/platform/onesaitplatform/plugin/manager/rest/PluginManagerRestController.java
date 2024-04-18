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
package com.minsait.onesait.platform.onesaitplatform.plugin.manager.rest;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.onesaitplatform.plugin.manager.dto.RestErrorMessage;
import com.minsait.onesait.platform.onesaitplatform.plugin.manager.service.PluginManagerService;
import com.minsait.onesait.platform.plugin.Module;
import com.minsait.onesait.platform.plugin.PlatformPlugin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/plugins")
@Slf4j
public class PluginManagerRestController {

	@Autowired
	private PluginManagerService pluginManagerService;

	@Operation(summary = "Upload Plugin JAR")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> upload(@RequestParam("file") MultipartFile file, @RequestParam("user") String user,
			@RequestParam(value = "module", required = false, defaultValue = "ALL") Module module) {
		try {
			pluginManagerService.uploadPlugin(module, user, file);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().body(new RestErrorMessage(e.getMessage()));
		}
		return ResponseEntity.ok().build();

	}

	@Operation(summary = "Get existing plugins")
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PlatformPlugin>> getPlugins(
			@RequestParam(required = false, value = "module") Module module) {
		return ResponseEntity.ok(pluginManagerService.getPluginsForModule(module));
	}

	@Operation(summary = "Get plugin by ID")
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PlatformPlugin> getPlugins(@PathVariable("id") String pluginId) {
		final PlatformPlugin plugin = pluginManagerService.getPlugin(pluginId);
		if (plugin == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(plugin);
	}

	@Operation(summary = "Get plugin by ID")
	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PlatformPlugin> deletePlugin(@PathVariable("id") String pluginId) {
		pluginManagerService.deletePlugin(pluginId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Download Plugin JAR")
	@GetMapping("/{id}/download")
	public ResponseEntity<ByteArrayResource> getBinary(@PathVariable("id") String pluginId) {
		try {

			final File f = pluginManagerService.getPluginJAR(pluginId);
			if (f != null) {
				final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(f.toPath()));

				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(f.getName()))
						.contentLength(resource.contentLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
			} else {
				pluginManagerService.deletePlugin(pluginId);
			}

		} catch (final Exception e) {
			log.error("Error while retrieving plugin", e);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);

	}

	@Operation(summary = "Set plugin loaded")
	@PostMapping("/{id}/loaded/{loaded}")
	public ResponseEntity<Void> setLoaded(@PathVariable("id") String pluginId, @PathVariable("loaded") Boolean loaded,
			@RequestParam(value = "module", required = false) Module module) {
		pluginManagerService.setPluginLoaded(pluginId, loaded);
		return ResponseEntity.ok().build();
	}

}

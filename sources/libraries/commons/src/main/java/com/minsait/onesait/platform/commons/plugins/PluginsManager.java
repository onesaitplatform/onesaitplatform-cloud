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
package com.minsait.onesait.platform.commons.plugins;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.plugin.Module;
import com.minsait.onesait.platform.plugin.PlatformPlugin;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginsManager {

	private static final String APPLICATION_BOOT_INF_LIB = System.getenv("PLUGINS_LIB") == null
			? "/application/plugins/"
			: System.getenv("PLUGINS_LIB");
	private static final String PLUGINS_MANAGER_URL = "http://plugin-manager:18080/plugins-manager";
	private static final String PLUGINS_MANAGER_API = "/api/plugins";
	private static final String PLUGINS_MANAGER_API_DOWNLOAD = "/download";
	private static final String PLUGINS_MANAGER_API_LOADED = "/loaded/";

	private static final RestTemplate TEMPLATE = new RestTemplate();

	public static void loadPlugins(Module module) {
		if (new File(APPLICATION_BOOT_INF_LIB).exists()) {
			log.info("Loading plugins for module {}", module.name());
			final List<PlatformPlugin> plugins = PluginsManager.plugins(module);
			if (!CollectionUtils.isEmpty(plugins)) {
				plugins.stream().forEach(p -> {
					log.info("Loading plugin {}", p.getJarFile());
					PluginsManager.loadPlugin(p);
				});
			}
		} else {
			log.warn("Skipping plugins as it's not a Docker deploy, {} directory does not exist",
					APPLICATION_BOOT_INF_LIB);
		}

	}

	public static List<PlatformPlugin> plugins(Module module) {
		try {
			final ResponseEntity<List<PlatformPlugin>> plugins = TEMPLATE.exchange(
					PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API + "?module=" + module.name(), HttpMethod.GET, null,
					new ParameterizedTypeReference<List<PlatformPlugin>>() {
					});
			return plugins.getBody();
		} catch (final HttpStatusCodeException e) {
			log.error("Error {} while getting plugins: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
		} catch (final Exception e) {
			log.error("Error while getting plugins", e);
		}
		return null;
	}

	public static List<PlatformPlugin> plugins() {
		try {
			final ResponseEntity<List<PlatformPlugin>> plugins = TEMPLATE.exchange(
					PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<PlatformPlugin>>() {
					});
			return plugins.getBody();
		} catch (final HttpStatusCodeException e) {
			log.error("Error {} while getting plugins: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
		} catch (final Exception e) {
			log.error("Error while getting plugins", e);
		}
		return null;
	}

	public static PlatformPlugin plugin(String id) {
		try {
			final ResponseEntity<PlatformPlugin> plugin = TEMPLATE.exchange(
					PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API + "/" + id, HttpMethod.GET, null, PlatformPlugin.class);
			return plugin.getBody();
		} catch (final HttpStatusCodeException e) {
			log.error("Error {} while getting plugins: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
		} catch (final Exception e) {
			log.error("Error while getting plugins", e);
		}
		return null;
	}

	public static void deletePlugin(String id) {
		try {
			TEMPLATE.exchange(PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API + "/" + id, HttpMethod.DELETE, null,
					Void.class);
		} catch (final HttpStatusCodeException e) {
			log.error("Error {} while delete plugin: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
		} catch (final Exception e) {
			log.error("Error while delete plugin", e);
		}
	}

	public static void loadPlugin(PlatformPlugin p) {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
			final HttpEntity<String> entity = new HttpEntity<>(headers);
			final ResponseEntity<byte[]> response = TEMPLATE.exchange(
					PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API + "/" + p.getId() + PLUGINS_MANAGER_API_DOWNLOAD,
					HttpMethod.GET, entity, byte[].class);

			Files.write(Paths.get(APPLICATION_BOOT_INF_LIB + p.getJarFile()), response.getBody());
			TEMPLATE.exchange(
					PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API + "/" + p.getId() + PLUGINS_MANAGER_API_LOADED + "true",
					HttpMethod.POST, entity, Void.class);
		} catch (final Exception e) {
			log.error("Could not load plugin {}", p.getJarFile(), e);
		}
	}

	public static void uploadPlugin(PlatformPlugin p, MultipartFile jarFile) throws Exception {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", jarFile.getResource());
			body.add("user", p.getPublisher());
			body.add("module", p.getModule().name());
			final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			TEMPLATE.exchange(PLUGINS_MANAGER_URL + PLUGINS_MANAGER_API, HttpMethod.POST, requestEntity, Void.class);
		} catch (final HttpStatusCodeException e) {
			log.error("Error {} while uploading plugin: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
			throw new Exception(e.getResponseBodyAsString(), e);
		} catch (final Exception e) {
			log.error("Error while uploading plugin", e);
			throw e;
		}

	}

}

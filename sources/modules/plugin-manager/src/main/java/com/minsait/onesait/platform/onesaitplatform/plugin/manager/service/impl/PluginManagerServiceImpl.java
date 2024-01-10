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
package com.minsait.onesait.platform.onesaitplatform.plugin.manager.service.impl;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.onesaitplatform.plugin.manager.model.Plugin;
import com.minsait.onesait.platform.onesaitplatform.plugin.manager.repository.PluginRepository;
import com.minsait.onesait.platform.onesaitplatform.plugin.manager.service.PluginManagerService;
import com.minsait.onesait.platform.plugin.Module;
import com.minsait.onesait.platform.plugin.PlatformPlugin;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PluginManagerServiceImpl implements PluginManagerService {

	@Value("${onesaitplatform.plugins.path}")
	private String pluginsPath;

	@Autowired
	private PluginRepository pluginRepository;

	@PostConstruct
	public void checkPluginsDirectory() {
		final File f = new File(pluginsPath);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	@Override
	public void uploadPlugin(Module module, String user, MultipartFile jarFile) {
		final File f = new File(pluginsPath + File.separator + jarFile.getOriginalFilename());
		try {
			// DELETE MATCHING IN DB
			pluginRepository.deleteByModuleAndJarFile(module, jarFile.getOriginalFilename());
			jarFile.transferTo(f);
			final Plugin plugin = new Plugin();
			plugin.setModule(module);
			plugin.setJarFile(jarFile.getOriginalFilename());
			plugin.setPublisher(user);
			pluginRepository.save(plugin);

		} catch (final Exception e) {
			log.error("Could not upload Plugin", e);
		}

	}

	@Override
	public List<PlatformPlugin> getPlugins() {
		return pluginRepository.findAll().stream()
				.map(p -> new PlatformPlugin(p.getId(), p.getJarFile(), p.isLoaded(), p.getPublisher(), p.getModule()))
				.toList();
	}

	@Override
	public List<PlatformPlugin> getPluginsForModule(Module module) {
		if (module == null) {
			return getPlugins();
		}
		return pluginRepository.findByModule(module).stream()
				.map(p -> new PlatformPlugin(p.getId(), p.getJarFile(), p.isLoaded(), p.getPublisher(), p.getModule()))
				.toList();
	}

	@Override
	public File getPluginJAR(String id) {
		final Optional<Plugin> plugin = pluginRepository.findById(id);
		if (plugin.isPresent()) {
			final String jarPath = pluginsPath + File.separator + plugin.get().getJarFile();
			return new File(jarPath);
		}
		return null;
	}

	@Override
	public void setPluginLoaded(String id, boolean loaded) {
		pluginRepository.updateLoadedState(loaded, id);
	}

	@Override
	public PlatformPlugin getPlugin(String id) {
		final Optional<Plugin> p = pluginRepository.findById(id);
		if (p.isPresent()) {
			return new PlatformPlugin(p.get().getId(), p.get().getJarFile(), p.get().isLoaded(), p.get().getPublisher(),
					p.get().getModule());
		}
		return null;
	}

	@Override
	public void deletePlugin(String id) {
		pluginRepository.findById(id).ifPresent(p -> new File(pluginsPath + File.separator + p.getJarFile()).delete());
		pluginRepository.deleteById(id);
	}

}

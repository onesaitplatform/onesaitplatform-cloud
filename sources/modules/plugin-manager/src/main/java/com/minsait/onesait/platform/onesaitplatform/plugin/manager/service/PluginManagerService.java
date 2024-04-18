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
package com.minsait.onesait.platform.onesaitplatform.plugin.manager.service;

import java.io.File;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.plugin.Module;
import com.minsait.onesait.platform.plugin.PlatformPlugin;

public interface PluginManagerService {

	void uploadPlugin(Module module, String user, MultipartFile jarFile);

	PlatformPlugin getPlugin(String id);

	void deletePlugin(String id);

	List<PlatformPlugin> getPlugins();

	List<PlatformPlugin> getPluginsForModule(Module module);

	File getPluginJAR(String id);

	void setPluginLoaded(String id, boolean loaded);

}

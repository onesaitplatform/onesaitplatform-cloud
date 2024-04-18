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
package com.minsait.onesait.platform.config.versioning;

import java.util.List;

import com.minsait.onesait.platform.config.model.interfaces.Versionable;

public interface VersioningIOService {

	public static final String DIR = "/tmp/resources/";

	public static final String CONFIG_MODEL_CLASS_PREFIX = "com.minsait.onesait.platform.config.model.";

	public <T> void serializeToFileSystem(Versionable<T> versionable);

	public <T> void serializeToFileSystem(Versionable<T> versionable, String directory);

	public <T> void restoreFromFileSystem(Versionable<T> versionable);

	public <T> void removeFromFileSystem(Versionable<T> versionable);

	public <T> String absolutePath(Versionable<T> versionable);

	public <T> String relativePath(Versionable<T> versionable);

	public List<Versionable<?>> readAllVersionables(String directory);

	public List<Versionable<?>> readAllVersionables();

	public String getClassNameFromPath(String path);

	public <T> void restoreFromFileSystem(Versionable<T> versionable, String absolutePath);

}

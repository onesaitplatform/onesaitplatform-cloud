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
package com.minsait.onesait.platform.config.services.spark;

import java.util.List;
import java.util.Optional;

import com.minsait.onesait.platform.config.model.SparkLauncherConfig;

public interface SparkLauncherConfigService {

	public void createSparkLauncherConfig(SparkLauncherConfig conf);

	public void deleteSparkLauncherConfig(String id);

	public void updateSparkLauncherConfig(SparkLauncherConfig conf);

	public List<SparkLauncherConfig> listSparkLauncherConfig(String identification, String description);
	
	public Optional<SparkLauncherConfig> getById(String id);
}

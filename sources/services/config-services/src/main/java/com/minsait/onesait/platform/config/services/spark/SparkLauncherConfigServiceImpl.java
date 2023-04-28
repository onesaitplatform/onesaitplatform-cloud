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

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.SparkLauncherConfig;
import com.minsait.onesait.platform.config.repository.SparkLauncherConfigRepository;
import com.minsait.onesait.platform.config.services.exceptions.SparkLauncherConfigException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SparkLauncherConfigServiceImpl implements SparkLauncherConfigService {
	private SparkLauncherConfigRepository sparkLauncherConfigRepo;

	@Override
	public void createSparkLauncherConfig(SparkLauncherConfig conf) {
		sparkLauncherConfigRepo.save(conf);
	}

	@Override
	public void deleteSparkLauncherConfig(String id) {
		sparkLauncherConfigRepo.deleteById(id);
	}

	@Override
	public void updateSparkLauncherConfig(SparkLauncherConfig conf) {

		Optional<SparkLauncherConfig> instance = sparkLauncherConfigRepo.findById(conf.getId());
		if (instance.isPresent()) {
			instance.get().setDescription(conf.getDescription());
			instance.get().setDriverMemory(conf.getDriverMemory());
			instance.get().setDriverExtraClassPath(conf.getDriverExtraClassPath());
			instance.get().setDriverExtraJavaOpts(conf.getDriverExtraJavaOpts());
			instance.get().setDriverExtraLibPath(conf.getDriverExtraLibPath());
			instance.get().setExecutorCores(conf.getExecutorCores());
			instance.get().setExecutorExtraClassPath(conf.getExecutorExtraClassPath());
			instance.get().setExecutorExtraJavaOpts(conf.getExecutorExtraJavaOpts());
			instance.get().setExecutorExtraLibPath(conf.getExecutorExtraLibPath());
			instance.get().setExecutorMemory(conf.getExecutorMemory());
			instance.get().setSparkConfToken(conf.getSparkConfToken());
			sparkLauncherConfigRepo.save(instance.get());
		} else {
			// instasnce not found
			log.error("Error updating SparkLauncher configuration {}. Configuration not found.",
					conf.getIdentification());
			throw new SparkLauncherConfigException("SparkLauncher configuration not found");
		}
	}

	@Override
	public List<SparkLauncherConfig> listSparkLauncherConfig(String identification, String description) {
		if (identification != null) {
			if (description != null) {
				return sparkLauncherConfigRepo.findByIdentificationContainingAndDescriptionContaining(identification,
						description);
			} else {
				return sparkLauncherConfigRepo.findByIdentificationContaining(identification);
			}
		} else {
			if (description != null) {
				return sparkLauncherConfigRepo.findByDescriptionContaining(description);
			}
		}
		return sparkLauncherConfigRepo.findAll();
	}

	@Override
	public Optional<SparkLauncherConfig> getById(String id) {
		return sparkLauncherConfigRepo.findById(id);
	}
}

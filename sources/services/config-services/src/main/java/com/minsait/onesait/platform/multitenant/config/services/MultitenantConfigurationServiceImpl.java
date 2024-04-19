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
package com.minsait.onesait.platform.multitenant.config.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.minsait.onesait.platform.multitenant.config.model.MasterConfiguration;
import com.minsait.onesait.platform.multitenant.config.model.MasterConfiguration.Type;
import com.minsait.onesait.platform.multitenant.config.repository.MasterConfigurationRepository;
import com.minsait.onesait.platform.multitenant.pojo.RTDBConfiguration;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MultitenantConfigurationServiceImpl implements MultitenantConfigurationService {

	@Autowired
	private MasterConfigurationRepository masterConfigurationRepository;

	@Override
	public List<MasterConfiguration> getMasterConfigurations() {
		return masterConfigurationRepository.findAll();
	}

	@Override
	public RTDBConfiguration getMultitenantRTDBConfiguration() {
		final MasterConfiguration configuration = masterConfigurationRepository.findByType(Type.RTDB);
		if (configuration == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			return mapper.readValue(configuration.getYmlConfig(), RTDBConfiguration.class);
		} catch (final IOException e) {
			log.error("Could not deserialize Yaml of Master RTDB configuration");
			return null;
		}
	}

	@Override
	public MasterConfiguration getConfiguration(String id) {
		return masterConfigurationRepository.findById(id).orElse(null);
	}

	@Override
	public void updateConfiguration(MasterConfiguration configuration, String id) {
		final MasterConfiguration dbConfiguration = getConfiguration(id);
		dbConfiguration.setDescription(configuration.getDescription());
		dbConfiguration.setYmlConfig(configuration.getYmlConfig());
		dbConfiguration.setType(configuration.getType());
		masterConfigurationRepository.save(dbConfiguration);
	}

	@Override
	public void createConfiguration(MasterConfiguration configuration) {
		masterConfigurationRepository.save(configuration);

	}

	@Override
	public void deleteConfiguration(String id) {
		masterConfigurationRepository.deleteById(id);
	}

}

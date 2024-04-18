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
package com.minsait.onesait.platform.config.services.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.kafka.KafkaConfigService;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;

@Service
public class KafkaConfigurationServiceImpl implements KafkaConfigService {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profiledetector;

	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getKafkaConfigProperties() {
		Map<String, Object> props = new HashMap<>();

		com.minsait.onesait.platform.config.model.Configuration kafkaClientConfig = configurationService
				.getConfiguration(Type.KAFKA_INTERNAL_CLIENT_PROPERTIES, profiledetector.getActiveProfile(), null);
		if(kafkaClientConfig == null) {
			return null;
		}
		props = configurationService.fromYaml(kafkaClientConfig.getYmlConfig());
	
		//decrypting encrypted properties
				for(Entry<String, Object> entry:props.entrySet()) {
					if(entry.getValue().getClass()==String.class) {
						String propertyVal = (String)entry.getValue();
						if(propertyVal.startsWith("ENC(")) {
							//property is encrypted
							String encrypedValue = propertyVal.substring(4, propertyVal.length()-1);
							String decrytedValue = JasyptConfig.getEncryptor().decrypt(encrypedValue);
							entry.setValue(decrytedValue);
						}
						
					}
				}
		return props;
	}
}

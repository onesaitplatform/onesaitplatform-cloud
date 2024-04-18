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
package com.minsait.onesait.platform.router.config;

import static com.minsait.onesait.platform.encryptor.config.JasyptConfig.JASYPT_BEAN;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.encryptor.config.JasyptConfig;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.kafka", name = "enable", havingValue = "true")
@EnableKafka
@Configuration
@DependsOn(JASYPT_BEAN)
@Slf4j
public class KafkaProducerConfig {
	
	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.brokers:none}")
	private String kafkaBrokers;	

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.host:localhost}")
	private String kafkaHost;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.port:9092}")
	private String kafkaPort;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.partitions:1}")
	int partitions;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.replication:1}")
	short replication;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.prefix:ontology_}")
	private String ontologyPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.group:ontologyGroup}")
	private String ontologyGroup;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.router.topic:router}")
	private String topicRouter;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.user:admin}")
	private String kafkaUser;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.password:admin-secret}")
	private String kafkaPassword;
	
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profiledetector;
	
	private static final String EMPTY_BROKERS = "none";
	private static final String KAFKA_DEFAULTPORT = "9092";	

	@SuppressWarnings("unchecked")
	private Map<String, Object> getKafkaClientPropertiesFromConfig() {
		Map<String, Object> props = new HashMap<>();

		com.minsait.onesait.platform.config.model.Configuration kafkaClientConfig = configurationService
				.getConfiguration(Type.KAFKA_INTERNAL_CLIENT_PROPERTIES, profiledetector.getActiveProfile(), null);
		if(kafkaClientConfig == null) {
			return null;
		}
		props = configurationService.fromYaml(kafkaClientConfig.getYmlConfig());
		if (props.containsKey("group.id")) {
			ontologyGroup = props.get("group.id").toString();
		}
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
						log.info("Reading Kafka props: {}: {}",entry.getKey(), (String)entry.getValue());
					}
				}
		return props;
	}
	
	private Map<String, Object> getKafkaClientPropertiesLegacy(String groupId) {
		Map<String, Object> configProps = new HashMap<>();
		if (!EMPTY_BROKERS.equals(kafkaBrokers)) {
			configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		} else {
			configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
		}
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		applySecurity(configProps);

		return configProps;
	}
	
	private void applySecurity(Map<String, Object> config) {
		if (!kafkaPort.contains(KAFKA_DEFAULTPORT) || kafkaBrokers.contains(KAFKA_DEFAULTPORT)) {
			config.put("security.protocol", "SASL_PLAINTEXT");
			config.put("sasl.mechanism", "PLAIN");

			config.put("sasl.jaas.config",
					"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + kafkaUser
							+ "\" pass"+"word=\"" + kafkaPassword + "\";");
		}
	}

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> props = getKafkaClientPropertiesFromConfig();
		if (props == null) {
			log.info("SETTING UP KAFKA LEGACY CONFIG");
			props = getKafkaClientPropertiesLegacy(ontologyGroup);
		}
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public ProducerFactory<String, NotificationModel> operationFactory() {
		Map<String, Object> props = getKafkaClientPropertiesFromConfig();
		if (props == null) {
			log.info("SETTING UP KAFKA LEGACY CONFIG");
			props = getKafkaClientPropertiesLegacy(ontologyGroup);
		}
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, NotificationModel> operationKafkaTemplate() {
		return new KafkaTemplate<>(operationFactory());
	}

}
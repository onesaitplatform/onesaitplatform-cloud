/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.streaming;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.kafka", name = "enable", havingValue = "true")
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.brokers:none}")
	private String kafkaBrokers;
	
	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.host:localhost}")
	private String kafkaHost;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.port:9092}")
	private String kafkaPort;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.user:admin}")
	private String kafkaUser;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.password:admin-secret}")
	private String kafkaPassword;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.partitions:1}")
	int partitions;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.replication:1}")
	short replication;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.prefix:ontology_}")
	private String ontologyPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.group:ontologyGroup}")
	private String ontologyGroup;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.consumer.maxPollRecords:50}")
	private String maxPollRecords;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.consumer.maxAge:5000}")
	private String maxAge;
	
	private static final String EMPTY_BROKERS = "none";
	private static final String KAFKA_DEFAULTPORT = "9092";	

	public ConsumerFactory<String, String> consumerFactory(String groupId) {
		Map<String, Object> props = new HashMap<>();
		if (!EMPTY_BROKERS.equals(kafkaBrokers)) {
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		} else {	
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
		}
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, maxAge);

		applySecurity(props);

		return new DefaultKafkaConsumerFactory<>(props);
	}

	public ConsumerFactory<String, String> consumerFactoryManualAck(String groupId) {
		Map<String, Object> props = new HashMap<>();
		if (!EMPTY_BROKERS.equals(kafkaBrokers)) {
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		} else {	
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
		}
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, maxAge);

		applySecurity(props);

		return new DefaultKafkaConsumerFactory<>(props);
	}

	public ConsumerFactory<String, String> consumerFactoryBatch(String groupId) {
		Map<String, Object> props = new HashMap<>();
		if (!EMPTY_BROKERS.equals(kafkaBrokers)) {
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		} else {	
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
		}
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
		props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, maxAge);

		applySecurity(props);

		return new DefaultKafkaConsumerFactory<>(props);
	}

	private void applySecurity(Map<String, Object> config) {
		if (!kafkaPort.contains(KAFKA_DEFAULTPORT) || kafkaBrokers.contains(KAFKA_DEFAULTPORT)) {
			config.put("security.protocol", "SASL_PLAINTEXT");
			config.put("sasl.mechanism", "PLAIN");

			config.put("sasl.jaas.config",
					"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + kafkaUser
							+ "\" pass" + "word=\"" + kafkaPassword + "\";");
		}
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory(ontologyGroup));

		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryBatch() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactoryBatch(ontologyGroup));
		factory.setBatchListener(true);
		return factory;
	}

}
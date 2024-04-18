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
package com.minsait.onesait.platform.commons.kafka;

import java.util.Arrays;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.kafka", name = "enable", havingValue = "true")
@Service
@Slf4j
public class KafkaService {

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

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.ksql.in.prefix:intopic_}")
	private String ksqlInTopicPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.ksql.out.prefix:KSQLDESTYNY_}")
	private String ksqlOutTopicPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.group:ontologyGroup}")
	private String ontologyGroup;
	
	private static final String KAFKA_ERROR = "Error processing kafka topic creation";
	
	private static final String CREATING_TOPIC ="Creating topic '{}'";
	private static final String CANNOT_ENSURE_CREATING_TOPIC ="Cannot ensure topic creation for  '{}'";
	
	private static final String KAFKA_DEFAULTPORT = "9092";

	private AdminClient adminAcl;
	
	

	private void applySecurity(Properties config) {
		if (!KAFKA_DEFAULTPORT.equals(kafkaPort)) {
			config.put("security.protocol", "SASL_PLAINTEXT");
			config.put("sasl.mechanism", "PLAIN");

			config.put("sasl.jaas.config",
					"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + kafkaUser
							+ "\" pass"+"word=\"" + kafkaPassword + "\";");
		} else {
			log.info("Kafka configuration with no security applied");
		}
	}

	@PostConstruct
	public void postKafka() {

		try {
			Properties config = new Properties();
			config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost + ":" + kafkaPort);
			applySecurity(config);
			adminAcl = AdminClient.create(config);
		} catch (Exception e) {
			log.error("Something went wrong applying kafka security config, or not established", e);
		}

	}

	public String getTopicName(String ontology) {
		return (ontologyPrefix + ontology).toUpperCase();
	}

	public CreateTopicsResult createTopicWithPrefix(String name, int partitions, short replication) {
		CreateTopicsResult result = null;
		try {
			NewTopic t = new NewTopic(getTopicName(name), partitions, replication);
			result = adminAcl.createTopics(Arrays.asList(t));
		} catch (Exception e) {
			log.error(KAFKA_ERROR, e);
		}
		return result;
	}

	public CreateTopicsResult createTopicWithPrefix(String name) {
		CreateTopicsResult result = null;
		try {
			NewTopic t = new NewTopic(getTopicName(name), partitions, replication);
			result = adminAcl.createTopics(Arrays.asList(t));			
		} catch (Exception e) {
			log.error(KAFKA_ERROR, e);
		}		
		return result;
	}

	public boolean createTopicForOntology(String name) {
		NewTopic t = new NewTopic(getTopicName(name), partitions, replication);
		try {
			log.info("Kafka configuration to be applied, partitions: {}, replication: {} ",partitions,replication);
			log.info(CREATING_TOPIC, getTopicName(name));
			CreateTopicsResult result = adminAcl.createTopics(Arrays.asList(t));
			result.all().get();
			return true;
		} catch (Exception e) {
			log.info(CANNOT_ENSURE_CREATING_TOPIC, getTopicName(name));
			log.error(KAFKA_ERROR, e);
			return false;
		}
	}

	public boolean createTopicForKsqlInput(String name) {

		NewTopic t = new NewTopic(ksqlInTopicPrefix + name, partitions, replication);
		try {
			log.info(CREATING_TOPIC, getTopicName(name));
			CreateTopicsResult result = adminAcl.createTopics(Arrays.asList(t));
			result.all().get();
			return true;
		} catch (Exception e) {
			log.info(CANNOT_ENSURE_CREATING_TOPIC, getTopicName(name));
			return false;
		}
	}

	public boolean createTopic(String name) {

		NewTopic t = new NewTopic(name, partitions, replication);
		try {
			log.info(CREATING_TOPIC, name);
			CreateTopicsResult result = adminAcl.createTopics(Arrays.asList(t));
			result.all().get();
			return true;
		} catch (Exception e) {
			log.info(CANNOT_ENSURE_CREATING_TOPIC, name);
			return false;
		}
	}

	public DeleteTopicsResult deleteTopic(String name) {
		DeleteTopicsResult result = null;
		try {
			result = adminAcl.deleteTopics(Arrays.asList(name));
		} catch (Exception e) {
			log.error("Error processing kafka topic deletion", e);
		}
		return result;
	}

}

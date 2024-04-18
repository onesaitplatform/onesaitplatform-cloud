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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.Resource;
import org.apache.kafka.common.resource.ResourceFilter;
import org.apache.kafka.common.resource.ResourceType;
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
	private String ontologyInputPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.notification.prefix:ontology_output_}")
	private String ontologyNotificationPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.ksql.in.prefix:intopic_}")
	private String ksqlInTopicPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.ksql.out.prefix:KSQLDESTYNY_}")
	private String ksqlOutTopicPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.group:ontologyGroup}")
	private String ontologyGroup;

	private static final String CREATING_TOPIC = "Creating topic '{}'";
	private static final String CANNOT_ENSURE_CREATING_TOPIC = "Cannot ensure topic creation for  '{}'";

	private static final String KAFKA_DEFAULTPORT = "9092";
	private static final String ACL_CREATED = "Creating {} access for user {} to topic {}...";
	private static final String PRINCIPAL = "User:";

	private AdminClient adminAcl;

	private void applySecurity(Properties config) {
		if (!KAFKA_DEFAULTPORT.equals(kafkaPort)) {
			config.put("security.protocol", "SASL_PLAINTEXT");
			config.put("sasl.mechanism", "PLAIN");

			config.put("sasl.jaas.config",
					"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + kafkaUser
							+ "\" pass" + "word=\"" + kafkaPassword + "\";");
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
		return (ontologyInputPrefix + ontology).toUpperCase();
	}

	public String getNotificationTopicName(String ontology) {
		return (ontologyNotificationPrefix + ontology).toUpperCase();
	}

	public boolean createTopic(String name, int partitions, short replication) {

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

	public boolean createInputTopicForOntology(String name) {
		try {
			// chekc if topic exists
			ListTopicsResult topicList = adminAcl.listTopics();
			Set<String> currentTopicList = topicList.names().get();
			for (String existingTopic : currentTopicList) {
				if (existingTopic.equals(getTopicName(name))) {
					return true;
				}
			}
			return createTopic(getTopicName(name), partitions, replication);
		} catch (Exception e) {
			log.info(CANNOT_ENSURE_CREATING_TOPIC, getTopicName(name));
			log.error("Error creating input topic for Ontology", e);
			return false;
		}
	}

	public boolean createNotificationTopicForOntology(String name) {
		try {
			// chekc if topic exists
			ListTopicsResult topicList = adminAcl.listTopics();
			Set<String> currentTopicList = topicList.names().get();
			for (String existingTopic : currentTopicList) {
				if (existingTopic.equals(getNotificationTopicName(name))) {
					return true;
				}
			}
			return createTopic(getNotificationTopicName(name), partitions, replication);
		} catch (Exception e) {
			log.info(CANNOT_ENSURE_CREATING_TOPIC, getTopicName(name));
			log.error("Error creating notification topic for Ontology", e);
			return false;
		}
	}

	public boolean createTopic(String name) {
		return createTopic(name, partitions, replication);
	}

	public boolean createTopicForKsqlInput(String name) {
		return createTopic(ksqlInTopicPrefix + name, partitions, replication);
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

	public void addAcls(String client, String operation, String topic) {
		List<AclBinding> acls = new ArrayList<>();
		Resource kafkaResource = new Resource(ResourceType.TOPIC, topic);

		switch (operation) {
		case "QUERY":
			acls.add(generateAclBinding(PRINCIPAL + client, AclOperation.READ, kafkaResource));
			log.info(ACL_CREATED, AclOperation.READ.name(), client, topic);
			break;
		case "INSERT":
			acls.add(generateAclBinding(PRINCIPAL + client, AclOperation.WRITE, kafkaResource));
			log.info(ACL_CREATED, AclOperation.WRITE.name(), client, topic);
			break;
		case "ALL":
			acls.add(generateAclBinding(PRINCIPAL + client, AclOperation.READ, kafkaResource));
			log.info(ACL_CREATED, AclOperation.READ.name(), client, topic);
			acls.add(generateAclBinding(PRINCIPAL + client, AclOperation.WRITE, kafkaResource));
			log.info(ACL_CREATED, AclOperation.WRITE.name(), client, topic);
			break;
		default:
			break;
		}

		CreateAclsResult result = adminAcl.createAcls(acls);
		try {
			result.all().get(1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("Error while adding ACLs. Principal={}, Operation={}, Topic={}. Cause={}", client, operation,
					topic, e.getMessage());
			throw new KafkaExectionException("Error while adding ACLs for client to the kafka topic.", e);
		}
		log.info("Access granted for user {} to topic {}.", client, topic);

	}

	public void deleteAcls(String client, String topic) {
		List<AclBindingFilter> filters = new ArrayList<>();
		ResourceFilter resourceFilter = new ResourceFilter(ResourceType.TOPIC, topic);
		AccessControlEntryFilter entityFilter = new AccessControlEntryFilter(PRINCIPAL + client, "*", AclOperation.ANY,
				AclPermissionType.ANY);
		AclBindingFilter filter = new AclBindingFilter(resourceFilter, entityFilter);
		filters.add(filter);
		log.info("Deleting ACLs for user {} to topic {}.", client, topic);
		DeleteAclsResult result = adminAcl.deleteAcls(filters);
		try {
			result.all().get(1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("Error while adding ACLs. Principal={}, Topic={}. Cause={}", client, topic, e.getMessage());
			throw new KafkaExectionException("Error while removing ACLs for client to the kafka topic.", e);
		}
		log.info("ACLs successfully deleted for user {} to topic {}.", client, topic);
	}

	private AclBinding generateAclBinding(String principal, AclOperation operation, Resource kafkaResource) {
		AccessControlEntry accessControlEntity = new AccessControlEntry(principal, "*", operation,
				AclPermissionType.ALLOW);
		return new AclBinding(kafkaResource, accessControlEntity);
	}
}

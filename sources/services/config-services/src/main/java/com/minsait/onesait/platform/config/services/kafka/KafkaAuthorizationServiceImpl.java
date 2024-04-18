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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.kafka.KafkaService;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKafkaTopic;
import com.minsait.onesait.platform.config.model.OntologyKafkaTopic.TopicType;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.services.ontologykafkatopic.OntologyKafkaTopicService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaAuthorizationServiceImpl implements KafkaAuthorizationService {

	@Autowired(required = false)
	private KafkaService kafkaService;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private MasterDeviceTokenRepository masterDeviceTokenRepository;
	@Autowired
	private VerticalRepository verticalRepository;
	@Autowired
	private OntologyKafkaTopicService ontologyKafkaTopicService;

	private static final String DASH = "-";
	private static final String KAFKA_DISABLED = "Kafka is not enabled, skipping ACL creation.";

	@Override
	public void checkOntologyAclAfterUpdate(Ontology ontology) {
		if (kafkaService != null) {
			// If kafka topics are enabled
			if (ontology.isAllowsCreateTopic() || ontology.isAllowsCreateNotificationTopic()) {
				// Get all clients
				List<ClientPlatformOntology> clients = clientPlatformOntologyRepository.findByOntology(ontology);
				for (ClientPlatformOntology client : clients) {
					addAclToOntologyClient(client, ontology.isAllowsCreateTopic(),
							ontology.isAllowsCreateNotificationTopic());
				}
			}
			// If kafka topics are disabled
			checkDisabledKafkaTopicsAfterUpdate(ontology);
		} else {
			log.warn(KAFKA_DISABLED);
		}

	}

	private void checkDisabledKafkaTopicsAfterUpdate(Ontology ontology) {
		List<OntologyKafkaTopic> kafkaTopicsForOntology = new ArrayList<>();
		if (!ontology.isAllowsCreateTopic()) {
			// Search if there are input topics for this ontology.
			List<OntologyKafkaTopic> kafkaInputTopicsForOntology = ontologyKafkaTopicService
					.getTopicsByOntologyAndTopicType(ontology, TopicType.INPUT);
			if (kafkaInputTopicsForOntology != null && !kafkaInputTopicsForOntology.isEmpty()) {
				kafkaTopicsForOntology.addAll(kafkaInputTopicsForOntology);
			}
		}
		if (!ontology.isAllowsCreateNotificationTopic()) {
			// Search if there are output topics for this ontology.
			List<OntologyKafkaTopic> kafkaNotifTopicsForOntology = ontologyKafkaTopicService
					.getTopicsByOntologyAndTopicType(ontology, TopicType.OUTPUT);
			if (kafkaNotifTopicsForOntology != null && !kafkaNotifTopicsForOntology.isEmpty()) {
				kafkaTopicsForOntology.addAll(kafkaNotifTopicsForOntology);
			}
		}
		if (!kafkaTopicsForOntology.isEmpty()) {
			// Delete ACLs, Kafka topic and configDB topics related to ontology if there
			// were any
			removeTopicAndAclsFromOntology(ontology, kafkaTopicsForOntology);
		}
	}

	private void removeTopicAndAclsFromOntology(Ontology ontology, List<OntologyKafkaTopic> kafkaTopicsForOntology) {
		List<ClientPlatformOntology> clientOntologyList = clientPlatformOntologyRepository.findByOntology(ontology);
		for (ClientPlatformOntology clientOntology : clientOntologyList) {
			Set<Token> tokens = clientOntology.getClientPlatform().getTokens();
			for (Token token : tokens) {
				MasterDeviceToken masterDeviceToken = masterDeviceTokenRepository.findByTokenName(token.getTokenName());
				String tenant = masterDeviceToken.getTenant();
				String vertical = verticalRepository.findBySchema(masterDeviceToken.getVerticalSchema()).getName();
				// set kafka USER and topic name for each case
				String kafkaUSer = clientOntology.getClientPlatform().getIdentification();
				if (!Tenant2SchemaMapper.defaultTenantName(vertical).equals(tenant)
						|| !Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME.equals(vertical)) {
					kafkaUSer += DASH + vertical + DASH + tenant;
				}
				// For each Kafka User, delete topic ACLs
				for (OntologyKafkaTopic topic : kafkaTopicsForOntology) {
					kafkaService.deleteAcls(kafkaUSer, topic.getIdentification());
				}

			}
		}
		for (OntologyKafkaTopic topic : kafkaTopicsForOntology) {
			// Remove topic from Kafka
			kafkaService.deleteTopic(topic.getIdentification());
			// Remove topic form ConfigDB
			ontologyKafkaTopicService.deleteTopic(topic);
		}
	}

	@Override
	public void addAclToOntologyClient(ClientPlatformOntology clientPlatformOntology, boolean createInputTopic,
			boolean createNotificationTopic) {
		if (kafkaService != null) {
			if (createInputTopic || createNotificationTopic) {
				Set<Token> tokens = clientPlatformOntology.getClientPlatform().getTokens();
				for (Token token : tokens) {
					// For each token validate vertical/tenant
					generateAcls(clientPlatformOntology, token, false, createInputTopic, createNotificationTopic);
				}
			}
		} else {
			log.warn(KAFKA_DISABLED);
		}
	}

	@Override
	public void addAclToOntologyClient(ClientPlatformOntology clientPlatformOntology) {
		addAclToOntologyClient(clientPlatformOntology, clientPlatformOntology.getOntology().isAllowsCreateTopic(),
				clientPlatformOntology.getOntology().isAllowsCreateNotificationTopic());
	}

	@Override
	public void addAclToOntologyOnClientCreation(ClientPlatformOntology clientPlatformOntology, Set<Token> tokens) {
		if (kafkaService != null) {
			if (clientPlatformOntology.getOntology().isAllowsCreateTopic()
					|| clientPlatformOntology.getOntology().isAllowsCreateNotificationTopic()) {
				for (Token token : tokens) {
					// For each token validate vertical/tenant
					generateAcls(clientPlatformOntology, token, true,
							clientPlatformOntology.getOntology().isAllowsCreateTopic(),
							clientPlatformOntology.getOntology().isAllowsCreateNotificationTopic());
				}
			}
		} else {
			log.warn(KAFKA_DISABLED);
		}
	}

	private void generateAcls(ClientPlatformOntology clientPlatformOntology, Token token, boolean clientCreation,
			boolean createInputTopic, boolean createNotificationTopic) {
		String tenant = MultitenancyContextHolder.getTenantName();
		String vertical = verticalRepository.findBySchema(MultitenancyContextHolder.getVerticalSchema()).getName();
		if (!clientCreation) {
			MasterDeviceToken masterDeviceToken = masterDeviceTokenRepository.findByTokenName(token.getTokenName());
			tenant = masterDeviceToken.getTenant();
			vertical = verticalRepository.findBySchema(masterDeviceToken.getVerticalSchema()).getName();
		}
		// set kafka USER and topic name for each case
		String kafkaUSer = clientPlatformOntology.getClientPlatform().getIdentification();
		if (!Tenant2SchemaMapper.defaultTenantName(vertical).equals(tenant)
				|| !Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME.equals(vertical)) {
			kafkaUSer += DASH + vertical + DASH + tenant;
		}

		if (createInputTopic) {
			String topicName = kafkaService.getTopicName(clientPlatformOntology.getOntology().getIdentification(),
					vertical, tenant);
			// Check if topic exists in ConfigDB, if not, add it
			OntologyKafkaTopic ontologyKafkaTopic = ontologyKafkaTopicService.getTopicByIdentification(topicName);
			if (ontologyKafkaTopic == null) {
				ontologyKafkaTopicService.createKafkaTopic(clientPlatformOntology.getOntology(), topicName,
						TopicType.INPUT);
			}
			kafkaService.createInputTopicForOntology(clientPlatformOntology.getOntology().getIdentification(), vertical,
					tenant);
			// Create ACL
			kafkaService.addAcls(kafkaUSer, clientPlatformOntology.getAccess().toString(), topicName);
		}
		if (createNotificationTopic) {
			String topicName = kafkaService.getNotificationTopicName(
					clientPlatformOntology.getOntology().getIdentification(), vertical, tenant);
			// Check if topic exists in ConfigDB, if not, add it
			OntologyKafkaTopic ontologyKafkaTopic = ontologyKafkaTopicService.getTopicByIdentification(topicName);
			if (ontologyKafkaTopic == null) {
				ontologyKafkaTopicService.createKafkaTopic(clientPlatformOntology.getOntology(), topicName,
						TopicType.OUTPUT);
			}
			kafkaService.createNotificationTopicForOntology(clientPlatformOntology.getOntology().getIdentification(),
					vertical, tenant);
			// Create ACL
			kafkaService.addAcls(kafkaUSer, clientPlatformOntology.getAccess().toString(), topicName);
		}
	}

	@Override
	public void removeAclToOntologyClient(ClientPlatformOntology clientPlatformOntology) {

		if (kafkaService != null) {
			if (clientPlatformOntology.getOntology().isAllowsCreateTopic()
					|| clientPlatformOntology.getOntology().isAllowsCreateNotificationTopic()) {
				Set<Token> tokens = clientPlatformOntology.getClientPlatform().getTokens();
				for (Token token : tokens) {
					// For each token validate vertical/tenant
					deleteAcls(clientPlatformOntology, token);
				}
			}
		} else {
			log.warn(KAFKA_DISABLED);
		}
	}

	private void deleteAcls(ClientPlatformOntology clientPlatformOntology, Token token) {
		MasterDeviceToken masterDeviceToken = masterDeviceTokenRepository.findByTokenName(token.getTokenName());
		String tenant = masterDeviceToken.getTenant();
		String vertical = verticalRepository.findBySchema(masterDeviceToken.getVerticalSchema()).getName();
		// set kafka USER and topic name for each case
		String kafkaUSer = clientPlatformOntology.getClientPlatform().getIdentification();
		if (!Tenant2SchemaMapper.defaultTenantName(vertical).equals(tenant)
				|| !Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME.equals(vertical)) {
			kafkaUSer += DASH + vertical + DASH + tenant;
		}

		if (clientPlatformOntology.getOntology().isAllowsCreateTopic()) {
			String topicName = kafkaService.getTopicName(clientPlatformOntology.getOntology().getIdentification(),
					vertical, tenant);
			// Check if topic is no longer needed
			removeTopicIfNotUsed(clientPlatformOntology, masterDeviceToken.getVerticalSchema(), tenant, topicName);
			// Remove ACL
			kafkaService.deleteAcls(kafkaUSer, topicName);
		}
		if (clientPlatformOntology.getOntology().isAllowsCreateNotificationTopic()) {
			String topicName = kafkaService.getNotificationTopicName(
					clientPlatformOntology.getOntology().getIdentification(), vertical, tenant);
			// Check if topic is no longer needed
			removeTopicIfNotUsed(clientPlatformOntology, masterDeviceToken.getVerticalSchema(), tenant, topicName);
			// Remove ACL
			kafkaService.deleteAcls(kafkaUSer, topicName);
		}

	}

	private void removeTopicIfNotUsed(ClientPlatformOntology clientPlatformOntology, String verticalSchema,
			String tenant, String topicName) {
		if (checkTopicRemoval(clientPlatformOntology, verticalSchema, tenant)) {
			// remove topic from ConfigDB
			ontologyKafkaTopicService.deleteTopic(ontologyKafkaTopicService.getTopicByIdentification(topicName));
			// remove topic from kafka
			kafkaService.deleteTopic(topicName);
		}
	}

	private boolean checkTopicRemoval(ClientPlatformOntology clientPlatformOntology, String verticalSchema,
			String tenant) {
		// Is there any other token with same vertical/tenant in a client with that
		// ontology?

		// Get all clients with that ontology
		List<ClientPlatformOntology> clientOntologies = clientPlatformOntologyRepository
				.findByOntology(clientPlatformOntology.getOntology());

		for (ClientPlatformOntology clientOntology : clientOntologies) {
			if (clientOntology.equals(clientPlatformOntology)) {
				Set<Token> clientTokens = clientOntology.getClientPlatform().getTokens();
				for (Token token : clientTokens) {
					MasterDeviceToken masterToken = masterDeviceTokenRepository.findByTokenName(token.getTokenName());
					if (masterToken.getTenant().equals(tenant)
							&& masterToken.getVerticalSchema().equals(verticalSchema)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void addAclToClientForNewToken(Token token) {
		// get all clientOntologies
		List<ClientPlatformOntology> clientPlatformOntologies = clientPlatformOntologyRepository
				.findByClientPlatform(token.getClientPlatform());
		for (ClientPlatformOntology clientPlatformOntology : clientPlatformOntologies) {
			generateAcls(clientPlatformOntology, token, false, clientPlatformOntology.getOntology().isAllowsCreateTopic(),
					clientPlatformOntology.getOntology().isAllowsCreateNotificationTopic());
		}
	}

	@Override
	public void removeAclToClientForToken(Token token) {

		MasterDeviceToken masterDeviceToken = masterDeviceTokenRepository.findByTokenName(token.getTokenName());
		String tenant = masterDeviceToken.getTenant();
		String verticalSchema = masterDeviceToken.getVerticalSchema();
		Set<Token> tokens = token.getClientPlatform().getTokens();
		boolean deleteTopic = true;
		for (Token clientToken : tokens) {
			MasterDeviceToken clientMasterDeviceToken = masterDeviceTokenRepository
					.findByTokenName(clientToken.getTokenName());
			if (clientMasterDeviceToken.getTenant().equals(tenant)
					&& clientMasterDeviceToken.getVerticalSchema().equals(verticalSchema)) {
				// Found other token for same vertical and tenant, so no deletion needed
				deleteTopic = false;
			}
		}
		if (deleteTopic) {
			List<ClientPlatformOntology> clientPlatformOntologies = clientPlatformOntologyRepository
					.findByClientPlatform(token.getClientPlatform());
			for (ClientPlatformOntology clientPlatformOntology : clientPlatformOntologies) {
				deleteAcls(clientPlatformOntology, token);
			}
		}

	}

	@Override
	public void deactivateToken(Token token, boolean active) {

		if (active) {
			// Add ACLs for token
			addAclToClientForNewToken(token);
		} else {
			// Delete ACLS for token
			removeAclToClientForToken(token);
		}
	}

}

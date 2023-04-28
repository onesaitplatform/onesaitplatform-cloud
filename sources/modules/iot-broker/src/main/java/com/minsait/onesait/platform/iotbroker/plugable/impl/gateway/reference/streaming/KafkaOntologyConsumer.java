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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.streaming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.log.interceptor.aop.LogInterceptable;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty(prefix = "onesaitplatform.iotbroker.plugable.gateway.kafka", name = "enable", havingValue = "true")
@Slf4j
@Component
public class KafkaOntologyConsumer {

	@Autowired
	OntologyService ontologyService;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	OntologyDataService ontologyDataService;

	@Autowired
	RouterService routerService;

	@Autowired
	private MultitenancyService multitenancyService;

	ObjectMapper mapper = new ObjectMapper();

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.prefix:ONTOLOGY_}")
	private String ontologyPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.ksql.out.prefix:KSQLDESTYNY_}")
	private String ksqlPrefix;

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.router.topic:router}")
	private String topicRouter;

	@Autowired
	private KafkaTemplate<String, NotificationModel> kafkaTemplate;

	@KafkaListener(topicPattern = "${onesaitplatform.iotbroker.plugable.gateway.kafka.ksql.out.topic.pattern}", containerFactory = "kafkaListenerContainerFactoryBatch")
	@LogInterceptable
	public void listenToParitionKsqlBatch(List<String> data,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.RECEIVED_TOPIC) List<String> receivedTopicList,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		try {
			log.info("listenToParitionKsqlBatch: Start consuming batch messages size: {}", data.size());

			final Map<String, StringBuilder> batchsToInsert = new HashMap<>();

			String comma = "";
			for (int i = 0; i < data.size(); i++) {

				final String receivedTopic = receivedTopicList.get(i);
				final String ontologyId = receivedTopic.substring(ksqlPrefix.length(), receivedTopic.lastIndexOf('_'));

				if (!batchsToInsert.containsKey(ontologyId)) {
					final StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("[");
					comma = "";
					batchsToInsert.put(ontologyId, stringBuilder);
				}

				final StringBuilder sb = batchsToInsert.get(ontologyId);

				final String message = data.get(i);
				sb.append(comma);
				sb.append(message);
				comma = ",";
			}

			final Set<String> ontologies = batchsToInsert.keySet();
			for (final String ontologyFromtopic : ontologies) {
				final String batch = batchsToInsert.get(ontologyFromtopic).append("]").toString();

				final OperationType operationType = OperationType.INSERT;
				final Ontology ontology = ontologyRepository.findByIdentificationIgnoreCase(ontologyFromtopic).get(0);
				final OperationModel model = OperationModel.builder(ontology.getIdentification(),
						OperationType.valueOf(operationType.name()), null, OperationModel.Source.KSQL).body(batch)
						.deviceTemplate("").cacheable(false).build();
				final NotificationModel modelNotification = new NotificationModel();
				modelNotification.setOperationModel(model);
				try {
					routerService.insert(modelNotification);
				} catch (final Exception e) {
					log.error("listenToPartitionBatch:Message ignored by error:", e);
				}
			}
		} catch (final Exception e) {
			log.error("listenToPartitionBatch:Error:", e);
		}

	}

	@KafkaListener(topicPattern = "${onesaitplatform.iotbroker.plugable.gateway.kafka.topic.pattern}", containerFactory = "kafkaListenerContainerFactoryBatch")
	@LogInterceptable
	public void listenToPartitionBatch(List<String> data,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.RECEIVED_TOPIC) List<String> receivedTopicList,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		try {
			log.info("listenToPartitionBatch: Start consuming batch messages size:{} ", data.size());

			final Map<String, StringBuilder> batchsToInsert = new HashMap<>();

			String comma = "";
			for (int i = 0; i < data.size(); i++) {

				String ontologyId = "";
				String vertical = Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME;
				String tenant = Tenant2SchemaMapper.defaultTenantName(vertical);
				final String receivedTopic = receivedTopicList.get(i);
				// Get Ontology, vertical name/schema and tenant.
				String[] topicInfo = receivedTopic.split("-");

				if (topicInfo.length != 1) {
					// We have a tenant that is not the default one
					vertical = topicInfo[1].toLowerCase();
					tenant = topicInfo[2].toLowerCase();
					ontologyId = topicInfo[3];
				} else {
					ontologyId = receivedTopic.replace(ontologyPrefix, "");
				}

				if (!batchsToInsert.containsKey(ontologyId + "-" + vertical + "-" + tenant)) {
					final StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("[");
					comma = "";
					batchsToInsert.put(ontologyId + "-" + vertical + "-" + tenant, stringBuilder);
				}

				final StringBuilder sb = batchsToInsert.get(ontologyId + "-" + vertical + "-" + tenant);

				final String message = data.get(i);
				sb.append(comma);
				sb.append(message);
				comma = ",";

			}

			final Set<String> ontologies = batchsToInsert.keySet();
			for (final String ontologyVerticalTenant : ontologies) {
				final String batch = batchsToInsert.get(ontologyVerticalTenant).append("]").toString();
				String[] ontologyInfo = ontologyVerticalTenant.split("-");
				final String ontologyName = ontologyInfo[0];
				final String verticalName = ontologyInfo[1];
				final String tenantName = ontologyInfo[2];

				Optional<Vertical> vertical = multitenancyService.getVertical(verticalName);
				Optional<Tenant> tenant = multitenancyService.getTenant(tenantName);
				final Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
				if (vertical.isPresent() && tenant.isPresent() && ontology != null) {
					final OperationType operationType = OperationType.INSERT;
					final OperationModel model = OperationModel
							.builder(ontology.getIdentification(), OperationType.valueOf(operationType.name()), null,
									OperationModel.Source.KAFKA)
							.body(batch).deviceTemplate("").cacheable(false).build();
					final NotificationModel modelNotification = new NotificationModel();
					modelNotification.setOperationModel(model);
					try {
						// set Multitenancy context
						MultitenancyContextHolder.setTenantName(tenant.get().getName());
						MultitenancyContextHolder.setVerticalSchema(vertical.get().getSchema());
						routerService.insert(modelNotification);
					} catch (final Exception e) {
						log.error("listenToPartitionBatch:Message ignored by error:", e);
					}
				} else {
					log.debug(
							" Vertical  or Tenant not found. Insertion to ontology will be skipped. Ontology={}, Vertical={}, Tenant={}, Topic={}",
							verticalName, ontology, verticalName, tenant, ontologyVerticalTenant);
				}
			}
		} catch (final Exception e) {
			log.error("listenToPartitionBatch:Error:", e);
		}

	}

	public void sendMessage(NotificationModel message) {
		final ListenableFuture<SendResult<String, NotificationModel>> pp = kafkaTemplate.send(topicRouter, message);
	}

}

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
package com.minsait.onesait.platform.router.service.app.service.advice;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.OntologyKafkaTopic;
import com.minsait.onesait.platform.config.model.OntologyKafkaTopic.TopicType;
import com.minsait.onesait.platform.config.services.ontologykafkatopic.OntologyKafkaTopicService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.router.service.app.service.KafkaTopicOntologyNotificationService;

@Service
public class KafkaTopicOntologyNotificationServiceImpl implements KafkaTopicOntologyNotificationService {

	@Value("${onesaitplatform.iotbroker.plugable.gateway.kafka.notification.prefix:ontology_output_}")
	private String ontologyNotificationPrefix;
	
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private OntologyKafkaTopicService ontologyKafkaTopicService;

	@Override
	public String getKafkaTopicOntologyNotification(String ontologyIdentification) {

		final String verticalSchema = MultitenancyContextHolder.getVerticalSchema();
		String vertical = Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME;
		Optional<Vertical> verticalOpt = multitenancyService.getVertical(verticalSchema);
		if (verticalOpt.isPresent()) {
			vertical = verticalOpt.get().getName();
		}
		final String tenant = MultitenancyContextHolder.getTenantName();
		String topicEnding = ontologyNotificationPrefix + ontologyIdentification;
		if (!vertical.equals(Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME)
				|| !tenant.equals(Tenant2SchemaMapper.defaultTenantName(vertical))) {
			// not default values on tenant or vertical, not default case
			topicEnding = ontologyNotificationPrefix.substring(0,ontologyNotificationPrefix.length()-1) + "-" + vertical + "-" + tenant + "-" + ontologyIdentification;
		}
		topicEnding = topicEnding.toUpperCase();
		// search if any notification topic matches by ending
		OntologyKafkaTopic topic = ontologyKafkaTopicService.getTopicByTypeAndEnding(TopicType.OUTPUT, topicEnding);
		return topic == null ? null : topic.getIdentification();
	}

}

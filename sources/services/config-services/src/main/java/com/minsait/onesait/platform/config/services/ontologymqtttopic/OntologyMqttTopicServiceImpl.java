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
package com.minsait.onesait.platform.config.services.ontologymqtttopic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyMqttTopic;
import com.minsait.onesait.platform.config.repository.OntologyMqttTopicRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyMqttTopicServiceImpl implements OntologyMqttTopicService {

	@Autowired
	private OntologyMqttTopicRepository ontologyMqttTopicRepository;

	@Override
	public OntologyMqttTopic getTopicByOntology(Ontology ontology) {
		return ontologyMqttTopicRepository.findByOntology(ontology);
	}

	@Override
	public void createMqttTopic(Ontology ontology, String identification) {
		if (ontologyMqttTopicRepository.findByOntology(ontology) == null) {
			OntologyMqttTopic topic = new OntologyMqttTopic();
			topic.setIdentification(identification);
			topic.setOntology(ontology);
			ontologyMqttTopicRepository.save(topic);
		}
	}

	@Override
	public void deleteTopic(Ontology ontology) {
		OntologyMqttTopic mqttTopic = ontologyMqttTopicRepository.findByOntology(ontology);
		if (mqttTopic != null) {
			ontologyMqttTopicRepository.delete(mqttTopic);
		}
	}

	@Override
	public OntologyMqttTopic getTopicByIdentification(String identification) {
		return ontologyMqttTopicRepository.findByIdentification(identification);
	}

}
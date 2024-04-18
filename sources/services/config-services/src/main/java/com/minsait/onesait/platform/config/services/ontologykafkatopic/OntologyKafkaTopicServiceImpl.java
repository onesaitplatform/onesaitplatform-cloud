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
package com.minsait.onesait.platform.config.services.ontologykafkatopic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyKafkaTopic;
import com.minsait.onesait.platform.config.model.OntologyKafkaTopic.TopicType;
import com.minsait.onesait.platform.config.repository.OntologyKafkaTopicRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyKafkaTopicServiceImpl implements OntologyKafkaTopicService {
	
	@Autowired
	private OntologyKafkaTopicRepository ontologyKafkaTopicRepository;
	
	@Override
	public List<OntologyKafkaTopic> getTopicsByOntology(Ontology ontology) {
		return ontologyKafkaTopicRepository.findByOntology(ontology);
	}
	
	@Override
	public List<OntologyKafkaTopic> getTopicsByOntologyAndTopicType(Ontology ontology, TopicType topicType) {
		return ontologyKafkaTopicRepository.findByOntologyAndTopicType(ontology, topicType);
	}
	
	@Override
	public void createKafkaTopic(Ontology ontology, String identification, TopicType type) {
		OntologyKafkaTopic topic = new OntologyKafkaTopic();
		topic.setIdentification(identification);
		topic.setOntology(ontology);
		topic.setTopicType(type);
		ontologyKafkaTopicRepository.save(topic);
	}

	@Override
	public void deleteTopic(OntologyKafkaTopic topic) {
		ontologyKafkaTopicRepository.delete(topic);
	}

	@Override
	public OntologyKafkaTopic getTopicByIdentification(String identification) {
		return ontologyKafkaTopicRepository.findByIdentification(identification);
	}
	
	@Override
	public OntologyKafkaTopic getTopicByTypeAndEnding(TopicType type, String topicEnding) {
		return ontologyKafkaTopicRepository.findByTopicTypeAndIdentificationEndingWith(type, topicEnding);
	}

}

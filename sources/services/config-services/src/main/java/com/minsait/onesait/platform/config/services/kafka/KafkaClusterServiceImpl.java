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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.config.model.KafkaClusterInstance;
import com.minsait.onesait.platform.config.repository.KafkaClusterInstanceRepository;
import com.minsait.onesait.platform.config.services.exceptions.KafkaClusterServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaClusterServiceImpl implements KafkaClusterService {

	@Autowired
	private KafkaClusterInstanceRepository kafkaClusterRepository;

	@Override
	public void createClusterConnection(KafkaClusterInstance kafkaClusterInstance) {
		// if there are no servers defined, then throw exception
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj;
		try {
			JsonNode bootstrapServers=null;
			actualObj = mapper.readTree(kafkaClusterInstance.getKafkaConfig());
			ArrayNode array = ((ArrayNode)actualObj);
			 for (JsonNode jsonNode : array) {
				 if(jsonNode.get("bootstrap.servers")!=null) {
					 bootstrapServers=jsonNode;
					 break;
				 }
			 }
			if (bootstrapServers == null) {
				log.error(
						"Error creating Kafka cluster instance {}. Instance \"bootstrap.servers\" property not found.",
						kafkaClusterInstance.getIdentification());
				throw new KafkaClusterServiceException(
						"Kafka cluster instance \"bootstrap.servers\" property not found.");
			}
			kafkaClusterRepository.save(kafkaClusterInstance);
		} catch (IOException e) {
			log.error("Error creating Kafka cluster instance. Instance={}, Cause={}, Message={}",
					kafkaClusterInstance.getIdentification(), e.getCause(), e.getMessage());
			throw new KafkaClusterServiceException("Kafka cluster instance \"bootstrap.servers\" property not found.",
					e);
		}
	}

	@Override
	public void deleteClusterConnection(String id) {
		kafkaClusterRepository.deleteById(id);
	}

	@Override
	public void updateClusterConnection(KafkaClusterInstance kafkaClusterInstance) {
		Optional<KafkaClusterInstance> instance = kafkaClusterRepository.findById(kafkaClusterInstance.getId());
		if (instance.isPresent()) {
			instance.get().setDescription(kafkaClusterInstance.getDescription());
			instance.get().setKafkaConfig(kafkaClusterInstance.getKafkaConfig());
			kafkaClusterRepository.save(instance.get());
		} else {
			// instasnce not found
			log.error("Error updating Kafka cluster instance {}. Instance not found.",
					kafkaClusterInstance.getIdentification());
			throw new KafkaClusterServiceException("Kafka cluster instance not found");
		}
	}

	@Override
	public List<KafkaClusterInstance> listClusterConnections(String identification, String description) {
		if (identification != null) {
			if (description != null) {
				return kafkaClusterRepository.findByIdentificationContainingAndDescriptionContaining(identification, description);
			} else {
				return kafkaClusterRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (description != null) {
				return kafkaClusterRepository.findByDescriptionContaining(description);
			}
		}
		return kafkaClusterRepository.findAll();
	}
	
	@Override
	public Optional<KafkaClusterInstance> getById(String id) {
		return kafkaClusterRepository.findById(id);
	}

}

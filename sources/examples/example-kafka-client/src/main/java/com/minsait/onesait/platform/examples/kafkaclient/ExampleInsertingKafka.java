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
package com.minsait.onesait.platform.examples.kafkaclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ExampleInsertingKafka {
	
	private static final Logger log = LoggerFactory.getLogger(ExampleInsertingKafka.class);
	
	private static String url = "localhost:9095";
	private static String token = "02148604a7ed4a4986c973513d35cca3";
	private static String deviceTemplate = "KafkaInserts";
	private static String prefix = "ONTOLOGY_";
	private static String ontology = "EXAMPLE_KAFKA";
	
	private static Properties createConfig(String token, String clientPlatform) {
		Properties config = new Properties();
		config.put(ProducerConfig.CLIENT_ID_CONFIG, "localhost");
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
		config.put("security.protocol", "SASL_PLAINTEXT");
		config.put("sasl.mechanism", "PLAIN");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
				+ clientPlatform + "\" password=\"" + token + "\";");
		return config;
	}
	
	private static JsonNode getInstances() throws IOException {
		ClassLoader classLoader = ExampleInsertingKafka.class.getClassLoader();
		InputStream dataInputStream = classLoader.getResourceAsStream("data.json");
		final ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = mapper.readTree(dataInputStream);
		
		return rootNode;
	}
	
	private static void insertInstance(KafkaProducer<String, String> producer, String ontology, String instance, String prefix) {
		
		try {
			producer.send(new ProducerRecord<String, String>(prefix + ontology, instance));		
			//metadata.get();
		} catch (Exception e) {
			throw new RuntimeException("Error inserting data with kafka", e);
		}

	}
	
	public static void main (String...args) throws IOException, InterruptedException {
		
		log.info("Starting insert example...");
		Properties config = createConfig(token, deviceTemplate);
		log.info("Configuraion stablished");
		
		KafkaProducer<String, String> producer = new KafkaProducer<>(config);
		log.info("Kafka producer created");
		
		JsonNode allData = getInstances();
		log.info("Example instances loaded");
		
		Consumer<JsonNode> insertInstance = new Consumer<JsonNode>() {
			@Override
			public void accept(JsonNode instance) {
				String instanceString = instance.toString();
				insertInstance(producer, ontology, instanceString, prefix);
				log.info("instance inserted - {}: {}", new Date().getTime(), instanceString);
			}
		};
		
		if (allData.isArray()) {
			ArrayNode arrayInstances = (ArrayNode) allData;
			arrayInstances.forEach(insertInstance);
		}		
		
		producer.flush();
		producer.close();
	}
}
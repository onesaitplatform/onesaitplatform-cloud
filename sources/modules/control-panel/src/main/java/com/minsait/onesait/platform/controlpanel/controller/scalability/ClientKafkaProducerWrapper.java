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
package com.minsait.onesait.platform.controlpanel.controller.scalability;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientKafkaProducerWrapper implements Client {

	KafkaProducer<String, String> producer;
	Properties config;
	String url;
	String prefix = "ontology_";

	public ClientKafkaProducerWrapper(String url) {
		createClient(url);

	}

	@Override
	public void createClient(String url) {
		this.url = url;
		config = new Properties();
		config.put(ProducerConfig.CLIENT_ID_CONFIG, "localhost");
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
		config.put("security.protocol", "SASL_PLAINTEXT");
		config.put("sasl.mechanism", "PLAIN");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
	}

	@Override
	public void connect(String token, String clientPlatform, String clientPlatformInstance,
			boolean avoidSSLValidation) {
		config.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
				+ clientPlatform + "\" pass" + "word=\"" + token + "\";");
		producer = new KafkaProducer<>(config);
	}

	@Override
	public void insertInstance(String ontology, String instance) {
		Future<RecordMetadata> metadata = producer
				.send(new ProducerRecord<String, String>(prefix + ontology, instance));
		try {
			metadata.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error inserting message", e);
			throw new GenericRuntimeOPException("Error inserting data with kafka", e);
		}
		// producer.flush();
	}

	@Override
	public void disconnect() {
		producer.close();
	}

	@Override
	public String getProtocol() {
		return "kafka";
	}

}

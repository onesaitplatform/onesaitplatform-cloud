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
package com.minsait.onesait.platform.videobroker.kafkaclient;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaClientTest {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	private final String prefix = "ONTOLOGY_";
	private final String ontology = "VideoResults";
	private final String instance = "{\"VideoResults\":{\"name\":\"test\"}}";

	@Test
	public void insert() {
		final Future<SendResult<String, String>> metadata = kafkaTemplate
				.send(new ProducerRecord<String, String>(prefix + ontology, instance));
		try {
			metadata.get();
		} catch (final Exception e) {
		}

	}

}

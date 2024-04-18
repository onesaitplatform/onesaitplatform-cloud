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
package com.minsait.onesait.platform.videobroker.kafka;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaResultsHandler {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	private BlockingQueue<VideoProcessorResults> resultsQueue;
	@Value("${onesaitplatform.kafka.prefix:ONTOLOGY_}")
	private String prefix;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private TaskExecutor taskExecutor;

	@PostConstruct
	public void start() {
		taskExecutor.execute(() -> {

			while (true) {
				try {
					final VideoProcessorResults res = resultsQueue.take();
					if (res != null) {
						insertInstance(res);
					}
				} catch (final InterruptedException e) {
					log.error("Interrupted Thread", e);
					Thread.currentThread().interrupt();
				} catch (final JsonProcessingException e) {
					log.error("Malformed JSON");
				}

			}

		});

	}

	private void insertInstance(VideoProcessorResults results) throws JsonProcessingException {
		final Future<SendResult<String, String>> metadata = kafkaTemplate.send(
				new ProducerRecord<String, String>(prefix + results.getOntology(), mapper.writeValueAsString(results)));
		try {
			metadata.get();
		} catch (final Exception e) {
			log.error("Error inserting instance {}", mapper.writeValueAsString(results));
		}
	}

}

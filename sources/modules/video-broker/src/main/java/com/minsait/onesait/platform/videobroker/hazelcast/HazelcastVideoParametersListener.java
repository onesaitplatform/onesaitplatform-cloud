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
package com.minsait.onesait.platform.videobroker.hazelcast;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.hazelcast.core.IQueue;
import com.minsait.onesait.platform.videobroker.service.CaptureManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HazelcastVideoParametersListener {

	@Autowired
	@Qualifier("videoQueue")
	private IQueue<String> videoQueue;
	@Autowired
	private CaptureManager captureManager;
	@Autowired
	private TaskExecutor threadPoolTaskExecutor;

	@PostConstruct
	public void init() {
		threadPoolTaskExecutor.execute(() -> {
			while (true) {
				try {
					final String id = videoQueue.take();
					log.info("New event: {}", id);
					newEvent(id);
				} catch (final InterruptedException e) {
					log.error("Error while retrieving object of type VideoParameters");
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	private void newEvent(String id) {
		captureManager.manageThread(id);
	}

}

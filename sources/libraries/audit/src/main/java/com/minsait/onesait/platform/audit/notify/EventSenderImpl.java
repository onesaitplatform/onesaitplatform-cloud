/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.audit.notify;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventSenderImpl implements EventRouter {

	public static final String AUDIT_QUEUE_NAME = "auditQueue";
	public static final int EXECUTOR_THREADS = 5;

	@Value("${onesaitplatform.audit.global.notify:true}")
	private boolean sendAudit;

	@Autowired(required = false)
	@Qualifier("globalCache")
	private HazelcastInstance instance;

	private ExecutorService senderExecutor;

	@PostConstruct
	public void init() {
		senderExecutor = Executors.newFixedThreadPool(EXECUTOR_THREADS);
	}

	@PreDestroy
	public void destroy() {
		senderExecutor.shutdown();
	}

	@Override
	public void notify(String event) {
		if (sendAudit) {
			log.debug("Received Audit Event: {} ", event);
			if (instance != null) {
				senderExecutor.execute(() -> {
					try {
						instance.getQueue(AUDIT_QUEUE_NAME).offer(event);
					} catch (final Exception e4) {
						log.error("Error notifing Adit evento to Hazelcast queue", e4);
					}
				});
			}
		}
	}

	/*
	 * @Override public void notify(Sofia2AuditEvent event) { log.
	 * info("EventSenderImpl :: thread '{}' handling '{}' Notify to Router The Event: "
	 * , Thread.currentThread(), event.getMessage());
	 * instance.getQueue("audit").offer(event.toJson());
	 *
	 * }
	 */

}

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
package com.minsait.onesait.platform.router.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.minsait.onesait.platform.audit.notify.EventSenderImpl;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.transaction.OntologyStatus;
import com.minsait.onesait.platform.router.transaction.operation.Transaction;

@Configuration
public class HazelcastCacheConfig {

	@Bean(name = "disconectedClientsQueue")
	public IQueue<String> hazelcastDisconectedClientsQueue() {
		return hazelcastInstance.getQueue("disconectedClientsQueue");
	}

	@Bean(name = "disconectedClientsSubscription")
	public IQueue<String> hazelcastDisconectedClientsSubscription() {
		return hazelcastInstance.getQueue("disconectedClientsSubscription");
	}

	@Bean(name = "auditQueue")
	public IQueue<String> hazelcastAuditQueue() {
		return hazelcastInstance.getQueue(EventSenderImpl.AUDIT_QUEUE_NAME);
	}

	@Bean(name = "transactionalOperations")
	public IMap<String, Transaction> transactionalOperations() {
		return hazelcastInstance.getMap("transactionalOperations");
	}

	@Bean(name = "lockedOntologies")
	public IMap<String, OntologyStatus> lockedOntologies() {
		return hazelcastInstance.getMap("lockedOntologies");
	}
	
	@Bean(name = "notificationAdviceNodeRED")
	public IQueue<NotificationCompositeModel> notificationAdviceNodeRED() {
		return hazelcastInstance.getQueue("notificationAdviceNodeRED");
	}

	@Autowired
	HazelcastInstance hazelcastInstance;

}

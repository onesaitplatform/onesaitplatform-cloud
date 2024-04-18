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
package com.minsait.onesait.platform.commons.kafka.integration;

import lombok.Getter;

public class KafkaIntegrationServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public enum KafkaIntegrationServiceExceptionType {
		CONNECTION, GETINFO, LIST, CREATE, UPDATE, DELETE, CONSUME, PRODUCE, PURGE;
	}

	public enum KafkaIntegrationServiceExceptionElement {
		CLUSTER, TOPIC, GROUP, CONSUMER, PRODUCER;
	}

	@Getter
	private KafkaIntegrationServiceExceptionType type;
	@Getter
	private KafkaIntegrationServiceExceptionElement element;

	public KafkaIntegrationServiceException(String message, Exception e,
			KafkaIntegrationServiceExceptionElement element, KafkaIntegrationServiceExceptionType type) {
		super(message, e);
		this.type = type;
		this.element = element;
	}

	public KafkaIntegrationServiceException(String message, KafkaIntegrationServiceExceptionElement element,
			KafkaIntegrationServiceExceptionType type) {
		super(message);
		this.type = type;
		this.element = element;
	}
}

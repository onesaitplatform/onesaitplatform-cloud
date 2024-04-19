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
package com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consumergroup;

import org.apache.kafka.clients.admin.MemberAssignment;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.common.TopicPartition;

import lombok.Getter;
import lombok.Setter;

public class MemberDescriptionDTO {
	@Getter
	@Setter
	private String consumerId;
	@Getter
	@Setter
	private String clientId;
	@Getter
	@Setter
	private String host;
	@Getter
	@Setter
	private MemberAssignment assignment;

	public String getAssignments() {
		StringBuilder assignmentText = new StringBuilder();
		for (TopicPartition p : assignment.topicPartitions()) {
			assignmentText.append(", ").append(p.topic()).append("-").append(p.partition());
		}
		if(assignmentText.length()>=2) {
			assignmentText.delete(0, 2);
		}
		return assignmentText.toString();
	}
	public MemberDescriptionDTO(MemberDescription m) {
		consumerId = m.consumerId();
		clientId=m.clientId();
		host=m.host();
		assignment=m.assignment();
	}
}

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
package com.minsait.onesait.platform.controlpanel.controller.kafka.dto.consumergroup;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.common.ConsumerGroupState;

import lombok.Getter;
import lombok.Setter;

public class ConsumerGroupDescriptionDTO {
	@Getter
	@Setter
	private String groupId;
	@Getter
	@Setter
	private List<MemberDescriptionDTO> members;
	@Getter
	@Setter
	private ConsumerGroupState state;

	public ConsumerGroupDescriptionDTO(ConsumerGroupDescription consumerDesc) {
		groupId = consumerDesc.groupId();
		state = consumerDesc.state();
		members = new ArrayList<>();
		for (MemberDescription m : consumerDesc.members()) {
			members.add(new MemberDescriptionDTO(m));
		}
	}

}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.kafka.dto.cluster.status;

import java.util.List;

import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.common.Node;

import com.minsait.onesait.platform.controlpanel.controller.kafka.dto.connections.KakfaClusterConnectionDTO;

import lombok.Getter;
import lombok.Setter;

public class KafkaClusterStatusDTO {
	@Getter
	@Setter
	private KakfaClusterConnectionDTO connection;
	@Getter
	@Setter
	private List<Node> brokerNodes;
	@Getter
	@Setter
	private List<String> topics;
	@Getter
	@Setter
	private List<ConsumerGroupListing> consumerGroups;
}

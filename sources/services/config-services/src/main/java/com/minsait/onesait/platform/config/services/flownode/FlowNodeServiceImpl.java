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
package com.minsait.onesait.platform.config.services.flownode;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.MessageType;
import com.minsait.onesait.platform.config.model.NotificationEntity;
import com.minsait.onesait.platform.config.repository.FlowNodeRepository;
import com.minsait.onesait.platform.config.services.exceptions.FlowNodeServiceException;

@Service
public class FlowNodeServiceImpl implements FlowNodeService {

	@Autowired
	private FlowNodeRepository nodeRepository;

	@Override
	public List<FlowNode> getAllFlowNodes() {
		return nodeRepository.findAll();
	}

	@Override
	public FlowNode createFlowNode(FlowNode flowNode) {
		List<FlowNode> result = nodeRepository.findByNodeRedNodeId(flowNode.getNodeRedNodeId());
		if (result == null || result.isEmpty()) {
			return nodeRepository.save(flowNode);
		} else {
			throw new FlowNodeServiceException("Flow node already exists.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<NotificationEntity> getNotificationsByOntologyAndMessageType(String ontology, String messageType) {
		return (List<NotificationEntity>) (List<? extends NotificationEntity>) nodeRepository
				.findNotificationByOntologyAndMessageType(ontology, MessageType.valueOf(messageType));
	}

}

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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.MessageType;

public interface FlowNodeRepository extends JpaRepository<FlowNode, String> {

	List<FlowNode> findByNodeRedNodeId(String nodeRedNodeId);

	List<FlowNode> findByFlow_NodeRedFlowId(String nodeRedFlowId);

	List<FlowNode> findByflowNodeType(String flowNodeType);

	@Cacheable(cacheNames = "FlowNodeRepositoryByOntologyAndMessageType", key = "#p0.concat('-').concat(#p1.name())")
	@Query("SELECT N FROM FlowNode N "
			+ "WHERE N.flowNodeType = 'HTTP_NOTIFIER' AND N.ontology.identification = :ontology AND N.messageType = :messageType")
	List<FlowNode> findNotificationByOntologyAndMessageType(@Param("ontology") String ontology,
			@Param("messageType") MessageType messageType);

	@Override
	@CacheEvict(cacheNames = "FlowNodeRepositoryByOntologyAndMessageType", allEntries = true)
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = "FlowNodeRepositoryByOntologyAndMessageType", allEntries = true)
	@Transactional
	void delete(FlowNode entity);

	@Override
	@CacheEvict(cacheNames = "FlowNodeRepositoryByOntologyAndMessageType", allEntries = true)
	<S extends FlowNode> S save(S flow);

	@Override
	@CacheEvict(cacheNames = "FlowNodeRepositoryByOntologyAndMessageType", allEntries = true)
	void flush();
}

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
package com.minsait.onesait.platform.config.repository;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.dto.PipelineForList;
import com.minsait.onesait.platform.config.model.DataflowInstance;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.User;

public interface PipelineRepository extends JpaRepository<Pipeline, String> {

	@Override
	List<Pipeline> findAll();

	Pipeline findByIdentification(String pipelineId);

	List<Pipeline> findByUser(User user);

	List<Pipeline> findByInstance(DataflowInstance instance);

	@Query("SELECT o FROM Pipeline AS o WHERE (o.user=:user OR o.isPublic=TRUE OR o.id IN (SELECT uo.pipeline.id FROM PipelineUserAccess AS uo WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<Pipeline> findByUserAndAccess(@Param("user") User user);

	@Query("SELECT o.identification FROM Pipeline AS o WHERE (o.user=:user OR o.id IN (SELECT uo.pipeline.id FROM PipelineUserAccess AS uo WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<String> findIdentificationsByUserAndAccess(@Param("user") User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, 'null', o.createdAt, o.updatedAt, o.user, 'DATAFLOW', 0) FROM Pipeline AS o WHERE (o.user=:user OR o.id IN (SELECT uo.pipeline.id FROM PipelineUserAccess AS uo WHERE uo.user=:user)) AND o.identification like %:identification% ORDER BY o.identification ASC")
	List<OPResourceDTO> findDtoByUserAndPermissions(@Param("user") User user,
			@Param("identification") String identification);

	@Query("SELECT o.identification FROM Pipeline AS o ORDER BY o.identification ASC")
	List<String> findIdentifications();

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, 'null', o.createdAt, o.updatedAt, o.user, 'DATAFLOW', 0) FROM Pipeline AS o WHERE o.identification like %:identification% ORDER BY o.identification ASC")
	List<OPResourceDTO> findAllDto(@Param("identification") String identification);

	List<Pipeline> findByIdentificationAndIdstreamsets(String pipelineId, String idstreamsets);

	Pipeline findByIdstreamsets(String idstreamsets);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.PipelineForList(o.id, o.identification, o.idstreamsets, o.user, o.isPublic, 'null') " + "FROM Pipeline AS o ")
	List<PipelineForList> findAllPipelineList();

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}

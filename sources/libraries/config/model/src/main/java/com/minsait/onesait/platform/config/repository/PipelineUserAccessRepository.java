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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.PipelineUserAccessType;
import com.minsait.onesait.platform.config.model.User;

public interface PipelineUserAccessRepository extends JpaRepository<PipelineUserAccess, String> {

	@SuppressWarnings("unchecked")
	@Override
	PipelineUserAccess save(PipelineUserAccess entity);

	List<PipelineUserAccess> findAllByOrderById();

	@Query("SELECT o FROM PipelineUserAccess AS o WHERE o.pipeline=:id")
	List<PipelineUserAccess> findByPipeline(@Param("id") Pipeline id);

	@Query("SELECT o FROM PipelineUserAccess AS o WHERE o.pipeline=:id AND o.user=:user")
	PipelineUserAccess findByPipelineAndUser(@Param("id") Pipeline id, @Param("user") User user);

	@Query("SELECT o FROM PipelineUserAccess AS o WHERE o.pipeline=:id AND o.user=:user AND o.pipelineUserAccessType=:pipelineUserAccessType")
	PipelineUserAccess findByPipelineAndUserAndAccess(@Param("id") Pipeline id, @Param("user") User user,
			@Param("pipelineUserAccessType") PipelineUserAccessType pipelineUserAccessType);

}

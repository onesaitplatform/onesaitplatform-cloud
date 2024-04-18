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
package com.minsait.onesait.platform.config.repository;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;

public interface FlowRepository extends JpaRepository<Flow, String> {

	List<Flow> findByIdentification(String identification);

	List<Flow> findByFlowDomain_Identification(String domainIdentification);

	Flow findByNodeRedFlowId(String nodeRedFlowId);
	
	@Query("SELECT f FROM Flow f WHERE f.identification= :identification OR f.id= :identification")
	Flow findByIdentificationOrId(@Param("identification") String identification);

	@Query("SELECT f FROM Flow f WHERE f.flowDomain.user= :#{#user}")
	List<Token> findByUser(User user);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}

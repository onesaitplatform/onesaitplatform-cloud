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

import com.minsait.onesait.platform.config.model.FlowDomain;

public interface FlowDomainRepository extends JpaRepository<FlowDomain, String> {

	FlowDomain findByIdentification(String identification);

	FlowDomain findByUserUserId(String userId);

	@Query("SELECT d.port FROM FlowDomain as d")
	List<Integer> findAllDomainPorts();

	@Query("SELECT d.servicePort FROM FlowDomain as d")
	List<Integer> findAllServicePorts();

	@Modifying
	@Transactional
	void deleteByIdentification(String identification);

	@Modifying
	@Transactional
	@Query("UPDATE FlowDomain d SET d.autorecover = :autorecover WHERE d.id = :id")
	void saveAutorecover(@Param("autorecover") boolean autorecover, @Param("id") String id);

	@Modifying
	@Transactional
	@Query("UPDATE FlowDomain d SET d.state = :state WHERE d.id = :id")
	void saveState(@Param("state") String state, @Param("id") String id);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}

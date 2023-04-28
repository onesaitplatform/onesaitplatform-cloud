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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.User;

public interface MicroserviceRepository extends JpaRepository<Microservice, String> {

	Microservice findByIdentificationAndActiveTrue(String identification);

	List<Microservice> findByUserAndActiveTrue(User user);

	List<Microservice> findByActiveTrue();

	@Override
	<S extends Microservice> S save(S entity);

	@Modifying
	@Transactional
	@Query("UPDATE Microservice m SET m.jenkinsQueueId= NULL")
	int updateMicroserviceSetJenkinsQueueIdNull();

	@Modifying
	@Transactional
	@Query("UPDATE Microservice m SET m.jenkinsQueueId= NULL WHERE m.id= :id")
	int updateMicroserviceSetJenkinsQueueIdNullByMicroserviceId(@Param("id") String id);

	@Query("SELECT m.identification FROM Microservice AS m ORDER BY m.identification ASC")
	List<String> findAllIdentifications();

	@Query("SELECT m.identification FROM Microservice AS m WHERE m.user=:user ORDER BY m.identification ASC")
	List<String> findAllIdentificationsByUser(@Param("user") User user);
}

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

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

public interface OPResourceRepository extends JpaRepository<OPResource, String> {

	@Override
	List<OPResource> findAll();

	List<OPResource> findByIdentificationContainingIgnoreCase(String identification);

	List<OPResource> findByIdentification(String identification);

	List<OPResource> findByIdentificationContainingIgnoreCaseAndUser(String identification, User user);

	List<OPResource> findByUser(User user);

	@Modifying
	@Transactional
	@Query("DELETE FROM OPResource a WHERE a.user.userId = :userId")
	void deleteByUser(@Param("userId") String userId);


}

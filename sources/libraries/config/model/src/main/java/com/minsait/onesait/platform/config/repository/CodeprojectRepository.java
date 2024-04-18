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

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.CodeProject;
import com.minsait.onesait.platform.config.model.User;

public interface CodeprojectRepository extends JpaRepository<CodeProject, String> {

	CodeProject findByIdentificationAndActiveTrue(String identification);

	List<CodeProject> findByUserAndActiveTrue(User user);

	List<CodeProject> findByActiveTrue();

	@Override
	<S extends CodeProject> S save(S entity);

	@Query(value = "SELECT * FROM code_project cp WHERE cp.active = true AND cp.identification LIKE %:desc% OR cp.name LIKE %:desc%", nativeQuery = true)
	List<CodeProject> findByDescActiveTrue(@Param("desc") String desc);

	CodeProject findByIdentification(String identification);
}

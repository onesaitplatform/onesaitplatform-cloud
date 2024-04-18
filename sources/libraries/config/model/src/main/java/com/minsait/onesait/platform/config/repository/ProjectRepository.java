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

import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.User;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, String> {

	@SuppressWarnings("unchecked")
	@Override
	Project save(Project project);

	@Override
	void delete(Project project);
	
	@Query("SELECT o FROM ProjectList AS o")
	public List<ProjectList> findAllForList();

	public List<Project> findByUsersIn(List<User> users);

	public List<Project> findByIdentification(String identification);

	@Query("SELECT o FROM ProjectList AS o Where o.id = :id")
	public List<ProjectList> findByIdForList(@Param("id") String id);

	@Query("SELECT o FROM ProjectList AS o Where o.identification = :identification")
	public List<ProjectList> findByIdentificationForList(@Param("identification") String identification);
	
}

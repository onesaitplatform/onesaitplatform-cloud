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

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

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

	List<Project> findByUser(User user);

	@Modifying
	@Transactional
	@Query("DELETE FROM ProjectList AS p WHERE p.id NOT IN :ids")
	void deleteByIdNotInCustom(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM PROJECT_RESOURCE_ACCESS WHERE PROJECT_RESOURCE_ACCESS.PROJECT_ID NOT IN :ids", nativeQuery = true)
	void deleteProjectResourceAccessWhereIdNotIn(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM PROJECT_RESOURCE_ACCESS WHERE PROJECT_RESOURCE_ACCESS.PROJECT_ID= :id", nativeQuery = true)
	void deleteProjectResourceAccessWhereIdIs(@Param("id") String id);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM USER_PROJECT WHERE USER_PROJECT.PROJECT_ID NOT IN :ids", nativeQuery = true)
	void deleteProjectUsersWhereIdNotIn(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM USER_PROJECT WHERE USER_PROJECT.PROJECT_ID= :id", nativeQuery = true)
	void deleteProjectUsersWhereIdIs(@Param("id") String id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE APP SET PROJECT_ID=NULL WHERE PROJECT_ID NOT IN :ids", nativeQuery = true)
	void setAppProjectToNullWhereIdNotIn(@Param("ids") Collection<String> ids);

	@Modifying
	@Transactional
	@Query(value = "UPDATE APP SET PROJECT_ID=NULL WHERE PROJECT_ID= :id", nativeQuery = true)
	void setAppProjectToNullWhereIdIs(@Param("id") String id);

	@Modifying
	@Transactional
	@Query("DELETE FROM ProjectList AS p WHERE p.id= :id")
	void deleteByIdCustom(@Param("id") String id);

	@Modifying
	@Transactional
	default void deleteByIdNotIn(Collection<String> ids) {
		deleteProjectUsersWhereIdNotIn(ids);
		deleteProjectResourceAccessWhereIdNotIn(ids);
		setAppProjectToNullWhereIdNotIn(ids);
		deleteByIdNotInCustom(ids);
	}

	@Modifying
	@Transactional
	@Override
	default void deleteById(String id) {
		deleteProjectUsersWhereIdIs(id);
		deleteProjectResourceAccessWhereIdIs(id);
		setAppProjectToNullWhereIdIs(id);
		deleteByIdCustom(id);
	}

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.identification, o.id, 'Project') FROM Project AS o")
	public List<VersionableVO> findVersionableViews();

}

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

import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.NotebookUserAccessType;
import com.minsait.onesait.platform.config.model.User;

public interface NotebookUserAccessRepository extends JpaRepository<NotebookUserAccess, String> {

	@SuppressWarnings("unchecked")
	@Override
	NotebookUserAccess save(NotebookUserAccess entity);

	List<NotebookUserAccess> findAllByOrderById();

	@Query("SELECT o FROM NotebookUserAccess AS o WHERE o.notebook=:id")
	List<NotebookUserAccess> findByNotebook(@Param("id") Notebook id);

	@Query("SELECT o FROM NotebookUserAccess AS o WHERE o.notebook=:id AND o.user=:user")
	List<NotebookUserAccess> findByNotebookAndUser(@Param("id") Notebook id, @Param("user") User user);

	@Query("SELECT o FROM NotebookUserAccess AS o WHERE o.notebook=:id AND o.user=:user AND o.notebookUserAccessType=:notebookUserAccessType")
	NotebookUserAccess findByNotebookAndUserAndAccess(@Param("id") Notebook id, @Param("user") User user,
			@Param("notebookUserAccessType") NotebookUserAccessType notebookUserAccessType);
	
	@Query("SELECT o FROM NotebookUserAccess AS o WHERE o.user=:user AND o.notebookUserAccessType=:notebookUserAccessType")
	NotebookUserAccess findByUserAndAccess(@Param("user") User user,
			@Param("notebookUserAccessType") NotebookUserAccessType notebookUserAccessType);

	@Query("SELECT o FROM NotebookUserAccess AS o WHERE o.user=:user")
	List<NotebookUserAccess> findByUser(@Param("user") User user);

}

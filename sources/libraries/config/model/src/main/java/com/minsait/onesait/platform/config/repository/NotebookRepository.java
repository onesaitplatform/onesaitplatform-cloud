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

import com.minsait.onesait.platform.config.dto.NotebookForList;
import com.minsait.onesait.platform.config.dto.NotebookForListExt;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.User;

public interface NotebookRepository extends JpaRepository<Notebook, String> {

	List<Notebook> findAllByOrderByIdentificationAsc();

	Notebook findByIdentification(String notebookId);

	@Query("SELECT o FROM Notebook AS o WHERE o.user=:user ORDER BY o.identification ASC")
	List<Notebook> findByUser(@Param("user") User user);

	@Query("SELECT o FROM Notebook AS o WHERE (o.user=:user OR o.isPublic=TRUE OR o.id IN (SELECT uo.notebook.id FROM NotebookUserAccess AS uo WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<Notebook> findByUserAndAccess(@Param("user") User user);

	@Query("SELECT o.identification FROM Notebook AS o WHERE (o.user=:user OR o.id IN (SELECT uo.notebook.id FROM NotebookUserAccess AS uo WHERE uo.user=:user)) ORDER BY o.identification ASC")
	List<String> findIdentificationsByUserAndPermissions(@Param("user") User user);

	List<Notebook> findByIdentificationAndIdzep(String notebookId, String idzep);

	Notebook findByIdzep(String idzep);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.NotebookForList(o.id, o.identification, o.idzep, o.user, o.isPublic, 'null') "
			+ "FROM Notebook AS o ")
	List<NotebookForList> findAllNotebookList();
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.NotebookForListExt(o.id, o.identification, o.idzep, o.user, o.isPublic, 'null', o.createdAt, o.updatedAt) "
			+ "FROM Notebook AS o ")
	List<NotebookForListExt> findAllNotebookListExt();
	
	@Query("SELECT new com.minsait.onesait.platform.config.dto.NotebookForListExt(o.id, o.identification, o.idzep, o.user, o.isPublic, 'null', o.createdAt, o.updatedAt) "
			+ "FROM Notebook AS o WHERE (o.user=:user OR o.id IN (SELECT uo.notebook.id FROM NotebookUserAccess AS uo WHERE uo.user=:user)) OR o.isPublic = true ORDER BY o.identification ASC")
	List<NotebookForListExt> findUserNotebookListExt(@Param("user") User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, 'null', o.createdAt, o.updatedAt, o.user, 'NOTEBOOK', 0) FROM Notebook AS o WHERE o.identification like %:identification%  ORDER BY o.identification ASC")
	List<OPResourceDTO> findAllDto(@Param("identification") String identification);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, 'null', o.createdAt, o.updatedAt, o.user, 'NOTEBOOK', 0) FROM Notebook AS o WHERE (o.user=:user OR o.id IN (SELECT uo.notebook.id FROM NotebookUserAccess AS uo WHERE uo.user=:user)) AND o.identification like %:identification% ORDER BY o.identification ASC")
	List<OPResourceDTO> findDtoByUserAndPermissions(@Param("user") User user,
			@Param("identification") String identification);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}

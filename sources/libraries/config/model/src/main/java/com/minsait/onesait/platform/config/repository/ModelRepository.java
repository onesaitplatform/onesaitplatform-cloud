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

import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.User;

public interface ModelRepository extends JpaRepository<Model, String> {

	List<Model> findByIdentification(String identification);

	List<Model> findByIdentificationContaining(String identification);

	List<Model> findAllByOrderByIdentificationAsc();

	List<Model> findByIdentificationLike(String identification);

	List<Model> findByNotebook(Notebook notebook);

	List<Model> findByUser(User user);

	Model findByUserAndIdentification(User user, String identification);

	@Query("SELECT o FROM Model AS o WHERE ((o.notebook.id IN (SELECT nua.notebook FROM NotebookUserAccess as nua where nua.user=:user)) OR "
			+ "(o.notebook.id IN (SELECT nb.id FROM Notebook as nb where nb.user=:user)))")
	List<Model> findByUserNoAdministratorIsOwnerOrHasPermission(@Param("user") User user);

}

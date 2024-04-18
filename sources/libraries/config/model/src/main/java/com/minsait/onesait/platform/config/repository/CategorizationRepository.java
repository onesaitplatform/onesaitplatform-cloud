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

import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.User;

public interface CategorizationRepository extends JpaRepository<Categorization, String> {

	List<Categorization> findByUser(User user);

	Categorization findByIdentification(String identification);

	@Query("SELECT o.id FROM Categorization AS o WHERE o.identification=:identification")
	String findIdByIdentification(@Param("identification") String identification);

	@Query("SELECT o FROM CategorizationUser AS o WHERE o.active=1")
	List<Categorization> findActive();

	@SuppressWarnings("unchecked")
	@Override
	Categorization save(Categorization entity);

	@Override
	void delete(Categorization id);

}

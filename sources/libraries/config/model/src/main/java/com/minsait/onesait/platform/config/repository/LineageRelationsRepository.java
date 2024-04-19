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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.LineageRelations;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;

public interface LineageRelationsRepository extends JpaRepository<LineageRelations, Long> {

	List<LineageRelations> findBySource(OPResource source);

	List<LineageRelations> findByTarget(OPResource target);

	List<LineageRelations> findByTargetOrSource(OPResource target, OPResource source);

	@Query("SELECT r FROM LineageRelations AS r WHERE r.user=:user AND (r.source=:source OR r.target=:target)")
	List<LineageRelations> findByTargetOrSourceAndUser(@Param("user") User user, @Param("source") OPResource source,
			@Param("target") OPResource target);

}

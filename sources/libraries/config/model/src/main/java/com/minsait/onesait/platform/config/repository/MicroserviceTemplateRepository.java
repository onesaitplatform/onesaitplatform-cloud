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

import com.minsait.onesait.platform.config.model.MicroserviceTemplate;
import com.minsait.onesait.platform.config.model.User;

public interface MicroserviceTemplateRepository extends JpaRepository<MicroserviceTemplate, String> {

	@Override
	<S extends MicroserviceTemplate> S save(S mstemplate);

	public MicroserviceTemplate findByIdentification(String identification);

	public MicroserviceTemplate findMicroserviceTemplateByIdentificationAndUser(String identification, User user);

	public MicroserviceTemplate findMicroserviceTemplateById(String id);

	public List<MicroserviceTemplate> findByUserOrIsPublicTrueOrderByIdentificationAsc(User user);

	public List<MicroserviceTemplate> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	@Query("SELECT m.identification FROM MicroserviceTemplate AS m ORDER BY m.identification ASC")
	public List<String> findAllIdentifications();

	@Query("SELECT m.identification FROM MicroserviceTemplate AS m WHERE (m.user=:user OR m.isPublic=True) ORDER BY m.identification ASC")
	public List<String> findAllIdentificationsByUser(@Param("user") User user);

	@Query("SELECT m FROM MicroserviceTemplate AS m WHERE m.identification LIKE %:identification% ORDER BY m.identification ASC")
	public List<MicroserviceTemplate> findByIdentificationContaining(@Param("identification") String identification);

	@Query("SELECT m FROM MicroserviceTemplate AS m WHERE m.identification LIKE %:identification% AND (m.user=:user OR m.isPublic=True) ORDER BY m.identification ASC")
	public List<MicroserviceTemplate> findByUserAndIdentificationContainingOrderByIdentificationAsc(
			@Param("user") User user, @Param("identification") String identification);

}

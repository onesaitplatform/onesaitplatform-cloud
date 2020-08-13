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

import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.User;

public interface GadgetTemplateRepository extends JpaRepository<GadgetTemplate, String> {

	GadgetTemplate findById(String Id);

	List<GadgetTemplate> findByUser(User user);

	List<GadgetTemplate> findByUserAndIdentificationContaining(User user, String identification);

	List<GadgetTemplate> findByUserAndDescriptionContaining(User user, String description);

	List<GadgetTemplate> findAllByOrderByIdentificationAsc();

	List<GadgetTemplate> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	List<GadgetTemplate> findByIdentificationContaining(String identification);

	List<GadgetTemplate> findByDescriptionContaining(String description);

	List<GadgetTemplate> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	GadgetTemplate findByIdentification(String identification);

	@Query("SELECT o FROM GadgetTemplate AS o WHERE (o.user.userId LIKE %:userId% ) OR o.isPublic IS true")
	List<GadgetTemplate> findGadgetTemplateByUserAndIsPublicTrue(@Param("userId") String userId);

	@Query("SELECT o FROM GadgetTemplate AS o WHERE ((o.user.userId LIKE %:userId% ) OR o.isPublic IS true) AND o.identification=:identification ")
	GadgetTemplate findGadgetTemplateByUserAndIsPublicTrueAndIdentification(@Param("userId") String userId,
			@Param("identification") String identification);
	
	@Query("SELECT o FROM GadgetTemplate AS o WHERE ((o.user.userId LIKE %:userId% ) OR o.isPublic IS true) AND o.identification LIKE %:identification% ")
	List<GadgetTemplate> findGadgetTemplateByUserAndIsPublicTrueAndIdentificationLike(@Param("userId") String userId,
			@Param("identification") String identification);

}

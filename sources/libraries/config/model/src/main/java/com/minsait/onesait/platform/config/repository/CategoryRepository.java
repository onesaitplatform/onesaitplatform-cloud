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

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Category.Type;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Category findById(String id);

	Category findByIdentification(String identification);
	
	//List<Category> findByIdentificationLike(String identification);
	@Query("SELECT cp FROM Category cp WHERE cp.identification like %:identification%")	
	List<Category> findByIdentificationLike(@Param("identification") String identification);

	List<Category> findByDescription(String description);
	
	//List<Category> findByDescriptionLike(String description);
	@Query("SELECT cp FROM Category cp WHERE cp.description like %:description%")	
	List<Category> findByDescriptionLike(@Param("description") String description);

	List<Category> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Category> findByIdentificationContaining(String identification);

	List<Category> findByDescriptionContaining(String description);

	List<Category> findAllByOrderByIdentificationAsc();

	List<Category> findByIdentificationAndDescription(String identification, String description);

	//List<Category> findByIdentificationLikeAndDescriptionLike(String identification, String description);
	@Query("SELECT cp FROM Category cp WHERE cp.description like %:description% and cp.identification like %:identification%")	
	List<Category> findByIdentificationLikeAndDescriptionLike(@Param("description") String description, @Param("identification") String identification);
	
	List<Category> findByTypeIn(List<Type> types);
	
	List<Category> findByType(Type type);

}

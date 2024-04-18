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

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Tag;

public interface TagRepository extends JpaRepository<Tag, String> {

	List<Tag> findByName(String name);

	@Query("SELECT t.name FROM Tag t")
	List<String> findTagNames();

	@Query("SELECT t FROM Tag t where t.name = :name")
	Tag findTag(@Param("name") String name);
	
	@Query("SELECT t FROM Tag t where t.name like %:name%")
	List<Tag> findTagsLike(@Param("name") String name);

	@Modifying
	@Transactional(value = TxType.REQUIRES_NEW)
	@Query(value = "DELETE FROM RESOURCE_TAG WHERE RESOURCE_ID IN (:ids) ", nativeQuery = true)
	void deleteByResourceId(@Param("ids") List<String> ids);

	@Modifying
	@Transactional(value = TxType.REQUIRES_NEW)
	@Query(value = "DELETE FROM RESOURCE_TAG WHERE TAG_ID = :tagId AND RESOURCE_ID IN (:ids) ", nativeQuery = true)
	void deleteByResourcesIdsAndTagId(@Param("tagId") String tagId, @Param("ids") List<String> ids);
	
	@Query("SELECT t.id FROM Tag t WHERE t.name = :name")
	String findIdByTagName(@Param("name") String name);

	@Modifying
	@Transactional
	void deleteByName(String name);
	
	@Modifying
	@Transactional(value = TxType.REQUIRES_NEW)
	@Query(value = "DELETE FROM RESOURCE_TAG WHERE TAG_ID = :tagId AND RESOURCE_ID IN (:ids) ", nativeQuery = true)
	void deleteResourceByResourceIdAndTagId(@Param("tagId") String tagId, @Param("ids") List<String> ids);
	
	@Modifying
	@Transactional(value = TxType.REQUIRES_NEW)
	@Query(value = "DELETE FROM RESOURCE_TAG WHERE TAG_ID = (SELECT T.ID FROM TAG T WHERE NAME=:tagId )" , nativeQuery = true)
	void deleteResourcesByTagId(@Param("tagId") String tagId);
	
}

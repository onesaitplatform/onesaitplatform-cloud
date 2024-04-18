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

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.User;

public interface BinaryFileRepository extends JpaRepository<BinaryFile, String> {

	@Query("select bf from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) AND bf.id=:id ORDER BY bf.fileName ASC")
	BinaryFile findByUserAndId(@Param("user") User user, @Param("id") String id);

	@Query("select bf from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE (bfa.user=:user AND bfa.accessType=1))) AND bf.id=:id ORDER BY bf.fileName ASC")
	BinaryFile findByUserAndIdWrite(@Param("user") User user, @Param("id") String id);

	@Query("select bf from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE (bfa.user=:user AND bfa.accessType=0))) AND bf.id=:id ORDER BY bf.fileName ASC")
	BinaryFile findByUserAndIdRead(@Param("user") User user, @Param("id") String id);

	@Query("select bf from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) ORDER BY bf.fileName ASC")
	List<BinaryFile> findByUser(@Param("user") User user);

	BinaryFile findById(String id);

	@Transactional
	void deleteById(String id);

}

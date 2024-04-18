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

	@Query("select bf from BinaryFile as bf WHERE (bf.owner=:user OR bf.isPublic=TRUE OR bf.fileId IN (SELECT bfa.binaryFile.fileId FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) AND bf.fileId=:fileId ORDER BY bf.fileName ASC")
	BinaryFile findByUserAndFileId(@Param("user") User user, @Param("fileId") String fileId);

	@Query("select bf from BinaryFile as bf WHERE (bf.owner=:user OR bf.isPublic=TRUE OR bf.fileId IN (SELECT bfa.binaryFile.fileId FROM BinaryFileAccess AS bfa WHERE (bfa.user=:user AND bfa.accessType=1))) AND bf.fileId=:fileId ORDER BY bf.fileName ASC")
	BinaryFile findByUserAndFileIdWrite(@Param("user") User user, @Param("fileId") String fileId);

	@Query("select bf from BinaryFile as bf WHERE (bf.owner=:user OR bf.isPublic=TRUE OR bf.fileId IN (SELECT bfa.binaryFile.fileId FROM BinaryFileAccess AS bfa WHERE (bfa.user=:user AND bfa.accessType=0))) AND bf.fileId=:fileId ORDER BY bf.fileName ASC")
	BinaryFile findByUserAndFileIdRead(@Param("user") User user, @Param("fileId") String fileId);

	@Query("select bf from BinaryFile as bf WHERE (bf.owner=:user OR bf.isPublic=TRUE OR bf.fileId IN (SELECT bfa.binaryFile.fileId FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) ORDER BY bf.fileName ASC")
	List<BinaryFile> findByUser(@Param("user") User user);

	BinaryFile findByOwnerAndFileId(User owner, String fileId);

	BinaryFile findByFileId(String fileId);

	@Transactional
	void deleteByFileId(String fileId);

}

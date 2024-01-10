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
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	@Query("select bf from BinaryFile as bf WHERE (bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) ORDER BY bf.fileName ASC")
	List<BinaryFile> findByUserAllowed(@Param("user") User user);

	@Query("select bf from BinaryFile as bf WHERE (bf.path=:path)")
	List<BinaryFile> findByPath(@Param("path") String path);

	@Override
	long count();
	
	@Query("select count(bf.id) from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user))")
	long countByUser(@Param("user") User user);
	
	@Query("select bf from BinaryFile as bf WHERE (bf.fileName LIKE :fileName OR bf.identification LIKE :fileId OR bf.fileExtension LIKE :fileExt OR bf.metadata LIKE :metaData OR bf.user.userId LIKE :owner)")
	List<BinaryFile> findAllByCriteria(@Param("fileName") String fileName, @Param("fileId") String fileId,  @Param("fileExt") String fileExt, @Param("metaData") String metaData, @Param("owner") String owner);
	
	@Query("select bf from BinaryFile as bf WHERE ((bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) AND (bf.fileName LIKE :fileName OR bf.identification LIKE :fileId OR bf.fileExtension LIKE :fileExt OR bf.metadata LIKE :metaData)) ORDER BY bf.fileName ASC")
	List<BinaryFile> findByUserByCriteria(@Param("user") User user, @Param("fileName") String fileName, @Param("fileId") String fileId,  @Param("fileExt") String fileExt, @Param("metaData") String metaData);	

	@Override
	@Transactional
	void deleteById(String id);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
	
	@Query("select count(bf.id) from BinaryFile as bf WHERE bf.fileName NOT LIKE 'Audit_%'")
	long countNoAudit();
	
	@Query("select count(bf.id) from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) AND bf.fileName NOT LIKE 'Audit_%'")
	long countByUserNoAudit(@Param("user") User user);

	@Query("select bf from BinaryFile as bf WHERE bf.fileName NOT LIKE 'Audit_%'")
	List<BinaryFile> findAllNoAudit();

	@Query("select bf from BinaryFile as bf WHERE (bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) AND (bf.fileName NOT LIKE 'Audit_%')")
	List<BinaryFile> findByUserNoAudit(@Param("user") User user);

	@Query("select bf from BinaryFile as bf WHERE (bf.fileName LIKE :fileName OR bf.identification LIKE :fileId OR bf.fileExtension LIKE :fileExt OR bf.metadata LIKE :metaData OR bf.user.userId LIKE :owner) AND (bf.fileName NOT LIKE 'Audit_%')")
	List<BinaryFile> findAllByCriteriaNoAudit(@Param("fileName") String fileName, @Param("fileId") String fileId, @Param("fileExt") String fileExt, @Param("metaData") String metaData, @Param("owner") String owner);

	@Query("select bf from BinaryFile as bf WHERE ((bf.user=:user OR bf.isPublic=TRUE OR bf.id IN (SELECT bfa.binaryFile.id FROM BinaryFileAccess AS bfa WHERE bfa.user=:user)) AND (bf.fileName LIKE :fileName OR bf.identification LIKE :fileId OR bf.fileExtension LIKE :fileExt OR bf.metadata LIKE :metaData) AND (bf.fileName NOT LIKE 'Audit_%')) ORDER BY bf.fileName ASC")
	List<BinaryFile> findByUserByCriteriaNoAudit(@Param("user") User user, @Param("fileName") String fileName,
			@Param("fileId") String fileId, @Param("fileExt") String fileExt, @Param("metaData") String metaData);

	@Modifying
	@Transactional
	void deleteByIdIn(Collection<String> ids);

	@Query("select bf.id from BinaryFile bf where bf.createdAt <= :date ")
	List<String> getAllIdsBeforeDate(@Param("date") Date date);
}

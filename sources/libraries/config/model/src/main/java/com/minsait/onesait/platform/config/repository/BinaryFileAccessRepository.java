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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.User;

public interface BinaryFileAccessRepository extends JpaRepository<BinaryFileAccess, String> {

	public List<BinaryFileAccess> findByUser(User user);

	public List<BinaryFileAccess> findByBinaryFile(BinaryFile binaryFile);

	public BinaryFileAccess findByUserAndBinaryFile(User user, BinaryFile binaryFile);

	@Transactional
	public void deleteByUserAndBinaryFile(User user, BinaryFile binaryFile);

	@Override
	@Modifying
	@Transactional
	public void deleteById(String id);

	@Query("select bfa from BinaryFileAccess as bfa WHERE bfa.user=:user AND bfa.binaryFile=:file AND bfa.accessType=1")
	public BinaryFileAccess findByUserAndWriteAccess(@Param("file") BinaryFile file, @Param("user") User user);
}

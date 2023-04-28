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
package com.minsait.onesait.platform.business.services.binaryrepository;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;

public interface BinaryRepositoryLogicService {

	public String addBinary(MultipartFile file, String metadata) throws BinaryRepositoryException, IOException;

	public String addBinary(MultipartFile file, String metadata, RepositoryType repository)
			throws BinaryRepositoryException, IOException;

	public void updateBinary(String fileId, MultipartFile file, String metadata)
			throws BinaryRepositoryException, IOException;

	public void removeBinary(String fileId) throws BinaryRepositoryException;

	public BinaryFileData getBinaryFile(String fileId) throws IOException, BinaryRepositoryException;

	public void authorizeUser(String fileId, String userId, String accessType) throws BinaryRepositoryException;

	public BinaryFileData getBinaryFileWOPermission(String fileId) throws IOException, BinaryRepositoryException;

	public String downloadForPagination(String fileId, Long startLine, Long maxLines, Boolean skipHeaders)
			throws IOException, BinaryRepositoryException;

	public Boolean closePagination(String fileId) throws IOException, BinaryRepositoryException;

	public void setAuthorization(String fileId, String userId, String accessType) throws BinaryRepositoryException;
	
	public void deleteAuthorization(String fileId, String userId) throws BinaryRepositoryException;

}

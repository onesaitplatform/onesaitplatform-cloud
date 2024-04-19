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
package com.minsait.onesait.platform.config.services.binaryfile;

import java.util.List;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.BinaryFileAccess.Type;
import com.minsait.onesait.platform.config.model.User;

public interface BinaryFileService {

	public void createBinaryFile(BinaryFile binaryFile);

	public void updateBinaryFile(String fileId, String metadata, String mime, String fileName);

	public boolean hasUserPermissionWrite(String fileId, User user);

	public boolean hasUserPermissionRead(String fileId, User user);

	public void authorizeUser(String fileId, BinaryFileAccess.Type accessType, User user);

	void deleteFile(String fileId);

	BinaryFile getFile(String fileId);

	List<BinaryFile> getAllFiles(User user);

	public void changePublic(String fileId);

	public boolean isUserOwner(String fileId, User user);

	public BinaryFileAccess createBinaryFileAccess(String fileId, String userId, String accessType, User user)
			throws GenericOPException;

	public List<BinaryFileAccess> getAuthorizations(String id, User user);

	public void deleteBinaryFileAccess(String id, User user) throws GenericOPException;

	public BinaryFileAccess updateBinaryFileAccess(String id, String accesstype, User user) throws GenericOPException;

	public boolean canUserEditAccess(User user, String id);

	public void setAuthorization(String id, Type accessType, User user);
	
	public void deleteAuthorization(String id, User user) throws GenericOPException;

}

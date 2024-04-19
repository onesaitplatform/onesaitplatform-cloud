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
package com.minsait.onesait.platform.binaryrepository.service;

import java.io.IOException;
import java.io.InputStream;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;

public interface BinaryRepository {

	public String addBinary(InputStream binaryStream, String metadata, String id) throws BinaryRepositoryException;

	public String addBinary(byte[] binaryBytes, String metadata) throws BinaryRepositoryException;

	public void removeBinary(String id) throws BinaryRepositoryException;

	public void updateBinary(String id, InputStream binaryStream, String metadata) throws BinaryRepositoryException;

	public void updateBinary(String id, byte[] binaryBytes, String metadata) throws BinaryRepositoryException;

	public byte[] getBinaryData(String id) throws IOException, BinaryRepositoryException;

	public BinaryFileData getBinaryFile(String id) throws IOException, BinaryRepositoryException;

	public String getBinaryFileForPaginate(String id, Long startLine, Long maxLines, Boolean skipHeaders)
			throws IOException, BinaryRepositoryException;

	public Boolean closePaginate(String id) throws IOException, BinaryRepositoryException;

}

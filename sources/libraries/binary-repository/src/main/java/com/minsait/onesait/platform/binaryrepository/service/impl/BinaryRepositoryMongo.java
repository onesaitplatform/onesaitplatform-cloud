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
package com.minsait.onesait.platform.binaryrepository.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.persistence.PersistenceException;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.binaryrepository.service.BinaryRepository;
import com.minsait.onesait.platform.persistence.mongodb.gridfs.GridFSConnector;
import com.mongodb.client.gridfs.GridFSDownloadStream;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service("BinaryRepositoryMongo")
// @ConditionalOnProperty(name = "onesaitplatform.binary-repository.type",
// havingValue = "mongo")
@Slf4j
public class BinaryRepositoryMongo implements BinaryRepository {

	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	@Getter
	@Setter
	private String database;

	@Autowired
	private GridFSConnector gridFSConnector;

	@Override
	public String addBinary(InputStream binaryStream, String metadata, String id) throws BinaryRepositoryException {
		try {
			return gridFSConnector.uploadGridFsFile(database, binaryStream, metadata).toHexString();
		} catch (final PersistenceException e) {
			log.error("Could not store file");
			throw new BinaryRepositoryException("Could not store file", e);
		}
	}

	@Override
	public String addBinary(byte[] binaryBytes, String metadata) throws BinaryRepositoryException {
		return addBinary(new ByteArrayInputStream(binaryBytes), metadata, null);
	}

	@Override
	public void removeBinary(String id) throws BinaryRepositoryException {
		try {
			gridFSConnector.removeGridFsFile(database, new ObjectId(id));
		} catch (final PersistenceException e) {
			log.error("Could not remove file {}", id);
			throw new BinaryRepositoryException("Could not remove file", e);
		}
	}

	@Override
	public void updateBinary(String id, InputStream binaryStream, String metadata) throws BinaryRepositoryException {
		try {
			gridFSConnector.updateGridFsFile(database, new ObjectId(id), binaryStream, metadata);
		} catch (final PersistenceException e) {
			log.error("Could not update file {}", id);
			throw new BinaryRepositoryException("Could not update file", e);
		}
	}

	@Override
	public void updateBinary(String id, byte[] binaryBytes, String metadata) throws BinaryRepositoryException {
		this.updateBinary(id, new ByteArrayInputStream(binaryBytes), metadata);

	}

	@Override
	public byte[] getBinaryData(String id) throws IOException, BinaryRepositoryException {

		return ((ByteArrayOutputStream) getBinaryFile(id).getData()).toByteArray();
	}

	@Override
	public BinaryFileData getBinaryFile(String id) throws IOException, BinaryRepositoryException {
		try {
			final GridFSDownloadStream downloadStream = gridFSConnector.readGridFsFile(database, new ObjectId(id));
			final OutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(downloadStream, outputStream);
			downloadStream.close();
			outputStream.close();

			return BinaryFileData.builder().data(outputStream).build();
		} catch (final PersistenceException e) {
			log.error("Could not get file {}", id);
			throw new BinaryRepositoryException("Could not get file", e);
		}

	}

}

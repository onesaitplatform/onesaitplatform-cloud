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
package com.minsait.onesait.platform.binaryrepository.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.PersistenceException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.binaryrepository.service.BinaryRepository;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.persistence.mongodb.gridfs.GridFSConnector;
import com.mongodb.client.gridfs.GridFSDownloadStream;

import lombok.extern.slf4j.Slf4j;

@Service("BinaryRepositoryMongo")
@Slf4j
public class BinaryRepositoryMongo implements BinaryRepository {

	@Autowired
	private GridFSConnector gridFSConnector;

	@Value("${onesaitplatform.binary-repository.tmp.file.path:/tmp/files}")
	private String filepath;

	@Value("${onesaitplatform.binary-repository.remove.file.timeout:600}")
	private Long timeout;

	@Value("${onesaitplatform.binary-repository.tmp.file.max.size:100}")
	private Long tempFolderMaxSize;

	@Override
	public String addBinary(InputStream binaryStream, String metadata, String id) throws BinaryRepositoryException {
		try {
			return gridFSConnector.uploadGridFsFile(Tenant2SchemaMapper.getRtdbSchema(), binaryStream, metadata)
					.toHexString();
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
			gridFSConnector.removeGridFsFile(Tenant2SchemaMapper.getRtdbSchema(), new ObjectId(id));
		} catch (final PersistenceException e) {
			log.error("Could not remove file {}", id);
			throw new BinaryRepositoryException("Could not remove file", e);
		}
	}

	@Override
	public void updateBinary(String id, InputStream binaryStream, String metadata) throws BinaryRepositoryException {
		try {
			gridFSConnector.updateGridFsFile(Tenant2SchemaMapper.getRtdbSchema(), new ObjectId(id), binaryStream,
					metadata);
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
			final GridFSDownloadStream downloadStream = gridFSConnector
					.readGridFsFile(Tenant2SchemaMapper.getRtdbSchema(), new ObjectId(id));
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

	@Override
	public String getBinaryFileForPaginate(String id, Long startLine, Long maxLines, Boolean skipHeaders)
			throws IOException, BinaryRepositoryException {
		try {
			// If startLine=1, we change for 0
			startLine = startLine == 1 ? 0 : startLine;
			File file = new File(filepath + id);
			if (!file.exists()) {

				final GridFSDownloadStream downloadStream = gridFSConnector
						.readGridFsFile(Tenant2SchemaMapper.getRtdbSchema(), new ObjectId(id));
				final OutputStream outputStream = new FileOutputStream(file);
				IOUtils.copy(downloadStream, outputStream);
				downloadStream.close();
				outputStream.close();
				Long size = FileUtils.sizeOfDirectory(new File(filepath)) / 1000000;
				if (size > tempFolderMaxSize) {
					file.delete();
					log.error(
							"The temporal directory is full, wait for paginate another document or close some pagination in progress.");
					throw new BinaryRepositoryException(
							"The temporal directory is full, wait for paginate another document or close some pagination in progress.");
				}
				this.scheduleDeleteFile(id);
			}
			Stream<String> lines = null;
			if (!skipHeaders) {
				Stream<String> headers = null;
				if (startLine > 0) {
					headers = Files.lines(Paths.get(filepath + id)).skip(0).limit(1);
					lines = Files.lines(Paths.get(filepath + id)).skip(startLine - 1).limit(maxLines);
					Stream<String> resultingStream = Stream.concat(headers, lines);
					return resultingStream.collect(Collectors.joining("\n"));
				}
				lines = Files.lines(Paths.get(filepath + id)).skip(startLine == 0 ? 0 : startLine - 1).limit(maxLines);

				return lines.collect(Collectors.joining("\n"));
			} else {
				lines = Files.lines(Paths.get(filepath + id)).skip(startLine == 0 ? 1 : startLine - 1).limit(maxLines);

				return lines.collect(Collectors.joining("\n"));
			}

		} catch (final PersistenceException e) {
			log.error("Could not get file for pagination {}", id);
			throw new BinaryRepositoryException("Could not get file for pagination.", e);
		} catch (IllegalArgumentException e) {
			log.error("This file cannot be paginated with this parameters. {}", e);
			throw new BinaryRepositoryException("This file cannot be paginated with this parameters.", e);
		}

	}

	@Override
	public Boolean closePaginate(String id) throws IOException, BinaryRepositoryException {
		File file = new File(filepath + id);
		return file.delete();
	}

	private void scheduleDeleteFile(String id) {
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				File file = new File(filepath + id);
				file.delete();
			}
		}, timeout * 1000);
	}

}

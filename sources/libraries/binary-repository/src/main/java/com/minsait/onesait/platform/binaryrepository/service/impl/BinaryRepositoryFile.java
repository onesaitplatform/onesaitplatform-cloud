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
package com.minsait.onesait.platform.binaryrepository.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.binaryrepository.service.BinaryRepository;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;

import lombok.extern.slf4j.Slf4j;

@Service("BinaryRepositoryFile")
// @ConditionalOnProperty(name = "onesaitplatform.binary-repository.type",
// havingValue = "file")
@Slf4j
public class BinaryRepositoryFile implements BinaryRepository {
	@Autowired
	private BinaryFileService binaryFileService;

	@Override
	public String addBinary(InputStream binaryStream, String metadata, String id) throws BinaryRepositoryException {
		return addBinary(id, binaryStream);
	}

	@Override
	public String addBinary(byte[] binaryBytes, String metadata) throws BinaryRepositoryException {
		return addBinary(new ByteArrayInputStream(binaryBytes), metadata, null);
	}

	public String addBinary(String id, InputStream binaryStream) throws BinaryRepositoryException {
		final String path = id;
		try (OutputStream os = new FileOutputStream(this.getFile(path))) {
			os.write(IOUtils.toByteArray(binaryStream));
			os.flush();
			log.info("Created file at {}", path);
		} catch (final IOException e) {
			log.error("Could not store binary File");
			throw new BinaryRepositoryException("Could not store file", e);
		}

		return new File(path).getName();
	}

	@Override
	public void removeBinary(String id) throws BinaryRepositoryException {
		final String path = binaryFileService.getFile(id).getPath();
		try {
			final File file = new File(path);
			boolean deleted = file.delete();
			log.info("Deleted file:" + deleted + " at {}", path);
		} catch (final RuntimeException e) {
			log.error("Could not delete file {}", id);
			throw new BinaryRepositoryException("Could not delete file", e);

		}

	}

	@Override
	public void updateBinary(String id, InputStream binaryStream, String metadata) throws BinaryRepositoryException {
		removeBinary(id);
		final String path = binaryFileService.getFile(id).getPath();
		try (final OutputStream os = new FileOutputStream(new File(path))) {
			os.write(IOUtils.toByteArray(binaryStream));
			os.flush();
			log.info("Updated file at {}", path);
		} catch (final IOException e) {
			log.error("Could not store binary File");
			throw new BinaryRepositoryException("Could not store file", e);
		}

	}

	@Override
	public void updateBinary(String id, byte[] binaryBytes, String metadata) throws BinaryRepositoryException {
		updateBinary(id, new ByteArrayInputStream(binaryBytes), metadata);

	}

	@Override
	public byte[] getBinaryData(String id) throws IOException {
		final Path path = Paths.get(binaryFileService.getFile(id).getPath());
		log.info("Retrieved file at {}", path);
		return Files.readAllBytes(path);
	}

	@Override
	public BinaryFileData getBinaryFile(String id) throws IOException, BinaryRepositoryException {
		final String path = binaryFileService.getFile(id).getPath();
		final File file = new File(path);
		if (file.exists() && file.isFile()) {
			final InputStream is = new FileInputStream(file);
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			IOUtils.copy(is, os);
			is.close();
			os.close();
			log.info("Retrieved file at {}", path);
			return BinaryFileData.builder().fileName(id).data(os).build();
		} else
			throw new BinaryRepositoryException("Could not read binary file {}");

	}

	private File getFile(String path) throws IOException {
		final File file = new File(path);

		file.getParentFile().mkdirs();
		boolean newFile = file.createNewFile();
		if (log.isDebugEnabled()) {
			log.debug("Created new File:{}", newFile);
		}
		return file;
	}

	@Override
	public String getBinaryFileForPaginate(String id, Long startLine, Long maxLines, Boolean skipHeaders)
			throws IOException, BinaryRepositoryException {
		return null;
	}

	@Override
	public Boolean closePaginate(String id) throws IOException, BinaryRepositoryException {
		return null;
	}

}

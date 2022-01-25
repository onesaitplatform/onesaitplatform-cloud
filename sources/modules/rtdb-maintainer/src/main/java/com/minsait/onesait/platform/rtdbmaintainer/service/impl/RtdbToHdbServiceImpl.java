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
package com.minsait.onesait.platform.rtdbmaintainer.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.factory.BinaryRepositoryFactory;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbToHdbService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RtdbToHdbServiceImpl implements RtdbToHdbService {
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private BinaryRepositoryFactory binaryRepositoryFactory;

	@Override
	public void postProcessExport(Ontology ontology, ExportData exportData) {
		final File file = new File(exportData.getPath());
		if (file.exists() && ontology.getRtdbToHdbStorage().equals(Ontology.RtdbToHdbStorage.MONGO_GRIDFS)) {
			try {
				final String id = binaryRepositoryFactory.getInstance(RepositoryType.MONGO_GRIDFS)
						.addBinary(new FileInputStream(file), null, null);
				final BinaryFile binaryFile = new BinaryFile();
				binaryFile.setFileName(file.getName());
				binaryFile.setIdentification(file.getName());
				binaryFile.setRepository(RepositoryType.MONGO_GRIDFS);
				binaryFile.setId(id);
				binaryFile.setMetadata(null);
				binaryFile.setMime("text/csv");
				binaryFile.setFileExtension(FilenameUtils.getExtension(file.getName()));
				binaryFile.setUser(ontology.getUser());
				binaryFileService.createBinaryFile(binaryFile);
				final boolean delete = file.delete();
				log.debug("delete:" + delete);
			} catch (FileNotFoundException | BinaryRepositoryException e) {
				log.error("Could not store file {} on Binary Repository: {}", file.getName(), e.getMessage());
			}

		} else if (file.exists() && ontology.getRtdbToHdbStorage().equals(Ontology.RtdbToHdbStorage.DIRECTORY)) {
			final BinaryFile binaryFile = new BinaryFile();
			binaryFile.setFileName(file.getName());
			binaryFile.setRepository(RepositoryType.FILE);
			binaryFile.setPath(exportData.getPath());
			binaryFile.setId(UUID.randomUUID().toString());
			binaryFile.setIdentification(file.getName());
			binaryFile.setMetadata(null);
			final String mime = FilenameUtils.getExtension(file.getName()).toLowerCase().contains("json")
					? "application/json"
					: "text/csv";
			binaryFile.setMime(mime);
			binaryFile.setFileExtension(FilenameUtils.getExtension(file.getName()));
			binaryFile.setUser(ontology.getUser());
			binaryFileService.createBinaryFile(binaryFile);
		}
	}

	@Override
	public void deleteTmpFile(ExportData exportData) {
		final File file = new File(exportData.getPath());
		if (file.exists()) {
			final boolean delete = file.delete();
			log.debug("delete:" + delete);
		}
	}
}

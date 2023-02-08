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
package com.minsait.onesait.platform.config.services.opendata.binaryFiles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.ODBinaryFilesDataset;
import com.minsait.onesait.platform.config.repository.ODBinaryFilesDatasetRepository;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.exceptions.ODBinaryFilesDatasetServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BinaryFilesDatasetServiceImpl implements BinaryFilesDatasetService {

	@Autowired
	private ODBinaryFilesDatasetRepository filesDatasetRepository;
	@Autowired
	private BinaryFileService binaryFileService;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private static final String BINARYFILES_NOT_EXIST = "BinaryFiles does not exist in the database";

	@Override
	public List<String> getAllIds() {
		final List<ODBinaryFilesDataset> typologies = filesDatasetRepository.findAll();
		final List<String> identifications = new ArrayList<>();
		for (final ODBinaryFilesDataset BinaryFiles : typologies) {
			identifications.add(BinaryFiles.getId());

		}
		return identifications;
	}

	@Override
	public List<ODBinaryFilesDataset> getAllBinaryFilesDatasets() {
		return filesDatasetRepository.findAll();
	}

	@Transactional
	@Override
	public void deleteBinaryFilesDatasetByDataset(String datasetId) {
		final List<ODBinaryFilesDataset> binaryFiles = filesDatasetRepository
				.findBinaryFilesDatasetByDatasetId(datasetId);
		if (binaryFiles != null) {
			for (ODBinaryFilesDataset file : binaryFiles) {
				filesDatasetRepository.delete(file);
			}
		}
	}

	@Override
	public void saveBinaryFiles(String id, ODBinaryFilesDataset binaryFiles) {

		final ODBinaryFilesDataset filesEnt = filesDatasetRepository.findBinaryFilesDatasetById(binaryFiles.getId());
		filesEnt.setDatasetId(binaryFiles.getDatasetId());
		filesEnt.setFilesId(binaryFiles.getFilesId());

		filesDatasetRepository.save(filesEnt);

	}

	@Override
	public ODBinaryFilesDataset getBinaryFilesById(String id) {
		return filesDatasetRepository.findBinaryFilesDatasetById(id);
	}

	private ODBinaryFilesDataset getNewBinaryFiles(ODBinaryFilesDataset BinaryFiles) {
		log.debug("BinaryFiles no exist, creating...");
		final ODBinaryFilesDataset d = new ODBinaryFilesDataset();

		d.setDatasetId(BinaryFiles.getDatasetId());
		d.setFilesId(BinaryFiles.getFilesId());

		return filesDatasetRepository.save(d);
	}

	@Override
	public String createNewBinaryFiles(ODBinaryFilesDataset binaryFiles) {
		if (binaryFiles.getId() != null)
			throw new ODBinaryFilesDatasetServiceException("BinaryFiles already exists in Database");

		final ODBinaryFilesDataset dAux = getNewBinaryFiles(binaryFiles);

		return dAux.getId();

	}

	@Transactional
	@Override
	public String updatePublicBinaryFiles(ODBinaryFilesDataset binaryFiles) {
		if (binaryFiles.getId() != null) {
			throw new ODBinaryFilesDatasetServiceException(BINARYFILES_NOT_EXIST);
		} else {
			final ODBinaryFilesDataset d = filesDatasetRepository.findBinaryFilesDatasetById(binaryFiles.getId());
			d.setDatasetId(binaryFiles.getDatasetId());
			d.setFilesId(binaryFiles.getFilesId());

			return d.getId();
		}
	}

	@Override
	public List<ODBinaryFilesDataset> getBinaryFilesByDatasetId(String datasetId) {
		return filesDatasetRepository.findBinaryFilesDatasetByDatasetId(datasetId);
	}
	
	@Override
	public List<ODBinaryFilesDataset> getBinaryFilesByFilesId(String fileId){
		return filesDatasetRepository.findBinaryFilesDatasetByFilesId(fileId);
	}
	
	@Override
	public List<String> getBinaryFileIdsByDatasetId(String datasetId) {
		final List<ODBinaryFilesDataset>  files = filesDatasetRepository.findBinaryFilesDatasetByDatasetId(datasetId);
		List<String> filesIds = new ArrayList<>();
		if (files != null)
			files.forEach(file -> filesIds.add(file.getFilesId()));
		return filesIds;
	}
	
	@Override
	public List<BinaryFile> getBinaryFilesObjectByDatasetId(String datasetId) {
		final List<ODBinaryFilesDataset>  files = filesDatasetRepository.findBinaryFilesDatasetByDatasetId(datasetId);
		List<BinaryFile> binaryFileList = new ArrayList<>();
		if (files != null)
			files.forEach(file -> binaryFileList.add(binaryFileService.getFile(file.getFilesId())));
		return binaryFileList;
	}
	
	
}

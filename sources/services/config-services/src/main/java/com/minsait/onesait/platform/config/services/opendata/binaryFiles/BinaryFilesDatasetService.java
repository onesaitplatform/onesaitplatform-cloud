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

import java.util.List;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.ODBinaryFilesDataset;

public interface BinaryFilesDatasetService {

	List<String> getAllIds();

	List<ODBinaryFilesDataset> getAllBinaryFilesDatasets();

	void deleteBinaryFilesDatasetByDataset(String id);

	void saveBinaryFiles(String id, ODBinaryFilesDataset binaryFiles);

	ODBinaryFilesDataset getBinaryFilesById(String id);

	String createNewBinaryFiles(ODBinaryFilesDataset binaryFilesCreate);

	String updatePublicBinaryFiles(ODBinaryFilesDataset binaryFiles);

	List<ODBinaryFilesDataset> getBinaryFilesByDatasetId(String datasetId);

	List<ODBinaryFilesDataset> getBinaryFilesByFilesId(String fileId);

	List<String> getBinaryFileIdsByDatasetId(String datasetId);

	List<BinaryFile> getBinaryFilesObjectByDatasetId(String datasetId);
}

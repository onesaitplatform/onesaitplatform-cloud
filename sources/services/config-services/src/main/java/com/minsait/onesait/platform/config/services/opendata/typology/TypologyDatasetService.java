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
package com.minsait.onesait.platform.config.services.opendata.typology;

import java.util.List;

import com.minsait.onesait.platform.config.model.ODTypologyDataset;

public interface TypologyDatasetService {

	List<String> getAllIds();

	List<ODTypologyDataset> getAllTypologiesDatasets();

	void deleteTypologyDatasetById(String id);
	
	void deleteTypologyDatasetByDatasetId(String datasetId);

	void saveTypologyDataset(String id, ODTypologyDataset typology);

	ODTypologyDataset getTypologyById(String id);

	String createNewTypologyDataset(ODTypologyDataset typologyCreate);

	String updateTypologyIdTypologyDataset(ODTypologyDataset typology);
	
	ODTypologyDataset getTypologyByDatasetId(String datasetId);
	
	List<ODTypologyDataset> getTypologyByTypologyID(String datasetId);

	String getTypologyIdentificationByDatasetId(String datasetId);

	String getTypologyIdByDatasetId(String datasetId);
}

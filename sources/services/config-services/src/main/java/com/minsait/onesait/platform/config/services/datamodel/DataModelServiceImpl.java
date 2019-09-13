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
package com.minsait.onesait.platform.config.services.datamodel;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.DataModel.MainType;
import com.minsait.onesait.platform.config.repository.DataModelRepository;

@Service
public class DataModelServiceImpl implements DataModelService {

	@Autowired
	private DataModelRepository dataModelRepository;

	@Override
	public void deleteDataModel(String id) {
		dataModelRepository.delete(id);
	}

	@Override
	public void createDataModel(DataModel dataModel) {
		dataModelRepository.save(dataModel);
	}

	@Override
	public List<MainType> getAllDataModelsTypes() {
		return Arrays.asList(DataModel.MainType.values());
	}

	@Override
	public List<DataModel> getAllDataModels() {
		return dataModelRepository.findAll();
	}

	@Override
	public List<DataModel> getDataModelsByCriteria(String id, String name, String description) {
		return dataModelRepository.findByIdOrNameOrDescription(id, name, description);
	}

	@Override
	public DataModel getDataModelById(String dataModelId) {
		return dataModelRepository.findById(dataModelId);
	}

	@Override
	public DataModel getDataModelByName(String dataModelName) {
		return dataModelRepository.findByName(dataModelName).get(0);
	}

	@Override
	public boolean dataModelExists(DataModel datamodel) {
		List<DataModel> datamodelList = dataModelRepository.findByName(datamodel.getName());
		return !(datamodelList == null || datamodelList.isEmpty());
	}

	@Override
	public void updateDataModel(DataModel datamodel) {
		DataModel oldDataModel = this.dataModelRepository.findById(datamodel.getId());
		if (oldDataModel != null) {
			oldDataModel.setName(datamodel.getName());
			oldDataModel.setLabels(datamodel.getLabels());
			oldDataModel.setType(datamodel.getType());
			oldDataModel.setDescription(datamodel.getDescription());
			oldDataModel.setJsonSchema(datamodel.getJsonSchema());
			this.dataModelRepository.save(oldDataModel);
		}
	}

}

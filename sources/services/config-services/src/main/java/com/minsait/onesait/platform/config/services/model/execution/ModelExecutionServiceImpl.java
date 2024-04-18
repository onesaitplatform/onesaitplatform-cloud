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
package com.minsait.onesait.platform.config.services.model.execution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.repository.ModelExecutionRepository;

@Service
public class ModelExecutionServiceImpl implements ModelExecutionService {

	@Autowired
	private ModelExecutionRepository modelExecutionRepository;

	@Override
	public ModelExecution create(ModelExecution modelExecution) {
		return modelExecutionRepository.save(modelExecution);
	}

	@Override
	public List<ModelExecution> findAllExecutionModels() {
		return modelExecutionRepository.findAll();
	}

	@Override
	public ModelExecution getModelExecutionById(String id) {
		return modelExecutionRepository.findById(id);
	}

	@Override
	public List<ModelExecution> findExecutionModelsByModel(Model model) {
		return modelExecutionRepository.findByModel(model);
	}

}

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
package com.minsait.onesait.platform.config.services.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ModelExecutionRepository;
import com.minsait.onesait.platform.config.services.exceptions.ModelServiceException;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class ModelExecutionServiceImpl implements ModelExecutionService {

	@Autowired
	private ModelExecutionRepository modelExecutionRepository;

	@Autowired
	private NotebookService notebookService;

	@Autowired
	private UserService userService;

	private static final String ERROR_NOT_POSSIBLE_CLONE_NOTEBOOK = "Not possible to clone notebook to save model execution";
	private static final String ERROR_USER_NOT_FOUND = "User not found";

	@Override
	public ModelExecution save(ModelExecution modelExecution) {
		return modelExecutionRepository.save(modelExecution);
	}

	@Override
	public List<ModelExecution> findAllExecutionModels() {
		return modelExecutionRepository.findAll();
	}

	@Override
	public ModelExecution getModelExecutionById(String id) {
		return modelExecutionRepository.findById(id).orElse(null);
	}

	@Override
	public List<ModelExecution> findExecutionModelsByModel(Model model) {
		return modelExecutionRepository.findByModel(model);
	}

	@Override
	public ModelExecution findByIdentification(String identification) {
		return modelExecutionRepository.findByIdentification(identification);
	}

	@Override
	public ModelExecution findModelExecutionByExecutionId(String executionId) {
		return modelExecutionRepository.findByIdEject(executionId);
	}

	@Override
	public ModelExecution findModelExecutionByIdentificationAndUserId(String identification, String userId) {
		final User user = userService.getUser(userId);
		if (user == null) {
			throw new ModelServiceException(ModelServiceException.Error.USER_NOT_FOUND, ERROR_USER_NOT_FOUND);
		}
		return modelExecutionRepository.findByIdentificationAndUser(identification, user);
	}

	@Override
	public ModelExecution cloneNotebookAndSave(Model model, String idEject, String identification, String description,
			String params, String userId) {

		final User user = userService.getUser(userId);

		if (user == null) {
			throw new ModelServiceException(ModelServiceException.Error.USER_NOT_FOUND, ERROR_USER_NOT_FOUND);
		}

		final String cloneNotebookName = model.getNotebook().getIdentification() + "_"
				+ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
		final String idzep = model.getNotebook().getIdzep();
		final Notebook clonedNotebook = notebookService.cloneNotebook(cloneNotebookName, idzep, userId);

		if (clonedNotebook == null) {
			throw new ModelServiceException(ModelServiceException.Error.BAD_RESPONSE_FROM_NOTEBOOK_SERVICE,
					ERROR_NOT_POSSIBLE_CLONE_NOTEBOOK);
		}

		final ModelExecution modelExecution = new ModelExecution();
		modelExecution.setParameters(params);
		modelExecution.setIdEject(idEject);
		modelExecution.setModel(model);
		modelExecution.setUser(user);
		modelExecution.setIdZeppelin(clonedNotebook.getIdzep());
		modelExecution.setDescription(description);
		modelExecution.setIdentification(identification);

		return save(modelExecution);
	}

}

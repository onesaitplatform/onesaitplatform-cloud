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
package com.minsait.onesait.platform.config.services.model;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.model.dto.ModelServiceDTO;

public interface ModelService {

	List<Model> getModelsByIdentification(String identification);

	List<String> getAllIdentifications();

	void createModel(Model model);

	Model getModelToUpdate(String id);

	void updateModel(Model model);
	
	String executeModel(String id, String parameters, String dashboardUrl, String notebookUrl, String userId, boolean returnData);

	Model getModelById(String id);

	Model getModelByIdentification(String identification);

	List<Model> findAllModels();

	List<Model> findAllModelsByUser(String userId);

	void deleteModel(String id);

	boolean isIdValid(String identification);

	void createModel(Model modelp, Category category, Subcategory subcategory, HttpServletRequest httpServletRequest);

	void updateModel(Model model, Category category, Subcategory subcategory, HttpServletRequest request);

	List<Model> findAllModelsByUserHasPermission(User user);

	List<ModelServiceDTO> getModelsByCategoryAndSubcategory(String category, String subcategory, String dashboardUrl,
			String notebookUrl, String userId);

	public Model getModelByIdentificationAndUser(String identification, String userId);

	ModelServiceDTO modelToModelServiceDTO(Model model);

	void raiseExceptionIfIncorrect(List<ParameterModel> modelParams, String jsonParamsAsString);

}

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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.CategoryRelation.Type;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.ModelRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.ModelServiceException;
import com.minsait.onesait.platform.config.services.parametermodel.ParameterModelService;

@Service
public class ModelServiceImpl implements ModelService {

	@Autowired
	private ModelRepository modelRepository;

	@Autowired
	private ParameterModelService parameterModelService;

	@Autowired
	private CategoryRelationRepository categoryRelationRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public List<Model> getModelsByIdentification(String identification) {

		identification = identification == null ? "" : identification;

		List<Model> models = modelRepository.findByIdentificationLike(identification);

		return models;
	}

	@Override
	public List<String> getAllIdentifications() {
		List<Model> models = modelRepository.findAllByOrderByIdentificationAsc();

		final List<String> identifications = new ArrayList<String>();
		for (final Model model : models) {
			identifications.add(model.getIdentification());

		}
		return identifications;
	}

	@Override
	public void createModel(Model model) {
		modelRepository.save(model);
	}

	@Override
	public Model getModelToUpdate(String id) {

		return modelRepository.findById(id);
	}

	@Override
	public void updateModel(Model model) {
		modelRepository.save(model);
	}

	@Override
	public Model getModelById(String id) {

		return modelRepository.findById(id);
	}

	@Override
	public List<Model> findAllModels() {

		return modelRepository.findAll();
	}

	@Override
	public void deleteModel(String id) {
		Model model = modelRepository.findById(id);
		if (model != null) {
			modelRepository.delete(model);
			CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(model.getId());
			categoryRelationRepository.delete(categoryRelation);
		}
	}

	@Override
	public Model getModelByIdentification(String identification) {
		return modelRepository.findByIdentification(identification).get(0);
	}

	@Override
	public boolean isIdValid(String identification) {

		final String regExp = "^[^\\d].*";
		return (identification.matches(regExp));
	}

	@Override
	public void createModel(Model modelp, Category category, Subcategory subcategory,
			HttpServletRequest httpServletRequest) {
		if (modelRepository.findByIdentification(modelp.getIdentification()).isEmpty()) {
			try {

				Model modelAux = modelRepository.save(modelp);

				CategoryRelation relation = new CategoryRelation();
				relation.setCategory(category.getId());
				relation.setSubcategory(subcategory.getId());
				relation.setType(Type.MODEL);
				relation.setTypeId(modelAux.getId());
				categoryRelationRepository.save(relation);

				parameterModelService.createParameterModel(httpServletRequest, modelAux);

			} catch (Exception e) {
				throw new ModelServiceException("Problems creating the model: " + e.getMessage());
			}

		} else {
			throw new ModelServiceException("Model with identification: " + modelp.getIdentification() + " exists");
		}
	}

	@Override
	public void updateModel(Model model, Category category, Subcategory subcategory, HttpServletRequest request) {
		if (!modelRepository.findByIdentification(model.getIdentification()).isEmpty()) {
			try {
				String idOld = model.getId();
				Model modelAux = modelRepository.save(model);

				parameterModelService.updateParameterModel(request, modelAux);

				CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(idOld);
				categoryRelation.setTypeId(modelAux.getId());
				categoryRelation.setCategory(category.getId());
				categoryRelation.setSubcategory(subcategory.getId());
				categoryRelationRepository.save(categoryRelation);

			} catch (Exception e) {
				throw new ModelServiceException("Problems updating the model: " + e.getMessage());
			}

		} else {
			throw new ModelServiceException("Model with identification: " + model.getIdentification() + " not exists");
		}

	}

	@Override
	public List<Model> findAllModelsByUser(String userId) {
		User user = userRepository.findByUserId(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return modelRepository.findAll();
		} else {
			return modelRepository.findByUser(user);
		}
	}

	@Override
	public List<Model> findAllModelsByUserHasPermission(User user) {

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return modelRepository.findAll();
		} else {
			return modelRepository.findByUserNoAdministratorIsOwnerOrHasPermission(user);
		}
	}

}

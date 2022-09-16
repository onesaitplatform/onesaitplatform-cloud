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
package com.minsait.onesait.platform.config.services.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.ModelRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.ModelServiceException;
import com.minsait.onesait.platform.config.services.model.dto.ModelServiceDTO;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.parametermodel.ParameterModelService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

	@Autowired
	private ModelRepository modelRepository;

	@Autowired
	private ParameterModelService parameterModelService;

	@Autowired
	private CategoryRelationRepository categoryRelationRepository;

	@Autowired
	CategoryRelationService categoryRelationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private SubcategoryRepository subcategoryRepository;

	@Autowired
	private NotebookService notebookService;

	@Autowired
	private UserService userService;

	private static final String RESULT_STR = "result";
	private static final String DATA_STR = "data";
	private static final String ERROR_USER_NOT_FOUND = "Invalid input data: User not found";
	private static final String MISSING_PARAMETER = "There are missing parameters";
	private static final String ASLFRAME_STR = "?asIframe";
	private static final String NOTEBOOK_STR = "#/notebook/";
	private static final String PARAGRAPH_STR = "/paragraph/";

	public Model getModelByIdentificationUserOrId(String identificationOrId, String userId) {
		Model model = getModelById(identificationOrId);

		if (model == null) {
			model = getModelByIdentificationAndUser(identificationOrId, userId);
		}
		return model;
	}

	@Override
	public void raiseExceptionIfIncorrect(List<ParameterModel> modelParams, String jsonParamsAsString) {
		if (!modelParams.isEmpty()) {
			final JSONObject jsonParams = new JSONObject(jsonParamsAsString);
			for (final ParameterModel param : modelParams) {

				if (!jsonParams.has(param.getIdentification())) {
					throw new ModelServiceException(ModelServiceException.Error.MISSING_PARAMETER,
							MISSING_PARAMETER + ": " + param.getIdentification());
				}
			}
		}
	}

	@Override
	public ModelServiceDTO modelToModelServiceDTO(Model model) {

		Category category = null;
		Subcategory subcategory = null;
		List<ParameterModel> parameters;

		final CategoryRelation categoryRelation = categoryRelationService.getByTypeIdAndType(model.getId(),
				Category.Type.MODEL);
		if (categoryRelation != null) {

			category = categoryRepository.findById(categoryRelation.getCategory());
			subcategory = subcategoryRepository.findById(categoryRelation.getSubcategory());
		}
		parameters = parameterModelService.findAllParameterModelsByModel(model);

		return new ModelServiceDTO(model, category, subcategory, null, parameters);
	}

	@Override
	public List<Model> getModelsByIdentification(String identification) {

		identification = identification == null ? "" : identification;
		return modelRepository.findByIdentificationLike(identification);
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<Model> models = modelRepository.findAllByOrderByIdentificationAsc();

		final List<String> identifications = new ArrayList<>();
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

		return modelRepository.findById(id).orElse(null);
	}

	@Override
	public void updateModel(Model model) {
		modelRepository.save(model);
	}

	private ResponseEntity<?> executeConfigurationParagraphFromModel(Model model, JSONObject finalJson) {
		ResponseEntity<?> response;
		try {
			response = notebookService.runParagraph(model.getNotebook().getIdzep(), model.getInputParagraphId(),
					finalJson.toString());
		} catch (final Exception e) {
			response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private ResponseEntity<?> executeAllNotebookParagraphsFromModel(Model model) {
		ResponseEntity<?> response;
		try {
			response = notebookService.runAllParagraphs(model.getNotebook().getIdzep());
		} catch (final Exception e) {
			response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private JSONObject generateExecutionResponse(Model model, String idEject, String dashboardUrl, String notebookUrl,
			boolean returnData) throws URISyntaxException, IOException {
		final JSONObject result = new JSONObject();

		result.put("idEject", idEject);
		if (model.getDashboard() != null) {
			result.put(RESULT_STR, dashboardUrl + model.getDashboard().getId());

		} else if (model.getOutputParagraphId() != null) {
			final String url = notebookUrl + "#/notebook/" + model.getNotebook().getIdzep() + "/paragraph/"
					+ model.getOutputParagraphId() + "?asIframe";
			result.put(RESULT_STR, url);

			if (returnData) {
				final String paragraphOutput = notebookService.getParagraphOutputMessage(model.getNotebook().getIdzep(),
						model.getOutputParagraphId());
				result.put(DATA_STR, paragraphOutput);
			}

		}
		return result;
	}

	@Override
	public String executeModel(String id, String parameters, String dashboardUrl, String notebookUrl, String userId,
			boolean returnData) {
		// INFO: parameters must be on json format {"param1": "value1", "param2":
		// "value2", ...}
		JSONObject result = new JSONObject();
		final JSONObject json = new JSONObject();
		final JSONObject finalJson = new JSONObject();
		final Model model = getModelById(id);

		if (model != null) {

			if (notebookService.hasUserPermissionForNotebook(model.getNotebook().getIdzep(), userId)) {

				final String idEject = UUID.randomUUID().toString();
				json.put("id_ejec", idEject);
				json.put("params", parameters);
				finalJson.put("params", json);

				log.info("Attemp to execute configuration paragraph from model {}", model.getIdentification());
				final ResponseEntity<?> response = executeConfigurationParagraphFromModel(model, finalJson);

				if (response.getStatusCode() == HttpStatus.OK) {
					log.info("Executed configuration paragraph from model {}", model.getIdentification());
					log.info("Attemp to execute model {}", model.getIdentification());

					final ResponseEntity<?> responseAux = executeAllNotebookParagraphsFromModel(model);
					if (responseAux.getStatusCode() == HttpStatus.OK) {
						try {
							result = generateExecutionResponse(model, idEject, dashboardUrl, notebookUrl, returnData);
						} catch (URISyntaxException | IOException e) {
							log.error("Error running the notebook: {} - {}" + model.getNotebook().getIdentification(),
									e.getMessage());
							throw new ModelServiceException(
									ModelServiceException.Error.BAD_RESPONSE_FROM_NOTEBOOK_SERVICE,
									"Error running the notebook: {} - {}" + model.getNotebook().getIdentification()
											+ ", " + e.getMessage());
						}
						return result.toString();
					} else {
						log.error("Error running the notebook: " + model.getNotebook().getIdentification());
						throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND,
								"Error running the notebook: " + model.getNotebook().getIdentification());
					}
				} else {
					log.error("Error running the paragraph of configuration: " + model.getInputParagraphId());
					throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND,
							"Error running the paragraph of configuration: " + model.getInputParagraphId());
				}
			} else {
				log.error("User not allowed for notebook: " + model.getNotebook().getIdentification());
				throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND,
						"User not allowed for notebook: " + model.getNotebook().getIdentification());
			}
		} else {
			log.error("Model not found with id: " + id);
			throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND, "Model not found with id: " + id);
		}

	}

	@Override
	public Model getModelById(String id) {

		return modelRepository.findById(id).orElse(null);
	}

	@Override
	public List<Model> findAllModels() {

		return modelRepository.findAll();
	}

	@Override
	public void deleteModel(String id) {
		final Model model = modelRepository.findById(id).orElse(null);
		if (model != null) {
			modelRepository.delete(model);
			final CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(model.getId());
			categoryRelationRepository.delete(categoryRelation);
		}
	}

	@Override
	public Model getModelByIdentification(String identification) {
		return modelRepository.findByIdentification(identification).get(0);
	}

	@Override
	public Model getModelByIdentificationAndUser(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user == null) {
			throw new ModelServiceException(ModelServiceException.Error.USER_NOT_FOUND, ERROR_USER_NOT_FOUND);
		}
		return modelRepository.findByUserAndIdentification(user, identification);
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

				final Model modelAux = modelRepository.save(modelp);

				categoryRelationService.createCategoryRelation(modelAux.getId(), category, subcategory, Category.Type.MODEL);
				parameterModelService.createParameterModel(httpServletRequest, modelAux);

			} catch (final Exception e) {
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
				final String idOld = model.getId();
				final Model modelAux = modelRepository.save(model);

				parameterModelService.updateParameterModel(request, modelAux);
				
				final CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(idOld);
				if (categoryRelation == null) {
					categoryRelationService.createCategoryRelation(idOld, category, subcategory, Category.Type.MODEL);
				} else {
					categoryRelationService.updateCategoryRelation(categoryRelation, idOld, category, subcategory);					
				}

			} catch (final Exception e) {
				throw new ModelServiceException("Problems updating the model: " + e.getMessage());
			}

		} else {
			throw new ModelServiceException("Model with identification: " + model.getIdentification() + " not exists");
		}

	}

	@Override
	public List<Model> findAllModelsByUser(String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return modelRepository.findAll();
		} else {
			return modelRepository.findByUser(user);
		}
	}

	@Override
	public List<Model> findAllModelsByUserHasPermission(User user) {

		if (userService.isUserAdministrator(user)) {
			return modelRepository.findAll();
		} else {
			return modelRepository.findByUserNoAdministratorIsOwnerOrHasPermission(user);
		}
	}

	@Override
	public List<ModelServiceDTO> getModelsByCategoryAndSubcategory(String category, String subcategory,
			String dashboardUrl, String notebookUrl, String userId) {

		final User user = userRepository.findByUserId(userId);
		if (user == null) {
			throw new ModelServiceException(ModelServiceException.Error.USER_NOT_FOUND, ERROR_USER_NOT_FOUND);
		}
		final List<Model> models = findAllModelsByUserHasPermission(user);

		return filterModelsByCategoryAndSubcategory(models, category, subcategory, dashboardUrl, notebookUrl);
	}

	private List<ModelServiceDTO> filterModelsByCategoryAndSubcategory(List<Model> models, String category,
			String subcategory, String dashboardUrl, String notebookUrl) {
		final List<ModelServiceDTO> modelsResult = new ArrayList<>();
		for (final Model m : models) {
			final CategoryRelation categoryRelation = categoryRelationService.getByTypeIdAndType(m.getId(),
					Category.Type.MODEL);
			if (categoryRelation != null) {

				final Category c = categoryRepository.findById(categoryRelation.getCategory());
				final Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

				if (c != null && subc != null && category.equalsIgnoreCase(c.getIdentification())
						&& subcategory.equalsIgnoreCase(subc.getIdentification())) {
					final List<ParameterModel> parameterModelDTOs = new ArrayList<>();
					final List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(m);
					for (final ParameterModel param : parameters) {
						parameterModelDTOs.add(param);
					}

					if (m.getDashboard() != null) {
						modelsResult.add(new ModelServiceDTO(m.getId(), m.getIdentification(), m.getDescription(),
								m.getNotebook().getIdentification(), m.getDashboard().getIdentification(),
								c.getIdentification(), subc.getIdentification(), null, m.getInputParagraphId(),
								dashboardUrl + m.getDashboard().getId(), parameterModelDTOs,
								m.getCreatedAt().toString()));
					} else {
						modelsResult.add(new ModelServiceDTO(m.getId(), m.getIdentification(), m.getDescription(),
								m.getNotebook().getIdentification(), null, c.getIdentification(),
								subc.getIdentification(), m.getOutputParagraphId(), m.getInputParagraphId(),
								notebookUrl + NOTEBOOK_STR + m.getNotebook().getIdzep() + PARAGRAPH_STR
										+ m.getOutputParagraphId() + ASLFRAME_STR,
								parameterModelDTOs, m.getCreatedAt().toString()));
					}

				}
			}
		}

		return modelsResult;
	}

}

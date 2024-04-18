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
package com.minsait.onesait.platform.controlpanel.rest.management.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.ModelServiceException;
import com.minsait.onesait.platform.config.services.model.ModelExecutionService;
import com.minsait.onesait.platform.config.services.model.ModelService;
import com.minsait.onesait.platform.config.services.model.dto.ModelServiceDTO;
import com.minsait.onesait.platform.config.services.parametermodel.ParameterModelService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.model.ModelController;
import com.minsait.onesait.platform.controlpanel.rest.management.models.model.ExecutionDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.models.model.ModelDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.models.model.ModelsResponseErrorDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.models.model.ParameterModelDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/models")
@EnableAutoConfiguration
@Slf4j
public class ModelsRestControllerImpl implements ModelsRestController {

	private static final String MODEL_NOT_FOUND = "Model is not found.";
	private static final String MODEL_EXECUTION_NOT_FOUND = "Model execution not found";
	private static final String DUPLICATED_ID = "Conflict: duplidated id";
	private static final String USER_NOT_FOUND = "User is not found.";
	private static final String NOT_ALLOWED = "Not Allowed to check model elements of other user";
	private static final String ERROR_SAVE_MODEL_EXECUTION = "Not possible to save model execution";
	private static final String ERROR_EXECUTE_MODEL = "Not possible to execute model";
	private static final String ASLFRAME_STR = "?asIframe";
	private static final String NOTEBOOK_STR = "#/notebook/";
	private static final String PARAGRAPH_STR = "/paragraph/";
	private static final String ADMINISTRATOR_STR = "administrator";

	@Autowired
	private CategoryRelationService categoryRelationService;

	@Autowired
	private ModelExecutionService modelExecutionService;

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subcategoryService;

	@Autowired
	private ParameterModelService parameterModelService;
	
	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private AppWebUtils utils;

	private String dashboardUrl;

	private String notebookUrl;

	@Autowired
	private ModelController modelController;

	@Autowired
	private ModelService modelService;

	@PostConstruct
	public void init() {
		notebookUrl = resourcesService.getUrl(Module.NOTEBOOK, ServiceUrl.URL);
		dashboardUrl = resourcesService.getUrl(Module.DASHBOARDENGINE, ServiceUrl.ONLYVIEW);
	}

	private String generateOutputUrl(Model model) {
		String modelUrl = "";
		if (model.getDashboard() != null) {
			
			modelUrl = dashboardUrl + model.getDashboard().getId(); 
		} else if (model.getNotebook() != null){
			modelUrl = notebookUrl + NOTEBOOK_STR + model.getNotebook().getIdzep() + PARAGRAPH_STR
							+ model.getOutputParagraphId() + ASLFRAME_STR;
		}
		return modelUrl;
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models")
	@GetMapping(value = "/")
	public ResponseEntity<?> list() {
		
		ResponseEntity<?> response;
		String loggedUser = utils.getUserId();
		List<ModelDTO> modelsResult = new ArrayList<>();
		
		try {
			List<Model> models = modelService.findAllModelsByUser(loggedUser);

			if (models.isEmpty() ) {
				response = new ResponseEntity<>(modelsResult, HttpStatus.NO_CONTENT);
			}
			else {
				for (Model model: models) {
					ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(model);
					modelServiceDTO.setOutputURL(generateOutputUrl(model));
					modelsResult.add(new ModelDTO(modelServiceDTO));
				}
				response = new ResponseEntity<>(modelsResult, HttpStatus.OK);
			}
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in list: {}, {}", e.getError().name(), e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		return response;
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models by category and subcategory")
	@GetMapping(value = "/category/{category}/subcategory/{subcategory}")
	public ResponseEntity<?> getModelsByCategoryAndSubcategory(
		@ApiParam(value = "Model category", required = true) @PathVariable("category") String category, 
		@ApiParam(value = "Model subcategory", required = true) @PathVariable("subcategory") String subcategory) {
		
		ResponseEntity<?> response;
		
		String loggedUser = utils.getUserId();
		List<ModelDTO> modelsResult = new ArrayList<>();
		
		try {
			List<ModelServiceDTO> models = modelService.getModelsByCategoryAndSubcategory(
					category, subcategory, dashboardUrl, notebookUrl, loggedUser);
			
			if (models.isEmpty() ) {
				response = new ResponseEntity<>(modelsResult, HttpStatus.NO_CONTENT);
			}
			else {
				for (ModelServiceDTO model: models) {
					modelsResult.add(new ModelDTO(model));
				}
				response = new ResponseEntity<>(modelsResult, HttpStatus.OK);
			}
		} catch (ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getModelsByCategoryAndSubcategory: {}, {}", e.getError().name(), e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		
		return response;
		
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO.class))
	@ApiOperation(value = "Get Model by user and name of the model")
	@GetMapping(value = "/model/{modelIdentification}/user/{userId}")
	public ResponseEntity<?> getModelByUserAndModelId(
			@ApiParam(value = "Model identification or id", required = true) @PathVariable("modelIdentification") String modelIdentification, 
			@ApiParam(value = "User identification", required = true) @PathVariable("userId") String userId) {
		
		ResponseEntity<?> response;
		String loggedUser = utils.getUserId();
		final User user = userService.getUser(loggedUser);
		
		try {
			
			if (!(user.getUserId().equals(userId) || utils.isAdministrator())) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}
			
			Model model = modelService.getModelByIdentificationAndUser(modelIdentification, userId);
			
			if (model == null) {
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			else {
				ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(model);
				modelServiceDTO.setOutputURL(generateOutputUrl(model));
				response = new ResponseEntity<>(new ModelDTO(modelServiceDTO), HttpStatus.OK);
			}
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getModelByUserAndModelId: {}, {}", e.getError().name(), e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		return response;
		
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO.class))
	@ApiOperation(value = "Get Model by name of the model")
	@GetMapping(value = "/{modelIdentification}")
	public ResponseEntity<?> getModelByUserAndModelIdentification(
			@ApiParam(value = "Model identification or id", required = true) @PathVariable("modelIdentification") String modelIdentification) {
		
		ResponseEntity<?> response;
		String loggedUser = utils.getUserId();
		
		try {
			
			Model model = modelService.getModelByIdentificationAndUser(modelIdentification, loggedUser);
			
			if (model == null) {
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			else {
				ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(model);
				modelServiceDTO.setOutputURL(generateOutputUrl(model));
				response = new ResponseEntity<>(new ModelDTO(modelServiceDTO), HttpStatus.OK);
			}
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getModelByUserAndModelIdentification: {}, {}", e.getError().name(), e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		return response;
		
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models by user,  category and subcategory")
	@GetMapping(value = "/category/{category}/subcategory/{subcategory}/user/{userId}")
	public ResponseEntity<?> getModelsByUserIdAndCategoryAndSubcaegory(
			@ApiParam(value = "User identification", required = true) @PathVariable("userId") String userId,
			@ApiParam(value = "Category", required = true) @PathVariable("category") String category,
			@ApiParam(value = "Subcategory", required = true) @PathVariable("subcategory") String subcategory) {
		
		ResponseEntity<?> response;
		List<ModelDTO> modelsResult = new ArrayList<>();
		String loggedUser = utils.getUserId();
		final User user = userService.getUser(loggedUser);
		
		try {
			
			if (!(user.getUserId().equals(userId) || utils.isAdministrator())) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}
			
			List<ModelServiceDTO> models = modelService.getModelsByCategoryAndSubcategory(category, subcategory, 
					dashboardUrl, notebookUrl, userId);
			
			if (models.isEmpty() ) {
				response = new ResponseEntity<>(modelsResult, HttpStatus.NO_CONTENT);
			}
			else {
				for (ModelServiceDTO model: models) {
					modelsResult.add(new ModelDTO(model));
				}
				response = new ResponseEntity<>(modelsResult, HttpStatus.OK);
			}
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getByUserIdAndCategoryAndSubcaegory: {}, {}", e.getError().name(), e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		return response;
	}
		
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Execute model")
	@PostMapping(value = "/execute/{modelIdentification}")
	public ResponseEntity<?> execute (
			@ApiParam(value = "Model identification or id", required = true) @PathVariable("modelIdentification") String modelIdentification,
			@ApiParam(value = "User identification (optional, if not passed use logged user)", required = false) @RequestParam(name = "userId", required = false, defaultValue = "") String userId,
			@ApiParam(value = "Return output data in response") @RequestParam(required = false, defaultValue = "true") boolean returnData,
			@ApiParam(value = "Parameters needed to execute the model ({\"param\":\"value\", ...})") @RequestBody(required = true) String params) {
		
		ResponseEntity<?> response;
		
		try {
			
			final String loggedUser = utils.getUserId();
			final User user = userService.getUser(loggedUser);
			
			if (userId.equals("")) { // empy string is default
				userId = loggedUser;
			}
			
			if (!(user.getUserId().equals(userId) || utils.isAdministrator())) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}

			Model model = modelService.getModelByIdentificationAndUser(modelIdentification, userId);
			
			if (model == null) {
				throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND, MODEL_NOT_FOUND);
			}
			
			ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(model);
			modelService.raiseExceptionIfIncorrect(modelServiceDTO.getParameters(), params);

			final String result = modelService.executeModel(modelServiceDTO.getId(), params,  dashboardUrl, notebookUrl, userId, returnData);
			response = new ResponseEntity<>(result, HttpStatus.OK);

		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in execute: {}, {}", e.getError().name(), e.getMessage());
		} catch (final Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(ERROR_EXECUTE_MODEL + ": " + e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		
		return response;
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ExecutionDTO.class))
	@ApiOperation(value = "Show an execution of model")
	@GetMapping(value = "/executions/{executionId}")
	public ResponseEntity<?> getExecutionByIdEjec(
			@ApiParam(value = "Execution ID", required = true) @PathVariable("executionId")  String executionId) {
		
		ResponseEntity<?> response;
		
		try {

			ModelExecution modelExecution = modelExecutionService.findModelExecutionByExecutionId(executionId);
			
			if (modelExecution == null) {
				throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND, MODEL_EXECUTION_NOT_FOUND);
			}
			if (!(utils.isAdministrator() || utils.getUserId().equals(modelExecution.getUser().getUserId()))) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}
			
			ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(modelExecution.getModel());
			Category category = categoryService.getCategoryById(modelServiceDTO.getCategorymodel());
			Subcategory subcategory = subcategoryService.getSubcategoryById(modelServiceDTO.getSubcategorymodel());
			
			ExecutionDTO executionDTO = new ExecutionDTO(modelExecution, category, subcategory);
			response = new ResponseEntity<>(executionDTO, HttpStatus.OK);
					
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getExecutionByIdEjec: {}, {}", e.getError().name(), e.getMessage());
		} catch (final Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO("There was an error executing the model: " + e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		
		return response;	

	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ExecutionDTO.class))
	@ApiOperation(value = "Show an execution of model")
	@GetMapping(value = "/executions/identification/{executionIdentification}/user/{userId}")
	public ResponseEntity<?> getExecutionByExecutionIdAndUser(
			@ApiParam(value = "Execution identification", required = true) @PathVariable("executionIdentification")  String executionIdentification,
			@ApiParam(value = "User identification", required = true) @PathVariable("userId")  String userId) {
		
		ResponseEntity<?> response;
		
		try {

			ModelExecution modelExecution = modelExecutionService.findModelExecutionByIdentificationAndUserId(executionIdentification, userId);
			
			if (modelExecution == null) {
				throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND, MODEL_EXECUTION_NOT_FOUND);
			}
			if (!(utils.isAdministrator() || utils.getUserId().equals(modelExecution.getUser().getUserId()))) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}
			
			ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(modelExecution.getModel());
			Category category = categoryService.getCategoryById(modelServiceDTO.getCategorymodel());
			Subcategory subcategory = subcategoryService.getSubcategoryById(modelServiceDTO.getSubcategorymodel());
			
			ExecutionDTO executionDTO = new ExecutionDTO(modelExecution, category, subcategory);
			response = new ResponseEntity<>(executionDTO, HttpStatus.OK);
					
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getExecutionByExecutionIdAndUser: {}, {}", e.getError().name(), e.getMessage());
		} catch (final Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO("There was an error executing the model: " + e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		
		return response;	

	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ExecutionDTO[].class))
	@ApiOperation(value = "Get List of executions of models")
	@GetMapping(value = "/executions")
	public ResponseEntity<?> getAllExecutions(
		@ApiParam(value = "User identification (optional, if not passed use logged user)", required = false) @RequestParam(name = "userId", required = false, defaultValue = "") String userId) {
		
		ResponseEntity<?> response;
		List<ModelExecution> executions= new ArrayList<>();
		final List<ExecutionDTO> executionsDto = new ArrayList<>();
		
		try {
			
			final String loggedUser = utils.getUserId();
			final User user = userService.getUser(loggedUser);
			
			if (userId.equals("")) { // empty string is default
				userId = loggedUser;
			}
			
			if (!(user.getUserId().equals(userId) || utils.isAdministrator())) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}
			
			if (utils.isAdministrator() && userId.equals(ADMINISTRATOR_STR)) {
				executions.addAll(modelExecutionService.findAllExecutionModels());
			}
			else {
				final User requestUser = userService.getUser(userId);
				if (requestUser == null) {
					throw new ModelServiceException(ModelServiceException.Error.USER_NOT_FOUND, USER_NOT_FOUND);
				}
				final List<Model> models = modelService.findAllModelsByUserHasPermission(requestUser);
				models.forEach(m -> executions.addAll(modelExecutionService.findExecutionModelsByModel(m)));
			}
			
			for (final ModelExecution execution : executions) {

				final CategoryRelation categoryRelation = categoryRelationService
						.getByTypeIdAndType(execution.getModel().getId(), CategoryRelation.Type.MODEL);
				if (categoryRelation != null) {

					final Category category = categoryService.getCategoryById(categoryRelation.getCategory());
					final Subcategory subcategory = subcategoryService.getSubcategoryById(categoryRelation.getSubcategory());
					executionsDto.add(new ExecutionDTO(execution, category, subcategory));
					}
			}
			
			if (executionsDto.isEmpty()) {
				response = new ResponseEntity<>(executionsDto, HttpStatus.NO_CONTENT);
			}
			else {
				response = new ResponseEntity<>(executionsDto, HttpStatus.OK);
			}
			
			
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in getAllExecutions: {}, {}", e.getError().name(), e.getMessage());
		} catch (final Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO("Error getting executions: " + e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		
		return response;
	}
	
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ExecutionDTO.class))
	@ApiOperation(value = "Create the execution of the model")
	@PostMapping(value = "/execution")
	public ResponseEntity<?> createModelExecution(
			@ApiParam(value = "Model execution data") @RequestBody(required = true) @Valid ExecutionDTO execution
			) {
		
		ResponseEntity<?> response;
		
		try {
			
			String userId = execution.getUser();
			String modelIdentification = execution.getModel();
			String executionId = execution.getIdEject();
			String executionName = execution.getIdentification();
			String executionDescription = execution.getDescription();
			String params = execution.getParams();
			
			
			
			final String loggedUser = utils.getUserId();
			final User user = userService.getUser(loggedUser);
			
			if (userId.equals("")) { // empy string is default
				userId = loggedUser;
			}
			
			if (!(user.getUserId().equals(userId) || utils.isAdministrator())) {
				throw new ModelServiceException(ModelServiceException.Error.PERMISSION_DENIED, NOT_ALLOWED);
			}
			
			if (modelExecutionService.findModelExecutionByExecutionId(executionId) != null) {
				throw new ModelServiceException(ModelServiceException.Error.DUPLICATED_ID, DUPLICATED_ID);
			}
		
			Model model = modelService.getModelByIdentificationAndUser(modelIdentification, userId);
			
			if (model == null) {
				throw new ModelServiceException(ModelServiceException.Error.NOT_FOUND, MODEL_NOT_FOUND);
			}
			
			ModelServiceDTO modelServiceDTO = modelService.modelToModelServiceDTO(model);
			modelService.raiseExceptionIfIncorrect(modelServiceDTO.getParameters(), params);
			
			if (modelExecutionService.cloneNotebookAndSave(model, executionId, executionName, executionDescription, params, userId) == null) {
				throw new ModelServiceException(ModelServiceException.Error.GENERIC_ERROR, ERROR_SAVE_MODEL_EXECUTION);
			}
			
			ExecutionDTO executionDTO = new ExecutionDTO(
					executionId, modelServiceDTO.getCategorymodel(), modelServiceDTO.getSubcategorymodel(), executionName, executionDescription, modelServiceDTO.getIdentification(),
					modelServiceDTO.getUserId(), modelServiceDTO.getCreatedAt(), execution.getParams());
			response = new ResponseEntity<>(executionDTO, HttpStatus.OK);
			
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in saveModelExecution: {}, {}", e.getError().name(), e.getMessage());
		} catch (final Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO("There was an error executing the model: " + e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		
		return response;
		
	}
	
	// ------------------------------------- deprecation area -------------------------------------
	
	@Deprecated
	private ResponseEntity<?> tryExecuteModel(Model model, String params, String dashboardUrl, String notebookUrl, String userId, boolean returnData) {
		ResponseEntity<?> response;
		try {
			final String result = modelService.executeModel(model.getId(), params,  dashboardUrl, notebookUrl, userId, returnData);
			response = new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final ModelServiceException e) {
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO(e, e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
			log.error("Error in tryExecuteModel: {}, {}", e.getError().name(), e.getMessage());
		} catch (final Exception e) {
			log.error(e.getMessage());
			ModelsResponseErrorDTO errorDTO = new ModelsResponseErrorDTO("There was an error executing the model: " + e.getMessage());
			response = new ResponseEntity<>(errorDTO, errorDTO.defaultHttpStatus());
		}
		return response;
	}
	
	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Execute model")
	@PostMapping(value = "/executeModel")
	public ResponseEntity<?> executeModel(
			@ApiParam(value = "User identification (optional)", required = false) @RequestParam(name = "userId", required = false, defaultValue = "") String userId,
			@ApiParam(value = "A JSON with parameters needed to execute the model") @RequestParam(required = false, defaultValue = "{}") String params,
			@ApiParam(value = "Model name", required = true) @RequestParam(name = "modelName") String modelName, 
			@ApiParam(value = "Return output data in response") @RequestParam(required = false, defaultValue = "false") boolean returnData) {
		try {

			userId = utils.getUserId(); // deprecated - useless
			final User user = userService.getUser(userId);
			if (user == null) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
				
			Model model = null;

			final Optional<Model> modelOptional = modelService.findAllModelsByUserHasPermission(user).stream()
					.filter(current -> current.getIdentification().equals(modelName))
					.findFirst();

			if (modelOptional.isPresent()) {
				model = modelOptional.get();
			}
			
			if (model != null) {
				final JSONObject jsonParams = new JSONObject(params);
				final List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(model);

				for (final ParameterModel param : parameters) {

					if (!jsonParams.has(param.getIdentification())) {
						return new ResponseEntity<>("There are params missing.", HttpStatus.BAD_REQUEST);
					}
				}
				
				return tryExecuteModel(model, params, dashboardUrl, notebookUrl, userId, returnData);

			} else {
				return new ResponseEntity<>(MODEL_NOT_FOUND, HttpStatus.NOT_FOUND);
			}


		} catch (final Exception e) {
			log.error("Error executing model", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models category and subcategory, using header token to identify the user")
	@PostMapping(value = "/getByCategoryAndSubcategory")
	public ResponseEntity<?> getByCategoryAndSubcategory(
			@ApiParam(value = "Model category", required = true) @RequestParam(name = "category", required = true, defaultValue = "") String category,
			@ApiParam(value = "Model subcategory", required = true) @RequestParam(name = "subcategory", required = true, defaultValue = "") String subcategory) {

		String loggedUser = utils.getUserId();

		if (loggedUser == null) {
			return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		try {
			final User user = userService.getUser(loggedUser);

			if (user != null) {
				final List<Model> models = modelService.findAllModelsByUserHasPermission(user);

				final List<ModelDTO> modelsResult = filterModelsByCategoryAndSubcategory(models, category, subcategory);

				return new ResponseEntity<>(modelsResult, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}
		} catch (final Exception e) {
			log.error("Error getting by Category and Subcategory", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Model and name of the model, using header token to identify the user")
	@PostMapping(value = "/getByModelId")
	public ResponseEntity<?> getByUserHeaderAndModelId(
			@ApiParam(value = "Model identification", required = true) @RequestParam(value = "modelName", required = true) String modelName) {

		String loggedUser = utils.getUserId();

		return getByUserAndModelId(loggedUser, modelName);
	}

	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Models by user, category and subcategory.")
	@PostMapping(value = "/getByUserAndCategoryAndSubcategory")
	public ResponseEntity<?> getByUserAndCategoryAndSubcaegory(
			@ApiParam(value = "User identification", required = true) @RequestParam(name = "userId", required = true) String userId,
			@ApiParam(value = "Category", required = true) @RequestParam(name = "category", required = true) String category,
			@ApiParam(value = "Subcategory", required = true) @RequestParam(name = "subcategory", required = true) String subcategory) {
		try {

			final String loggedUser = utils.getUserId();
			if (loggedUser == null) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userService.getUser(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {
				final User user = userService.getUser(userId);

				if (user != null) {
					final List<Model> models = modelService.findAllModelsByUserHasPermission(user);

					final List<ModelDTO> modelsResult = filterModelsByCategoryAndSubcategory(models, category,
							subcategory);
					return new ResponseEntity<>(modelsResult, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
			} else {
				return new ResponseEntity<>(NOT_ALLOWED, HttpStatus.FORBIDDEN);
			}

		} catch (final Exception e) {
			log.error("Error getting by User, Category and Subcategory", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ModelDTO[].class))
	@ApiOperation(value = "Get Model by user and name of the model")
	@PostMapping(value = "/getByUserAndModelId")
	public ResponseEntity<?> getByUserAndModelId(
			@ApiParam(value = "User identification", required = true) @RequestParam(name = "userId", required = true) String userId,
			@ApiParam(value = "Model identification", required = true) @RequestParam(name = "modelName", required = true) String modelName) {
		try {

			String loggedUser = utils.getUserId();
			if (loggedUser == null) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userService.getUser(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userService.getUser(userId);

				if (user != null) {
					Model m = null;

					final Optional<Model> modelOptional = modelService.findAllModelsByUserHasPermission(user).stream()
							.filter(current -> current.getIdentification().equals(modelName))
							.findFirst();

					if (modelOptional.isPresent()) {
						m = modelOptional.get();
					}
					
					final List<ModelDTO> modelsResult = new ArrayList<>();
					if (m != null) {
						final CategoryRelation categoryRelation = categoryRelationService
								.getByTypeIdAndType(m.getId(), CategoryRelation.Type.MODEL);
						if (categoryRelation != null) {

							final Category c = categoryService.getCategoryById(categoryRelation.getCategory());
							final Subcategory subc = subcategoryService.getSubcategoryById(categoryRelation.getSubcategory());

							final List<ParameterModelDTO> parameterModelDTOs = new ArrayList<>();
							final List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(m);
							for (final ParameterModel param : parameters) {
								parameterModelDTOs
										.add(new ParameterModelDTO(param.getIdentification(), param.getType().name(),
												param.getRangeFrom(), param.getRangeFrom(), param.getEnumerators()));
							}

							if (m.getDashboard() != null) {
								modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
										m.getNotebook().getIdentification(), m.getDashboard().getIdentification(),
										c.getIdentification(), subc.getIdentification(), null, m.getInputParagraphId(),
										dashboardUrl + m.getDashboard().getId(), parameterModelDTOs,
										m.getCreatedAt().toString()));
							} else {
								modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
										m.getNotebook().getIdentification(), null, c.getIdentification(),
										subc.getIdentification(), m.getOutputParagraphId(), m.getInputParagraphId(),
										notebookUrl + NOTEBOOK_STR + m.getNotebook().getIdzep() + PARAGRAPH_STR
												+ m.getOutputParagraphId() + ASLFRAME_STR,
										parameterModelDTOs, m.getCreatedAt().toString()));
							}

						}
					} else {
						return new ResponseEntity<>(MODEL_NOT_FOUND, HttpStatus.NOT_FOUND);
					}
					return new ResponseEntity<>(modelsResult, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
				}

			} else {
				return new ResponseEntity<>(NOT_ALLOWED, HttpStatus.FORBIDDEN);
			}

		} catch (final Exception e) {
			log.error("Error getting by User and ModelId", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Save the execution of the model")
	@PostMapping(value = "/saveExecution")
	public ResponseEntity<?> saveExecution(
			@ApiParam(value = "User identification", required = true) @RequestParam(name = "userId", required = true) String userId,
			@ApiParam(value = "A JSON with parameters needed yo execute the model", required = true) @RequestParam(name = "params", required = true) String params, 
			@ApiParam(value = "Model identification", required = true) @RequestParam(name = "modelName", required = true) String modelName, 
			@ApiParam(value = "Name of the execution", required = true) @RequestParam(name = "executionName", required = true) String executionName, 
			@ApiParam(value = "Description of the execution", required = true) @RequestParam(name = "executionDescription", required = true) String executionDescription, 
			@ApiParam(value = "Execution ID", required = true) @RequestParam(name = "executionId", required = true) String executionId) {
		
		try {

			String loggedUser = utils.getUserId();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userService.getUser(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userService.getUser(userId);
				final JSONObject jsonParams = new JSONObject(params);

				if (user != null) {

					Model model = null;

					final Optional<Model> modelOptional = modelService.findAllModelsByUserHasPermission(user).stream()
							.filter(current -> current.getIdentification().equals(modelName))
							.findFirst();

					if (modelOptional.isPresent()) {
						model = modelOptional.get();
					}
					
					if (model != null) {
						final List<String> listParams = new ArrayList<>();
						final List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(model);

						final Iterator<String> iterator = jsonParams.keys();
						final JSONArray array = new JSONArray();
						while (iterator.hasNext()) {
							final String paramName = iterator.next();
							final String paramValue = jsonParams.getString(paramName);
							array.put(new JSONObject(
									"{\"param\":\"" + paramName + "\",\"value\":\"" + paramValue + "\"}"));
							listParams.add(paramName);
						}

						for (final ParameterModel param : parameters) {

							if (!listParams.contains(param.getIdentification())) {
								return new ResponseEntity<>("There are params missing.", HttpStatus.BAD_REQUEST);
							}
						}

						final String result = modelController.save(model.getId(), array.toString(), executionId,
								executionName, executionDescription);

						if (result == null) {
							return new ResponseEntity<>("There was an error executing the model.",
									HttpStatus.INTERNAL_SERVER_ERROR);
						} else {
							return new ResponseEntity<>(result, HttpStatus.OK);
						}

					} else {
						return new ResponseEntity<>(MODEL_NOT_FOUND, HttpStatus.NOT_FOUND);
					}

				} else {
					return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
			} else {
				return new ResponseEntity<>(NOT_ALLOWED, HttpStatus.FORBIDDEN);
			}

		} catch (final Exception e) {
			log.error("Error saving execution", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = ExecutionDTO[].class))
	@ApiOperation(value = "Get List of executions of models")
	@PostMapping(value = "/getExecutions")
	public ResponseEntity<?> getExecutions(
			@ApiParam(value = "User identification", required = true) @RequestParam(name = "userId", required = true) String userId) {
		
		try {

			String loggedUser = utils.getUserId();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userService.getUser(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userService.getUser(userId);

				if (user != null) {

					final List<ModelExecution> executions = new ArrayList<>();

					if (userService.isUserAdministrator(user)) {
						executions.addAll(modelExecutionService.findAllExecutionModels());
					} else {

						final List<Model> models = modelService.findAllModelsByUserHasPermission(user);

						models.forEach(m -> executions.addAll(modelExecutionService.findExecutionModelsByModel(m)));
					}

					final List<ExecutionDTO> executionsDto = new ArrayList<>();

					for (final ModelExecution execution : executions) {

						final CategoryRelation categoryRelation = categoryRelationService
								.getByTypeIdAndType(execution.getModel().getId(), CategoryRelation.Type.MODEL);
						if (categoryRelation != null) {

							final Category c = categoryService.getCategoryById(categoryRelation.getCategory());
							final Subcategory subc = subcategoryService.getSubcategoryById(categoryRelation.getSubcategory());

							
							final ExecutionDTO executionDto = new ExecutionDTO(execution.getIdEject(), c.getIdentification(),
									subc.getIdentification(), execution.getIdentification(), execution.getDescription(),
									execution.getModel().getIdentification(), execution.getUser().getUserId(), 
									execution.getCreatedAt().toString(), execution.getParameters());

							executionsDto.add(executionDto);
						}

					}

					return new ResponseEntity<>(executionsDto, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
				}

			} else {
				return new ResponseEntity<>(NOT_ALLOWED, HttpStatus.FORBIDDEN);
			}

		} catch (final Exception e) {
			log.error("Error getting execution", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Deprecated
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@ApiOperation(value = "Show an execution of model")
	@PostMapping(value = "/showExecution")
	public ResponseEntity<?> showExecution(
			@ApiParam(value = "User identification", required = true) @RequestParam(name = "userId", required = true) String userId,
			 @ApiParam(value = "Name of the execution", required = true) @RequestParam(name = "executionName", required = true) String executionName) {
		try {

			String loggedUser = utils.getUserId();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userService.getUser(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userService.getUser(userId);

				if (user != null) {

					ModelExecution execution = new ModelExecution();

					if (userService.isUserAdministrator(user)) {
						execution = modelExecutionService.findByIdentification(executionName);
					} else {
						final ModelExecution candidateExecution = modelExecutionService
								.findByIdentification(executionName);
						final Model modelCandidate = candidateExecution.getModel();

						final Optional<Model> modelOptional = modelService.findAllModelsByUserHasPermission(user).stream()
								.filter(m -> m.getId().equals(modelCandidate.getId()))
								.findFirst();

						if (modelOptional.isPresent()) {
							execution = candidateExecution;
						}							
					}
					
					if (execution != null) {
						String url = null;
						if (execution.getModel().getDashboard() != null) {
							url = dashboardUrl + execution.getModel().getDashboard().getId() + "?idExecution="
									+ execution.getIdEject();

						} else if (execution.getModel().getOutputParagraphId() != null) {
							url = notebookUrl + NOTEBOOK_STR + execution.getModel().getNotebook().getIdzep()
									+ PARAGRAPH_STR + execution.getModel().getOutputParagraphId() + ASLFRAME_STR;

						}

						final JSONObject json = new JSONObject();
						json.put("url", url);
						json.put("params", execution.getParameters());

						return new ResponseEntity<>(json.toString(), HttpStatus.OK);
					} else {
						return new ResponseEntity<>("Execution model not found for this user", HttpStatus.NOT_FOUND);
					}

				} else {
					return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
				}
			} else {
				return new ResponseEntity<>(NOT_ALLOWED, HttpStatus.FORBIDDEN);
			}

		} catch (final Exception e) {
			log.error("Error recovering execution", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Deprecated
	private List<ModelDTO> filterModelsByCategoryAndSubcategory(List<Model> models, String category,
			String subcategory) {
		final List<ModelDTO> modelsResult = new ArrayList<>();
		for (final Model m : models) {
			final CategoryRelation categoryRelation = categoryRelationService
					.getByTypeIdAndType(m.getId(), CategoryRelation.Type.MODEL);
			if (categoryRelation != null) {

				final Category c = categoryService.getCategoryById(categoryRelation.getCategory());
				final Subcategory subc = subcategoryService.getSubcategoryById(categoryRelation.getSubcategory());

				if (c != null && subc != null && category.equalsIgnoreCase(c.getIdentification())
						&& subcategory.equalsIgnoreCase(subc.getIdentification())) {
					final List<ParameterModelDTO> parameterModelDTOs = new ArrayList<>();
					final List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(m);
					for (final ParameterModel param : parameters) {
						parameterModelDTOs.add(new ParameterModelDTO(param.getIdentification(), param.getType().name(),
								param.getRangeFrom(), param.getRangeTo(), param.getEnumerators()));
					}

					if (m.getDashboard() != null) {
						modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
								m.getNotebook().getIdentification(), m.getDashboard().getIdentification(),
								c.getIdentification(), subc.getIdentification(), null, m.getInputParagraphId(),
								dashboardUrl + m.getDashboard().getId(), parameterModelDTOs,
								m.getCreatedAt().toString()));
					} else {
						modelsResult.add(new ModelDTO(m.getId(), m.getIdentification(), m.getDescription(),
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
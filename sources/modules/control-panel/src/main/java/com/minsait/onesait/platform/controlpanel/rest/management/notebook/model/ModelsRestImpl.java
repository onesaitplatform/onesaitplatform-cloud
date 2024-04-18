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
package com.minsait.onesait.platform.controlpanel.rest.management.notebook.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
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
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.ModelExecutionRepository;
import com.minsait.onesait.platform.config.repository.ModelRepository;
import com.minsait.onesait.platform.config.repository.ParameterModelRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.model.ModelService;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.controlpanel.controller.model.ModelController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/models")
@EnableAutoConfiguration
@Slf4j
public class ModelsRestImpl implements ModelsRest {

	private static final String BEARER_TOKEN = "Bearer";
	private static final String MODEL_NOT_FOUND = "Model is not found.";
	private static final String USER_NOT_FOUND = "User is not found.";
	private static final String NOT_ALLOWED = "Not Allowed to check models of other user";
	private static final String ASLFRAME_STR = "?asIframe";
	private static final String NOTEBOOK_STR = "#/notebook/";
	private static final String PARAGRAPH_STR = "/paragraph/";

	@Autowired
	CategoryRelationRepository categoryRelationRepository;

	@Autowired
	ModelRepository modelRepository;

	@Autowired
	ModelExecutionRepository modelExecutionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	SubcategoryRepository subcategoryRepository;

	@Autowired
	ParameterModelRepository parameterModelRepository;

	@Value("${onesaitplatform.dashboardengine.url.only.view}")
	private String dashboardUrl;

	@Value("${onesaitplatform.notebook.url}")
	private String notebookUrl;

	@Autowired
	private ModelController modelController;

	@Autowired
	private ModelService modelService;

	@Autowired
	private JWTService jwtService;

	@Override
	public ResponseEntity<?> getByCategoryAndSubcategory(@RequestHeader(value = "Authorization") String authorization,
			@RequestParam(value = "Category", required = true) String category,
			@RequestParam(value = "Subcategory", required = true) String subcategory) {

		String jwtToken;
		String loggedUser = null;
		if (authorization.startsWith(BEARER_TOKEN)) {
			jwtToken = authorization.split(" ")[1];
		} else {
			jwtToken = authorization;
		}
		loggedUser = jwtService.getAuthentication(jwtToken).getName();

		if (loggedUser.trim().equals("")) {
			return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		try {
			final User user = userRepository.findByUserId(loggedUser);

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
	public ResponseEntity<?> getByUserHeaderAndModelId(@RequestHeader(value = "Authorization") String authorization,
			@RequestParam(value = "Model name", required = true) String modelName) {

		String jwtToken;
		if (authorization.startsWith(BEARER_TOKEN)) {
			jwtToken = authorization.split(" ")[1];
		} else {
			jwtToken = authorization;
		}
		final String loggedUser = jwtService.getAuthentication(jwtToken).getName();

		return getByUserAndModelId(authorization, loggedUser, modelName);
	}

	@Override
	public ResponseEntity<?> getByUserAndCategoryAndSubcaegory(
			@RequestHeader(value = "Authorization") String authorization,
			@RequestParam(name = "userId", required = false) String userId,
			@RequestParam(name = "category", required = true) String category,
			@RequestParam(name = "subcategory", required = true) String subcategory) {
		try {

			String jwtToken;
			if (authorization.startsWith(BEARER_TOKEN)) {
				jwtToken = authorization.split(" ")[1];
			} else {
				jwtToken = authorization;
			}

			final String loggedUser = jwtService.getAuthentication(jwtToken).getName();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userRepository.findByUserId(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {
				final User user = userRepository.findByUserId(userId);

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
	public ResponseEntity<?> getByUserAndModelId(@RequestHeader(value = "Authorization") String authorization,
			@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "modelName", required = true) String modelName) {
		try {

			String jwtToken;
			if (authorization.startsWith(BEARER_TOKEN)) {
				jwtToken = authorization.split(" ")[1];
			} else {
				jwtToken = authorization;
			}

			final String loggedUser = jwtService.getAuthentication(jwtToken).getName();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userRepository.findByUserId(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userRepository.findByUserId(userId);

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
						final CategoryRelation categoryRelation = categoryRelationRepository
								.findByTypeIdAndType(m.getId(), CategoryRelation.Type.MODEL).get(0);
						if (categoryRelation != null) {

							final Category c = categoryRepository.findById(categoryRelation.getCategory());
							final Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

							final List<ParameterModelDTO> parameterModelDTOs = new ArrayList<>();
							final List<ParameterModel> parameters = parameterModelRepository.findAllByModel(m);
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
	public ResponseEntity<?> executeModel(@RequestHeader(value = "Authorization") String authorization, String userId,
			String params, String modelName) {
		try {

			String jwtToken;
			if (authorization.startsWith(BEARER_TOKEN)) {
				jwtToken = authorization.split(" ")[1];
			} else {
				jwtToken = authorization;
			}

			final String loggedUser = jwtService.getAuthentication(jwtToken).getName();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userRepository.findByUserId(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userRepository.findByUserId(userId);
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
						final List<ParameterModel> parameters = parameterModelRepository.findAllByModel(model);

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

						final String result = modelController.execute(model.getId(), array.toString());
						final JSONObject jsonResult = new JSONObject(result);
						final String modelResult = jsonResult.getString("result");
						if (modelResult.equals("error")) {
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
			log.error("Error executing model", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> saveExecution(@RequestHeader(value = "Authorization") String authorization, String userId,
			String params, String modelName, String executionName, String executionDescription, String executionId) {
		try {

			String jwtToken;
			if (authorization.startsWith(BEARER_TOKEN)) {
				jwtToken = authorization.split(" ")[1];
			} else {
				jwtToken = authorization;
			}

			final String loggedUser = jwtService.getAuthentication(jwtToken).getName();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userRepository.findByUserId(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userRepository.findByUserId(userId);
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
						final List<ParameterModel> parameters = parameterModelRepository.findAllByModel(model);

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
	public ResponseEntity<?> getExecutions(@RequestHeader(value = "Authorization") String authorization,
			String userId) {
		try {

			String jwtToken;
			if (authorization.startsWith(BEARER_TOKEN)) {
				jwtToken = authorization.split(" ")[1];
			} else {
				jwtToken = authorization;
			}

			final String loggedUser = jwtService.getAuthentication(jwtToken).getName();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userRepository.findByUserId(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userRepository.findByUserId(userId);

				if (user != null) {

					final List<ModelExecution> executions = new ArrayList<>();

					if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
						executions.addAll(modelExecutionRepository.findAll());
					} else {

						final List<Model> models = modelService.findAllModelsByUserHasPermission(user);

						models.forEach(m -> executions.addAll(modelExecutionRepository.findByModel(m)));
					}

					final List<ExecutionDTO> executionsDto = new ArrayList<>();

					for (final ModelExecution execution : executions) {

						final CategoryRelation categoryRelation = categoryRelationRepository
								.findByTypeIdAndType(execution.getModel().getId(), CategoryRelation.Type.MODEL).get(0);
						if (categoryRelation != null) {

							final Category c = categoryRepository.findById(categoryRelation.getCategory());
							final Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

							final ExecutionDTO executionDto = new ExecutionDTO(c.getIdentification(),
									subc.getIdentification(), execution.getIdentification(), execution.getDescription(),
									execution.getModel().getIdentification(), execution.getCreatedAt().toString());

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
	public ResponseEntity<?> showExecution(@RequestHeader(value = "Authorization") String authorization, String userId,
			String executionName) {
		try {

			String jwtToken;
			if (authorization.startsWith(BEARER_TOKEN)) {
				jwtToken = authorization.split(" ")[1];
			} else {
				jwtToken = authorization;
			}

			final String loggedUser = jwtService.getAuthentication(jwtToken).getName();
			if (loggedUser.trim().equals("")) {
				return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			final User userInHeader = userRepository.findByUserId(loggedUser);
			if (null != userInHeader && (userInHeader.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
					|| loggedUser.equals(userId))) {

				final User user = userRepository.findByUserId(userId);

				if (user != null) {

					ModelExecution execution = new ModelExecution();

					if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
						execution = modelExecutionRepository.findByIdentification(executionName);
					} else {
						final ModelExecution candidateExecution = modelExecutionRepository
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

	private List<ModelDTO> filterModelsByCategoryAndSubcategory(List<Model> models, String category,
			String subcategory) {
		final List<ModelDTO> modelsResult = new ArrayList<>();
		for (final Model m : models) {
			final CategoryRelation categoryRelation = categoryRelationRepository
					.findByTypeIdAndType(m.getId(), CategoryRelation.Type.MODEL).get(0);
			if (categoryRelation != null) {

				final Category c = categoryRepository.findById(categoryRelation.getCategory());
				final Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

				if (c != null && subc != null && category.equalsIgnoreCase(c.getIdentification())
						&& subcategory.equalsIgnoreCase(subc.getIdentification())) {
					final List<ParameterModelDTO> parameterModelDTOs = new ArrayList<>();
					final List<ParameterModel> parameters = parameterModelRepository.findAllByModel(m);
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

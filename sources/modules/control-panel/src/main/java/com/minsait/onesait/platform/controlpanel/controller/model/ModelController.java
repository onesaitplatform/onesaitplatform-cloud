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
package com.minsait.onesait.platform.controlpanel.controller.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.exceptions.CategoryServiceException;
import com.minsait.onesait.platform.config.services.exceptions.ModelServiceException;
import com.minsait.onesait.platform.config.services.model.ModelExecutionService;
import com.minsait.onesait.platform.config.services.model.ModelService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.parametermodel.ParameterModelService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.notebook.NotebookManagementController;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/models")
@Slf4j
public class ModelController {

	@Autowired
	private ModelExecutionService modelExecutionService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private ModelService modelService;

	@Autowired
	private NotebookService notebookService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private SubcategoryService subcategoryService;

	@Autowired
	private ParameterModelService parameterModelService;

	@Autowired
	private CategoryRelationService categoryRelationService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private IntegrationResourcesService resourcesService;

	private String dashboardUrl;

	private String notebookUrl;

	@Autowired
	private NotebookManagementController notebookManager;

	private static final String PARAMETERS_STR = "parameters";
	private static final String REDIRECT_MODELS_LIST = "redirect:/models/list";
	private static final String MODEL_DTO_STR = "modelDto";
	private static final String ERROR_STR = "error";
	private static final String RESULT_STR = "result";
	private static final String STATUS_STR = "status";
	private static final String CAUSE_STR = "cause";
	private static final String VALIDATION_ERROR_STR = "validation error";
	private static final String ONTOLOGY_VAL_ERRROR = "ontology.validation.error";

	@PostConstruct
	public void init() {
		notebookUrl = resourcesService.getUrl(Module.NOTEBOOK, ServiceUrl.URL);
		dashboardUrl = resourcesService.getUrl(Module.DASHBOARDENGINE, ServiceUrl.ONLYVIEW);
	}
	
	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public String list(org.springframework.ui.Model model) {

		final List<com.minsait.onesait.platform.config.model.Model> models = modelService
				.findAllModelsByUser(utils.getUserId());
		model.addAttribute("models", models);
		return "models/list";
	}

	@GetMapping(value = "/run/{id}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public String run(org.springframework.ui.Model model, HttpServletRequest request, @PathVariable("id") String id) {

		Model modl = modelService.getModelById(id);
		if (modl != null) {
			List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(modl);

			List<ParameterModelDTO> dtoParameters = new ArrayList<ParameterModelDTO>();

			for (ParameterModel paramBdc : parameters) {
				ParameterModelDTO dto = new ParameterModelDTO(paramBdc);
				dtoParameters.add(dto);
			}

			model.addAttribute("modelName", modl.getIdentification());
			model.addAttribute(PARAMETERS_STR, dtoParameters);
			model.addAttribute("modelId", id);
		} else {
			log.error("Model not found: " + id);
			return REDIRECT_MODELS_LIST;
		}

		return "models/run";
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return modelService.getAllIdentifications();
	}

	@GetMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public String create(org.springframework.ui.Model model) {
		model.addAttribute(MODEL_DTO_STR, new ModelDTO());
		model.addAttribute("notebooks", notebookService.getNotebooks(utils.getUserId()));
		model.addAttribute("categories", categoryService.getAllIdentifications());
		model.addAttribute("ontologies", ontologyService.getOntologiesByUserId(utils.getUserId()));
		model.addAttribute("dashboards", dashboardService.getByUserId(utils.getUserId()));
		return "models/create";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(org.springframework.ui.Model model, @PathVariable("id") String id) {
		try {
			Model modelp = modelService.getModelById(id);

			CategoryRelation categoryRelation = categoryRelationService.getByTypeIdAndType(modelp.getId(),
					CategoryRelation.Type.MODEL);
			Category category = categoryService.getCategoryById(categoryRelation.getCategory());
			Subcategory subcategory = subcategoryService.getSubcategoryById(categoryRelation.getSubcategory());

			ModelDTO modelDto = new ModelDTO();
			modelDto.setDescription(modelp.getDescription());
			modelDto.setId(modelp.getId());
			modelDto.setIdentification(modelp.getIdentification());
			modelDto.setNotebook(modelp.getNotebook().getIdentification());
			modelDto.setCategorymodel(category.getIdentification());
			modelDto.setSubcategorymodel(subcategory.getIdentification());
			modelDto.setDashboard(modelp.getDashboard() != null ? modelp.getDashboard().getIdentification() : null);
			modelDto.setOutputParagraphId(modelp.getOutputParagraphId() != null ? modelp.getOutputParagraphId() : null);
			modelDto.setInputParagraphId(modelp.getInputParagraphId());

			List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(modelp);
			List<ParameterModelDTO> paramsDto = new ArrayList<ParameterModelDTO>();

			for (ParameterModel param : parameters) {

				ParameterModelDTO paramDto = new ParameterModelDTO(param.getId(), param.getIdentification(),
						param.getRangeFrom(), param.getRangeTo(), param.getType().name(), param.getEnumerators());
				paramsDto.add(paramDto);
			}

			ResponseEntity<?> response = notebookManager.getAllParagraphStatus(modelp.getNotebook().getIdzep());
			List<String> ids = new ArrayList<String>();
			if (response.getStatusCode() == HttpStatus.OK) {
				String body = (String) response.getBody();
				try {
					JSONObject json = new JSONObject(body);
					JSONArray paragrahs = json.getJSONArray("body");

					for (int i = 0; i < paragrahs.length(); i++) {
						JSONObject jsonAux = paragrahs.getJSONObject(i);
						ids.add(jsonAux.getString("id"));
					}
				} catch (JSONException e) {
					log.error("Error parsing response of getConfigParagraph for notebook: "
							+ modelp.getNotebook().getIdentification());
				}
			}

			model.addAttribute(MODEL_DTO_STR, modelDto);
			model.addAttribute(PARAMETERS_STR, paramsDto);
			model.addAttribute("ids", ids);
			model.addAttribute("notebooks", notebookService.getNotebooks(utils.getUserId()));
			model.addAttribute("categories", categoryService.getAllIdentifications());
			model.addAttribute("subcategories", subcategoryService.findSubcategoriesByCategory(category));
			model.addAttribute("ontologies", ontologyService.getOntologiesByUserId(utils.getUserId()));
			model.addAttribute("dashboards", dashboardService.getByUserId(utils.getUserId()));
			return "models/create";
		} catch (Exception e) {
			log.error("Error prasing parameters model: " + e.getMessage());
			return REDIRECT_MODELS_LIST;
		}
	}

	@GetMapping(value = "/getSubcategories/{category}")
	public @ResponseBody List<String> getSubcategories(@PathVariable("category") String category,
			HttpServletResponse response) {
		return subcategoryService
				.findSubcategoriesNamesByCategory(categoryService.getCategoryByIdentification(category));
	}

	@GetMapping(value = "/getConfigParagraph/{notebook}")
	public @ResponseBody List<String> getConfigParagraph(@PathVariable("notebook") String notebookName) {

		Notebook notebook = notebookService.getNotebook(notebookName, utils.getUserId());
		if (notebook != null) {
			ResponseEntity<?> response = notebookManager.getAllParagraphStatus(notebook.getIdzep());
			if (response.getStatusCode() == HttpStatus.OK) {
				String body = (String) response.getBody();
				try {
					JSONObject json = new JSONObject(body);
					JSONArray paragrahs = json.getJSONArray("body");
					List<String> ids = new ArrayList<String>();
					for (int i = 0; i < paragrahs.length(); i++) {
						JSONObject jsonAux = paragrahs.getJSONObject(i);
						ids.add(jsonAux.getString("id"));
					}
					return ids;
				} catch (JSONException e) {
					log.error("Error parsing response of getConfigParagraph for notebook: " + notebookName);
				}
			}
		}

		return null;
	}

	@GetMapping(value = "/getOutputParagraph/{notebook}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public @ResponseBody String getOutputParagraph(@PathVariable("notebook") String notebookName) {

		Notebook notebook = notebookService.getNotebook(notebookName, utils.getUserId());
		if (notebook != null) {
			ResponseEntity<?> response = notebookManager.getAllParagraphStatus(notebook.getIdzep());
			if (response.getStatusCode() == HttpStatus.OK) {
				String body = (String) response.getBody();
				try {
					JSONObject json = new JSONObject(body);
					JSONArray array = json.getJSONArray("body");
					String id = array.getJSONObject(array.length() - 1).getString("id");
					return id;
				} catch (JSONException e) {
					log.error("Error parsing response of getOutputParagraph for notebook: " + notebookName);
				}
			}
		}

		return ERROR_STR;
	}

	@PostMapping(value = "/run/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public @ResponseBody String execute(@PathVariable("id") String id, @RequestParam String parameters) {

		try {
			JSONObject result = new JSONObject();
			JSONObject json = new JSONObject();
			JSONObject finalJson = new JSONObject();
			Model model = modelService.getModelById(id);
			if (model != null) {

				String idEject = UUID.randomUUID().toString();
				json.put("id_ejec", idEject);
				JSONObject params = new JSONObject();

				JSONArray parametersArray = new JSONArray(parameters);
				for (int i = 0; i < parametersArray.length(); i++) {
					JSONObject jsonAux = parametersArray.getJSONObject(i);
					String param = jsonAux.getString("param");
					String value = jsonAux.getString("value");
					params.put(param, value);
				}

				json.put("params", params.toString());
				finalJson.put("params", json);

				log.info("Attemp to execute configuration paragraph from model {}", model.getIdentification());
				ResponseEntity<?> response = notebookManager.runParagraph(model.getNotebook().getIdzep(),
						model.getInputParagraphId(), finalJson.toString());

				if (response.getStatusCode() == HttpStatus.OK) {
					log.info("Execute configuration paragraph from model {}", model.getIdentification());
					log.info("Attemp to execute model {}", model.getIdentification());

					ResponseEntity<?> responseAux = notebookManager.runAllParagraphs(model.getNotebook().getIdzep());
					if (responseAux.getStatusCode() == HttpStatus.OK) {
						result.put("idEject", idEject);
						if (model.getDashboard() != null) {
							result.put(RESULT_STR, dashboardUrl + model.getDashboard().getId());
							return result.toString();
						} else if (model.getOutputParagraphId() != null) {
							String url = notebookUrl + "#/notebook/" + model.getNotebook().getIdzep() + "/paragraph/"
									+ model.getOutputParagraphId() + "?asIframe";
							result.put(RESULT_STR, url);
							return result.toString();
						}
					} else {
						log.error("Error running the notebook: " + model.getNotebook().getIdentification());
						result.put(RESULT_STR, ERROR_STR);
						return result.toString();
					}
				} else {
					log.error("Error running the paragraph of configuration: " + model.getInputParagraphId());
					result.put(RESULT_STR, ERROR_STR);
					return result.toString();
				}

			} else {
				log.error("Model not found with id: " + id);
				return result.toString();
			}

		} catch (JSONException e) {
			log.error("Error parsing parameters of the model with id: " + id + ". " + e.getMessage());
		}
		return "";
	}

	@PostMapping(value = "/run/save/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public @ResponseBody String save(@PathVariable("id") String id, @RequestParam String parameters,
			@RequestParam String idEject, @RequestParam String executionName,
			@RequestParam String executionDescription) {

		try {

			JSONObject jsonParamsResult = new JSONObject();
			Model model = modelService.getModelById(id);
			if (model != null) {

				// First clone the notebook
				ResponseEntity<?> response = notebookManager.cloneNotebook(model.getNotebook().getIdzep(),
						model.getNotebook().getIdentification() + "_"
								+ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
				if (response.getStatusCode() == HttpStatus.OK) {
					JSONArray parametersArray = new JSONArray(parameters);
					for (int i = 0; i < parametersArray.length(); i++) {
						JSONObject json = parametersArray.getJSONObject(i);
						String param = json.getString("param");
						String value = json.getString("value");
						jsonParamsResult.put(param, value);
					}

					String idzep = response.getBody().toString();

					ModelExecution modelExecution = new ModelExecution();
					modelExecution.setParameters(jsonParamsResult.toString());
					modelExecution.setIdEject(idEject);
					modelExecution.setModel(model);
					modelExecution.setUser(userService.getUser(utils.getUserId()));
					modelExecution.setIdZeppelin(idzep);
					modelExecution.setDescription(executionDescription);
					modelExecution.setIdentification(executionName);

					modelExecutionService.save(modelExecution);
					String url = null;
					if (modelExecution.getModel().getDashboard() != null) {
						url = dashboardUrl + modelExecution.getModel().getDashboard().getId() + "?id_ejec="
								+ modelExecution.getIdEject();

					} else if (modelExecution.getModel().getOutputParagraphId() != null) {
						url = notebookUrl + "#/notebook/" + modelExecution.getModel().getNotebook().getIdzep()
								+ "/paragraph/" + modelExecution.getModel().getOutputParagraphId() + "?asIframe";

					}
					return url;
				} else {
					log.error("Error clonning notebook {} with status {}", model.getNotebook().getIdentification(),
							response.getStatusCode());
					return null;
				}

			} else {
				log.error("Model not found with id: " + id);
				return null;
			}

		} catch (JSONException e) {
			log.error("Error parsing parameters of the model with id: " + id + ". " + e.getMessage());
			return null;
		}

	}

	@PostMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@Transactional
	public ResponseEntity<Map<String, String>> createModel(org.springframework.ui.Model model, @Valid ModelDTO modelDto,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest httpServletRequest) {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONTOLOGY_VAL_ERRROR, VALIDATION_ERROR_STR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		Model modelp = new Model();

		if (!modelService.isIdValid(modelDto.getIdentification())) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONTOLOGY_VAL_ERRROR, VALIDATION_ERROR_STR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		try {

			User user = userService.getUser(utils.getUserId());
			Notebook notebook = notebookService.getNotebook(modelDto.getNotebook(), utils.getUserId());
			Category category = categoryService.getCategoryByIdentification(modelDto.getCategorymodel());
			Subcategory subcategory = subcategoryService
					.getSubcategoryByIdentificationAndCategory(modelDto.getSubcategorymodel(), category);

			if (modelDto.getDashboard() != null && !modelDto.getDashboard().contentEquals("")) {
				Dashboard dashboard = dashboardService.getDashboardByIdentification(modelDto.getDashboard(),
						utils.getUserId());
				modelp.setDashboard(dashboard);
			} else if (modelDto.getOutputParagraphId() != null && !modelDto.getOutputParagraphId().equals("")) {
				modelp.setOutputParagraphId(modelDto.getOutputParagraphId());
			}

			modelp.setIdentification(modelDto.getIdentification());
			modelp.setDescription(modelDto.getDescription());
			modelp.setUser(user);
			modelp.setNotebook(notebook);
			modelp.setInputParagraphId(modelDto.getInputParagraphId());

			log.info("Model is going to be created.");
			modelService.createModel(modelp, category, subcategory, httpServletRequest);

		} catch (ModelServiceException e) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONTOLOGY_VAL_ERRROR, VALIDATION_ERROR_STR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}
		response.put("redirect", "/controlpanel/models/list");
		response.put(STATUS_STR, "ok");
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);
	}

	@GetMapping("/show/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public String show(org.springframework.ui.Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final Model modelp = modelService.getModelById(id);
			if (modelp != null) {

				CategoryRelation categoryRelation = categoryRelationService.getByTypeIdAndType(modelp.getId(),
						CategoryRelation.Type.MODEL);
				Category category = categoryService.getCategoryById(categoryRelation.getCategory());
				Subcategory subcategory = subcategoryService
						.getSubcategoryById(categoryRelation.getSubcategory());

				final List<ParameterModel> parameters = parameterModelService.findAllParameterModelsByModel(modelp);

				List<ParameterModelDTO> dtoParameters = new ArrayList<ParameterModelDTO>();

				for (ParameterModel paramBdc : parameters) {
					ParameterModelDTO dto = new ParameterModelDTO(paramBdc);
					dtoParameters.add(dto);
				}

				ModelDTO modelDto = new ModelDTO();
				modelDto.setDescription(modelp.getDescription());
				modelDto.setId(modelp.getId());
				modelDto.setIdentification(modelp.getIdentification());
				modelDto.setNotebook(modelp.getNotebook().getIdentification());
				modelDto.setCategorymodel(category.getIdentification());
				modelDto.setSubcategorymodel(subcategory.getIdentification());
				modelDto.setDashboard(modelp.getDashboard() != null ? modelp.getDashboard().getIdentification() : null);
				modelDto.setOutputParagraphId(
						modelp.getOutputParagraphId() != null ? modelp.getOutputParagraphId() : null);
				modelDto.setInputParagraphId(modelp.getInputParagraphId());

				model.addAttribute(PARAMETERS_STR, dtoParameters);

				model.addAttribute("category", category);
				model.addAttribute("subcategory", subcategory);
				model.addAttribute(MODEL_DTO_STR, modelDto);
				return "models/show";

			} else {
				utils.addRedirectMessage("model.notfound.error", redirect);
				return REDIRECT_MODELS_LIST;
			}
		} catch (final CategoryServiceException e) {
			return REDIRECT_MODELS_LIST;
		}
	}

	@PutMapping(value = "/update/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@Transactional
	public ResponseEntity<Map<String, String>> updateModel(org.springframework.ui.Model model, @PathVariable("id") String id,
			@Valid ModelDTO modelDto, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONTOLOGY_VAL_ERRROR, VALIDATION_ERROR_STR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}

		Model modelp = modelService.getModelByIdentification(modelDto.getIdentification());

		try {

			User user = userService.getUser(utils.getUserId());

			Category category = categoryService.getCategoryByIdentification(modelDto.getCategorymodel());
			Subcategory subcategory = subcategoryService
					.getSubcategoryByIdentificationAndCategory(modelDto.getSubcategorymodel(), category);
			Notebook notebook = notebookService.getNotebook(modelDto.getNotebook(), utils.getUserId());

			if (modelDto.getDashboard() != null && !modelDto.getDashboard().equals("")) {
				Dashboard dashboard = dashboardService.getDashboardByIdentification(modelDto.getDashboard(),
						utils.getUserId());
				modelp.setDashboard(dashboard);
				modelp.setOutputParagraphId(null);
			} else if (modelDto.getOutputParagraphId() != null && !modelDto.getOutputParagraphId().equals("")) {
				modelp.setOutputParagraphId(modelDto.getOutputParagraphId());
				modelp.setDashboard(null);
			}
			modelp.setIdentification(modelDto.getIdentification());
			modelp.setDescription(modelDto.getDescription());
			modelp.setUser(user);
			modelp.setNotebook(notebook);
			modelp.setInputParagraphId(modelDto.getInputParagraphId());

			log.info("Model is going to be created.");
			modelService.updateModel(modelp, category, subcategory, request);

			response.put("redirect", "/controlpanel/models/list");
			response.put(STATUS_STR, "ok");
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.CREATED);

		} catch (ModelServiceException e) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(ONTOLOGY_VAL_ERRROR, VALIDATION_ERROR_STR));
			return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	@Transactional
	public String delete(org.springframework.ui.Model model, @PathVariable("id") String id,
			RedirectAttributes redirect) {

		final Model modelDto = modelService.getModelById(id);
		if (modelDto != null) {
			try {
				modelService.deleteModel(id);
			} catch (final Exception e) {
				utils.addRedirectMessageWithParam("model.delete.error", e.getMessage(), redirect);
				log.error("Error deleting model. ", e);
				return "redirect:/models/show/" + id;
			}
			return REDIRECT_MODELS_LIST;
		} else {
			return REDIRECT_MODELS_LIST;
		}
	}

}

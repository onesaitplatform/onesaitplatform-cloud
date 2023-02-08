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
package com.minsait.onesait.platform.controlpanel.controller.executionmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.services.model.ModelExecutionService;
import com.minsait.onesait.platform.config.services.model.ModelService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/executionmodels")
@Slf4j
public class ExecutionModelController {

	@Autowired
	private ModelExecutionService modelExecutionService;

	@Autowired
	private ModelService modelService;
	
	@Autowired
	private IntegrationResourcesService resourcesService;
	
	@Autowired 
	private HttpSession httpSession;

	private String dashboardUrl;

	private String notebookUrl;

	private static final String REDIRECT_MODELS_LIST = "redirect:/models/list";
	private static final String ID_EXECUTION_STR = "?id_ejec=";
	private static final String NOTEBOOK_STR = "#/notebook/";
	private static final String AS_IFRAME_STR = "?asIframe";
	private static final String PARAGRAPH_STR = "/paragraph/";
	private static final String ERROR_PARSE_PARAM = "Error parsing parameters of execution model ";
	private static final String APP_ID = "appId";

	@PostConstruct
	public void init() {
		notebookUrl = resourcesService.getUrl(Module.NOTEBOOK, ServiceUrl.URL);
		dashboardUrl = resourcesService.getUrl(Module.DASHBOARDENGINE, ServiceUrl.ONLYVIEW);
	}
	
	@GetMapping(value = "/list/{id}", produces = "text/html")
	public String list(org.springframework.ui.Model model, @PathVariable("id") String id) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		final Model modl = modelService.getModelById(id);
		if (modl != null) {
			final List<ModelExecution> executions = modelExecutionService.findExecutionModelsByModel(modl);

			List<ExecutionModelDTO> executionsDTO = new ArrayList<ExecutionModelDTO>();

			for (ModelExecution execution : executions) {
				ExecutionModelDTO executionDTO = new ExecutionModelDTO();
				executionDTO.setCreatedAt(execution.getCreatedAt());
				executionDTO.setIdExecution(execution.getIdEject());
				executionDTO.setModel(execution.getModel().getIdentification());
				executionDTO.setNotebook(execution.getModel().getNotebook().getIdentification());
				executionDTO.setId(execution.getId());
				executionDTO.setIdentification(execution.getIdentification());
				executionDTO.setDescription(execution.getDescription());

				executionsDTO.add(executionDTO);
			}
			model.addAttribute("executionmodels", executionsDTO);
		} else {
			log.error("Model not found: " + id);
			return REDIRECT_MODELS_LIST;
		}
		return "executionmodels/list";
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(org.springframework.ui.Model model, HttpServletRequest request, @PathVariable("id") String id) {

		ModelExecution execution = modelExecutionService.getModelExecutionById(id);
		if (execution != null) {

			try {
				List<ModelParameterDTO> parametersDTO = new ArrayList<ModelParameterDTO>();
				JSONObject parameters = new JSONObject(execution.getParameters());
				Iterator<String> iterator = parameters.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					ModelParameterDTO param = new ModelParameterDTO();
					param.setParam(key);
					param.setValue(parameters.getString(key));
					parametersDTO.add(param);
				}

				model.addAttribute("parameters", parametersDTO);
				model.addAttribute("executionName", execution.getIdentification());
				if (execution.getModel().getDashboard() != null) {
					String url = dashboardUrl + execution.getModel().getDashboard().getId() + ID_EXECUTION_STR
							+ execution.getIdEject();
					model.addAttribute("url", url);
				} else if (execution.getModel().getOutputParagraphId() != null) {
					String url = notebookUrl + NOTEBOOK_STR + execution.getModel().getNotebook().getIdzep()
							+ PARAGRAPH_STR + execution.getModel().getOutputParagraphId() + AS_IFRAME_STR;
					model.addAttribute("url", url);
				}

			} catch (JSONException e) {
				log.error(ERROR_PARSE_PARAM + id);
			}

		} else {
			log.error("Model Execution not found: " + id);
			return REDIRECT_MODELS_LIST;
		}

		return "executionmodels/show";
	}

	@GetMapping(value = "/comparation", produces = "text/html")
	public String compareExecutions(org.springframework.ui.Model model, HttpServletRequest request) {

		String id1 = request.getParameter("id1");
		String id2 = request.getParameter("id2");

		ModelExecution execution1 = modelExecutionService.getModelExecutionById(id1);
		ModelExecution execution2 = modelExecutionService.getModelExecutionById(id2);
		if (execution1 != null && execution2 != null) {
			model.addAttribute("modelName1", execution1.getIdentification());
			model.addAttribute("modelName2", execution2.getIdentification());

			try {
				List<ModelParameterDTO> parametersDTO1 = new ArrayList<ModelParameterDTO>();
				JSONObject parameters1 = new JSONObject(execution1.getParameters());
				Iterator<String> iterator1 = parameters1.keys();
				while (iterator1.hasNext()) {
					String key = iterator1.next();
					ModelParameterDTO param = new ModelParameterDTO();
					param.setParam(key);
					param.setValue(parameters1.getString(key));
					parametersDTO1.add(param);
				}
				model.addAttribute("parameters1", parametersDTO1);
				if (execution1.getModel().getDashboard() != null) {
					String url1 = dashboardUrl + execution1.getModel().getDashboard().getId() + ID_EXECUTION_STR
							+ execution1.getIdEject();
					model.addAttribute("url1", url1);
				} else if (execution1.getModel().getOutputParagraphId() != null) {
					String url1 = notebookUrl + NOTEBOOK_STR + execution1.getModel().getNotebook().getIdzep()
							+ PARAGRAPH_STR + execution1.getModel().getOutputParagraphId() + AS_IFRAME_STR;
					model.addAttribute("url1", url1);
				}

			} catch (JSONException e) {
				log.error(ERROR_PARSE_PARAM + id1);
				return "redirect:/executionmodels/list";
			}

			try {
				List<ModelParameterDTO> parametersDTO2 = new ArrayList<ModelParameterDTO>();
				JSONObject parameters2 = new JSONObject(execution2.getParameters());
				Iterator<String> iterator2 = parameters2.keys();
				while (iterator2.hasNext()) {
					String key = iterator2.next();
					ModelParameterDTO param = new ModelParameterDTO();
					param.setParam(key);
					param.setValue(parameters2.getString(key));
					parametersDTO2.add(param);
				}
				model.addAttribute("parameters2", parametersDTO2);
				if (execution2.getModel().getDashboard() != null) {
					String url2 = dashboardUrl + execution2.getModel().getDashboard().getId() + ID_EXECUTION_STR
							+ execution2.getIdEject();
					model.addAttribute("url2", url2);
				} else if (execution2.getModel().getOutputParagraphId() != null) {
					String url2 = notebookUrl + NOTEBOOK_STR + execution2.getModel().getNotebook().getIdzep()
							+ PARAGRAPH_STR + execution2.getModel().getOutputParagraphId() + AS_IFRAME_STR;
					model.addAttribute("url2", url2);
				}

			} catch (JSONException e) {
				log.error(ERROR_PARSE_PARAM + id2);
				return "redirect:/executionmodels/list/" + execution1.getModel().getId();
			}

		} else {
			log.error("Models Executions not found for comparation: {} - {}", id1, id2);
			return REDIRECT_MODELS_LIST;
		}
		return "executionmodels/compare";
	}

}

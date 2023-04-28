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
package com.minsait.onesait.platform.controlpanel.controller.lineage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.LineageRelations.Group;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.lineage.LineageService;
import com.minsait.onesait.platform.config.services.microservice.MicroserviceService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/lineage")
@Slf4j
public class LineageController {

	@Autowired
	private GraphLineageUtil graphUtil;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private ApiManagerService apiService;
	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private LineageService lineageService;
	@Autowired
	private ClientPlatformService clientPlatformDervice;
	@Autowired
	private GadgetService gadgetService;
	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;
	@Autowired
	private NotebookRepository notebookRepository;
	@Autowired
	private MicroserviceService microserviceService;
	@Autowired 
	private HttpSession httpSession;
	
	private static final String APP_ID = "appId";

	@GetMapping("/getgraph")
	public @ResponseBody String getGraph(Model model, @RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "identification", required = true) String identification,
			HttpServletRequest request) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		final Set<GraphLineageDTO> arrayLinks = new HashSet<>();
		final User user = userService.getUser(utils.getUserId());

		if (Group.valueOf(type).equals(Group.ONTOLOGY)) {
			arrayLinks.addAll(graphUtil.constructGraphWithClientPlatformsForOntology(identification, user));
			arrayLinks.addAll(graphUtil.constructGraphWithAPIsForOntology(identification, user));
			arrayLinks.addAll(graphUtil.constructGraphWithDashboardsForOntology(identification, user));
			arrayLinks.addAll(graphUtil.constructGraphWithDataflowForOntology(identification, user));
			arrayLinks.addAll(graphUtil.constructGraphWithFlowengineForOntology(identification, user));
		} else if (Group.valueOf(type).equals(Group.DASHBOARD)) {
			arrayLinks.addAll(graphUtil.constructGraphForDashboard(identification, user));
		} else if (Group.valueOf(type).equals(Group.API)) {
			arrayLinks.addAll(graphUtil.constructGraphForApi(identification, user));
		} else if (Group.valueOf(type).equals(Group.DATAFLOW)) {
			arrayLinks.addAll(graphUtil.constructGraphForDataflow(identification, user));
		} else if (Group.valueOf(type).equals(Group.GADGET)) {
			arrayLinks.addAll(graphUtil.constructGraphForGadget(identification, user));
		} else if (Group.valueOf(type).equals(Group.DATASOURCE)) {
			arrayLinks.addAll(graphUtil.constructGraphForDatasource(identification, user));
		}

		return arrayLinks.toString();
	}

	@GetMapping(value = "/show", produces = "text/html")
	public String show(Model model, @RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {
		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		List<OPResourceDTO> opresources = new ArrayList<>();
		opresources.addAll(ontologyService.getDtoByUserAndPermissions(utils.getUserId(), identification, description));
		opresources.addAll(dashboardService.getDtoByUserAndPermissions(utils.getUserId(), identification, description));
		opresources.addAll(dataflowService.getDtoByUserAndPermissions(utils.getUserId(), identification));
		opresources.addAll(apiService.getDtoByUserAndPermissions(utils.getUserId(), identification, description));
		opresources.addAll(gadgetService.getDtoByUserAndPermissions(utils.getUserId(), identification, description));
		opresources.addAll(
				gadgetDatasourceService.getDtoByUserAndPermissions(utils.getUserId(), identification, description));

		model.addAttribute("ontologies", ontologyService.getIdentificationsByUserAndPermissions(utils.getUserId()));
		model.addAttribute("dashboards", dashboardService.getIdentificationsByUserId(utils.getUserId()));
		model.addAttribute("pipelines", dataflowService.getIdentificationByUser(utils.getUserId()));
		model.addAttribute("apis", apiService.getIdentificationsByUserOrPermission(utils.getUserId()));
		model.addAttribute("notebooks",
				notebookRepository.findIdentificationsByUserAndPermissions(userService.getUser(utils.getUserId())));
		model.addAttribute("digitalclients",
				clientPlatformDervice.getclientPlatformsIdentificationByUser(utils.getUserId()));
		model.addAttribute("gadgets", gadgetService.getAllIdentificationsByUser(utils.getUserId()));
		model.addAttribute("gadgetdatasources", gadgetDatasourceService.getAllIdentificationsByUser(utils.getUserId()));
		model.addAttribute("microservices", microserviceService.getAllIdentificationsByUser(utils.getUserId()));
		model.addAttribute("relationTypes", Group.values());

		model.addAttribute("opresources", opresources);
		model.addAttribute("relationTypes", Group.values());
		try {
			model.addAttribute("loadExample",
					ontologyService.getOntologyByIdentification("airportsdata", utils.getUserId()) != null);
		} catch (OntologyServiceException e) {
			model.addAttribute("loadExample", "false");
		}

		return "lineage/show";
	}

	@PostMapping(value = "/addRelation")
	public ResponseEntity<T> addRelation(@RequestParam String source, @RequestParam String target,
			@RequestParam String sourceType, @RequestParam String targetType) {
		try {
			if (source == null || target == null || source.isEmpty() || target.isEmpty() || sourceType == null
					|| targetType == null || sourceType.isEmpty() || targetType.isEmpty()) {
				log.error("Error creating Lineage relation. Type and node connot be null");
				return new ResponseEntity<T>(HttpStatus.BAD_REQUEST);
			}
			lineageService.createRelation(source, target, sourceType, targetType, utils.getUserId());
			return new ResponseEntity<T>(HttpStatus.CREATED);
		} catch (Exception e) {
			log.error("Error creatin Lineage relation.", e);
			return new ResponseEntity<T>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/deleteRelation")
	public ResponseEntity<T> deleteRelation(@RequestParam(value = "node", required = true) String node,
			@RequestParam(value = "nodeType", required = true) String nodeType) {
		try {
			if (nodeType == null || node == null || nodeType.isEmpty() || node.isEmpty()) {
				log.error("Error deleting Lineage relation of {}. Type and node connot be null", node);
				return new ResponseEntity<T>(HttpStatus.BAD_REQUEST);
			}
			lineageService.deleteRelation(node, nodeType, utils.getUserId());
			return new ResponseEntity<T>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error deleting Lineage relation of {}.", node, e);
			return new ResponseEntity<T>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

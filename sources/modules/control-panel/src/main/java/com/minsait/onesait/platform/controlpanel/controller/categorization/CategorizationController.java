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
package com.minsait.onesait.platform.controlpanel.controller.categorization;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.CategorizationRepository;
import com.minsait.onesait.platform.config.repository.CategorizationUserRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.categorization.CategorizationService;
import com.minsait.onesait.platform.config.services.categorization.user.CategorizationUserService;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/categorization")
@Slf4j
public class CategorizationController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private CategorizationRepository categorizationRepository;
	@Autowired
	private CategorizationUserRepository categorizationUserRepository;	
	@Autowired
	private CategorizationUserService categorizationUserService;	
	@Autowired
	private CategorizationService categorizationService;	
	@Autowired
	private OntologyService ontologyService;	
	@Autowired
	private FlowDomainRepository flowRepository;	
	@Autowired
	private ApiManagerService apiService;	
	@Autowired
	private ClientPlatformService clientPlatformService;	
	@Autowired
	private DashboardService dashboardService;	
	@Autowired
	private NotebookRepository notebookRepository;	
	@Autowired
	private PipelineRepository dataflowRepository;	
	@Autowired
	private ViewerRepository viewerRepository;
	@Autowired
	private ReportService reportService;

	
	private static final String CREATE_URL = "categorization/create";
	private static final String CATEGORIZATION = "categorization";
	private static final String E403 = "error/403";
	private static final String FAIL = "{\"status\" : \"fail\"}";
	private static final String OK = "{\"status\" : \"ok\"}";
	
	
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model) {
		User user = userService.getUser(utils.getUserId());
		model.addAttribute("categorizations", categorizationUserService.findbyUser(user));
		return "categorization/list";
	}
	
	@GetMapping(value = "/create", produces = "text/html")
	public String create(Model model) {
		final Categorization categorization = new Categorization();
		categorization.setJson("{}");
		model.addAttribute(CATEGORIZATION, categorization);
		return CREATE_URL;
	}
	
	@GetMapping(value = "/edit/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		final Categorization categorization = categorizationRepository.findById(id);
		if (!categorization.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return E403;
		}
		model.addAttribute(CATEGORIZATION, categorization);
		return CREATE_URL;
		
	}
	
	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {
		final Categorization categorization = categorizationRepository.findById(id);
		User user = userService.getUser(utils.getUserId());
		if (!categorizationService.hasUserPermission(user, categorization)
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return E403;
		}
		model.addAttribute(CATEGORIZATION, categorization);
		model.addAttribute("show", true);
		return CREATE_URL;
		
	}
	
	@GetMapping(value = "/share/{id}", produces = "text/html")
	public String share(Model model, @PathVariable("id") String id) {
		final Categorization categorization = categorizationRepository.findById(id);
		User user = userService.getUser(utils.getUserId());
		
		if (!categorization.getUser().getUserId().equals(user.getUserId())
				&& !userService.isUserAdministrator(user)) {
			return E403;
		}
		
		List<User> users = userService.getAllUsers();
		model.addAttribute("users", users);

		final List<CategorizationUser> catUsers = categorizationUserRepository.findByCategorizationNotOwn(user, categorizationRepository.findById(id));
		model.addAttribute("catUsers", catUsers);
		model.addAttribute("categorizationId", id);
		
		return "categorization/share";
	}
	
	@PostMapping(value = "/auth")
	public ResponseEntity<String> addAuthorization (@RequestParam("id") String id, @RequestParam("user") String userId,
			@RequestParam("shareType") String shareType) {
		final Categorization categorization = categorizationRepository.findById(id);
		if (!categorization.getUser().getUserId().equals(utils.getUserId()) || shareType.equals("OWNER")
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		try {
			User user = userService.getUser(userId);
			
			categorizationService.addAuthorization(categorization, user, shareType);
		} catch (Exception e) {
			log.error("Could not create the share authorization");
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(OK, HttpStatus.OK);
	}
	
	@PostMapping(value = "/auth/delete")
	public ResponseEntity<String> deleteAuthorization (@RequestParam("id") String id) {
		final CategorizationUser categorizationUser = categorizationUserRepository.findById(id);
		Categorization categorization = categorizationUser.getCategorization();
		if (!categorization.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		try {
			categorizationUserRepository.delete(categorizationUser);
		} catch (Exception e) {
			log.error("Could not delete the share authorization");
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(OK, HttpStatus.OK);
	}
	
	@PostMapping(value = "/create")
	public ResponseEntity<String> createCategorization (@RequestParam("name") String name, @RequestParam("json") String json) {
		try {
			if (categorizationRepository.findIdByIdentification(name) != null) {
				log.error("There is a Categorization Tree with the same Identification");
				throw new GenericOPException("There is a Categorization Tree with the same Identification");
			}
			User user = userService.getUser(utils.getUserId());
			
			categorizationService.createCategorization(name, json, user);
		} catch (GenericOPException e) {
			log.error("Could not create the Categorization tree");
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}	 
		catch (Exception e) {
			log.error("Could not create the Categorization tree");
			return new ResponseEntity<>("Could not create the Categorization tree", HttpStatus.BAD_REQUEST);
		}		
		return new ResponseEntity<>(OK, HttpStatus.OK);
	}
	
	@PostMapping(value = "/edit")
	public ResponseEntity<String> editCategorization (@RequestParam("id") String id, @RequestParam("json") String json) {
		final Categorization categorization = categorizationRepository.findById(id);
		if (!categorization.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		try {			
			categorizationService.updateCategorization(id, json);
		} catch (Exception e) {
			log.error("Could not create the Categorization tree");
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(OK, HttpStatus.OK);
	}
	
	@Transactional
	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> delete(@RequestParam String id) {
		final Categorization categorization = categorizationRepository.findById(id);
		if (!categorization.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
		try {
			categorizationRepository.delete(id);
			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error delating the categorization tree: " + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);			
		}
	}
	
	@Transactional
	@RequestMapping(value = "/setActive", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> setActive(@RequestParam String id) {
		try {
			User user = userService.getUser(utils.getUserId());
			
			categorizationService.setActive(id, user);

			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error activating the tree: " + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/deactivate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> inactive(@RequestParam String id) {
		try {
			categorizationService.deactivate(id);

			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error deactivating the tree: " + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getCategorizationJson", method = { RequestMethod.GET, RequestMethod.POST}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getCategorizationJson(@RequestParam(required = false) String id){
		String treeJson = null;
		JSONArray jsonArray = null;
		final List<CategorizationUser> activeCategorization = new ArrayList<>();
		User user = userService.getUser(utils.getUserId());
		if (id != null) {
			activeCategorization.add(categorizationUserRepository.findById(id));
			treeJson = categorizationRepository.findById(id).getJson();
		} else {
			activeCategorization.addAll(categorizationUserRepository.findByUserAndActive(user));
			if (activeCategorization.size() != 1) {
				return new ResponseEntity<>("{}", HttpStatus.OK);
			}
			treeJson = activeCategorization.get(0).getCategorization().getJson();
		}
		JSONArray jsonResponse = new JSONArray();
		if (activeCategorization.size() == 1) {
			try {
				jsonArray = new JSONArray(treeJson);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject json = new JSONObject(jsonArray.get(i).toString());
					JSONObject attrJson = new JSONObject(json.get("a_attr").toString());
					if (!attrJson.get("href").equals("#")) {
						if (getIdentification(attrJson.get("elementId").toString(), attrJson.get("elementType").toString()) != null) {
							json.put("text", getIdentification(attrJson.get("elementId").toString(), attrJson.get("elementType").toString()));
							jsonResponse.put(json);
						}
					} else {jsonResponse.put(json);}
				}
			} catch (Exception e) {log.error("error :"+e.getMessage());}
			return new ResponseEntity<>(jsonResponse.toString(), HttpStatus.OK);
		}
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}
	
	@Transactional
	@RequestMapping(value = "/getOntologies", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getOntologies(){
		try {
			JSONObject list = new JSONObject();
			List<Ontology> ontologies = ontologyService.getOntologiesByUserAndAccess(utils.getUserId(), null, null);
			for(Ontology ont : ontologies) {
				list.put(ont.getIdentification(),ont.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting ontologies: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getFlows", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getFlows(){
		try {
			String identification = null;
			FlowDomain flow = flowRepository.findByUserUserId(utils.getUserId());
			if (flow != null) {
				identification = flow.getIdentification();
			}
			return new ResponseEntity<>(identification, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting flows: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getDevices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getDevices(){
		try {
			JSONObject list = new JSONObject();
			List<ClientPlatform> devices = clientPlatformService.getAllClientPlatformByCriteria(utils.getUserId(), null, null);
			for(ClientPlatform device : devices) {
				list.put(device.getIdentification(), device.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting IoT Client: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getApis", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getApis(){
		try {
			JSONObject list = new JSONObject();
			List<Api> apis = apiService.loadAPISByFilter(null, null, null, utils.getUserId());
			for(Api api : apis) {
				list.put(api.getIdentification(),api.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting APIs: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getDashboards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getDashboards(){
		try {
			JSONObject list = new JSONObject();
			List<DashboardDTO> dashboards = dashboardService
					.findDashboardWithIdentificationAndDescription(null, null, utils.getUserId());
			for(DashboardDTO dashboard : dashboards) {
				list.put(dashboard.getIdentification(),dashboard.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting Dashboards: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getDataflows", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getDataflows(){
		List<Pipeline> dataflows = new ArrayList<>();
		User user = userService.getUser(utils.getUserId());
		try {
			JSONObject list = new JSONObject();			
			if (!userService.isUserAdministrator(user)) {
				dataflows = dataflowRepository.findByUserAndAccess(user);
			} else {
				dataflows = dataflowRepository.findAll();
			}			
			for(Pipeline dataflow : dataflows) {
				list.put(dataflow.getIdentification(),dataflow.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting DataFlows: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getNotebooks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getNotebooks(){
		User user = userService.getUser(utils.getUserId());
		try {
			JSONObject list = new JSONObject();
			List<Notebook> notebooks = null;
			if (!userService.isUserAdministrator(user)) {
				notebooks = notebookRepository.findByUserAndAccess(user);
			} else {
				notebooks =  notebookRepository.findAllByOrderByIdentificationAsc();
			}
			for(Notebook notebook : notebooks) {
				list.put(notebook.getIdentification(),notebook.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting Dashboards: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getViewers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getViewers(){
		User user = userService.getUser(utils.getUserId());
		try {
			JSONObject list = new JSONObject();
			List<Viewer> viewers = null;
			if (userService.isUserAdministrator(user)) {
				viewers = viewerRepository.findAll();
			} else {
				viewers = viewerRepository.findByIsPublicTrueOrUser(user);
			}
			for(Viewer viewer : viewers) {
				list.put(viewer.getIdentification(),viewer.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting Viewers: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@RequestMapping(value = "/getReports", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getReports(){
		User user = userService.getUser(utils.getUserId());
		try {
			JSONObject list = new JSONObject();
			List<Report> reports = null;
			if (userService.isUserAdministrator(user)) {
				reports = reportService.findAllActiveReports();
			} else {
				reports = reportService.findAllActiveReportsByUserId(user.getUserId());
			}
			for(Report report : reports) {
				list.put(report.getIdentification(),report.getId());
			}
			return new ResponseEntity<>(list.toString(), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Fail requesting Viewers: "+e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	
	private String getIdentification (String id, String type) {
		User user = userService.getUser(utils.getUserId());
		String identification = null;
		switch (type) {
		case "ontology":
			try {
				identification = ontologyService.getOntologyById(id, utils.getUserId()).getIdentification();
			} catch (Exception e) {
				log.error("Error getting ontology identification: {}", e);
			}
			break;
		case "flows":
			try {
				if(flowRepository.findByIdentification(id) != null &&
						(flowRepository.findByIdentification(id).getUser().getUserId().equals(utils.getUserId()) ||
								utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))) {
					identification = flowRepository.findByIdentification(id).getIdentification();
				}
			} catch (Exception e) {
				log.error("Error getting flow identification: {}", e);}
			break;
		case "apis":
			try {
				List<Api> apis = apiService.loadAPISByFilter("", "", "", utils.getUserId());
				Api api = apiService.getById(id);
				if (apis.contains(api)) {
					identification = api.getIdentification();
				}
			}
			catch (Exception e) {
				log.error("Error getting api identification: {}", e);}
			break;
		case "devices":
			try {
				List<ClientPlatform> devices = clientPlatformService.getAllClientPlatformByCriteria(utils.getUserId(), null, null);
				ClientPlatform device = clientPlatformService.getById(id);
				if (devices.contains(device)) {
					identification = device.getIdentification();
				}
			}
			catch (Exception e) {
				log.error("Error getting device identification: {}", e);
			}
			break;
		case "dashboards":
			try {
				Dashboard dashboard = dashboardService.getDashboardById(id, utils.getUserId());
				List<DashboardDTO> dashboards = dashboardService
						.findDashboardWithIdentificationAndDescription(null, null, utils.getUserId());
				for (DashboardDTO dashboardDTO : dashboards) {
					if (dashboardDTO.getId().equals(dashboard.getId())) {
						identification = dashboard.getIdentification();
					}
				}
			}
			catch (Exception e) {
				log.error("Error getting dashboard identification: {}", e);}
			break;
		case "notebooks":
			try {
				Notebook notebook = notebookRepository.findById(id);
				List<Notebook> notebooks = null;
				if (!userService.isUserAdministrator(user)) {
					notebooks = notebookRepository.findByUserAndAccess(user);
				} else {
					notebooks =  notebookRepository.findAllByOrderByIdentificationAsc();
				}
				if (notebooks.contains(notebook)) {
					identification = notebook.getIdentification();
				}
			}
			catch (Exception e) {
				log.error("Error getting notebook identification: {}", e);}
			break;
		case "dataflows":
			try {
				List<Pipeline> dataflows = null;
				Pipeline dataflow = dataflowRepository.findOne(id);
				if (!userService.isUserAdministrator(user)) {
					dataflows = dataflowRepository.findByUserAndAccess(user);
				} else {
					dataflows = dataflowRepository.findAll();
				}
				if (dataflows.contains(dataflow)) {
					identification = dataflow.getIdentification();
				}
			}
			catch (Exception e) {
				log.error("Error getting dataflow identification: {}", e);}
			break;
		case "viewers":
			try {
				Viewer viewer = viewerRepository.findById(id);
				List<Viewer> viewers = null;
				if (userService.isUserAdministrator(user)) {
					viewers = viewerRepository.findAll();
				} else {
					viewers = viewerRepository.findByIsPublicTrueOrUser(user);
				}
				if (viewers.contains(viewer)) {
					identification = viewer.getIdentification();
				}
			}
			catch (Exception e) {
				log.error("Error getting viewer identification: {}", e);}
			break;
		case "reports":
			try {
				Report report = reportService.findById(id);
				List<Report> reports = null;
				if (userService.isUserAdministrator(user)) {
					reports = reportService.findAllActiveReports();
				} else {
					reports = reportService.findAllActiveReportsByUserId(user.getUserId());
				}
				if (reports.contains(report)) {
					identification = report.getIdentification();
				}
			}
			catch (Exception e) {
				log.error("Error getting report identification: {}", e);}
			break;
		default:
			break;
		}
		return identification;
	}

}

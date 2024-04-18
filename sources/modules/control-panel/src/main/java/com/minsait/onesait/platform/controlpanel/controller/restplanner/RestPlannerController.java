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
package com.minsait.onesait.platform.controlpanel.controller.restplanner;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.RestPlanner;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.restplanner.RestPlannerService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.quartz.services.restplanner.RestPlannerQuartzService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/restplanner")
@Slf4j
public class RestPlannerController {

	@Autowired
	private EventRouter eventRouter;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private RestPlannerService restPlannerService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private RestPlannerQuartzService restPlannerQuartzService;
	@Autowired 
	private HttpSession httpSession;
	
	private static final String CREATE_URL = "restplanner/create";
	private static final String REST_PLANNER = "restplanner";
	private static final String E403 = "error/403";
	private static final String FAIL = "{\"status\" : \"fail\"}";
	private static final String OK = "{\"status\" : \"ok\"}";
	private static final String ERROR_STR = "error";
	private static final String STATUS_STR = "status";
	private static final String CAUSE_STR = "cause";
	private static final String ENV = "${ENV}";
	private static final String APP_ID = "appId";

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		User user = userService.getUser(utils.getUserId());
		if (userService.isUserAdministrator(user))
			model.addAttribute("restservices", restPlannerService.getAllRestPlanners());
		else
			model.addAttribute("restservices", restPlannerService.getAllRestPlannersByUser(utils.getUserId()));
		return "restplanner/list";
	}

	@GetMapping(value = "/create", produces = "text/html")
	public String create(Model model) {
		final RestPlanner restPlanner = new RestPlanner();
		model.addAttribute(REST_PLANNER, restPlanner);
		model.addAttribute("methods", HttpMethod.values());
		return CREATE_URL;
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		final RestPlanner restPlanner = restPlannerService.getRestPlannerById(id);
		if (!restPlanner.getUser().getUserId().equals(utils.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return E403;
		}
		model.addAttribute(REST_PLANNER, restPlanner);
		model.addAttribute("methods", HttpMethod.values());
		return CREATE_URL;

	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {
		final RestPlanner restPlanner = restPlannerService.getRestPlannerById(id);
		User user = userService.getUser(utils.getUserId());
		if (!restPlannerService.hasUserPermission(id, user.getUserId())
				&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return E403;
		}
		model.addAttribute(REST_PLANNER, restPlanner);
		return "restplanner/show";

	}

	@PostMapping(value = "/create")
	public String createRestPlanner(Model model, @Valid RestPlanner restPlanner) {
		try {
			if (restPlannerService.getRestPlannerByIdentification(restPlanner.getIdentification()) != null) {
				log.error("There is already a Rest planner with the same Identification");
				throw new GenericOPException("There is already a Rest planner with the same Identification");
			}
			User user = userService.getUser(utils.getUserId());

			restPlannerService.createRestPlannerService(restPlanner, user);
		} catch (Exception e) {
			log.error("Could not create the Rest Planner Service");
			return E403;
		}
		return "redirect:/restplanner/list";
	}

	@PutMapping("update/{id}")
	public String updateRestPlanner(Model model, @Valid RestPlanner restPlanner) {
		final RestPlanner restPlannerDB = restPlannerService.getRestPlannerById(restPlanner.getId());
		User user = userService.getUser(utils.getUserId());
		if (!restPlannerDB.getUser().getUserId().equals(utils.getUserId()) && !userService.isUserAdministrator(user)) {
			return E403;
		}
		try {
			restPlannerService.updateRestPlanner(restPlanner);
		} catch (Exception e) {
			log.error("Could not create the Rest Planner Service");
			return E403;
		}

		return "redirect:/restplanner/list";
	}

	@Transactional
	@DeleteMapping("/{id}")
	public String deleteRestPlanner(Model model, @PathVariable("id") String id) {
		final RestPlanner restPlanner = restPlannerService.getRestPlannerById(id);
		if (restPlanner != null) {
			if (!restPlanner.getUser().getUserId().equals(utils.getUserId())
					&& !utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				return E403;
			}
			try {
				if (restPlanner.isActive()) {
					restPlannerQuartzService.unschedule(restPlanner);
				}
				restPlannerService.deleteRestPlannerById(id);
				return "redirect:/restplanner/list";
			} catch (Exception e) {
				log.error("Error deleting the rest planner: " + e);
				return E403;
			}
		} else {
			return E403;
		}

	}

	@PostMapping("startstop")
	public String startStop(Model model, @RequestParam String id) {
		RestPlanner restPlanner = restPlannerService.getRestPlannerById(id);

		final User user = userService.getUser(utils.getUserId());
		if (restPlanner != null) {
			if (restPlanner.isActive()) {
				restPlannerQuartzService.unschedule(restPlanner);
				final OPAuditEvent s2Event = OPEventFactory.builder().build().createAuditEvent(EventType.GENERAL,
						"Rest Planner " + restPlanner.getIdentification() + " successfully stopped");
				s2Event.setModule(OPAuditEvent.Module.PLANNER);
				s2Event.setUser(user.getUserId());
				s2Event.setOperationType(OperationType.STOP.name());
				s2Event.setOtherType("RestPlannerStop");
				s2Event.setResultOperation(ResultOperationType.SUCCESS);
				eventRouter.notify(s2Event.toJson());
			} else {
				String newUrl = checkEnvVariable(restPlanner);
				restPlannerQuartzService.schedule(restPlanner, newUrl);
				final OPAuditEvent s2Event = OPEventFactory.builder().build().createAuditEvent(EventType.GENERAL,
						"Rest Planner " + restPlanner.getIdentification() + " successfully started");
				s2Event.setModule(OPAuditEvent.Module.PLANNER);
				s2Event.setUser(user.getUserId());
				s2Event.setOperationType(OperationType.START.name());
				s2Event.setOtherType("RestPlannerStart");
				s2Event.setResultOperation(ResultOperationType.SUCCESS);
				eventRouter.notify(s2Event.toJson());
			}
		}
		if (userService.isUserAdministrator(user))
			model.addAttribute("restservices", restPlannerService.getAllRestPlanners());
		else
			model.addAttribute("restservices", restPlannerService.getAllRestPlannersByUser(utils.getUserId()));
		return "redirect:/restplanner/list";
	}

	@PostMapping("execute")
	public ResponseEntity<Map<String, String>> executeRP(Model model, @RequestParam String id) {
		RestPlanner restPlanner = restPlannerService.getRestPlannerById(id);
		final Map<String, String> response = new HashMap<>();
		final User user = userService.getUser(utils.getUserId());
		try {
			String newUrl = checkEnvVariable(restPlanner);
			String result = restPlannerService.execute(utils.getUserId(), newUrl, restPlanner.getMethod(),
					restPlanner.getBody(), restPlanner.getHeaders());
			if (result.startsWith("OK")) {
				response.put(STATUS_STR, "ok");
				log.info("Rest Service " + restPlanner.getIdentification() + " executed by user "
						+ restPlanner.getUser() + ": " + restPlanner.getMethod() + " " + newUrl);
				log.info("Response: " + result);
				final OPAuditEvent s2Event = OPEventFactory.builder().build().createAuditEvent(EventType.GENERAL,
						"Rest Planner " + restPlanner.getIdentification() + " successfully executed");
				s2Event.setModule(OPAuditEvent.Module.PLANNER);
				s2Event.setUser(user.getUserId());
				s2Event.setOperationType(OperationType.EXECUTION.name());
				s2Event.setOtherType("RestPlannerExecution");
				s2Event.setResultOperation(ResultOperationType.SUCCESS);
				eventRouter.notify(s2Event.toJson());
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				response.put(STATUS_STR, ERROR_STR);
				log.info("Rest Service " + restPlanner.getIdentification() + " not executed");
				log.info("Response: " + result);
				final OPAuditEvent s2Event = OPEventFactory.builder().build().createAuditEvent(EventType.GENERAL,
						"Error in Rest Planner " + restPlanner.getIdentification() + " execution");
				s2Event.setModule(OPAuditEvent.Module.PLANNER);
				s2Event.setUser(user.getUserId());
				s2Event.setOperationType(OperationType.EXECUTION.name());
				s2Event.setOtherType("RestPlannerExecution");
				s2Event.setResultOperation(ResultOperationType.ERROR);
				eventRouter.notify(s2Event.toJson());
				return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
			}

		} catch (final Exception e) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			final OPAuditEvent s2Event = OPEventFactory.builder().build().createAuditEvent(EventType.GENERAL,
					"Generic error in Rest Planner " + restPlanner.getIdentification() + " execution");
			s2Event.setModule(OPAuditEvent.Module.PLANNER);
			s2Event.setUser(user.getUserId());
			s2Event.setOperationType(OperationType.EXECUTION.name());
			s2Event.setOtherType("RestPlannerExecution");
			s2Event.setResultOperation(ResultOperationType.ERROR);
			eventRouter.notify(s2Event.toJson());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private String checkEnvVariable(RestPlanner restPlanner) {
		String newUrl = restPlanner.getUrl();
		if (restPlanner.getUrl().contains(ENV)) {
			newUrl = restPlanner.getUrl().split("\\/", 2)[1];
			newUrl = resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE) + newUrl;
		}
		return newUrl;
	}

}

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
package com.minsait.onesait.platform.controlpanel.controller.flow;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomainStatus;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowDomain.State;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.flow.engine.FlowEngineService;
import com.minsait.onesait.platform.libraries.flow.engine.FlowEngineServiceFactory;
import com.minsait.onesait.platform.libraries.nodered.auth.NoderedAuthenticationServiceImpl;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/flows")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class FlowDomainController {

	@Value("${onesaitplatform.flowengine.services.request.timeout.ms:5000}")
	private int restRequestTimeout;

	private String baseUrl;

	private String proxyUrl;

	@Value("${onesaitplatform.controlpanel.avoidsslverification:false}")
	private boolean avoidSSLVerification;

	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private UserService userService;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private NoderedAuthenticationServiceImpl noderedAuthService;

	@Autowired
	private HttpSession httpSession;

	private FlowEngineService flowEngineService;

	private static final String DOMAINS_STR = "domains";
	private static final String ERROR_403 = "error/403";
	private static final String FLOW_ENGINE_ACTIVE_STR = "flowEngineActive";
	private static final String MESSAGE_STR = "message";
	private static final String MESSAGE_ALERT_TYPE_STR = "messageAlertType";
	private static final String REDIRECT_FLOWS_CREATE = "redirect:/flows/create";
	private static final String FLOWS_CREATE = "flows/create";
	private static final String REDIRECT_FLOWS_LIST = "redirect:/flows/list";
	private static final String APP_ID = "appId";

	@PostConstruct
	public void init() {
		proxyUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.PROXYURL);
		baseUrl = resourcesService.getUrl(Module.FLOWENGINE, ServiceUrl.BASE); // <host>/flowengine/admin
		flowEngineService = FlowEngineServiceFactory.getFlowEngineService(baseUrl, restRequestTimeout,
				avoidSSLVerification);

	}

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model) {
		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		final List<FlowEngineDomainStatus> domainStatusList = getUserDomains(model);
		model.addAttribute(DOMAINS_STR, domainStatusList);
		model.addAttribute("userRole", utils.getRole());
		return "flows/list";
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(id);
			if (!domainService.hasUserManageAccess(domain.getId(), utils.getUserId())) {
				log.debug("Flow domain not found.");
				// utils.addRedirectMessage("domain.delete.error", redirect);
				return ERROR_403;
			}
			model.addAttribute("domain", domain);
			model.addAttribute("userRole", utils.getRole());
		} catch (final Exception e) {
			log.error("Cannot find flow domain.");
			return ERROR_403;
		}
		return FLOWS_CREATE;
	}

	@PutMapping(value = "/update/{id}")
	public String update(@Valid FlowDomainDTO domainDTO, @PathVariable("id") String id, BindingResult bindingResult,
			RedirectAttributes redirect) {

		final User user = userService.getUser(utils.getUserId());
		if (domainDTO.getIdentification() == null || domainDTO.getIdentification().isEmpty()) {
			log.error("Domain identifier is missing");
			utils.addRedirectMessage("domain.create.error", redirect);
			return REDIRECT_FLOWS_LIST;
		}
		final List<FlowDomain> domains = domainService.getFlowDomainByUser(user);

		if (domains == null || domains.isEmpty()) {
			// TODO: Set error, no domain found for user
			return REDIRECT_FLOWS_LIST;
		}
		final Optional<FlowDomain> optDomain = domains.stream()
				.filter(d -> d.getIdentification().equals(domainDTO.getIdentification())).findFirst();

		if (!optDomain.isPresent()) {
			// TODO: Set error, no domain found for user
			return REDIRECT_FLOWS_LIST;
		}
		optDomain.get().setAutorecover(domainDTO.getAutorecover());
		optDomain.get().setThresholds(domainDTO.getThresholds());
		try {
			domainService.updateDomain(optDomain.get());
		} catch (final Exception e) {
			log.error("Cannot update flow domain.");
			utils.addRedirectMessage("domain.update.error", redirect);
			return REDIRECT_FLOWS_CREATE;
		}
		return REDIRECT_FLOWS_LIST;
	}

	@GetMapping(value = "/data")
	public @ResponseBody List<FlowEngineDomainStatus> data(Model model) {
		return getUserDomains(model);
	}

	@PostMapping(value = "/create")
	public String create(@Valid FlowDomainDTO domain, BindingResult bindingResult, RedirectAttributes redirect) {

		final User user = userService.getUser(utils.getUserId());
		if (checkDomainsOwnedByUser(user) > 0) {
			log.error("User already has one domain created.");
			utils.addRedirectMessage("domain.create.error.already.owns.domains", redirect);
			return REDIRECT_FLOWS_CREATE;
		}
		if (domain.getIdentification() == null || domain.getIdentification().isEmpty()) {
			log.error("Domain identifier is missing");
			utils.addRedirectMessage("domain.create.error", redirect);
			return REDIRECT_FLOWS_CREATE;
		}
		try {
			domainService.createFlowDomain(domain.getIdentification(), userService.getUser(utils.getUserId()));
		} catch (final Exception e) {
			log.error("Cannot create flow domain.");
			utils.addRedirectException(e, redirect);
			return REDIRECT_FLOWS_CREATE;
		}
		return REDIRECT_FLOWS_LIST;
	}

	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {
		final FlowDomain domain = new FlowDomain();
		model.addAttribute("domain", domain);
		return FLOWS_CREATE;

	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(id);
			if (!domainService.hasUserManageAccess(domain.getId(), utils.getUserId())) {
				log.debug("Cannot delete flow domain.");
				utils.addRedirectMessage("domain.delete.error", redirect);
				return REDIRECT_FLOWS_LIST;
			}
			domainService.deleteFlowdomain(id);
		} catch (final Exception e) {
			log.error("Cannot delete flow domain.");
			utils.addRedirectException(e, redirect);
			return REDIRECT_FLOWS_LIST;
		}
		return REDIRECT_FLOWS_LIST;
	}

	@PostMapping(value = "/startstop/{id}")
	public ResponseEntity<String> startStop(@PathVariable("id") String id) {
		try {
			final FlowDomain domain = domainService.getFlowDomainById(id);
			if (!domainService.hasUserManageAccess(domain.getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			final FlowEngineDomain engineDom = FlowEngineDomain.builder().domain(domain.getIdentification())
					.port(domain.getPort()).home(domain.getHome()).servicePort(domain.getServicePort())
					.vertical(MultitenancyContextHolder.getVerticalSchema()).build();
			if (State.STOP.name().equals(domain.getState())) {
				flowEngineService.startFlowEngineDomain(engineDom);
				domain.setState(State.START.name());
			} else if (State.START.name().equals(domain.getState())) {
				flowEngineService.stopFlowEngineDomain(domain.getIdentification());
				domain.setState(State.STOP.name());
				domain.setAutorecover(false);
			}
			domainService.updateDomain(domain);

		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/start")
	public ResponseEntity<String> startDomain(Model model, @Valid @RequestBody FlowEngineDomainStatus domainStatus,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(domainStatus.getDomain());
			final ResponseEntity<?> re = startStop(domain.getId());
			if (!re.getStatusCode().equals(HttpStatus.OK)) {
				if (re.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				} else {
					throw new GenericOPException();
				}
			}
			domainStatus.setState(State.START.name());
			model.addAttribute(FLOW_ENGINE_ACTIVE_STR, true);
		} catch (final GenericOPException e) {
			log.error("Unable to start domain = {}.", domainStatus.getDomain());
			return new ResponseEntity<>(utils.getMessage("domain.error.notstarted", "Unable to stop domain"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@PostMapping(value = "/stop", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> stopDomain(Model model, @Valid @RequestBody FlowEngineDomainStatus domainStatus,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		try {
			final FlowDomain domain = domainService.getFlowDomainByIdentification(domainStatus.getDomain());
			final ResponseEntity<?> re = startStop(domain.getId());
			if (!re.getStatusCode().equals(HttpStatus.OK)) {
				if (re.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
					return new ResponseEntity<>(HttpStatus.FORBIDDEN);
				} else {
					throw new GenericOPException();
				}
			}
			// Clean status not executing
			domainStatus.setState(State.STOP.name());
			domainStatus.setCpu("--");
			domainStatus.setMemory("--");
		} catch (final GenericOPException e) {
			log.error("Unable to stop domain = {}.", domainStatus.getDomain());
			return new ResponseEntity<>(utils.getMessage("domain.error.notstopped", "Unable to stop domain"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}

	@GetMapping(value = "/show/{domainId}", produces = "text/html")
	public String showNodeRedPanelForm(Model model, @PathVariable(value = "domainId") String domainId,
			@RequestParam(value = "flow", required = false) String flowId, RedirectAttributes ra) {
		final FlowDomain domain = domainService.getFlowDomainByIdentification(domainId);
		if (domainService.hasUserViewAccess(domain.getId(), utils.getUserId())) {
			try {

				final String password = domain.getUser().getPassword();
				final String auth = domain.getUser().getUserId() + ":" + password + ":"
						+ MultitenancyContextHolder.getVerticalSchema();
				final String authBase64 = Base64.getEncoder().encodeToString(auth.getBytes());
				final String accessToken = noderedAuthService.getNoderedAuthAccessToken(domain.getUser().getUserId(),
						domainId);
				String proxyUrlAndDomain = proxyUrl + domainId + "/?authentication=" + authBase64 + "&access_token="
						+ accessToken;
				if (flowId != null) {
					proxyUrlAndDomain += "#flow/" + flowId;
				}

				model.addAttribute("proxy", proxyUrlAndDomain);
				return "flows/show";
			} catch (final Exception e) {
				utils.addRedirectException(e, ra);
				return REDIRECT_FLOWS_LIST;
			}
		} else {
			return ERROR_403;
		}

	}

	@GetMapping(value = "/check/available/{domainId}")
	public @ResponseBody boolean checkAvailableDomainIdentifier(@PathVariable(value = "domainId") String domainId) {
		return !domainService.domainExists(domainId);
	}

	@GetMapping(value = "/check/amount/{domainId}")
	public @ResponseBody boolean checkDomainAmountByUser(@PathVariable(value = "domainId") String domainId) {
		final User user = userService.getUser(utils.getUserId());
		return checkDomainsOwnedByUser(user) <= 0;
	}

	private List<FlowEngineDomainStatus> getUserDomains(Model model) {
		List<FlowEngineDomainStatus> domainStatusList = null;
		List<FlowEngineDomainStatus> filteredDomainStatusList = new ArrayList<>();
		final List<FlowDomain> domainList = domainService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
		if (domainList != null && !domainList.isEmpty()) {

			final List<String> domainListIds = new ArrayList<>();
			for (final FlowDomain domain : domainList) {
				domainListIds.add(domain.getIdentification());
			}
			// Get status info from FlowEngineAdmin
			try {
				domainStatusList = flowEngineService.getAllFlowEnginesDomains();
				model.addAttribute(FLOW_ENGINE_ACTIVE_STR, true);
			} catch (final Exception e) {
				// Flow Engine is either unavailable or not synchronized
				log.error("Unable to retrieve Flow Domain info. Cause = {}, Message = {}", e.getCause(),
						e.getMessage());
				model.addAttribute(FLOW_ENGINE_ACTIVE_STR, false);
				model.addAttribute(MESSAGE_STR,
						utils.getMessage("domain.flow.Engine.notstarted", "Flow Engine is temporarily unreachable."));
				model.addAttribute(MESSAGE_ALERT_TYPE_STR, "WARNING");
			}

			if (domainStatusList == null) {
				domainStatusList = new ArrayList<>();
			}

			filteredDomainStatusList = getFilteredDomainStatuses(domainList, domainStatusList);

		}
		return filteredDomainStatusList;
	}

	private List<FlowEngineDomainStatus> getFilteredDomainStatuses(List<FlowDomain> domainList,
			List<FlowEngineDomainStatus> domainStatusList) {
		final List<FlowEngineDomainStatus> filteredDomainStatusList = new ArrayList<>();
		for (final FlowDomain domain : domainList) {
			final FlowEngineDomainStatus domainStatus = new FlowEngineDomainStatus();
			domainStatus.setDomain(domain.getIdentification());
			domainStatus.setPort(domain.getPort());
			domainStatus.setHome(domain.getHome());
			domainStatus.setServicePort(domain.getServicePort());

			domainStatus.setRuntimeState("--");
			domainStatus.setCpu("--");
			domainStatus.setMemory("--");
			domainStatus.setUser(domain.getUser().getUserId());
			domainStatus.setAutorecover(domain.getAutorecover());
			domainStatus.setCreatedAt(domain.getCreatedAt());
			domainStatus.setUpdatedAt(domain.getUpdatedAt());

			final Optional<FlowEngineDomainStatus> status = domainStatusList.stream()
					.filter(domStatus -> domStatus.getDomain().equals(domain.getIdentification())).findAny();
			if (status.isPresent()) {
				domainStatus.setState(status.get().getState());
				if (status.get().getState().equals("STOP") && !status.get().getState().equals(domain.getState())) {
					domain.setState(status.get().getState());
					domainService.updateDomain(domain);
				}
				if (!status.get().getMemory().isEmpty()) {
					final Double mem = new Double(status.get().getMemory()) / (1024d * 1024d);
					domainStatus.setMemory(String.format("%.2f", mem));
				}
				if (!status.get().getCpu().isEmpty()) {
					final Double cpu = new Double(status.get().getCpu());
					domainStatus.setCpu(String.format("%.2f", cpu));
				}
			}

			filteredDomainStatusList.add(domainStatus);
		}
		return filteredDomainStatusList;
	}

	private int checkDomainsOwnedByUser(User user) {
		final List<FlowDomain> domainsFromUser = domainService.getFlowDomainByUser(user);
		int numOwnedDomains = 0;
		for (final FlowDomain dom : domainsFromUser) {
			if (dom.getUser().getUserId().equals(user.getUserId())) {
				numOwnedDomains++;
			}
		}
		return numOwnedDomains;
	}

}

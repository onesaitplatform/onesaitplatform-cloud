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
package com.minsait.onesait.platform.controlpanel.controller.subscription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
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

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.subscription.SubscriptionService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/subscriptions")
@Slf4j
public class SubscriptionController {

	private static final String SUBSCRIPTIONS_STR = "subscriptions";
	private static final String SUBSCRIPTION_STR = "subscription";
	private static final String SUBSCRIPTIONS_LIST = "subscriptions/list";
	private static final String SUBSCRIPTIONS_SHOW = "subscriptions/show";
	private static final String SUBSCRIPTIONS_CREATE = "subscriptions/create";
	private static final String ONTOLOGIES_STR = "ontologies";
	private static final String STATUS_STR = "status";
	private static final String VAL_ERROR = "validation error";
	private static final String ERROR_STR = "error";
	private static final String CAUSE_STR = "cause";
	private static final String SUBSCRIPTION_VAL_ERROR = "subscription.validation.error";
	private static final String SUBSCRIPTION_IDENT_NOT_VALID = "subscription.identification.not.valid";
	private static final String SUBSCRIPTION_IDENT_EXIST = "subscription.identification.exist";
	private static final String GEN_INTERN_ERROR_CREATE_SUBS = "Generic internal error creating subscription: ";
	private static final String REDIRECT = "redirect";
	private static final String REDIRECT_CONTROLPANEL_SUBSCRIPTIONS_LIST = "/controlpanel/subscriptions/list";
	private static final String REDIRECT_SUBSCRIPTIONS_LIST = "redirect:/subscriptions/list";
	private static final String REDIRECT_CONTROLPANEL_SUBSCRIPTIONS_CREATE = "/controlpanel/subscriptions/create";
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_500 = "error/500";

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ResourcesInUseService resourcesInUseService;

	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String listAll(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		final List<Subscription> subscriptions = subscriptionService.getWebProjectsWithDescriptionAndIdentification(
				userService.getUser(utils.getUserId()), identification, description);

		model.addAttribute(SUBSCRIPTIONS_STR, subscriptions);
		return SUBSCRIPTIONS_LIST;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		User user = userService.getUser(utils.getUserId());
		Subscription subscription = subscriptionService.findById(id, user);
		if (subscription != null) {
			model.addAttribute(SUBSCRIPTION_STR,
					this.formateSubscriptionToSubscriptionDto(new SubscriptionDTO(), subscription));
			return SUBSCRIPTIONS_SHOW;
		} else {
			log.error("Subscription with id {} not found.", id);
			return ERROR_500;
		}

	}

	@GetMapping(value = "/create")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String create(Model model) {

		model.addAttribute(SUBSCRIPTION_STR, new SubscriptionDTO());
		model.addAttribute(ONTOLOGIES_STR, ontologyService.getAllOntologies(utils.getUserId()));

		return SUBSCRIPTIONS_CREATE;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/update/{id}")
	public String update(Model model, @PathVariable("id") String id) {
		User user = userService.getUser(utils.getUserId());
		Subscription subscription = subscriptionService.findById(id, user);

		if (subscription == null) {
			log.error("Subscription with id {} doesn't exist.", id);
			return ERROR_500;
		}
		if (!utils.getUserId().equals(subscription.getUser().getUserId())
				&& !utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			log.error("User {} has no permission to edit the subscription with identification {}", utils.getUserId(),
					subscription.getIdentification());
			return ERROR_403;
		}

		SubscriptionDTO subscriotionDto = formateSubscriptionToSubscriptionDto(new SubscriptionDTO(), subscription);

		model.addAttribute(SUBSCRIPTION_STR, subscriotionDto);
		model.addAttribute(ONTOLOGIES_STR, ontologyService.getAllOntologies(utils.getUserId()));
		model.addAttribute(ResourcesInUseService.RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
		resourcesInUseService.put(id, utils.getUserId());

		return SUBSCRIPTIONS_CREATE;

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@PutMapping(value = "/update/{id}")
	@Transactional
	public ResponseEntity<Map<String, String>> updateSubscription(Model model, @Valid SubscriptionDTO subscriptionDto,
			@PathVariable("id") String id, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest httpServletRequest) {
		final Map<String, String> response = new HashMap<>();
		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(SUBSCRIPTION_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		User user = userService.getUser(utils.getUserId());
		Subscription subscription = subscriptionService.findById(id, user);

		if (subscription == null) {
			log.error("Subscription {} not found for the user {}", subscriptionDto.getIdentification(),
					user.getFullName());
			response.put(CAUSE_STR, "Subscription not found to update it.");
			response.put(STATUS_STR, ERROR_STR);
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		if (!user.getUserId().equals(subscription.getUser().getUserId())
				&& !utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			log.error("User {} has no permission to edit the subscription with identification {}", utils.getUserId(),
					subscription.getIdentification());
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, "User has not permission to edit the subscription.");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		formateSubscriptionDTOToSubscription(subscriptionDto, subscription);

		subscriptionService.createSubscription(subscription);

		resourcesInUseService.removeByUser(id, utils.getUserId());

		response.put(REDIRECT, REDIRECT_CONTROLPANEL_SUBSCRIPTIONS_LIST);
		response.put(STATUS_STR, "ok");
		return new ResponseEntity<>(response, HttpStatus.CREATED);

	}

	@PostMapping(value = "/create")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<Map<String, String>> createSubscription(Model model, @Valid SubscriptionDTO subscriptionDto,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(SUBSCRIPTION_VAL_ERROR, VAL_ERROR));
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		if (!subscriptionService.isIdValid(subscriptionDto.getIdentification())) {
			log.debug(utils.getMessage(SUBSCRIPTION_IDENT_NOT_VALID, VAL_ERROR));
			utils.addRedirectMessage(SUBSCRIPTION_IDENT_NOT_VALID, redirect);
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, utils.getMessage(SUBSCRIPTION_IDENT_NOT_VALID, VAL_ERROR));
			response.put(REDIRECT, REDIRECT_CONTROLPANEL_SUBSCRIPTIONS_CREATE);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			if (subscriptionService.existSubscriptionWithIdentification(subscriptionDto.getIdentification())) {
				log.debug(utils.getMessage(SUBSCRIPTION_IDENT_EXIST, VAL_ERROR));
				utils.addRedirectMessage(SUBSCRIPTION_IDENT_EXIST, redirect);
				response.put(STATUS_STR, ERROR_STR);
				response.put(CAUSE_STR, utils.getMessage(SUBSCRIPTION_IDENT_EXIST, VAL_ERROR));
				response.put(REDIRECT, REDIRECT_CONTROLPANEL_SUBSCRIPTIONS_CREATE);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			Subscription subscription = formateSubscriptionDTOToSubscription(subscriptionDto, new Subscription());

			subscriptionService.createSubscription(subscription);
			log.info("Subscription {} created successfully", subscription.getIdentification());

			response.put(STATUS_STR, "ok");
			response.put(REDIRECT, REDIRECT_CONTROLPANEL_SUBSCRIPTIONS_LIST);
			return new ResponseEntity<>(response, HttpStatus.CREATED);

		} catch (final Exception e) {
			log.error(GEN_INTERN_ERROR_CREATE_SUBS + " {} . {}", subscriptionDto.getIdentification(), e.getMessage());
			response.put(STATUS_STR, ERROR_STR);
			response.put(CAUSE_STR, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		User user = userService.getUser(utils.getUserId());
		final Subscription subscription = subscriptionService.findById(id, user);
		if (subscription != null) {
			try {
				subscriptionService.deleteSubscription(subscription, user);
				log.info("Subscription {} deleted successfully", subscription.getIdentification());
			} catch (final Exception e) {
				utils.addRedirectMessageWithParam("subscription.delete.error", e.getMessage(), redirect);
				log.error("Error deleting Subscription {}. {} ", subscription.getIdentification(), e);
				return REDIRECT_SUBSCRIPTIONS_LIST;
			}
			return REDIRECT_SUBSCRIPTIONS_LIST;
		} else {
			return REDIRECT_SUBSCRIPTIONS_LIST;
		}
	}

	private Subscription formateSubscriptionDTOToSubscription(SubscriptionDTO subscriptionDto,
			Subscription subscription) {

		Ontology ontology = ontologyService.getOntologyByIdentification(subscriptionDto.getOntology());
		if (subscription.getId() == null) {
			subscription.setUser(userService.getUser(utils.getUserId()));
		} else {
			subscription.setId(subscriptionDto.getId());
		}
		subscription.setDescription(subscriptionDto.getDescription());
		subscription.setIdentification(subscriptionDto.getIdentification());
		subscription.setOntology(ontology);
		subscription.setProjection(subscriptionDto.getProjection());
		subscription.setQueryField(subscriptionDto.getQueryField());
		subscription.setQueryOperator(subscriptionDto.getQueryOperator());
		return subscription;
	}

	private SubscriptionDTO formateSubscriptionToSubscriptionDto(SubscriptionDTO subscriptionDto,
			Subscription subscription) {

		SubscriptionDTO subscriotionDto = new SubscriptionDTO();
		subscriotionDto.setDescription(subscription.getDescription());
		subscriotionDto.setOntology(subscription.getOntology().getIdentification());
		subscriotionDto.setId(subscription.getId());
		subscriotionDto.setIdentification(subscription.getIdentification());
		subscriotionDto.setProjection(subscription.getProjection());
		subscriotionDto.setQueryField(subscription.getQueryField());
		subscriotionDto.setQueryOperator(subscription.getQueryOperator());
		return subscriotionDto;
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free dashboard resource ", id);
	}
}

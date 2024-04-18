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
package com.minsait.onesait.platform.controlpanel.controller.twitter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.ClientPlatformServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.exceptions.TokenServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.twitter.TwitterListeningService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.quartz.services.twitter.TwitterControlService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/twitter")
@Slf4j
public class TwitterListeningController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private TwitterListeningService twitterListeningService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private TwitterControlService twitterControlService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired 
	private HttpSession httpSession;

	private static final String APP_ID = "appId";
	
	@Autowired
	UserService userService;

	// SCHEDULED SEARCH BEGIN METHODS
	@GetMapping("/scheduledsearch/list")
	public String listListenings(Model model) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		model.addAttribute("twitterListenings", this.twitterListeningService.getAllListeningsByUser(utils.getUserId()));
		return "twitter/scheduledsearch/list";
	}

	@GetMapping("/scheduledsearch/create")
	public String createForm(Model model) {
		this.loadOntologiesAndConfigurations(model);
		model.addAttribute("twitterListening", new TwitterListening());
		return "twitter/scheduledsearch/create";
	}

	@GetMapping("/scheduledsearch/update/{id}")
	public String updateForm(Model model, @PathVariable("id") String id) {
		TwitterListening twitterListening = null;
		twitterListening = this.twitterListeningService.getListenById(id);
		if (twitterListening == null)
			twitterListening = new TwitterListening();
		model.addAttribute("twitterListening", twitterListening);
		this.loadOntologiesAndConfigurations(model);
		return "twitter/scheduledsearch/create";
	}

	@PutMapping("/scheduledsearch/update/{id}")
	public String update(Model model, @PathVariable("id") String id, @ModelAttribute TwitterListening twitterListener) {

		if (twitterListener != null)
			this.twitterControlService.updateTwitterListening(twitterListener);
		return "redirect:/twitter/scheduledsearch/update/" + id;
	}

	@PostMapping("/scheduledsearch/create")
	public String create(Model model, @Valid TwitterListening twitterListening, BindingResult bindingResult,
			RedirectAttributes redirect, @RequestParam("_new") Boolean newOntology,
			@RequestParam("_newclient") Boolean newClientPlatform,
			@RequestParam(value = "ontologyId", required = false) String ontologyId,
			@RequestParam(value = "clientPlatformId", required = false) String clientPlatformId) {

		if (bindingResult.hasErrors() && !newOntology) {
			log.debug("TwitterListening object has errors");
			this.utils.addRedirectMessage("twitterlistening.validation.error", redirect);
			return "redirect:/twitter/scheduledsearch/create";
		}

		if (!newOntology && !newClientPlatform) {
			if (twitterListening.getUser() == null)
				twitterListening.setUser(this.userService.getUser(this.utils.getUserId()));
			twitterListening = this.twitterListeningService.createListening(twitterListening, this.utils.getUserId());

		} else if (newClientPlatform && !ontologyId.equals("") && !clientPlatformId.equals("")) {

			try {
				Ontology ontology = this.twitterListeningService.createTwitterOntology(ontologyId);
				ontology.setUser(this.userService.getUser(this.utils.getUserId()));
				ontologyService.createOntology(ontology, null);
				ontology = ontologyService.getOntologyByIdentification(ontology.getIdentification(), utils.getUserId());

				ArrayList<Ontology> ontologies = new ArrayList<>();
				ontologies.add(ontology);

				ClientPlatform client = new ClientPlatform();
				client.setUser(this.userService.getUser(utils.getUserId()));
				client.setIdentification(clientPlatformId);

				Token token = this.clientPlatformService.createClientAndToken(ontologies, client);

				twitterListening.setOntology(ontology);
				twitterListening.setToken(token);
				twitterListening.setUser(this.userService.getUser(this.utils.getUserId()));
				this.twitterListeningService.createListening(twitterListening, utils.getUserId());
			} catch (OntologyServiceException e) {
				log.debug("Error creating ontology");
				log.error(e.getMessage());
			} catch (ClientPlatformServiceException e) {
				log.debug("Error creating platform client");
				log.error(e.getMessage());
			} catch (TokenServiceException e) {
				log.debug("Error generating token");
				log.error(e.getMessage());
			}

		} else if (newOntology && !ontologyId.equals("")) {
			Ontology ontology = this.twitterListeningService.createTwitterOntology(ontologyId);
			ontology.setUser(this.userService.getUser(this.utils.getUserId()));
			ontologyService.createOntology(ontology, null);
			ontology = ontologyService.getOntologyByIdentification(ontology.getIdentification(), utils.getUserId());

			ClientPlatform client = this.clientPlatformService.getByIdentification(clientPlatformId);
			if (ontology != null && client != null) {
				this.clientPlatformService.createOntologyRelation(ontology, client);
				Token token = this.tokenService.getToken(client);

				twitterListening.setOntology(ontology);
				twitterListening.setToken(token);
				twitterListening.setUser(this.userService.getUser(this.utils.getUserId()));
				this.twitterListeningService.createListening(twitterListening, utils.getUserId());
			}

		}
		this.twitterControlService.scheduleTwitterListening(twitterListening);
		return "redirect:/twitter/scheduledsearch/list";

	}

	@PostMapping("/scheduledsearch/getclients")
	public @ResponseBody List<String> getClientsOntology(@RequestBody String ontologyId) {
		return this.twitterListeningService.getClientsFromOntology(ontologyId, utils.getUserId());
	}

	@GetMapping("/scheduledsearch/getallclients")
	public String getAllClients(Model model) {

		model.addAttribute("clients",
				this.twitterListeningService.getAllClientsForUser(this.userService.getUser(utils.getUserId())));
		return "twitter/scheduledsearch/create :: clients";
	}

	@PostMapping("/scheduledsearch/gettokens")
	public @ResponseBody List<String> getTokensClient(@RequestBody String clientPlatformId) {
		return this.twitterListeningService.getTokensFromClient(clientPlatformId);
	}

	@PostMapping("/scheduledsearch/existontology")
	public @ResponseBody boolean existOntology(@RequestBody String identification) {
		return this.twitterListeningService.existOntology(identification, utils.getUserId());
	}

	@PostMapping("/scheduledsearch/existclient")
	public @ResponseBody boolean existClient(@RequestBody String identification) {
		return this.twitterListeningService.existClientPlatform(identification);
	}

	public void loadOntologiesAndConfigurations(Model model) {
		List<Configuration> configurations = null;
		List<Ontology> ontologies = null;
		List<Ontology> ontologiesSocial = new ArrayList<>();
		if (utils.isAdministrator()) {
			configurations = this.twitterListeningService.getAllConfigurations();
			ontologies = this.ontologyService.getAllOntologies(utils.getUserId());
		} else {
			configurations = this.twitterListeningService.getAllConfigurations();
			ontologies = this.ontologyService.getOntologiesByUserId(this.utils.getUserId());
		}
		for (Ontology ontology : ontologies) {
			if (ontology.getDataModel() != null
					&& ontology.getDataModel().getType().equals(DataModel.MainType.SOCIAL_MEDIA.name())) {
				ontologiesSocial.add(ontology);
			}
		}
		model.addAttribute("configurations", configurations);
		model.addAttribute("ontologies", ontologiesSocial);

	}

	@DeleteMapping("/scheduledsearch/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		TwitterListening listening = this.twitterListeningService.getListenById(id);
		if (listening != null) {
			this.entityDeletionService.deleteTwitterListening(listening);
			this.twitterControlService.unscheduleTwitterListening(listening);

		} else {
			this.utils.addRedirectMessage("scheduledsearch.delete.fail", redirect);
		}
		return "redirect:/twitter/scheduledsearch/list";
	}

	// SCHEDULED SEARCH BEGIN END

	@GetMapping("/configurations/list")
	public String listConfigurations(Model model) {
		List<Configuration> configurations = this.configurationService.getConfigurations(Configuration.Type.TWITTER,
				this.userService.getUser(this.utils.getUserId()));
		model.addAttribute("configurations", configurations);
		return "configurations/list";
	}

	public void populateFormData(Model model) {
		model.addAttribute("configurationTypes", Configuration.Type.TWITTER);
	}
}

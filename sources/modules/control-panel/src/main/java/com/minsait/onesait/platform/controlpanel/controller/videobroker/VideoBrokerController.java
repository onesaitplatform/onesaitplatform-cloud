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
package com.minsait.onesait.platform.controlpanel.controller.videobroker;

import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hazelcast.core.IQueue;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.VideoCapture;
import com.minsait.onesait.platform.config.model.VideoCapture.Processor;
import com.minsait.onesait.platform.config.model.VideoCapture.State;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.videobroker.VideoBrokerService;
import com.minsait.onesait.platform.config.services.videobroker.VideoBrokerServiceImpl;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("video-captures")
@Slf4j
public class VideoBrokerController {

	@Autowired
	private VideoBrokerService videoBrokerService;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	@Qualifier("videoQueue")
	private IQueue<String> videoQueue;

	@GetMapping("list")
	public String list(Model model) {
		model.addAttribute("videoCaptures", videoBrokerService.getVideoCaptures(utils.getUserId()));
		return "videobroker/list";
	}

	@GetMapping("create")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String create(Model model) {
		model.addAttribute("videoCapture", new VideoCapture());
		populateForm(model);
		return "videobroker/create";
	}

	@GetMapping("update/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String update(Model model, @PathVariable("id") String id) {
		if (!videoBrokerService.hasUserAccess(id, utils.getUserId()))
			return "error/403";
		model.addAttribute("videoCapture", videoBrokerService.get(id));
		populateForm(model);
		return "videobroker/create";
	}

	@PostMapping("create")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String createVideoCapture(Model model, @Valid VideoCapture videoCapture,
			@RequestParam("new") boolean newOntology, BindingResult bindingResult, RedirectAttributes redirect) {

		videoCapture.setUser(userService.getUser(utils.getUserId()));
		if (newOntology) {
			try {
				final Ontology ontology = videoBrokerService.createOntologyVideoResults(videoCapture);
				ontologyBusinessService.createOntology(ontology, utils.getUserId(), null);
				videoCapture.setOntology(
						ontologyService.getOntologyByIdentification(ontology.getIdentification(), utils.getUserId()));
			} catch (final OntologyBusinessServiceException e) {
				utils.addRedirectException(e, redirect);
				log.error("Exception while creating ontology for video results {}", e);
				return "redirect:/video-captures/create";
			}
		}
		final String id = videoBrokerService.create(videoCapture);
		createEvent(id);
		return "redirect:/video-captures/update/" + id;
	}

	@PostMapping("startstop/{id}")
	public ResponseEntity<?> start(@PathVariable("id") String id) {
		if (!videoBrokerService.hasUserAccess(id, utils.getUserId()))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		final VideoCapture videoCapture = videoBrokerService.get(id);
		if (videoCapture == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		videoBrokerService.updateState(videoCapture);
		createEvent(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("update/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String update(Model model, @Valid VideoCapture videoCapture, @PathVariable("id") String id) {
		if (!videoBrokerService.hasUserAccess(id, utils.getUserId()))
			return "error/403";
		if (videoBrokerService.get(id).getState().equals(State.START)) {
			videoBrokerService.updateState(videoBrokerService.get(id));
			createEvent(id);
		}
		videoBrokerService.update(videoCapture);
		return "redirect:/video-captures/list";
	}

	@DeleteMapping("{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<?> delete(@PathVariable("id") String id) {
		if (!videoBrokerService.hasUserAccess(id, utils.getUserId()))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		final VideoCapture vc = videoBrokerService.get(id);
		if (vc.getState().equals(State.START)) {
			videoBrokerService.updateState(vc);
			createEvent(vc.getId());
		}
		videoBrokerService.delete(id);
		return new ResponseEntity<>("/controlpanel/video-captures/list", HttpStatus.OK);
	}

	private void populateForm(Model model) {
		model.addAttribute("processors", Processor.values());
		model.addAttribute("ontologies",
				ontologyService.getOntologiesByUserId(utils.getUserId()).stream()
						.filter(o -> o.getDataModel().getIdentification().equals(VideoBrokerServiceImpl.VIDEO_RESULT_DATA_MODEL))
						.collect(Collectors.toList()));
	}

	public void createEvent(String id) {
		boolean createdEvent =videoQueue.offer(id);
		log.debug("createEvent:"+createdEvent);
	}
}

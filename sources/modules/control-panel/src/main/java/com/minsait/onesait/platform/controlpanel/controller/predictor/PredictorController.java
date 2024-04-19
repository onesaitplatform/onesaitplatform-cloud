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
package com.minsait.onesait.platform.controlpanel.controller.predictor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.business.services.predictor.PredictorBusinessService;
import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.predictor.PredictorService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("predictors")
@Slf4j
public class PredictorController {

	@Autowired
	private PredictorService predictorService;
	@Autowired
	private PredictorBusinessService predictorBusinessService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private AppWebUtils utils;
	@Autowired 
	private HttpSession httpSession;
	
	private static final String APP_ID = "appId";

	@GetMapping("list")
	public String predictors(Model model) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		model.addAttribute("predictors", predictorBusinessService.predictors(utils.getUserId()));
		return "predictors/list";
	}

	@GetMapping("show/{predictor}")
	public String predictor(@PathVariable("predictor") String predictor, Model model) {
		model.addAttribute("predictorInfo", predictorBusinessService.predictorInfo(predictor));
		return "predictors/show";
	}

	@GetMapping("create")
	public String create(Model model, RedirectAttributes ra) {
		final List<OntologyDTO> ontologies = ontologyService
				.getAllOntologiesForListWithProjectsAccess(utils.getUserId());
		model.addAttribute("ontologies", ontologies.stream().filter(o -> !o.getRtdbDatasource().equals(RtdbDatasource.AI_MINDS_DB)).collect(Collectors.toList()));
		model.addAttribute("predictor", new PredictorDTO());
		if (model.asMap().get("error") != null) {
			model.addAttribute("message", model.asMap().get("error"));
		}
		return "predictors/create";
	}

	@PostMapping("create")
	public String create(@ModelAttribute PredictorDTO predictor, RedirectAttributes ra) {
		try {
			predictor.setUser(utils.getUserId());
			predictorBusinessService.create(predictor);
		} catch (final Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/predictors/create";
		}
		return "redirect:/predictors/list";
	}

	@DeleteMapping("{predictor}")
	public ResponseEntity<String> delete(@PathVariable("predictor") String predictor, @RequestParam("connName") String connName){
		try {
			predictorBusinessService.deletePredictor(predictor, connName);
			return ResponseEntity.ok().build();
		}catch (final Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	@PostMapping("retrain")
	public ResponseEntity<String> retrainModel(@RequestBody String predictor) {
		predictorBusinessService.retrain(predictor);
		return ResponseEntity.ok().build();
	}

	@GetMapping("{name}/code/download")
	public ResponseEntity<ByteArrayResource> getCode(@PathVariable("name") String name) {
		try {
			final String code = predictorBusinessService.code(name);
			if (code != null) {
				final ByteArrayResource resource = new ByteArrayResource(
						code.getBytes(StandardCharsets.UTF_8));
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + ".py")
						.contentLength(resource.contentLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		} catch (final Exception e) {
			log.error("Error retrieving code for predictor {}", name, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

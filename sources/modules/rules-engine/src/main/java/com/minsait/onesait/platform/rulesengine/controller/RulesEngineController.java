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
package com.minsait.onesait.platform.rulesengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.router.service.app.model.RulesEngineModel;
import com.minsait.onesait.platform.rulesengine.service.RulesEngineService;

@RestController
public class RulesEngineController {

	@Autowired
	private RulesEngineService rulesEngineService;

	@PostMapping("advice")
	public ResponseEntity<String> adviceNotification(@RequestBody RulesEngineModel model) throws GenericOPException {

		rulesEngineService.executeRulesAsync(model.getOntology(), model.getJson());

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "execute/rule/{identification}", produces = "application/json")
	public ResponseEntity<String> executeRule(@PathVariable("identification") String identification,
			@RequestBody String jsonInput) throws GenericOPException {
		if (rulesEngineService.canUserExecuteRule(identification,
				SecurityContextHolder.getContext().getAuthentication().getName())) {
			try {
				final String output = rulesEngineService.executeRestRule(identification, jsonInput);
				return new ResponseEntity<>(output, HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

}

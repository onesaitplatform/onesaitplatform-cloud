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
package com.minsait.onesait.platform.controlpanel.rest.management.ai;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.ai.AIService;
import com.minsait.onesait.platform.config.services.exceptions.AIServiceException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.annotations.ApiParam;



@Tag(name = "AI management")
@RestController
@RequestMapping("api/ai")
public class AIManagementController {

	@Autowired
	AIService aiService;
	
	@Autowired
	private AppWebUtils utils;
	
	@Operation(summary = "Text To SQL")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "Using chatgpt configured model transform plain text into sql query for the user. You can pass your OpenAI apiKey or leave empty if want to use the OpenAI apikey configured in the platform"))
	@PostMapping(value = "/textToSQL")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> textToSQL(
			@RequestParam(value = "apikey", required = false) String apikey, @ApiParam(value="TextToSQL") @Valid @RequestBody String input) {

		String answer;
		try {
		
			if (apikey == null) {
				answer = aiService.textToSQL(utils.getUserId(), input);
			} else {
				answer = aiService.textToSQL(utils.getUserId(), input, apikey);
			}
			return new ResponseEntity<>(answer, HttpStatus.OK);
		} catch(AIServiceException e) {
			switch (e.getError()) {
				case GENERIC_ERROR:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				case PERMISSION_DENIED:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
				case TOKEN_NOT_FOUND:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
				default:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(summary = "Text To Answer")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "Using chatgpt configured model send plain text and return the answer. You can pass your OpenAI apiKey or leave empty if want to use the OpenAI apikey configured in the platform"))
	@PostMapping(value = "textToAnswer")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<String> textToAnswer(
			@RequestParam(value = "apikey", required = false) String apikey, @ApiParam(value="TextToSQL") @Valid @RequestBody String input) {

		String answer;
		try {
			if (apikey == null) {
				answer = aiService.textToAnswer(utils.getUserId(), input);
			} else {
				answer = aiService.textToAnswer(utils.getUserId(), input, apikey);
			}
			return new ResponseEntity<>(answer, HttpStatus.OK);
		} catch(AIServiceException e) {
			switch (e.getError()) {
				case GENERIC_ERROR:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
				case PERMISSION_DENIED:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
				case TOKEN_NOT_FOUND:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
				default:
					return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

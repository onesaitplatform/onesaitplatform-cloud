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
package com.minsait.onesait.platform.controlpanel.rest.codeproject;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.CodeProject;
import com.minsait.onesait.platform.config.services.codeproject.CodeProjectService;
import com.minsait.onesait.platform.config.services.codeproject.dto.CodeProjectDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/codeproject")
@Tag(name = "Codeproject")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
@Slf4j
public class CodeprojectRestController {

	private CodeProjectService codeprojectService;

	@Operation(summary = "Create a code project")
	@PostMapping
	public ResponseEntity<String> create(
			@Parameter(description = "CodeProject configuration data") @RequestBody(required = true) @Valid CodeprojectEntity codeproject) {
		if (!codeproject.getCodeproject().getName().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Operation(summary = "Update a code project")
	@PutMapping
	public ResponseEntity<String> update(
			@Parameter(description = "code project configuration data") @RequestBody(required = true) @Valid CodeProjectDTO codeproject) {
		codeprojectService.update(codeproject);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Update a code project")
	@GetMapping
	public ResponseEntity<CodeProjectDTO> getCodeProject(
			@Parameter(description = "Code project name") @RequestBody(required = true) String identification) {
		final CodeProject cp = codeprojectService.getByIdentification(identification);
		if (cp == null) {
			return ResponseEntity.notFound().build();
		}
		final CodeProjectDTO dto = CodeProjectDTO.builder().name(cp.getIdentification()).owner(cp.getUserJson())
				.privateToken(cp.getGitlabConfiguration().getPrivateToken()).repo(cp.getGitlabRepository()).build();
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
}

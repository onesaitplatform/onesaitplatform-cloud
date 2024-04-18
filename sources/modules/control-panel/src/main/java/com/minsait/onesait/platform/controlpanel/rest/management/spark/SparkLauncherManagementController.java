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
package com.minsait.onesait.platform.controlpanel.rest.management.spark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.spark.SparkLauncherExecutorService;
import com.minsait.onesait.platform.config.services.spark.dto.SparkLaunchJobModel;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Spark Launcher")
@RestController
@RequestMapping("api/sparklauncher")
public class SparkLauncherManagementController {

	@Autowired
	private SparkLauncherExecutorService sparkExecutor;

	@Operation(summary = "Upload/update Spark job to Object Store and run it")
	@PostMapping(value = "/uploadAndRun")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Object Uploaded", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Bad request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found in Onesait Platform"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> upload(//@RequestParam(value = "filePath", required = true) String filePath,
		//	@RequestPart("file") MultipartFile file, 
			@RequestBody SparkLaunchJobModel sparkJobModel) {
		// upload Spark file to object storage
		try {

			sparkExecutor.executeJob(sparkJobModel);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}

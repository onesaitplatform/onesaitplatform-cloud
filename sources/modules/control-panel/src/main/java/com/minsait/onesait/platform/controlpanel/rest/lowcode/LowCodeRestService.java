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
package com.minsait.onesait.platform.controlpanel.rest.lowcode;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.controlpanel.services.lowcode.FigmaExtractedData;
import com.minsait.onesait.platform.controlpanel.services.lowcode.FigmaService;
import com.minsait.onesait.platform.controlpanel.services.lowcode.FigmaServiceException;
import com.minsait.onesait.platform.controlpanel.services.lowcode.FigmaSetUp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/low-code")
@Tag(name = "Low Code UI Generation")
@Slf4j
public class LowCodeRestService {

	@Autowired
	private FigmaService figmaService;

	@GetMapping("plugin-mappings")
	@Operation(summary = "Extract data from FIGMA project")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FigmaExtractedData.class)))
	public ResponseEntity<FigmaExtractedData> getPluginMappings(@RequestParam("fimaFileId") String figmaFileId,
			@RequestParam("figmaToken") String figmaToken) {

		try {
			return ResponseEntity.ok(figmaService.getPluginMappings(figmaFileId, figmaToken));
		} catch (final FigmaServiceException e) {
			log.error("Error extracting FIGMA data: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.valueOf(e.getStatusCode()));
		}
	}

	@PostMapping("generate")
	@Operation(summary = "Generate low code UI")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = byte[].class)))
	public ResponseEntity<byte[]> getPluginMappings(@RequestBody FigmaSetUp figmaSetUp) {

		try {
			final File app = figmaService.generateFromTemplate(figmaSetUp);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(app.getName()))
					.header(HttpHeaders.SET_COOKIE, "fileDownload=true")
					.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(app.length())
					.contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(FileUtils.readFileToByteArray(app));
		} catch (final FigmaServiceException e) {
			log.error("Error extracting FIGMA data: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.valueOf(e.getStatusCode()));
		} catch (final IOException e) {
			log.error("Error extracting FIGMA data: {}", e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

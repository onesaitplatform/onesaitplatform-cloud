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
package com.minsait.onesait.platform.controlpanel.rest.bundles;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.business.services.bundles.BundleBusinessService;
import com.minsait.onesait.platform.business.services.bundles.BundleDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/bundles")
@Tag(name = "Bundles")
@Slf4j
public class BundleRestService {

	@Autowired
	private BundleBusinessService bundleBusinessService;

	@GetMapping
	@Operation(summary = "Get all bundles")
	@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = BundleDTO[].class)))
	public ResponseEntity<List<BundleDTO>> getBundles() {

		return ResponseEntity.ok(bundleBusinessService.getBundles());

	}

	@PostMapping(value = "/download")
	public ResponseEntity<ByteArrayResource> download(@RequestParam(required = true) String projectURL,
			@RequestParam(required = true) String gitBranch, @RequestParam(required = true) String rootFolder) {
		try {
			final File output = bundleBusinessService.downloadBundle(projectURL); // TODO
			final ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(output.toPath()));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(output.getName()))
					.contentLength(resource.contentLength())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (final Exception e) {
			return ResponseEntity.internalServerError().build();
		}

	}
}

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
package com.minsait.onesait.platform.controlpanel.rest.management.themes;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.services.themes.ThemesServiceImpl;
import com.minsait.onesait.platform.config.services.themes.dto.ThemesDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Themes")
@RequestMapping("api/themes")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden"),
		@ApiResponse(responseCode = "404", description = "Not found") })

public class ThemesRestController {

	@Autowired
	private ThemesServiceImpl themesService;

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get theme CSS ")
	@GetMapping(value = "/css/{id}")

	public ResponseEntity<String> getcss(@PathVariable("id") String id) {

		final ThemesDTO f = themesService.getThemeByIdentification(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		JSONObject obj = f.getJson();

		return ResponseEntity.ok(obj.getString("CSS"));
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	@Operation(summary = "Get theme ThemesDTO")
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)

	public ResponseEntity<String> getTheme(@PathVariable("id") String id) {

		final ThemesDTO f = themesService.getThemeByIdentification(id);
		if (f == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(f.getJson().toString());
	}

}

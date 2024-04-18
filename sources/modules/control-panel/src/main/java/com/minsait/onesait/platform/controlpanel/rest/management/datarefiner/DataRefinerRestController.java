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
package com.minsait.onesait.platform.controlpanel.rest.management.datarefiner;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.controlpanel.services.datarefiner.DataRefinerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/datarefiner")
@Tag(name = "Data Refiner")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
public class DataRefinerRestController {

	@Autowired
	private DataRefinerService dataRefinerService;

	@Operation(summary = "processes input file and returns output file the selected exportype and the requested operations")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ByteArrayResource> processFile(
			@Parameter(description = "operations to be performed on the data in the file, are written in this way {...}, {...}, ... each operation between braces separated by commas") @RequestParam(required = false, value = "operations") String operations,
			@Parameter(description = "Allowed export formats csv tsv xls xlsx ods html txt") @RequestParam(required = true, value = "exportType") String exportType,
			@Parameter(description = "JSON string... (e.g. '{\"facets\":[],\"mode\":\"row-based\"}')") @RequestParam(required = false, value = "engine") String engine,
			@Parameter(description = "Json object containing options relevant to the file format ") @RequestParam(required = false, value = "importOptions") String importOptions,
			@Parameter(description = "'text/line-based': Line-based text files\n"
					+ "'text/line-based/*sv': CSV / TSV / separator-based files [separator to be used in specified in the json submitted to the options parameter]\n"
					+ "'text/line-based/fixed-width': Fixed-width field text files\n"
					+ "'binary/text/xml/xls/xlsx': Excel files\n" + "'text/json': JSON files\n"
					+ "'text/xml': XML files") @RequestPart("file") MultipartFile file,
			HttpServletRequest request) {

		if (exportType == null || exportType.equals("")) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (file == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		String authorization = request.getHeader("Authorization");
		if (authorization != null) {
			String typeAuthorization = "Authorization";
			return dataRefinerService.makeProcessData(operations, exportType, engine, importOptions, file,
					authorization, typeAuthorization);
		} else {
			authorization = request.getHeader("X-OP-APIKey");
			final String typeAuthorization = "X-OP-APIKey";
			return dataRefinerService.makeProcessData(operations, exportType, engine, importOptions, file,
					authorization, typeAuthorization);
		}
	}

}

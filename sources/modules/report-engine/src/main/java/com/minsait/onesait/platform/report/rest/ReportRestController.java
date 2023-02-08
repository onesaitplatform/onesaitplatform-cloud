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
package com.minsait.onesait.platform.report.rest;

import static com.minsait.onesait.platform.report.service.ReportInfoServiceImpl.JSON_DATA_SOURCE_ATT_NAME;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.dto.report.ParameterMapConverter;
import com.minsait.onesait.platform.config.dto.report.ReportInfoDto;
import com.minsait.onesait.platform.config.dto.report.ReportParameter;
import com.minsait.onesait.platform.config.dto.report.ReportParameterType;
import com.minsait.onesait.platform.config.dto.report.ReportType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.report.service.ReportInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Reports")
@RestController
@RequestMapping("api/reports")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
@Slf4j
public class ReportRestController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private ParameterMapConverter parameterMapConverter;

	@Autowired
	private ReportInfoService reportInfoService;

	@Operation(summary = "Download report")
	@PostMapping("{id}/{extension}")
	@Transactional
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=String.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> downloadReport(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id,
			@Parameter(description= "Parameters") @RequestBody(required = false) ReportParameter[] params,
			@Parameter(description= "Output file format", required = true) @PathVariable("extension") ReportType extension) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(SecurityContextHolder.getContext().getAuthentication().getName(), entity,
				ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		final List<ReportParameter> parameters = params != null ? Arrays.asList(params) : new ArrayList<>();
		if (StringUtils.hasText(entity.getDataSourceUrl())) {
			parameters.add(ReportParameter.builder().name(JSON_DATA_SOURCE_ATT_NAME).type(ReportParameterType.STRING)
					.value(entity.getDataSourceUrl()).build());
		}
		try {
			final Map<String, Object> map = parameters == null ? new HashMap<>()
					: parameterMapConverter.convert(parameters);

			final byte[] content = reportInfoService.generate(entity, extension, map);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=" + entity.getIdentification() + "." + extension.extension())
					.header(HttpHeaders.CONTENT_TYPE, extension.contentType())
					.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(content.length)
					.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(content);
		} catch (final Exception e) {
			log.error("Error while invoking Report REST endpoint {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Retrieve declared parameters in Jasper Template when their default values")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=ReportParameter[].class)), responseCode = "200", description = "OK"))
	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> parameters(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id) {

		final Report report = reportService.findByIdentificationOrId(id);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(SecurityContextHolder.getContext().getAuthentication().getName(), report,
				ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		try {

			final ReportInfoDto reportInfoDto = reportInfoService.extract(new ByteArrayInputStream(report.getFile()),
					report.getExtension());

			return new ResponseEntity<>(reportInfoDto.getParameters(), HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Retrieve datasource from Jasper Report")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=ReportParameter[].class)) , responseCode = "200", description = "OK"))
	@GetMapping(value = "/{id}/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> datasource(@PathVariable("id") String id) {
		final Report entity = reportService.findById(id);
		try {
			final String dataSource = reportInfoService
					.extract(new ByteArrayInputStream(entity.getFile()), entity.getExtension()).getDataSource();
			if (StringUtils.hasText(dataSource)) {
				return new ResponseEntity<>(dataSource, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

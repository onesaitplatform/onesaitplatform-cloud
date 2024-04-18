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
package com.minsait.onesait.platform.report.rest;

import static com.minsait.onesait.platform.report.service.ReportInfoServiceImpl.JSON_DATA_SOURCE_ATT_NAME;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.derby.iapi.services.io.ArrayInputStream;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(value = "Reports", tags = { "Reports REST API" })
@RestController
@RequestMapping("api/reports")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
@Slf4j
public class ReportRestController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private ParameterMapConverter parameterMapConverter;

	@Autowired
	private ReportInfoService reportInfoService;

	@ApiOperation(value = "Download report")
	@PostMapping("{id}/{extension}")
	@Transactional
	@ApiResponses(@ApiResponse(response = String.class, code = 200, message = "OK"))
	public ResponseEntity<?> downloadReport(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Parameters") @RequestBody(required = false) ReportParameter[] params,
			@ApiParam(value = "Output file format", required = true) @PathVariable("extension") ReportType extension) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(SecurityContextHolder.getContext().getAuthentication().getName(), entity,
				ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		final List<ReportParameter> parameters = params != null ? Arrays.asList(params) : new ArrayList<>();
		if (!StringUtils.isEmpty(entity.getDataSourceUrl())) {
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

	@ApiOperation(value = "Retrieve declared parameters in Jasper Template when their default values")
	@ApiResponses(@ApiResponse(response = ReportParameter[].class, code = 200, message = "OK"))
	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> parameters(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id) {

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

	@ApiOperation(value = "Retrieve datasource from Jasper Report")
	@ApiResponses(@ApiResponse(response = ReportParameter[].class, code = 200, message = "OK"))
	@GetMapping(value = "/{id}/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> datasource(@PathVariable("id") String id) {
		final Report entity = reportService.findById(id);
		try {
			final String dataSource = reportInfoService
					.extract(new ArrayInputStream(entity.getFile()), entity.getExtension()).getDataSource();
			if (!StringUtils.isEmpty(dataSource)) {
				return new ResponseEntity<>(dataSource, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.OK);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

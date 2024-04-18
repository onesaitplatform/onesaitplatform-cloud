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
package com.minsait.onesait.platform.controlpanel.rest.report;

import static com.minsait.onesait.platform.controlpanel.services.report.ReportInfoServiceImpl.JSON_DATA_SOURCE_ATT_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ParameterMapConverter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameterType;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportType;
import com.minsait.onesait.platform.controlpanel.rest.report.model.ReportDTO;
import com.minsait.onesait.platform.controlpanel.services.report.ReportInfoService;
import com.minsait.onesait.platform.controlpanel.services.report.UploadFileException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Reports", tags = { "Reports REST API" })
@RestController
@RequestMapping("api/reports")
@ApiResponses({ @ApiResponse(code = 400, message = "Bad request"),
		@ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 403, message = "Forbidden") })
@PreAuthorize("!hasRole('USER')")
public class ReportRestController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private ParameterMapConverter parameterMapConverter;

	@Autowired
	private ReportInfoService reportInfoService;

	@Autowired
	private AppWebUtils utils;

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
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		final List<ReportParameter> parameters = Arrays.asList(params);
		if (!StringUtils.isEmpty(entity.getDataSourceUrl()))
			parameters.add(ReportParameter.builder().name(JSON_DATA_SOURCE_ATT_NAME).type(ReportParameterType.STRING)
					.value(entity.getDataSourceUrl()).build());
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
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Retrieve declared parameters in Jasper Template when their default values")
	@ApiResponses(@ApiResponse(response = ReportParameter[].class, code = 200, message = "OK"))
	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ReportParameter>> parameters(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id) {

		final Report report = reportService.findByIdentificationOrId(id);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		final ReportInfoDto reportInfoDto = reportInfoService.extract(new ByteArrayInputStream(report.getFile()),
				report.getExtension());

		return new ResponseEntity<>(reportInfoDto.getParameters(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get all reports")
	@ApiResponse(response = ReportDTO[].class, code = 200, message = "OK")
	@GetMapping()
	public ResponseEntity<List<ReportDTO>> getReports() {

		final List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports()
				: reportService.findAllActiveReportsByUserId(utils.getUserId());

		final List<ReportDTO> listDTO = new ArrayList<>();
		for (final Report report : reports) {
			final ReportDTO dto = new ReportDTO();
			dto.setCreatedAt(report.getCreatedAt());
			dto.setDescription(report.getDescription());
			dto.setName(report.getIdentification());
			listDTO.add(dto);
		}

		return new ResponseEntity<>(listDTO, HttpStatus.OK);
	}

	@ApiOperation(value = "Get report by name or ID")
	@ApiResponses(value = { @ApiResponse(response = ReportDTO.class, code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Not found") })
	@GetMapping("/{id}")
	public ResponseEntity<ReportDTO> getReportById(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		final ReportDTO dto = new ReportDTO();
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setDescription(entity.getDescription());
		dto.setName(entity.getIdentification());

		return new ResponseEntity<>(dto, HttpStatus.OK);

	}

	@ApiOperation(value = "Create new report")
	@ApiResponse(code = 201, message = "CREATED")
	@PostMapping(consumes = { "multipart/form-data" })
	@Transactional
	public ResponseEntity<Object> createNewReport(
			@RequestParam(required = true, value = "identification") String identification,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = true, value = "isPublic") boolean isPublic,
			@RequestParam(required = true, value = "file") MultipartFile file) {

		final Report entity = reportService.findByIdentificationOrId(identification);
		if (entity != null) {
			return ResponseEntity.badRequest().body("Report ID must be unique");
		}

		final Report report = new Report();
		report.setIdentification(identification);
		report.setDescription(description);
		report.setIsPublic(isPublic);
		try {
			report.setFile(file.getBytes());
		} catch (final IOException e) {
			throw new UploadFileException();
		}

		report.setExtension(
				ReportExtension.valueOf(FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase()));

		report.setActive(true);

		final User user = new User();
		user.setUserId(utils.getUserId());
		report.setUser(user);

		reportService.saveOrUpdate(report);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Update report by ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found") })
	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<Object> updateReport(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id,
			@RequestParam(required = false, value = "description") String description,
			@RequestParam(required = false, value = "identification") String identification,
			@RequestParam(required = false, value = "file") MultipartFile file) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		if (description != null) {
			entity.setDescription(description);
		}
		if (identification != null) {
			entity.setIdentification(identification);
		}

		if (file != null) {
			try {
				entity.setFile(file.getBytes());
			} catch (final IOException e) {
				throw new UploadFileException();
			}
		}

		reportService.saveOrUpdate(entity);

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@ApiOperation(value = "Delete report by ID")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "DELETED"),
			@ApiResponse(code = 404, message = "Not found") })
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteReport(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id) {
		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		reportService.delete(entity.getId());

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Get file of report")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
			@ApiResponse(code = 404, message = "Not found") })
	@GetMapping("/{id}/file")
	public ResponseEntity<Object> getFileOfReport(
			@ApiParam(value = "Report ID or Name", required = true) @PathVariable("id") String id) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + entity.getIdentification() + "." + entity.getExtension())
				.header(HttpHeaders.CONTENT_TYPE, "application/" + entity.getExtension().toString().toLowerCase())
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(entity.getFile().length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(entity.getFile());

	}
}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.dto.report.ReportParameter;
import com.minsait.onesait.platform.config.dto.report.ReportType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.controlpanel.rest.report.model.ReportDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

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
@ApiResponses({ @ApiResponse(responseCode= "400", description= "Bad request"),
	@ApiResponse(responseCode= "500", description= "Internal server error"), @ApiResponse(responseCode= "403", description= "Forbidden") })
@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
@Slf4j
public class ReportRestController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String REPORT_API_PATH = "/api/reports";
	private RestTemplate restTemplate;

	@Autowired
	private AppWebUtils utils;

	@PostConstruct
	void setup() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.getInterceptors().add((request, body, execution) -> {
			if (utils.getCurrentXOpAPIKey() != null) {
				request.getHeaders().add("X-OP-APIKey", utils.getCurrentXOpAPIKey());
			}
			try {
				request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + utils.getCurrentUserOauthToken());
			} catch (final Exception e) {
				log.error("No authentication found, Bearer token");
			}
			try {
				request.getHeaders().add(Tenant2SchemaMapper.VERTICAL_HTTP_HEADER,
						MultitenancyContextHolder.getVerticalSchema());
				request.getHeaders().add(Tenant2SchemaMapper.TENANT_HTTP_HEADER,
						MultitenancyContextHolder.getTenantName());
			} catch (final Exception e) {
				log.error("No authentication found, could not add tenant/vertical headers to HTTP request");
			}

			return execution.execute(request, body);
		});
	}

	@Operation(summary= "Download report")
	@PostMapping("{id}/{extension}")
	@Transactional
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=String.class)), responseCode= "200", description= "OK"))
	public ResponseEntity<?> downloadReport(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id,
			@Parameter(description= "Parameters") @RequestBody(required = false) ReportParameter[] params,
			@Parameter(description= "Output file format", required = true) @PathVariable("extension") ReportType extension)
					throws UnsupportedEncodingException {
		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

		final String requestURL = resourcesService.getUrl(Module.REPORT_ENGINE, ServiceUrl.BASE) + REPORT_API_PATH + "/"
				+ URLEncoder.encode(id, StandardCharsets.UTF_8.name()) + "/"
				+ URLEncoder.encode(extension.name(), StandardCharsets.UTF_8.name());
		try {
			final ResponseEntity<byte[]> response = restTemplate.exchange(requestURL, HttpMethod.POST,
					new HttpEntity<>(params), byte[].class);

			return generateAttachmentResponse(response.getBody(), extension.contentType(),
					entity.getIdentification() + "." + extension.extension());
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	@Operation(summary= "Retrieve declared parameters in Jasper Template when their default values")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=ReportParameter[].class)), responseCode= "200", description= "OK"))
	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> parameters(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id)
					throws UnsupportedEncodingException {

		final Report report = reportService.findByIdentificationOrId(id);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		final String requestURL = resourcesService.getUrl(Module.REPORT_ENGINE, ServiceUrl.BASE) + REPORT_API_PATH + "/"
				+ URLEncoder.encode(id, StandardCharsets.UTF_8.name()) + "/parameters";
		try {
			final ResponseEntity<List<ReportParameter>> response = restTemplate.exchange(requestURL, HttpMethod.GET,
					null, new ParameterizedTypeReference<List<ReportParameter>>() {
			});

			return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	@Operation(summary= "Get all reports")
	@ApiResponse(content=@Content(schema=@Schema(implementation=ReportDTO[].class)), responseCode= "200", description= "OK")
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

	@Operation(summary= "Get report by name or ID")
	@ApiResponses(value = { @ApiResponse(content=@Content(schema=@Schema(implementation=ReportDTO.class)), responseCode= "200", description= "OK"),
			@ApiResponse(responseCode= "404", description= "Not found") })
	@GetMapping("/{id}")
	public ResponseEntity<ReportDTO> getReportById(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		final ReportDTO dto = new ReportDTO();
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setDescription(entity.getDescription());
		dto.setName(entity.getIdentification());

		return new ResponseEntity<>(dto, HttpStatus.OK);

	}

	@Operation(summary= "Create new report")
	@ApiResponse(responseCode= "201", description= "CREATED")
	@PostMapping(consumes = { "multipart/form-data" })
	@Transactional
	public ResponseEntity<Object> createNewReport(
			@RequestParam(required = true, value = "identification") String identification,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = true, value = "isPublic") boolean isPublic,
			@RequestParam(required = true, value = "file") MultipartFile file) {

		if (!identification.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

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
			log.error("Error while creating Report REST :{}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		report.setExtension(
				ReportExtension.valueOf(FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase()));

		report.setActive(true);

		final User user = new User();
		user.setUserId(utils.getUserId());
		report.setUser(user);

		reportService.saveOrUpdate(report);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Deprecated
	@Operation(summary= "Update report by ID")
	@ApiResponses(value = { @ApiResponse(responseCode= "200", description= "OK"), @ApiResponse(responseCode= "404", description= "Not found") })
	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<Object> updateReport(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id,
			@RequestParam(required = false, value = "description") String description,
			@RequestParam(required = false, value = "identification") String identification,
			@RequestParam(required = false, value = "file") MultipartFile file) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

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
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		reportService.saveOrUpdate(entity);

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Operation(summary= "Update report by ID")
	@ApiResponses(value = { @ApiResponse(responseCode= "200", description= "OK"), @ApiResponse(responseCode= "404", description= "Not found") })
	@PostMapping(value = "/{id}", consumes = { "multipart/form-data" })
	@Transactional
	public ResponseEntity<Object> updateWithPostReport(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id,
			@RequestParam(required = false, value = "description") String description,
			@RequestParam(required = false, value = "identification") String identification,
			@RequestParam(required = false, value = "file") MultipartFile file) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

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
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		reportService.saveOrUpdate(entity);

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Operation(summary= "Delete report by ID")
	@ApiResponses(value = { @ApiResponse(responseCode= "200", description= "DELETED"),
			@ApiResponse(responseCode= "404", description= "Not found") })
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteReport(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id) {
		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		reportService.delete(entity.getId());

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary= "Get file of report")
	@ApiResponses(value = { @ApiResponse(responseCode= "200", description= "OK", content=@Content(schema=@Schema(implementation=String.class))),
			@ApiResponse(responseCode= "404", description= "Not found") })
	@GetMapping("/{id}/file")
	public ResponseEntity<Object> getFileOfReport(
			@Parameter(description= "Report ID or Name", required = true) @PathVariable("id") String id) {

		final Report entity = reportService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + entity.getIdentification() + "." + entity.getExtension())
				.header(HttpHeaders.CONTENT_TYPE, "application/" + entity.getExtension().toString().toLowerCase())
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(entity.getFile().length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(entity.getFile());

	}

	private ResponseEntity<?> generateAttachmentResponse(byte[] byteArray, String contentType, String fileName) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(byteArray.length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(byteArray);

	}
}

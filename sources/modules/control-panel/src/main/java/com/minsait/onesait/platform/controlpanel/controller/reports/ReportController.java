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
package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.business.services.binaryrepository.factory.BinaryRepositoryServiceFactory;
import com.minsait.onesait.platform.business.services.report.ReportBusinessService;
import com.minsait.onesait.platform.business.services.report.ReportConverter;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.dto.report.ReportDto;
import com.minsait.onesait.platform.config.dto.report.ReportParameter;
import com.minsait.onesait.platform.config.dto.report.ReportResourceDTO;
import com.minsait.onesait.platform.config.dto.report.ReportType;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Role.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

@Slf4j
@RequestMapping("/reports")
@Controller
public class ReportController {

	@Autowired
	private UserService userService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportBusinessService reportBusinessService;

	@Autowired
	private BinaryRepositoryServiceFactory binaryFactory;

	@Autowired
	private ReportConverter reportConverter;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private HttpSession httpSession;

	private static final String REPORT_API_PATH = "/api/reports";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";

	private RestTemplate restTemplate;

	@PostConstruct
	void setup() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.getInterceptors().add((request, body, execution) -> {

			request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + utils.getCurrentUserOauthToken());
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

	private static final String REPORT = "report";

	@GetMapping(value = "/list/data", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<List<ReportDto>> listData() {

		final List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports()
				: reportService.findAllActiveReportsByUserId(utils.getUserId());

		return new ResponseEntity<>(reports.stream().map(r -> reportConverter.convert(r)).collect(Collectors.toList()),
				HttpStatus.OK);
	}

	@GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String list(Model model) {

		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		model.addAttribute("owners",
				userService.getAllActiveUsers().stream()
						.filter(user -> !Type.ROLE_ADMINISTRATOR.toString().equals(user.getRole().getId())
								&& !Type.ROLE_SYS_ADMIN.toString().equals(user.getRole().getId()))
						.map(User::getUserId).collect(Collectors.toList()));
		model.addAttribute("types", Arrays.asList(ReportType.values()).stream().filter(t -> !t.equals(ReportType.JRXML))
				.collect(Collectors.toList()));
		final List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports()
				: reportService.findAllActiveReportsByUserId(utils.getUserId());

		model.addAttribute("reports",
				reports.stream().map(r -> reportConverter.convert(r)).collect(Collectors.toList()));
		return "reports/list";
	}

	@GetMapping(value = "/runReport/{id}", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String runReport(@PathVariable("id") String id, Model model) {
		model.addAttribute("owners",
				userService.getAllActiveUsers().stream()
						.filter(user -> !Type.ROLE_ADMINISTRATOR.toString().equals(user.getRole().getId())
								&& !Type.ROLE_SYS_ADMIN.toString().equals(user.getRole().getId()))
						.map(User::getUserId).collect(Collectors.toList()));
		model.addAttribute("types", Arrays.asList(ReportType.values()).stream().filter(t -> !t.equals(ReportType.JRXML))
				.collect(Collectors.toList()));
		final List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports()
				: reportService.findAllActiveReportsByUserId(utils.getUserId());

		model.addAttribute("reports",
				reports.stream().map(r -> reportConverter.convert(r)).collect(Collectors.toList()));
		model.addAttribute("idToRun", id);
		return "reports/list";
	}

	@GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	public ModelAndView create(Model model) {

		ReportDto report = ReportDto.builder().isPublic(Boolean.FALSE).build();

		if (model.asMap().get(REPORT) != null) {
			report = (ReportDto) model.asMap().get(REPORT);
		}

		ModelAndView newModel = new ModelAndView("reports/create", REPORT, report);
		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			newModel.addObject(APP_ID, projectId.toString());
		}

		return newModel;
	}

	@GetMapping(value = "/edit/{id}", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String edit(@PathVariable("id") String id, Model model) throws UnsupportedEncodingException {

		final Report entity = reportService.findById(id);
		if (entity == null) {
			return "redirect:/404";
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return "redirect:/403";
		}
		final ReportDto report = reportConverter.convert(entity);
		if (StringUtils.isEmpty(entity.getDataSourceUrl())) {
			final String requestURL = resourcesService.getUrl(Module.REPORT_ENGINE, ServiceUrl.BASE) + REPORT_API_PATH
					+ "/" + URLEncoder.encode(id, StandardCharsets.UTF_8.name()) + "/datasource";
			try {
				final ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, null,
						String.class);
				if (!StringUtils.isEmpty(response.getBody())) {
					model.addAttribute("dataSource", response.getBody());
				}
			} catch (final Exception e) {
				log.error("Could not extract datasource from report, leaving empty");
			}
		}

		model.addAttribute(REPORT, report);

		return "reports/create";
	}

	@PostMapping(value = "/edit/{id}/resources/fragment", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String resourcesFragment(@PathVariable("id") String id, Model model) throws UnsupportedEncodingException {

		final Report entity = reportService.findById(id);
		if (entity == null) {
			return "redirect:/404";
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return "redirect:/403";
		}
		final ReportDto report = reportConverter.convert(entity);
		if (StringUtils.isEmpty(entity.getDataSourceUrl())) {
			final String requestURL = resourcesService.getUrl(Module.REPORT_ENGINE, ServiceUrl.BASE) + REPORT_API_PATH
					+ "/" + URLEncoder.encode(id, StandardCharsets.UTF_8.name()) + "/datasource";
			try {
				final ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.GET, null,
						String.class);
				if (!StringUtils.isEmpty(response.getBody())) {
					model.addAttribute("dataSource", response.getBody());
				}
			} catch (final Exception e) {
				log.error("Could not extract datasource from report, leaving empty");
			}
		}

		model.addAttribute(REPORT, report);

		return "fragments/report-edit";
	}

	@PostMapping(value = "/save", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String save(@Valid @ModelAttribute("report") ReportDto report, RedirectAttributes ra) {
		try {
			final Report entity = reportConverter.convert(report);
			if (reportService.findByIdentificationOrId(entity.getIdentification()) != null) {
				utils.addRedirectMessage("reports.duplicated", ra);
				ra.addFlashAttribute(REPORT, report);
				return "redirect:/reports/create";
			}
			reportService.saveOrUpdate(entity);

			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.REPORT.toString());
				httpSession.setAttribute("resourceIdentificationAdded", entity.getIdentification());
				httpSession.removeAttribute(APP_ID);
				return REDIRECT_PROJECT_SHOW + projectId.toString();
			}

			return "redirect:/reports/list";
		} catch (final Exception e) {
			log.error("Error creating Report", e);
			utils.addRedirectException(e, ra);
			ra.addFlashAttribute(REPORT, report);
			return "redirect:/reports/create";

		}
	}

	@PostMapping(value = "/update", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String update(@Valid @ModelAttribute("report") ReportDto report, RedirectAttributes ra) {

		final Report target = reportService.findById(report.getId());
		if (target == null) {
			return "redirect:/404";
		}
		if (!reportService.hasUserPermission(utils.getUserId(), target, ResourceAccessType.MANAGE)) {
			return "redirect:/403";
		}
		try {
			final Report entity = reportConverter.merge(target, report);

			reportService.saveOrUpdate(entity);

			return "redirect:/reports/list";
		} catch (final Exception e) {
			log.error("Error updating report", e);
			utils.addRedirectException(e, ra);
			return "redirect:/update/" + target.getId();

		}
	}

	@PostMapping(value = "/download/report/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> downloadReport(@PathVariable("id") String id, @RequestParam("parameters") String params,
			@RequestParam("extension") ReportType extension) throws JRException, IOException {

		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
		final List<ReportParameter> parameters = mapper.readValue(params, new TypeReference<List<ReportParameter>>() {
		});
		final String requestURL = resourcesService.getUrl(Module.REPORT_ENGINE, ServiceUrl.BASE) + REPORT_API_PATH + "/"
				+ URLEncoder.encode(id, StandardCharsets.UTF_8.name()) + "/"
				+ URLEncoder.encode(extension.name(), StandardCharsets.UTF_8.name());
		try {
			final ResponseEntity<byte[]> response = restTemplate.exchange(requestURL, HttpMethod.POST,
					new HttpEntity<>(parameters), byte[].class);

			return generateAttachmentResponse(response.getBody(), extension.contentType(),
					entity.getIdentification() + "." + extension.extension());
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
			final org.springframework.http.HttpHeaders responseHeaders = new org.springframework.http.HttpHeaders();
			responseHeaders.add("X-Download-Error",
					e.getResponseBodyAsString().replaceAll("\n", " ").replace("\r", " "));
			return new ResponseEntity<>(e.getResponseBodyAsString(), responseHeaders, e.getStatusCode());
		}

	}

	@GetMapping(value = "/download/report-design/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> downloadTemplate(@PathVariable("id") String id) {

		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return generateAttachmentResponse(entity.getFile(), ReportType.JRXML.contentType(),
				entity.getIdentification() + "." + ReportType.JRXML.extension());

	}

	@DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<Boolean> delete(@PathVariable("id") String id) {
		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		final List<String> resourceIds = entity.getResources().stream().map(BinaryFile::getId)
				.collect(Collectors.toList());
		reportService.delete(id);
		resourceIds.forEach(r -> {
			if (reportService.countAssociatedReportsToResource(r) == 0) {
				try {
					binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).removeBinary(r);
				} catch (final BinaryRepositoryException e) {
					log.error("Could not delete resource from binary repository");
				}
			}
		});

		return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
	}

	// @PostMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	// @PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	// @Transactional
	// public ResponseEntity<ReportInfoDto> reportInfo(@RequestParam("file")
	// MultipartFile multipartFile)
	// throws IOException {
	//
	// final ReportInfoDto reportInfoDto =
	// reportInfoService.extract(multipartFile.getInputStream(),
	// ReportExtension.valueOf(FilenameUtils.getExtension(multipartFile.getOriginalFilename()).toUpperCase()));
	//
	// return new ResponseEntity<>(reportInfoDto, HttpStatus.OK);
	// }

	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> parameters(@PathVariable("id") String id) throws UnsupportedEncodingException {

		final Report report = reportService.findById(id);
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

	@DeleteMapping("report/{report}/resource/{resource}")
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<String> removeResource(@PathVariable("report") String reportId,
			@PathVariable("resource") String resource) {
		final Report report = reportService.findById(reportId);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		reportService.deleteResource(report, resource);
		if (reportService.countAssociatedReportsToResource(resource) == 0) {
			try {
				binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).removeBinary(resource);
			} catch (final BinaryRepositoryException e) {
				log.error("Could not delete binary file ", e);
				return new ResponseEntity<>("Could not delete binary file ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("report/{report}/resource/{resource}")
	@Transactional
	public String updateResource(@PathVariable("report") String reportId, @PathVariable("resource") String resource,
			@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
		final Report report = reportService.findById(reportId);
		if (report == null) {
			return "error/404";
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW)) {
			return "error/403";
		}
		try {
			reportBusinessService.updateResource(report, resource, file);
		} catch (final Exception e) {
			log.error("Could not update resource for report {}", report, e);
			utils.addRedirectException(e, ra);
			return "redirect:/reports/edit/" + reportId;
		}

		return "redirect:/reports/edit/" + reportId;
	}

	@PutMapping("report/resources")
	@Transactional
	public ResponseEntity<String> updateResource(@RequestParam("resourceId") String resourceId,
			@RequestParam("reportId") String reportId) {
		final Report report = reportService.findById(reportId);
		if (report == null) {
			return ResponseEntity.notFound().build();
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.MANAGE)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		try {
			reportService.addBinaryFileToResource(report, resourceId);
		} catch (final Exception e) {
			log.error("Could not add resource to report {}", report, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok().build();
	}

	@GetMapping("/resources")
	@Transactional
	public ResponseEntity<List<ReportResourceDTO>> resources(@RequestParam("currentReportId") String currentReportId) {
		try {
			final List<ReportResourceDTO> resources = reportService
					.findResourcesAvailableExcludingSelf(utils.getUserId(), currentReportId).stream()
					.map(r -> ReportResourceDTO.builder().fileName(r.getFileName()).id(r.getId())
							.userId(r.getUser().getUserId()).build())
					.collect(Collectors.toList());
			return ResponseEntity.ok().body(resources);
		} catch (final Exception e) {
			log.error("Error while fetching available resources for report {}", currentReportId, e);
			return ResponseEntity.badRequest().build();
		}
	}

	private ResponseEntity<?> generateAttachmentResponse(byte[] byteArray, String contentType, String fileName) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(byteArray.length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(byteArray);

	}
}

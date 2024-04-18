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
package com.minsait.onesait.platform.controlpanel.controller.reports;

import static com.minsait.onesait.platform.controlpanel.services.report.ReportInfoServiceImpl.JSON_DATA_SOURCE_ATT_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.apache.derby.iapi.services.io.ArrayInputStream;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.model.Role.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ParameterMapConverter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameterType;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportType;
import com.minsait.onesait.platform.controlpanel.services.report.ReportInfoException;
import com.minsait.onesait.platform.controlpanel.services.report.ReportInfoService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

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
	private ReportConverter reportConverter;

	@Autowired
	private ParameterMapConverter parameterMapConverter;

	@Autowired
	private ReportInfoService reportInfoService;

	@Autowired
	private AppWebUtils utils;

	private static final String REPORT = "report";

	@GetMapping(value = "/list/data", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<List<ReportDto>> listData() {

		final List<Report> reports = utils.isAdministrator() ? reportService.findAllActiveReports()
				: reportService.findAllActiveReportsByUserId(utils.getUserId());

		return new ResponseEntity<>(reports.stream().map(r -> reportConverter.convert(r)).collect(Collectors.toList()),
				HttpStatus.OK);
	}

	@GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public String list(Model model) {
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
	@PreAuthorize("!hasRole('ROLE_USER')")
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
	@PreAuthorize("!hasRole('ROLE_USER')")
	public ModelAndView create(Model model) {

		ReportDto report = ReportDto.builder().isPublic(Boolean.FALSE).build();

		if (model.asMap().get(REPORT) != null)
			report = (ReportDto) model.asMap().get(REPORT);

		return new ModelAndView("reports/create", REPORT, report);
	}

	@GetMapping(value = "/edit/{id}", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public String edit(@PathVariable("id") String id, Model model) {

		final Report entity = reportService.findById(id);
		if (entity == null)
			return "redirect:/404";
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE))
			return "redirect:/403";
		final ReportDto report = reportConverter.convert(entity);
		if (StringUtils.isEmpty(entity.getDataSourceUrl())) {
			try {
				final String dataSource = reportInfoService
						.extract(new ArrayInputStream(entity.getFile()), entity.getExtension()).getDataSource();
				if (!StringUtils.isEmpty(dataSource))
					model.addAttribute("dataSource", dataSource);
			} catch (final ReportInfoException e) {
				log.error("Jasper template error", e);
			}
		}

		model.addAttribute(REPORT, report);

		return "reports/create";
	}

	@PostMapping(value = "/save", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
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

			return "redirect:/reports/list";
		} catch (final Exception e) {
			log.error("Error creating Report", e);
			utils.addRedirectException(e, ra);
			ra.addFlashAttribute(REPORT, report);
			return "redirect:/reports/create";

		}
	}

	@PostMapping(value = "/update", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public String update(@Valid @ModelAttribute("report") ReportDto report, RedirectAttributes ra) {

		final Report target = reportService.findById(report.getId());
		if (target == null)
			return "redirect:/404";
		if (!reportService.hasUserPermission(utils.getUserId(), target, ResourceAccessType.MANAGE))
			return "redirect:/403";
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
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> downloadReport(@PathVariable("id") String id, @RequestParam("parameters") String params,
			@RequestParam("extension") ReportType extension) throws JRException, IOException {

		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		try {

			final ObjectMapper mapper = new ObjectMapper();
			mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
			final List<ReportParameter> parameters = mapper.readValue(params,
					new TypeReference<List<ReportParameter>>() {
					});
			if (!StringUtils.isEmpty(entity.getDataSourceUrl()))
				parameters.add(ReportParameter.builder().name(JSON_DATA_SOURCE_ATT_NAME)
						.type(ReportParameterType.STRING).value(entity.getDataSourceUrl()).build());

			final Map<String, Object> map = parameters == null ? new HashMap<>()
					: parameterMapConverter.convert(parameters);

			final byte[] content = reportInfoService.generate(entity, extension, map);

			return generateAttachmentResponse(content, extension.contentType(),
					entity.getIdentification() + "." + extension.extension());
		} catch (final Exception e) {
			log.error("Could not generate report {}", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("X-Download-Error", e.getMessage())
					.build();
		}

	}

	@GetMapping(value = "/download/report-design/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> downloadTemplate(@PathVariable("id") String id) {

		final Report entity = reportService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		return generateAttachmentResponse(entity.getFile(), ReportType.JRXML.contentType(),
				entity.getIdentification() + "." + ReportType.JRXML.extension());

	}

	@DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<Boolean> delete(@PathVariable("id") String id) {
		final Report entity = reportService.findById(id);
		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		reportService.delete(id);

		return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
	}

	@PostMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<ReportInfoDto> reportInfo(@RequestParam("file") MultipartFile multipartFile)
			throws IOException {

		final ReportInfoDto reportInfoDto = reportInfoService.extract(multipartFile.getInputStream(),
				ReportExtension.valueOf(FilenameUtils.getExtension(multipartFile.getOriginalFilename()).toUpperCase()));

		return new ResponseEntity<>(reportInfoDto, HttpStatus.OK);
	}

	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> parameters(@PathVariable("id") String id) {

		final Report report = reportService.findById(id);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		try {
			final ReportInfoDto reportInfoDto = reportInfoService.extract(new ByteArrayInputStream(report.getFile()),
					report.getExtension());

			return new ResponseEntity<>(reportInfoDto.getParameters(), HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@DeleteMapping("report/{report}/resource/{resource}")
	@PreAuthorize("!hasRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<String> removeResource(@PathVariable("report") String reportId,
			@PathVariable("resource") String resource) {
		final Report report = reportService.findById(reportId);
		if (report == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		report.getResources().removeIf(r -> r.getId().equals(resource));
		reportService.saveOrUpdate(report);
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
		if (!reportService.hasUserPermission(utils.getUserId(), report, ResourceAccessType.VIEW))
			return "error/403";
		try {
			reportInfoService.updateResource(report, resource, file);
		} catch (final Exception e) {
			log.error("Could not update resource for report {}", report, e);
			utils.addRedirectException(e, ra);
			return "redirect:/reports/edit/" + reportId;
		}

		return "redirect:/reports/edit/" + reportId;
	}

	private ResponseEntity<?> generateAttachmentResponse(byte[] byteArray, String contentType, String fileName) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(byteArray.length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(byteArray);

	}
}

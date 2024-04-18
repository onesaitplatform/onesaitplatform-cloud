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
package com.minsait.onesait.platform.controlpanel.controller.email;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.dto.email.EmailDTO;
import com.minsait.onesait.platform.config.dto.report.ReportInfoMSTemplateDTO;
import com.minsait.onesait.platform.config.dto.report.ReportType;
import com.minsait.onesait.platform.config.model.Email;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role.Type;
import com.minsait.onesait.platform.config.services.email.EmailConverter;
import com.minsait.onesait.platform.config.services.email.EmailService;
import com.minsait.onesait.platform.config.services.templates.poi.PoiTemplatesUtil;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailServiceImpl;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/emails")
@Controller
public class EmailController {
	
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AppWebUtils utils;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private EmailConverter emailConverter;
	
	@Autowired
	PoiTemplatesUtil poiTemplatesUtil;
	
	@Autowired
	MailServiceImpl mailServiceImpl;
	
	private static final String EMAIL = "email";

	
	@GetMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String list(Model model) {


		model.addAttribute("owners",
				userService.getAllActiveUsers().stream()
						.filter(user -> !Type.ROLE_ADMINISTRATOR.toString().equals(user.getRole().getId())
								&& !Type.ROLE_SYS_ADMIN.toString().equals(user.getRole().getId()))
						.map(User::getUserId).collect(Collectors.toList()));
		final List<Email> emails = utils.isAdministrator() ? emailService.findAllEmails(utils.getUserId())
				: emailService.findAllEmailsByUserId(utils.getUserId());

		model.addAttribute("emails",
				emails.stream().map(r -> emailConverter.convert(r)).collect(Collectors.toList()));
		return "emails/list";
	}
	
	@GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	public ModelAndView create(Model model) {

		EmailDTO email = EmailDTO.builder().build();

		if (model.asMap().get(EMAIL) != null) {
			email = (EmailDTO) model.asMap().get(EMAIL);
		}

		ModelAndView newModel = new ModelAndView("emails/create", EMAIL, email);

		return newModel;
	}
	
	@PostMapping(value = "/save", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String save(@Valid @ModelAttribute("email") EmailDTO email, RedirectAttributes ra) {
		try {
			final Email entity = emailConverter.convert(email);
			if (emailService.findByIdentificationOrId(entity.getIdentification()) != null) {
				utils.addRedirectMessage("emails.duplicated", ra);
				ra.addFlashAttribute(EMAIL, email);
				return "redirect:/emails/create";
			}
			emailService.saveOrUpdate(entity);

			

			return "redirect:/emails/list";
		} catch (final Exception e) {
			log.error("Error creating Email", e);
			utils.addRedirectException(e, ra);
			ra.addFlashAttribute(EMAIL, email);
			return "redirect:/emails/create";

		}
	}
	
	@GetMapping(value = "/edit/{id}", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String edit(@PathVariable("id") String id, Model model) throws UnsupportedEncodingException {

		final Email entity = emailService.findById(id);
		if (entity == null) {
			return "redirect:/404";
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return "redirect:/403";
		}
		final EmailDTO email = emailConverter.convert(entity);
	
		model.addAttribute(EMAIL, email);

		return "emails/create";
	}

	@PostMapping(value = "/update", produces = MediaType.TEXT_HTML_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public String update(@Valid @ModelAttribute("email") EmailDTO email, RedirectAttributes ra) {

		final Email target = emailService.findById(email.getId());
		if (target == null) {
			return "redirect:/404";
		}
		if (!emailService.hasUserPermission(utils.getUserId(), target, ResourceAccessType.MANAGE)) {
			return "redirect:/403";
		}
		try {
			final Email entity = emailConverter.merge(target, email);

			emailService.saveOrUpdate(entity);

			return "redirect:/emails/list";
		} catch (final Exception e) {
			log.error("Error updating email", e);
			utils.addRedirectException(e, ra);
			return "redirect:/emails/" + target.getId();

		}
	}
	
	@GetMapping(value = "/download/template/{id}", produces = { MediaType.APPLICATION_PDF_VALUE })
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> downloadTemplate(@PathVariable("id") String id) {

		final Email entity = emailService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return generateAttachmentResponse(entity.getFile(), ReportType.DOCX.contentType(),
				entity.getIdentification() + "." + ReportType.DOCX.extension());

	}
	
	private ResponseEntity<?> generateAttachmentResponse(byte[] byteArray, String contentType, String fileName) {
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(byteArray.length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(byteArray);

	}
	
	@DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<Boolean> delete(@PathVariable("id") String id) {
		final Email entity = emailService.findById(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		emailService.delete(entity);
		

		return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
	}
	
	@PostMapping(value ="/sendEmail/{id}")
	@ApiOperation(value = "Send Email")
	public ResponseEntity<?> generateAndDownloadReport(@PathVariable("id") String id,
			@RequestParam("parameters") String params, @RequestParam("subject") String subject, @RequestParam("recievers") String[] recievers)
			throws IOException {

		Email email = emailService.findById(id);
		
		if (email == null || email.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), email, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		String wordPath = poiTemplatesUtil.generateReport(params, email.getFile());
		
		InputStream docxstream = new FileInputStream(wordPath);
			
		String html = emailService.createEmailContent(docxstream);
		
		try {
			mailServiceImpl.sendHtmlMail(recievers, subject, html);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			return new ResponseEntity<String>("Error sending email",HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<String>("Email Sent correctly",HttpStatus.OK);


		

		
		
	}
	
	@GetMapping(value = "/{id}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Transactional
	public ResponseEntity<?> parameters(@PathVariable("id") String id) throws UnsupportedEncodingException {

		List<String> parameters = new ArrayList<String>();

		final Email email = emailService.findById(id);
		if (email == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), email, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}


		try {
			parameters = poiTemplatesUtil.extractFromDocx(new ByteArrayInputStream(email.getFile()));
		} catch (IOException |OpenXML4JException e) {
			return new ResponseEntity<String>("Error collecting parameters",HttpStatus.FORBIDDEN);
		} 

		ReportInfoMSTemplateDTO reportInfoMSTemplateDTO = new ReportInfoMSTemplateDTO();
		reportInfoMSTemplateDTO.setFormType(poiTemplatesUtil.formType(parameters));

		reportInfoMSTemplateDTO.setJsonParameters(poiTemplatesUtil.generateJSONObject(parameters,reportInfoMSTemplateDTO.getFormType()));

		return new ResponseEntity<>(reportInfoMSTemplateDTO, HttpStatus.OK);

	}
	
}

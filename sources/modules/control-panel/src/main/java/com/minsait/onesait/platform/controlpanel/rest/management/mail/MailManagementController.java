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
package com.minsait.onesait.platform.controlpanel.rest.management.mail;

import static com.minsait.onesait.platform.controlpanel.rest.management.mail.MailManagementUrl.OP_MAIL;
import static com.minsait.onesait.platform.controlpanel.rest.management.mail.MailManagementUrl.OP_TEMPLATE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
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
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.dto.email.EmailDTO;
import com.minsait.onesait.platform.config.model.Email;
import com.minsait.onesait.platform.config.model.SupportRequest;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.repository.SupportRepository;
import com.minsait.onesait.platform.config.services.email.EmailService;
import com.minsait.onesait.platform.config.services.templates.poi.PoiTemplatesUtil;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.annotations.ApiOperation;


import lombok.extern.slf4j.Slf4j;





@Tag(name = "Mail Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@Slf4j
@RequestMapping("api" + OP_MAIL)
public class MailManagementController {

	@Value("${onesaitplatform.mailService.mailSupport}")
	private String suportRequest;

	private static final String STATUS_OK = "{\"status\" : \"ok\"}";
	private static final String STATUS_FAIL = "{\"status\" : \"fail\"}";
	private static final String SUPPORT_REQUEST = "Support Request";
	private static final String ERROR_REQUEST = "Error sending the support request: ";

	@Autowired
	EmailService emailService;
	
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;
	@Autowired
	private SupportRepository supportRepository;
	@Autowired
	private PoiTemplatesUtil poiTemplatesUtil;


	@Operation(summary = "Send mail to support")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendSupport")
	public ResponseEntity<String> sendSupport(@RequestParam("message") String message) {
		final User user = userService.getUserByIdentification(utils.getUserId());
		final String supportRequest;
		supportRequest = "User: " + user.getUserId() + "\nEmail: " + user.getEmail() + "\nText:\n    " + message;

		try {
			mailService.sendMail(suportRequest, SUPPORT_REQUEST, supportRequest);
		} catch (final RuntimeException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@Operation(summary = "Send html mail to support")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendHtmlSupport")
	public ResponseEntity<String> sendSupportHtml(@RequestParam("htmlMessage") String htmlMessage,
			String attachmentName, @RequestBody String attachment) {
		try {
			final String[] to = new String[1];
			to[0] = suportRequest;
			mailService.sendHtmlMailWithFile(to, SUPPORT_REQUEST, htmlMessage, attachmentName, attachment, true);
		} catch (final RuntimeException | MessagingException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@Operation(summary = "Send mail to support with templates")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendTemplatesSupport")
	public ResponseEntity<String> sendSupportTemplates(@RequestParam("template") SimpleMailMessage template,
			@RequestParam("templateArgs") String templateArgs) {
		try {
			mailService.sendMailWithTemplate(suportRequest, SUPPORT_REQUEST, template, templateArgs);
		} catch (final RuntimeException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendMultiple")
	public ResponseEntity<String> send(@RequestParam("to") String[] to, @RequestParam("message") String message,
			String subject) {
		final User user = userService.getUserByIdentification(utils.getUserId());

		try {
			mailService.sendMail(to, subject,
					message + "\n\n Message sent by " + user.getUserId() + "\n Onesait Platform");
		} catch (final RuntimeException e) {
			log.error("Error sending the mail: " + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/send")
	public ResponseEntity<String> sendToOne(@RequestParam("to") String to, @RequestParam("message") String message,
			String subject) {
		return send(toArray(to), message, subject);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send html mail")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendHtmlMultiple")
	public ResponseEntity<String> sendHtml(@RequestParam("to") String[] to,
			@RequestParam("htmlMessage") String htmlMessage, String attachmentName, @RequestBody String attachment,
			String subject) {
		try {
			mailService.sendHtmlMailWithFile(to, subject, htmlMessage, attachmentName, attachment, true);
		} catch (final RuntimeException | MessagingException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send html mail")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendHtml")
	public ResponseEntity<String> sendHtmlToOne(@RequestParam("to") String to,
			@RequestParam("htmlMessage") String htmlMessage, String attachmentName, @RequestBody String attachment,
			String subject) {
		return sendHtml(toArray(to), htmlMessage, attachmentName, attachment, subject);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail with file")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendMailWithFileMultiple")
	public ResponseEntity<String> sendMailWithFile(@RequestParam("to") String[] to,
			@RequestParam("Message") String message, String attachmentName, @RequestBody String attachment,
			String subject) {
		try {
			mailService.sendHtmlMailWithFile(to, subject, message, attachmentName, attachment, false);
		} catch (final RuntimeException | MessagingException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail with file")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendMailWithFile")
	public ResponseEntity<String> sendMailWithFileToOne(@RequestParam("to") String to,
			@RequestParam("Message") String message, String attachmentName, @RequestBody String attachment,
			String subject) {
		return sendMailWithFile(toArray(to), message, attachmentName, attachment, subject);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail with templates")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendTemplatesMultiple")
	public ResponseEntity<String> sendTemplates(@RequestParam("to") String[] to,
			@RequestParam("template") SimpleMailMessage template, @RequestParam("templateArgs") String templateArgs,
			String subject) {
		try {
			mailService.sendMailWithTemplate(to, subject, template, templateArgs);
		} catch (final RuntimeException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail with templates")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendTemplates")
	public ResponseEntity<String> sendTemplatesToOne(@RequestParam("to") String to,
			@RequestParam("template") SimpleMailMessage template, @RequestParam("templateArgs") String templateArgs,
			String subject) {
		return sendTemplates(toArray(to), template, templateArgs, subject);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@Operation(summary = "Send support Request mail")
	@PostMapping(OP_MAIL + "/sendSupportRequest")
	public ResponseEntity<String> sendEmail(@RequestParam("supportRequestId") String supportRequestId,
			@RequestParam("message") String message) {

		final Optional<SupportRequest> opt = supportRepository.findById(supportRequestId);
		if (!opt.isPresent())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		final User user = opt.get().getUser();

		try {
			mailService.sendMail(user.getEmail(), SUPPORT_REQUEST, message);
			log.info("Send email to: " + user.getEmail() + "with the message" + message);
		} catch (final RuntimeException e) {
			log.error("Error sending the e-mail: " + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}
	
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@Operation(summary = "Send mail with html body")	
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=MailService.class)), responseCode = "200", description = "OK"))
	@PostMapping(OP_MAIL + "/sendHtmlBodyEmail")
	public ResponseEntity<String> sendHtmlBodyEmail(@RequestParam("to") String[] to, @RequestBody String message,
			String subject) {
		try {
			mailService.sendHtmlMail(to, subject, message);
		} catch (final RuntimeException | MessagingException e) {
			log.error(ERROR_REQUEST + e.getMessage());
			return new ResponseEntity<>(STATUS_FAIL, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
	}
	
	@Operation(summary = "Create new email template")
	@ApiResponse(responseCode = "201", description = "CREATED")
	@PostMapping(value = OP_TEMPLATE + "/newEmailTemplate", consumes = { "multipart/form-data" })
	public ResponseEntity<Object> createNewEmail(
			@RequestParam(required = true, value = "identification") String identification,
			@RequestParam(required = true, value = "description") String description,
			@RequestParam(required = true, value = "file") MultipartFile file) {

		if (!identification.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		final Email entity = emailService.findByIdentificationOrId(identification);
		if (entity != null) {
			return ResponseEntity.badRequest().body("email ID must be unique");
		}

		final Email email = new Email();
		email.setIdentification(identification);
		email.setDescription(description);
		try {
			email.setFile(file.getBytes());
		} catch (final IOException e) {
			log.error("Error while creating email REST :{}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		final User user = new User();
		user.setUserId(utils.getUserId());
		email.setUser(user);

		emailService.saveOrUpdate(email);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Operation(summary = "Update email by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@PutMapping(value = OP_TEMPLATE+"/{id}", consumes = { "multipart/form-data" })
	@Transactional
	public ResponseEntity<Object> updateWithPostEmail(
			@Parameter(description = "Email ID or Name", required = true) @PathVariable("id") String id,
			@RequestParam(required = false, value = "description") String description,
			@RequestParam(required = false, value = "identification") String identification,
			@RequestParam(required = false, value = "file") MultipartFile file) {

		final Email entity = emailService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
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

		emailService.saveOrUpdate(entity);

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@Operation(summary = "Get template file")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@GetMapping( OP_TEMPLATE+"/{id}/file")
	public ResponseEntity<Object> getFileOfEmail(
			@Parameter(description = "Email ID or Name", required = true) @PathVariable("id") String id) {

		final Email entity = emailService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + entity.getIdentification() + ".docx")
				.header(HttpHeaders.CONTENT_TYPE, "application/docx")
				.header(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate").contentLength(entity.getFile().length)
				.header(HttpHeaders.SET_COOKIE, "fileDownload=true").body(entity.getFile());

	}

	@Operation(summary = "Delete email by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "DELETED"),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@DeleteMapping(OP_TEMPLATE+"/{id}")
	public ResponseEntity<Object> deleteEmail(
			@Parameter(description = "email ID or Name", required = true) @PathVariable("id") String id) {
		final Email entity = emailService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.MANAGE)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		emailService.delete(entity);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Get email template by name or ID")
	@ApiResponses(value = {
			@ApiResponse(content = @Content(schema = @Schema(implementation = EmailDTO.class)), responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@GetMapping(OP_TEMPLATE+"/{id}")
	public ResponseEntity<EmailDTO> getEmailById(
			@Parameter(description = "Email ID or Name", required = true) @PathVariable("id") String id) {

		final Email entity = emailService.findByIdentificationOrId(id);

		if (entity == null || entity.getFile() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!emailService.hasUserPermission(utils.getUserId(), entity, ResourceAccessType.VIEW)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		final EmailDTO dto = new EmailDTO();
		dto.setDescription(entity.getDescription());
		dto.setIdentification(entity.getIdentification());
		dto.setCreated(entity.getCreatedAt());
		dto.setFileName(entity.fileName());
		dto.setId(entity.getId());
		dto.setOwner(entity.getUser().getUserId());
		return new ResponseEntity<>(dto, HttpStatus.OK);

	}

	@Operation(summary = "Get all email templates")
	@ApiResponse(content = @Content(schema = @Schema(implementation = EmailDTO[].class)), responseCode = "200", description = "OK")
	@GetMapping( OP_TEMPLATE + "/allEmails")
	public ResponseEntity<List<EmailDTO>> getEmails() {

		final List<Email> emails = utils.isAdministrator() ? emailService.findAllEmails(utils.getUserId())
				: emailService.findAllEmailsByUserId(utils.getUserId());

		final List<EmailDTO> listDTO = new ArrayList<>();
		for (final Email email : emails) {
			final EmailDTO dto = new EmailDTO();
			dto.setCreated(email.getCreatedAt());
			dto.setDescription(email.getDescription());
			dto.setIdentification(email.getIdentification());
			dto.setFileName(email.fileName());
			dto.setId(email.getId());
			dto.setOwner(email.getUser().getUserId());
			listDTO.add(dto);
		}

		return new ResponseEntity<>(listDTO, HttpStatus.OK);
	}

	@Operation(summary = "Send email by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@PostMapping(value =  OP_TEMPLATE + "/sendEmail/{id}")
	public ResponseEntity<?> sendEmail(
			@PathVariable("id") String id,
			@RequestParam("to") String[] to, @RequestParam("subject") String subject, @RequestParam("parameters") String params){

		Email email = emailService.findByIdentificationOrId(id);
		
		
		try {
			String path = poiTemplatesUtil.generateReport(params, email.getFile());
			InputStream is = new FileInputStream(path);
			String html = emailService.createEmailContent(is);
			is.close();
			mailService.sendHtmlMail(to, subject, html);
		} catch (MessagingException | IOException e) {
			return new ResponseEntity<>("Error sending the email", HttpStatus.INTERNAL_SERVER_ERROR);

		}
		
		
		return new ResponseEntity<>("Email Sent", HttpStatus.OK);

	}
	
	@Operation(summary = "Get HTML for email template")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not found") })
	@PostMapping(value =  OP_TEMPLATE + "/getHtml/{id}")
	public ResponseEntity<?> getEmailHtml(
			@PathVariable("id") String id,@RequestParam("parameters") String params){

		Email email = emailService.findByIdentificationOrId(id);
		
		String html="";
		try {
			String path = poiTemplatesUtil.generateReport(params, email.getFile());
			InputStream is = new FileInputStream(path);
			 html = emailService.createEmailContent(is);
		} catch (IOException e) {
			return new ResponseEntity<>("Error sending the email", HttpStatus.INTERNAL_SERVER_ERROR);

		}
		
		
		return new ResponseEntity<>(html, HttpStatus.OK);

	}
	

	private String[] toArray(String address) {
		final String[] array = new String[1];
		array[0] = address;
		return array;
	}
}

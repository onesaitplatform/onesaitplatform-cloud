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
package com.minsait.onesait.platform.controlpanel.rest.management.mail;

import static com.minsait.onesait.platform.controlpanel.rest.management.mail.MailManagementUrl.OP_MAIL;

import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.SupportRequest;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.SupportRepository;
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
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;
	@Autowired
	private SupportRepository supportRepository;

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

	private String[] toArray(String address) {
		final String[] array = new String[1];
		array[0] = address;
		return array;
	}
}

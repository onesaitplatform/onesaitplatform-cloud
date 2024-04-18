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
package com.minsait.onesait.platform.controlpanel.controller.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.SupportRepository;
import com.minsait.onesait.platform.config.services.support.SupportService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/support")
@Slf4j
public class SupportController {
	
	private static final String FAIL = "{\"status\" : \"fail\"}";
	private static final String OK = "{\"status\" : \"ok\"}";

	@Value("${onesaitplatform.mailService.mailSupport:support@onesaitplatform.com}")
	private String supportEmail;
	
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private MailService mailService;
	@Autowired
	private UserService userService;
	@Autowired
	private SupportService supportService;
	@Autowired
	private SupportRepository supportRepository;
	
	@GetMapping(value = "/create", produces = "text/html")
	public String create(Model model) {
		List<String> rolesToSelect = new ArrayList<>();
		rolesToSelect.add("ROLE_DATASCIENTIST");
		rolesToSelect.add("ROLE_DATAVIEWER");
		rolesToSelect.add("ROLE_DEVELOPER");
		rolesToSelect.add("ROLE_USER");
		model.addAttribute("roleTypes", rolesToSelect);
		return "support/create";
	}
	
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@GetMapping(value = "/notifications", produces = "text/html")
	public String notifications(Model model) {
		model.addAttribute("notifications", supportRepository.findAll());
		return "support/notifications";
	}
	
	@PostMapping(value = "/send")
	public ResponseEntity<String> send(@RequestParam("text") String text, @RequestParam("rol") String rol, @RequestParam("type") String type) {
		User user = userService.getUserByIdentification(utils.getUserId());
		final String supportRequest;
		supportService.createSupportRequest(user, type, text, rol);
		if (!type.equals("ROLE_CHANGE")) {supportRequest = "User: "+user.getUserId()
														+ "\nEmail: "+user.getEmail()
														+ "\nRequest Type: "+type 
														+ "\nText:\n	"+text;}
		else {supportRequest = "User: "+user.getUserId()
							+ "\nEmail: "+user.getEmail()
							+ "\nRequest Type: "+type 
							+ "\nChange to: "+rol
							+ "\nText:\n	"+text;}
		try {
			mailService.sendMail(supportEmail, "Support Request", supportRequest);
		}
		catch (final RuntimeException e) {
			log.error("Error sending the support request: " + e.getMessage());
			return new ResponseEntity<>(FAIL,HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(OK, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@PostMapping(value = "/notifications/update")
	public ResponseEntity<String> update(@RequestParam("user") User user, @RequestParam("role") Role role) {
		try {
			supportService.changeRole(user, role);
			
			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error updating the role: " + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@PostMapping(value = "/notifications/updateStatus")
	public ResponseEntity<String> updateStatus(@RequestParam("supportRequestId") String supportRequestId) {
		try {
			supportService.updateStatus(supportRepository.findById(supportRequestId));
			
			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error updating the notification status: " + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/notifications/delete/{id}")
	public @ResponseBody String delete(Model model, @PathVariable("id") String id) {
		try {
			supportRepository.delete(id);}
		catch(final Exception e) {log.error("Error delating the support request: " + e);}
		return "redirect:/support/notifications";
	}
	
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	@PostMapping(value = "/notifications/sendEmail")
	public ResponseEntity<String> sendEmail(@RequestParam("supportRequestId") String supportRequestId, @RequestParam("message") String message) {
		
		User user = supportRepository.findById(supportRequestId).getUser();
		
		try {
			mailService.sendMail(user.getEmail(), "Support Request", message);
			log.info("Send email to: "+user.getEmail()+ "with the message" + message);
		}
		catch (final RuntimeException e) {
			log.error("Error sending the e-mail: " + e.getMessage());
			return new ResponseEntity<>(FAIL,HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(OK, HttpStatus.OK);
	}

}

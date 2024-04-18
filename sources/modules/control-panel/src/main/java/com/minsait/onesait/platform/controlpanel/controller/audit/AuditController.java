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
package com.minsait.onesait.platform.controlpanel.controller.audit;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.business.services.audit.AuditService;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/audit")
public class AuditController {

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;
	
	@Autowired
	private AuditService auditService;

	@GetMapping("show")
	public String show(Model model) {

		final List<OperationType> operations = Arrays.asList(OperationType.values());
		final List<Module> modulesnames = Arrays.asList(Module.values());
		final List<ResultOperationType> resultevent = Arrays.asList(ResultOperationType.values());
		final List<User> users = userService.getAllActiveUsers();
		model.addAttribute("operations", operations);
		model.addAttribute("modulesnames", modulesnames);
		model.addAttribute("resultevent", resultevent);
		model.addAttribute("userRole", utils.getRole());
		model.addAttribute("users", users);
		return "audit/show";
	}

	@PostMapping("executeQuery")
	public String query(Model model, @RequestParam String resultType, @RequestParam String modulesname, @RequestParam String operation,
			@RequestParam String offset, @RequestParam (required = false, name = "user") String user) {
		String userQuery;
		if (utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			userQuery = user;
		} else {
			userQuery = utils.getUserId();
		}
		try {
			final String queryResult = auditService.getUserAuditData(resultType, modulesname, operation, offset, userQuery);
			model.addAttribute("queryResult", queryResult);
		} catch (final Exception e) {
			log.error("Error getting audit of user {}", utils.getUserId());
		}
		return ("audit/show :: query");
	}

}

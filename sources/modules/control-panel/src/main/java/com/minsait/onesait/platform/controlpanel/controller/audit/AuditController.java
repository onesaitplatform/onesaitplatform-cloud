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

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/audit")
public class AuditController {

	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private OntologyService ontologyServie;
	@Autowired
	private AppWebUtils utils;
	
	@Autowired
	private UserService userService;

	@GetMapping("show")
	public String show(Model model) {

		final List<OperationType> operations = Arrays.asList(OperationType.values());
		final List<User> users = userService.getAllActiveUsers();
		model.addAttribute("operations", operations);
		model.addAttribute("userRole", utils.getRole());
		model.addAttribute("users", users);
		return "audit/show";
	}

	@PostMapping("executeQuery")
	public String query(Model model, @RequestParam String offset, @RequestParam String operation, String user) {

		String result = "main";
		String userQuery;
		if (utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			userQuery = user;
		} else {
			userQuery = utils.getUserId();
		}

		try {

			try {
				final String queryResult = getResultForQuery(userQuery, operation, offset);
				model.addAttribute("queryResult", queryResult);
			} catch (final Exception e) {
				log.error("Error getting audit of user {}", utils.getUserId());
			}

			result = "audit/show :: query";

		} catch (final Exception e) {
			model.addAttribute("queryResult",
					utils.getMessage("querytool.query.native.error", "Error malformed query"));
		}
		return result;
	}

	private String getResultForQuery(String user, String operation, String offset) {

		final String collection = ServiceUtils.getAuditCollectionName(user);

		String query = "select message, user, formatedTimeStamp, module, ontology, operationType, data from "
				+ collection;

		if (!operation.equalsIgnoreCase("all")) {
			query += " where operationType = \"" + operation + "\"";
		}

		query += " order by timeStamp desc limit " + Integer.parseInt(offset);

		return queryToolService.querySQLAsJson(utils.getUserId(), collection, query, 0);

	}
}

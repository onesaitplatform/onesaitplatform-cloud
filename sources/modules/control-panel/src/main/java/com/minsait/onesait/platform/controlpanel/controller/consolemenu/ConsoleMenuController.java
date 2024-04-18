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
package com.minsait.onesait.platform.controlpanel.controller.consolemenu;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.minsait.onesait.platform.config.model.ConsoleMenu;
import com.minsait.onesait.platform.config.repository.ConsoleMenuRepository;
import com.minsait.onesait.platform.config.services.menu.MenuServiceImpl;
import com.minsait.onesait.platform.controlpanel.controller.rollback.RollbackController;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/consolemenu")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
@Slf4j
public class ConsoleMenuController {

	@Autowired
	private ConsoleMenuRepository consoleMenuRepository;
	@Autowired
	private MenuServiceImpl menuService;
	@Autowired
	private RollbackController rollbackController;
	@Autowired
	private AppWebUtils utils;
	@Autowired 
	private HttpSession httpSession;

	private static final String CONSTANT_RN = "\r\n";
	private static final String CONSTANT_TYPE_STRING = "\"type\": \"string\",";
	private static final String CONSTANT_DEFAULT = "\"default\": \"\",";
	private static final String CONSTANT_PATTERN = "\"pattern\": \"^(.*)$\"";
	private static final String CONSTANT = "},";
	private static final String APP_ID = "appId";

	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String list(Model model) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		model.addAttribute("menus", consoleMenuRepository.findAll());

		return "consolemenu/list";
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(Model model, @PathVariable("id") String id) {

		model.addAttribute("option", "show");
		model.addAttribute("menu", consoleMenuRepository.findById(id).orElse(new ConsoleMenu()).getJson());
		model.addAttribute("role", consoleMenuRepository.findById(id).orElse(new ConsoleMenu()).getRoleType().getId());

		return "consolemenu/show";
	}

	@GetMapping(value = "/edit/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {

		model.addAttribute("option", "edit");
		model.addAttribute("menu", consoleMenuRepository.findById(id).orElse(new ConsoleMenu()).getJson());
		model.addAttribute("idCm", id);
		model.addAttribute("role", consoleMenuRepository.findById(id).orElse(new ConsoleMenu()).getRoleType().getId());

		return "consolemenu/show";
	}

	@Transactional
	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<ConsoleMenu> updateConsoleMenu(@RequestParam String menuId, @RequestParam String menuJson,
			HttpServletRequest request) throws IOException, ProcessingException {

		try {
			final JsonNode menuJsonNode = JsonLoader.fromString(menuJson);
			final JsonNode jsonSchema = JsonLoader.fromString("{" + CONSTANT_RN + "  \"definitions\": {}," + CONSTANT_RN
					+ "  \"type\": \"object\"," + CONSTANT_RN + "  \"title\": \"The Root Schema\"," + CONSTANT_RN
					+ "  \"required\": [" + CONSTANT_RN + "    \"menu\"," + CONSTANT_RN + "    \"rol\"," + CONSTANT_RN
					+ "    \"noSession\"," + CONSTANT_RN + "    \"navigation\"" + CONSTANT_RN + "  ]," + CONSTANT_RN
					+ "  \"properties\": {" + CONSTANT_RN + "    \"menu\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING
					+ CONSTANT_RN + "      \"title\": \"The Menu Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT
					+ CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN + CONSTANT + CONSTANT_RN + "    \"rol\": {"
					+ CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN + "      \"title\": \"The Rol Schema\","
					+ CONSTANT_RN + CONSTANT_DEFAULT + CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN + "    },"
					+ CONSTANT_RN + "    \"noSession\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "      \"title\": \"The Nosession Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT + CONSTANT_RN
					+ CONSTANT_PATTERN + CONSTANT_RN + "    }," + CONSTANT_RN + "    \"navigation\": {" + CONSTANT_RN
					+ "      \"type\": \"array\"," + CONSTANT_RN + "      \"title\": \"The Navigation Schema\","
					+ CONSTANT_RN + "      \"items\": {" + CONSTANT_RN + "        \"type\": \"object\"," + CONSTANT_RN
					+ "        \"title\": \"The Items Schema\"," + CONSTANT_RN + "        \"required\": [" + CONSTANT_RN
					+ "          \"title\"," + CONSTANT_RN + "          \"icon\"," + CONSTANT_RN + "          \"url\","
					+ CONSTANT_RN + "          \"submenu\"" + CONSTANT_RN + "        ]," + CONSTANT_RN
					+ "        \"properties\": {" + CONSTANT_RN + "          \"title\": {" + CONSTANT_RN
					+ "            \"type\": \"object\"," + CONSTANT_RN + "            \"title\": \"The Title Schema\","
					+ CONSTANT_RN + "            \"required\": [" + CONSTANT_RN + "              \"EN\"," + CONSTANT_RN
					+ "              \"ES\"" + CONSTANT_RN + "            ]," + CONSTANT_RN
					+ "            \"properties\": {" + CONSTANT_RN + "              \"EN\": {" + CONSTANT_RN
					+ CONSTANT_TYPE_STRING + CONSTANT_RN + "                \"title\": \"The En Schema\"," + CONSTANT_RN
					+ CONSTANT_DEFAULT + CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN + "              }," + CONSTANT_RN
					+ "              \"ES\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "                \"title\": \"The Es Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT + CONSTANT_RN
					+ CONSTANT_PATTERN + CONSTANT_RN + "              }" + CONSTANT_RN + "            }" + CONSTANT_RN
					+ CONSTANT + CONSTANT_RN + "          \"icon\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING
					+ CONSTANT_RN + "            \"title\": \"The Icon Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT
					+ CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN + "          }," + CONSTANT_RN
					+ "          \"url\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "            \"title\": \"The Url Schema\"," + CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN
					+ "          }," + CONSTANT_RN + "          \"submenu\": {" + CONSTANT_RN
					+ "            \"type\": \"array\"," + CONSTANT_RN
					+ "            \"title\": \"The Submenu Schema\"," + CONSTANT_RN + "            \"items\": {"
					+ CONSTANT_RN + "              \"type\": \"object\"," + CONSTANT_RN
					+ "              \"title\": \"The Items Schema\"," + CONSTANT_RN + "              \"required\": ["
					+ CONSTANT_RN + "                \"title\"," + CONSTANT_RN + "                \"icon\","
					+ CONSTANT_RN + "                \"url\"" + CONSTANT_RN + "              ]," + CONSTANT_RN
					+ "              \"properties\": {" + CONSTANT_RN + "                \"title\": {" + CONSTANT_RN
					+ "                  \"type\": \"object\"," + CONSTANT_RN
					+ "                  \"title\": \"The Title Schema\"," + CONSTANT_RN
					+ "                  \"required\": [" + CONSTANT_RN + "                    \"EN\"," + CONSTANT_RN
					+ "                    \"ES\"" + CONSTANT_RN + "                  ]," + CONSTANT_RN
					+ "                  \"properties\": {" + CONSTANT_RN + "                    \"EN\": {"
					+ CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "                      \"title\": \"The En Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT
					+ CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN + "                    }," + CONSTANT_RN
					+ "                    \"ES\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "                      \"title\": \"The Es Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT
					+ CONSTANT_RN + CONSTANT_PATTERN + CONSTANT_RN + "                    }" + CONSTANT_RN
					+ "                  }" + CONSTANT_RN + "                }," + CONSTANT_RN
					+ "                \"icon\": {" + CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "                  \"title\": \"The Icon Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT + CONSTANT_RN
					+ CONSTANT_PATTERN + CONSTANT_RN + "                }," + CONSTANT_RN + "                \"url\": {"
					+ CONSTANT_RN + CONSTANT_TYPE_STRING + CONSTANT_RN
					+ "                  \"title\": \"The Url Schema\"," + CONSTANT_RN + CONSTANT_DEFAULT + CONSTANT_RN
					+ CONSTANT_PATTERN + CONSTANT_RN + "                }" + CONSTANT_RN + "              }"
					+ CONSTANT_RN + "            }" + CONSTANT_RN + "          }" + CONSTANT_RN + "        }"
					+ CONSTANT_RN + "      }" + CONSTANT_RN + "    }" + CONSTANT_RN + "  }" + CONSTANT_RN + "}");
			final JsonSchemaFactory factoryJson = JsonSchemaFactory.byDefault();
			final JsonSchema schema = factoryJson.getJsonSchema(jsonSchema);
			final ProcessingReport report = schema.validate(menuJsonNode);
			if (report != null && !report.isSuccess()) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		} catch (final RuntimeException e) {
			log.error("Error validating Json structure: ", e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			menuService.updateMenu(menuId, menuJson);
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById(menuId);
			if (!opt.isPresent())
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			final ConsoleMenu menu = opt.get();

			if (menu.getRoleType().getId().equals(utils.getRole())) {
				utils.setSessionAttribute(request, "menu", menu.getJson());
			}

			return new ResponseEntity<>(menu, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			log.error("Error updating console menu: ", e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@Transactional
	@PostMapping(value = "/rollback/")
	public String rollbackMenu(@RequestParam String menuId, HttpServletRequest request) {

		final Optional<ConsoleMenu> opt = consoleMenuRepository.findById(menuId);
		if (!opt.isPresent())
			return "error/404";
		final ConsoleMenu menu = opt.get();

		final ConsoleMenu originalMenu = (ConsoleMenu) rollbackController.getRollback(menuId);
		final String originalMenuJson = originalMenu.getJson();

		menu.setJson(originalMenuJson);
		consoleMenuRepository.save(menu);

		if (menu.getRoleType().getId().equals(utils.getRole())) {
			utils.setSessionAttribute(request, "menu", menu.getJson());
		}

		return "consolemenu/list";
	}

}

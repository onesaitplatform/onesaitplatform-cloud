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
package com.minsait.onesait.platform.controlpanel.controller.notebook;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.NotebookServiceException;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/notebooks")
@Controller
@Slf4j
public class NotebookController {

	@Autowired
	private NotebookService notebookService;

	@Autowired
	private NotebookUserAccessRepository notebookUserAccessRepository;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	ServletContext context;

	@Autowired
	private UserRepository userRepository;

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/createNotebook")
	@ResponseBody
	public ResponseEntity<String> createNotebook(@RequestParam("name") String name) {
		try {
			return new ResponseEntity<>(notebookService.createEmptyNotebook(name, utils.getUserId()).getIdzep(),HttpStatus.OK);
		} 
		catch (final NotebookServiceException e) {
			switch(e.getError()) {
				case DUPLICATE_NOTEBOOK_NAME:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
				default:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch (final Exception e) {
			log.error("Cannot create notebook: ", e);
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/cloneNotebook")
	@ResponseBody
	public ResponseEntity<String> cloneNotebook(@RequestParam("name") String name, @RequestParam("idzep") String idzep) {
		try {
			return new ResponseEntity<>(notebookService.cloneNotebook(name, idzep, utils.getUserId()).getIdzep(),HttpStatus.OK);
		}
		catch (final NotebookServiceException e) {
			switch(e.getError()) {
				case DUPLICATE_NOTEBOOK_NAME:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
				case PERMISSION_DENIED:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
				default:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch (final Exception e) {
			log.error("Cannot clone notebook: ", e);
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/renameNotebook")
	@ResponseBody
	public ResponseEntity<String> renameNotebook(@RequestParam("name") String name, @RequestParam("idzep") String idzep) {
		try {
			notebookService.renameNotebook(name, idzep, utils.getUserId());
			return new ResponseEntity<>(HttpStatus.OK);
		} 
		catch (final NotebookServiceException e) {
			switch(e.getError()) {
				case DUPLICATE_NOTEBOOK_NAME:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
				case PERMISSION_DENIED:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
				default:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch (final Exception e) {
			log.error("Cannot rename notebook: ", e);
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/importNotebook")
	@ResponseBody
	public ResponseEntity<String> importNotebook(@RequestParam("name") String name, @RequestParam("data") String data) {
		try {
			return new ResponseEntity<>(notebookService.importNotebook(name, data, utils.getUserId()).getIdzep(),HttpStatus.OK);
		} 
		catch (final NotebookServiceException e) {
			switch(e.getError()) {
				case DUPLICATE_NOTEBOOK_NAME:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
				default:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch (final Exception e) {
			log.error("Cannot import notebook: ", e);
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/importNotebookFromJupyter")
	@ResponseBody
	public ResponseEntity<String> importNotebookFromJupyter(@RequestParam("name") String name, @RequestParam("data") String data) {
		try {
			return new ResponseEntity<>(notebookService.importNotebookFromJupyter(name, data, utils.getUserId()).getIdzep(),HttpStatus.OK);
		} 
		catch (final NotebookServiceException e) {
			switch(e.getError()) {
				case DUPLICATE_NOTEBOOK_NAME:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.CONFLICT);
				case INVALID_FORMAT_NOTEBOOK:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
				default:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch (final Exception e) {
			log.error("Cannot import notebook: ", e);
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@GetMapping(value = "/exportNotebook/{id}", produces = "text/html")
	@ResponseBody
	public ResponseEntity<byte[]> exportNotebook(@PathVariable("id") String id, Model uiModel) {
		JSONObject nt = notebookService.exportNotebook(id, utils.getUserId());
		final HttpHeaders headers = notebookService.exportHeaders(nt.get("name").toString());
		return new ResponseEntity<>(nt.toString().getBytes(Charset.forName("UTF-8")), headers,
				HttpStatus.OK);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@DeleteMapping(value = "/{id}", produces = "text/html")
	public String removeNotebook(@PathVariable("id") String id, Model uiModel, RedirectAttributes ra) {
		try {
			notebookService.removeNotebook(id, utils.getUserId());
			uiModel.asMap().clear();
		} catch (final RuntimeException e) {
			utils.addRedirectException(e, ra);
		}

		return "redirect:/notebooks/list";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel) {

		uiModel.addAttribute("lnt", notebookService.getNotebooks(utils.getUserId()));
		uiModel.addAttribute("user", utils.getUserId());
		uiModel.addAttribute("userRole", utils.getRole());

		return "notebooks/list";
	}

	@PostMapping("/public")
	@ResponseBody
	public String changePublic(@RequestParam("id") Notebook notebookId) {
		if (notebookService.hasUserPermissionInNotebook(notebookId, utils.getUserId())) {
			notebookService.changePublic(notebookId);
			return "ok";
		} else {
			return "ko";
		}
	}

	@GetMapping(value = "/share/{id}", produces = "text/html")
	public String share(Model model, @PathVariable("id") Notebook notebook) {
		String userId = utils.getUserId();
		User user = userRepository.findByUserId(userId);
		if (notebook.getUser().toString().equals(userId) || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			final List<User> users = userService.getDifferentUsersWithRole(user, Role.Type.ROLE_DATASCIENTIST);
			users.remove(notebook.getUser());

			model.addAttribute("users", users);
			model.addAttribute("int", notebookUserAccessRepository.findByNotebook(notebook));
			model.addAttribute("notebookid", notebook.getId());

			return "notebooks/share";
		} else {
			return "error/403";
		}
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<NotebookUserAccess> createAuthorization(@RequestParam String accesstype,
			@RequestParam String notebook, @RequestParam String user) {
		ResponseEntity<NotebookUserAccess> response;
		try {
			NotebookUserAccess notebookUserAccess = notebookService.createUserAccess(notebook, user, accesstype);
			response = new ResponseEntity<>(notebookUserAccess, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			response =  new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return response;

	}

	@PostMapping(value = "/auth/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		try {
			notebookService.deleteUserAccess(id);
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@GetMapping(value = "/app/api/security/ticket")
	@ResponseBody
	public String loginAppOrGetWSToken() {
		if (utils.isAdministrator()) {
			return notebookService.loginOrGetWSTokenAdmin();
		} else {
			return notebookService.loginOrGetWSToken();
		}

	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = { "/app/api/interpreter/**", "/app/api/configurations/**", "/app/api/credential/**",
			"/app/api/version" })
	@ResponseBody
	public ResponseEntity<String> adminAppRest(Model uiModel, HttpServletRequest request)
			throws URISyntaxException, IOException {
		return notebookService.sendHttp(request, HttpMethod.GET, "");
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = { "/app/api/interpreter/**",
			"/app/api/helium/**" }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> adminAppRestPutJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body)
			throws URISyntaxException, IOException {
		return notebookService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = { "/app/api/interpreter/**",
			"/app/api/helium/**" }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> adminAppRestPostJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body)
			throws URISyntaxException, IOException {
		return notebookService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}

	@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping(value = { "/app/api/interpreter/**",
			"/app/api/helium/**" }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> adminAppRestDeleteJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body)
			throws URISyntaxException, IOException {
		return notebookService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@GetMapping(value = { "/app/api/notebook/**", "/app/api/helium/**" })
	@ResponseBody
	public ResponseEntity<String> analyAppRest(Model uiModel, HttpServletRequest request)
			throws URISyntaxException, IOException {
		return notebookService.sendHttp(request, HttpMethod.GET, "");
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@GetMapping(value = "/app/")
	public String indexAppRedirectNoPath(Model uiModel, HttpServletRequest request) {
		return "notebooks/index";
	}
	
	@GetMapping(value = "/nameByIdZep/{id}", produces = "text/html")
	@ResponseBody
	public ResponseEntity<String> nameByIdZep(Model model, @PathVariable("id") String id) {
		try {
			return new ResponseEntity<>(notebookService.notebookNameByIdZep(id, utils.getUserId()),HttpStatus.OK);
		}
		catch(NotebookServiceException e) {
			switch(e.getError()) {
				case PERMISSION_DENIED:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
				default:
					return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST')")
	@PutMapping(value = { "/app/api/interpreter/setting/restart/**"
			 }, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> analyAppRestPutJSON(Model uiModel, HttpServletRequest request,
			@RequestBody(required = false) String body)
			throws URISyntaxException, IOException {
		return notebookService.sendHttp(request, HttpMethod.valueOf(request.getMethod()), body);
	}
	
	@GetMapping(value = "/show/{id}")
	public String showNotebook(Model Model, @PathVariable("id") String id) {
		Notebook notebook = notebookService.getNotebook(id);
		if (notebook ==  null) {
			return "error/403";
		}
		String idZep = notebook.getIdzep();
		return "redirect:/notebooks/app/#/notebook/"+idZep;
	}

}
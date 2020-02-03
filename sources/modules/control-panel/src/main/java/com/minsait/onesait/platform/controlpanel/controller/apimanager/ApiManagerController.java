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
package com.minsait.onesait.platform.controlpanel.controller.apimanager;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
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

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
/* TODELETECE
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeApi;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeException;  */
import com.minsait.onesait.platform.controlpanel.helper.apimanager.ApiManagerHelper;
import com.minsait.onesait.platform.controlpanel.multipart.ApiMultipart;
/* TODELETECE
import com.minsait.onesait.platform.controlpanel.services.gravitee.GraviteeService;
 */
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/apimanager")
@Slf4j
public class ApiManagerController {

	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private ApiManagerHelper apiManagerHelper;
	@Autowired
	private AppWebUtils utils;
	/* TODELETECE
	@Autowired(required = false)
	private GraviteeService graviteeService; */
	@Autowired
	private IntegrationResourcesService resourcesService;
	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";
	private static final String STATUS_OK = "{\"status\" : \"ok\"}";
	private static final String GRAVITEE_MANAGEMENT = "/management";
	private static final String GRAVITEE_APIS = "/apis";

	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String createForm(Model model) {

		apiManagerHelper.populateApiManagerCreateForm(model);

		return "apimanager/create";
	}

	@GetMapping(value = "/update/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String updateForm(@PathVariable("id") String id, Model model) {
		if (!apiManagerService.hasUserEditAccess(id, utils.getUserId()))
			return ERROR_403;
		apiManagerHelper.populateApiManagerUpdateForm(model, id);

		return "apimanager/create";
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(@PathVariable("id") String id, Model model) {
		if (!apiManagerService.hasUserAccess(id, utils.getUserId()))
			return ERROR_403;
		apiManagerHelper.populateApiManagerShowForm(model, id);

		return "apimanager/show";
	}

	/* TODELETECE
	@GetMapping(value = "/gravitee/{id}", produces = "text/html")
	public String graviteeStats(@PathVariable("id") String id, Model model, @RequestParam("iframe") String iframe) {
		if (!apiManagerService.hasUserAccess(id, utils.getUserId()))
			return ERROR_403;
		final Api api = apiManagerService.getById(id);
		if (null == graviteeService || null == api || StringUtils.isEmpty(api.getGraviteeId()))
			return ERROR_404;
		final String url = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI).concat(GRAVITEE_MANAGEMENT)
				.concat(GRAVITEE_APIS).concat("/" + api.getGraviteeId()).concat("/" + iframe);
		apiManagerHelper.populateApiManagerShowForm(model, id);
		model.addAttribute("api", api);
		model.addAttribute("url", url);
		return "apimanager/gravitee";
	} */

	
	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER','ROLE_USER')")
	public String list(Model model, @RequestParam(required = false) String apiId,
			@RequestParam(required = false) String state, @RequestParam(required = false) String user) {
		try {
			apiManagerHelper.populateApiManagerListForm(model);

			model.addAttribute("apis", apiManagerService.loadAPISByFilter(apiId, state, user, utils.getUserId()));
			/* TODELETECEif (graviteeService != null)
				addGraviteeUrls(model);*/

		} catch (final Exception e) {
			log.error("Error at /controlpanel/apimanager/list {}", e);
		}

		return "apimanager/list";
	}

	@PostMapping(value = "/create")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String create(ApiMultipart api, BindingResult bindingResult, HttpServletRequest request,
			@RequestParam(required = false) String postProcessFx,
			@RequestParam(required = false, defaultValue = "false") Boolean publish2gravitee,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some user properties missing");
			utils.addRedirectMessage("api.create.error", redirect);
			return "redirect:/apimanager/create";
		}

		try {
			final String operationsObject = request.getParameter("operationsObject");
			final String authenticationObject = request.getParameter("authenticationObject");

			final String apiId = apiManagerService.createApi(apiManagerHelper.apiMultipartMap(api), operationsObject,
					authenticationObject);
			/*  TODELETECE
			if (!StringUtils.isEmpty(postProcessFx))
				apiManagerService.updateApiPostProcess(apiId, postProcessFx);

			if (graviteeService != null && publish2gravitee) {
				publish2Gravitee(apiId);
			} */

			return "redirect:/apimanager/show/" + utils.encodeUrlPathSegment(apiId, request);
		} catch (final Exception e) {
			log.error("Could not create API : {}", e);
			utils.addRedirectMessage("api.create.error", redirect);
			return "redirect:/apimanager/create";
		}
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String update(@PathVariable("id") String id, ApiMultipart api, BindingResult bindingResult,
			@RequestParam(required = false) String operationsObject,
			@RequestParam(required = false) String authenticationObject,
			@RequestParam(required = false) String deprecateApis, @RequestParam(required = false) String postProcessFx,
			@RequestParam(required = false, defaultValue = "false") Boolean publish2gravitee,
			RedirectAttributes redirect) {
		if (!apiManagerService.hasUserEditAccess(id, utils.getUserId())
				|| !apiManagerService.isApiStateValidForEdit(id))
			return ERROR_403;
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("api.update.error", redirect);
			return "redirect:/apimanager/update";
		}

		try {

			apiManagerService.updateApi(apiManagerHelper.apiMultipartMap(api), deprecateApis, operationsObject,
					authenticationObject);
			if (!StringUtils.isEmpty(postProcessFx))
				apiManagerService.updateApiPostProcess(api.getId(), postProcessFx);

			/*  TODELETECE
			if (graviteeService != null && publish2gravitee) {
				publish2Gravitee(id);
			} */

			return "redirect:/apimanager/show/" + api.getId();
		} catch (final Exception e) {
			log.error("Could not update API: {}", e);
			utils.addRedirectMessage("api.update.error", redirect);
			return "redirect:/apimanager/update";
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@Transactional
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			if (!apiManagerService.hasUserEditAccess(id, utils.getUserId()))
				return ERROR_403;
			final Api api = apiManagerService.getById(id);
			if (null != api) {
				apiManagerService.removeAPI(id);
				/*  TODELETE
				if (!StringUtils.isEmpty(api.getGraviteeId()) && graviteeService != null)
					graviteeService.deleteApi(api.getGraviteeId()); */
			}

		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);

		} catch (final Exception e) {
			log.error("Exception reached " + e.getMessage(), e);
			utils.addRedirectMessage("apimanager.delete.error", redirect);
			return "/controlpanel/apimanager/list";
		}

		return "/controlpanel/apimanager/list";
	}

	// AUTHORIZATIONS//
	@GetMapping(value = "/authorize/list", produces = "text/html")
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public String index(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size, Model model) {
		apiManagerHelper.populateAutorizationForm(model);
		return "apimanager/authorize";
	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<UserApiDTO> createAuthorization(@RequestParam String api, @RequestParam String user) {
		try {
			if (!apiManagerService.hasUserEditAccess(api, utils.getUserId()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			final UserApi userApi = apiManagerService.updateAuthorization(api, user);
			final UserApiDTO userApiDTO = new UserApiDTO(userApi);

			return new ResponseEntity<>(userApiDTO, HttpStatus.CREATED);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {
		try {
			final UserApi userApi = apiManagerService.getUserApiAccessById(id);
			if (userApi == null)
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			if (!apiManagerService.hasUserEditAccess(userApi.getApi().getId(), utils.getUserId()))
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			apiManagerService.removeAuthorizationById(id);
			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/token/list", produces = "text/html")
	public String token(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size, Model model, HttpServletRequest request) {
		apiManagerHelper.populateUserTokenForm(model);
		return "apimanager/token";
	}

	@GetMapping(value = "/invoke/{id}", produces = "text/html")
	public String invoker(Model model, @PathVariable("id") String apiId) {

		apiManagerHelper.populateApiManagerInvokeForm(model, apiId);

		return "apimanager/invoke";
	}

	@PostMapping(value = "numVersion")
	public @ResponseBody Integer numVersion(@RequestBody String numversionData) {
		return (apiManagerService.calculateNumVersion(numversionData));
	}

	@GetMapping(value = "/{id}/getImage")
	public void showImg(@PathVariable("id") String id, HttpServletResponse response) {
		final byte[] buffer = apiManagerService.getImgBytes(id);
		if (buffer.length > 0) {
			try (OutputStream output = response.getOutputStream();) {
				response.setContentLength(buffer.length);
				output.write(buffer);
			} catch (final Exception e) {
				log.error("showImg error: " + e.getMessage());
			}
		}
	}

	@GetMapping(value = "/updateState/{id}/{state}")
	public String updateState(@PathVariable("id") String id, @PathVariable("state") String state, Model uiModel)
			throws GenericOPException {
		if (!apiManagerService.hasUserEditAccess(id, utils.getUserId()))
			return ERROR_403;
		apiManagerService.updateState(id, state);
		final Api api = apiManagerService.getById(id);
		/* TODELETECE 
		if (!StringUtils.isEmpty(api.getGraviteeId())) {
			switch (api.getState()) {
			case PUBLISHED:
			case DEPRECATED:
				graviteeService.changeLifeCycleState(api.getGraviteeId(), api.getState());
				break;
			case DELETED:
				graviteeService.changeLifeCycleState(api.getGraviteeId(), api.getState());
				break;
			default:
				break;
			}
		} */
		return "redirect:/apimanager/list";
	}

	@PostMapping(value = "/generateToken")
	public @ResponseBody ResponseEntity<String> generateToken() {
		try {
			apiManagerService.generateToken(utils.getUserId());
			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/removeToken")
	public @ResponseBody ResponseEntity<String> removeToken(@RequestBody String token) {
		try {
			apiManagerService.removeToken(utils.getUserId(), token);
			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/* TODELETECE
	private void addGraviteeUrls(Model model) {

		final String UI = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI);
		model.addAttribute("graviteeUI", UI);

	} */

	/* TODELETECE
	private void publish2Gravitee(String apiId) throws GenericOPException {
		final Api apiDb = apiManagerService.getById(apiId);
		try {
			final GraviteeApi graviteeApi = graviteeService.processApi(apiDb);
			apiDb.setGraviteeId(graviteeApi.getApiId());
			apiManagerService.updateApi(apiDb);
		} catch (final GraviteeException e) {
			log.error("Could not publish API to Gravitee {}", e.getMessage());
		}
	} */
}

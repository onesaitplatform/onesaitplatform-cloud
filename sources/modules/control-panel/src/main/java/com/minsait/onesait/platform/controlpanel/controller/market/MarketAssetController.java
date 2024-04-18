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
package com.minsait.onesait.platform.controlpanel.controller.market;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetState;
import com.minsait.onesait.platform.config.services.market.MarketAssetService;
import com.minsait.onesait.platform.controlpanel.helper.market.MarketAssetHelper;
import com.minsait.onesait.platform.controlpanel.multipart.MarketAssetMultipart;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/marketasset")
@Slf4j
public class MarketAssetController {

	@Autowired
	MarketAssetService marketAssetService;
	@Autowired
	MarketAssetHelper marketAssetHelper;
	@Autowired
	private AppWebUtils utils;

	private static final String MARKETASSETS = "marketAssets";
	private static final String REDIRECT = "redirect:/";
	private static final String MARKETASSET_LIST = "marketasset/list";
	private static final String MARKETASSET_CREATE = "marketasset/create";
	private static final String MARKETASSET_UPDATE = "marketasset/update";
	private static final String MARKETASSET_SHOW =  "marketasset/show";
	
	private static final String MARKETASSET_FRAGMENTS =  "marketasset/marketassetfragments :: ";
	private static final String CANNOT_UPDATE_ASSET = "Cannot update asset that does not exist";
	private static final String API_UPDATE_ERROR = "api.update.error";

	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	public String createForm(Model model) {

		marketAssetHelper.populateMarketAssetCreateForm(model);

		return MARKETASSET_CREATE;
	}

	@GetMapping(value = "/update/{id}")
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	public String updateForm(@PathVariable("id") String id, Model model) {

		try {
			marketAssetHelper.populateMarketAssetUpdateForm(model, id);
		} catch (final Exception e) {
			marketAssetHelper.populateMarketAssetListForm(model);
			model.addAttribute(MARKETASSETS, marketAssetHelper
					.toMarketAssetBean(marketAssetService.loadMarketAssetByFilter("", utils.getUserId())));
			return MARKETASSET_LIST;
		}

		return MARKETASSET_CREATE;
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(@PathVariable("id") String id, Model model) {

		try {
			marketAssetHelper.populateMarketAssetShowForm(model, id);
		} catch (final Exception e) {
			marketAssetHelper.populateMarketAssetListForm(model);
			model.addAttribute(MARKETASSETS, marketAssetHelper
					.toMarketAssetBean(marketAssetService.loadMarketAssetByFilter("", utils.getUserId())));
			return MARKETASSET_LIST;
		}
		return MARKETASSET_SHOW;
	}

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String marketassetId) {

		marketAssetHelper.populateMarketAssetListForm(model);
		model.addAttribute(MARKETASSETS, marketAssetHelper
				.toMarketAssetBean(marketAssetService.loadMarketAssetByFilter(marketassetId, utils.getUserId())));

		return MARKETASSET_LIST;
	}

	@PostMapping(value = "/create")
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	public String create(MarketAssetMultipart marketAssetMultipart, BindingResult bindingResult,
			MultipartHttpServletRequest request, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some user properties missing");
			utils.addRedirectMessage("resource.create.error", redirect);
			return REDIRECT + MARKETASSET_CREATE;
		}

		try {

			final String apiId = marketAssetService
					.createMarketAsset(marketAssetHelper.marketAssetMultipartMap(marketAssetMultipart));

			return REDIRECT + MARKETASSET_SHOW + "/" + apiId;
		} catch (final Exception e) {
			log.error("Error creating asset", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT + MARKETASSET_CREATE;
		}
	}

	@PostMapping(value = "/update/{id}")
	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	public String update(@PathVariable("id") String id, MarketAssetMultipart marketAssetMultipart,
			MultipartHttpServletRequest request, BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage(API_UPDATE_ERROR, redirect);
			return REDIRECT + MARKETASSET_UPDATE;
		}

		try {
			marketAssetService.updateMarketAsset(id, marketAssetHelper.marketAssetMultipartMap(marketAssetMultipart),
					utils.getUserId());

			return REDIRECT + MARKETASSET_SHOW + "/" + id;
		} catch (final Exception e) {
			log.error("Error updating asset", e);
			utils.addRedirectException(e, redirect);
			return REDIRECT + MARKETASSET_UPDATE;
		}
	}

	@PreAuthorize("!@securityService.hasAnyRole('ROLE_USER')")
	@GetMapping(value = "/delete/{id}", produces = "text/html")
	public String delete(Model model, @PathVariable("id") String id) {

		marketAssetService.delete(id, utils.getUserId());

		return REDIRECT + MARKETASSET_LIST;
	}

	@GetMapping(value = "/rateit/{id}/{rate}", produces = "text/html")
	public String rateit(Model model, @PathVariable("id") String id, @PathVariable("rate") String rate) {

		marketAssetService.rate(id, rate, utils.getUserId());

		return REDIRECT + MARKETASSET_SHOW + "/" + id;
	}

	@PostMapping(value = "/comment")
	public String comment(HttpServletRequest request, RedirectAttributes redirect) {
		final String id = request.getParameter("marketAssetId");
		final String title = request.getParameter("commentTitle");
		final String comment = request.getParameter("comment");

		try {
			marketAssetService.createComment(id, utils.getUserId(), title, comment);

			return REDIRECT + MARKETASSET_SHOW + "/" + id;
		} catch (final Exception e) {
			log.debug(CANNOT_UPDATE_ASSET);
			utils.addRedirectMessage(API_UPDATE_ERROR, redirect);
			return REDIRECT + MARKETASSET_SHOW + "/" + id;
		}
	}

	@GetMapping(value = "/deletecomment/{marketassetid}/{id}", produces = "text/html")
	public String deletecomment(Model model, @PathVariable("marketassetid") String marketassetid,
			@PathVariable("id") String id) {

		marketAssetService.deleteComment(id);

		return REDIRECT + MARKETASSET_SHOW + "/" + marketassetid;
	}

	@GetMapping(value = "/fragment/{type}")
	public String fragment(Model model, @PathVariable("type") String type) {

		marketAssetHelper.populateMarketAssetFragment(model, type);

		return MARKETASSET_FRAGMENTS + type + "MarketAssetFragment";
	}

	@GetMapping(value = "/apiversions/{identification}")
	public String apiversions(Model model, @PathVariable("identification") String identification) {

		marketAssetHelper.populateApiVersions(model, identification);

		return MARKETASSET_FRAGMENTS + "#versions";
	}

	@GetMapping(value = "/apidescription")
	public @ResponseBody String apidescription(@RequestBody String apiData) {
		return (marketAssetHelper.getApiDescription(apiData));
	}

	@GetMapping(value = "/urlwebproject")
	public @ResponseBody String urlwebproject(@RequestBody String webProjectData) {
		return (marketAssetHelper.getUrlWebProjectData(webProjectData));
	}

	@GetMapping(value = "/validateId")
	public @ResponseBody String validateId(@RequestBody String marketAssetId) {
		return (marketAssetHelper.validateId(marketAssetId));
	}

	@GetMapping(value = "/{id}/getImage")
	public void showImg(@PathVariable("id") String id, HttpServletResponse response) {
		final byte[] buffer = marketAssetService.getImgBytes(id);
		if (buffer.length > 0) {
			OutputStream output = null;
			try {
				output = response.getOutputStream();
				response.setContentLength(buffer.length);
				output.write(buffer);
			} catch (final Exception e) {
				log.error("Error getting image",e);
			} finally {
				try {
					if (output!=null) {
						output.close();
					}
				} catch (final IOException e) {
					log.error("Error getting image",e);
				}
			}
		}
	}

	@PostMapping(value = "/{id}/downloadContent")
	public ResponseEntity<ByteArrayResource> download(@PathVariable("id") String id) {
		final MarketAsset asset = marketAssetService.getMarketAssetById(id);
		if ((asset.getState().equals(MarketAssetState.PENDING)) && (!utils.getUserId().equals(asset.getUser().getUserId()) && !utils.isAdministrator()))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		final ByteArrayResource resource = new ByteArrayResource(asset.getContent());
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(asset.getContentId()))
				.contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/updateState/{id}/{state}")
	public @ResponseBody String updateState(@PathVariable("id") String id, @PathVariable("state") String state,
			@RequestBody String reasonData) {
		return (marketAssetService.updateState(id, state, reasonData));
	}

}

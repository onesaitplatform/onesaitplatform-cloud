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
package com.minsait.onesait.platform.controlpanel.controller.cache;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.business.services.cache.CacheBusinessService;
import com.minsait.onesait.platform.business.services.cache.CacheBusinessServiceException;
import com.minsait.onesait.platform.config.model.Cache;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.cache.CacheDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/caches")
@Slf4j
public class CacheController {
	@Autowired
	private CacheBusinessService cacheBS;

	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	private static final String CACHE_CREATE = "caches/create";
	private static final String REDIRECT_CACHE_CREATE = "redirect:/caches/create";
	private static final String REDIRECT_CACHE_LIST = "redirect:/caches/list";

	private static final String CACHE = "cache";
	private static final String CANNOT_UPDATE_CACHE = "Cannot update cache";

	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String list(Model model, HttpServletRequest request, String identification) {

		model.addAttribute("caches", cacheBS.getByIdentificationLikeOrderByIdentification(identification));
		return "caches/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return cacheBS.getCachesIdentifications(utils.getUserId());
	}

	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String create(Model model, @Valid Cache cache, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			model.addAttribute(CACHE, new Cache());
		}

		model.addAttribute("cacheTypes", Cache.Type.values());
		model.addAttribute("cacheMaxSizePolicies", Cache.MaxSizePolicy.values());
		model.addAttribute("cacheEvictionPolicies", Cache.EvictionPolicy.values());
		return CACHE_CREATE;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/create")
	public String createCache(Model model, @Valid CacheDTO cacheDTO, BindingResult bindingResult,
			RedirectAttributes redirect, String identification) throws JsonProcessingException {
		if (bindingResult.hasErrors()) {
			log.debug("Some cache properties missing");
			utils.addRedirectMessage("cache.validation.error", redirect);
			return REDIRECT_CACHE_CREATE;
		}

		if (!cacheBS.cacheExists(identification)) {
			try {
				log.debug("Recieved request to create a new cached map {}", identification);
				final User user = userService.getUserByIdentification(utils.getUserId());

				final Cache cache = new Cache();
				cache.setUser(user);
				cache.setIdentification(identification);
				cache.setType(cacheDTO.getType());
				cache.setEvictionPolicy(cacheDTO.getEvictionPolicy());
				cache.setMaxSizePolicy(cacheDTO.getMaxSizePolicy());
				cache.setSize(cacheDTO.getSize());

				cacheBS.<String, String>createCache(cache);
			} catch (final CacheBusinessServiceException e) {
				log.error("Cannot create cache because of: " + e.getMessage());
				utils.addRedirectMessage("cache.create.error", redirect);
				return REDIRECT_CACHE_CREATE;
			}
		} else {
			log.error("Cannot create cache because of: " + "Cache with identification: " + identification
					+ " already exists");
			utils.addRedirectMessage("cache.validation.exists", redirect);
			return REDIRECT_CACHE_CREATE;
		}

		return REDIRECT_CACHE_LIST;
	}

	@GetMapping(value = "/update/{identification}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String update(Model model, @PathVariable("identification") String identification) {
		final Cache cache = cacheBS.getCacheWithId(identification);
		model.addAttribute("cacheTypes", Cache.Type.values());
		model.addAttribute("cacheMaxSizePolicies", Cache.MaxSizePolicy.values());
		model.addAttribute("cacheEvictionPolicies", Cache.EvictionPolicy.values());

		if (cache != null) {
			model.addAttribute(CACHE, cache);
			return CACHE_CREATE;
		} else {
			return CACHE_CREATE;
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PutMapping(value = "/update/{identification}", produces = "text/html")
	public String updateCache(Model model, @PathVariable("identification") String identification, @Valid Cache cache,
			BindingResult bindingResult, RedirectAttributes redirect) throws JsonProcessingException {
		if (bindingResult.hasErrors()) {
			log.debug("Some cache properties missing");
			utils.addRedirectMessage("cache.validation.error", redirect);
			return "redirect:/caches/update/" + identification;
		}
		cacheBS.updateCache(identification, cache);

		//        if (cacheBS.cacheExists(identification)) {
		//               cacheBS.updateCache(identification);
		//        } else {
		//            log.error("Cannot update cache");
		//            utils.addRedirectMessage("cache.update.error", redirect);
		//            return "redirect:/caches/update/" + identification;
		//        }

		return REDIRECT_CACHE_LIST;
	}

	@GetMapping(value = "/delete/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String deleteCache(Model model, @PathVariable("identification") String identification,
			RedirectAttributes redirect) {

		final Cache cache = cacheBS.getCacheWithId(identification);
		if (cache != null) {
			cacheBS.deleteCacheById(identification);
		} else {
			return REDIRECT_CACHE_LIST;
		}
		return REDIRECT_CACHE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping(value = "/maps/{identification}/", produces = "text/html")
	public String deleteMap(Model model, @PathVariable("identification") String identification,
			RedirectAttributes redirect) {
		log.debug("Recieved request to delete a cached map {}", identification);

		final User user = userService.getUserByIdentification(utils.getUserId());

		try {
			cacheBS.deleteMap(identification, user);
		} catch (final CacheBusinessServiceException e) {
			log.error(CANNOT_UPDATE_CACHE, e);
			utils.addRedirectMessage("cache.delete.error", redirect);
			return REDIRECT_CACHE_LIST;
		}

		return REDIRECT_CACHE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/maps/{identification}/put/{key}/", produces = "text/html")
	public String putIntoMap(Model model, @PathVariable("identification") String identification, String key,
			String value, RedirectAttributes redirect) {

		log.debug("Recieved request to put data into cached map {} with key {} and value {}", identification, key,
				value);

		final User user = userService.getUser(utils.getUserId());

		try {
			cacheBS.putIntoMap(identification, key, value, user);
		} catch (final CacheBusinessServiceException e) {
			log.error(CANNOT_UPDATE_CACHE, e);
			utils.addRedirectMessage("cache.update.error", redirect);
			return REDIRECT_CACHE_LIST;
		}

		return REDIRECT_CACHE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/show/{identification}", produces = "text/html")
	public String show(Model model, @PathVariable("identification") String id) {
		final Cache cache = cacheBS.getCacheWithId(id);
		if (cache != null) {
			model.addAttribute(CACHE, cache);
			return "caches/show";
		} else {
			return "error/404";
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/maps/{identification}/putMany/", produces = "text/html")
	public String putManyIntoMap(Model model, @PathVariable("identification") String identification,
			Map<String, String> values, RedirectAttributes redirect) throws IOException {

		log.debug("Recieved request to put several data into cached map {}", identification);

		final User user = userService.getUser(utils.getUserId());

		try {
			cacheBS.putAllIntoMap(identification, values, user);
		} catch (final CacheBusinessServiceException e) {
			log.error(CANNOT_UPDATE_CACHE, e);
			utils.addRedirectMessage("cache.update.error", redirect);
			return REDIRECT_CACHE_LIST;
		}

		return REDIRECT_CACHE_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/maps/{identification}/get/{key}/", produces = "text/html")
	public ResponseEntity<String> getFromMap(
			@Parameter(description = "Identification of the map to get data", required = true) @PathVariable("identification") String identification,
			@Parameter(description = "Key to search the data", required = true) @PathVariable("key") String key) {
		log.debug("Recieved request to get data from cached map {} with key {}", identification, key);

		final User user = userService.getUser(utils.getUserId());

		try {
			final String value = cacheBS.getFromMap(identification, user, key);
			return ResponseEntity.ok().body(value);
		} catch (final CacheBusinessServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/maps/{identification}/getAll/", produces = "text/html")
	public ResponseEntity<Map<String, String>> getAllFromMap(
			@Parameter(description = "Identification of the map to get data", required = true) @PathVariable("identification") String identification) {
		log.debug("Recieved request to get all data from cached map {}", identification);

		final User user = userService.getUser(utils.getUserId());

		try {
			final Map<String, String> values = cacheBS.getAllFromMap(identification, user);
			return ResponseEntity.ok().body(values);
		} catch (final CacheBusinessServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/maps/{identification}/getMany/", produces = "text/html")
	public ResponseEntity<Map<String, String>> getManyFromMap(
			@Parameter(description = "Identification of the map to get data", required = true) @PathVariable("identification") String identification,
			@RequestBody(required = true) Set<String> keys) {
		log.debug("Recieved request to get several data from cached map {}", identification);

		final User user = userService.getUser(utils.getUserId());

		try {
			final Map<String, String> values = cacheBS.getManyFromMap(identification, user, keys);
			return ResponseEntity.ok().body(values);
		} catch (final CacheBusinessServiceException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

	}

}

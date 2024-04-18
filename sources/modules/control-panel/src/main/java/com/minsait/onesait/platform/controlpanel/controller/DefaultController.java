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
package com.minsait.onesait.platform.controlpanel.controller;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.config.model.Themes;
import com.minsait.onesait.platform.config.model.Themes.editItems;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ThemesRepository;
import com.minsait.onesait.platform.controlpanel.rest.management.login.LoginManagementController;
import com.minsait.onesait.platform.controlpanel.security.twofactorauth.TwoFactorAuthService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DefaultController {

	@Autowired
	private AppWebUtils utils;

	private static final String CAS = "cas";
	private static final String SAML = "saml";

	@Autowired
	private ThemesRepository themesRepository;

	@Autowired
	private LoginManagementController loginController;

	@Value("${captcha.enable}")
	private boolean captchaOn;

	@Value("${captcha.token}")
	private String captchaToken;

	@Value("${onesaitplatform.password.pattern}")
	private String passwordPattern;

	@Value("${onesaitplatform.authentication.provider}")
	private String provider;

	private static final String USERS_CONSTANT = "users";

	private static final String PASS_CONSTANT = "passwordPattern";

	@Autowired(required=false)
	private TwoFactorAuthService twoFactorAuthService;

	@GetMapping("/")
	public String base() {
		if (utils.isAuthenticated()) {
			if (utils.isUser()) {
				return "redirect:/marketasset/list";
			} else if (utils.isDataViewer()) {
				return "redirect:/dashboards/viewerlist";
			}
			return "redirect:/main";
		}
		return "redirect:/";
	}

	@PreAuthorize("hasRole('ROLE_PREVERIFIED_ADMINISTRATOR')")
	@GetMapping("/verify")
	public String verifyIndex(Authentication auth, HttpServletRequest request) {
		if (twoFactorAuthService.isUserInPurgatory(auth.getName()))
			return "verify";
		else {
			request.getSession().invalidate();
			return "redirect:/login";
		}
	}

	@GetMapping("/home")
	public String home() {
		return "home";
	}

	@GetMapping("/login")
	public String login(Model model) {
		readThemes(model);
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute("captchaToken", captchaToken);
		model.addAttribute("captchaEnable", captchaOn);
		model.addAttribute(PASS_CONSTANT, passwordPattern);
		if (provider.equals(CAS) || provider.equals(SAML))
			return "redirect:/";
		else
			return "login";
	}

	@GetMapping("/error")
	public String error() {
		return "error/500";
	}

	@GetMapping("/403")
	public String error403(Model model) {
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, passwordPattern);
		return "error/403";
	}

	@GetMapping("/500")
	public String error500(Model model) {
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, passwordPattern);
		return "error/500";
	}

	@GetMapping("/404")
	public String error404() {
		return "error/404";
	}

	@PreAuthorize("hasRole('ROLE_PREVERIFIED_ADMINISTRATOR')")
	@PostMapping("/verify")
	public String verify(Authentication auth, @RequestParam("code") String code, HttpServletRequest request) {

		if (twoFactorAuthService.verify(auth.getPrincipal().toString(), code)) {
			twoFactorAuthService.promoteToRealRole(auth);
			// request.getSession().setAttribute("oauthToken",
			// loginController.postLoginOauthNopass(auth));
			return "redirect:/main";
		} else
			return "redirect:verify?error";
	}

	private void readThemes(Model model) {
		JSONObject json = new JSONObject();
		try {
			final List<Themes> activeThemes = themesRepository.findActive();
			if (activeThemes.size() == 1) {
				json = new JSONObject(activeThemes.get(0).getJson());
			}
		} catch (final Exception e) {
			log.error("Error reading Json: ", e);
		}

		final Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			try {
				final Themes.editItems loginTitle = editItems.valueOf(keys.next());
				switch (loginTitle) {
				case LOGIN_TITLE:
					model.addAttribute("title", json.getString(Themes.editItems.LOGIN_TITLE.toString()));
					break;
				case LOGIN_TITLE_ES:
					model.addAttribute("title_es", json.getString(Themes.editItems.LOGIN_TITLE_ES.toString()));
					break;
				case LOGIN_IMAGE:
					model.addAttribute("image", json.getString(Themes.editItems.LOGIN_IMAGE.toString()));
					break;
				case LOGIN_BACKGROUND_COLOR:
					model.addAttribute("backgroundColor",
							json.getString(Themes.editItems.LOGIN_BACKGROUND_COLOR.toString()));
					break;
				default:
					break;
				}
			} catch (final Exception e) {
				log.error("Error parsing Json: ", e);
			}
		}
	}

}

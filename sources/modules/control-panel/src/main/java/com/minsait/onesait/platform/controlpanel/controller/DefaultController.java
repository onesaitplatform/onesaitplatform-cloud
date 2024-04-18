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
package com.minsait.onesait.platform.controlpanel.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.minsait.onesait.platform.config.components.GoogleAnalyticsConfiguration;
import com.minsait.onesait.platform.config.model.Themes;
import com.minsait.onesait.platform.config.model.Themes.editItems;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ThemesRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.controlpanel.security.twofactorauth.TwoFactorAuthService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DefaultController {

	@Autowired
	private AppWebUtils utils;

	private static final String CAS = "cas";

	@Autowired
	private ThemesRepository themesRepository;

	@Autowired
	private ConfigurationService configurationService;

	@Value("${captcha.enable}")
	private boolean captchaOn;

	@Value("${captcha.token}")
	private String captchaToken;

	@Value("${onesaitplatform.authentication.provider}")
	private String provider;

	@Value("${splash.enable}")
	private boolean splashEnable;

	@Value("${splash.everyXHours}")
	private int everyXHours;

	private static final String USERS_CONSTANT = "users";

	private static final String PASS_CONSTANT = "passwordPattern";
	private static final String LOGIN_LOCALE = "login_locale";
	private static final String PASSWORD_PATTERN = "password-pattern";
	private static final String APP_ID = "appId";

	@Autowired(required = false)
	private TwoFactorAuthService twoFactorAuthService;

	@Autowired
	private MultitenancyService multitenancyService;

	@Autowired
	private IntegrationResourcesService resourcesService;
	
	@Autowired 
	private HttpSession httpSession;

	@GetMapping("/")
	public String base() {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		if (utils.isAuthenticated()) {
			if (utils.isDataViewer()) {
				return "redirect:/dashboards/viewerlist";
			}
			return "redirect:/main";
		}
		return "redirect:/";
	}

	@PreAuthorize("hasRole('ROLE_PREVERIFIED_ADMINISTRATOR')")
	@GetMapping("/verify")
	public String verifyIndex(Authentication auth, HttpServletRequest request) {
		if (twoFactorAuthService.isUserInPurgatory(auth.getName())) {
			return "verify";
		} else {
			request.getSession().invalidate();
			return "redirect:/login";
		}
	}

	@PreAuthorize("hasRole('ROLE_PREVERIFIED_TENANT_USER')")
	@GetMapping("/promote")
	public String promote(Authentication auth, HttpServletRequest request, Model model) {
		final List<Vertical> verticals = multitenancyService.getVerticals(auth.getName());
		model.addAttribute("verticals", verticals);
		return "multitenancy/promote";
	}

	@PreAuthorize("hasRole('ROLE_PREVERIFIED_TENANT_USER')")
	@PostMapping("/promote")
	public String promoteRoleVertical(Authentication auth, HttpServletRequest request,
			@RequestParam("vertical") String vertical) {
		multitenancyService.promoteRole(vertical, auth);
		utils.renewOauth2AccessToken(request, SecurityContextHolder.getContext().getAuthentication());
		return "redirect:/main";
	}

	@PreAuthorize("hasRole('ROLE_COMPLETE_IMPORT')")
	@GetMapping("/user-import")
	public String completeImport(Authentication auth, HttpServletRequest request, Model model) {
		model.addAttribute("verticals", multitenancyService.getAllVerticals());
		return "multitenancy/complete-import";
	}

	@PreAuthorize("hasRole('ROLE_COMPLETE_IMPORT')")
	@PostMapping("/user-import")
	public String completeImport(Authentication auth, HttpServletRequest request,
			@RequestParam("vertical") String vertical, @RequestParam("tenant") String tenant) {
		multitenancyService.changeUserTenant(SecurityContextHolder.getContext().getAuthentication().getName(), tenant);
		multitenancyService.promoteRole(vertical, auth);
		multitenancyService.removeFromDefaultTenant(SecurityContextHolder.getContext().getAuthentication().getName(),
				tenant);
		utils.renewOauth2AccessToken(request, SecurityContextHolder.getContext().getAuthentication());
		return "redirect:/main";
	}

	@PreAuthorize("hasRole('ROLE_COMPLETE_IMPORT')")
	@GetMapping("/user-import/vertical/{vertical}/tenants")
	public @ResponseBody List<String> getTenants(@RequestParam("vertical") String vertical) {
		final Optional<Vertical> v = multitenancyService.getVertical(vertical);
		if (v.isPresent()) {
			return v.get().getTenants().stream().map(Tenant::getName).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	@GetMapping("/home")
	public String home() {
		return "home";
	}

	@GetMapping("/login")
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
		final GoogleAnalyticsConfiguration configuration = configurationService
				.getGoogleAnalyticsConfiguration("default");
		readThemes(model);
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute("captchaToken", captchaToken);
		model.addAttribute("captchaEnable", captchaOn);
		model.addAttribute("googleAnalyticsToken", configuration.getTrackingid());
		model.addAttribute("googleAnalyticsEnable", configuration.isEnable());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		model.addAttribute("splashEnable", splashEnable);
		model.addAttribute("everyXHours", everyXHours);

		final String locale = (String) request.getSession().getAttribute(LOGIN_LOCALE);
		if (locale != null) {
			RequestContextUtils.getLocaleResolver(request).setLocale(request, response, Locale.forLanguageTag(locale));
			request.getSession().removeAttribute(LOGIN_LOCALE);
		}
		if (provider.equals(CAS)) {
			return "redirect:/";
		} else {
			return "login";
		}
	}

	@RequestMapping(value = { "/error" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String error() {
		return "error/500";
	}

	@GetMapping("/403")
	public String error403(Model model) {
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		return "error/403";
	}

	@GetMapping("/500")
	public String error500(Model model) {
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		return "error/500";
	}

	@GetMapping("/404")
	public String error404() {
		return "error/404";
	}

	@GetMapping("/blocked")
	public String blocked(Model model) {
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		return "blocked";
	}

	@GetMapping("/loginerror")
	public String loginerror(Model model) {
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		return "loginerror";
	}

	@PreAuthorize("hasRole('ROLE_PREVERIFIED_ADMINISTRATOR')")
	@PostMapping("/verify")
	public String verify(Authentication auth, @RequestParam("code") String code, HttpServletRequest request) {

		if (twoFactorAuthService.verify(auth.getName(), code)) {
			twoFactorAuthService.promoteToRealRole(auth);
			// request.getSession().setAttribute("oauthToken",
			// loginController.postLoginOauthNopass(auth));
			return "redirect:/main";
		} else {
			return "redirect:verify?error";
		}
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
				case CSS:
					model.addAttribute("css", json.getString(Themes.editItems.CSS.toString()));
					break;
				case JS:
					model.addAttribute("js", json.getString(Themes.editItems.JS.toString()));
					break;
				default:
					break;
				}
			} catch (final Exception e) {
				log.error("Error parsing Json: ", e);
			}
		}
	}

	private String getPasswordPattern() {
		return ((String) resourcesService.getGlobalConfiguration().getEnv().getControlpanel().get(PASSWORD_PATTERN));
	}

}

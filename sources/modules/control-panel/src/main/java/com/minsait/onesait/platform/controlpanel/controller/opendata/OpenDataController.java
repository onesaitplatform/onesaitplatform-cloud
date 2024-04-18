/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.opendata;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.business.services.opendata.OpenDataApi;
import com.minsait.onesait.platform.config.components.GoogleAnalyticsConfiguration;
import com.minsait.onesait.platform.config.model.Themes;
import com.minsait.onesait.platform.config.model.Themes.editItems;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ThemesRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.user.UserPendingValidation;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.libraries.mail.util.HtmlFileAttachment;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/opendata")
@Controller
@Slf4j
public class OpenDataController {

	@Value("${captcha.enable}")
	private boolean captchaOn;

	@Value("${captcha.token}")
	private String captchaToken;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Value("${onesaitplatform.user.registry.validation.url:http://localhost:18000/controlpanel/users/validateNewUserFromLogin/}")
	private String validationUrlNewUser;

	private String openDataUrl;

	@Autowired
	private OpenDataApi api;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ThemesRepository themesRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private MailService mailService;
	@Autowired()
	@Qualifier("cachePendingRegistryUsers")
	private Map<String, UserPendingValidation> cachePendingRegistryUsers;

	private static final String REDIRECT_REGISTER = "redirect:/opendata/register";
	private static final String PASSWORD_PATTERN = "password-pattern";
	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@PostConstruct
	public void openDataSetUp() {
		openDataUrl = integrationResourcesService.getUrl(Module.OPEN_DATA, ServiceUrl.BASE);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/access")
	public ModelAndView accessPortal() {
		return new ModelAndView("redirect:" + api.accessPortal());
	}

	@GetMapping(value = "/register")
	public String registerUserLogin(Model model) {
		final GoogleAnalyticsConfiguration configuration = configurationService
				.getGoogleAnalyticsConfiguration("default");
		readThemes(model);
		model.addAttribute("users", new User());
		model.addAttribute("captchaToken", captchaToken);
		model.addAttribute("captchaEnable", captchaOn);
		model.addAttribute("googleAnalyticsToken", configuration.getTrackingid());
		model.addAttribute("googleAnalyticsEnable", configuration.isEnable());
		model.addAttribute("passwordPattern", getPasswordPattern());
		return "opendata/register";
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String registerUserLogin(@ModelAttribute User user, RedirectAttributes redirectAttributes,
			HttpServletRequest request) {

		log.info("Received request to register new user: {}", user.getUserId());

		final String nameRole = request.getParameter("roleName");

		if (user != null) {
			if (userService.emailExists(user)) {
				log.debug("There is already an user with this email");
				utils.addRedirectMessage("login.error.email.duplicate", redirectAttributes);
				return REDIRECT_REGISTER;
			}
			if (userService.userExists(user)) {
				log.debug("There is already an user with this identifier");
				utils.addRedirectMessage("login.error.username", redirectAttributes);
				return REDIRECT_REGISTER;
			}

			if (!userService.emailExists(user)) {

				try {
					if (nameRole == null) {
						log.debug("A role must be selected");
						utils.addRedirectMessage("login.error.user.register", redirectAttributes);
						return REDIRECT_REGISTER;
					}

					boolean inFlight = false;
					for (final UserPendingValidation cached : cachePendingRegistryUsers.values()) {
						if (cached.getUser().getEmail().equals(user.getEmail())) {
							inFlight = true;
						}
					}

					if (!inFlight) {

						final String temporalUuid = UUID.randomUUID().toString();
						final UserPendingValidation userPendingValidation = new UserPendingValidation();
						userPendingValidation.setRoleName(nameRole);
						userPendingValidation.setUser(user);

						final String defaultTitle = "[Onesait Plaform] New Account";
						final String defaultMessage = "To complete your registry in Onesait Plaform, click in the link|Register User|In case of not being redirected, please copy this url in your browser|If after 10 minutes you don't activate your user, it will be deleted";

						final String emailTitle = utils.getMessage("user.create.mail.title", defaultTitle);
						String emailMessage = utils.getMessage("user.create.mail.body", defaultMessage);

						String[] emailParts = emailMessage.split("\\|");

						String validationUrl = validationUrlNewUser.concat(temporalUuid);

						String htmlText = "<html><body>"
								+ "<div><img src='cid:onesaitplatformimg' style='height:230px;' /></div>" + "<div>"
								+ emailParts[0] + "</div>" + "<br/>" + "<div>" + "<a href='" + validationUrl + "'>"
								+ emailParts[1] + "</a></div>" + "<br/>" + "<div>" + emailParts[2] + ":</div>"
								+ "<div><strong>" + validationUrl + "</strong></div>" + "<br/>" + "<div>"
								+ emailParts[3] + "</div>" + "</body></html>";

						File imgOnesaitPlatform = new ClassPathResource("static/img/onesaitplatform.jpeg").getFile();

						HtmlFileAttachment demoImg = new HtmlFileAttachment();
						demoImg.setFile(imgOnesaitPlatform);
						demoImg.setFileKey("onesaitplatformimg");

						log.info("Send email to: {} in order to register new user", user.getEmail());

						mailService.sendConfirmationMailMessage(user.getEmail(), emailTitle, htmlText, demoImg);

						cachePendingRegistryUsers.put(temporalUuid, userPendingValidation);

						utils.addRedirectMessage("user.create.mail.sended", redirectAttributes);
						return "redirect:" + openDataUrl;

					} else {// There is a previous request in flight
						if (log.isDebugEnabled()) {
							log.debug("There is a previous request to create a user using email: {}", user.getEmail());
						}
						utils.addRedirectMessage("user.create.mail.inflight", redirectAttributes);
						return REDIRECT_REGISTER;
					}

				} catch (final UserServiceException e) {
					log.error("This user already exist", e);
					utils.addRedirectMessage("login.error.register", redirectAttributes);
					return REDIRECT_REGISTER;
				} catch (final MailSendException e) {
					log.error("Error sending mail to finish user creation", e);
					utils.addRedirectMessage("login.error.email.fail", redirectAttributes);
					return REDIRECT_REGISTER;
				} catch (final Exception e) {
					log.error("Error creating user", e);
					utils.addRedirectMessage("user.error.mailservice", redirectAttributes);
					return REDIRECT_REGISTER;
				}
			}
		}
		return "redirect:/login?errorRegister";

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

	private String getPasswordPattern() {
		return ((String) resourcesService.getGlobalConfiguration().getEnv().getControlpanel().get(PASSWORD_PATTERN));
	}
}

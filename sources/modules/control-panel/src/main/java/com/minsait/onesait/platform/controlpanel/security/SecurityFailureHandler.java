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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityFailureHandler implements AuthenticationFailureHandler {

	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ConfigurationService configurationService;
	@Value("${onesaitplatform.urls.iotbroker}")
	private String SERVERNAME;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String userName = request.getParameter("username");

		if (userName != null && userName.length() > 0) {
			// catch user
			// if user exist increase failed attemps
			MasterUser masterUser = multitenancyService.increaseFailedAttemp(userName);
			if (masterUser == null) {
				// show message
				response.sendRedirect(request.getContextPath() + "/loginerror");
			} else if (masterUser != null && !masterUser.isActive()) {
				// redirect to blocked page
				response.sendRedirect(request.getContextPath() + "/blocked");
			} else {
				// validate limit
				final Configuration configuration = configurationService
						.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
				final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
						.fromYaml(configuration.getYmlConfig()).get("Authentication");
				final int limitFailedAttemp = (Integer) ymlExpirationUsersPassConfig.get("limitFailedAttemp");

				if (masterUser.getFailedAtemps() >= limitFailedAttemp && limitFailedAttemp >= 0) {
					// if the limit is equal or higher
					if (userService.deactivateUser(userName)) {
						// send mail
						try {
							final String defaultTitle = "[Onesait Plaform] User account locked";
							final String defaultMessage = " Your account has been blocked contact your administrator.";
							final String defaultServer1 = "On the ";
							final String defaultServer2 = "server. ";
							final String emailTitle = utils.getMessage("user.attemp.bloqued.mail.title", defaultTitle);
							String emailMessage = utils.getMessage("user.attemp.bloqued.mail.body", defaultMessage);
							String server1 = utils.getMessage("user.attemp.bloqued.mail.body.server.1", defaultServer1);
							String server2 = utils.getMessage("user.attemp.bloqued.mail.body.server.2", defaultServer2);
							emailMessage = server1 + SERVERNAME + " " + server2 + emailMessage;
							log.info("Send email to {} in order user account locked", masterUser.getEmail());
							mailService.sendMail(masterUser.getEmail(), emailTitle, emailMessage);

						} catch (final Exception e) {
							log.warn("Problem sending mail on update User ", e);
						}
					}
					// redirect to blocked page
					response.sendRedirect(request.getContextPath() + "/blocked");
				} else {
					// show message
					response.sendRedirect(request.getContextPath() + "/loginerror");
				}

			}
		} else {
			// show message
			response.sendRedirect("login");
		}

	}

}

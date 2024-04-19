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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;
import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OPLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	@Autowired
	EventRouter eventRouter;

	@Autowired(required = false)
	PlugableOauthAuthenticator plugableAuthenticator;

	@PostConstruct
	public void setUp() {
		if (plugableAuthenticator != null && !StringUtils.isEmpty(plugableAuthenticator.getLogoutUrl())) {
			setDefaultTargetUrl(plugableAuthenticator.getLogoutUrl());
		}
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

		super.onLogoutSuccess(request, response, authentication);
		new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY)
				.logout(request, response, authentication);

		if (authentication == null) {
			return;
		}
		String user;
		if (authentication.getPrincipal() instanceof UserDetails) {
			user = ((UserDetails) authentication.getPrincipal()).getUsername();
		} else {
			user = (String) authentication.getPrincipal();
		}

		if (user != null) {
			final OPAuditEvent s2event = OPEventFactory.builder().build().createAuditEvent(EventType.SECURITY,
					"Logout Success for user: " + user);

			s2event.setUser(user);
			s2event.setOperationType(OperationType.LOGOUT.name());
			s2event.setOtherType("LogoutEventSuccess");
			s2event.setResultOperation(ResultOperationType.SUCCESS);
			// if (authentication.getDetails() != null) {
			// WebAuthenticationDetails details2 = (WebAuthenticationDetails)
			// authentication.getDetails();
			// s2event.setRemoteAddress(details2.getRemoteAddress());
			// s2event.setSessionId(details2.getSessionId());
			// }
			s2event.setModule(Module.CONTROLPANEL);
			eventRouter.notify(s2event.toJson());

		} else {
			log.info("No User recovered to process audit event");
		}

	}
}

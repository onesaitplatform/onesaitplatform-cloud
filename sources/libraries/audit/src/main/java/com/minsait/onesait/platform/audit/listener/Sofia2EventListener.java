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
package com.minsait.onesait.platform.audit.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditRemoteEvent;
import com.minsait.onesait.platform.audit.bean.OPAuthAuditEvent;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.audit.notify.EventRouter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Sofia2EventListener {

	@Autowired
	private EventRouter eventRouter;

	@Async
	@EventListener
	void handleAsync(OPAuditError event) throws JsonProcessingException {
		log.info("Sofia2EventListener :: Default Event Processing detected : thread '{}' handling '{}' async event",
				event.getType(), event.getMessage());
		eventRouter.notify(event.toJson());
	}

	@Async
	@EventListener
	void handleAsyncEvent(OPAuditEvent event) throws JsonProcessingException {
		if (!(event instanceof OPAuditRemoteEvent)) {
			log.info("Sofia2EventListener :: Default Event Processing detected : thread '{}' handling '{}' async event",
					event.getType(), event.getMessage());
			log.info(event.toJson());

		}

	}

	@EventListener
	@Async
	public void handleAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
		final OPAuthAuditEvent s2event = OPEventFactory.builder().build().createAuditAuthEvent(EventType.SECURITY,
				"Login Success for User : " + event.getAuthentication().getPrincipal().toString());

		if (event.getSource() instanceof UsernamePasswordAuthenticationToken
				&& ((UsernamePasswordAuthenticationToken) event.getSource())
						.getDetails() instanceof WebAuthenticationDetails) {// Login in web
			log.info("Authentication success event for user {}", event.getAuthentication().getPrincipal().toString());
			s2event.setOperationType(OperationType.LOGIN.name());
		} else { // OAuth Token generation after login
			log.info("OAuth Authentication success event for user {}", event.getAuthentication().getPrincipal().toString());
			s2event.setOperationType(OperationType.LOGIN_OAUTH.name());
		}

		s2event.setOtherType(AuthenticationSuccessEvent.class.getName());
		s2event.setUser((String) event.getAuthentication().getPrincipal());
		s2event.setResultOperation(ResultOperationType.SUCCESS);
		if (event.getAuthentication().getDetails() != null) {
			final Object details = event.getAuthentication().getDetails();
			setAuthValues(details, s2event);
		}
		getEventRouter().notify(s2event.toJson());

	}

	@EventListener
	@Async
	public void handleAuthenticationFailureBadCredentialsEvent(AuthenticationFailureBadCredentialsEvent event) {
		log.info("authentication failure bad credentials event for user {}", event.getAuthentication().getPrincipal().toString());

		final String message = "Login Failed (Incorrect Credentials) for User: "+ event.getAuthentication().getPrincipal().toString();

		final OPAuthAuditEvent s2event = OPEventFactory.builder().build().createAuditAuthEvent(EventType.SECURITY,
				message);

		s2event.setOperationType(OperationType.LOGIN.name());
		s2event.setUser((String) event.getAuthentication().getPrincipal());
		s2event.setOtherType(AuthorizationFailureEvent.class.getName());

		s2event.setResultOperation(ResultOperationType.ERROR);

		if (event.getAuthentication().getDetails() != null) {
			final Object details = event.getAuthentication().getDetails();
			setAuthValues(details, s2event);
		}

		getEventRouter().notify(s2event.toJson());
	}

	@EventListener
	@Async
	public void handleAuthorizationFailureEvent(AuthorizationFailureEvent errorEvent) {
		log.info("authorization failure  event for user {} ",errorEvent.getAuthentication().getPrincipal().toString());

		final OPAuthAuditEvent s2event = OPEventFactory.builder().build().createAuditAuthEvent(EventType.SECURITY,
				"Login Failed (AuthorizationFailure) for User: "
						+ errorEvent.getAuthentication().getPrincipal().toString());

		s2event.setOperationType(OperationType.LOGIN.name());
		s2event.setUser((String) errorEvent.getAuthentication().getPrincipal());
		s2event.setOtherType(AuthorizationFailureEvent.class.getName());

		s2event.setResultOperation(ResultOperationType.ERROR);

		if (errorEvent.getAuthentication().getDetails() != null) {
			final Object details = errorEvent.getAuthentication().getDetails();
			setAuthValues(details, s2event);
		}

		getEventRouter().notify(s2event.toJson());
	}

	private static void setAuthValues(Object details, OPAuthAuditEvent s2event) {
		if (details instanceof WebAuthenticationDetails) {
			final WebAuthenticationDetails details2 = (WebAuthenticationDetails) details;
			s2event.setRemoteAddress(details2.getRemoteAddress());
			s2event.setModule(Module.CONTROLPANEL);
		} else if (details instanceof OAuth2AuthenticationDetails) {
			final OAuth2AuthenticationDetails details2 = (OAuth2AuthenticationDetails) details;
			s2event.setRemoteAddress(details2.getRemoteAddress());
			s2event.setModule(Module.APIMANAGER);
			final Map<String, Object> data = new HashMap<>();
			data.put("tokenType", details2.getTokenType());
			data.put("tokenValue", details2.getTokenValue());
			s2event.setExtraData(data);

		} else if (details instanceof Map) {// OAuth Token generation after login in controlpanel
			s2event.setModule(Module.CONTROLPANEL);
		}
	}

	public EventRouter getEventRouter() {
		return eventRouter;
	}

}

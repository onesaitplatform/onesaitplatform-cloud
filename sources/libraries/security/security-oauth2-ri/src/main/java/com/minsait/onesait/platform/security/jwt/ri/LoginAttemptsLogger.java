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
package com.minsait.onesait.platform.security.jwt.ri;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginAttemptsLogger {

	private static final String DETAILS_STR = "details";

	@EventListener
	public void auditEventHappened(AuditApplicationEvent auditApplicationEvent) {
		final AuditEvent auditEvent = auditApplicationEvent.getAuditEvent();
		if (log.isDebugEnabled()) {
			log.debug("Begin -> Audit Login Happened -> Principal {} - {}" + auditEvent.getPrincipal(),
				auditEvent.getType());
		}	

		if (auditEvent.getData().get(DETAILS_STR) instanceof WebAuthenticationDetails) {
			final WebAuthenticationDetails details = (WebAuthenticationDetails) auditEvent.getData().get(DETAILS_STR);
			if (log.isDebugEnabled()) {
				log.debug("  Class Id: WebAuthenticationDetails Remote IP address: {}, Session Id: {}",
					details.getRemoteAddress(), details.getSessionId());
			}			
		} else if (auditEvent.getData().get(DETAILS_STR) instanceof OAuth2AuthenticationDetails) {
			final OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auditEvent.getData()
					.get(DETAILS_STR);
			if (log.isDebugEnabled()) {
				log.debug(
					"  Class Id: OAuth2AuthenticationDetails Remote IP address: {}, Session Id: {}, Token Type: {}, Token Value: {}",
					details.getRemoteAddress(), details.getSessionId(), details.getTokenType(),
					details.getTokenValue());
			}			
		}
		if (log.isDebugEnabled()) {
			log.debug("  Request URL: {} ", auditEvent.getData().get("requestUrl"));
			log.debug("End -> Audit Login Happened -> Principal {} - {} ", auditEvent.getPrincipal(), auditEvent.getType());
		}		
	}
}
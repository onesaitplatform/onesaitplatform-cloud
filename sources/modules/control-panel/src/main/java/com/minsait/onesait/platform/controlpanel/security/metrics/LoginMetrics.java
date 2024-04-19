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
package com.minsait.onesait.platform.controlpanel.security.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;

@Component
public class LoginMetrics {

	@Autowired(required = false)
	private MetricsManager metricsManager;

	@EventListener
	@Async
	public void handleAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
		if (null != this.metricsManager) {
			this.metricsManager.logControlPanelLogin(event.getAuthentication().getName(), "OK");
		}
	}

	@EventListener
	@Async
	public void handleAuthenticationFailureBadCredentialsEvent(AuthenticationFailureBadCredentialsEvent event) {
		if (null != this.metricsManager) {
			this.metricsManager.logControlPanelLogin(event.getAuthentication().getName(), "KO");
		}
	}
}

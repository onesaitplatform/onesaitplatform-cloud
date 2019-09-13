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
package com.minsait.onesait.platform.controlpanel.security;

import javax.servlet.http.HttpSessionEvent;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "cas")
public class SLOConfigurationCAS {
	@Value("${onesaitplatform.authentication.cas.base_url}")
	private String casBaseUrl;
	@Value("${onesaitplatform.authentication.cas.logout_url:http://localhost:8080/cas/logout}")
	private String casLogoutUrl;

	@Bean
	public SecurityContextLogoutHandler securityContextLogoutHandler() {
		return new SecurityContextLogoutHandler();
	}

	@Bean
	public LogoutFilter logoutFilter() {
		final LogoutFilter logoutFilter = new LogoutFilter(casLogoutUrl, securityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl("/logout");
		return logoutFilter;
	}

	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		final SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		singleSignOutFilter.setCasServerUrlPrefix(casBaseUrl + "/cas");
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		return singleSignOutFilter;
	}

	@EventListener
	public SingleSignOutHttpSessionListener singleSignOutHttpSessionListener(HttpSessionEvent event) {
		return new SingleSignOutHttpSessionListener();
	}
}

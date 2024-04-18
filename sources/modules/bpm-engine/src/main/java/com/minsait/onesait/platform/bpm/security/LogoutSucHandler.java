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
package com.minsait.onesait.platform.bpm.security;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.security.PlugableOauthAuthenticator;

@Component
public class LogoutSucHandler extends SimpleUrlLogoutSuccessHandler {

	@Autowired(required = false)
	private PlugableOauthAuthenticator plugableAuthenticator;

	@Value("${security.oauth2.client.logoutUrl}")
	private String logoutUrl;

	@PostConstruct
	public void setUp() {
		if (plugableAuthenticator != null && StringUtils.hasText(plugableAuthenticator.getLogoutUrl())) {
			setDefaultTargetUrl(plugableAuthenticator.getLogoutUrl());
		} else {
			setDefaultTargetUrl(logoutUrl);
		}
	}

}

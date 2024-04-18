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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpSessionEvent;

import org.jasig.cas.client.configuration.ConfigurationKey;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@Bean("casLogoutFilter")
	public LogoutFilter logoutFilter() {
		final LogoutFilter logoutFilter = new LogoutFilter(casLogoutUrl, securityContextLogoutHandler());
		logoutFilter.setFilterProcessesUrl("/logout");
		return logoutFilter;
	}

	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
		final SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
		//singleSignOutFilter.setCasServerUrlPrefix(casBaseUrl + "/cas");
		
        try {
            ConfigurationKey<String> key = ConfigurationKeys.CAS_SERVER_URL_PREFIX;
            Method setter = singleSignOutFilter.getClass()
                    .getDeclaredMethod("set" + StringUtils.capitalize(key.getName()));
            setter.invoke(singleSignOutFilter, casBaseUrl + "/cas");
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            // since commit :
            // https://github.com/apereo/java-cas-client/commit/fdc948b8ec697be0ae04da2f91c66c6526d463b5#diff-676b9d196aacd4b54bc978c62ccbacd8d29552fcd694061355bd930405560fb5
            // setCasServerUrlPrefix(getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX)); does NOT exists anymore
            log.info(
                    "Since apereo CAS client 3.6.0 setCasServerUrlPrefix(getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX)); does NOT exists anymore");
        }		
		singleSignOutFilter.setIgnoreInitConfiguration(true);
		return singleSignOutFilter;
	}

	@EventListener
	public SingleSignOutHttpSessionListener singleSignOutHttpSessionListener(HttpSessionEvent event) {
		return new SingleSignOutHttpSessionListener();
	}
}

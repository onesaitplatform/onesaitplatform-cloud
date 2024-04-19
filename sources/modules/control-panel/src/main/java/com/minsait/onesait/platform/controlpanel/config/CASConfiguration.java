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
package com.minsait.onesait.platform.controlpanel.config;

import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.minsait.onesait.platform.controlpanel.security.CASUserDetailsService;
import com.minsait.onesait.platform.controlpanel.security.CustomCASAuthenticationEntryPoint;

@Configuration
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "cas")
public class CASConfiguration {

	@Value("${onesaitplatform.authentication.cas.base_url}")
	private String casBaseUrl;
	@Value("${onesaitplatform.authentication.cas.login_url:/cas/login}")
	private String casLoginUrl;
	@Value("${onesaitplatform.authentication.cas.service_id}")
	private String serviceId;
	@Value("${onesaitplatform.authentication.cas.key}")
	private String key;

	@Bean
	public ServiceProperties serviceProperties() {
		final ServiceProperties serviceProperties = new ServiceProperties();
		serviceProperties.setService(serviceId);
		serviceProperties.setSendRenew(false);
		return serviceProperties;
	}

	@Bean
	@Primary
	public AuthenticationEntryPoint authenticationEntryPoint(ServiceProperties sP) {

		final CustomCASAuthenticationEntryPoint entryPoint = new CustomCASAuthenticationEntryPoint();
		entryPoint.setLoginUrl(casLoginUrl);
		entryPoint.setServiceProperties(sP);
		return entryPoint;
	}

	@Bean
	public TicketValidator ticketValidator() {
		return new Cas30ServiceTicketValidator(casBaseUrl + "/cas");
	}

	@Bean
	@Qualifier("casAuthenticationProvider")
	@Primary
	public CasAuthenticationProvider casAuthenticationProvider(CASUserDetailsService userDetailsService) {

		final CasAuthenticationProvider provider = new CasAuthenticationProvider();
		provider.setServiceProperties(serviceProperties());
		provider.setTicketValidator(ticketValidator());
		provider.setAuthenticationUserDetailsService(userDetailsService);
		provider.setKey(key);
		return provider;
	}

}
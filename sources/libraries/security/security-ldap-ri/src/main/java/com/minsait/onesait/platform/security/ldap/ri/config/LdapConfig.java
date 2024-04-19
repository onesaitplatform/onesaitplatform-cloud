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
package com.minsait.onesait.platform.security.ldap.ri.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "ldap")
public class LdapConfig {

	@Value("${ldap.url}")
	private String ldapUrl;

	@Value("${ldap.base}")
	private String ldapBaseDn;

	@Value("${ldap.username}")
	private String ldapSecurityPrincipal;

	@Value("${ldap.password}")
	private String ldapPrincipalPassword;

	@Value("${ldap.ignorepartialresult:false}")
	private boolean ignorePartialResult;

	public static final String LDAP_TEMPLATE_NO_BASE = "ldapTemplateNoBase";
	public static final String LDAP_TEMPLATE_BASE = "ldapTemplateBase";

	@Bean
	public LdapContextSource contextSource() {
		final LdapContextSource contextSource = new LdapContextSource();

		contextSource.setUrl(ldapUrl);
		contextSource.setBase(ldapBaseDn);
		contextSource.setUserDn(ldapSecurityPrincipal);
		contextSource.setPassword(ldapPrincipalPassword);
		contextSource.afterPropertiesSet();
		return contextSource;
	}

	@Bean
	@Qualifier(LDAP_TEMPLATE_BASE)
	public LdapTemplate ldapTemplate() {
		LdapTemplate result = new LdapTemplate(contextSource());
		result.setIgnorePartialResultException(ignorePartialResult);
		return result;
	}

	@Bean
	public LdapContextSource contextSourceNoBase() {
		final LdapContextSource contextSource = new LdapContextSource();

		contextSource.setUrl(ldapUrl);
		contextSource.setUserDn(ldapSecurityPrincipal);
		contextSource.setPassword(ldapPrincipalPassword);
		contextSource.afterPropertiesSet();
		return contextSource;
	}

	@Bean
	@Qualifier(LDAP_TEMPLATE_NO_BASE)
	public LdapTemplate ldapTemplateNoBase() {
		LdapTemplate result = new LdapTemplate(contextSourceNoBase());
		result.setIgnorePartialResultException(ignorePartialResult);
		return result;
	}
}
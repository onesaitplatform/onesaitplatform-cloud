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
package com.minsait.onesait.platform.resources.service;

import static com.minsait.onesait.platform.encryptor.config.JasyptConfig.JASYPT_BEAN;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.minsait.onesait.platform.config.components.MailConfiguration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.sun.mail.util.MailSSLSocketFactory;

import lombok.extern.slf4j.Slf4j;

@Configuration
@DependsOn(JASYPT_BEAN)
@Slf4j
public class MailConfig {

	@Autowired
	private ConfigurationService configurationService;

	private static final String DEFAULT_PROFILE = "default";

	@Value("${proxy-mail.host:localhost}")
	private String proxyHost;

	@Value("${proxy-mail.port:8080}")
	private int proxyPort;

	@Bean("emailSender")
	public JavaMailSender getJavaMailSender() {
		final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		final MailConfiguration configuration = mailConfigurationOSP();
		mailSender.setHost(configuration.getHost());
		mailSender.setPort(configuration.getPort());
		mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());
		mailSender.setUsername(configuration.getUsername());
		mailSender.setPassword(configuration.getPassword());

		final Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", configuration.getSmtp().isAuth());
		props.put("mail.smtp.starttls.enable", configuration.getSmtp().isStarttls_enable());
		props.put("mail.smtp.starttls.required", configuration.getSmtp().isStarttls_required());
		props.put("mail.smtp.timeout", configuration.getSmtp().getTimeout());
		props.put("mail.smtp.connectiontimeout", configuration.getSmtp().getConnectiontimeout());
		props.put("mail.smtp.writetimeout", configuration.getSmtp().getWritetimeout());
		MailSSLSocketFactory sf;
		try {
			sf = new MailSSLSocketFactory();
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.socketFactory", sf);
		} catch (final GeneralSecurityException e) {
			log.warn("Could not trust all certificates Mail config");
		}


		if (configuration.getSmtp().getFrom() != null && !configuration.getSmtp().getFrom().isEmpty()) {
			props.put("mail.smtp.from", configuration.getSmtp().getFrom());
		}

		if (proxyHost != null && !proxyHost.equals("localhost")) {
			props.put("mail.smtp.proxy.host", proxyHost);
			props.put("mail.smtp.proxy.port", proxyPort);
		}

		return mailSender;
	}

	@Bean("mailConfigurationOSP")
	public MailConfiguration mailConfigurationOSP() {
		return configurationService.getMailConfiguration(DEFAULT_PROFILE);
	}

	@Bean("mailFrom")
	public String mailFrom() {
		return mailConfigurationOSP().getSmtp().getFrom();
	}

}

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
package com.minsait.onesait.platform.libraries.mail;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@ContextConfiguration(classes = MailService.class)
@SpringBootTest
@SpringBootApplication
@Slf4j
public class MailServiceIntegrationTest {
	public static void main(String[] args) {
		SpringApplication.run(MailServiceIntegrationTest.class, args);
	}

	@Autowired
	MailService mail;


	@Test
	public void given_OneValidEmailAddress_When_OneTextMessageIsSent_Then_TheMessageIsSent() {
		try {
			mail.sendMail("support@onesaitplatform.com", "Test", "Test");
		} catch (Exception e) {
			log.error("Exception reached " + e.getMessage(), e);
			Assert.fail("Error sending mail");
		}
	}

	@Test
	public void given_OneValidEmailAddress_When_OneHTMLMessageIsSent_Then_TheMessageIsSent() {
		try {
			String htmlMail = "<html><body>Here is application.yml<body></html>";
			String base64data = "Q1AAAAiGsAAABAXAMAAIC4BgAAAHENAAAA4hoAAAAQ1wAAADAn/w8VUKiZ/Tt/9QAAAABJRU5ErkJggg==";
			mail.sendHtmlMailWithFile("support@onesaitplatform.com", "Test", htmlMail, "Attachment.jpg", base64data,
					true);
		} catch (Exception e) {
			log.error("Exception reached " + e.getMessage(), e);
			Assert.fail("Error sending mail");
		}
	}

	@Test
	public void given_OneValidEmailAddress_When_OneMessageWithTemplatesIsSent_Then_TheMessageIsSent() {
		try {
			final SimpleMailMessage message = new SimpleMailMessage();
			message.setSubject("Test");
			message.setText("Test mail with templates.");
			String args = null;
			mail.sendMailWithTemplate("support@onesaitplatform.com", "Test", message, args);
		} catch (Exception e) {
			log.error("Exception reached " + e.getMessage(), e);
			Assert.fail("Error sending mail");
		}
	}

}

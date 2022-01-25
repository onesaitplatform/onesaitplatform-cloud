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
package com.minsait.onesait.platform.libraries.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yml" })
@Slf4j
public class MailServiceImplTest {

	@Rule
	public GreenMailRule server = new GreenMailRule(new ServerSetup(2525, "localhost", "smtp"));

	@Autowired
	private MailService mailServiceImpl;

	private final static String TEST_MAIL = "test@onesaitplatform.com";

	@Ignore
	@Test
	public void testSendMailOK() {
		try {
			mailServiceImpl.sendMail(TEST_MAIL, "Subject", "Text");

			MimeMessage[] receivedMessages = server.getReceivedMessages();
			assertEquals("", 1, receivedMessages.length);
			assertEquals("Text", GreenMailUtil.getBody(server.getReceivedMessages()[0]));
			assertTrue("OK testSendMailOK", true);
		} catch (final Exception e) {
			log.error("Error testSendMailOK", e);
			log.error("If you are using GMail check https://support.google.com/accounts/answer/6010255", e);
			Assert.fail("Error sending mail");
		}
	}

	@Ignore
	@Test
	public void testSendMailWithNoTo() {
		try {
			String to = null;
			mailServiceImpl.sendMail(to, "Subject", "Text");
			assertTrue("OK testSendMailWithNoTo", true);
		} catch (final Exception e) {
			log.error("Error testSendMailWithNoTo", e);
			log.error("If you are using GMail check https://support.google.com/accounts/answer/6010255", e);
			Assert.assertTrue("You can´t send a mail without To", true);
		}
	}

	@Ignore
	@Test
	public void testSendMailWithAttachment() {
		try {
			String htmlMail = "<html><body>Here is application.yml<body></html>";
			String base64data = "Q1AAAAiGsAAABAXAMAAIC4BgAAAHENAAAA4hoAAAAQ1wAAADAn/w8VUKiZ/Tt/9QAAAABJRU5ErkJggg==";
			mailServiceImpl.sendHtmlMailWithFile("support@onesaitplatform.com", "Test", htmlMail, "Attachment.jpg",
					base64data, true);
			assertTrue("OK testSendMailWithAttachment", true);
		} catch (Exception e) {
			log.error("Exception reached " + e.getMessage(), e);
			Assert.fail("Error testSendMailWithAttachment");
		}
	}

	@Ignore
	@Test
	public void testSendMailArrayOK() {
		try {
			String[] to = new String[1];
			to[0] = TEST_MAIL;
			mailServiceImpl.sendMail(to, "Subject", "Text");

			MimeMessage[] receivedMessages = server.getReceivedMessages();
			assertEquals("", 1, receivedMessages.length);
			assertEquals("Text", GreenMailUtil.getBody(server.getReceivedMessages()[0]));
			assertTrue("OK testSendMailOK", true);
		} catch (final Exception e) {
			log.error("Error testSendMailOK", e);
			log.error("If you are using GMail check https://support.google.com/accounts/answer/6010255", e);
			Assert.fail("Error sending mail");
		}
	}

}

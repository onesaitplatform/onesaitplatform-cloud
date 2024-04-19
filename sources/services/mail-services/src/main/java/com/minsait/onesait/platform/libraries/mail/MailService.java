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
package com.minsait.onesait.platform.libraries.mail;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.mail.SimpleMailMessage;

import com.minsait.onesait.platform.libraries.mail.util.HtmlFileAttachment;

public interface MailService {
	void sendMail(String[] to, String subject, String text);
	void sendMail(String to, String subject, String text);

	void sendMailWithTemplate(String[] to, String subject, SimpleMailMessage template, String... templateArgs);
	void sendMailWithTemplate(String to, String subject, SimpleMailMessage template, String... templateArgs);

	void sendHtmlMailWithFile(String[] to, String subject, String text, String attachmentName, String attachment,
			boolean htmlenable) throws MessagingException;
	void sendHtmlMailWithFile(String to, String subject, String text, String attachmentName, String attachment,
            boolean htmlenable) throws MessagingException;
	
	void sendConfirmationMailMessage(String to, String subject, String htmlText, HtmlFileAttachment... files) throws MessagingException, IOException;
	
	void sendHtmlMail(String to[], String subject, String htmlText) throws MessagingException;
}

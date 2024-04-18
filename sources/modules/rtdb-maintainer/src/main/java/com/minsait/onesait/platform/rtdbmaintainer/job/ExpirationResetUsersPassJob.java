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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExpirationResetUsersPassJob {

	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;
	@Autowired
	private MessageSource messageSource;

	public void execute(JobExecutionContext context) throws IOException {

		final int hours = context.getJobDetail().getJobDataMap().getInt("hours");

		try {
			if (hours >= 0) {
				validateResetPass(hours);
			}
			log.debug("validate Reset Pass For Users");
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Rest error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
		} catch (final Exception e) {
			log.error("Error validate Reset Pass For Users", e);
		}
	}

	private void validateResetPass(int hours) {

		List<MasterUser> users = multitenancyService.getUsers();
		Date now = new Date();
		Date restartLimitDate = subtractHours(now, hours);

		users.forEach((masterUser) -> {
			if (masterUser.isActive()) {
				// check inactive days
				log.debug("----------------------------------");
				if (log.isDebugEnabled()) {
					log.debug("restartLimitDate :{}", restartLimitDate);
					log.debug("USER :{}", masterUser.getUserId());
					log.debug("masterUser.getResetPass() :{}", masterUser.getResetPass());
				}

				if (masterUser.getResetPass() != null && masterUser.getResetPass().before(restartLimitDate)) {
					log.debug("masterUser.getResetPass().before(restartLimitDate)): "
							+ masterUser.getResetPass().before(restartLimitDate));
					// block User and send mail
					userService.deactivateUser(masterUser.getUserId());
					sendPasswordExpiredEmail(masterUser);
				}
				log.debug("----------------------------------");

			}
		});
	}

	/**
	 * subtract days to date in java
	 * 
	 * @param date
	 * @param days
	 * @return
	 */
	private static Date subtractHours(Date date, int hours) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, -hours);
		return cal.getTime();
	}

	private void sendPasswordExpiredEmail(MasterUser masterUser) {
		final String defaultTitle = "[Onesait Plaform] Password expiration";
		final String defaultMessage = "Your password has expired contact your administrator to update it";
		final String emailTitle = getMessage("user.expiration.pass.notice.title", defaultTitle);
		String emailBody = getMessage("user.expiration.pass.expired.body", defaultMessage);
		log.info("Send email to {} in order to report password has expired due to the maximum time allowed for reset",
				masterUser.getEmail());
		mailService.sendMail(masterUser.getEmail(), emailTitle, emailBody);
	}

	private String getMessage(String key, String valueDefault) {
		try {
			return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Key:{} not found. Returns:{}", key, valueDefault);
			}			
			return valueDefault;
		}
	}

}

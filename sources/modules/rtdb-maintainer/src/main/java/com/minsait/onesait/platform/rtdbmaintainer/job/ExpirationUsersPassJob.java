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
public class ExpirationUsersPassJob {

	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;
	@Autowired
	private MessageSource messageSource;

	public void execute(JobExecutionContext context) throws IOException {

		final int timeLifePass = context.getJobDetail().getJobDataMap().getInt("timeLifePass");
		final int noticesDaysBefore = context.getJobDetail().getJobDataMap().getInt("noticesDaysBefore");
		final int maxInactiveDays = context.getJobDetail().getJobDataMap().getInt("maxInactiveDays");

		try {

			validateLifetimePassForUsers(timeLifePass, noticesDaysBefore, maxInactiveDays);

			log.debug("validate Lifetime Pass For Users");
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Rest error: code {}, {}", e.getStatusCode(), e.getResponseBodyAsString());
		} catch (final Exception e) {
			log.error("Error validate Lifetime Pass For Users", e);
		}
	}

	private void validateLifetimePassForUsers(int timeLifePass, int noticesDaysBefore, int maxInactiveDays) {

		List<MasterUser> users = multitenancyService.getUsers();
		Date now = new Date();
		Date inactiveLimitDate = subtractDays(now, maxInactiveDays);
		Date timeLifeDate = subtractDays(now, timeLifePass);
		Date noticesDateBeforeDate = subtractDays(now, timeLifePass - noticesDaysBefore);

		users.forEach((masterUser) -> {
			if (masterUser.isActive()) {
				// check inactive days
				log.debug("----------------------------------");
				if (log.isDebugEnabled()) {
					log.debug("maxInactiveDays :{}", maxInactiveDays);
					log.debug("timeLifePass :{}", timeLifePass);
					log.debug("noticesDaysBefore :{}", noticesDaysBefore);
	
					log.debug("inactiveLimitDate :{}", inactiveLimitDate);
					log.debug("timeLifeDate :{}", timeLifeDate);
					log.debug("noticesDateBeforeDate :{}", noticesDateBeforeDate);
	
					log.debug("USER :{}", masterUser.getUserId());
					log.debug("masterUser.getLastLogin() :{}", masterUser.getLastLogin());
					log.debug("masterUser.getLastPswdUpdate() :{}", masterUser.getLastPswdUpdate());
				}
				

				if (masterUser.getLastLogin() != null && masterUser.getLastLogin().before(inactiveLimitDate)
						&& maxInactiveDays >= 0) {

					if (log.isDebugEnabled()) {
						log.debug("masterUser.getLastLogin().before(inactiveLimitDate)): {}"
							, masterUser.getLastLogin().before(inactiveLimitDate));
						// block User and send mail
	
						log.info("----------------------------------");
						log.info("CAUSE :" + "disabled user due to inactivity");
						log.info("USER :{}", masterUser.getUserId());
						log.info("now :{}", now);
						log.info("maxInactiveDays :{}", maxInactiveDays);
						log.info("timeLifePass :{}", timeLifePass);
						log.info("noticesDaysBefore :{}", noticesDaysBefore);
	
						log.info("inactiveLimitDate :{}", inactiveLimitDate);
						log.info("timeLifeDate :{}", timeLifeDate);
						log.info("noticesDateBeforeDate :{}", noticesDateBeforeDate);
	
						log.info("masterUser.getLastLogin() :{}", masterUser.getLastLogin());
						log.info("masterUser.getLastPswdUpdate() :{}", masterUser.getLastPswdUpdate());
					}
					
					log.info("----------------------------------");

					userService.deactivateUser(masterUser.getUserId());
					sendInactivityEmail(masterUser);
				} else if (masterUser.getLastPswdUpdate() != null && masterUser.getLastPswdUpdate().before(timeLifeDate)
						&& timeLifePass >= 0) {
					log.debug(" masterUser.getLastPswdUpdate().before(timeLifeDate)): "
							+ masterUser.getLastPswdUpdate().before(timeLifeDate));
					// block User and send mail

					log.info("----------------------------------");
					log.info("CAUSE :" + "expired password");
					log.info("USER :" + masterUser.getUserId());
					log.info("now :" + now);
					log.info("maxInactiveDays :" + maxInactiveDays);
					log.info("timeLifePass :" + timeLifePass);
					log.info("noticesDaysBefore :" + noticesDaysBefore);

					log.info("inactiveLimitDate :" + inactiveLimitDate);
					log.info("timeLifeDate :" + timeLifeDate);
					log.info("noticesDateBeforeDate :" + noticesDateBeforeDate);

					log.info("masterUser.getLastLogin() :" + masterUser.getLastLogin());
					log.info("masterUser.getLastPswdUpdate() :" + masterUser.getLastPswdUpdate());
					log.info("----------------------------------");

					userService.deactivateUser(masterUser.getUserId());
					sendPasswordExpiredEmail(masterUser);
				} else if (masterUser.getLastPswdUpdate() != null
						&& masterUser.getLastPswdUpdate().before(noticesDateBeforeDate) && timeLifePass >= 0
						&& noticesDaysBefore >= 0) {
					log.debug("masterUser.getLastPswdUpdate().before(noticesDateBeforeDate): "
							+ masterUser.getLastPswdUpdate().before(noticesDateBeforeDate));
					// send mail
					sendPasswordExpirationEmail(masterUser,
							getDiffDates(noticesDateBeforeDate, masterUser.getLastPswdUpdate()));
				}
				log.debug("----------------------------------");
			}
		});
	}

	private int getDiffDates(Date dateA, Date dateB) {
		long diff = dateB.getTime() - dateA.getTime();
		return Math.abs((int) (diff / (24 * 60 * 60 * 1000)));
	}

	/**
	 * subtract days to date in java
	 * 
	 * @param date
	 * @param days
	 * @return
	 */
	private static Date subtractDays(Date date, int days) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, -days);
		return cal.getTime();
	}

	private void sendPasswordExpirationEmail(MasterUser masterUser, int remainingDays) {
		final String defaultTitle = "[Onesait Plaform] Password expiration";
		final String defaultMessage1 = "There are ";
		final String defaultMessage2 = " days left until your account password expires. For that reason, we ask you to change it as soon as possible";

		final String emailTitle = getMessage("user.expiration.pass.notice.title", defaultTitle);
		String emailMessage1 = getMessage("user.expiration.pass.notice.body1", defaultMessage1);
		String emailMessage2 = getMessage("user.expiration.pass.notice.body2", defaultMessage2);
		String emailBody = emailMessage1 + remainingDays + emailMessage2;

		log.info("Send email to {} in order to report next password expiration", masterUser.getEmail());
		mailService.sendMail(masterUser.getEmail(), emailTitle, emailBody);
	}

	private void sendPasswordExpiredEmail(MasterUser masterUser) {
		final String defaultTitle = "[Onesait Plaform] Password expiration";
		final String defaultMessage = "Your password has expired contact your administrator to update it";
		final String emailTitle = getMessage("user.expiration.pass.notice.title", defaultTitle);
		String emailBody = getMessage("user.expiration.pass.expired.body", defaultMessage);
		log.info("Send email to {} in order to report password has expired ", masterUser.getEmail());
		mailService.sendMail(masterUser.getEmail(), emailTitle, emailBody);
	}

	private void sendInactivityEmail(MasterUser masterUser) {
		final String defaultTitle = "[Onesait Plaform] Your password has expired due to inactivity";
		final String defaultMessage = "Your password has expired contact your administrator to update it";
		final String emailTitle = getMessage("user.expiration.inactivity.notice.title", defaultTitle);
		String emailBody = getMessage("user.expiration.pass.expired.body", defaultMessage);

		log.info("Send email to {} in order to report password has expired due to inactivity", masterUser.getEmail());
		mailService.sendMail(masterUser.getEmail(), emailTitle, emailBody);
	}

	private String getMessage(String key, String valueDefault) {
		try {
			return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
		} catch (final Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Key:{} not found. Returns:", key, valueDefault);
			}			
			return valueDefault;
		}
	}

}

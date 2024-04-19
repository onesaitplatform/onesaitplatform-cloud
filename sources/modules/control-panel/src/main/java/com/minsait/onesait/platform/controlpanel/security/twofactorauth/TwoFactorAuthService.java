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
package com.minsait.onesait.platform.controlpanel.security.twofactorauth;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(value = "onesaitplatform.authentication.twofa.enabled", havingValue = "true")
@Slf4j
public class TwoFactorAuthService {

	@Autowired
	@Qualifier("purgatoryCache")
	private Map<String, Verification> purgatoryCache;
	@Autowired
	private UserService userService;
	@Autowired
	private MailService mailService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserDetailsService userDetailsService;
	@Value("${onesaitplatform.authentication.twofa.purgatory_time_minutes}")
	private int purgatoryTime;

	@Scheduled(fixedDelay = 10000)
	public void cleanPurgatory() {
		final Date now = new Date();
		final Iterator<Entry<String, Verification>> iterator = purgatoryCache.entrySet().iterator();
		while (iterator.hasNext()) {
			final Entry<String, Verification> next = iterator.next();
			if (next.getValue().getExpirationDate().getTime() < now.getTime())
				purgatoryCache.remove(next.getKey());
		}

	}

	public boolean verify(String userId, String code) {
		if (purgatoryCache.get(userId) != null && purgatoryCache.get(userId).getCode().equals(code)) {
			purgatoryCache.remove(userId);
			return true;
		}
		return false;
	}

	public void newVerificationRequest(String userId) {
		if (purgatoryCache.get(userId) != null) {
			purgatoryCache.get(userId).setExpirationDate(new Date());
		} else {
			final Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.MINUTE, purgatoryTime);
			final String code = UUID.randomUUID().toString();
			purgatoryCache.put(userId,
					Verification.builder().code(code).userId(userId).expirationDate(c.getTime()).build());
			log.info("Code generated is : {}", code);
			sendCodeViaMail(userId, code);

		}

	}

	public void promoteToRealRole(Authentication authentication) {
		final UserDetails details = userDetailsService.loadUserByUsername(authentication.getName());

		final Authentication newAuthentication = new UsernamePasswordAuthenticationToken(details, details.getPassword(),
				details.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);

	}

	public boolean isUserInPurgatory(String userId) {
		return purgatoryCache.containsKey(userId);
	}

	private void sendCodeViaMail(String userId, String code) {
		final User user = userService.getUser(userId);
		final String message = utils.getMessage("user.auth.code", "Your generated code is:").concat(" " + code);
		final String subject = utils.getMessage("user.auth.code.subject", "Your onesait Platform code");
		try {
			mailService.sendMail(user.getEmail(), subject, message);
		} catch (final Exception e) {
			log.error("Could not send verification email ", e);
		}
	}
}

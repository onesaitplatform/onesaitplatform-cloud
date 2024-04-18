/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.monitoring.configs;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.RemindingNotifier;

@Configuration
@EnableScheduling
public class NotifierConfiguration {

	@Autowired
	private Notifier notifier;

	@Value("${onesaitplatform.reminder.periodInMins:5}")
	private long reminderPeriodinMins;

	@Value("${onesaitplatform.reminder.enabled:true}")
	private Boolean enabled;

	@Value("${onesaitplatform.reminder.statuses:DOWN}")
	private String statuses;

	@Primary
	@Bean
	public RemindingNotifier remindingNotifier() {
		RemindingNotifier myNotifier = new RemindingNotifier(notifier);
		myNotifier.setReminderPeriod(Duration.ofMinutes(reminderPeriodinMins).toMillis());
		myNotifier.setEnabled(enabled);
		myNotifier.setReminderStatuses(statuses.split(","));
		return myNotifier;
	}

	@Scheduled(fixedRateString = "${onesaitplatform.reminder.eachInMs}")
	public void remind() {
		remindingNotifier().sendReminders();
	}
}

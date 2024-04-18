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
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.LoggingNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;

@Configuration
@EnableScheduling
public class NotifierConfiguration {
	@Autowired
	private InstanceRepository repository;
	@Autowired
	private ObjectProvider<List<Notifier>> otherNotifiers;

	@Value("${onesaitplatform.reminder.periodInMins:5}")
	private long reminderPeriodinMins;

	@Value("${onesaitplatform.reminder.enabled:true}")
	private Boolean enabled;

	@Value("${onesaitplatform.reminder.statuses:DOWN}")
	private String statuses;

	@Value("${onesaitplatform.reminder.eachInMs}")
	private long reminderCheckInterval;

	@Bean
	public FilteringNotifier filteringNotifier() {
		final CompositeNotifier delegate = new CompositeNotifier(otherNotifiers.getIfAvailable(Collections::emptyList));
		return new FilteringNotifier(delegate, repository);
	}

	@Bean
	public LoggingNotifier notifier() {
		return new LoggingNotifier(repository);
	}

	@Primary
	@Bean
	public RemindingNotifier remindingNotifier() {
		final RemindingNotifier myNotifier = new RemindingNotifier(filteringNotifier(), repository);
		myNotifier.setReminderPeriod(Duration.ofMinutes(reminderPeriodinMins));
		myNotifier.setEnabled(enabled);
		myNotifier.setReminderStatuses(statuses.split(","));
		myNotifier.setCheckReminderInverval(Duration.ofSeconds(reminderCheckInterval));
		return myNotifier;
	}

}

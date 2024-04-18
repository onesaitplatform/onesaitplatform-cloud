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
package com.minsait.onesait.platform.streaming.twitter.listener.scheduler;

import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.listeners.SchedulerListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.services.twitter.TwitterListeningService;
import com.minsait.onesait.platform.streaming.twitter.service.TwitterStreamService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TwitterSchedulerListener extends SchedulerListenerSupport {

	@Autowired
	private TwitterStreamService twitterStreamService;

	@Autowired
	private TwitterListeningService twitterListeningService;

	@Override
	public void jobUnscheduled(TriggerKey triggerKey) {
		System.out.println(triggerKey);
	}

	@Override
	public void triggerFinalized(Trigger trigger) {
		System.out.println("Trigger finalized");
		TwitterListening twitterListening = this.twitterListeningService
				.getListeningByJobName(trigger.getJobKey().getName());
		try {
			twitterListening.setJobName(null);
			this.twitterListeningService.updateListening(twitterListening);
			this.twitterStreamService.unsubscribe(twitterListening.getId());

		} catch (Exception e) {

			log.info("Could not unsuscribe and close twitter listener with id: " + twitterListening.getId());
		}
	}

}

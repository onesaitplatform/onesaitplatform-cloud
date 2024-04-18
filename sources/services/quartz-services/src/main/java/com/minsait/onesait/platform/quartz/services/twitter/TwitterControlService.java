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
package com.minsait.onesait.platform.quartz.services.twitter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.services.twitter.TwitterListeningService;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

@Service
@Lazy
public class TwitterControlService {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TwitterListeningService twitterListeningService;

	public void scheduleTwitterListening(TwitterListening twitterListening) {

		TaskInfo task = new TaskInfo();
		task.setJobName(twitterListening.getId());
		task.setSchedulerType(SchedulerType.TWITTER);

		Map<String, Object> jobContext = new HashMap<>();
		jobContext.put("id", twitterListening.getId());
		jobContext.put("ontology", twitterListening.getOntology().getIdentification());
		jobContext.put("clientPlatform", twitterListening.getToken().getClientPlatform().getIdentification());
		jobContext.put("token", twitterListening.getToken().getTokenName());
		jobContext.put("topics", twitterListening.getTopics());
		jobContext.put("geolocation", false);
		jobContext.put("userId", twitterListening.getUser().getUserId());
		jobContext.put("timeout", 2);
		if (twitterListening.getConfiguration() != null) {
			jobContext.put("configurationId", twitterListening.getConfiguration().getId());
		} else {
			jobContext.put("configurationId", null);
		}

		task.setUsername(twitterListening.getUser().getUserId());
		task.setData(jobContext);
		task.setSingleton(false);
		task.setCronExpression("0/20 * * ? * * *");

		task.setStartAt(twitterListening.getDateFrom());
		task.setEndAt(twitterListening.getDateTo());
		ScheduleResponseInfo response = taskService.addJob(task);
		twitterListening.setJobName(response.getJobName());
		this.twitterListeningService.updateListening(twitterListening);

	}

	public void unscheduleTwitterListening(TwitterListening twitterListening) {
		TaskOperation operation = new TaskOperation();
		operation.setJobName(twitterListening.getJobName());
		if (operation.getJobName() != null) {
			this.taskService.unscheduled(operation);
		}

	}

	public void updateTwitterListening(TwitterListening twitterListening) {
		this.twitterListeningService.updateListening(twitterListening);
		twitterListening = this.twitterListeningService.getListenById(twitterListening.getId());
		this.unscheduleTwitterListening(twitterListening);
		if (twitterListening.getDateTo().getTime() > System.currentTimeMillis())
			this.scheduleTwitterListening(twitterListening);
	}

}

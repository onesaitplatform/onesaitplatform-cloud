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
package com.minsait.onesait.platform.scheduler.scheduler.bean;

import java.util.Date;
import java.util.Map;

import com.minsait.onesait.platform.scheduler.SchedulerType;

public class TaskInfo {
		
	private String jobName;
	private String username;
	private String cronExpression;	
	private SchedulerType schedulerType;
	private Map<String, Object> data;
	private boolean isSingleton;
	private Date startAt;
	private Date endAt;
	
	public TaskInfo () {
		
	}
	
	public TaskInfo(String jobName, String username, String cronExpression, SchedulerType schedulerType, Map<String, Object> data,
			boolean isSingleton, Date startAt, Date endAt) {
		super();
		this.jobName = jobName;
		this.username = username;
		this.cronExpression = cronExpression;
		this.schedulerType = schedulerType;
		this.data = data;
		this.isSingleton = isSingleton;
		this.startAt = startAt;
		this.endAt = endAt;
	}
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public SchedulerType getSchedulerType() {
		return schedulerType;
	}

	public void setSchedulerType(SchedulerType schedulerType) {
		this.schedulerType = schedulerType;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public boolean isSingleton() {
		return isSingleton;
	}

	public void setSingleton(boolean isSingleton) {
		this.isSingleton = isSingleton;
	}

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Date getEndAt() {
		return endAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

}

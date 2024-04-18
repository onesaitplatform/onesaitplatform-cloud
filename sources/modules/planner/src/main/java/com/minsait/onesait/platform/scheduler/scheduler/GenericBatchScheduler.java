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
package com.minsait.onesait.platform.scheduler.scheduler;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

public class GenericBatchScheduler implements BatchScheduler {
	
	private Scheduler scheduler;
	private String name;
	
	public GenericBatchScheduler (Scheduler scheduler, String name) {
		this.scheduler = scheduler;
		this.name = name;
	}

	@Override
	public void addCalendar(String arg0, Calendar arg1, boolean arg2, boolean arg3) throws SchedulerException {
		scheduler.addCalendar(arg0, arg1, arg2, arg3);
		
	}

	@Override
	public void addJob(JobDetail arg0, boolean arg1) throws SchedulerException {
		scheduler.addJob(arg0, arg1);
		
	}

	@Override
	public void addJob(JobDetail arg0, boolean arg1, boolean arg2) throws SchedulerException {
		scheduler.addJob(arg0, arg1, arg2);
	}

	@Override
	public boolean checkExists(JobKey arg0) throws SchedulerException {
		return scheduler.checkExists(arg0);
	}

	@Override
	public boolean checkExists(TriggerKey arg0) throws SchedulerException {
		return scheduler.checkExists(arg0);
	}

	@Override
	public void clear() throws SchedulerException {
		scheduler.clear();
	}

	@Override
	public boolean deleteCalendar(String arg0) throws SchedulerException {
		return scheduler.deleteCalendar(arg0);
	}

	@Override
	public boolean deleteJob(JobKey jobKey) throws SchedulerException {
		return this.scheduler.deleteJob(jobKey);
	}

	@Override
	public boolean deleteJobs(List<JobKey> keys) throws SchedulerException {
		return this.scheduler.deleteJobs(keys);
	}

	@Override
	public Calendar getCalendar(String cal) throws SchedulerException {
		return this.scheduler.getCalendar(cal);
	}

	@Override
	public List<String> getCalendarNames() throws SchedulerException {
		return this.scheduler.getCalendarNames();
	}

	@Override
	public SchedulerContext getContext() throws SchedulerException {
		return this.scheduler.getContext();
	}

	@Override
	public List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException {
		return this.scheduler.getCurrentlyExecutingJobs();
	}

	@Override
	public JobDetail getJobDetail(JobKey jobKey) throws SchedulerException {
		return this.scheduler.getJobDetail(jobKey);
	}

	@Override
	public List<String> getJobGroupNames() throws SchedulerException {
		return this.scheduler.getJobGroupNames();
	}

	@Override
	public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
		return this.scheduler.getJobKeys(matcher);
	}

	@Override
	public ListenerManager getListenerManager() throws SchedulerException {
		return this.scheduler.getListenerManager();
	}

	@Override
	public SchedulerMetaData getMetaData() throws SchedulerException {
		return this.scheduler.getMetaData();
	}

	@Override
	public Set<String> getPausedTriggerGroups() throws SchedulerException {
		return this.scheduler.getPausedTriggerGroups();
	}

	@Override
	public String getSchedulerInstanceId() throws SchedulerException {
		return this.scheduler.getSchedulerInstanceId();
	}

	@Override
	public String getSchedulerName() throws SchedulerException {
		return this.scheduler.getSchedulerName();
	}

	@Override
	public Trigger getTrigger(TriggerKey triggerKey) throws SchedulerException {
		return this.scheduler.getTrigger(triggerKey);
	}

	@Override
	public List<String> getTriggerGroupNames() throws SchedulerException {
		return this.scheduler.getTriggerGroupNames();
	}

	@Override
	public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
		return this.scheduler.getTriggerKeys(matcher);
	}

	@Override
	public TriggerState getTriggerState(TriggerKey triggerKey) throws SchedulerException {
		return this.scheduler.getTriggerState(triggerKey);
	}

	@Override
	public List<? extends Trigger> getTriggersOfJob(JobKey jobKey) throws SchedulerException {
		return this.scheduler.getTriggersOfJob(jobKey);
	}

	@Override
	public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
		return this.scheduler.interrupt(jobKey);
	}

	@Override
	public boolean interrupt(String arg0) throws UnableToInterruptJobException {
		return this.scheduler.interrupt(arg0);
	}

	@Override
	public boolean isInStandbyMode() throws SchedulerException {
		return this.scheduler.isInStandbyMode();
	}

	@Override
	public boolean isShutdown() throws SchedulerException {
		return this.scheduler.isShutdown();
	}

	@Override
	public boolean isStarted() throws SchedulerException {
		return this.scheduler.isStarted();
	}

	@Override
	public void pauseAll() throws SchedulerException {
		this.scheduler.pauseAll();
		
	}

	@Override
	public void pauseJob(JobKey jobKey) throws SchedulerException {
		this.scheduler.pauseJob(jobKey);
	}

	@Override
	public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
		this.scheduler.pauseJobs(matcher);
	}

	@Override
	public void pauseTrigger(TriggerKey triggerKey) throws SchedulerException {
		this.scheduler.pauseTrigger(triggerKey);
	}

	@Override
	public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
		this.scheduler.pauseTriggers(matcher);
	}

	@Override
	public Date rescheduleJob(TriggerKey triggerKey, Trigger trigger) throws SchedulerException {
		return this.scheduler.rescheduleJob(triggerKey, trigger);
	}

	@Override
	public void resumeAll() throws SchedulerException {
		this.scheduler.resumeAll();
	}

	@Override
	public void resumeJob(JobKey jobKey) throws SchedulerException {
		this.scheduler.resumeJob(jobKey);
	}

	@Override
	public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
		this.scheduler.resumeJobs(matcher);
	}

	@Override
	public void resumeTrigger(TriggerKey triggerKey) throws SchedulerException {
		this.scheduler.resumeTrigger(triggerKey);
	}

	@Override
	public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
		this.scheduler.resumeTriggers(matcher);
	}

	@Override
	public Date scheduleJob(Trigger trigger) throws SchedulerException {
		return this.scheduler.scheduleJob(trigger);
	}

	@Override
	public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		return this.scheduler.scheduleJob(jobDetail, trigger);
	}

	@Override
	public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggers, boolean arg2) throws SchedulerException {
		this.scheduler.scheduleJob(jobDetail, triggers, arg2);
	}

	@Override
	public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> arg0, boolean arg1) throws SchedulerException {
		this.scheduler.scheduleJobs(arg0, arg1);
	}

	@Override
	public void setJobFactory(JobFactory factory) throws SchedulerException {
		this.scheduler.setJobFactory(factory);
	}

	@Override
	public void shutdown() throws SchedulerException {
		this.scheduler.shutdown();
	}

	@Override
	public void shutdown(boolean arg0) throws SchedulerException {
		this.scheduler.shutdown(arg0);
	}

	@Override
	public void standby() throws SchedulerException {
		this.scheduler.standby();
	}

	@Override
	public void start() throws SchedulerException {
		this.scheduler.start();
	}

	@Override
	public void startDelayed(int arg0) throws SchedulerException {
		this.scheduler.startDelayed(arg0);
	}

	@Override
	public void triggerJob(JobKey jobKey) throws SchedulerException {
		this.scheduler.triggerJob(jobKey);
	}

	@Override
	public void triggerJob(JobKey jobKey, JobDataMap jobData) throws SchedulerException {
		this.scheduler.triggerJob(jobKey, jobData);
	}

	@Override
	public boolean unscheduleJob(TriggerKey triggerKey) throws SchedulerException {
		return this.scheduler.unscheduleJob(triggerKey);
	}

	@Override
	public boolean unscheduleJobs(List<TriggerKey> triggerKeys) throws SchedulerException {
		return this.scheduler.unscheduleJobs(triggerKeys);
	}

	@Override
	public String getName() {
		return this.name;
	}

}

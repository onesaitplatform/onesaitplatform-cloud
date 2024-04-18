/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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

	private final Scheduler scheduler;
	private final String name;

	public GenericBatchScheduler(Scheduler scheduler, String name) {
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
		return scheduler.deleteJob(jobKey);
	}

	@Override
	public boolean deleteJobs(List<JobKey> keys) throws SchedulerException {
		return scheduler.deleteJobs(keys);
	}

	@Override
	public Calendar getCalendar(String cal) throws SchedulerException {
		return scheduler.getCalendar(cal);
	}

	@Override
	public List<String> getCalendarNames() throws SchedulerException {
		return scheduler.getCalendarNames();
	}

	@Override
	public SchedulerContext getContext() throws SchedulerException {
		return scheduler.getContext();
	}

	@Override
	public List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException {
		return scheduler.getCurrentlyExecutingJobs();
	}

	@Override
	public JobDetail getJobDetail(JobKey jobKey) throws SchedulerException {
		return scheduler.getJobDetail(jobKey);
	}

	@Override
	public List<String> getJobGroupNames() throws SchedulerException {
		return scheduler.getJobGroupNames();
	}

	@Override
	public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
		return scheduler.getJobKeys(matcher);
	}

	@Override
	public ListenerManager getListenerManager() throws SchedulerException {
		return scheduler.getListenerManager();
	}

	@Override
	public SchedulerMetaData getMetaData() throws SchedulerException {
		return scheduler.getMetaData();
	}

	@Override
	public Set<String> getPausedTriggerGroups() throws SchedulerException {
		return scheduler.getPausedTriggerGroups();
	}

	@Override
	public String getSchedulerInstanceId() throws SchedulerException {
		return scheduler.getSchedulerInstanceId();
	}

	@Override
	public String getSchedulerName() throws SchedulerException {
		return scheduler.getSchedulerName();
	}

	@Override
	public Trigger getTrigger(TriggerKey triggerKey) throws SchedulerException {
		return scheduler.getTrigger(triggerKey);
	}

	@Override
	public List<String> getTriggerGroupNames() throws SchedulerException {
		return scheduler.getTriggerGroupNames();
	}

	@Override
	public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
		return scheduler.getTriggerKeys(matcher);
	}

	@Override
	public TriggerState getTriggerState(TriggerKey triggerKey) throws SchedulerException {
		return scheduler.getTriggerState(triggerKey);
	}

	@Override
	public List<? extends Trigger> getTriggersOfJob(JobKey jobKey) throws SchedulerException {
		return scheduler.getTriggersOfJob(jobKey);
	}

	@Override
	public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
		return scheduler.interrupt(jobKey);
	}

	@Override
	public boolean interrupt(String arg0) throws UnableToInterruptJobException {
		return scheduler.interrupt(arg0);
	}

	@Override
	public boolean isInStandbyMode() throws SchedulerException {
		return scheduler.isInStandbyMode();
	}

	@Override
	public boolean isShutdown() throws SchedulerException {
		return scheduler.isShutdown();
	}

	@Override
	public boolean isStarted() throws SchedulerException {
		return scheduler.isStarted();
	}

	@Override
	public void pauseAll() throws SchedulerException {
		scheduler.pauseAll();

	}

	@Override
	public void pauseJob(JobKey jobKey) throws SchedulerException {
		scheduler.pauseJob(jobKey);
	}

	@Override
	public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
		scheduler.pauseJobs(matcher);
	}

	@Override
	public void pauseTrigger(TriggerKey triggerKey) throws SchedulerException {
		scheduler.pauseTrigger(triggerKey);
	}

	@Override
	public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
		scheduler.pauseTriggers(matcher);
	}

	@Override
	public Date rescheduleJob(TriggerKey triggerKey, Trigger trigger) throws SchedulerException {
		return scheduler.rescheduleJob(triggerKey, trigger);
	}

	@Override
	public void resumeAll() throws SchedulerException {
		scheduler.resumeAll();
	}

	@Override
	public void resumeJob(JobKey jobKey) throws SchedulerException {
		scheduler.resumeJob(jobKey);
	}

	@Override
	public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
		scheduler.resumeJobs(matcher);
	}

	@Override
	public void resumeTrigger(TriggerKey triggerKey) throws SchedulerException {
		scheduler.resumeTrigger(triggerKey);
	}

	@Override
	public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
		scheduler.resumeTriggers(matcher);
	}

	@Override
	public Date scheduleJob(Trigger trigger) throws SchedulerException {
		return scheduler.scheduleJob(trigger);
	}

	@Override
	public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		return scheduler.scheduleJob(jobDetail, trigger);
	}

	@Override
	public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggers, boolean arg2)
			throws SchedulerException {
		scheduler.scheduleJob(jobDetail, triggers, arg2);
	}

	@Override
	public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> arg0, boolean arg1) throws SchedulerException {
		scheduler.scheduleJobs(arg0, arg1);
	}

	@Override
	public void setJobFactory(JobFactory factory) throws SchedulerException {
		scheduler.setJobFactory(factory);
	}

	@Override
	public void shutdown() throws SchedulerException {
		scheduler.shutdown();
	}

	@Override
	public void shutdown(boolean arg0) throws SchedulerException {
		scheduler.shutdown(arg0);
	}

	@Override
	public void standby() throws SchedulerException {
		scheduler.standby();
	}

	@Override
	public void start() throws SchedulerException {
		scheduler.start();
	}

	@Override
	public void startDelayed(int arg0) throws SchedulerException {
		scheduler.startDelayed(arg0);
	}

	@Override
	public void triggerJob(JobKey jobKey) throws SchedulerException {
		scheduler.triggerJob(jobKey);
	}

	@Override
	public void triggerJob(JobKey jobKey, JobDataMap jobData) throws SchedulerException {
		scheduler.triggerJob(jobKey, jobData);
	}

	@Override
	public boolean unscheduleJob(TriggerKey triggerKey) throws SchedulerException {
		return scheduler.unscheduleJob(triggerKey);
	}

	@Override
	public boolean unscheduleJobs(List<TriggerKey> triggerKeys) throws SchedulerException {
		return scheduler.unscheduleJobs(triggerKeys);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void resetTriggerFromErrorState(TriggerKey arg0) throws SchedulerException {
		// TODO Auto-generated method stub

	}

}

/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.scheduler.scheduler.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.domain.ScheduledJob;
import com.minsait.onesait.platform.scheduler.exception.BatchSchedulerException;
import com.minsait.onesait.platform.scheduler.job.BatchGenericJob;
import com.minsait.onesait.platform.scheduler.job.JobParamNames;
import com.minsait.onesait.platform.scheduler.scheduler.BatchScheduler;
import com.minsait.onesait.platform.scheduler.scheduler.bean.ListTaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.generator.JobGenerator;
import com.minsait.onesait.platform.scheduler.scheduler.generator.TriggerGenerator;
import com.minsait.onesait.platform.scheduler.scheduler.service.BatchSchedulerFactory;
import com.minsait.onesait.platform.scheduler.scheduler.service.ScheduledJobService;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;
import com.minsait.onesait.platform.scheduler.util.DateUtil;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

	private static final String NOT_FOUND = " not found";
	private static final String JOB_WITH_NAME = "Job with name ";
	private static final String GROUP_NAME = "groupName: ";

	@Autowired
	private BatchSchedulerFactory batchSchedulerFactory;

	@Autowired
	private TriggerGenerator triggerGenerator;

	@Autowired
	private JobGenerator jobGenerator;

	@Autowired
	private ScheduledJobService scheduledJobService;

	@Autowired
	private UserService userService;

	@Override
	public List<ListTaskInfo> list(String username) {

		List<ListTaskInfo> list = new ArrayList<>();

		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userService.getUser(userId);

		if (!userService.isUserAdministrator(user) && !username.equals(user.getUserId())) {
			return (new ArrayList<>());
		}

		log.info("get task list for user " + username);

		try {

			for (BatchScheduler scheduler : batchSchedulerFactory.getSchedulers()) {

				for (String groupJob : scheduler.getJobGroupNames()) {

					for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(groupJob))) {

						List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

						for (Trigger trigger : triggers) {

							TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
							JobDetail jobDetail = scheduler.getJobDetail(jobKey);

							String cronExpression = "";

							if (trigger instanceof CronTrigger) {
								CronTrigger cronTrigger = (CronTrigger) trigger;
								cronExpression = cronTrigger.getCronExpression();
							}

							log.info(jobKey.getName() + "[" + triggerState.name() + "]");

							String status = ("NORMAL".equals(triggerState.name())) ? "EXECUTING" : triggerState.name();

							ListTaskInfo info = new ListTaskInfo(jobKey.getName(), jobKey.getGroup(),
									jobDetail.getDescription(), status, cronExpression,
									SchedulerType.fromString(scheduler.getSchedulerName()).toString(),
									DateUtil.parseDate(trigger.getStartTime()),
									DateUtil.parseDate(trigger.getNextFireTime()),
									DateUtil.parseDate(trigger.getPreviousFireTime()));

							list.add(info);
						}
					}
				}
			}

		} catch (SchedulerException e) {
			log.error("Error listing task", e);
		}

		if (user == null) {
			log.error("The user doesnt exists so return empty list");
			list = new ArrayList<>();
		} else {

			if (!userService.isUserAdministrator(user)) {
				// if the user is not administrator we have to filter the jobs
				List<ScheduledJob> userJobs = scheduledJobService.getScheduledJobsByUsername(username);
				List<String> userJobsId = userJobs.stream().map(x -> x.getJobName()).collect(Collectors.toList());

				list = list.stream().filter(x -> userJobsId.contains(x.getJobName())).collect(Collectors.toList());
			}
		}
		return list;
	}

	@Override
	public ScheduleResponseInfo addJob(TaskInfo info) {
		String jobName = generateJobName(info);
		String jobGroup = generateGroupName(info.getSchedulerType());
		String cronExpression = info.getCronExpression();
		String jobDescription = "";

		log.info("add job [jobname: " + jobName + ", groupName: " + jobGroup + "]");

		try {

			BatchScheduler scheduler = batchSchedulerFactory.getScheduler(info.getSchedulerType());

			if (checkExists(new TaskOperation(jobName))) {
				log.info("AddJob fails, job already exist, jobGroup:{}, jobName:{}", jobGroup, jobName);
				throw new BatchSchedulerException(String.format("Job, jobName:{%s},jobGroup:{%s}", jobName, jobGroup));
			}

			JobDataMap jobDataMap = initTaskDataMap(info);

			JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

			JobDetail jobDetail = jobGenerator.createJobDetail(jobKey, jobDataMap, BatchGenericJob.class,
					jobDescription);

			TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);

			Trigger trigger = null;

			if (cronExpression != null && !"".equals(cronExpression)) {
				trigger = triggerGenerator.createCronTrigger(cronExpression, jobDetail, triggerKey, info.getStartAt(),
						info.getEndAt());
			} else {
				trigger = triggerGenerator.createTrigger(jobDetail, triggerKey, info.getStartAt(), info.getEndAt());
			}

			Date date = scheduler.scheduleJob(jobDetail, trigger);

			ScheduledJob job = new ScheduledJob(info.getUsername(), jobName, jobGroup,
					info.getSchedulerType().toString(), info.isSingleton());

			scheduledJobService.createScheduledJob(job);

			log.info("job added. Date: {}", date.toString());

		} catch (SchedulerException | BatchSchedulerException | NotFoundException e) {
			log.error("Error adding task", e);
			return new ScheduleResponseInfo(false, e.getMessage(), null);
		}

		return new ScheduleResponseInfo(true, "", jobName);
	}

	private String generateJobName(TaskInfo info) {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		String jobName = info.getJobName() + "-" + info.getSchedulerType().toString();

		if (!info.isSingleton()) {
			jobName += "-" + format.format(Calendar.getInstance().getTime());
		}

		return jobName;
	}

	private String generateGroupName(SchedulerType schedulerType) {
		return schedulerType.toString() + "-Group";
	}

	@Override
	public boolean unscheduled(TaskOperation operation) {

		boolean unscheduled = false;

		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userService.getUser(userId);

		String jobName = operation.getJobName();

		try {

			log.info("unschedule job [jobname: " + jobName + "]");

			ScheduledJob job = scheduledJobService.findByJobName(jobName);

			if (!userService.isUserAdministrator(user) && !job.getUserId().equals(user.getUserId())) {
				log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				throw new NotFoundException(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
			}

			if (job != null) {

				String groupName = job.getGroupName();

				log.info("unschedule job [" + " groupName: " + groupName + "]");

				if (checkExists(operation)) {

					TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);

					BatchScheduler scheduler = batchSchedulerFactory.getScheduler(job.getSchedulerId());

					scheduler.pauseTrigger(triggerKey);
					unscheduled = scheduler.unscheduleJob(triggerKey);
					if (unscheduled) {
						scheduler.deleteJob(new JobKey(jobName, groupName));
						scheduledJobService.deleteById(job.getId());
					}
					log.info("unschedule, triggerKey:{}", triggerKey);

				} else {
					log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				}

			}

		} catch (SchedulerException | NotFoundException e) {
			log.error("Error unscheduled task", e);
		}

		return unscheduled;
	}

	@Override
	public boolean unscheduledFromAnonymous(TaskOperation operation) {

		boolean unscheduled = false;

		String jobName = operation.getJobName();

		try {

			log.info("unschedule job [jobname: " + jobName + "]");

			ScheduledJob job = scheduledJobService.findByJobName(jobName);

			if (job != null) {

				String groupName = job.getGroupName();

				log.info("unschedule job [" + " groupName: " + groupName + "]");

				if (checkExists(operation)) {

					TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);

					BatchScheduler scheduler = batchSchedulerFactory.getScheduler(job.getSchedulerId());

					scheduler.pauseTrigger(triggerKey);
					unscheduled = scheduler.unscheduleJob(triggerKey);
					if (unscheduled) {
						scheduler.deleteJob(new JobKey(jobName, groupName));
						scheduledJobService.deleteById(job.getId());
					}
					log.info("unschedule, triggerKey:{}", triggerKey);

				} else {
					log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				}

			}

		} catch (SchedulerException | NotFoundException e) {
			log.error("Error unscheduled task", e);
		}

		return unscheduled;
	}

	@Override
	public boolean pause(TaskOperation operation) {

		boolean paused = false;

		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userService.getUser(userId);

		String jobName = operation.getJobName();

		try {

			log.info("pause job [jobname: " + jobName + "]");

			ScheduledJob job = scheduledJobService.findByJobName(jobName);

			if (!userService.isUserAdministrator(user) && !job.getUserId().equals(user.getUserId())) {
				log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				throw new NotFoundException(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
			}

			if (job != null) {

				String groupName = job.getGroupName();

				log.info("pause job [" + GROUP_NAME + groupName + "]");

				if (checkExists(operation)) {
					TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);

					BatchScheduler scheduler = batchSchedulerFactory.getScheduler(job.getSchedulerId());

					scheduler.pauseTrigger(triggerKey);
					log.info("Pause success, triggerKey:{}", triggerKey);
					paused = true;

				} else {
					log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				}
			}

		} catch (SchedulerException | NotFoundException e) {
			log.error("Error pause task", e);
		}

		return paused;
	}

	@Override
	public boolean resume(TaskOperation operation) {

		boolean resumed = false;

		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userService.getUser(userId);

		String jobName = operation.getJobName();

		try {

			log.info("resume job [jobname: " + jobName + "]");

			ScheduledJob job = scheduledJobService.findByJobName(jobName);

			if (!userService.isUserAdministrator(user) && !job.getUserId().equals(user.getUserId())) {
				log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				throw new NotFoundException(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
			}

			if (job != null) {

				String groupName = job.getGroupName();

				log.info("resume job [" + GROUP_NAME + groupName + "]");

				BatchScheduler scheduler = batchSchedulerFactory.getScheduler(job.getSchedulerId());

				if (checkExists(operation)) {

					TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);
					scheduler.resumeTrigger(triggerKey);
					log.info("Resume success, triggerKey:{}", triggerKey);
					resumed = true;
				} else {
					log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
				}
			}

		} catch (SchedulerException | NotFoundException e) {
			log.error("Error resume task", e);
		}

		return resumed;
	}

	@Override
	public boolean checkExists(TaskOperation operation) {

		boolean exists = false;
		String jobName = operation.getJobName();

		try {
			log.info("check if exists job [jobname: " + jobName + "]");
			ScheduledJob job = scheduledJobService.findByJobName(jobName);

			if (job != null) {

				String groupName = job.getGroupName();

				log.info("check if exists job [" + GROUP_NAME + groupName + "]");

				BatchScheduler scheduler = batchSchedulerFactory.getScheduler(job.getSchedulerId());

				TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);
				exists = scheduler.checkExists(triggerKey);
			} else {
				log.info(JOB_WITH_NAME + operation.getJobName() + NOT_FOUND);
			}

		} catch (SchedulerException | NotFoundException e) {
			log.error("Error checking if a job exists ", e);
		}

		return exists;
	}

	@Override
	public JobDataMap initTaskDataMap(TaskInfo info) {
		JobDataMap jobDataMap = new JobDataMap();

		jobDataMap.put(JobParamNames.USERNAME, info.getUsername());
		jobDataMap.put(JobParamNames.SCHEDULER_TYPE, info.getSchedulerType());

		if (info.getData() != null) {
			jobDataMap.putAll(info.getData());
		}
		return jobDataMap;
	}

}

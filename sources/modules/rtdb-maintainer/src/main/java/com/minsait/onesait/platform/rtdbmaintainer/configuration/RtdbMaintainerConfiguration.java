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
package com.minsait.onesait.platform.rtdbmaintainer.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.objectstorage.ObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RtdbMaintainerConfiguration implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	ConfigurationService configurationService;
	@Autowired
	TaskService taskService;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectStorageService objectStorageService;

	@Value("${onesaitplatform.database.configdb.export.minio.enabled:true}")
	private Boolean minioEnabled;

	private static final String JOB_NAME = "RtdbMaintainerJob";
	private static final String JOB_EXPIRATION_USERS_PASS = "ExpirationUsersPassJob";
	private static final String JOB_EXPIRATION_RESET_USERS_PASS = "ExpirationResetUsersPassJob";
	private static final String JOB_BACKUP_MINIO = "BackupMinioJob";
	private static final String SYSADMIN_USER = "sysadmin";

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {

		initializeRtdbMaintainerJob();

		initializeUsersPassJob();

		initializeResetUsersPassJob();
		if (minioEnabled) {
			initializeBackupMinioJob();
		}

	}

	private void initializeRtdbMaintainerJob() {

		final Configuration configuration = configurationService.getConfiguration(Configuration.Type.SCHEDULING,
				"default", null);

		@SuppressWarnings("unchecked")
		final Map<String, Object> ymlConfig = (Map<String, Object>) configurationService
		.fromYaml(configuration.getYmlConfig()).get("RtdbMaintainer");
		final String cron = (String) ymlConfig.get("cron");
		final TimeUnit timeUnit = TimeUnit.valueOf((String) ymlConfig.get("timeUnit"));
		final long timeout = ((Integer) ymlConfig.get("timeout")).longValue();

		final TaskOperation taskOperation = new TaskOperation();
		taskOperation.setJobName(JOB_NAME + "-" + SchedulerType.BATCH.toString());
		if (taskService.checkExists(taskOperation)) {
			taskService.unscheduledFromAnonymous(taskOperation);
		}
		final TaskInfo task = new TaskInfo();
		task.setSchedulerType(SchedulerType.BATCH);
		task.setCronExpression(cron);
		task.setSingleton(true);
		task.setJobName(JOB_NAME);
		task.setUsername("administrator");
		final Map<String, Object> jobContext = new HashMap<>();
		jobContext.put("timeout", timeout);
		jobContext.put("timeUnit", timeUnit);
		task.setData(jobContext);
		taskService.addJob(task);

	}

	private void initializeUsersPassJob() {

		final Configuration configuration = configurationService.getConfiguration(Configuration.Type.EXPIRATIONUSERS,
				"default", null);

		@SuppressWarnings("unchecked")
		final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
		.fromYaml(configuration.getYmlConfig()).get("ExpirationUsersPass");
		final String cron = (String) ymlExpirationUsersPassConfig.get("cron");
		final int timeLifePass = ((Integer) ymlExpirationUsersPassConfig.get("timeLifePass")).intValue();
		final int noticesDaysBefore = ((Integer) ymlExpirationUsersPassConfig.get("noticesDaysBefore")).intValue();
		final int maxInactiveDays = ((Integer) ymlExpirationUsersPassConfig.get("maxInactiveDays")).intValue();

		// expiration task manage passwords and users
		final TaskOperation taskManageUsersOperation = new TaskOperation();
		taskManageUsersOperation.setJobName(JOB_EXPIRATION_USERS_PASS + "-" + SchedulerType.EXPIRATIONUSERS.toString());

		if (taskService.checkExists(taskManageUsersOperation)) {
			taskService.unscheduledFromAnonymous(taskManageUsersOperation);
		}

		final TaskInfo task = new TaskInfo();
		task.setSchedulerType(SchedulerType.EXPIRATIONUSERS);
		task.setCronExpression(cron);
		task.setSingleton(true);
		task.setJobName(JOB_EXPIRATION_USERS_PASS);
		task.setUsername("administrator");
		final Map<String, Object> jobContext = new HashMap<>();
		jobContext.put("timeLifePass", timeLifePass);
		jobContext.put("noticesDaysBefore", noticesDaysBefore);
		jobContext.put("maxInactiveDays", maxInactiveDays);
		task.setData(jobContext);
		taskService.addJob(task);
	}

	private void initializeResetUsersPassJob() {

		final Configuration configuration = configurationService.getConfiguration(Configuration.Type.EXPIRATIONUSERS,
				"default", null);

		@SuppressWarnings("unchecked")
		final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
		.fromYaml(configuration.getYmlConfig()).get("ResetUserPass");
		final String cron = (String) ymlExpirationUsersPassConfig.get("cron");
		final int hours = ((Integer) ymlExpirationUsersPassConfig.get("hours")).intValue();

		// expiration task manage passwords and users
		final TaskOperation taskManageUsersOperation = new TaskOperation();
		taskManageUsersOperation
		.setJobName(JOB_EXPIRATION_RESET_USERS_PASS + "-" + SchedulerType.EXPIRATIONRESETUSER.toString());

		if (taskService.checkExists(taskManageUsersOperation)) {
			taskService.unscheduledFromAnonymous(taskManageUsersOperation);
		}
		// one minute for test "0 0/1 * 1/1 * ? *"

		final TaskInfo task = new TaskInfo();
		task.setSchedulerType(SchedulerType.EXPIRATIONRESETUSER);
		task.setCronExpression(cron);
		task.setSingleton(true);
		task.setJobName(JOB_EXPIRATION_RESET_USERS_PASS);
		task.setUsername("administrator");
		final Map<String, Object> jobContext = new HashMap<>();
		jobContext.put("hours", hours);
		task.setData(jobContext);
		taskService.addJob(task);
	}

	private void initializeBackupMinioJob() {

		final Configuration configuration = configurationService.getConfiguration(Configuration.Type.SCHEDULING,
				"default", null);

		@SuppressWarnings("unchecked")
		final Map<String, Object> ymlConfig = (Map<String, Object>) configurationService
		.fromYaml(configuration.getYmlConfig()).get("BackupMinio");
		final String cron = (String) ymlConfig.get("cron");
		final TimeUnit timeUnit = TimeUnit.valueOf((String) ymlConfig.get("timeUnit"));
		final long timeout = ((Integer) ymlConfig.get("timeout")).longValue();

		final TaskOperation taskOperation = new TaskOperation();
		taskOperation.setJobName(JOB_BACKUP_MINIO + "-" + SchedulerType.BACKUPMINIO.toString());
		if (taskService.checkExists(taskOperation)) {
			taskService.unscheduledFromAnonymous(taskOperation);
		}
		final TaskInfo task = new TaskInfo();
		task.setSchedulerType(SchedulerType.BACKUPMINIO);
		task.setCronExpression(cron);
		task.setSingleton(true);
		task.setJobName(JOB_BACKUP_MINIO);
		task.setUsername(SYSADMIN_USER);
		final Map<String, Object> jobContext = new HashMap<>();
		jobContext.put("timeout", timeout);
		jobContext.put("timeUnit", timeUnit);
		task.setData(jobContext);
		taskService.addJob(task);

		createSysAdminBucket();
	}

	private void createSysAdminBucket() {
		try {
			final String superUserToken = objectStorageService.logIntoObjectStorageWithSuperUser();
			if (!objectStorageService.existUserInObjectStore(superUserToken, SYSADMIN_USER)) {

				final String requesterUserToken = userTokenService.getToken(userService.getUser(SYSADMIN_USER)).getToken();

				objectStorageService.createBucketForUser(SYSADMIN_USER);
				objectStorageService.createPolicyForBucketUser(superUserToken, SYSADMIN_USER);
				objectStorageService.createUserInObjectStore(superUserToken, SYSADMIN_USER, requesterUserToken);
			}

		} catch (ObjectStoreLoginException | ObjectStoreBucketCreateException | ObjectStoreCreateUserException
				| ObjectStoreCreatePolicyException e) {
			log.error("Error creating user or bucket in Object store", e);
		} catch (final Exception e) {
			log.error("Error initializing Minio Job", e);
		}
	}

}

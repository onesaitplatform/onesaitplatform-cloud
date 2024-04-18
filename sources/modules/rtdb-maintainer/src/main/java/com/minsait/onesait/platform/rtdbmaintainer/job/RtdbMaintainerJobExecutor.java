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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.job.BatchGenericExecutor;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RtdbMaintainerJobExecutor implements BatchGenericExecutor {

	@Autowired
	RtdbMaintainerJob rtdbMaintainerJob;

	@Autowired
	OKPIJob oKPIJob;
	@Autowired
	ExpirationUsersPassJob expirationUsersPassJob;
	@Autowired
	ExpirationResetUsersPassJob expirationResetUsersPassJob;
	@Autowired
	ProcessExecutionJob processExecutionJob;
	@Autowired
	BackupMinioJob backupMinio;

	private static final String OKPI_JOB_KEY = "Ontology KPI";
	private static final String JOB_EXPIRATION_USERS_PASS = "ExpirationUsersPassJob";
	private static final String JOB_EXPIRATION_RESET_USERS_PASS = "ExpirationResetUsersPassJob";
	private static final String PROCESS_EXECUTION_KEY = "ProcessExecution";
	private static final String BACKUP_MINIO_KEY = "BackupMinio";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			if (context.getJobDetail().getKey().toString().contains(OKPI_JOB_KEY)) {
				oKPIJob.execute(context);
			} else if (context.getJobDetail().getKey().toString().contains(JOB_EXPIRATION_USERS_PASS)) {
				expirationUsersPassJob.execute(context);
			} else if (context.getJobDetail().getKey().toString().contains(JOB_EXPIRATION_RESET_USERS_PASS)) {
				expirationResetUsersPassJob.execute(context);
			} else if (context.getJobDetail().getKey().toString().contains(PROCESS_EXECUTION_KEY)) {
				processExecutionJob.execute(context);
			} else if (context.getJobDetail().getKey().toString().contains(BACKUP_MINIO_KEY)) {
				backupMinio.execute(context);
			} else {
				rtdbMaintainerJob.execute(context);
			}

			log.info("Executed");
		} catch (final Exception e) {
			log.error("Not executed" + e.getMessage(), e);
		}

	}

}

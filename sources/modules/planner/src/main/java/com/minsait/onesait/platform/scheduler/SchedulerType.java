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
package com.minsait.onesait.platform.scheduler;

import com.minsait.onesait.platform.scheduler.scheduler.instance.SchedulerNames;

public enum SchedulerType {

	TWITTER(SchedulerNames.TWITTER_SCHEDULER_NAME), SCRIPT(SchedulerNames.SCRIPT_SCHEDULER_NAME),
	SIMULATION(SchedulerNames.SIMULATION_SCHEDULER_NAME), BATCH(SchedulerNames.BATCH_SCHEDULER_NAME),
	OKPI(SchedulerNames.OKPI_SCHEDULER_NAME), RESTPLANNER(SchedulerNames.REST_PLANNER_SCHEDULER_NAME),
	EXPIRATIONUSERS(SchedulerNames.EXPIRATION_USERS_NAME),
	EXPIRATIONRESETUSER(SchedulerNames.EXPIRATION_RESET_USERS_NAME),
	PROCESSEXECUTION(SchedulerNames.PROCESS_EXECUTION_NAME), BACKUPMINIO(SchedulerNames.BACKUP_MINIO_NAME);

	private String schedulerName;

	private SchedulerType(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public String getSchedulerName() {

		return schedulerName;
	}

	// Add fromString method to convert string to enum
	public static SchedulerType fromString(String input) {
		for (SchedulerType schedulerType : SchedulerType.values()) {
			if (schedulerType.schedulerName.equals(input)) {
				return schedulerType;
			}
		}
		return null;
	}

}

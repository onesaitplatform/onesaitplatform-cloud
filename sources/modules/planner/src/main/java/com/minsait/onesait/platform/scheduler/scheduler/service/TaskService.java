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
package com.minsait.onesait.platform.scheduler.scheduler.service;

import java.util.List;

import org.quartz.JobDataMap;

import com.minsait.onesait.platform.scheduler.scheduler.bean.ListTaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;

public interface TaskService {
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	
	List<ListTaskInfo> list(String username);
	
	/**
	 * Schedule a job
	 * @param info
	 * @return
	 */
	
	ScheduleResponseInfo addJob(TaskInfo info);
	
	/**
	 * Initialize the data map, adding the parameters user name and schedulerType 
	 * @param info
	 * @return
	 */
	
	JobDataMap initTaskDataMap (TaskInfo info);
	
	/**
	 * Unscheduled a job
	 * @param operation
	 * @return
	 */
	
	boolean unscheduled (TaskOperation operation);
	
	/**
	 * Pause a job
	 * @param operation
	 * @return
	 */
	
	boolean pause(TaskOperation operation);
	
	/**
	 * Resume a job
	 * @param operation
	 * @return
	 */
	
	boolean resume(TaskOperation operation);
	
	/**
	 * Checks if a job exists
	 * @param operation
	 * @return
	 */
	
	boolean checkExists(TaskOperation operation);
}

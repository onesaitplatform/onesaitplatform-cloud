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
package com.minsait.onesait.platform.scheduler.scheduler.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.scheduler.scheduler.bean.ListTaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.response.ScheduleResponseInfo;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

@RestController
@RequestMapping("/scheduler")
public class SchedulerDefaultController {
	
	@Autowired
	private TaskService taskService;
	
	@RequestMapping (method = RequestMethod.GET, value="/list/{username}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public List<ListTaskInfo> list( @PathVariable String username) {
		return taskService.list(username);
	}
		
	@RequestMapping(method = RequestMethod.POST, value="/schedule", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ScheduleResponseInfo schedule(@RequestBody TaskInfo taskInfo) {
		return taskService.addJob(taskInfo);
	}
	
	@RequestMapping (method = RequestMethod.GET, value="/unschedule/{jobName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseInfo unschedule(@PathVariable String jobName) {
		boolean unscheduled = taskService.unscheduled(new TaskOperation(jobName));
		return new ResponseInfo(unscheduled, "");
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/pause/{jobName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseInfo pause(@PathVariable String jobName){
		boolean resumed = taskService.pause(new TaskOperation(jobName));		
		return new ResponseInfo(resumed, "");
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/resume/{jobName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseInfo resume(@PathVariable String jobName){
		boolean resumed = taskService.resume(new TaskOperation(jobName));		
		return new ResponseInfo(resumed, "");
	}
	
}

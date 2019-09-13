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
package com.minsait.onesait.platform.controlpanel.controller.planner;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.scheduler.scheduler.bean.ListTaskInfo;
import com.minsait.onesait.platform.scheduler.scheduler.bean.TaskOperation;
import com.minsait.onesait.platform.scheduler.scheduler.service.TaskService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/planner")
@Slf4j
@Lazy
public class PlannerController {

	@Autowired
	private TaskService taskService;

	@Autowired
	private AppWebUtils utils;

	private static final String REDIRECT_PLANNER_LIST = "redirect:/planner/list";

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request) {

		List<ListTaskInfo> tasks = taskService.list(utils.getUserId());

		model.addAttribute("tasks", tasks);
		return "planner/list";
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@RequestMapping(method = RequestMethod.GET, value = "/unschedule/{jobName}")
	public String unschedule(@PathVariable String jobName) {

		boolean unscheduled = taskService.unscheduled(new TaskOperation(jobName));
		return REDIRECT_PLANNER_LIST;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@RequestMapping(method = RequestMethod.GET, value = "/pause/{jobName}")
	public String pause(@PathVariable String jobName) {

		boolean resumed = taskService.pause(new TaskOperation(jobName));
		return REDIRECT_PLANNER_LIST;
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_DATASCIENTIST','ROLE_DEVELOPER')")
	@RequestMapping(method = RequestMethod.GET, value = "/resume/{jobName}")
	public String resume(@PathVariable String jobName) {

		boolean resumed = taskService.resume(new TaskOperation(jobName));
		return REDIRECT_PLANNER_LIST;
	}

}

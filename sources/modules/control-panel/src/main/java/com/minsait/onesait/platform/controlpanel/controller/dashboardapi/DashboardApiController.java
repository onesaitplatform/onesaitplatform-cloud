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
package com.minsait.onesait.platform.controlpanel.controller.dashboardapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.services.dashboard.DashboardService;
import com.minsait.onesait.platform.config.services.dashboardapi.DashboardApiService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/dashboardapi")
@Controller
@Slf4j
public class DashboardApiController {

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private DashboardApiService dashboardApiService;

	@PostMapping(value = { "/createGadget" }, produces = "application/json")
	public @ResponseBody String createGadget(String json) {
		return dashboardApiService.createGadget(json, this.utils.getUserId());
	}

	@PostMapping(value = { "/updateGadget" }, produces = "application/json")
	public @ResponseBody String updateGadget(String json) {
		return dashboardApiService.updateGadget(json, this.utils.getUserId());
	}

	@PostMapping(value = { "/deleteGadget" }, produces = "application/json")
	public @ResponseBody String deleteGadget(String json) {
		return "";
	}

	@PutMapping(value = "/savemodel/{id}", produces = "application/json")
	public @ResponseBody String updateDashboardModel(@PathVariable("id") String id, @RequestBody String json) {
		if (id != null && !id.equals("") && json != null && !json.equals("")) {
			if (dashboardService.dashboardExistsById(id)) {
				dashboardService.saveDashboardModel(id, json, utils.getUserId());
				dashboardService.generateDashboardImage(id, utils.getCurrentUserOauthToken());
				return "{\"ok\":true}";
			} else {
				return "{\"error\":\"Dashboard does not exist\"}";
			}
		} else
			log.error("Missing json data");
		return "{\"error\":\"Missing json data\"}";
	}

	@PostMapping(value = { "/setSynopticElementDataSource" }, produces = "application/json")
	public @ResponseBody String setSynopticDataSource(String json) {
		return dashboardApiService.setSynopticElementDataSource(json, this.utils.getUserId());
	}
}

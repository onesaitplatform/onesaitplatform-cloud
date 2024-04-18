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
package com.minsait.onesait.platform.controlpanel.controller.digitaltwin.device.display;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.digitaltwin.type.DigitalTwinTypeService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.mongodb.MongoBasicOpsDBRepository;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinModel;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/digitaltwindisplay")
public class DigitalTwinDisplayController {

	static final String LOG_COLLECTION = "TwinLogs";
	static final String PROPERTIES_COLLECTION = "TwinProperties";
	static final String EVENTS_COLLECTION = "TwinEvents";
	static final String ACTIONS_COLLECTION = "TwinActions";

	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	@Autowired
	MongoBasicOpsDBRepository mongoRepo;

	@Autowired
	private DigitalTwinTypeService typeService;

	@Autowired
	private DigitalTwinDeviceService deviceService;

	@Autowired
	private AppWebUtils utils;

	private static final String FIND_DEVICEID = ".find({deviceId:'";
	private static final String SORT_TIMESTAMP = "'}).sort({timestamp: -1}).limit(";
	ObjectMapper mapper = new ObjectMapper();

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping("show")
	public String show(Model model) {
		List<DigitalTwinType> types = this.typeService.getDigitalTwinTypesByUserId(utils.getUserId());
		model.addAttribute("types", types);
		return "digitaltwindisplay/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@GetMapping(value = "/getDevices/{typeId}")
	public @ResponseBody List<String> getDevices(Model model, @PathVariable("typeId") String typeId) {
		try {
			return this.deviceService.getDigitalTwinDevicesByTypeId(typeId);
		} catch (Exception e) {
			log.error("Error getting devices of Digital Twin type: " + typeId);
			return Collections.emptyList();
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@PostMapping("executeQuery")
	public String executeQuery(Model model, @RequestParam String type, @RequestParam String device,
			@RequestParam String offset, @RequestParam String operation, @RequestParam String eventName,
			@RequestParam String actionName) {
		String collection = LOG_COLLECTION;
		String queryResult = null;
		List<String> results = new ArrayList<>();
		List<String> devices = new ArrayList<>();
		List<String> types = new ArrayList<>();
		try {

			if (type.equalsIgnoreCase("all")) {
				devices = this.deviceService.getDigitalTwinDevicesIdsByUser(utils.getUserId());
				List<DigitalTwinType> lTypes = this.typeService.getDigitalTwinTypesByUserId(utils.getUserId());
				for (DigitalTwinType t : lTypes) {
					types.add(t.getIdentification());
				}
			} else if (device.equalsIgnoreCase("all")) {
				devices = this.deviceService.getDigitalTwinDevicesIdsByUserAndTypeId(utils.getUserId(), type);
				types.add(type);
			} else {
				devices.add(this.deviceService.getDigitalTwinDevicebyName(device).getId());
				types.add(type);
			}

			for (String d : devices) {
				for (String t : types) {
					if (operation.equalsIgnoreCase(DigitalTwinModel.EventType.SHADOW.name())) {
						collection = PROPERTIES_COLLECTION + t.substring(0, 1).toUpperCase() + t.substring(1);
					} else if (operation.equalsIgnoreCase(DigitalTwinModel.EventType.LOG.name())) {
						collection = LOG_COLLECTION;
					} else if (operation.equalsIgnoreCase(DigitalTwinModel.EventType.NOTEBOOK.name())
							|| operation.equalsIgnoreCase(DigitalTwinModel.EventType.PIPELINE.name())
							|| operation.equalsIgnoreCase(DigitalTwinModel.EventType.FLOW.name())
							|| operation.equalsIgnoreCase(DigitalTwinModel.EventType.RULE.name())
							|| operation.equalsIgnoreCase(DigitalTwinModel.EventType.CUSTOM.name())
							|| operation.equalsIgnoreCase(DigitalTwinModel.EventType.REGISTER.name())
							|| operation.equalsIgnoreCase(DigitalTwinModel.EventType.PING.name())) {

						collection = EVENTS_COLLECTION;
					} else if (operation.equalsIgnoreCase("action")) {
						collection = ACTIONS_COLLECTION + t.substring(0, 1).toUpperCase() + t.substring(1);
					}

					if (operation.equalsIgnoreCase(DigitalTwinModel.EventType.CUSTOM.name()) && eventName != "") {
						queryResult = mongoRepo.queryNativeAsJson(collection,
								"db." + collection + FIND_DEVICEID + d + "',event:'" + operation.toUpperCase()
										+ "',eventName:'" + eventName + SORT_TIMESTAMP + Integer.parseInt(offset)
										+ ")");
					} else if (collection.equalsIgnoreCase(EVENTS_COLLECTION)) {
						queryResult = mongoRepo.queryNativeAsJson(collection,
								"db." + collection + FIND_DEVICEID + d + "',event:'" + operation.toUpperCase()
										+ SORT_TIMESTAMP + Integer.parseInt(offset) + ")");
					} else if (collection.equalsIgnoreCase(ACTIONS_COLLECTION)) {
						queryResult = mongoRepo.queryNativeAsJson(collection, "db." + collection + FIND_DEVICEID + d
								+ "',action:'" + actionName + SORT_TIMESTAMP + Integer.parseInt(offset) + ")");
					} else {
						queryResult = mongoRepo.queryNativeAsJson(collection, "db." + collection + FIND_DEVICEID + d
								+ SORT_TIMESTAMP + Integer.parseInt(offset) + ")");
					}

					JSONArray arrayResult = new JSONArray(queryResult);

					for (int i = 0; i < arrayResult.length(); i++) {
						results.add(arrayResult.getJSONObject(i).toString());
					}
				}
			}
			model.addAttribute("queryResult", results);
			return "digitaltwindisplay/show :: query";
		} catch (Exception e) {
			log.error("Error getting shadow devices");
			model.addAttribute("queryResult",
					utils.getMessage("querytool.query.native.error", "Error malformed query"));
			return "digitaltwindisplay/show :: query";
		}
	}
}

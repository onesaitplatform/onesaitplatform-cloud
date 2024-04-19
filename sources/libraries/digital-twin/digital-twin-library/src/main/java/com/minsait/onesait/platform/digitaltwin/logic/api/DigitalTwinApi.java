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
package com.minsait.onesait.platform.digitaltwin.logic.api;

import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.digitaltwin.event.manager.EventManager;
import com.minsait.onesait.platform.digitaltwin.status.IDigitalTwinStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides Digital Device API to Javascript Logic
 * 
 * @author minsait by Indra
 *
 */
@Slf4j
@Component
public class DigitalTwinApi implements DigitalTwinLogicAPI {

	@Autowired
	private EventManager eventManager;

	@Autowired
	private IDigitalTwinStatus digitalTwinStatus;

	private static DigitalTwinApi instance;

	@PostConstruct
	@Override
	public void init() {
		instance = this;
	}

	public static DigitalTwinApi getInstance() {
		return instance;
	}

	@Override
	public void log(String trace) {
		eventManager.log(trace);
	}

	@Override
	public void setStatusValue(String property, Object value) {
		try {
			digitalTwinStatus.setProperty(property, value);
		} catch (Exception e) {
			log.error("Error setting status property {}", property, e);
		}
	}

	@Override
	public Object getStatusValue(String property) {
		try {
			return digitalTwinStatus.getProperty(property);
		} catch (Exception e) {
			log.error("Error getting status property {}", property, e);
			return null;
		}
	}

	@Override
	public void sendUpdateShadow(String data) {
		try {
			JSONObject jsonData = new JSONObject(data);
			Iterator<String> keys = jsonData.keys();
			HashMap<String, Object> properties = new HashMap<String, Object>();
			while (keys.hasNext()) {
				String property = keys.next();
				Object value = jsonData.get(property);
				properties.put(property, value.toString());
				setStatusValue(property, value);
			}
			eventManager.updateShadow(properties);
		} catch (JSONException e) {
			log.error("Error parsing data");
		}
	}

	@Override
	public void sendCustomEvent(String eventName) {
		eventManager.sendCustomEvent(digitalTwinStatus.toMap(), eventName);
	}
}

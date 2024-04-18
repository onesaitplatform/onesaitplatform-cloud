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
package com.minsait.onesait.platform.digitaltwin.broker.plugable.impl.gateway.reference.rest;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;

@Component
public class SenseHatDevicesImpl implements SenseHatDevices {

	@Autowired
	private DigitalTwinDeviceRepository deviceRepo;

	@Autowired
	private DigitalTwinTypeRepository typeRepo;

	@Override
	public ResponseEntity<?> getSensehatDevices() {
		try {
			DigitalTwinType type = typeRepo.findByName("sensehat");
			List<DigitalTwinDevice> devices = deviceRepo.findByTypeId(type);
			JSONArray array = new JSONArray();
			for (DigitalTwinDevice device : devices) {
				JSONObject obj = new JSONObject();
				obj.put("identification", device.getIdentification());
				obj.put("digitalKey", device.getDigitalKey());
				array.put(obj);
			}

			return new ResponseEntity<>(array.toString(), HttpStatus.OK);
		} catch (JSONException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

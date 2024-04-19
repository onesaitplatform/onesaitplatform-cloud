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
package com.minsait.onesait.platform.config.services.digitaltwin.device;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.minsait.onesait.platform.config.model.DigitalTwinDevice;

public interface DigitalTwinDeviceService {

	List<String> getAllIdentifications();

	List<DigitalTwinDevice> getAll();

	List<DigitalTwinDevice> getAllByUserId(String userId);
	
	List<DigitalTwinDevice> getAllByUserIdAndIdentification(String userId, String identification);

	List<String> getAllDigitalTwinTypeNames();

	public String generateToken();

	public String getLogicFromType(String type);

	public void createDigitalTwinDevice(DigitalTwinDevice digitalTwinDevice, HttpServletRequest httpServletRequest);

	void getDigitalTwinToUpdate(Model model, String id);

	DigitalTwinDevice getDigitalTwinDeviceById(String id);

	void updateDigitalTwinDevice(DigitalTwinDevice digitalTwinDevice, HttpServletRequest httpServletRequest);

	void deleteDigitalTwinDevice(DigitalTwinDevice digitalTwinDevice);

	List<String> getDigitalTwinDevicesByTypeId(String typeId);

	List<DigitalTwinDevice> getAllDigitalTwinDevicesByTypeId(String typeId);

	List<String> getDigitalTwinDevicesIdsByUser(String user);

	List<String> getDigitalTwinDevicesIdsByUserAndTypeId(String userId, String typeId);

	DigitalTwinDevice getDigitalTwinDevicebyName(String name);

	DigitalTwinDevice getDigitalTwinDevicebyName(String apiKey, String name);

	Integer getNumOfDevicesByTypeId(String type);

	boolean hasUserEditAccess(String id, String userId);

	boolean hasUserAccess(String id, String userId);

	DigitalTwinDevice save(DigitalTwinDevice digitalTwinDevice);
}

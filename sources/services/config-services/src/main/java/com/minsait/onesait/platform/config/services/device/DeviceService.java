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
package com.minsait.onesait.platform.config.services.device;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.minsait.onesait.platform.config.components.LogOntology;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Device;

public interface DeviceService {

	Device getByClientPlatformIdAndIdentification(ClientPlatform clientPlatform, String identification);

	void createDevice(Device device);

	Device updateDevice(Device device);

	void patchDevice(String deviceId, String tags);

	List<Device> getAll();

	Device getById(String id);

	int updateDeviceStatusAndDisableWhenUpdatedAtLessThanDate(boolean status, boolean disabled, Date date);

	List<Device> getByClientPlatformId(ClientPlatform clientPlatform);

	List<LogOntology> getLogInstances(String resultFromQueryTool) throws IOException;

	List<String> getDeviceCommands(Device device);

	void deleteDevice(Device device);

}

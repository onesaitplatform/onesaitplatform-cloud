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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.components.LogOntology;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Device;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DeviceRepository;

@Service
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	DeviceRepository deviceRepository;
	@Autowired
	ClientPlatformRepository clientPlatformRepository;
	@Autowired
	ObjectMapper mapper;

	@Autowired(required = false)
	DeviceScheduledUpdater deviceScheuduledUpdater;

	@Override
	public List<Device> getAll() {
		return deviceRepository.findAll();
	}

	@Override
	public List<Device> getByClientPlatformId(ClientPlatform clientPlatform) {

		return deviceRepository.findByClientPlatform(clientPlatform);

	}

	@Override
	public void createDevice(Device device) {
		deviceRepository.save(device);
	}

	@Override
	@CachePut(cacheNames = "DeviceRepository", key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	public Device updateDevice(Device device) {
		return deviceScheuduledUpdater.updateDevice(device);
	}

	@Override
	@Transactional
	public int updateDeviceStatusAndDisableWhenUpdatedAtLessThanDate(boolean status, boolean disabled, Date date) {
		return deviceRepository.updateDeviceStatusByUpdatedAt(status, disabled, date);
	}

	@Override
	public Device getByClientPlatformIdAndIdentification(ClientPlatform clientPlatform, String identification) {
		return deviceRepository.findByClientPlatformAndIdentification(clientPlatform, identification);
	}

	@Override
	public Device getById(String id) {
		return deviceRepository.findById(id);
	}

	@Override
	public void patchDevice(String deviceId, String tags) {
		final Device device = deviceRepository.findById(deviceId);
		device.setTags(tags);
		deviceRepository.save(device);

	}

	@Override
	public List<LogOntology> getLogInstances(String resultFromQueryTool) throws IOException {
		try {
			final ArrayNode arrayResult = (ArrayNode) mapper.readTree(resultFromQueryTool);
			final ArrayNode newArray = mapper.createArrayNode();
			for (final JsonNode node : arrayResult) {
				newArray.add(node.get("value").get("DeviceLog"));
			}

			return mapper.readValue(newArray.toString(), new TypeReference<List<LogOntology>>() {
			});
		} catch (final RuntimeException re) {
			return new ArrayList<>();
		}

	}

	@Override
	public List<String> getDeviceCommands(Device device) {
		final List<String> commandActions = new ArrayList<>();
		if (!StringUtils.isEmpty(device.getJsonActions())) {
			try {
				final JsonNode commands = mapper.readTree(device.getJsonActions());
				addCommandsFromJson(commandActions, commands);
			} catch (final IOException e) {
				throw new GenericRuntimeOPException(e);
			}
		}
		return commandActions;
	}

	private void addCommandsFromJson(List<String> commandActions, JsonNode commands) {
		if (!commands.isArray()) {
			final Iterator<String> fields = commands.fieldNames();
			while (fields.hasNext()) {
				commandActions.add(fields.next());
			}
		} else {
			for (final JsonNode command : commands) {
				final Iterator<String> fields = command.fieldNames();
				while (fields.hasNext()) {
					commandActions.add(fields.next());
				}
			}
		}
	}

	@Override
	public void deleteDevice(Device device) {
		deviceRepository.deleteById(device);
	}

}

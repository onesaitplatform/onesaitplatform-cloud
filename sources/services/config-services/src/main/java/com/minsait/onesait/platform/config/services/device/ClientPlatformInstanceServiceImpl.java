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
package com.minsait.onesait.platform.config.services.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.components.LogOntology;
import com.minsait.onesait.platform.config.dto.ClientPlatformSimplifiedDTO;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.multitenant.config.repository.IoTSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientPlatformInstanceServiceImpl implements ClientPlatformInstanceService {

	@Autowired
	ClientPlatformInstanceRepository cpiRepository;
	@Autowired
	ClientPlatformRepository cpRepository;
	@Autowired
	IoTSessionRepository iotsessionRepository;
	@Autowired
	ObjectMapper mapper;

	@Value("${onesaitplatform.iotbroker.devices.perclient.max:20}")
	private int maxvalue;

	@Autowired(required = false)
	ClientPlatformInstanceScheduledUpdater deviceScheuduledUpdater;

	@Value("${spring.datasource.hikari.jdbc-url:mysql}")
	private String datasource;


	@Override
	public List<ClientPlatformInstance> getAll() {
		return cpiRepository.findAll();
	}

	@Override
	public List<ClientPlatformInstance> getByClientPlatformId(ClientPlatform clientPlatform) {

		return cpiRepository.findByClientPlatform(clientPlatform);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@CachePut(cacheNames = ClientPlatformInstanceRepository.CLIENTPLATFORMINSTANCE_REPOSITORY, key = "#p1.concat('-').concat(#p0.identification)", unless = "#result == 0")
	public int createOrUpdateClientPlatformInstance(ClientPlatformInstance clientPlatformInstance, String cpIdentification) {
		Date date = new Date();
		clientPlatformInstance.setUpdatedAt(date);
		clientPlatformInstance.setCreatedAt(date);


		ClientPlatform cp = clientPlatformInstance.getClientPlatform();
		ClientPlatformSimplifiedDTO cpDTO;
		if ( cp == null) {
			cpDTO = cpRepository.findClientPlatformIdByIdentification(cpIdentification);
		} else {
			cpDTO = new ClientPlatformSimplifiedDTO(cp.getId(), cp.getIdentification());
		}
		int inserted = 0;
		int	 updated = 0;
		clientPlatformInstance.setId(UUID.randomUUID().toString());
		int getClientPlatformInstance = cpiRepository.getClientPlatformInstance(clientPlatformInstance, cpDTO.getClientPlatformId());
		
		if(getClientPlatformInstance == 0) {

			inserted = cpiRepository.createClientPlatformInstance(clientPlatformInstance, cpDTO.getClientPlatformId());
				
		} else {
			
			updated = cpiRepository.updateClientPlatformInstance(clientPlatformInstance, cpDTO.getClientPlatformId());
		}
		
		if (inserted > 0 || updated > 0) {
			if (log.isDebugEnabled()) {
				log.debug("Created or modified device. Identification: {}, clientPlatformId: {}",
						clientPlatformInstance.getIdentification(),
						cpDTO.getClientPlatformId());
			}
		} else {
			if (log.isWarnEnabled()) {
				log.warn("Fail creating or updating ClientPlatformInstance. Identification: {}, clientPlatformId: {}",
						clientPlatformInstance.getIdentification(),
						cpDTO.getClientPlatformId());
			}
		}

		return inserted;
	}

	@Override
	public ClientPlatformInstance updateClientPlatformInstance(ClientPlatformInstance clientPlatformInstance, String cpIdentification) {
		return deviceScheuduledUpdater.saveDevice(clientPlatformInstance, cpIdentification);
	}

	@Override
	@Transactional
	public int updateClientPlatformInstanceStatusAndDisableWhenUpdatedAtLessThanDate(boolean status, boolean disabled,
			Date date) {
		return cpiRepository.updateClientPlatformInstanceStatusByUpdatedAt(status, disabled, date);
	}

	@Override
	public ClientPlatformInstance getByClientPlatformIdAndIdentification(ClientPlatform clientPlatform,
			String identification) {
		return cpiRepository.findByClientPlatformAndIdentification(clientPlatform, identification);
	}

	@Override
	public ClientPlatformInstance getByClientPlatformIdAndIdentification(String clienttPlatformIdentification, String identification) {
		return cpiRepository.findByClientPlatformAndIdentification(clienttPlatformIdentification, identification);
	}

	@Override
	public ClientPlatformInstance getById(String id) {
		return cpiRepository.findById(id).orElse(null);
	}

	@Override
	public void patchClientPlatformInstance(String deviceId, String tags) {
        cpiRepository.findById(deviceId).ifPresent(device -> {
			device.setTags(tags);
			cpiRepository.save(device);
		});
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
	public List<String> getClientPlatformInstanceCommands(ClientPlatformInstance clientPlatformInstance) {
		final List<String> commandActions = new ArrayList<>();
		if (StringUtils.hasText(clientPlatformInstance.getJsonActions())) {
			try {
				final JsonNode commands = mapper.readTree(clientPlatformInstance.getJsonActions());
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
	public void deleteClientPlatformInstance(ClientPlatformInstance clientPlatformInstance) {
		cpiRepository.deleteById(clientPlatformInstance);
	}

	@Override
	public List<IoTSession> getSessionKeys(ClientPlatformInstance clientPlatformInstance) {
		return iotsessionRepository.findByClientPlatformID(clientPlatformInstance.getId());
	}

}

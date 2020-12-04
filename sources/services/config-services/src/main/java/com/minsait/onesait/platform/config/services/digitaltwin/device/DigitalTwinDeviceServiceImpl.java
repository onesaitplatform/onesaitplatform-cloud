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
package com.minsait.onesait.platform.config.services.digitaltwin.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.services.exceptions.DigitalTwinServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterDigitalTwinDeviceToken;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDigitalTwinDeviceTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DigitalTwinDeviceServiceImpl implements DigitalTwinDeviceService {

	@Autowired
	private DigitalTwinDeviceRepository digitalTwinDeviceRepo;

	@Autowired
	private DigitalTwinTypeRepository digitalTwinTypeRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	private MasterDigitalTwinDeviceTokenRepository masterTokenRepository;

	@Override
	public List<String> getAllIdentifications() {
		final List<DigitalTwinDevice> digitalDevices = digitalTwinDeviceRepo.findAllByOrderByIdentificationAsc();
		final List<String> identifications = new ArrayList<>();
		for (final DigitalTwinDevice device : digitalDevices) {
			identifications.add(device.getIdentification());
		}
		return identifications;
	}

	@Override
	public List<DigitalTwinDevice> getAll() {
		return digitalTwinDeviceRepo.findAll();
	}

	@Override
	public List<String> getAllDigitalTwinTypeNames() {
		return digitalTwinTypeRepo.findAllIdentifications();
	}

	@Override
	public String generateToken() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@Override
	public String getLogicFromType(String type) {
		final DigitalTwinType digitalTwinType = digitalTwinTypeRepo.findByIdentification(type);
		if (digitalTwinType != null) {
			final String logic = digitalTwinType.getLogic();
			if (logic != null) {
				return logic;
			} else {
				log.error("Error, logic not found for Digital Twin Type: " + type);
				return null;
			}
		} else {
			log.error("Error, Digital Twin Type not found: " + type);
			return null;
		}

	}

	@Override
	public void createDigitalTwinDevice(DigitalTwinDevice digitalTwinDevice, HttpServletRequest httpServletRequest) {
		if (digitalTwinDeviceRepo.findByIdentification(digitalTwinDevice.getIdentification()) == null) {
			try {
				final String type = httpServletRequest.getParameter("typeSelected").trim();
				if (type != null && type != "") {
					final DigitalTwinType digitalTwinType = digitalTwinTypeRepo.findByIdentification(type);
					if (digitalTwinType == null) {
						log.error("Digital Twin Type : " + type + "doesn't exist.");
						return;
					}
					digitalTwinDevice.setIp("");
					final User user = userService.getUser(digitalTwinDevice.getUser().getUserId());
					if (user != null) {
						digitalTwinDevice.setUser(user);
						digitalTwinDevice.setTypeId(digitalTwinType);
						digitalTwinDeviceRepo.save(digitalTwinDevice);
					} else {
						log.error("Invalid user");
						return;
					}
				} else {
					log.error("Invalid Digital Twin Type.");
					return;
				}
			} catch (final Exception e) {
				throw new DigitalTwinServiceException("Problems creating the digital twin device", e);
			}
		} else {
			throw new DigitalTwinServiceException(
					"Digital Twin with identification: " + digitalTwinDevice.getIdentification() + " exists");
		}
	}

	@Override
	public void getDigitalTwinToUpdate(Model model, String id) {
		final DigitalTwinDevice digitalTwinDevice = digitalTwinDeviceRepo.findById(id).orElse(null);
		if (digitalTwinDevice != null) {
			model.addAttribute("digitaltwindevice", digitalTwinDevice);
			model.addAttribute("logic", digitalTwinDevice.getTypeId().getLogic());
			model.addAttribute("typeDigital", digitalTwinDevice.getTypeId().getIdentification());
		} else {
			log.error("DigitalTwinDevice with id:" + id + ", not found.");
		}
	}

	@Override
	public DigitalTwinDevice getDigitalTwinDeviceById(String id) {
		return digitalTwinDeviceRepo.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void updateDigitalTwinDevice(DigitalTwinDevice digitalTwinDevice, HttpServletRequest httpServletRequest) {
		// Update DigitalTwinDevice
		digitalTwinDeviceRepo.findById(digitalTwinDevice.getId()).ifPresent(digitalTwinDeviceDb -> {
			final User user = digitalTwinDeviceDb.getUser();
			final String id = digitalTwinDeviceDb.getId();
			digitalTwinDevice.setUser(user);
			digitalTwinDevice.setId(id);
			digitalTwinDeviceRepo.deleteById(digitalTwinDeviceDb.getId());
			createDigitalTwinDevice(digitalTwinDevice, httpServletRequest);
		});

	}

	@Override
	public void deleteDigitalTwinDevice(DigitalTwinDevice digitalTwinDevice) {
		if (resourceService.isResourceSharedInAnyProject(digitalTwinDevice))
			throw new OPResourceServiceException(
					"This DigitalTwinDevice is shared within a Project, revoke access from project prior to deleting");
		final DigitalTwinType type = digitalTwinDevice.getTypeId();
		type.getDigitalTwinDevices().remove(digitalTwinDevice);
		digitalTwinTypeRepo.save(type);
		digitalTwinDeviceRepo.deleteById(digitalTwinDevice.getId());
	}

	@Override
	public List<String> getDigitalTwinDevicesByTypeId(String typeId) {
		return digitalTwinDeviceRepo.findNamesByTypeId(digitalTwinTypeRepo.findByIdentification(typeId));
	}

	@Override
	public List<DigitalTwinDevice> getAllDigitalTwinDevicesByTypeId(String typeId) {
		return digitalTwinDeviceRepo.findByTypeId(digitalTwinTypeRepo.findByIdentification(typeId));
	}

	@Override
	public List<String> getDigitalTwinDevicesIdsByUser(String userId) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			return digitalTwinDeviceRepo.findAllIds();
		} else {
			return digitalTwinDeviceRepo.findIdsByUser(userService.getUser(userId));
		}

	}

	@Override
	public List<String> getDigitalTwinDevicesIdsByUserAndTypeId(String userId, String typeId) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			return digitalTwinDeviceRepo.findIdsByTypeId(digitalTwinTypeRepo.findByIdentification(typeId));
		} else {
			return digitalTwinDeviceRepo.findIdsByUserAndTypeId(user, digitalTwinTypeRepo.findByIdentification(typeId));
		}
	}

	@Override
	public DigitalTwinDevice getDigitalTwinDevicebyName(String name) {
		return digitalTwinDeviceRepo.findByIdentification(name);
	}

	@Override
	public Integer getNumOfDevicesByTypeId(String type) {
		final DigitalTwinType digitalTwinType = digitalTwinTypeRepo.findByIdentification(type);
		if (digitalTwinType != null) {
			return digitalTwinDeviceRepo.findByTypeId(digitalTwinType).size();
		} else {
			return 0;
		}
	}

	@Override
	public List<DigitalTwinDevice> getAllByUserId(String userId) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			return digitalTwinDeviceRepo.findAll();
		} else {
			return digitalTwinDeviceRepo.findByUser(user);
		}

	}
	
	@Override
	public List<DigitalTwinDevice> getAllByUserIdAndIdentification(String userId, String identification) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			return digitalTwinDeviceRepo.findByIdentificationContaining(identification);
		} else {
			return digitalTwinDeviceRepo.findByUserAndIdentificationLike(user, identification);
		}

	}

	@Override
	public boolean hasUserEditAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final OPResource resource = resourceService.getResourceById(id);
		if (resource.getUser().equals(user) || userService.isUserAdministrator(user))
			return true;
		else
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
	}

	@Override
	public boolean hasUserAccess(String id, String userId) {
		final User user = userService.getUser(userId);
		final OPResource resource = resourceService.getResourceById(id);
		if (resource.getUser().equals(user) || userService.isUserAdministrator(user))
			return true;
		else
			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
	}

	@Override
	public DigitalTwinDevice getDigitalTwinDevicebyName(String apiKey, String name) {
		final Optional<MasterDigitalTwinDeviceToken> token = Optional
				.ofNullable(masterTokenRepository.findByTokenName(apiKey));
		token.ifPresent(t -> {
			MultitenancyContextHolder.setTenantName(t.getTenant());
			MultitenancyContextHolder.setVerticalSchema(t.getVerticalSchema());
		});
		return digitalTwinDeviceRepo.findByIdentification(name);
	}

	@Override
	public DigitalTwinDevice save(DigitalTwinDevice digitalTwinDevice) {
		return digitalTwinDeviceRepo.save(digitalTwinDevice);
	}

}

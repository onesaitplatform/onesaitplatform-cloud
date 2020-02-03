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
package com.minsait.onesait.platform.controlpanel.controller.devicemanagement;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceRepository;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Component
public class GraphDeviceUtil {

	private String urlImages;
	private String genericUserName = "USER";

	@Autowired
	private ClientPlatformRepository clientPlatformRepository;

	@Autowired
	private ClientPlatformInstanceRepository deviceRepository;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private IntegrationResourcesService intregationResourcesService;

	@Value("${onesaitplatform.devices.timeout_devices_inseconds:300}")
	private int maxTimeUpdateInSeconds;

	private static final String ACTIVE = "active";
	private static final String INACTIVE = "inactive";

	private static final String IMAGE_DEVICE_ACTIVE = "deviceActive.png";
	private static final String IMAGE_DEVICE_INACTIVE = "deviceInactive.png";
	private static final String IMAGE_DEVICE_ERROR = "deviceError.png";
	private static final String IMAGE_CLIENT_PLATFORMS = "clientPlat.png";
	private static final String IMAGE_CLIENT = "client.png";
	private static final String IMAGE_CLIENT_ERROR = "clientError.png";
	private static final String CLIENT_PLATFORM_STR = "clientplatform";

	@PostConstruct
	public void init() {
		// initialize URLS
		String url = this.intregationResourcesService.getUrl(Module.CONTROLPANEL, ServiceUrl.BASE);
		this.urlImages = url + "/static/images/";
	}

	public List<GraphDeviceDTO> constructGraphWithClientPlatformsForUser() {

		List<GraphDeviceDTO> arrayLinks = new LinkedList<>();
		String name = utils.getMessage("name.clients", "PLATFORM CLIENTS");

		arrayLinks.add(new GraphDeviceDTO(genericUserName, name, null, null, genericUserName, name, utils.getUserId(),
				name, "suit", this.urlImages + IMAGE_CLIENT_PLATFORMS, null, null, null, null));

		List<ClientPlatform> clientPlatforms = null;
		if (utils.isAdministrator()) {
			clientPlatforms = clientPlatformRepository.findAll();

		} else {
			clientPlatforms = clientPlatformRepository.findByUser(this.userService.getUser(utils.getUserId()));

		}

		for (ClientPlatform clientPlatform : clientPlatforms) {

			List<ClientPlatformInstance> listDevice = deviceRepository.findByClientPlatform(clientPlatform);

			String clientImage = IMAGE_CLIENT;
			if (listDevice != null && !listDevice.isEmpty()) {
				for (Iterator<ClientPlatformInstance> iterator = listDevice.iterator(); iterator.hasNext();) {
					ClientPlatformInstance device = iterator.next();
					if (!device.getStatus().equals(ClientPlatformInstance.StatusType.OK.toString())) {
						clientImage = IMAGE_CLIENT_ERROR;
					}
				}
			}

			arrayLinks.add(new GraphDeviceDTO(name, clientPlatform.getId(), null, null, name, CLIENT_PLATFORM_STR, name,
					clientPlatform.getIdentification(), "licensing", this.urlImages + clientImage, null, null, null,
					null));

			if (listDevice != null && !listDevice.isEmpty()) {
				for (Iterator<ClientPlatformInstance> iterator = listDevice.iterator(); iterator.hasNext();) {
					ClientPlatformInstance device = iterator.next();
					String state;
					String image;
					if (device.isConnected() && !maximunTimeUpdatingExceeded(device.getUpdatedAt())) {
						state = ACTIVE;
						image = IMAGE_DEVICE_ACTIVE;
						if (device.getStatus() != null && device.getStatus().trim().length() > 0
								&& !device.getStatus().equals(ClientPlatformInstance.StatusType.OK.toString())) {
							image = IMAGE_DEVICE_ERROR;
						}
					} else {
						state = INACTIVE;
						image = IMAGE_DEVICE_INACTIVE;
						if (device.getStatus() != null && device.getStatus().trim().length() > 0
								&& !device.getStatus().equals(ClientPlatformInstance.StatusType.OK.toString())) {
							image = IMAGE_DEVICE_ERROR;
						}
					}

					arrayLinks.add(new GraphDeviceDTO(clientPlatform.getId(), device.getId(), device.getProtocol(),
							device.getJsonActions(), CLIENT_PLATFORM_STR, CLIENT_PLATFORM_STR,
							clientPlatform.getIdentification(), device.getIdentification(), state,
							this.urlImages + image, device.getStatus(), state, device.getSessionKey(),
							device.getUpdatedAt()));
				}

			}
		}
		return arrayLinks;

	}

	private boolean maximunTimeUpdatingExceeded(Date lastUpdate) {
		Date currentDate = new Date();
		long diff = currentDate.getTime() - lastUpdate.getTime();

		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
		return seconds >= maxTimeUpdateInSeconds;
	}

}

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
package com.minsait.onesait.platform.api.rest.api.fiql;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.service.api.ApiSecurityService;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiSuscripcionDTO;

@Service
public class ApiSuscripcionFIQL {

	@Autowired
	private ApiSecurityService apiSecurityService;

	@Autowired
	private ApiServiceRest apiService;

	private DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	public List<ApiSuscripcionDTO> toApiSuscripcionesDTO(List<UserApi> userApis) {
		List<ApiSuscripcionDTO> suscripcionesDTO = new ArrayList<>();
		for (UserApi userApi : userApis) {
			suscripcionesDTO.add(toApiSuscripcionDTO(userApi));
		}
		return suscripcionesDTO;
	}

	public ApiSuscripcionDTO toApiSuscripcionDTO(UserApi userApi) {
		ApiSuscripcionDTO suscripcionDTO = new ApiSuscripcionDTO();
		suscripcionDTO.setApiIdentification(userApi.getApi().getIdentification());
		suscripcionDTO.setUserId(userApi.getUser().getUserId());
		if (userApi.getCreatedAt() != null) {
			suscripcionDTO.setCreatedAt(userApi.getCreatedAt().toString());
		}
		if (userApi.getUpdatedAt() != null) {
			suscripcionDTO.setUpdatedAt(userApi.getUpdatedAt().toString());
		}
		return suscripcionDTO;
	}

	public UserApi copyProperties(ApiSuscripcionDTO suscripcion) {
		UserApi userApi = new UserApi();
		userApi.setApi(apiService.getApi(suscripcion.getApiIdentification()));

		User user = apiSecurityService.getUser(suscripcion.getUserId());

		if (user != null) {
			userApi.setUser(user);
		} else {
			throw new IllegalArgumentException("WrongUser");
		}

		try {
			if (suscripcion.getCreatedAt() != null && !suscripcion.getCreatedAt().equals("")) {
				userApi.setCreatedAt(df.parse(suscripcion.getCreatedAt()));
			}
		} catch (ParseException ex) {
			throw new IllegalArgumentException("WrongInitDateFormat");
		}
		try {
			if (suscripcion.getUpdatedAt() != null && !suscripcion.getUpdatedAt().equals("")) {
				userApi.setUpdatedAt(df.parse(suscripcion.getUpdatedAt()));
			}
		} catch (ParseException ex) {
			throw new IllegalArgumentException("WrongFinishDateFormat");
		}

		return userApi;
	}

}

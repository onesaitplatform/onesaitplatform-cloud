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
package com.minsait.onesait.platform.controlpanel.controller.apimanager;

import java.io.Serializable;

import com.minsait.onesait.platform.config.model.UserApi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class UserApiDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	@Getter
 	@Setter
 	private String id;
	@Getter
	@Setter
	private String userId;
	@Getter
	@Setter
	private String apiId;
	@Getter
	@Setter
	private String userFullName;
	@Getter
	@Setter
	private String apiIdentification;
	@Getter
	@Setter
	private Integer apiVersion;

	public UserApiDTO(UserApi userApi) {
		this.id = userApi.getId();
		this.userId = userApi.getUser().getUserId();
		this.apiId = userApi.getApi().getId();
		this.userFullName = userApi.getUser().getFullName();
		this.apiIdentification = userApi.getApi().getIdentification();
		this.apiVersion = userApi.getApi().getNumversion();
	}
}

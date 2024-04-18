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
package com.minsait.onesait.platform.config.dto;

import java.util.Date;

import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.User;

import lombok.Data;

@Data
public class ApiForList {

	private String id;
	private String identification;
	private String description;
	private Integer numversion;
	private User user;
	private ApiType apiType;
	private boolean isPublic;
	private byte[] image;
	private ApiStates state;
	private String imageType;
	private Integer apicachetimeout;
	private String graviteeId;
	private Date createdAt;
	private Date updatedAt;
	private boolean isSync = true;

	public ApiForList(String id, String identification, String description, Integer numversion, User user,
			ApiType apiType, boolean isPublic, byte[] image, ApiStates state, String imageType, Integer apicachetimeout,
			String graviteeId, Date created_at, Date updated_at) {
		super();
		this.id = id;
		this.identification = identification;
		this.description = description;
		this.numversion = numversion;
		this.user = user;
		this.apiType = apiType;
		this.isPublic = isPublic;
		this.image = image;
		this.state = state;
		this.imageType = imageType;
		this.apicachetimeout = apicachetimeout;
		this.graviteeId = graviteeId;
		this.createdAt = created_at;
		this.updatedAt = updated_at;
	}
}

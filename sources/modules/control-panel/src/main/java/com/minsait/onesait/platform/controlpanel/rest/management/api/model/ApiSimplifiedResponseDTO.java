/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.api.model;

import java.io.Serializable;

import com.minsait.onesait.platform.config.model.Api;

import lombok.Getter;
import lombok.Setter;

public class ApiSimplifiedResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	@Getter
	@Setter
	private String id;
	@Getter
	@Setter
	private String identification;
	@Getter
	@Setter
	private int version;
	@Getter
	@Setter
	private String msg;
	
	public ApiSimplifiedResponseDTO() {

	}

	public ApiSimplifiedResponseDTO(Api api) {
		this.id = api.getId();
		this.identification = api.getIdentification();
		this.version = api.getNumversion();
		this.msg = "OK";
	}
	
	public ApiSimplifiedResponseDTO(String apiId, String apiIdentification, int apiNumVersion, String msg) {
		this.id = apiId;
		this.identification = apiIdentification;
		this.version = apiNumVersion;
		this.msg = msg;
	}
	
}

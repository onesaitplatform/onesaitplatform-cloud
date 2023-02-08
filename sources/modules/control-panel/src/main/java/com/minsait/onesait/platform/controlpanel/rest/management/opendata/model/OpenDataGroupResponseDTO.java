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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata.model;

import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataGroup;

import lombok.Getter;
import lombok.Setter;

public class OpenDataGroupResponseDTO {
	
	@Getter
	@Setter
	private String id;
	
	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;
	
	public OpenDataGroupResponseDTO(OpenDataGroup o) {
		this.id = o.getName();
		this.identification = o.getTitle();
		this.description = o.getDescription();
	}

}
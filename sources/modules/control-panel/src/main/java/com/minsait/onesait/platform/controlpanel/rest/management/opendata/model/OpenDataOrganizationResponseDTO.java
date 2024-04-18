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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata.model;

import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganizationDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OpenDataOrganizationResponseDTO {
		
	@Getter
	@Setter
	private String id;
	
	@Getter
	@Setter
	private String identification;
	
	@Getter
	@Setter
	private String description;
	
	@Getter
	@Setter
	private OpenDataRole userRole;
	
	public OpenDataOrganizationResponseDTO(OpenDataOrganization o) {
		this.id = o.getName();
		this.identification = o.getTitle();
		this.description = o.getDescription();
		if (o.getCapacity() != null) {
			this.userRole = OpenDataRole.valueOf(o.getCapacity().toUpperCase());
		} else {
			this.userRole = null;
		}
	}
	
	public OpenDataOrganizationResponseDTO(OpenDataOrganization o, String userId) {
		this(o);
		OpenDataUser user = o.getUsers().stream().filter(u -> userId.equals(u.getName())).findAny().orElse(null);
		if (user != null) {
			this.userRole = OpenDataRole.valueOf(user.getCapacity().toUpperCase());
		}
	}
	
	public OpenDataOrganizationResponseDTO(OpenDataOrganizationDTO o) {
		this.id = o.getName();
		this.identification = o.getTitle();
		this.description = o.getDescription();
		this.userRole = OpenDataRole.valueOf(o.getRole().toUpperCase());
	}
	
}

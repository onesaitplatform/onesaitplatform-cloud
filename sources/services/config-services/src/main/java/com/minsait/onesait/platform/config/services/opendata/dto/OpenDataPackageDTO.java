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
package com.minsait.onesait.platform.config.services.opendata.dto;

import java.util.Date;
import java.util.List;

import com.minsait.onesait.platform.config.model.ODTypology;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class OpenDataPackageDTO {
	
	@Getter
	@Setter
	private String id;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String title;
	
	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private Boolean isPublic;

	@Getter
	@Setter
	private String organization;
	
	@Getter
	@Setter
	private String license;

	@Getter
	@Setter
	private List<String> tags;

	@Getter
	@Setter
	private Date createdAt;
	
	@Getter
	@Setter
	private Date updatedAt;
	
	@Getter
	@Setter
	private String role;
	
	@Getter
	@Setter
	private String typology;
	
	@Getter
	@Setter
	private List<String> files;
	
	public OpenDataPackageDTO(String name, String title, String organization) {
		this.name = name;
		this.title = title;
		this.organization = organization;
	}
	
	public OpenDataPackageDTO(String id, String name, String title, String organization) {
		this(name, title, organization);
		this.id = id;
	}

}

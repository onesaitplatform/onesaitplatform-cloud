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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;

import lombok.Getter;
import lombok.Setter;

public class OpenDataDatasetResponseDTO {

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
	private Boolean isPublic;
	
	@Getter
	@Setter
	private OpenDataOrganizationResponseSimplifiedDTO organization;
	
	@Getter
	@Setter
	private String license;

	@Getter
	@Setter
	private List<String> tags;

	@Getter
	@Setter
	private String typology;
	
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<BinaryFileResponseDTO> binaryFiles;
	
	@Getter
	@Setter
	private List<OpenDataResourceSimplifiedResponseDTO> resources;
	
	@Getter
	@Setter
	private String lastModified;
	
	@Getter
	@Setter
	private List<OpenDataGroupResponseDTO> groups;

	public OpenDataDatasetResponseDTO(OpenDataPackage o, String typology, List<BinaryFileResponseDTO> binaryFiles) {
		this.id = o.getName();
		this.identification = o.getTitle();
		this.description = o.getNotes();
		this.isPublic = !o.getIsPrivate();
		this.organization = new OpenDataOrganizationResponseSimplifiedDTO(o.getOrganization());
		this.license = o.getLicense_title();
		List<String> tagList = new ArrayList<>();
		o.getTags().forEach(tag -> tagList.add(tag.getName()));
		this.tags = tagList;
		this.typology = typology;
		this.binaryFiles = binaryFiles;
		List<OpenDataResourceSimplifiedResponseDTO> resourcesList = new ArrayList<>();
		o.getResources().forEach(resource -> resourcesList.add(new OpenDataResourceSimplifiedResponseDTO(resource)));
		this.resources = resourcesList;
		this.lastModified = o.getMetadata_modified();
		List<OpenDataGroupResponseDTO> groupsList = new ArrayList<>();
		o.getGroups().forEach(group -> groupsList.add(new OpenDataGroupResponseDTO(group)));
		this.groups = groupsList;
	}

	public OpenDataDatasetResponseDTO(OpenDataPackage o, String typology, boolean showResources, boolean showGroups) {
		this.id = o.getName();
		this.identification = o.getTitle();
		this.description = o.getNotes();
		this.isPublic = !o.getIsPrivate();
		this.organization = new OpenDataOrganizationResponseSimplifiedDTO(o.getOrganization());
		this.license = o.getLicense_title();
		List<String> tagList = new ArrayList<>();
		o.getTags().forEach(tag -> tagList.add(tag.getName()));
		this.tags = tagList;
		this.typology = typology;
		this.lastModified = o.getMetadata_modified();
		List<OpenDataResourceSimplifiedResponseDTO> resourceList = new ArrayList<>();
		if (showResources) {			
			o.getResources().forEach(resource -> resourceList.add(new OpenDataResourceSimplifiedResponseDTO(resource)));
		}
		this.resources = resourceList;
		List<OpenDataGroupResponseDTO> groupsList = new ArrayList<>();
		if (showGroups) {
			o.getGroups().forEach(group -> groupsList.add(new OpenDataGroupResponseDTO(group)));
		}
		this.groups = groupsList;
	}
}

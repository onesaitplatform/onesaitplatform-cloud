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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenDataPackage {
	
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
	private String license_id;
	
	@Getter
	@Setter
	private String license_title;
	
	@Getter
	@Setter
	private String license_url;
	
	@Getter
	@Setter
	private String notes;
	
	@JsonProperty("private")
	@Getter
	@Setter
	private Boolean isPrivate;
	
	@Getter
	@Setter
	private List<OpenDataTag> tags;
	
	@Getter
	@Setter
	private int num_tags;
	
	@Getter
	@Setter
	private String owner_org;
	
	@Getter
	@Setter
	private OpenDataOrganization organization;
	
	@Getter
	@Setter
	private String maintainer;
	
	@Getter
	@Setter
	private String maintainer_email;
	
	@Getter
	@Setter
	private List relationships_as_object;
	
	@Getter
	@Setter
	private List relationships_as_subject;
	
	@Getter
	@Setter
	private String author;
	
	@Getter
	@Setter
	private String author_email;
	
	@Getter
	@Setter
	private String state;
	
	@Getter
	@Setter
	private String version;
	
	@Getter
	@Setter
	private String creator_user_id;
	
	@Getter
	@Setter
	private String type;
	
	@Getter
	@Setter
	private List<OpenDataResource> resources;
	
	@Getter
	@Setter
	private int num_resources;
	
	@Getter
	@Setter
	private List<OpenDataGroup> groups;
	
	@Getter
	@Setter
	private boolean isopen;
	
	@Getter
	@Setter
	private String url;
	
	@Getter
	@Setter
	private List extras;
	
	@Getter
	@Setter
	private String revision_id;
	
	@Getter
	@Setter
	private String metadata_created;
	
	@Getter
	@Setter
	private String metadata_modified;
	
}

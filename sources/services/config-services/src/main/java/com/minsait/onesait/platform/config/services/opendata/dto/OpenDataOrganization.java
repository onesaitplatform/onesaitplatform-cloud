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
package com.minsait.onesait.platform.config.services.opendata.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
public class OpenDataOrganization {
	
	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String title;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String display_name;
	
	@Getter
	@Setter
	private String capacity;
	
	@Getter
	@Setter
	private String description;
	
	@Getter
	@Setter
	private String created;
	
	@Getter
	@Setter
	private boolean is_organization;
	
	@Getter
	@Setter
	private String state;
	
	@Getter
	@Setter
	private MultipartFile image_upload;
	
	@Getter
	@Setter
	private String image_url;
	
	@Getter
	@Setter
	private String image_display_url;
	
	@Getter
	@Setter
	private String revision_id;
	
	@Getter
	@Setter
	private String type;
	
	@Getter
	@Setter
	private String approval_status;
	
	@Getter
	@Setter
	private List<OpenDataUser> users;
	
	@Getter
	@Setter
	private List<OpenDataPackage> packages;

}

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
public class OpenDataResource {
	
	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String description;
	
	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String format;
	
	@Getter
	@Setter
	private String cache_url;
	
	@Getter
	@Setter
	private String hash;
	
	@Getter
	@Setter
	private String url;
	
	@Getter
	@Setter
	private boolean datastore_active;
	
	@Getter
	@Setter
	private String cache_last_updated;
	
	@Getter
	@Setter
	private String package_id;
	
	@Getter
	@Setter
	private String created;
	
	@Getter
	@Setter
	private String state;
	
	@Getter
	@Setter
	private String mimetype;
	
	@Getter
	@Setter
	private String mimetype_inner;
	
	@Getter
	@Setter
	private String last_modified;
	
	@Getter
	@Setter
	private int position;
	
	@Getter
	@Setter
	private String revision_id;
	
	@Getter
	@Setter
	private String url_type;
	
	@Getter
	@Setter
	private String resource_type;
	
	@Getter
	@Setter
	private String size;
}

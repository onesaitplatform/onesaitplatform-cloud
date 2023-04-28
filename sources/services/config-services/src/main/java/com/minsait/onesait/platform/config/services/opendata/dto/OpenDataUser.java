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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
public class OpenDataUser {
	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String display_name;
	
	@Getter
	@Setter
	private String fullname;
	
	@Getter
	@Setter
	private String capacity;
	
	@Getter
	@Setter
	private boolean sysadmin;
	
	@Getter
	@Setter
	private String about;
	
	@Getter
	@Setter
	private String email_hash;
	
	@Getter
	@Setter
	private String state;
	
	@Getter
	@Setter
	private boolean activity_streams_email_notifications;
	
	@Getter
	@Setter
	private String created;
	
	@Getter
	@Setter
	private int number_of_edits;
	
	@Getter
	@Setter
	private int number_created_packages;
}

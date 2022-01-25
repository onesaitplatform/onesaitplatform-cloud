/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.io.Serializable;
import java.util.Date;

import com.minsait.onesait.platform.config.model.User;

import lombok.Data;

@Data
public class OPResourceDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String identification;
	private String description;
	private Date createdAt;
	private Date updatedAt;
	private String group;
	private User user;
	private Integer version;

	public OPResourceDTO(String identification, String description, Date created_at, Date updated_at, User user,
			String group, Integer version) {
		super();
		this.identification = identification;
		this.description = description;
		this.createdAt = created_at;
		this.updatedAt = updated_at;
		this.group = group;
		this.user = user;
		this.version = version;
	}
}
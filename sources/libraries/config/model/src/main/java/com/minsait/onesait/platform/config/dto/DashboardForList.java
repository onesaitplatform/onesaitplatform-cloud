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

import java.util.Date;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;

import lombok.Data;

@Data
public class DashboardForList {
	
	private String id;
	private String identification;
	private String description;
	private User user;
	private boolean isPublic;
	private DashboardType type;
	private Date created_at;
	private Date updated_at;
	private String accessType;
	
	public DashboardForList(String id, String identification, String description, DashboardType type, User user, boolean isPublic, Date created_at, Date updated_at, String accessType) {
		this.id = id;
		this.identification = identification;
		this.description = description;
		this.type = type;
		this.user = user;
		this.isPublic = isPublic;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.accessType = accessType;
	}

}
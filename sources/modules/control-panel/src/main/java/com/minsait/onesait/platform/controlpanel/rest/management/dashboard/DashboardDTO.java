/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard;

import java.util.List;

import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardUserAccessDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class DashboardDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String user;

	@Getter
	@Setter
	private String url;

	@Getter
	@Setter
	private String category;

	@Getter
	@Setter
	private String subcategory;

	@Getter
	@Setter
	private String createdAt;

	@Getter
	@Setter
	private String modifiedAt;

	@Getter
	@Setter
	private String viewUrl;

	@Getter
	@Setter
	private int nGadgets;

	@Getter
	@Setter
	private String headerlibs;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private List<DashboardUserAccessDTO> dashboardAuths;
//	public DashboardDTO(String identification, String user, String url, String category, String subcategory,
//			Date createdAt, Date modifiedAt) {
//		super();
//		this.identification = identification;
//		this.user = user;
//		this.url = url;
//		this.category = category;
//		this.subcategory = subcategory;
//		this.createdAt = createdAt;
//		this.modifiedAt = modifiedAt;
//	}

}

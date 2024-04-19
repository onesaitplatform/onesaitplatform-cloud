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
package com.minsait.onesait.platform.controlpanel.rest.management.models.model;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.ModelExecution;
import com.minsait.onesait.platform.config.model.Subcategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ExecutionDTO {

	@Getter
	@Setter
	private String idEject;
	
	@Getter
	@Setter
	private String category;

	@Getter
	@Setter
	private String subcategory;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String model;

	@Getter
	@Setter
	private String createdAt;
	
	@Getter
	@Setter
	private String user;
	
	@Getter
	@Setter
	private String params;

	public ExecutionDTO(ModelExecution execution, Category category, Subcategory subcategory) {
		super();
		this.idEject = execution.getIdEject();
		this.category = category.getIdentification();
		this.subcategory = subcategory.getIdentification();
		this.identification = execution.getIdentification();
		this.description = execution.getDescription();
		this.model = execution.getModel().getIdentification();
		this.user = execution.getUser().getUserId();
		this.createdAt = execution.getCreatedAt().toString();
		this.params = execution.getParameters();
	}
	
	public ExecutionDTO(String idEject, String category, String subcategory, String identification, String description, String model,
			String userId, String createdAt, String params) {
		super();
		this.idEject = idEject;
		this.category = category;
		this.subcategory = subcategory;
		this.identification = identification;
		this.description = description;
		this.model = model;
		this.user = userId;
		this.createdAt = createdAt;
		this.params = params;
	}

}

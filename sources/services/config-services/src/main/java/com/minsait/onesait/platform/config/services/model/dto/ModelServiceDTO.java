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
package com.minsait.onesait.platform.config.services.model.dto;

import java.util.List;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.model.Subcategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ModelServiceDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;
	
	@Getter
	@Setter
	private String userId;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String notebook;

	@Getter
	@Setter
	private String dashboard;

	@Getter
	@Setter
	private String categorymodel;

	@Getter
	@Setter
	private String subcategorymodel;

	@Getter
	@Setter
	private String outputParagraphId;

	@Getter
	@Setter
	private String inputParagraphId;

	@Getter
	@Setter
	private String outputURL;

	@Getter
	@Setter
	private String createdAt;

	@Getter
	@Setter
	private List<ParameterModel> parameters;

	public ModelServiceDTO(Model model, Category category, Subcategory subcategory, String outputURL, List<ParameterModel> parameters) {
		this.id = model.getId();
		this.identification = model.getIdentification();
		this.description = model.getDescription();
		if (model.getNotebook() != null)
			this.notebook = model.getNotebook().getIdentification();
		if (model.getDashboard() != null)
			this.dashboard = model.getDashboard().getIdentification();
		this.categorymodel = category.getIdentification();
		this.subcategorymodel = subcategory.getIdentification();
		this.outputParagraphId = model.getOutputParagraphId();
		this.inputParagraphId = model.getInputParagraphId();
		this.outputURL = outputURL;
		this.parameters = parameters;
		this.createdAt = model.getCreatedAt().toString();
		this.userId = model.getUser().getUserId();
	}
	
	public ModelServiceDTO(String id, String identification, String description, String notebook, String dashboard,
			String categorymodel, String subcategorymodel, String outputParagraphId, String inputParagraphId,
			String outputURL, List<ParameterModel> parameters, String createdAt) {
		this.id = id;
		this.identification = identification;
		this.description = description;
		this.notebook = notebook;
		this.dashboard = dashboard;
		this.categorymodel = categorymodel;
		this.subcategorymodel = subcategorymodel;
		this.outputParagraphId = outputParagraphId;
		this.inputParagraphId = inputParagraphId;
		this.outputURL = outputURL;
		this.parameters = parameters;
		this.createdAt = createdAt;
	}
}

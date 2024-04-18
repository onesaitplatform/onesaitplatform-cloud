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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.config.model.DataModel;

import lombok.Getter;
import lombok.Setter;

public class DataModelDTO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String user;

	@Getter
	@Setter
	private String jsonSchema;

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
	private String type;

	@Setter
	@Getter
	private String description;

	@Setter
	@Getter
	private String labels;

	public static DataModelDTO fromDataModel(DataModel datamodel) {
		DataModelDTO dto = new DataModelDTO();
		dto.user = datamodel.getUser().getUserId();
		dto.jsonSchema = datamodel.getJsonSchema();
		dto.name = datamodel.getIdentification();
		dto.type = datamodel.getType();
		dto.description = datamodel.getDescription();
		dto.labels = datamodel.getLabels();

		return dto;
	}

	public static List<DataModelDTO> fromDataModels(List<DataModel> datamodel) {

		List<DataModelDTO> ldtos = new ArrayList<>();

		datamodel.forEach(dm -> {
			DataModelDTO dto = new DataModelDTO();
			dto.user = dm.getUser().getUserId();
			dto.jsonSchema = dm.getJsonSchema();
			dto.name = dm.getIdentification();
			dto.type = dm.getType();
			dto.description = dm.getDescription();
			dto.labels = dm.getLabels();

			ldtos.add(dto);
		});

		return ldtos;
	}

}

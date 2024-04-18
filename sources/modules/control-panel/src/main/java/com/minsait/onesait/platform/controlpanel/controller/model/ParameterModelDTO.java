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
package com.minsait.onesait.platform.controlpanel.controller.model;

import com.minsait.onesait.platform.config.model.ParameterModel;

import lombok.Getter;
import lombok.Setter;

public class ParameterModelDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private Integer from;

	@Getter
	@Setter
	private Integer to;

	@Getter
	@Setter
	private String type;

	@Getter
	@Setter
	private String enumerators;

	public ParameterModelDTO(ParameterModel param) {
		super();
		this.id = param.getId();
		this.identification = param.getIdentification();
		this.from = param.getRangeFrom();
		this.to = param.getRangeTo();
		this.type = param.getType().name();
		this.enumerators = param.getEnumerators();
	}

	public ParameterModelDTO(String id, String identification, Integer from, Integer to, String type,
			String enumerators) {
		super();
		this.id = id;
		this.identification = identification;
		this.from = from;
		this.to = to;
		this.type = type;
		this.enumerators = enumerators;
	}

}

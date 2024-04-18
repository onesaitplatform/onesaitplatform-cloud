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
package com.minsait.onesait.platform.controlpanel.controller.ontology;

import lombok.Getter;
import lombok.Setter;

public class OntologyRestOperationParamDTO {

	@Getter
	@Setter
	private Integer index;
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String type;
	@Getter
	@Setter
	private String field;

	public OntologyRestOperationParamDTO(Integer index, String name, String type, String field) {
		super();
		this.index = index;
		this.name = name;
		this.type = type;
		this.field = field;
	}
}

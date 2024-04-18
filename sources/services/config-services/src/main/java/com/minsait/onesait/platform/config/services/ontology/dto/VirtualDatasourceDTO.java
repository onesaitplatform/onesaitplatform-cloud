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
package com.minsait.onesait.platform.config.services.ontology.dto;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;

import lombok.Getter;
import lombok.Setter;

public class VirtualDatasourceDTO {

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String domain;
	
	public VirtualDatasourceDTO(OntologyVirtualDatasource ontologyVirtualDatasource) {
		super();
		this.identification = ontologyVirtualDatasource.getDatasourceName();
		this.domain = ontologyVirtualDatasource.getDatasourceDomain();

	}

	public VirtualDatasourceDTO(String identification, String domain) {
		super();
		this.identification = identification;
		this.domain = domain;
	}


}
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
package com.minsait.onesait.platform.bean;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

public class OntologyDTO extends AuditableEntityWithUUID {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String user;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private boolean active;

	@Getter
	@Setter
	private DataModel dataModel;

	@Getter
	@Setter
	private RtdbDatasource rtdbDatasource;

	@Getter
	@Setter
	private String rtdbDatasourceType;

}
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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyVirtualDataSourceDTO {

	@Getter
	@Setter
	private String createdAt;

	@Getter
	@Setter
	private String updatedAt;

	@NotNull
	@Getter
	@Setter
	private String connectionString;

	@Getter
	@Setter
	private String user;

	@Getter
	@Setter
	private String credentials;

	@NotNull
	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String domain;

	@Getter
	@Setter
	private boolean isPublic = false;

	@Getter
	@Setter
	private String poolSize;

	@Getter
	@Setter
	private int queryLimit;

	@NotNull
	@Getter
	@Setter
	private VirtualDatasourceType sgbd;

	@NotNull
	@Getter
	@Setter
	private String userId;

	public OntologyVirtualDataSourceDTO(OntologyVirtualDatasource ontologyVirtualDataSource) {
		this.createdAt = ontologyVirtualDataSource.getCreatedAt().toString();
		this.updatedAt = ontologyVirtualDataSource.getUpdatedAt().toString();
		this.connectionString = ontologyVirtualDataSource.getConnectionString();
		this.credentials = ontologyVirtualDataSource.getCredentials();
		this.name = ontologyVirtualDataSource.getIdentification();
		this.domain = ontologyVirtualDataSource.getDatasourceDomain();
		this.isPublic = ontologyVirtualDataSource.isPublic();
		this.poolSize = ontologyVirtualDataSource.getPoolSize();
		this.queryLimit = ontologyVirtualDataSource.getQueryLimit();
		this.sgbd = ontologyVirtualDataSource.getSgdb();
		this.userId = ontologyVirtualDataSource.getUser().getUserId();
		this.user = ontologyVirtualDataSource.getUserId();
	}

}

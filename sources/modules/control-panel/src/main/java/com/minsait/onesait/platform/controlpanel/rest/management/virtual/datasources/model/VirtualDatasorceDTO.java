/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.virtual.datasources.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class VirtualDatasorceDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;

	
	@Getter
	@Setter
	private String datasourceDomain;

	@NotNull
	@Getter
	@Setter
	private String sgdb;

	@NotNull
	@Getter
	@Setter
	private String connectionString;

	@Getter
	@Setter
	private String userId;

	@Getter
	@Setter
	private String credentials;

	@NotNull
	@Getter
	@Setter
	private int queryLimit;

	@NotNull
	@Getter
	@Setter
	private String poolSize;

	@NotNull
	@Getter
	@Setter
	private boolean isPublic;
	
	@Getter
	@Setter
	private Date createdAt;
	
	@Getter
	@Setter
	private Date updatedAt;

	public static VirtualDatasorceDTO convert (OntologyVirtualDatasource o) {
		return VirtualDatasorceDTO.builder().id(o.getId()).identification(o.getIdentification()).datasourceDomain(o.getDatasourceDomain()).sgdb(o.getSgdb().name()).connectionString(o.getConnectionString()).
				userId(o.getUserId()).credentials(o.getCredentials()).queryLimit(o.getQueryLimit()).poolSize(o.getPoolSize()).isPublic(o.isPublic()).createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt()).build();
	}
	
	public static VirtualDatasorceDTO convertNoCredentials (OntologyVirtualDatasource o) {
		return VirtualDatasorceDTO.builder().id(o.getId()).identification(o.getIdentification()).datasourceDomain(o.getDatasourceDomain()).sgdb(o.getSgdb().name()).
				queryLimit(o.getQueryLimit()).poolSize(o.getPoolSize()).isPublic(o.isPublic()).createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt()).build();
	}
	
	public static OntologyVirtualDatasource convertFromDTO (VirtualDatasorceDTO o) {
		OntologyVirtualDatasource ontologyVirtualDatasource = new OntologyVirtualDatasource();
		ontologyVirtualDatasource.setId(o.getId());
		ontologyVirtualDatasource.setIdentification(o.getIdentification());
		ontologyVirtualDatasource.setDatasourceDomain(o.getDatasourceDomain());
		ontologyVirtualDatasource.setSgdb(OntologyVirtualDatasource.VirtualDatasourceType.valueOf(o.getSgdb()));
		ontologyVirtualDatasource.setConnectionString(o.getConnectionString());
		ontologyVirtualDatasource.setUserId(o.getUserId());
		ontologyVirtualDatasource.setCredentials(o.getCredentials());
		ontologyVirtualDatasource.setQueryLimit(o.getQueryLimit());
		ontologyVirtualDatasource.setPoolSize(o.getPoolSize());
		ontologyVirtualDatasource.setPublic(o.isPublic());
		return ontologyVirtualDatasource;
	}
	
}

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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_VIRTUAL_DATASOURCE")
public class OntologyVirtualDatasource extends OPResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Se guarda por índice en la tabla ontology_virtual_datasource así que no se
	 * puede cambiar orden
	 **/
	public enum VirtualDatasourceType {
		ORACLE, ORACLE11, MYSQL, MARIADB, SQLSERVER, POSTGRESQL, IMPALA, HIVE, OP_QUERYDATAHUB, KUDU, PRESTO
	}

	@Column(name = "DATASOURCE_DOMAIN", length = 128, nullable = true)
	@Getter
	@Setter
	private String datasourceDomain;

	@Column(name = "SGDB", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private VirtualDatasourceType sgdb;

	@Column(name = "CONNECTION_STRING", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String connectionString;

	@Column(name = "USER", length = 128, nullable = true)
	@Getter
	@Setter
	private String userId;

	@Column(name = "CREDENTIALS", length = 128, nullable = true)
	@Getter
	@Setter
	private String credentials;

	@Column(name = "QUERY_LIMIT")
	@NotNull
	@Getter
	@Setter
	private int queryLimit;

	@Column(name = "POOL_SIZE")
	@NotNull
	@Getter
	@Setter
	private String poolSize;

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Transient
	public String getValidationQuery() {
		switch (sgdb) {
		case ORACLE:
		case ORACLE11:
			return "select 1 from dual";
		case MYSQL:
		case MARIADB:
		case SQLSERVER:
		case POSTGRESQL:
		case HIVE:
		case IMPALA:
		default:
			return "select 1";
		}
	}

	@Column(name = "VALIDATION_QUERY_TIMEOUT", columnDefinition = "integer default 5")
	@Getter
	@Setter
	private Integer validationQueryTimeout;

	@Column(name = "TEST_ON_BORROW")
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("true")
	@Getter
	@Setter
	private Boolean testOnBorrow;

	@Column(name = "TEST_WHILE_IDLE")
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("true")
	@Getter
	@Setter
	private Boolean testWhileIdle;

}

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
package com.minsait.onesait.platform.config.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_VIRTUAL_DATASOURCE")
public class OntologyVirtualDatasource extends AuditableEntityWithUUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum VirtualDatasourceType {
		ORACLE,ORACLE11,MYSQL,MARIADB,SQLSERVER,POSTGRESQL,IMPALA,HIVE
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User userId;

	@OneToMany(mappedBy = "datasourceId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private Set<OntologyVirtual> ontologyVirtuals = new HashSet<>();

	@Column(name = "DATASOURCE_NAME", length = 128, nullable = false, unique = true)
	@NotNull
	@Getter
	@Setter
	private String datasourceName;

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

	@Column(name = "USER", length = 128, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String user;

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

	@Column(name = "PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

}

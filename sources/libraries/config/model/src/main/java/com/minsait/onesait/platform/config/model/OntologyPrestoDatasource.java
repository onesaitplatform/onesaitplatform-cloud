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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_PRESTO_DATASOURCE")
public class OntologyPrestoDatasource extends OPResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum PrestoDatasourceType {
		ACCUMULO ("accumulo"), 
		BIGQUERY ("bigquery"), 
		BLACKHOLE ("blackhole"), 
		CASSANDRA ("cassandra"), 
		CLICKHOUSE ("clickhouse"), 
		DELTA_LAKE ("delta"), 
		DRUID ("druid"), 
		ELASTICSEARCH ("elasticsearch"), 
		HIVE ("hive-hadoop2"),
		HUDI ("hudi"),
		ICEBERG ("iceberg"),
		JMX ("jmx"),
		KAFKA ("kafka"),
		KUDU ("kudu"),
		LARK_SHEETS ("lark-sheets"),
		LOCAL_FILE ("localfile"),
		MEMORY ("memory"),
		MONGODB ("mongodb"),
		MYSQL ("mysql"),
		ORACLE ("oracle"),
		APACHE_PINOT ("pinot"),
		POSTGRESQL ("postgresql"),
		PROMETHEUS ("prometheus"),
		REDIS ("redis"),
		REDSHIFT ("redshift"),
		SQL_SERVER ("sqlserver"),
		THRIFT ("presto-thrift"),
		TPCDS ("tpcds"),
		TPCH ("tpch");
		
		@Getter
		private final String prestoDatasourceType;

		private PrestoDatasourceType(String s) {
			this.prestoDatasourceType = s;
		}
	}

	@Column(name = "TYPE", length = 255, nullable = false)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private PrestoDatasourceType type;
	
	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

}
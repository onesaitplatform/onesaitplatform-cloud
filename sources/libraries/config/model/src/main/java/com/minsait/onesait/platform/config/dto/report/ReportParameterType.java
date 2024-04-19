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
package com.minsait.onesait.platform.config.dto.report;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 *
 * MySQL Type Java Type ---------- --------- VARCHAR java.lang.String NUMERIC
 * java.math.BigDecimal DECIMAL java.math.BigDecimal BIT java.lang.Boolean
 * INTEGER java.lang.Integer BIGINT java.lang.Long FLOAT java.lang.Double DOUBLE
 * java.lang.Double DATE java.sql.Date TIME java.sql.Time TIMESTAMP
 * java.sql.Tiimestamp
 *
 * @author aponcep
 *
 */
public enum ReportParameterType {

	@JsonEnumDefaultValue
	STRING("java.lang.String", "VARCHAR"), INTEGER("java.lang.Integer", "INTEGER"), DOUBLE("java.lang.Double",
			"DOUBLE"), DATE("java.util.Date", "DATE"), LIST("java.util.List",
					"LIST"), COLLECTION("java.util.Collection", "COLLECTION"), BOOLEAN("java.lang.Boolean", "BOOLEAN");

	private String javaType;
	private String dbType;

	private ReportParameterType(String javaType, String dbType) {
		this.javaType = javaType;
		this.dbType = dbType;
	}

	public String getDbType() {
		return dbType;
	}

	public String getJavaType() {
		return javaType;
	}

	public static ReportParameterType fromDatabaseType(String dbType) {

		final ReportParameterType[] values = ReportParameterType.values();

		for (final ReportParameterType value : values) {
			if (value.dbType.equals(dbType)) {
				return value;
			}
		}

		return null;
	}

	public static ReportParameterType fromJavaType(String javaType) {

		final ReportParameterType[] values = ReportParameterType.values();

		for (final ReportParameterType value : values) {
			if (value.javaType.equals(javaType)) {
				return value;
			}
		}

		return null;
	}
}

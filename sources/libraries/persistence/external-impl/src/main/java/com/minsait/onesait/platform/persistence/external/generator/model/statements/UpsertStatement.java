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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;

import lombok.Getter;
import lombok.Setter;

public class UpsertStatement implements SQLStatement {

	@NotNull
	@Size(min = 1)
	@Getter
	@Setter
	private String ontology;
	@NotNull
	@Getter
	@Setter
	private Map<String, String> values;
	private final SQLGenerator sqlGenerator;
	@Getter
	@Setter
	private String uniqueIDValue;
	@Getter
	@Setter
	private String uniqueID;

	public UpsertStatement(SQLGenerator sqlGenerator, String uniqueID) {
		this.sqlGenerator = sqlGenerator;
		this.uniqueID = uniqueID;
	}

	public UpsertStatement(SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public UpsertStatement withOntology(String ontology) {
		this.ontology = ontology;
		return this;
	}

	public UpsertStatement withValues(Map<String, String> values) {
		this.values = values;
		return this;
	}

	public UpsertStatement withUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
		return this;
	}

	public UpsertStatement setUniqueIDWithValue(final String uniqueIdentifier) {
		if (values != null && !values.isEmpty() && values.get(uniqueIdentifier) != null) {
			this.uniqueIDValue = values.get(uniqueIdentifier);
			this.uniqueID = uniqueIdentifier;
			return this;
		} else {
			throw new IllegalArgumentException("OID could not be set");
		}
	}

	@Override
	public PreparedStatement generate(boolean withParams) {
		if (sqlGenerator != null) {
			return sqlGenerator.generate(this, withParams);
		} else {
			throw new IllegalStateException(
					"SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}

}

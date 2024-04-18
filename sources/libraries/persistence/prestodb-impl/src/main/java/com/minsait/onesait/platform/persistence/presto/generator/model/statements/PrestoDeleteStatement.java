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
package com.minsait.onesait.platform.persistence.presto.generator.model.statements;

import com.minsait.onesait.platform.persistence.presto.generator.PrestoSQLGenerator;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.PrestoOrderByStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.PrestoWhereStatement;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class PrestoDeleteStatement implements PrestoSQLStatement {
	@NotNull
	@Size(min = 1)
	private String ontology;
	@NotNull
	private List<PrestoWhereStatement> where;
	private List<PrestoOrderByStatement> orderBy;
	@Min(1)
	private PrestoSQLGenerator sqlGenerator;

	public PrestoDeleteStatement() {
	}

	public PrestoDeleteStatement(final PrestoSQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public String getOntology() {
		return ontology;
	}

	public PrestoDeleteStatement setOntology(final String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()){
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public List<PrestoWhereStatement> getWhere() {
		return where;
	}

	public PrestoDeleteStatement setWhere(final List<PrestoWhereStatement> where) {
		this.where = where;
		return this;
	}

	public List<PrestoOrderByStatement> getOrderBy() {
		return orderBy;
	}

	public PrestoDeleteStatement setOrderBy(final List<PrestoOrderByStatement> orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	@Override
	public PrestoPreparedStatement generate(boolean withParams) {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this, withParams);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
}

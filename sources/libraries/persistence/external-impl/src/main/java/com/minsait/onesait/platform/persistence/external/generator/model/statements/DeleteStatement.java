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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.model.common.OrderByStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class DeleteStatement implements SQLStatement {
	@NotNull
	@Size(min = 1)
	private String ontology;
	@NotNull
	private List<WhereStatement> where;
	private List<OrderByStatement> orderBy;
	@Min(1)
	private SQLGenerator sqlGenerator;

	public DeleteStatement() {
	}

	public DeleteStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public String getOntology() {
		return ontology;
	}

	public DeleteStatement setOntology(final String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()){
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public List<WhereStatement> getWhere() {
		return where;
	}

	public DeleteStatement setWhere(final List<WhereStatement> where) {
		this.where = where;
		return this;
	}

	public List<OrderByStatement> getOrderBy() {
		return orderBy;
	}

	public DeleteStatement setOrderBy(final List<OrderByStatement> orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	@Override
	public PreparedStatement generate(boolean withParams) {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this, withParams);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
}

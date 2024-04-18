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

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
public class PrestoSelectStatement implements PrestoSQLStatement {
	@NotNull
	@Size(min = 1)
	@Getter
	private String ontology;
	private String alias;
	@Getter
	private List<String> columns;
	@NotNull
	@Getter
	private List<PrestoWhereStatement> where;
	@NotNull
	@Getter
	private List<PrestoOrderByStatement> orderBy;
	@Min(1)
	@Getter
	private Long limit;
	@Min(0)
	@Getter
	private Long offset;
	private PrestoSQLGenerator sqlGenerator;

	public PrestoSelectStatement(final PrestoSQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public PrestoSelectStatement setOntology(final String ontology) {
		if(ontology != null && !ontology.trim().isEmpty()){
			this.ontology = ontology.trim();
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	public PrestoSelectStatement setColumns(final List<String> columns) {
		this.columns = columns;
		return this;
	}

	public PrestoSelectStatement setWhere(final List<PrestoWhereStatement> where) {
		this.where = where;
		return this;
	}

	public PrestoSelectStatement setOrderBy(final List<PrestoOrderByStatement> orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public PrestoSelectStatement setLimit(final long limit) {
		if(limit <= 0) {
			throw new IllegalArgumentException("Limit can't be lower or equals than 0");
		} else{
			this.limit = limit;
			return this;
		}
	}

	public PrestoSelectStatement setOffset(final long offset) {
		if(offset < 0) {
			throw new IllegalArgumentException("Offset can't be lower than 0");
		} else{
			this.offset = offset;
			return this;
		}
	}

	@Override
	public PrestoPreparedStatement generate(boolean withParams) {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this, withParams);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
	
	public void setAlias(String alias) {
	    this.alias = alias;
	}

	public String getAlias() {
	    return alias;
	}
}

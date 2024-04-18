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
package com.minsait.onesait.platform.persistence.external.generator.model.statements;

import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.drop.Drop;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class DropStatement extends Drop implements SQLStatement {
	
	@Size(min = 1)
	private String ontology;
	@Min(1)
	private SQLGenerator sqlGenerator;
	
	private static final String TABLE = "TABLE";
	
	public DropStatement() {
		setType(TABLE);
	}
	
	public DropStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
		setType(TABLE);
	}

	public String getOntology() {
		return this.getName().toString();
	}
	
	public DropStatement setCheckIfExists(final boolean ifExists) {
		setIfExists(ifExists);
		return this;
		
	}

	public DropStatement setOntology(final String ontology) {
		if (getType() == null) {
			setType(TABLE);
		}
		if(ontology != null && !ontology.trim().isEmpty()){
			this.setName(new Table(ontology));
			return this;
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
	}

	@Override
	public PreparedStatement generate(boolean withParams) {
		
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
}

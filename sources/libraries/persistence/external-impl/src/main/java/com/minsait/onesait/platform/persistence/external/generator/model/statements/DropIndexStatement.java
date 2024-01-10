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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;

@NoArgsConstructor
public class DropIndexStatement extends ShowIndexStatement implements SQLStatement {
	
	@NotNull
	@Getter
	private String ontologyVirtual;
	
	@NotNull
	@Getter
	private String ontology;
	
	@NotNull
	@Getter
	private String columName;
	
	@Getter
	@Setter
	public VirtualDatasourceType virtualDatasourceType;
	
	@NotNull
	@Getter
	@Setter
	private SQLGenerator sqlGenerator;


	public DropIndexStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}
	
	public DropIndexStatement setOntologyVirtual(String ontologyVirtual) {
		this.ontologyVirtual = ontologyVirtual;
		return this;
	}
	
	public DropIndexStatement setOntology(String ontology) {
		this.ontology = ontology;
		return this;
	}
	
	public DropIndexStatement setColumName(String columName) {
		this.columName = columName;
		return this;
	}
	
	@Override
	public PreparedStatement generate(boolean withParams) {
		if(sqlGenerator != null) {
			return sqlGenerator.generate(this);
		} else {
			throw new IllegalStateException("SQL Generator service is not set, use SQLGenerator instance to generate or build the statement instead");
		}
	}
	
	@Override
    public String toString() {
		if (this.getVirtualDatasourceType().equals(VirtualDatasourceType.ORACLE)) {
			return ("DROP INDEX "+ this.getColumName()+"_"+this.getOntologyVirtual() + " ON "+ this.getOntologyVirtual()+"_index");
		} else if (this.getVirtualDatasourceType().equals(VirtualDatasourceType.POSTGRESQL)) {
			return ("DROP INDEX "+ this.getColumName()+"_"+this.getOntologyVirtual() +"_index");
		} else {
			return ("ALTER TABLE "+ this.getOntologyVirtual() +" DROP INDEX "+ this.getColumName()+"_"+this.getOntologyVirtual()  +"_index");
		}
	}

}

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

package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.sql;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.business.services.ontology.ConstraintBusiness;
import com.minsait.onesait.platform.persistence.external.generator.model.common.Constraint.ConstraintType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ConstraintDTO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Getter
	@Setter
	private String name = null;
	@Getter
	@Setter
	@NotNull
	private ConstraintType type = null;
	@Getter
	@Setter
	@NotNull
	private List<String> columns = null;
	@Getter
	@Setter
	private String referencedTable = null;
	@Getter
	@Setter
	private String referencedColumn = null;
	
	public ConstraintDTO(ConstraintBusiness constraint) {
		this.name = constraint.getName();
		this.type = constraint.getType();
		this.columns = constraint.getColumns();
		this.referencedTable = constraint.getReferencedTable();
		this.referencedColumn = constraint.getReferencedColumn();
	}
	
	public ConstraintBusiness toConstraint() {
		ConstraintBusiness constraint = new ConstraintBusiness();
		constraint.setName(this.name);
		constraint.setType(this.type);
		constraint.setColumns(this.columns);
		constraint.setReferencedTable(this.referencedTable);
		constraint.setReferencedColumn(this.referencedColumn);
		
		return constraint;
	}

}

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
package com.minsait.onesait.platform.controlpanel.controller.ontology.model.sql;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.business.services.ontology.ColumnDefinitionBusiness;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class ColumnDefinitionDTO implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Getter
	@Setter
	@NotNull
	private String name;
	@Getter
	@Setter
	@NotNull
	private String type;
	@Getter
	@Setter
	private boolean notNull = false;
	@Getter
	@Setter
	private boolean autoIncrement = false;
	@Getter
	@Setter
    private Object defautlValue = null;
	@Getter
	@Setter
    private String colComment = null;
	
	public ColumnDefinitionDTO(ColumnDefinitionBusiness column) {
		this.name = column.getName();
		this.type= column.getType();
		this.notNull = column.isNotNull();
		this.autoIncrement = column.isAutoIncrement();
		this.defautlValue = column.getDefautlValue();
		this.colComment = column.getColComment();
	}
	
	public ColumnDefinitionBusiness toColumnRelational() {
		ColumnDefinitionBusiness column = new ColumnDefinitionBusiness();
		column.setName(this.name);
		column.setType(this.type);
		column.setNotNull(this.notNull);
		column.setAutoIncrement(this.autoIncrement);
		column.setDefautlValue(this.defautlValue);
		column.setColComment(this.colComment);
		return column;
	}

}

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

package com.minsait.onesait.platform.business.services.ontology;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.persistence.external.generator.model.common.ColumnRelational;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.KuduColumn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.statement.create.table.ColDataType;

@NoArgsConstructor
@AllArgsConstructor
public class ColumnDefinitionBusiness implements java.io.Serializable {

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
	
	public ColumnDefinitionBusiness(ColumnRelational column) {
		this.name = column.getColumnName();
		this.type= column.getColDataType().getDataType();
		this.notNull = column.isNotNull();
		this.autoIncrement = column.isAutoIncrement();
		this.defautlValue = column.getColDefautlValue();
		this.colComment = column.getColComment();
	}
	
	public ColumnRelational toColumnRelational() {
		ColumnRelational column = new ColumnRelational();
		column.setColumnName(this.name);
		ColDataType dType = new ColDataType();
		dType.setDataType(this.type);
		column.setColDataType(dType);
		column.setNotNull(this.notNull);
		column.setAutoIncrement(this.autoIncrement);
		column.setColDefautlValue(this.defautlValue);
		column.setColComment(this.colComment);
		
		return column;
	}
	
	public ColumnDefinitionBusiness(KuduColumn column) {
		this.name = column.getName();
		this.type= column.getColumnType();
		this.notNull = column.isRequired();
		this.defautlValue = column.getDefaultValue();
		this.colComment = column.getComment();
	}
	
	public KuduColumn toKuduColumn() {
		KuduColumn column = new KuduColumn();
		column.setName(this.name);
		column.setColumnType(this.type);
		column.setRequired(this.notNull);
		column.setDefaultValue(this.defautlValue);
		column.setComment(this.colComment);
		
		return column;
	}

}

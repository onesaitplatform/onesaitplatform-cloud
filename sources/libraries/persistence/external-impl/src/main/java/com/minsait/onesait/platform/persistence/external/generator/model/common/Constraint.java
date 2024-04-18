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
package com.minsait.onesait.platform.persistence.external.generator.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.statement.create.table.Index;

@NoArgsConstructor
@AllArgsConstructor
public class Constraint extends Index {

	public enum ConstraintType {
		PRIMARY_KEY, FOREIGN_KEY, UNIQUE
	}
	
	@Getter
	private ConstraintType enumType = null;
	@Getter
	@Setter
	private String referencedTable = null;
	@Getter
	@Setter
	private String referencedColumn = null;
	
	@Override
	public void setName(String name) {
		if (name == null) {
			super.setName((String)null);
		}
		else {
			super.setName(name.trim().replace(" ", "_"));
		}
	}
	
	@Override
	public void setType(String type) {
		type = type.toUpperCase();
		if (type.contains("PRIMARY")) {
			enumType = ConstraintType.PRIMARY_KEY;
		}
		else if(type.contains("FOREIGN")) {
			enumType = ConstraintType.FOREIGN_KEY;
		}
		
		else if(type.contains("UNIQUE")) {
			enumType = ConstraintType.UNIQUE;
		}
		
		super.setType(type);
		
	}
	
	public void setTypeEnum(ConstraintType type) {
		setType(type.name().replace("_", " "));
	}
	
	    
}

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

import java.util.ArrayList;
import java.util.HashMap;
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
public class GetIndexStatement extends ShowIndexStatement implements SQLStatement {
	
	@NotNull
	@Getter
	private String dataTable;
	
	@NotNull
	@Getter
	private String ontology;
	
	@Getter
	@Setter
	public VirtualDatasourceType virtualDatasourceType;
	
	@NotNull
	@Getter
	@Setter
	private SQLGenerator sqlGenerator;


	public GetIndexStatement(final SQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}
	
	public GetIndexStatement setOntology(String ontology) {
		this.ontology = ontology;
		return this;
	}
	
	public GetIndexStatement setDatatable(String dataTable) {
		this.dataTable = dataTable;
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
			return ("SELECT index_name, table_name, column_name FROM all_ind_columns WHERE table_name = " + this.getDataTable()+"" );
		} else if (this.getVirtualDatasourceType().equals(VirtualDatasourceType.POSTGRESQL)) {
			return ("SELECT indexname AS index_name, indexdef AS column_name FROM pg_indexes WHERE tablename = '" + this.getDataTable() + "'");
		} else {
			return ("SHOW INDEX FROM " + this.getDataTable() + "");
		}
	}

	public Map<String, List<String>> parseListIndex(List<Map<String, Object>> listIndex) {
        
		Map<String,List<String>> indexMap = new HashMap<>();
		
		if (this.getVirtualDatasourceType().equals(VirtualDatasourceType.ORACLE)) {

			 for (Map<String, Object> indexObject : listIndex) {
		            if (indexObject.containsKey("column_name")) {
		            	List<String> existingIndexes;
		            	if(indexMap.get(indexObject.get("column_name").toString()) == null){
		            		existingIndexes = new  ArrayList<>();
		            	}
		            	else {
		            		existingIndexes = indexMap.get(indexObject.get("column_name").toString());
		            	}
		            	existingIndexes.add(indexObject.get("index_name").toString());
	            		indexMap.put(indexObject.get("column_name").toString(), existingIndexes);
		            	
		            }
			 }
		
		} else if (this.getVirtualDatasourceType().equals(VirtualDatasourceType.POSTGRESQL)) {
			 for (Map<String, Object> indexObject : listIndex) {
		            if (indexObject.containsKey("column_name")) {
		            	List<String> existingIndexes;
		            	 int start = indexObject.get("column_name").toString().indexOf("(");
		                 int end = indexObject.get("column_name").toString().indexOf(")");
		            	if(indexMap.get(indexObject.get("column_name").toString().substring(start + 1, end)) == null){
		            		existingIndexes = new  ArrayList<>();
		            	}
		            	else {
		            		existingIndexes = indexMap.get(indexObject.get("column_name").toString());
		            	}
		            	existingIndexes.add(indexObject.get("index_name").toString());
	            		indexMap.put(indexObject.get("column_name").toString().substring(start + 1, end), existingIndexes);
		            }
			 }
		
		}else {
			 for (Map<String, Object> indexObject : listIndex) {
		            if (indexObject.containsKey("Column_name")) {
		            	List<String> existingIndexes;
		            	if(indexMap.get(indexObject.get("Column_name").toString()) == null){
		            		existingIndexes = new  ArrayList<>();
		            	}
		            	else {
		            		existingIndexes = indexMap.get(indexObject.get("Column_name").toString());
		            	}
		            	existingIndexes.add(indexObject.get("key_name").toString());
	            		indexMap.put(indexObject.get("Column_name").toString(), existingIndexes);
		            	
		            }
			 }
		}
		
		return indexMap;
	}

}

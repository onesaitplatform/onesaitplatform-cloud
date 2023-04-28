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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.List;

import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class SQLTableReplacer {
	
    public static String replaceTableNameInSelect(String querySQL, OntologyVirtualRepository ontologyVirtualRepository, List<String> excludeParse)
            throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(querySQL);

        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {
            @Override
            public void visit(Table tableName) {
            	
                OntologyVirtual ontologyVirtual = ontologyVirtualRepository
                        .findOntologyVirtualByOntologyIdentification(tableName.getName());
                
            	if(ontologyVirtual != null ) {
            		String ontologyPath = SQLTableReplacer.getOntologyPath(ontologyVirtual); 
	                if ( null!=ontologyPath && !ontologyPath.equals("")) {
	                    getBuffer().append(ontologyPath).append(' ');
	                    if (tableName.getAlias() != null) {
	                        getBuffer().append(tableName.getAlias().getName());
	                    } else {
	                        getBuffer().append(tableName.getName()).append(' ');
	                        if (tableName.getAlias() != null) {
	                            getBuffer().append(tableName.getAlias().getName());
	                        }
	                    }
	                }
            	}
            	else {
            		String tableStr = tableName.getName();
            		if(excludeParse.indexOf(tableStr.toLowerCase()) != -1) {//no translate exception
            			getBuffer().append(tableStr + " ");
                	}
            		else {
            			getBuffer().append("{unknown ontology} ");
            		}
            	}
            }
        };
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);

        return (buffer.toString());
    }

    public static String replaceTableNameInInsert(String insertSQL, OntologyVirtual ontologyVirtual) throws JSQLParserException {
    	String newTablePath = SQLTableReplacer.getOntologyPath(ontologyVirtual);
    	
        Insert insertSQLObj = (Insert) CCJSqlParserUtil.parse(insertSQL);

        Table newTable = new Table();
        newTable.setName(newTablePath);

        insertSQLObj.setTable(newTable);

        return insertSQLObj.toString();
    }

    public static String replaceTableNameInDelete(String deleteSQL, OntologyVirtual ontologyVirtual) throws JSQLParserException {
    	String newTablePath = SQLTableReplacer.getOntologyPath(ontologyVirtual);
    	
        Delete deleteSQLObj = (Delete) CCJSqlParserUtil.parse(deleteSQL);

        Table newTable = new Table();
        newTable.setName(newTablePath);

        deleteSQLObj.setTable(newTable);

        return deleteSQLObj.toString();
    }

    public static String replaceTableNameInUpdate(String updateSQL, String oldTableName, OntologyVirtual ontologyVirtual)
            throws JSQLParserException {
    	String newTablePath = SQLTableReplacer.getOntologyPath(ontologyVirtual);
    	
        Update updateSQLObj = (Update) CCJSqlParserUtil.parse(updateSQL);

        Table table = updateSQLObj.getTable();
        if (table.getName().equals(oldTableName)) {
            table.setName(newTablePath);
        }
        return updateSQLObj.toString();
    }
    
    private static String getOntologyPath(OntologyVirtual ontologyVirtual) {
    	String ontologyVirtualTable =  ontologyVirtual.getDatasourceTableName();
		String ontologyVirtualDatabase =  ontologyVirtual.getDatasourceDatabase();
		String ontologyVirtualSchema =  ontologyVirtual.getDatasourceSchema();
		if (ontologyVirtualTable == null || "".equals(ontologyVirtualTable)) {
			return null;
		}
		if(VirtualDatasourceType.POSTGRESQL.equals(ontologyVirtual.getDatasourceId().getSgdb())) {
			ontologyVirtualTable = "\"" + ontologyVirtualTable + "\"";
			if(ontologyVirtualDatabase!=null && !"".equals(ontologyVirtualDatabase)) {
				ontologyVirtualDatabase = "\"" + ontologyVirtualDatabase + "\"";
			}
			if(ontologyVirtualSchema!=null && !"".equals(ontologyVirtualSchema)) {
				ontologyVirtualSchema = "\"" + ontologyVirtualSchema + "\"";
			}
		}
    	return (ontologyVirtualDatabase==null || "".equals(ontologyVirtualDatabase)?"":ontologyVirtualDatabase+".") + 
    			(ontologyVirtualSchema==null || "".equals(ontologyVirtualSchema)?"":ontologyVirtualSchema+".") +
    			ontologyVirtualTable;
    }

}

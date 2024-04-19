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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.List;

import com.minsait.onesait.platform.config.model.OntologyVirtual;
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
            		String ontologyVirtualTable =  ontologyVirtual.getDatasourceTableName();
	                if ( null!=ontologyVirtualTable && !ontologyVirtualTable.equals("")) {
	                    getBuffer().append(ontologyVirtualTable).append(' ');
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

    public static String replaceTableNameInInsert(String insertSQL, String newTableName) throws JSQLParserException {

        Insert insertSQLObj = (Insert) CCJSqlParserUtil.parse(insertSQL);

        Table newTable = new Table();
        newTable.setName(newTableName);

        insertSQLObj.setTable(newTable);

        return insertSQLObj.toString();
    }

    public static String replaceTableNameInDelete(String deleteSQL, String newTableName) throws JSQLParserException {

        Delete deleteSQLObj = (Delete) CCJSqlParserUtil.parse(deleteSQL);

        Table newTable = new Table();
        newTable.setName(newTableName);

        deleteSQLObj.setTable(newTable);

        return deleteSQLObj.toString();
    }

    public static String replaceTableNameInUpdate(String updateSQL, String oldTableName, String newTableName)
            throws JSQLParserException {

        Update updateSQLObj = (Update) CCJSqlParserUtil.parse(updateSQL);

        Table table = updateSQLObj.getTable();
        if (table.getName().equals(oldTableName)) {
            table.setName(newTableName);
        }
        return updateSQLObj.toString();
    }

}

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
package com.minsait.onesait.platform.persistence.presto.generator;

import java.util.List;

import com.minsait.onesait.platform.config.model.OntologyPresto;
import com.minsait.onesait.platform.config.repository.OntologyPrestoRepository;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class PrestoSQLTableReplacer {
	
    public static String replaceTableNameInSelect(String querySQL, OntologyPrestoRepository ontologyPrestoRepository, 
    		List<String> excludeParse, String ontology)
            throws JSQLParserException {
    	
    	Statement statement = CCJSqlParserUtil.parse(querySQL);
    	if (statement instanceof Insert) {
    		return replaceTableNameInInsert(querySQL, ontologyPrestoRepository, excludeParse, ontology);
    	}
        Select select = (Select) CCJSqlParserUtil.parse(querySQL);

        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {
            @Override
            public void visit(Table tableName) {
            	
                OntologyPresto OntologyPresto = ontologyPrestoRepository
                        .findOntologyPrestoByOntologyIdentification(tableName.getName());
                
            	if(OntologyPresto != null ) {
            		String ontologyPath = PrestoSQLTableReplacer.getOntologyPath(OntologyPresto); 
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
	                } else {
	                	
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

    public static String replaceTableNameInInsert(String insertSQL, OntologyPrestoRepository ontologyPrestoRepository, 
    		List<String> excludeParse, String ontology) throws JSQLParserException {
		
    	final OntologyPresto ontologyPresto = ontologyPrestoRepository.findOntologyPrestoByOntologyIdentification(ontology);
    	String newTablePath = PrestoSQLTableReplacer.getOntologyPath(ontologyPresto);
  
        Insert insertSQLObj = (Insert) CCJSqlParserUtil.parse(insertSQL);

        Table newTable = new Table();
        newTable.setName(newTablePath);

        insertSQLObj.setTable(newTable);
        
        if (insertSQLObj.getSelect() != null) {
        	String selectSQL = replaceTableNameInSelect(insertSQLObj.getSelect().toString(), ontologyPrestoRepository, excludeParse, ontology);
        	Select selectSQLObj = (Select) CCJSqlParserUtil.parse(selectSQL);
        	insertSQLObj.setSelect(selectSQLObj);
        }
        
        return insertSQLObj.toString();
    }

    public static String replaceTableNameInDelete(String deleteSQL, OntologyPresto OntologyPresto) throws JSQLParserException {
    	String newTablePath = PrestoSQLTableReplacer.getOntologyPath(OntologyPresto);
    	
        Delete deleteSQLObj = (Delete) CCJSqlParserUtil.parse(deleteSQL);

        Table newTable = new Table();
        newTable.setName(newTablePath);

        deleteSQLObj.setTable(newTable);

        return deleteSQLObj.toString();
    }

    public static String replaceTableNameInUpdate(String updateSQL, String oldTableName, OntologyPresto OntologyPresto)
            throws JSQLParserException {
    	String newTablePath = PrestoSQLTableReplacer.getOntologyPath(OntologyPresto);
    	
        Update updateSQLObj = (Update) CCJSqlParserUtil.parse(updateSQL);

        Table table = updateSQLObj.getTable();
        if (table.getName().equals(oldTableName)) {
            table.setName(newTablePath);
        }
        return updateSQLObj.toString();
    }
    
    private static String getOntologyPath(OntologyPresto OntologyPresto) {
    	String OntologyPrestoTable =  OntologyPresto.getDatasourceTableName();
		String OntologyPrestoCatalog =  OntologyPresto.getDatasourceCatalog();
		String OntologyPrestoSchema =  OntologyPresto.getDatasourceSchema();
		if (OntologyPrestoTable == null || "".equals(OntologyPrestoTable)) {
			return null;
		}

    	return (OntologyPrestoCatalog==null || "".equals(OntologyPrestoCatalog)?"":OntologyPrestoCatalog+".") + 
    			(OntologyPrestoSchema==null || "".equals(OntologyPrestoSchema)?"":OntologyPrestoSchema+".") +
    			OntologyPrestoTable;
    }

}

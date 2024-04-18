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
package com.minsait.onesait.platform.persistence.services.util;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

@Slf4j
public class DataAccessQueryProcessor {
	
	@Setter
	@Getter
	private String query;
	
	@Setter
	@Getter
	private Map<String, String> ontologiesMap;
	
	@Setter
	@Getter
	private String dataAccessQuery;
	
	@Setter
	@Getter
	private String ontology;
	
	@Setter
	@Getter
	private String alias;

	public DataAccessQueryProcessor(String query, Map<String, String> queryDataAccess) {
		super();
		this.ontologiesMap = queryDataAccess;
		this.query = query;
	}
	
	public DataAccessQueryProcessor(String query, Map<String, String> queryDataAccess, String ontology) {
		super();
		this.ontologiesMap = queryDataAccess;
		this.query = query;
		this.ontology = ontology;
	}
	
	public String process() throws JSQLParserException {

		Statement statement = CCJSqlParserUtil.parse(this.query);
		
		for (String  ontologyDataAccess : ontologiesMap.keySet()) {
			
			this.setOntology(ontologyDataAccess);
			
			if (statement instanceof Select) {
				this.setQuery(processSelect(statement));
			} else if (statement instanceof Insert) {
				this.setQuery(processInsert(statement));
			} else if (statement instanceof Update) {
				this.setQuery(processUpdate(statement));
			} else if (statement instanceof Delete) {
				this.setQuery(processDelete(statement));
			}
		}
	
		return this.query;
	}

	private String processSelect(Statement statement) throws JSQLParserException {
		Select select = (Select) statement;
		if (select.getSelectBody() instanceof SetOperationList) {

			for (int i = 0; i <  ((SetOperationList) select.getSelectBody()).getSelects().size(); i++) {
				DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((SetOperationList) select.getSelectBody()).getSelects().get(i).toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
				String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((SetOperationList) select.getSelectBody()).getSelects().get(i).toString()));
				((SetOperationList) select.getSelectBody()).getSelects().set(i, (PlainSelect)((Select)CCJSqlParserUtil.parse(subQueryReplaced)).getSelectBody());
			}
		} else {
			processSubQueries(select);
			
			processSelectAlias(select);

			replaceAliasInWhere();
			
			PlainSelect ps = (PlainSelect) select.getSelectBody();
				
			if ((this.dataAccessQuery!=null) && (ps.getFromItem() instanceof Table) && (((Table) ps.getFromItem()).getName().equalsIgnoreCase(this.ontology))) {
				Expression whereclause = ps.getWhere();
				if (whereclause!=null) {
					this.dataAccessQuery = "(" + ps.getWhere().toString() + ") AND " + this.dataAccessQuery;
				}
				
				ps.setWhere(CCJSqlParserUtil.parseCondExpression(this.dataAccessQuery));
				return ps.toString();
			}
		}
		return select.toString();
	}
	
	private String processInsert(Statement statement) throws JSQLParserException {
		//Insert insert = (Insert) statement;
		return statement.toString();
	}
	
	private String processUpdate(Statement statement) throws JSQLParserException {
		Update update = (Update) statement;
		
		processSubQueries(update);
		
		processUpdateAlias(update);

		replaceAliasInWhere();
			
		if ((this.dataAccessQuery!=null) && (update.getFromItem() instanceof Table) && (((Table) update.getFromItem()).getName().equalsIgnoreCase(this.ontology))) {
			Expression whereclause = update.getWhere();
			if (whereclause!=null) {
				this.dataAccessQuery = "(" + update.getWhere().toString() + ") AND " + this.dataAccessQuery;
			}
			
			update.setWhere(CCJSqlParserUtil.parseCondExpression(this.dataAccessQuery));
		}
		return update.toString();
	}
	
	private String processDelete(Statement statement) throws JSQLParserException {
		Delete delete = (Delete) statement;
		
		processSubQueries(delete);
		
		processDeleteAlias(delete);

		replaceAliasInWhere();
			
		if ((this.dataAccessQuery!=null) && (delete.getTable()instanceof Table) && (delete.getTable().getName().equalsIgnoreCase(this.ontology))) {
			Expression whereclause = delete.getWhere();
			if (whereclause!=null) {
				this.dataAccessQuery = "(" + delete.getWhere().toString() + ") AND " + this.dataAccessQuery;
			}
			
			delete.setWhere(CCJSqlParserUtil.parseCondExpression(this.dataAccessQuery));
		}
		
		return delete.toString();
	}

	private void processSubQueries(Select select) {
		StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser();
        SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {
            @Override
            public void visit(PlainSelect plainSelect) {
            	 plainSelect.getFromItem().accept(new FromItemVisitorAdapter() {
    			        @Override
    			        public void visit(SubSelect subSelect) {
			        		 DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((PlainSelect) subSelect.getSelectBody()).toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
			        		 try {
			        			 String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((PlainSelect) subSelect.getSelectBody()).toString()));

			        			 subSelect.setSelectBody((PlainSelect)((Select)CCJSqlParserUtil.parse(subQueryReplaced)).getSelectBody());
			        		 } catch (JSQLParserException e) {
			        			 log.error("Error processing SubQueries", e.getMessage());
			        		 }
    			        }});
            	 if (plainSelect.getJoins()!=null) {
            		 for (Join joinItem : plainSelect.getJoins()) {
            			 if (joinItem.getRightItem() instanceof SubSelect) {
            				 DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((PlainSelect) ((SubSelect)joinItem.getRightItem()).getSelectBody()).toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
            				 try {
            					 String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((PlainSelect) ((SubSelect)joinItem.getRightItem()).getSelectBody()).toString()));

	            				 ((SubSelect)joinItem.getRightItem()).setSelectBody((PlainSelect)((Select)CCJSqlParserUtil.parse(subQueryReplaced)).getSelectBody());
							} catch (JSQLParserException e) {
								log.error("Error processing Joins", e.getMessage());
							}
            			 } else if (joinItem.getRightItem() instanceof Table) {
            				 if (((Table)joinItem.getRightItem()).getName().equalsIgnoreCase(DataAccessQueryProcessor.this.getOntology()) && ((Table)joinItem.getRightItem()).getAlias()!=null) {
           						 DataAccessQueryProcessor.this.setAlias(((Table)joinItem.getRightItem()).getAlias().getName());
            				 }
            			 }
            		 }
            	 }
            	 if (plainSelect.getSelectItems()!=null) {
            		 for (SelectItem selectItem : plainSelect.getSelectItems()) {
            			 if (selectItem instanceof SelectExpressionItem) {
            				 DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((SelectExpressionItem) selectItem).getExpression().toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
            				 try {
            					 String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((SelectExpressionItem) selectItem).getExpression().toString()));

	            				 ((SelectExpressionItem) selectItem).setExpression(CCJSqlParserUtil.parseExpression(subQueryReplaced));
							} catch (JSQLParserException e) {
								log.error("Error processing Select items", e.getMessage());
							}

            			 }
            		 }
            	 }
            }
        };
		
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);
		
	}
	
	private void processSubQueries(Update update) {
		if (update.getExpressions()!=null) {
			for (int i = 0; i < update.getExpressions().size(); i++) {
				if (update.getExpressions().get(i) instanceof SubSelect) {
	        		 DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((PlainSelect) ((SubSelect) update.getExpressions().get(i)).getSelectBody()).toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
	        		 try {
	        			 String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((PlainSelect) ((SubSelect) update.getExpressions().get(i)).getSelectBody()).toString()));

	        			 ((SubSelect) update.getExpressions().get(i)).setSelectBody((PlainSelect)((Select)CCJSqlParserUtil.parse(subQueryReplaced)).getSelectBody());
	        		 } catch (JSQLParserException e) {
	        			 log.error("Error processing SubQueries", e.getMessage());
	        		 }
				}
			}
		}
		
		if (update.getJoins()!=null) {
			for (int i = 0; i < update.getJoins().size(); i++) {
				if (update.getJoins().get(i).getRightItem() instanceof SubSelect) {
	        		 DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((PlainSelect) ((SubSelect) update.getJoins().get(i).getRightItem()).getSelectBody()).toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
	        		 try {
	        			 String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((PlainSelect) ((SubSelect) update.getJoins().get(i).getRightItem()).getSelectBody()).toString()));

	        			 ((SubSelect) update.getJoins().get(i).getRightItem()).setSelectBody((PlainSelect)((Select)CCJSqlParserUtil.parse(subQueryReplaced)).getSelectBody());
	        		 } catch (JSQLParserException e) {
	        			 log.error("Error processing Joins", e.getMessage());
	        		 }
				}
			}
		}
	}

	private void processSubQueries(Delete delete) {	
		if (delete.getJoins()!=null) {
			for (int i = 0; i < delete.getJoins().size(); i++) {
				if (delete.getJoins().get(i).getRightItem() instanceof SubSelect) {
	        		 DataAccessQueryProcessor subqueryProcessor = new DataAccessQueryProcessor(((PlainSelect) ((SubSelect) delete.getJoins().get(i).getRightItem()).getSelectBody()).toString(), DataAccessQueryProcessor.this.getOntologiesMap(), DataAccessQueryProcessor.this.getOntology());
	        		 try {
	        			 String subQueryReplaced = subqueryProcessor.processSelect(CCJSqlParserUtil.parse(((PlainSelect) ((SubSelect) delete.getJoins().get(i).getRightItem()).getSelectBody()).toString()));

	        			 ((SubSelect) delete.getJoins().get(i).getRightItem()).setSelectBody((PlainSelect)((Select)CCJSqlParserUtil.parse(subQueryReplaced)).getSelectBody());
	        		 } catch (JSQLParserException e) {
	        			 log.error("Error processing SubQueries", e.getMessage());
	        		 }
				}
			}
		}
	}

	private void processSelectAlias(Select select) {
		if (DataAccessQueryProcessor.this.getAlias()==null || DataAccessQueryProcessor.this.getAlias().equals("")) {
			if (!(((PlainSelect) select.getSelectBody()).getFromItem() instanceof SubSelect)){
				StringBuilder buffer = new StringBuilder();
				ExpressionDeParser expressionDeParser = new ExpressionDeParser();
				SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {
					@Override
					public void visit(Table tableName) {
						if (tableName.getName().equalsIgnoreCase(DataAccessQueryProcessor.this.getOntology()) && tableName.getAlias()!=null){
							DataAccessQueryProcessor.this.setAlias(tableName.getAlias().getName());
						}
					}           
				};
			
				expressionDeParser.setSelectVisitor(deparser);
				expressionDeParser.setBuffer(buffer);
				select.getSelectBody().accept(deparser);
			} else if (((PlainSelect) select.getSelectBody()).getFromItem().getAlias()!=null) {
				DataAccessQueryProcessor.this.setAlias(((PlainSelect) select.getSelectBody()).getFromItem().getAlias().getName());
			} 
		}
	}
	
	private void processUpdateAlias(Update update) {
		if (update.getTable().getName().equalsIgnoreCase(this.getOntology()) && update.getTable().getAlias()!=null){
			this.setAlias(update.getTable().getAlias().getName());
		}
	}
	
	private void processDeleteAlias(Delete delete) {
		if (delete.getTable().getName().equalsIgnoreCase(this.getOntology()) && delete.getTable().getAlias()!=null){
			this.setAlias(delete.getTable().getAlias().getName());
		}
	}
	
	private void replaceAliasInWhere() throws JSQLParserException {
		Expression where = CCJSqlParserUtil.parseCondExpression(this.getOntologiesMap().get(this.getOntology()));
		if (DataAccessQueryProcessor.this.getAlias()!=null) {
			where.accept(new ExpressionVisitorAdapter() {
		        @Override
		        protected void visitBinaryExpression(BinaryExpression expr) {
		            if (expr instanceof ComparisonOperator) {
		            	if (expr.getLeftExpression() instanceof Column) {
		            		try {
								expr.setLeftExpression(CCJSqlParserUtil.parseExpression(DataAccessQueryProcessor.this.getAlias() + "." + expr.getLeftExpression().toString()));
							} catch (JSQLParserException e) {
								log.error("Error processing Alias", e.getMessage());
							}
		            	}
		            }
		            super.visitBinaryExpression(expr); 
		        }
		    });
		}
		this.dataAccessQuery = where.toString();
	}

}

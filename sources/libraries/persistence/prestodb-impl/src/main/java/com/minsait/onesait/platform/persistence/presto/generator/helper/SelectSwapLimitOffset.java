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
package com.minsait.onesait.platform.persistence.presto.generator.helper;

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.WindowDefinition;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.MySqlSqlCacheFlags;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.Wait;
import net.sf.jsqlparser.statement.select.WithIsolation;
import static java.util.stream.Collectors.joining;

public class SelectSwapLimitOffset extends PlainSelect {
	
	private static final long serialVersionUID = 1L;
	private Distinct distinct = null;
    private List<SelectItem> selectItems;
    private List<Table> intoTables;
    private FromItem fromItem;
    private List<Join> joins;
    private Expression where;
    private GroupByElement groupBy;
    private List<OrderByElement> orderByElements;
    private Expression having;
    private Limit limit;
    private Offset offset;
    private Fetch fetch;
    private OptimizeFor optimizeFor;
    private Skip skip;
    private boolean mySqlHintStraightJoin;
    private First first;
    private Top top;
    private OracleHierarchicalExpression oracleHierarchical = null;
    private OracleHint oracleHint = null;
    private boolean oracleSiblings = false;
    private boolean forUpdate = false;
    private Table forUpdateTable = null;
    private boolean skipLocked;
    private boolean useBrackets = false;
    private Wait wait;
    private boolean mySqlSqlCalcFoundRows = false;
    private MySqlSqlCacheFlags mySqlCacheFlag = null;
    private String forXmlPath;
    private KSQLWindow ksqlWindow = null;
    private boolean noWait = false;
    private boolean emitChanges = false;
    private WithIsolation withIsolation;
    private List<WindowDefinition> windowDefinitions;
    
    public SelectSwapLimitOffset(PlainSelect ps) {
    	this.useBrackets = ps.isUseBrackets();
    	this.selectItems = ps.getSelectItems();
    	this.mySqlHintStraightJoin = ps.getMySqlHintStraightJoin();
    	this.oracleHint = ps.getOracleHint();
    	this.skip = ps.getSkip();
    	this.first = ps.getFirst();
    	this.distinct = ps.getDistinct();
    	this.top = ps.getTop();
    	this.mySqlCacheFlag = ps.getMySqlSqlCacheFlag();
    	this.mySqlSqlCalcFoundRows = ps.getMySqlSqlCalcFoundRows();
    	this.forXmlPath = ps.getForXmlPath();
    	this.intoTables = ps.getIntoTables();
    	this.fromItem = ps.getFromItem();
    	this.joins = ps.getJoins();
    	this.ksqlWindow = ps.getKsqlWindow();
    	this.where = ps.getWhere();
    	this.forXmlPath = ps.getForXmlPath();
    	this.oracleHierarchical = ps.getOracleHierarchical();
    	this.groupBy = ps.getGroupBy();
    	this.having = ps.getHaving();
    	this.windowDefinitions = ps.getWindowDefinitions();
    	this.oracleSiblings = ps.isOracleSiblings();
    	this.orderByElements = ps.getOrderByElements();
    	this.emitChanges = ps.isEmitChanges();
    	this.orderByElements = ps.getOrderByElements();
    	this.limit = ps.getLimit();
    	this.offset = ps.getOffset();
    	this.fetch = ps.getFetch();
    	this.withIsolation = ps.getWithIsolation();
    	this.forUpdate = ps.isForUpdate();
    	this.forUpdateTable = ps.getForUpdateTable();
    	this.wait = ps.getWait();
    	this.noWait = ps.isNoWait();
    	this.skipLocked = ps.isSkipLocked();
    	this.optimizeFor = ps.getOptimizeFor();
    } 

    @Override
    public String toString() {
    	StringBuilder sql = new StringBuilder();
        if (useBrackets) {
            sql.append("(");
        }
        sql.append("SELECT ");

        if (this.mySqlHintStraightJoin) {
            sql.append("STRAIGHT_JOIN ");
        }

        if (oracleHint != null) {
            sql.append(oracleHint).append(" ");
        }

        if (skip != null) {
            sql.append(skip).append(" ");
        }

        if (first != null) {
            sql.append(first).append(" ");
        }

        if (distinct != null) {
            sql.append(distinct).append(" ");
        }
        if (top != null) {
            sql.append(top).append(" ");
        }
        if (mySqlCacheFlag != null) {
            sql.append(mySqlCacheFlag.name()).append(" ");
        }
        if (mySqlSqlCalcFoundRows) {
            sql.append("SQL_CALC_FOUND_ROWS").append(" ");
        }
        sql.append(getStringList(selectItems));
        
        if (intoTables != null) {
            sql.append(" INTO ");
            for (Iterator<Table> iter = intoTables.iterator(); iter.hasNext();) {
                sql.append(iter.next().toString());
                if (iter.hasNext()) {
                    sql.append(", ");
                }
            }
        }

        if (fromItem != null) {
            sql.append(" FROM ").append(fromItem);
            if (joins != null) {
                Iterator<Join> it = joins.iterator();
                while (it.hasNext()) {
                    Join join = it.next();
                    if (join.isSimple()) {
                        sql.append(", ").append(join);
                    } else {
                        sql.append(" ").append(join);
                    }
                }
            }

            if (ksqlWindow != null) {
                sql.append(" WINDOW ").append(ksqlWindow.toString());
            }
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
            if (oracleHierarchical != null) {
                sql.append(oracleHierarchical.toString());
            }
            if (groupBy != null) {
                sql.append(" ").append(groupBy.toString());
            }
            if (having != null) {
                sql.append(" HAVING ").append(having);
            }

            if (windowDefinitions != null) {
                sql.append(" WINDOW ");
                sql.append(windowDefinitions.stream().map(WindowDefinition::toString).collect(joining(", ")));
            }

            sql.append(orderByToString(oracleSiblings, orderByElements));
            if (emitChanges) {
                sql.append(" EMIT CHANGES");
            }
            if (offset != null) {
                sql.append(offset);
            }
            if (limit != null) {
                sql.append(limit);
            }
            if (fetch != null) {
                sql.append(fetch);
            }

            if (withIsolation != null) {
                sql.append(withIsolation);
            }
            
            if (forUpdate) {
                sql.append(" FOR UPDATE");

                if (forUpdateTable != null) {
                    sql.append(" OF ").append(forUpdateTable);
                }

                if (wait != null) {
                    // Wait's toString will do the formatting for us
                    sql.append(wait);
                }

                if (noWait) {
                    sql.append(" NOWAIT");
                } else if (skipLocked) {
                    sql.append(" SKIP LOCKED");
                }
            }
            
            if (optimizeFor != null) {
                sql.append(optimizeFor);
            }
        } else {
            // without from
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
            if (offset != null) {
                sql.append(offset);
            }
            
            if (limit != null) {
                sql.append(limit);
            }
            
            if (fetch != null) {
                sql.append(fetch);
            }
            if (withIsolation != null) {
                sql.append(withIsolation);
            }
        }
        if (forXmlPath != null) {
            sql.append(" FOR XML PATH(").append(forXmlPath).append(")");
        }
        if (useBrackets) {
            sql.append(")");
        }
        return sql.toString();
    }

}

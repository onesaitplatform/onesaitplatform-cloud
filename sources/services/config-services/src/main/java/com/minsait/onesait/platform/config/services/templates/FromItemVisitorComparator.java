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
package com.minsait.onesait.platform.config.services.templates;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

public class FromItemVisitorComparator implements FromItemVisitor{
    
    private FromItem otherFromItem;
    private MatchResult result;
    
    public FromItemVisitorComparator(FromItem otherFromItem, MatchResult result) {
        this.otherFromItem = otherFromItem;
        this.result = result;
    }
    
    private void genericResponse(Object obj1, Object obj2) {
      //TODO in the future, more complex comparison will be required for each case.
      boolean match = obj1.toString().equals(obj2.toString());
      result.setResult(match);
    }
    
    @Override
    public void visit(Table tableName1) {
        genericResponse(tableName1, otherFromItem);
    }

    @Override
    public void visit(SubSelect subSelect1) {
    	
    	boolean sameClass = true;
    	SubSelect subSelect2 = null;
        try {
        	subSelect2 = (SubSelect) otherFromItem;
        } catch(ClassCastException e) {
            sameClass = false;
        }
        
        if (sameClass) {
        	subSelect1.getSelectBody().accept(new SelectVisitorComparator(subSelect2.getSelectBody(), result));          
        } else {
            result.setResult(false);
        }        
        //genericResponse(subSelect1, otherFromItem);
    }

    @Override
    public void visit(SubJoin subjoin1) {
        genericResponse(subjoin1, otherFromItem);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect1) {
        genericResponse(lateralSubSelect1, otherFromItem);
    }

    @Override
    public void visit(ValuesList valuesList1) {
        genericResponse(valuesList1, otherFromItem);
    }

    @Override
    public void visit(TableFunction tableFunction1) {
        boolean sameClass = true;
        TableFunction tableFunction2 = null;
        try {
            tableFunction2 = (TableFunction) otherFromItem;
        } catch(ClassCastException e) {
            sameClass = false;
        }
        
        if (sameClass) {
            SqlComparator.matchTableFunction(tableFunction1, tableFunction2, result);            
        } else {
            result.setResult(false);
        }
    }

    @Override
    public void visit(ParenthesisFromItem aThis1) {
        genericResponse(aThis1, otherFromItem);
    }

}

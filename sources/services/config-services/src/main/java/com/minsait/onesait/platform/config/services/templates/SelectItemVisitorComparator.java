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

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

public class SelectItemVisitorComparator implements SelectItemVisitor {
    
    SelectItem otherSelectItem;
    MatchResult result;

    public SelectItemVisitorComparator(SelectItem otherSelectItem, MatchResult result) {
        this.otherSelectItem = otherSelectItem;
        this.result = result;
    }
    
    @Override
    public void visit(AllColumns allColumns1) {
        boolean sameClass = true;
        AllColumns allColumns2 = null;
        try {
            allColumns2 = (AllColumns) otherSelectItem;
        } catch(ClassCastException e) {
            sameClass = false;
        }
        
        if (sameClass) {
            SqlComparator.matchAllColumns(allColumns1, allColumns2, result);
        } else {
            result.setResult(false);
        }
    }

    @Override
    public void visit(AllTableColumns allTableColumns1) {
        boolean sameClass = true;
        AllTableColumns allTableColumns2 = null;
        try {
            allTableColumns2 = (AllTableColumns) otherSelectItem;
        } catch(ClassCastException e) {
            sameClass = false;
        }
        
        if (sameClass) {
            SqlComparator.matchAllTableColumns(allTableColumns1, allTableColumns2, result);
        } else {
            result.setResult(false);
        }
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem1) {
        boolean sameClass = true;
        SelectExpressionItem selectExpressionItem2 = null;
        try {
            selectExpressionItem2 = (SelectExpressionItem) otherSelectItem;
        } catch(ClassCastException e) {
            sameClass = false;
        }
        
        if (sameClass) {
            SqlComparator.matchSelectExpressionItem(selectExpressionItem1, selectExpressionItem2, result);
        } else {
            result.setResult(false);
        }
    }

}
